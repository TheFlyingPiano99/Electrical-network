package network;

import java.io.*;
import math.GaussException;

/**
 * JNI Native interface for high-performance circuit simulation.
 * Bridges Java GUI with C++23 computation engine.
 * 
 * @author Simon Zoltán
 */
public class NativeNetworkInterface {
    private static final String LIBRARY_NAME = "ElectricalJNI";
    private static boolean libraryLoaded = false;
    private static String loadError = null;
    
    // Static initializer - loads native library
    static {
        try {
            System.loadLibrary(LIBRARY_NAME);
            libraryLoaded = true;
            System.out.println("Native C++23 library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            loadError = e.getMessage();
            System.err.println("Failed to load native library: " + LIBRARY_NAME);
            System.err.println("Error: " + e.getMessage());
            System.err.println("This is non-fatal; will fall back to Java implementation");
        }
    }
    
    /**
     * Checks if native library is available
     */
    public static boolean isNativeLibraryLoaded() {
        return libraryLoaded;
    }
    
    /**
     * Gets load error if library failed to load
     */
    public static String getLoadError() {
        return loadError;
    }
    
    /**
     * Test JNI connectivity
     */
    public static native String testJNI();
    
    /**
     * Solves linear system using C++23 Gaussian elimination.
     * Much faster than Java implementation.
     * 
     * @param matrix_rows Number of matrix rows
     * @param matrix_cols Number of matrix columns
     * @param matrix_data Complex values in double array: [r0, i0, r1, i1, ...]
     * @return Solution vector: [r0, i0, r1, i1, ...]
     * @throws GaussException if no solution or infinite solutions
     */
    public static native double[] solveLinearSystem(
        int matrix_rows,
        int matrix_cols,
        double[] matrix_data) throws GaussException;
    
    /**
     * Calculates impedance for a component at given frequency.
     * C++23 optimized calculation.
     * 
     * @param resistance Resistance in ohms
     * @param inductance Inductance in henries
     * @param capacitance Capacitance in farads
     * @param angular_frequency Angular frequency in rad/s
     * @return [real_part, imag_part] of impedance
     */
    public static native double[] calculateImpedance(
        double resistance,
        double inductance,
        double capacitance,
        double angular_frequency);
    
    /**
     * Calculates magnitude of complex number
     */
    public static native double complexMagnitude(double real, double imag);
    
    /**
     * Calculates phase of complex number
     */
    public static native double complexPhase(double real, double imag);
}
