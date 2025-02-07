package gui;
import network.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.sound.sampled.*;

import static java.lang.Thread.sleep;

public class AudioPlayer {
    private static SourceDataLine dataLine = null;
    private static double startingTimeSec = 0.0;
    private static final Object dataLineMutexObj = new Object();
    private static final Object threadCollectionManipulationMutexObj = new Object();
    private static final Object idxMutexObj = new Object();
    private static final ArrayList<Thread> workThreads = new ArrayList<>();
    private static int nextThreadIndex = 0;
    private static int lastCompleteThreadIdx = -1;
    private static double volume = 1.0;
    private static final Object volumeMutexObj = new Object();

    private static class MyLineListener implements LineListener {
        @Override
        public void update(LineEvent event) {
        }
    }

    public static void playVoltage(Component selectedComponent) {
        if (selectedComponent == null) {
            return;
        }
        synchronized (threadCollectionManipulationMutexObj)
        {
            if (workThreads.size() < 10)
            {
                int sampleRateHz = 44100;
                int shortByteSize = 2;
                double timeStepSec = 1.0 / sampleRateHz;
                int dataPoints = 8196 / shortByteSize;
                double playBackSpeed = 1.0;
                boolean isFadeIn = (0 == nextThreadIndex);
                try {
                    Component clonedComponent = selectedComponent.clone();
                    ArrayList<Double> clonedOmegas = (ArrayList<Double>)(selectedComponent.getParent().getSimulatedAngularFrequencies().clone());
                    int threadIdx = nextThreadIndex++;
                    Thread workThread = new Thread(() -> {
                        playVoltageSubroutine(threadIdx, clonedComponent, clonedOmegas, sampleRateHz, shortByteSize, timeStepSec, dataPoints, startingTimeSec, playBackSpeed, isFadeIn);
                    });
                    startingTimeSec += dataPoints * timeStepSec;
                    workThread.start();
                    workThreads.add(workThread);
                }
                catch (CloneNotSupportedException e)
                {
                    System.err.println("Component can not be cloned!");
                }
            }
            for (int i = 0; i < workThreads.size(); i++)
            {
                if (!workThreads.get(i).isAlive()) {
                    workThreads.remove(i);
                }
            }
        }
    }

    private static void playVoltageSubroutine(int threadIdx, Component selectedComponent, ArrayList<Double> omegas, int sampleRateHz, int shortByteSize, double timeStepSec, int dataPoints, double startingTime, double playBackSpeed, boolean isFadeIn) {
        // Build audio buffer:
        double minOriginal = -1.0;
        double maxOriginal = 1.0;
        short minTarget = Short.MIN_VALUE;
        short maxTarget = Short.MAX_VALUE;

        ByteBuffer buffer = ByteBuffer.allocate(dataPoints * shortByteSize);
        boolean isFadeOut = false;
        int fadeOutStartIdx = 0;
        for (int n = 0; n < dataPoints; n++) {
            if (Thread.currentThread().isInterrupted()) {
                isFadeOut = true;
                fadeOutStartIdx = n;
            }
            selectedComponent.updateTimeDomainParameters(startingTime + n * timeStepSec * playBackSpeed, omegas);
            double v = volume;
            if (isFadeIn) {
                v = (n + 1) / (double)dataPoints * volume;
            }
            if (isFadeOut) {
                v = (1.0 - (n - fadeOutStartIdx) / (double)(dataPoints - fadeOutStartIdx)) * volume;
            }
            double value = Math.max(Math.min(v * selectedComponent.getTimeDomainVoltageDrop(), 1.0), -1.0);
            double remapped = (value - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget;
            buffer.putShort((short)remapped);
        }
        try {
            while (true) {  // Wait for correct order
                synchronized (idxMutexObj)
                {
                    if (lastCompleteThreadIdx == threadIdx - 1) {
                        break;
                    }
                }
                if (Thread.currentThread().isInterrupted())
                {
                    return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            synchronized (dataLineMutexObj) {
                if (null == dataLine) {
                    AudioFormat format = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,   // Encoding type
                            sampleRateHz,                        // Sample rate (in Hz)
                            8 * shortByteSize,                 // Sample size in bits
                            1,                                 // Channels (1 for mono, 2 for stereo)
                            shortByteSize,                     // Frame size (in bytes)
                            sampleRateHz,                        // Frame rate (usually the same as the sample rate)
                            true                              // Big-endian (true for big-endian, false for little-endian)
                    );
                    dataLine = AudioSystem.getSourceDataLine(format);
                    dataLine.open();
                    dataLine.start();
                }
                dataLine.write(buffer.array(), 0, dataPoints * shortByteSize);
            }
            synchronized (idxMutexObj)
            {
                lastCompleteThreadIdx++;
            }
        } catch (LineUnavailableException e) {
                System.err.println("Audio line is unavailable.");
        }
    }

    public static void resetAudioPlayback()
    {
        startingTimeSec = 0.0;
        closeStream();
    }

    public static void pauseAudioPlayback() {
        closeStream();
    }

    public static void closeStream() {
        Thread closeThread = new Thread(
            () -> {
                synchronized (threadCollectionManipulationMutexObj)
                {
                    for (Thread thread : workThreads) {
                        thread.interrupt();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    workThreads.clear();
                    synchronized (dataLineMutexObj) {
                        if (null != dataLine)
                        {
                            dataLine.stop();
                            dataLine.close();
                            dataLine = null;
                        }
                    }
                    nextThreadIndex = 0;
                    lastCompleteThreadIdx = -1;
                }
            }
        );
        closeThread.start();
    }
}
