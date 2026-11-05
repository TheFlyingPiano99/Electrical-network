package network;

/**
 * JNI Interface for harmonic balance solver computations.
 * Provides native C++23 implementations of frequency-domain and nonlinear device evaluations.
 * 
 * @author Copilot
 */
public class HarmonicBalanceJNI {
    
    static {
        try {
            System.loadLibrary("electrical_network");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library 'electrical_network' not found: " + e.getMessage());
        }
    }
    
    /**
     * Transforms frequency-domain Fourier coefficients to time-domain samples.
     * Implements inverse DFT with conjugate symmetry for real signals.
     * 
     * @param harmonics Packed harmonic coefficients [H0, Re(H1), Im(H1), ..., Re(Hk), Im(Hk)]
     * @param numHarmonics Number of frequency components
     * @param numTimeSamples Number of time samples to generate (FFT size)
     * @return Time-domain samples [s0, s1, ..., s_{M-1}]
     */
    public native static double[] frequencyToTime(double[] harmonics, int numHarmonics, 
                                                 int numTimeSamples);
    
    /**
     * Transforms time-domain samples to frequency-domain Fourier coefficients.
     * Implements DFT with real-signal conjugate symmetry enforced.
     * 
     * @param timeSamples Time-domain signal samples
     * @param numTimeSamples Number of time samples (FFT size)
     * @param numHarmonics Number of frequency components to compute
     * @return Packed harmonic coefficients [H0, Re(H1), Im(H1), ..., Re(Hk), Im(Hk)]
     */
    public native static double[] timeToFrequency(double[] timeSamples, int numTimeSamples,
                                                 int numHarmonics);
    
    /**
     * Evaluates BJT Ebers-Moll model currents and conductances.
     * Computes conductive currents i_C and i_E, plus derivatives g_F and g_R.
     * Implements numerically stable exponential evaluation.
     * 
     * @param v_be Base-emitter junction voltage
     * @param v_bc Base-collector junction voltage
     * @param alphaF Forward current gain (typically ~0.99)
     * @param alphaR Reverse current gain (typically ~0.1)
     * @param I_es Saturation current, base-emitter junction
     * @param I_cs Saturation current, base-collector junction
     * @param V_t Thermal voltage (~0.026 V at 300K)
     * @return [i_C, i_E, g_F, g_R] array
     */
    public native static double[] evaluateBJT(double v_be, double v_bc,
                                             double alphaF, double alphaR,
                                             double I_es, double I_cs, double V_t);
    
    /**
     * Calculates Jacobian block for BJT in harmonic balance formulation.
     * Returns conductance matrix derivatives needed for Newton iteration.
     * 
     * @param terminalVoltages Complex terminal voltages for all frequencies (Re, Im packed)
     * @param numFrequencies Number of frequency components
     * @param alphaF Forward current gain
     * @param alphaR Reverse current gain
     * @param I_es Saturation current BE junction
     * @param I_cs Saturation current BC junction
     * @param V_t Thermal voltage
     * @return Jacobian matrix (complex, row-major, packed as Re, Im)
     */
    public native static double[] calculateBJTJacobian(double[] terminalVoltages,
                                                      int numFrequencies,
                                                      double alphaF, double alphaR,
                                                      double I_es, double I_cs,
                                                      double V_t);
}
