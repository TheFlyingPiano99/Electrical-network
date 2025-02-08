package gui;

import network.Component;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;

public class AudioPlayer2 {
    private Thread workThread = null;
    private ConcurrentLinkedQueue<AudioCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private double masterVolume = 1.0;
    private Component selectedComponent = null;
    private ArrayList<Double> simulatedAngularFrequencies = null;
    private double periodTimeSec = 1;
    private boolean isPlaying = false;
    private boolean generateSamples = true;
    private final double lowestAudibleFrequencyHz = 31.0;
    private final double highestAudibleFrequencyHz = 19000.0;
    private boolean fadeIn = false;
    private boolean fadeOut = false;
    private PlaybackMode mode = PlaybackMode.CURRENT;

    enum PlaybackMode {
        VOLTAGE_DROP,
        CURRENT,
        INPUT_POTENTIAL,
        OUTPUT_POTENTIAL
    }

    abstract class AudioCommand
    {
        public abstract void execute();
    }

    class NewComponentCommand extends AudioCommand {
        private Component newComp;
        private ArrayList<Double> omegas;
        private double newPeriodTimeSec;

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
                int n = Math.min(originalN, 100);
                omegas = new ArrayList<>(n);
                newPeriodTimeSec = -1;
                for (Double omega : c.getParent().getSimulatedAngularFrequencies()) {
                    omegas.add(omega);
                    if (newPeriodTimeSec == -1 && omega > 0.0) {
                        newPeriodTimeSec = 2 * Math.PI / omega;
                    }
                    if (omegas.size() == n){
                        break;
                    }
                }
                if (-1 == newPeriodTimeSec) {
                    newComp = null;
                    omegas = null;
                    newPeriodTimeSec = 1 / highestAudibleFrequencyHz;
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
            periodTimeSec = newPeriodTimeSec;
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
            masterVolume = newVolume;
        }
    }

    public void setMasterVolume(double v) {
        commandQueue.add(new SetVolumeCommand(v));
    }

    class StartPlaybackCommand extends AudioCommand {

        @Override
        public void execute()
        {
            isPlaying = true;
            fadeIn = true;
            fadeOut = false;
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
            fadeIn = false;
            fadeOut = true;
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
            fadeIn = false;
            fadeOut = true;
        }
    }

    public void pausePlayback()
    {
        commandQueue.add(new PausePlaybackCommand());
    }

    class SetPlaybackMode extends AudioCommand
    {
        private PlaybackMode newMode;

        public SetPlaybackMode(PlaybackMode m)
        {
            newMode = m;
        }

        @Override
        public void execute() {
            mode = newMode;
            generateSamples = true;
        }
    }

    public PlaybackMode toogleMode()
    {
        PlaybackMode newMode = mode;
        switch (mode)
        {
            case PlaybackMode.CURRENT -> newMode = PlaybackMode.VOLTAGE_DROP;
            case PlaybackMode.VOLTAGE_DROP -> newMode = PlaybackMode.CURRENT;
            //case PlaybackMode.INPUT_POTENTIAL -> newMode = PlaybackMode.OUTPUT_POTENTIAL;
            //case PlaybackMode.OUTPUT_POTENTIAL -> newMode = PlaybackMode.CURRENT;
        }
        commandQueue.add(new SetPlaybackMode(newMode));
        return newMode;
    }

    public void setPlaybackMode(PlaybackMode m)
    {
        commandQueue.add(new SetPlaybackMode(m));
    }

    public void initializeWorkThread()
    {
        workThread = new Thread(
                () -> { this.playbackLoop(); }
        );
        workThread.start();
    }

    private double filterSample(double sample, LinkedList<Double> previousSamples){
        double sum = 0;
        for (Double prev : previousSamples) {
            sum += prev;
        }
        sum += sample;
        sample = sum / (previousSamples.size() + 1);
        previousSamples.push(sample);
        if (previousSamples.size() > 50) {
            previousSamples.poll();
        }
        return sample;
    }

