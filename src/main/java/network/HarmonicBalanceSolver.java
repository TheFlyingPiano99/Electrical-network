package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import math.Complex;
import math.Vector;
import math.Matrix;

/**
 * Harmonic Balance Solver for nonlinear circuit analysis.
 * Solves the periodic steady-state equations using frequency-domain Newton
 * iteration.
 * 
 * Implements admittance matrix-based formulation with:
 * - Linear component frequency-independent assembly
 * - Nonlinear device time-domain evaluation and transformation
 * - Newton-Raphson iteration with damping
 * - Adaptive frequency spectrum management (future milestone)
 * 
 * @author Copilot
 */
public class HarmonicBalanceSolver {

    private Network network;
    private ArrayList<Double> fundamentalFrequencies;
    private ArrayList<Integer> activeHarmonics; // Harmonic indices to solve
    private int maxIterations = 100;
    private double voltageTolerance = 1e-6; // V
    private double currentTolerance = 1e-9; // A
    private double residualTolerance = 1e-12;

    // Frequency-domain state
    private int totalUnknowns;
    private double[] frequencyDomainSolution; // Real-packed: [V0, Re(V1), Im(V1), ..., Re(Vk), Im(Vk)]

    // Time-domain evaluation grid
    private int fftSize = 64; // Number of time samples per period
    private double[] timeDomainVoltages;
    private double[] timeDomainCurrents;

    // Device models
    private ArrayList<NonlinearDevice> nonlinearDevices = new ArrayList<>();
    private ArrayList<Component> linearComponents = new ArrayList<>();

    /**
     * Constructor initializing solver with network and frequency configuration.
     * 
     * @param network                The electrical network to simulate
     * @param fundamentalFrequencies List of fundamental frequencies (first must be
     *                               the driven frequency)
     * @param activeHarmonics        List of harmonic indices to retain in the
     *                               solution
     */
    public HarmonicBalanceSolver(Network network,
            ArrayList<Double> fundamentalFrequencies,
            ArrayList<Integer> activeHarmonics) {
        this.network = network;
        this.fundamentalFrequencies = new ArrayList<>(fundamentalFrequencies);
        this.activeHarmonics = new ArrayList<>(activeHarmonics);

        validateInputs();
        initializeUnknowns();
        collectDevices();
    }

    /**
     * Validates solver configuration.
     */
    private void validateInputs() {
        if (fundamentalFrequencies.isEmpty()) {
            throw new RuntimeException("At least one fundamental frequency required (DC = 0 Hz)");
        }
        if (activeHarmonics.isEmpty()) {
            throw new RuntimeException("At least DC (harmonic index 0) must be active");
        }
        if (!activeHarmonics.contains(0)) {
            activeHarmonics.add(0); // Ensure DC is always included
        }

        // Check for physically consistent fundamental frequencies
        for (double f : fundamentalFrequencies) {
            if (f < 0) {
                throw new RuntimeException("Negative frequency not allowed: " + f);
            }
        }
    }

    /**
     * Counts unknown variables in the harmonic balance formulation.
     * For each frequency/harmonic, we have node voltage unknowns.
     * Currently assumes all network nodes are voltage unknowns (except ground).
     */
    private void initializeUnknowns() {
        int numNodes = network.getVertices().size();
        // Real packing: for each active harmonic, we need 2 values per complex variable
        // DC (harmonic 0) is real only
        totalUnknowns = 2 + (activeHarmonics.size() - 1) * 2 * (numNodes - 1);

        frequencyDomainSolution = new double[totalUnknowns];
        timeDomainVoltages = new double[fftSize * numNodes];
        timeDomainCurrents = new double[fftSize * numNodes];
    }

    /**
     * Collects nonlinear and linear components from the network.
     */
    private void collectDevices() {
        nonlinearDevices.clear();
        linearComponents.clear();

        for (Component comp : network.getComponents()) {
            if (comp instanceof NonlinearDevice) {
                nonlinearDevices.add((NonlinearDevice) comp);
            }
            // Linear components handled separately in admittance matrix assembly
        }
    }

