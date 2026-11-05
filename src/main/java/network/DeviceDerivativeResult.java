package network;

/**
 * Output structure for device derivative evaluation.
 * Contains conductance and capacitance matrices for Jacobian assembly.
 * 
 * @author Copilot
 */
public class DeviceDerivativeResult {
    
    /**
     * Conductance matrix: G = dI/du (derivative of conductive currents w.r.t. terminal voltages).
     * Row-major matrix of size (terminalCount x terminalCount).
     */
    public double[][] conductanceMatrix;
    
    /**
     * Capacitance matrix: C = dQ/du (derivative of charge w.r.t. terminal voltages).
     * Row-major matrix of size (terminalCount x terminalCount).
     */
    public double[][] capacitanceMatrix;
    
    /**
     * Constructor allocating matrices for a device with given terminal count.
     * 
     * @param terminalCount Number of reduced terminal voltage dimensions
     */
    public DeviceDerivativeResult(int terminalCount) {
        this.conductanceMatrix = new double[terminalCount][terminalCount];
        this.capacitanceMatrix = new double[terminalCount][terminalCount];
    }
    
    /**
     * Clears all matrices to zero. Call before re-evaluation.
     */
    public void clear() {
        for (int i = 0; i < conductanceMatrix.length; i++) {
            for (int j = 0; j < conductanceMatrix[i].length; j++) {
                conductanceMatrix[i][j] = 0.0;
                capacitanceMatrix[i][j] = 0.0;
            }
        }
    }
}
