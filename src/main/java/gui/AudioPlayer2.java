package gui;

import network.Component;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.*;

public class AudioPlayer2 {
    private Thread workThread = null;
    private ConcurrentLinkedQueue<AudioCommand> commandQueue = new ConcurrentLinkedQueue<>();
    SourceDataLine dataLine;
    private double volume = 1.0;
    private Component selectedComponent = null;
    private ArrayList<Double> simulatedAngularFrequencies = null;
    private boolean isPlaying = false;
    Object dataLineMutexObj = new Object();
    ConcurrentLinkedQueue<byte[]> bufferQueue = new ConcurrentLinkedQueue<>();

    abstract class AudioCommand
    {
        public abstract void execute();
    }

    class NewComponentCommand extends AudioCommand {
        private Component newComp;
        private ArrayList<Double> omegas;

        public NewComponentCommand(Component c)
        {
            if (null == c)
            {
                newComp = null;
                omegas = null;
                return;
            }
            try {
                newComp = c.clone();
                int originalN = c.getParent().getSimulatedAngularFrequencies().size();
                // "Low pass" filter:
                int n = Math.min(originalN, 50);
                omegas = new ArrayList<>(n);
                for (Double omega : c.getParent().getSimulatedAngularFrequencies()) {
                    omegas.add(omega);
                }
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void execute()
        {
            selectedComponent = newComp;
            simulatedAngularFrequencies = omegas;
            synchronized (dataLineMutexObj)
            {
                dataLine.flush();
                bufferQueue.clear();
                bufferQueue.clear();
            }
        }
    }

    public void setSelectedComponent(Component c)
    {
        commandQueue.add(new NewComponentCommand(c));
    }

    class SetVolumeCommand extends AudioCommand {
        private double newVolume;

        public SetVolumeCommand(double v)
        {
            newVolume = v;
        }

        @Override
        public void execute()
        {
            volume = newVolume;
        }
    }

    public void setVolume(double v) {
        commandQueue.add(new SetVolumeCommand(v));
    }

    class StartPlaybackCommand extends AudioCommand {

        @Override
        public void execute()
        {
            isPlaying = true;
            synchronized (dataLineMutexObj)
            {
                dataLine.flush();
                dataLine.start();
                bufferQueue.clear();
            }
        }
    }

    public void startPlayback()
    {
        commandQueue.add(new StartPlaybackCommand());
    }

    class StopPlaybackCommand extends AudioCommand {

        @Override
        public void execute()
        {
            isPlaying = false;
            synchronized (dataLineMutexObj)
            {
                dataLine.flush();
                dataLine.stop();
                bufferQueue.clear();
            }
        }
    }

    public void stopPlayback()
    {
        commandQueue.add(new StopPlaybackCommand());
    }

    class PausePlaybackCommand extends AudioCommand {

        @Override
        public void execute()
        {
            isPlaying = false;
            synchronized (dataLineMutexObj)
            {
                dataLine.flush();
                dataLine.stop();
                bufferQueue.clear();
            }
        }
    }

    public void pausePlayback()
    {
        commandQueue.add(new PausePlaybackCommand());
    }

    private static class MyLineListener implements LineListener {
        @Override
        public void update(LineEvent event) {
            // TODO
        }
    }

    public void initializeWorkThread()
    {
        workThread = new Thread(
                () -> { this.playbackLoop(); }
        );
        workThread.start();
    }

    public void playbackLoop() {
        int sampleRateHz = 44100;
        int shortByteSize = 2;
        double timeStepSec = 1.0 / sampleRateHz;
        int dataPointsPerBuffer = 2048 / shortByteSize;
        double playBackSpeed = 1.0;

        // Build audio buffer:
        double minOriginal = -1.0;
        double maxOriginal = 1.0;
        short minTarget = Short.MIN_VALUE;
        short maxTarget = Short.MAX_VALUE;

        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,   // Encoding type
                sampleRateHz,                        // Sample rate (in Hz)
                8 * shortByteSize,                 // Sample size in bits
                1,                                 // Channels (1 for mono, 2 for stereo)
                shortByteSize,                     // Frame size (in bytes)
                sampleRateHz,                        // Frame rate (usually the same as the sample rate)
                true                              // Big-endian (true for big-endian, false for little-endian)
        );

        try {
            dataLine = AudioSystem.getSourceDataLine(format);
            dataLine.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        Object lockObj = new Object();
        Thread dataExporterThread = new Thread(
                () -> {
                    while(true) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        else if (!bufferQueue.isEmpty()) {
                            byte[] buffer = bufferQueue.poll();
                            if (null != buffer) {
                                synchronized (dataLineMutexObj)
                                {
                                    dataLine.write(buffer, 0, dataPointsPerBuffer * shortByteSize);
                                }
                            }
                        }
                        else {
                            synchronized (lockObj)
                            {
                                try {
                                    lockObj.wait();
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }
                        }
                    }
                }
        );
        dataExporterThread.start();

        double timeSec = 0;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            ByteBuffer buffer = ByteBuffer.allocate(dataPointsPerBuffer * shortByteSize);
            for (int n = 0; n < dataPointsPerBuffer; n++) {
                while (!commandQueue.isEmpty()) {
                    AudioCommand command = commandQueue.poll();
                    if (null != command) {
                        command.execute();
                    }
                }
                if (isPlaying && null != selectedComponent && null != simulatedAngularFrequencies) {
                    selectedComponent.updateTimeDomainParameters(timeSec, simulatedAngularFrequencies);
                    double sample = Math.max(Math.min(volume * selectedComponent.getTimeDomainVoltageDrop(), 1.0), -1.0);
                    double remappedSample = (sample - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget;
                    buffer.putShort((short)remappedSample);
                    timeSec += timeStepSec * playBackSpeed;
                }
                else {
                    buffer.putShort((short) 0);
                }
            }

            bufferQueue.add(buffer.array());
            if (bufferQueue.size() > 2) {
                synchronized (lockObj)
                {
                    lockObj.notify();
                }
            }
        }

        dataExporterThread.interrupt();
        try {
            dataExporterThread.join();
        } catch (InterruptedException e) {
            //
        } finally {
            dataLine.flush();
            dataLine.close();
        }
    }


    public void joinWorkThread() {
        if (null != workThread && workThread.isAlive()) {
            workThread.interrupt();
            try {
                workThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        commandQueue.clear();
    }
}