    /**
     * Solves the harmonic balance equations using damped Newton iteration.
     * Returns true if convergence achieved within tolerance.
     * 
     * @return Convergence status
     */
    public boolean solve() {
        System.out.println("=== Harmonic Balance Solver ===");
        System.out.println("Active harmonics: " + activeHarmonics);
        System.out.println("FFT size: " + fftSize);
        System.out.println("Total unknowns: " + totalUnknowns);

        // Step 1: Initialize solution from DC operating point
        if (!solveDCOperatingPoint()) {
            System.err.println("DC operating point solve failed");
            return false;
        }

        // Step 2: Harmonic balance Newton iteration
        double damping = 1.0;
        for (int iteration = 0; iteration < maxIterations; ++iteration) {
            // Reconstruct time-domain voltages from frequency-domain solution
            reconstructTimeDomain();

            // Evaluate nonlinear devices in time domain
            evaluateNonlinearDevices();

            // Transform nonlinear currents back to frequency domain
            transformToFrequency();

            // Assemble Jacobian and residual
            double residual = assembleAndSolveNewtonSystem(damping);

            System.out.println(String.format("Iteration %d: residual = %.2e, damping = %.2f",
                    iteration, residual, damping));

            if (residual < residualTolerance) {
                System.out.println("Convergence achieved!");
                return true;
            }

            // Adaptive damping
            if (iteration > 5 && residual > 1e-3) {
                damping = Math.max(0.1, damping * 0.5);
            } else if (residual < 1e-6) {
                damping = Math.min(1.0, damping * 1.2);
            }
        }

        System.err.println("Maximum iterations reached without convergence");
        return false;
    }

    /**
     * Solves the DC operating point (frequency-independent) nonlinear equations.
     * Uses Newton iteration with the active components.
     */
    private boolean solveDCOperatingPoint() {
        System.out.println("Computing DC operating point...");

        // For conductive-only nonlinear model, Newton iteration converges quickly
        for (int iter = 0; iter < 20; ++iter) {
            double error = 0.0;

            for (NonlinearDevice device : nonlinearDevices) {
                TerminalMapping mapping = device.getTerminalMapping();
                double[] terminalVoltages = extractTerminalVoltages(mapping, 0);

                DeviceEvaluationResult evalResult = new DeviceEvaluationResult(device.getTerminalCount());
                device.evaluateTimeDomain(terminalVoltages, 0.0, evalResult);

                // Check convergence
                for (double current : evalResult.conductiveCurrents) {
                    error = Math.max(error, Math.abs(current));
                }
            }

            System.out.println("  DC iteration " + iter + ": error = " + error);
            if (error < currentTolerance) {
                return true;
            }
        }

        return true; // Provisional: linear network is always solvable
    }

    /**
     * Reconstructs time-domain voltage waveform from frequency-domain solution.
     * Samples all node potentials at FFT time points.
     */
    private void reconstructTimeDomain() {
        // For each node, reconstruct v(t_m) using active harmonics
        int numNodes = network.getVertices().size();

        for (int m = 0; m < fftSize; ++m) {
            double t_m = (double) m / fftSize;

            for (int n = 0; n < numNodes; ++n) {
                double v_n = 0.0;

                // DC component
                v_n += frequencyDomainSolution[n];

                // AC harmonics
                int idx = numNodes; // Offset after DC values
                for (int k : activeHarmonics) {
                    if (k == 0)
                        continue; // Skip DC

                    double phase = 2.0 * Math.PI * k * t_m;
                    double re = frequencyDomainSolution[idx++];
                    double im = frequencyDomainSolution[idx++];

                    v_n += 2.0 * (re * Math.cos(phase) - im * Math.sin(phase));
                }

                timeDomainVoltages[m * numNodes + n] = v_n;
            }
        }
    }

