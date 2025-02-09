package gui;

import network.Component;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;

public class AudioPlayer {
    private Thread workThread = null;
    private ConcurrentLinkedQueue<AudioCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private double masterVolume = 1.0;
    private Component selectedComponent = null;
    private ArrayList<Double> simulatedAngularFrequencies = null;
    private ArrayList<Integer> sampledFrequencyIndices = null;
    private double periodTimeSec = 1;
    private boolean isPlaying = false;
    private boolean generateSamples = true;
    private final double lowestAudibleFrequencyHz = 31.0;
    private final double highestAudibleFrequencyHz = 19000.0;
    private boolean fadeIn = false;
    private boolean fadeOut = false;
    private PlaybackMode mode = PlaybackMode.CURRENT;
    private boolean runWorkThread = true;

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
        private ArrayList<Double> newOmegas;
        private ArrayList<Integer> newSampledFrequencyIndices;
        private double newPeriodTimeSec;

        public NewComponentCommand(Component c)
        {
            if (null == c)
            {
                newComp = null;
                newOmegas = null;
                newSampledFrequencyIndices = null;
                newPeriodTimeSec = 0;
                return;
            }
            try {
                newComp = c.clone();
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
            newOmegas = newComp.getParent().getSimulatedAngularFrequencies();
            newSampledFrequencyIndices = new ArrayList<Integer>(200);

            // "Band pass" filtering the frequency components to the audible range:
            newPeriodTimeSec = -1;
            for (int i = 0; i < newOmegas.size(); i++) {
                double omega = newOmegas.get(i);
                if (omega > 2 * Math.PI * lowestAudibleFrequencyHz && omega < 2 * Math.PI * highestAudibleFrequencyHz) {
                    newSampledFrequencyIndices.add(i);
                    if (newSampledFrequencyIndices.size() > 200) {
                        break;
                    }
                    if (newPeriodTimeSec == -1) {   // Set to the first audible frequency
                        newPeriodTimeSec = 2 * Math.PI / omega;
                    }
                }
            }
            if (-1 == newPeriodTimeSec) {
                newComp = null;
                newOmegas = null;
                newSampledFrequencyIndices = null;
                newPeriodTimeSec = 0;
            }
        }

        @Override
        public void execute()
        {
            selectedComponent = newComp;
            simulatedAngularFrequencies = newOmegas;
            sampledFrequencyIndices = newSampledFrequencyIndices;
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

    public void initializePlayback()
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

    class TerminateCommand extends AudioCommand
    {

        @Override
        public void execute() {
            runWorkThread = false;
        }
    }

    private void processCommands()
    {
        while (!commandQueue.isEmpty()) {
            AudioCommand command = commandQueue.poll();
            if (null != command) {
                command.execute();
            }
        }
    }

    private double sampleData(double elapsedTimeSec)
    {
        selectedComponent.updateTimeDomainParametersUsingSpecificFrequencies(
                elapsedTimeSec,
                simulatedAngularFrequencies,
                sampledFrequencyIndices
        );
        switch (mode) {
            case PlaybackMode.VOLTAGE_DROP -> { return selectedComponent.getTimeDomainVoltageDrop(); }
            case PlaybackMode.CURRENT -> { return selectedComponent.getTimeDomainCurrent(); }
            case PlaybackMode.INPUT_POTENTIAL -> {}
            case PlaybackMode.OUTPUT_POTENTIAL -> {}
            default -> { return 0.0; }
        }
        return 0.0;
    }

    public void playbackLoop() {
        int sampleRateHz = 44100;
        double timeStepSec = 1.0 / sampleRateHz;
        int shortByteSize = 2;
        int bufferSampleCount = 512;
        double playBackSpeed = 1.0;
        double fadeSpeed = 4.0;
        double currentVolume = 0.0;
        int bufferSize = bufferSampleCount * shortByteSize;
        LinkedList<Double> previousSamples = new LinkedList<>();    // Accumulate for filtering

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
        ByteBuffer masteredByteBuffer = ByteBuffer.allocate(2 * bufferSize);
        while (runWorkThread) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            processCommands();

            if (generateSamples) {
                generateSamples = false;
                if (null != selectedComponent) {
                    bufferSampleOffset = 0;
                    bufferByteOffset = 0;
                    samplesToReconstructPeriod = (int)(periodTimeSec * sampleRateHz);
                    int precalculatedSampleCount = samplesToReconstructPeriod + bufferSampleCount;
                    sampleBuffer = new ArrayList<>(precalculatedSampleCount);
                    masteredByteBuffer = ByteBuffer.allocate(precalculatedSampleCount * shortByteSize);
                    double elapsedTimeSec = 0;
                    for (int n = 0; n < precalculatedSampleCount; n++) {
                        sampleBuffer.add(sampleData(elapsedTimeSec));
                        elapsedTimeSec += timeStepSec * playBackSpeed;
                    }
                }
                else {
                    sampleBuffer = null;
                }
            }

            // Fill mastered buffer:
            double minOriginal = -1.0;
            double maxOriginal = 1.0;
            short minTarget = Short.MIN_VALUE;
            short maxTarget = Short.MAX_VALUE;
            if (sampleBuffer != null) {
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
                    masteredByteBuffer.putShort(bufferByteOffset + i * shortByteSize, remappedSample);
                }
            }
            else {  // If no samples are available, we use zeros
                for (int i = 0; i < bufferSampleCount; i++) {
                    double filteredZeroSample = filterSample(0, previousSamples);
                    short remappedSample = (short)((filteredZeroSample - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget);
                    masteredByteBuffer.putShort(bufferByteOffset + i * shortByteSize, remappedSample);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Export mastered buffer through sound API:
            dataLine.write(masteredByteBuffer.array(), bufferByteOffset, bufferSize);   // NOTE: For continuous audio, it is good to reuse the same buffer
            // Increment buffer offsets:
            bufferSampleOffset = (bufferSampleOffset + bufferSampleCount) % samplesToReconstructPeriod;
            bufferByteOffset = (bufferByteOffset + bufferSize) % (samplesToReconstructPeriod * shortByteSize);
        }

        dataLine.flush();
        dataLine.close();
    }


    public void terminatePlayback() {
        if (null != workThread && workThread.isAlive()) {
            commandQueue.clear();
            commandQueue.add(new TerminateCommand());
            try {
                workThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
