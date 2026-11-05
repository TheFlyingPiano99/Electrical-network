package network;

import math.Complex;

/**
 * Interface for nonlinear device models supporting harmonic balance analysis.
 * Devices provide terminal currents, charges, and their derivatives for use in
 * frequency-domain Newton iteration.
 * 
 * @author Copilot
 */
public interface NonlinearDevice {
    
    /**
     * Terminal voltage mapping: extracts reduced terminal voltages from global node potentials.
     * For example, a BJT with base at node b, collector at c, emitter at e might use:
     * u = [v_b - v_e, v_b - v_c]^T
     * 
     * @return TerminalMapping defining how to extract reduced voltages from node potentials
     */
    TerminalMapping getTerminalMapping();
    
    /**
     * Evaluates conductive currents and their derivatives at given terminal voltages.
     * Results include conductive currents (at DC and all frequencies) and charge currents.
     * 
     * @param terminalVoltages Complex terminal voltages u_k at frequency k*omega_0
     * @param frequency Angular frequency (omega_0 for the fundamental)
     * @param result Output structure containing currents, charges, and Jacobians
     */
    void evaluateTimeDomain(double[] terminalVoltages, 
                           double frequency, 
                           DeviceEvaluationResult result);
    
    /**
     * Evaluates derivatives (conductances and capacitances) at given terminal voltages.
     * Called during Jacobian assembly for the harmonic balance solver.
     * 
     * @param terminalVoltages Real or complex reduced terminal voltages
     * @param result Output structure for derivative matrices
     */
    void evaluateDerivatives(double[] terminalVoltages,
                            DeviceDerivativeResult result);
    
    /**
     * Returns the number of reduced terminal voltage dimensions.
     * For example: BJT = 2, diode = 1.
     */
    int getTerminalCount();
    
    /**
     * Returns a descriptive name for this device model.
     */
    String getModelName();
    
    /**
     * Validates model parameters; throws RuntimeException if inconsistent.
     * Called before simulation starts.
     */
    void validateParameters();
}