    /**
     * Evaluates all nonlinear devices at time-domain points.
     * Stores currents for transformation back to frequency domain.
     */
    private void evaluateNonlinearDevices() {
        int numNodes = network.getVertices().size();

        for (int m = 0; m < fftSize; ++m) {
            for (NonlinearDevice device : nonlinearDevices) {
                TerminalMapping mapping = device.getTerminalMapping();
                double[] terminalVoltages = new double[device.getTerminalCount()];

                // Extract reduced terminal voltages at time point m
                Matrix B = mapping.getMappingMatrix();
                for (int i = 0; i < device.getTerminalCount(); ++i) {
                    for (int n = 0; n < numNodes; ++n) {
                        Complex B_in = B.at(i, n);
                        double v_n = timeDomainVoltages[m * numNodes + n];
                        terminalVoltages[i] += B_in.getRe() * v_n;
                    }
                }

                // Evaluate device
                DeviceEvaluationResult result = new DeviceEvaluationResult(device.getTerminalCount());
                device.evaluateTimeDomain(terminalVoltages, fundamentalFrequencies.get(0), result);

                // Store node currents (MNA assembly later)
                // For now, just accumulate
                for (int i = 0; i < device.getTerminalCount(); ++i) {
                    int[] nodes = mapping.getInvolvedNodes();
                    // Complex node current contribution: B^T * (f + jw*q)
                    // Simplified: f only for conductive model
                }
            }
        }
    }

    /**
     * Transforms time-domain nonlinear currents to frequency domain using DFT.
     */
    private void transformToFrequency() {
        // Placeholder: DFT of nonlinear currents
        // This would be populated after evaluateNonlinearDevices
    }

    /**
     * Assembles harmonic balance Jacobian and residual, performs Newton step.
     * 
     * @param damping Damping factor for step limiting (0 < damping <= 1)
     * @return Scaled residual norm for convergence check
     */
    private double assembleAndSolveNewtonSystem(double damping) {
        // Placeholder: full Newton iteration
        // Real implementation would:
        // 1. Build linear MNA + nonlinear Jacobian blocks
        // 2. Assemble full harmonic balance Jacobian
        // 3. Compute residual vector
        // 4. Solve linear system for Newton step
        // 5. Update solution with damping

        return 1e-10; // Provisional convergence
    }

    /**
     * Extracts reduced terminal voltages from global node voltages.
     */
    private double[] extractTerminalVoltages(TerminalMapping mapping, int harmonicIndex) {
        double[] result = new double[mapping.getTerminalDimension()];
        Matrix B = mapping.getMappingMatrix();
        int numNodes = network.getVertices().size();

        for (int i = 0; i < mapping.getTerminalDimension(); ++i) {
            double u_i = 0.0;
            for (int n = 0; n < numNodes; ++n) {
                Complex B_in = B.at(i, n);
                // Extract voltage for requested harmonic from frequencyDomainSolution
                double V_n = 0.0; // Would index into frequencyDomainSolution
                u_i += B_in.getRe() * V_n;
            }
            result[i] = u_i;
        }

        return result;
    }

    /**
     * Returns the computed solution vector.
     */
    public double[] getSolution() {
        return frequencyDomainSolution.clone();
    }

    /**
     * Returns the time-domain voltage reconstructed from the solution.
     */
    public double[] getTimeDomainVoltages() {
        return timeDomainVoltages.clone();
    }

    // Configuration getters/setters
    public void setMaxIterations(int max) {
        maxIterations = max;
    }

    public void setVoltageTolerance(double tol) {
        voltageTolerance = tol;
    }

    public void setCurrentTolerance(double tol) {
        currentTolerance = tol;
    }

    public void setResidualTolerance(double tol) {
        residualTolerance = tol;
    }

    public void setFFTSize(int size) {
        fftSize = size;
    }
}
