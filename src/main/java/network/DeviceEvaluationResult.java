package network;

/**
 * Output structure for device evaluation in time domain.
 * Contains conductive currents, charge currents, and related state.
 * 
 * @author Copilot
 */
public class DeviceEvaluationResult {
    
    /**
     * Conductive (resistive) currents f(u) at the given terminal voltages.
     * Real array indexed by terminal index.
     * These are reduced currents used in the MNA equation assembly.
     */
    public double[] conductiveCurrents;
    
    /**
     * Charge quantities q(u) for each terminal.
     * Multiply by jk*omega_0 to get frequency-domain charge current contribution.
     */
    public double[] chargeQuantities;
    
    /**
     * Conductance matrix: dI/du (partial derivative of conductive currents).
     * Row-major matrix of size (terminalCount x terminalCount).
     */
    public double[][] conductanceMatrix;
    
    /**
     * Capacitance matrix: dQ/du (partial derivative of charges).
     * Row-major matrix of size (terminalCount x terminalCount).
     */
    public double[][] capacitanceMatrix;
    
    /**
     * Constructor allocating arrays for a device with given terminal count.
     * 
     * @param terminalCount Number of reduced terminal voltage dimensions
     */
    public DeviceEvaluationResult(int terminalCount) {
        this.conductiveCurrents = new double[terminalCount];
        this.chargeQuantities = new double[terminalCount];
        this.conductanceMatrix = new double[terminalCount][terminalCount];
        this.capacitanceMatrix = new double[terminalCount][terminalCount];
    }
    
    /**
     * Clears all arrays to zero. Call before re-evaluation.
     */
    public void clear() {
        for (int i = 0; i < conductiveCurrents.length; i++) {
            conductiveCurrents[i] = 0.0;
            chargeQuantities[i] = 0.0;
            for (int j = 0; j < conductiveCurrents.length; j++) {
                conductanceMatrix[i][j] = 0.0;
                capacitanceMatrix[i][j] = 0.0;
            }
        }
    }
}
