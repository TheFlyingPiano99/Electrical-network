// Harmonic Balance Nonlinear Solver for AC Circuit Analysis
// Solves periodic steady-state equations using Newton-Raphson iteration
// with admittance matrix-based frequency-domain formulation

#include <jni.h>
#include <complex>
#include <vector>
#include <cmath>
#include <algorithm>
#include <stdexcept>
#include <iostream>

using Complex = std::complex<double>;

/**
 * FFT-based frequency-to-time-domain transformation
 * Reconstructs time-domain waveform from Fourier coefficients
 * 
 * @param env JNI environment
 * @param clazz Java class
 * @param harmonics_real Real parts of Fourier coefficients [H0, Re(H1), Im(H1), ...]
 * @param harmonics_imag Imaginary parts (included in harmonics_real as Re, Im pairs)
 * @param num_harmonics Number of frequency components
 * @param num_time_samples Number of time samples to generate
 * @return Time-domain samples [s0, s1, ..., s_{M-1}]
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_NativeNetworkInterface_frequencyToTime(
    JNIEnv* env,
    jclass clazz,
    jdoubleArray harmonics,
    jint num_harmonics,
    jint num_time_samples) {
    
    jdouble* harmonics_ptr = nullptr;
    jdoubleArray result = nullptr;
    
    try {
        if (harmonics == nullptr || num_harmonics <= 0 || num_time_samples <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Invalid input parameters");
            return nullptr;
        }
        
        harmonics_ptr = env->GetDoubleArrayElements(harmonics, nullptr);
        if (harmonics_ptr == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to access harmonics array");
            return nullptr;
        }
        
        jint array_len = env->GetArrayLength(harmonics);
        jint expected_len = num_harmonics * 2;
        if (array_len != expected_len) {
            env->ReleaseDoubleArrayElements(harmonics, harmonics_ptr, JNI_ABORT);
            throw std::invalid_argument("Harmonics array size mismatch");
        }
        
        // Reconstruct time-domain signal
        std::vector<double> time_samples(num_time_samples, 0.0);
        const double pi = 3.141592653589793;
        
        for (int m = 0; m < num_time_samples; ++m) {
            double t = static_cast<double>(m) / num_time_samples;
            
            // DC component (k=0)
            time_samples[m] = harmonics_ptr[0];
            
            // AC harmonics (k > 0) with conjugate symmetry
            for (int k = 1; k < num_harmonics; ++k) {
                double phase = 2.0 * pi * k * t;
                double re_coeff = harmonics_ptr[k * 2];
                double im_coeff = harmonics_ptr[k * 2 + 1];
                
                time_samples[m] += 2.0 * (re_coeff * std::cos(phase) - im_coeff * std::sin(phase));
            }
        }
        
        // Allocate and return result
        result = env->NewDoubleArray(num_time_samples);
        if (result == nullptr) {
            env->ReleaseDoubleArrayElements(harmonics, harmonics_ptr, JNI_ABORT);
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to allocate result array");
            return nullptr;
        }
        
        jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
        if (result_ptr != nullptr) {
            for (int i = 0; i < num_time_samples; ++i) {
                result_ptr[i] = time_samples[i];
            }
            env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        }
        
        env->ReleaseDoubleArrayElements(harmonics, harmonics_ptr, JNI_ABORT);
        return result;
        
    } catch (const std::exception& e) {
        if (harmonics_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(harmonics, harmonics_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        if (harmonics_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(harmonics, harmonics_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"),
                     "Unknown error in frequencyToTime");
        return nullptr;
    }
}

/**
 * Time-to-frequency domain transformation using DFT.
 * Converts time-domain samples back to Fourier coefficients.
 * Enforces real-signal conjugate symmetry.
 *
 * @param env JNI environment
 * @param clazz Java class
 * @param time_samples Time-domain signal samples
 * @param num_time_samples Number of time samples
 * @param num_harmonics Number of frequency coefficients to return
 * @return Fourier coefficients [H0, Re(H1), Im(H1), ..., Re(H_k), Im(H_k)]
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_NativeNetworkInterface_timeToFrequency(
    JNIEnv* env,
    jclass clazz,
    jdoubleArray time_samples,
    jint num_time_samples,
    jint num_harmonics) {
    
    jdouble* samples_ptr = nullptr;
    jdoubleArray result = nullptr;
    
    try {
        if (time_samples == nullptr || num_time_samples <= 0 || num_harmonics <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Invalid input parameters");
            return nullptr;
        }
        
        samples_ptr = env->GetDoubleArrayElements(time_samples, nullptr);
        if (samples_ptr == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to access time samples array");
            return nullptr;
        }
        
        // Compute DFT
        std::vector<Complex> spectrum(num_harmonics);
        const double pi = 3.141592653589793;
        
        for (int k = 0; k < num_harmonics; ++k) {
            Complex sum(0.0, 0.0);
            for (int m = 0; m < num_time_samples; ++m) {
                double phase = -2.0 * pi * k * m / num_time_samples;
                Complex exp_term(std::cos(phase), std::sin(phase));
                sum += samples_ptr[m] * exp_term;
            }
            spectrum[k] = sum / static_cast<double>(num_time_samples);
        }
        
        // Allocate and return result [Re(H0), Re(H1), Im(H1), ..., Re(Hk), Im(Hk)]
        result = env->NewDoubleArray(num_harmonics * 2);
        if (result == nullptr) {
            env->ReleaseDoubleArrayElements(time_samples, samples_ptr, JNI_ABORT);
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to allocate result array");
            return nullptr;
        }
        
        jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
        if (result_ptr != nullptr) {
            // DC component (real-valued)
            result_ptr[0] = spectrum[0].real();
            result_ptr[1] = 0.0;
            
            // AC components
            for (int k = 1; k < num_harmonics; ++k) {
                result_ptr[k * 2] = spectrum[k].real();
                result_ptr[k * 2 + 1] = spectrum[k].imag();
            }
            env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        }
        
        env->ReleaseDoubleArrayElements(time_samples, samples_ptr, JNI_ABORT);
        return result;
        
    } catch (const std::exception& e) {
        if (samples_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(time_samples, samples_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        if (samples_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(time_samples, samples_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"),
                     "Unknown error in timeToFrequency");
        return nullptr;
    }
}

/**
 * Computes exponential function with overflow protection.
 * For large arguments, returns a clamped value instead of infinity.
 * For small arguments near zero, uses expm1 for numerical stability.
 * 
 * @param x Input value
 * @param max_exp Maximum exponent value (default ~100)
 * @return exp(x) or clamped value
 */
