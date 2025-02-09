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
    private double targetMasterVolume = 1.0;
    private Component selectedComponent = null;
    private ArrayList<Double> simulatedAngularFrequencies = null;   // All the angular frequencies simulated by the network
    private ArrayList<Integer> sampledFrequencyIndices = null;      // The angular frequencies indices selected to create audio
    private Component previousSelectedComponent = null;
    private ArrayList<Double> previousSimulatedAngularFrequencies = null;
    private ArrayList<Integer> previousSampledFrequencyIndices = null;
    private double crossFadeParam = 0.0;
    private boolean isCrossFade = false;
    private boolean isPlaying = false;
    private final double lowestAudibleFrequencyHz = 31.0;
    private final double highestAudibleFrequencyHz = 19000.0;
    private boolean fadeIn = false;
    private boolean fadeOut = false;
    private PlaybackMode mode = PlaybackMode.CURRENT;
    private PlaybackMode previousMode = PlaybackMode.CURRENT;
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

    class SetComponentCommand extends AudioCommand {
        private Component newComp;
        private ArrayList<Double> newOmegas;
        private ArrayList<Integer> newSampledFrequencyIndices;

        public SetComponentCommand(Component c)
        {
            if (null == c)
            {
                newComp = null;
                newOmegas = null;
                newSampledFrequencyIndices = null;
                return;
            }
            synchronized (c.getParent().getMutexObj())
            {
                try {
                    newComp = c.clone();
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
                newOmegas = (ArrayList<Double>)c.getParent().getSimulatedAngularFrequencies().clone();
                newSampledFrequencyIndices = new ArrayList<Integer>(200);

                // "Band pass" filtering the frequency components to the audible range:
                for (int i = 0; i < newOmegas.size(); i++) {
                    double omega = newOmegas.get(i);
                    if (omega > 2 * Math.PI * lowestAudibleFrequencyHz && omega < 2 * Math.PI * highestAudibleFrequencyHz) {
                        newSampledFrequencyIndices.add(i);
                        if (newSampledFrequencyIndices.size() == 200) {
                            break;
                        }
                    }
                }
                if (newSampledFrequencyIndices.isEmpty()) {
                    newComp = null;
                    newOmegas = null;
                    newSampledFrequencyIndices = null;
                }
            }
        }

        @Override
        public void execute()
        {
            if (!isCrossFade) { // Only change previous state if no cross-fade is happening to prevent click
                previousSelectedComponent = selectedComponent;
                previousSimulatedAngularFrequencies = simulatedAngularFrequencies;
                previousSampledFrequencyIndices = sampledFrequencyIndices;
                previousMode = mode;
                crossFadeParam = 0.0;
                isCrossFade = true;
            }

            selectedComponent = newComp;
            simulatedAngularFrequencies = newOmegas;
            sampledFrequencyIndices = newSampledFrequencyIndices;

        }
    }

    public void setSelectedComponent(Component c)
    {
        commandQueue.add(new SetComponentCommand(c));
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
            targetMasterVolume = newVolume;
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
            if (!isCrossFade)
            {
                previousSelectedComponent = selectedComponent;
                previousSimulatedAngularFrequencies = simulatedAngularFrequencies;
                previousSampledFrequencyIndices = sampledFrequencyIndices;
                previousMode = mode;
                isCrossFade = true;
                crossFadeParam = 0.0;
            }

            mode = newMode;
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

    private double sampleData(double elapsedTimeSec, Component component, ArrayList<Double> omegas, ArrayList<Integer> indices, PlaybackMode playbackMode)
    {
        component.updateTimeDomainParametersUsingSpecificFrequencies(
                elapsedTimeSec,
                omegas,
                indices
        );
        switch (playbackMode) {
            case PlaybackMode.VOLTAGE_DROP -> { return component.getTimeDomainVoltageDrop(); }
            case PlaybackMode.CURRENT -> { return component.getTimeDomainCurrent(); }
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
        double crossFadeSpeed = 6.0;
        double faderVolume = 0.0;
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

        double elapsedTimeSec = 0;
        int bufferByteOffset = 0;
        ByteBuffer masteredByteBuffer = ByteBuffer.allocate(4 * bufferSize);
        while (runWorkThread) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            processCommands();

            // Fill mastered buffer:
            double minOriginal = -1.0;
            double maxOriginal = 1.0;
            short minTarget = Short.MIN_VALUE;
            short maxTarget = Short.MAX_VALUE;
            for (int i = 0; i < bufferSampleCount; i++) {
                double sample = 0;
                if (isCrossFade) {
                    if (null != previousSelectedComponent) {
                        sample = (1.0 - crossFadeParam) * sampleData(elapsedTimeSec * playBackSpeed,
                                previousSelectedComponent,
                                previousSimulatedAngularFrequencies,
                                previousSampledFrequencyIndices,
                                previousMode
                        );
                    }
                    if (null != selectedComponent) {
                        sample += crossFadeParam * sampleData(elapsedTimeSec * playBackSpeed,
                                selectedComponent,
                                simulatedAngularFrequencies,
                                sampledFrequencyIndices,
                                mode
                        );
                    }

                    crossFadeParam += timeStepSec * crossFadeSpeed;
                    if (crossFadeParam > 1.0) {
                        crossFadeParam = 1.0;
                        isCrossFade = false;
                    }
                }
                else {
                    if (null != selectedComponent) {
                        sample = sampleData(elapsedTimeSec * playBackSpeed,
                                selectedComponent,
                                simulatedAngularFrequencies,
                                sampledFrequencyIndices,
                                mode
                        );
                    }
                }

                // Gradually change fader volume:
                if (fadeIn) {
                    fadeOut = false;
                    isPlaying = true;
                    faderVolume += timeStepSec * fadeSpeed;
                    if (faderVolume > 1) {
                        faderVolume = 1;
                        fadeIn = false;
                    }
                }
                else if (fadeOut) {
                    fadeIn = false;
                    faderVolume -= timeStepSec * fadeSpeed;
                    if (faderVolume < 0) {
                        faderVolume = 0;
                        fadeOut = false;
                        isPlaying = false;
                    }
                }

                // Gradually change master volume:
                if (targetMasterVolume < masterVolume) {
                    masterVolume -= timeStepSec * fadeSpeed;
                    if (targetMasterVolume > masterVolume) {
                        masterVolume = targetMasterVolume;
                    }
                }
                else if (targetMasterVolume > masterVolume) {
                    masterVolume += timeStepSec * fadeSpeed;
                    if (targetMasterVolume < masterVolume) {
                        masterVolume = targetMasterVolume;
                    }
                }

                sample = Math.max(Math.min(Math.pow(masterVolume * faderVolume, 3) * sample, 1.0), -1.0); // Clip peaks
                sample = filterSample(sample, previousSamples);
                short remappedSample = (short)((sample - minOriginal) * (maxTarget - minTarget) / (maxOriginal - minOriginal) + minTarget);
                masteredByteBuffer.putShort(bufferByteOffset + i * shortByteSize, remappedSample);
                elapsedTimeSec += timeStepSec;
            }

            // Export mastered buffer through sound API:
            dataLine.write(masteredByteBuffer.array(), bufferByteOffset, bufferSize);   // NOTE: For continuous audio, it is good to reuse the same buffer
            // Increment buffer offsets:
            bufferByteOffset = (bufferByteOffset + bufferSize) % masteredByteBuffer.array().length;
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
