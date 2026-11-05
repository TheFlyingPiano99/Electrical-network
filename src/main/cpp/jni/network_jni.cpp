// JNI bridge for Network class
// Connects Java GUI with C++23 network engine

#include <jni.h>
#include <complex>
#include <vector>
#include <algorithm>
#include <stdexcept>

using Complex = std::complex<double>;

/**
 * Called by Java to test if JNI library loaded successfully
 */
extern "C" JNIEXPORT jstring JNICALL
Java_network_NativeNetworkInterface_testJNI(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("JNI Library Loaded Successfully - C++23");
}

/**
 * Solves a linear system using Gaussian elimination.
 * Called from Java NativeNetworkInterface.solveLinearSystem()
 *
 * @param env JNI environment
 * @param clazz Java class
 * @param matrix_rows Number of matrix rows (equations)
 * @param matrix_cols Number of matrix columns (unknowns + 1 for RHS)
 * @param matrix_data Complex values in row-major order (re, im, re, im, ...)
 * @return Solution vector as double array (re, im, re, im, ...)
 */
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_network_NativeNetworkInterface_solveLinearSystem(
    JNIEnv* env,
    jclass clazz,
    jint matrix_rows,
    jint matrix_cols,
    jdoubleArray matrix_data) {
    
    jdouble* matrix_ptr = nullptr;
    jdoubleArray result = nullptr;
    
    try {
        // Validate inputs
        if (matrix_rows <= 0 || matrix_cols <= 0) {
            env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), 
                         "Matrix dimensions must be positive");
            return nullptr;
        }
        
        if (matrix_data == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/NullPointerException"), 
                         "Matrix data is null");
            return nullptr;
        }
        
        // Get the matrix data from Java
        matrix_ptr = env->GetDoubleArrayElements(matrix_data, nullptr);
        if (matrix_ptr == nullptr) {
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), 
                         "Failed to get matrix data");
            return nullptr;
        }
        
        jint array_len = env->GetArrayLength(matrix_data);
        jint expected_len = matrix_rows * matrix_cols * 2;
        if (array_len != expected_len) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
            throw std::invalid_argument("Matrix data size mismatch");
        }
        
        // The Java matrix is column-major in the equation sense: every column is one equation,
        // rows 0..rows-2 are the coefficients of the unknowns and the last row is the right side.
        // Transpose it into a regular augmented system: one row per equation, [coefficients | rhs].
        const int num_unknowns = matrix_rows - 1;
        const int num_equations = matrix_cols;
        std::vector<std::vector<Complex>> M(num_equations, std::vector<Complex>(num_unknowns + 1));
        
        for (int row = 0; row < matrix_rows; ++row) {
            for (int col = 0; col < matrix_cols; ++col) {
                int idx = (row * matrix_cols + col) * 2;
                M[col][row] = Complex(matrix_ptr[idx], matrix_ptr[idx + 1]);
            }
        }
        
        // Gauss-Jordan elimination with partial pivoting. Redundant equations
        // (e.g. the ground vertex or isolated parts of the graph) are allowed.
        const double epsilon = 1e-12;
        int pivot_row_index = 0;
        for (int col = 0; col < num_unknowns; ++col) {
            int pivot_row = -1;
            double max_magnitude = epsilon;
            for (int row = pivot_row_index; row < num_equations; ++row) {
                double mag = std::abs(M[row][col]);
                if (mag > max_magnitude) {
                    max_magnitude = mag;
                    pivot_row = row;
                }
            }
            
            if (pivot_row < 0) {
                env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
                env->ThrowNew(env->FindClass("math/GaussException"), 
                             "Singular matrix: cannot find non-zero pivot");
                return nullptr;
            }
            
            std::swap(M[pivot_row_index], M[pivot_row]);
            
            Complex pivot = M[pivot_row_index][col];
            for (int j = col; j <= num_unknowns; ++j) {
                M[pivot_row_index][j] = M[pivot_row_index][j] / pivot;
            }
            
            for (int row = 0; row < num_equations; ++row) {
                if (row != pivot_row_index) {
                    Complex factor = M[row][col];
                    for (int j = col; j <= num_unknowns; ++j) {
                        M[row][j] = M[row][j] - (factor * M[pivot_row_index][j]);
                    }
                }
            }
            ++pivot_row_index;
        }
        
        // Remaining equations must be consistent (0 = 0).
        for (int row = pivot_row_index; row < num_equations; ++row) {
            if (std::abs(M[row][num_unknowns]) > 1e-9) {
                env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
                env->ThrowNew(env->FindClass("math/GaussException"), 
                             "Inconsistent linear system: no solution");
                return nullptr;
            }
        }
        
        jint solution_size = num_unknowns;
        result = env->NewDoubleArray(solution_size * 2);
        
        if (result == nullptr) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
            env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), 
                         "Failed to allocate result array");
            return nullptr;
        }
        
        jdouble* result_ptr = env->GetDoubleArrayElements(result, nullptr);
        if (result_ptr != nullptr) {
            for (int i = 0; i < solution_size; ++i) {
                Complex sol = M[i][num_unknowns];  // RHS column
                result_ptr[i * 2] = sol.real();
                result_ptr[i * 2 + 1] = sol.imag();
            }
            env->ReleaseDoubleArrayElements(result, result_ptr, 0);
        }
        
        env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
        return result;
        
    } catch (const std::bad_alloc&) {
        if (matrix_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), 
                     "Memory allocation failed");
        return nullptr;
    } catch (const std::exception& e) {
        if (matrix_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
        return nullptr;
    } catch (...) {
        if (matrix_ptr != nullptr) {
            env->ReleaseDoubleArrayElements(matrix_data, matrix_ptr, JNI_ABORT);
        }
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), 
                     "Unknown error in solveLinearSystem");
        return nullptr;
    }
}