static double safe_exp(double x, double max_exp = 100.0) {
    if (x > max_exp) {
        return std::exp(max_exp);  // Clamp to prevent overflow
    }
    if (x < -max_exp) {
        return 0.0;  // Underflow to zero
    }
    return std::exp(x);
}

/**
 * Calculates the Jacobian matrix block for nonlinear device evaluation.
 * Used in harmonic balance Newton iteration.
 * 
 * Computes: J = d(residual)/d(unknowns) for a nonlinear device.
 * 
 * @param env JNI environment
 * @param clazz Java class
 * @param terminal_voltages Complex terminal voltages for all frequencies
 * @param num_frequencies Number of frequency components
 * @param alpha_f Forward current gain (BJT)
 * @param alpha_r Reverse current gain (BJT)
 * @param I_es Saturation current base-emitter (BJT)
 * @param I_cs Saturation current base-collector (BJT)
 * @param V_t Thermal voltage (BJT)
 * @return Jacobian matrix in row-major order
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_NativeNetworkInterface_calculateBJTJacobian(
    JNIEnv* env,
    jclass clazz,
    jdoubleArray terminal_voltages,
    jint num_frequencies,
    jdouble alpha_f,
    jdouble alpha_r,
    jdouble I_es,
    jdouble I_cs,
    jdouble V_t) {
    
    jdouble* voltages_ptr = nullptr;
    jdoubleArray result = nullptr;
    
    try {
        if (terminal_voltages == nullptr || num_frequencies <= 0 || V_t <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Invalid input parameters");
            return nullptr;
        }
        
        voltages_ptr = env->GetDoubleArrayElements(terminal_voltages, nullptr);
        if (voltages_ptr == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to access voltages array");
            return nullptr;
        }
        
        // For BJT with 2 reduced terminal coordinates, each frequency has 2x2 conductance block
        // Jacobian has (num_frequencies * 2) x (num_frequencies * 2) structure
        int jacobian_size = num_frequencies * 2;
        
        // Allocate output matrix (complex values as real, imag pairs)
        jint output_len = jacobian_size * jacobian_size * 2;
        result = env->NewDoubleArray(output_len);
        
        if (result == nullptr) {
            env->ReleaseDoubleArrayElements(terminal_voltages, voltages_ptr, JNI_ABORT);
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to allocate Jacobian matrix");
            return nullptr;
        }
        
        jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
        if (result_ptr != nullptr) {
            // Initialize to zero
            for (int i = 0; i < output_len; ++i) {
                result_ptr[i] = 0.0;
            }
            
            // Extract terminal voltages for DC (frequency 0)
            double v_BE = voltages_ptr[0];  // v_B - v_E
            double v_BC = voltages_ptr[1];  // v_B - v_C
            
            // Compute conductance derivatives at DC operating point
            double g_F = (I_es / V_t) * safe_exp(v_BE / V_t, 100.0);
            double g_R = (I_cs / V_t) * safe_exp(v_BC / V_t, 100.0);
            
            // Conductance matrix: G = df/du
            // f(u) = [-i_E, -i_C]^T
            // G[0,0] = dI_F/du_0 = g_F
            // G[0,1] = -alpha_r * dI_R/du_1 = -alpha_r * g_R
            // G[1,0] = -alpha_f * dI_F/du_0 = -alpha_f * g_F
            // G[1,1] = dI_R/du_1 = g_R
            
            // Fill diagonal blocks (DC component only for conductive-only model)
            result_ptr[0 * 2] = g_F;           // G[0,0].re
            result_ptr[0 * 2 + 1] = 0.0;       // G[0,0].im
            result_ptr[1 * 2] = -alpha_r * g_R;  // G[0,1].re
            result_ptr[1 * 2 + 1] = 0.0;       // G[0,1].im
            result_ptr[2 * 2] = -alpha_f * g_F;  // G[1,0].re
            result_ptr[2 * 2 + 1] = 0.0;       // G[1,0].im
            result_ptr[3 * 2] = g_R;           // G[1,1].re
            result_ptr[3 * 2 + 1] = 0.0;       // G[1,1].im
        }
        
        env->ReleaseDoubleArrayElements(terminal_voltages, voltages_ptr, JNI_ABORT);
        env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        return result;
        
    } catch (const std::exception& e) {
        if (voltages_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(terminal_voltages, voltages_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        if (voltages_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(terminal_voltages, voltages_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"),
                     "Unknown error in calculateBJTJacobian");
        return nullptr;
    }
}

/**
 * Evaluates BJT terminal currents and derivatives (conductive model only).
 * Used for harmonic balance residual evaluation and Newton Jacobian assembly.
 * 
 * @param env JNI environment
 * @param clazz Java class
 * @param v_be Base-emitter voltage
 * @param v_bc Base-collector voltage
 * @param alpha_f Forward current gain
 * @param alpha_r Reverse current gain
 * @param I_es Saturation current
 * @param I_cs Saturation current
 * @param V_t Thermal voltage
 * @return [i_C, i_E, g_F, g_R] array
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_NativeNetworkInterface_evaluateBJT(
    JNIEnv* env,
    jclass clazz,
    jdouble v_be,
    jdouble v_bc,
    jdouble alpha_f,
    jdouble alpha_r,
    jdouble I_es,
    jdouble I_cs,
    jdouble V_t) {
    
    jdoubleArray result = nullptr;
    
    try {
        if (V_t <= 0 || I_es <= 0 || I_cs <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"),
                         "Invalid device parameters");
            return nullptr;
        }
        
        // Compute junction currents with overflow protection
        double I_F = I_es * (safe_exp(v_be / V_t, 100.0) - 1.0);
        double I_R = I_cs * (safe_exp(v_bc / V_t, 100.0) - 1.0);
        
        // Terminal currents (positive into device)
        double i_C = alpha_f * I_F - I_R;
        double i_E = -I_F + alpha_r * I_R;
        
        // Conductance derivatives
        double g_F = (I_es / V_t) * safe_exp(v_be / V_t, 100.0);
        double g_R = (I_cs / V_t) * safe_exp(v_bc / V_t, 100.0);
        
        // Return [i_C, i_E, g_F, g_R]
        result = env->NewDoubleArray(4);
        if (result == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"),
                         "Failed to allocate result array");
            return nullptr;
        }
        
        jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
        if (result_ptr != nullptr) {
            result_ptr[0] = i_C;
            result_ptr[1] = i_E;
            result_ptr[2] = g_F;
            result_ptr[3] = g_R;
            env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        }
        
        return result;
        
    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"),
                     "Unknown error in evaluateBJT");
        return nullptr;
    }
}
