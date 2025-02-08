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
    private double volume = 1.0;
    private Component selectedComponent = null;
    private ArrayList<Double> simulatedAngularFrequencies = null;
    private double periodTimeSec = 1;
    private boolean isPlaying = false;
    private boolean generateSamples = true;

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
                periodTimeSec = -1;
                for (Double omega : c.getParent().getSimulatedAngularFrequencies()) {
                    omegas.add(omega);
                    if (periodTimeSec == -1 && omega != 0.0) {
                        periodTimeSec = 2 * Math.PI / omega;
                    }
                }
                if (periodTimeSec == -1) {
                    periodTimeSec = 0.1;
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
            generateSamples = true;
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
            generateSamples = true;
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

        SourceDataLine dataLine;
        try {
            dataLine = AudioSystem.getSourceDataLine(format);
            dataLine.open();
            dataLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buffer = null;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            while (!commandQueue.isEmpty()) {
                AudioCommand command = commandQueue.poll();
                if (null != command) {
                    command.execute();
                }
            }
            if (isPlaying) {
                if (generateSamples) {
                    generateSamples = false;
                    int dataPointsPerBuffer = (int)(periodTimeSec * sampleRateHz);
                    System.out.println("sample / buffer = " + dataPointsPerBuffer);
                    buffer = ByteBuffer.allocate(dataPointsPerBuffer * shortByteSize);
                    dataLine.flush();
                    buffer.clear();
                    double timeSec = 0;
                    for (int n = 0; n < dataPointsPerBuffer; n++) {
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
                }
                System.out.println("Writing buffer");
                dataLine.write(buffer.array(), 0, buffer.array().length);
            }
        }

        dataLine.flush();
        dataLine.close();
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