    public void playbackLoop() {
        int sampleRateHz = 44100;
        double timeStepSec = 1.0 / sampleRateHz;
        int shortByteSize = 2;
        double playBackSpeed = 1.0;
        int bufferSampleCount = 1024;
        int bufferSize = bufferSampleCount * shortByteSize;
        double currentVolume = 0.0;
        double fadeSpeed = 1.0;
        LinkedList<Double> previousSamples = new LinkedList<>();

        // Build audio buffer:
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

        int bufferSampleOffset = 0;
        int bufferByteOffset = 0;
        int samplesToReconstructPeriod = bufferSize;
        ArrayList<Double> sampleBuffer = null;
        ByteBuffer masteredBuffer = ByteBuffer.allocate(2 * bufferSize);
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            while (!commandQueue.isEmpty()) {
                AudioCommand command = commandQueue.poll();
                if (null != command) {
                    command.execute();
                }
            }

            if (generateSamples) {
                generateSamples = false;
                if (null != selectedComponent) {
                    bufferSampleOffset = 0;
                    bufferByteOffset = 0;
                    samplesToReconstructPeriod = (int)(periodTimeSec * sampleRateHz);
                    int precalculatedSampleCount = samplesToReconstructPeriod + bufferSampleCount;
                    System.out.println("samples presampled = " + precalculatedSampleCount + "period time: " + periodTimeSec);
                    sampleBuffer = new ArrayList<>(precalculatedSampleCount);
                    masteredBuffer = ByteBuffer.allocate(precalculatedSampleCount * shortByteSize);

                    double timeSec = 0;
                    for (int n = 0; n < precalculatedSampleCount; n++) {
                        if (null != selectedComponent && null != simulatedAngularFrequencies) {
                            selectedComponent.updateTimeDomainParameters(timeSec, simulatedAngularFrequencies);
                            switch (mode) {
                                case PlaybackMode.VOLTAGE_DROP: {
                                    sampleBuffer.add(selectedComponent.getTimeDomainVoltageDrop());
                                    break;
                                }
                                case PlaybackMode.CURRENT: {
                                    sampleBuffer.add(selectedComponent.getTimeDomainCurrent());
                                    break;
                                }
                                case PlaybackMode.INPUT_POTENTIAL: {
                                    break;
                                }
                                case PlaybackMode.OUTPUT_POTENTIAL: {
                                    break;
                                }
                            }
                            timeSec += timeStepSec * playBackSpeed;
                        }
                        else {
                            sampleBuffer.add(0.0);
                        }
                    }
                }
                else {
                    sampleBuffer = null;
                }
            }

            double minOriginal = -1.0;
            double maxOriginal = 1.0;
            short minTarget = Short.MIN_VALUE;
            short maxTarget = Short.MAX_VALUE;
            if (sampleBuffer != null && sampleBuffer.size() >= bufferSampleCount) {
                for (int i = 0; i < bufferSampleCount; i++) {
                    double sample = sampleBuffer.get(bufferSampleOffset + i);
                    if (fadeIn) {
                        fadeOut = false;
                        isPlaying = true;
                        currentVolume += timeStepSec * fadeSpeed;
                        if (currentVolume > 1) {
                            currentVolume = 1;
                            fadeIn = false;
                        }
                    }
                    else if (fadeOut) {
                        fadeIn = false;
                        currentVolume -= timeStepSec * fadeSpeed;
                        if (currentVolume < 0) {
                            currentVolume = 0;
                            fadeOut = false;
                            isPlaying = false;
                        }
                    }
                    else if (isPlaying) {
                        currentVolume = 1;
                    }
                    else {
                        currentVolume = 0;
                    }
                    sample = Math.max(Math.min(Math.pow(masterVolume * currentVolume, 3) * sample, 1.0), -1.0); // Clip peaks
                    sample = filterSample(sample, previousSamples);
                    short remappedSample = (short)((sample - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget);
                    masteredBuffer.putShort(bufferByteOffset + i * shortByteSize, remappedSample);
                }
            }
            else {
                for (int i = 0; i < bufferSampleCount; i++) {
                    double sample = filterSample(0, previousSamples);
                    short remappedSample = (short)((sample - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget);
                    masteredBuffer.putShort(bufferByteOffset + i * shortByteSize, remappedSample);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            dataLine.write(masteredBuffer.array(), bufferByteOffset, bufferSize);
            bufferSampleOffset = (bufferSampleOffset + bufferSampleCount) % samplesToReconstructPeriod;
            bufferByteOffset = (bufferByteOffset + bufferSize) % (samplesToReconstructPeriod * shortByteSize);
        }

        dataLine.flush();
        dataLine.close();
    }


    public void joinWorkThread() {
        if (null != workThread && workThread.isAlive()) {
            commandQueue.clear();
            workThread.interrupt();
            try {
                workThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
