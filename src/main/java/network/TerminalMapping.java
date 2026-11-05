package network;

import math.Matrix;

/**
 * Terminal voltage mapping for nonlinear devices.
 * Extracts reduced terminal voltages u = B*V from global node voltages V.
 * The matrix B is typically sparse and defines which voltage differences are relevant.
 * 
 * @author Copilot
 */
public interface TerminalMapping {
    
    /**
     * Returns the mapping matrix B such that u = B * V_nodes.
     * Rows correspond to reduced terminal coordinates; columns to network nodes.
     * 
     * @return Matrix of size (terminalCount) x (numNodes)
     */
    Matrix getMappingMatrix();
    
    /**
     * Returns the number of reduced terminal voltage dimensions.
     */
    int getTerminalDimension();
    
    /**
     * Returns the global node indices involved in this mapping.
     * Used to identify which nodes' potentials affect this device.
     * 
     * @return Array of node indices (may contain -1 for ground)
     */
    int[] getInvolvedNodes();
}
