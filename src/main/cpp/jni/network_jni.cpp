// JNI bridge for Network class
// Connects Java GUI with C++23 network engine

#include <jni.h>
#include "electrical/math/gauss.hpp"
#include "electrical/math/matrix.hpp"

using namespace electrical::math;

/**
 * Called by Java to test if JNI library loaded successfully
 */
extern "C" JNIEXPORT jstring JNICALL
Java_network_Network_testJNI(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("JNI Library Loaded Successfully - C++23");
}

/**
 * Solves a linear system using C++23 Gaussian elimination.
 * Called from Java Network class.
 *
 * @param env JNI environment
 * @param clazz Java class
 * @param matrix_rows Number of matrix rows
 * @param matrix_cols Number of matrix columns  
 * @param matrix_data Complex values in row-major order (re, im, re, im, ...)
 * @return Solution vector as double array (re, im, re, im, ...)
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_LinearSystemForCurrent_solveSystemNative(
    JNIEnv* env,
    jclass clazz,
    jint matrix_rows,
    jint matrix_cols,
    jdoubleArray matrix_data) {
    
    try {
        // Get the matrix data from Java
        jdouble* matrix_ptr = env->GetDoubleArrayElements(matrix_data, nullptr);
        jint array_len = env->GetArrayLength(matrix_data);
        
        if (array_len != matrix_rows * matrix_cols * 2) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
            throw std::invalid_argument("Matrix data size mismatch");
        }
        
        // Convert to C++ Matrix format (column-major)
        Matrix system(matrix_rows, matrix_cols);
        
        int idx = 0;
        for (int col = 0; col < matrix_cols; ++col) {
            for (int row = 0; row < matrix_rows; ++row) {
                double real_part = matrix_ptr[idx++];
                double imag_part = matrix_ptr[idx++];
                system.set_at(row, col, Complex(real_part, imag_part));
            }
        }
        
        // Solve using Gaussian elimination
        Vector solution = Gauss::eliminate(system);
        
        // Convert solution back to Java format
        jint solution_size = solution.get_dimension();
        jdoubleArray result = env->NewDoubleArray(solution_size * 2);
        
        if (result != nullptr) {
            jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
            
            for (jint i = 0; i < solution_size; ++i) {
                Complex c = solution.at(i);
                result_ptr[i * 2] = c.real();
                result_ptr[i * 2 + 1] = c.imag();
            }
            
            env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        }
        
        env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
        return result;
        
    } catch (const NoSolution& e) {
        env->ThrowNew(env->FindClass("network/GaussException"), 
                     "No solution exists for the linear system");
        return nullptr;
    } catch (const InfiniteSolutions& e) {
        env->ThrowNew(env->FindClass("network/GaussException"), 
                     "Infinite solutions exist for the linear system");
        return nullptr;
    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    }
}

/**
 * Calculates complex impedance for a component.
 * Z = R + j*ωL + 1/(j*ωC) = R + j*(ωL - 1/(ωC))
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_Component_calculateImpedanceNative(
    JNIEnv* env,
    jclass clazz,
    jdouble resistance,      // R in ohms
    jdouble inductance,      // L in henries
    jdouble capacitance,     // C in farads
    jdouble angular_frequency) { // ω in rad/s
    
    try {
        // Calculate impedance
        double real_z = resistance;
        double imag_z = angular_frequency * inductance - 1.0 / (angular_frequency * capacitance + 1e-12);
        
        // Return as [real, imag]
        jdoubleArray result = env->NewDoubleArray(2);
        if (result != nullptr) {
            jdouble values[2] = {real_z, imag_z};
            env->SetDoubleArrayRegion(result, 0, 2, values);
        }
        return result;
        
    } catch (const std::exception& e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    }
}

/**
 * Calculates the magnitude of a complex number.
 */
extern "C" JNIEXPORT jdouble JNICALL
Java_math_Complex_magnitudeNative(
    JNIEnv* env,
    jclass clazz,
    jdouble real_part,
    jdouble imag_part) {
    
    Complex c(real_part, imag_part);
    return c.magnitude();
}

/**
 * Calculates the phase of a complex number.
 */
extern "C" JNIEXPORT jdouble JNICALL
Java_math_Complex_phaseNative(
    JNIEnv* env,
    jclass clazz,
    jdouble real_part,
    jdouble imag_part) {
    
    Complex c(real_part, imag_part);
    return c.phase();
}
