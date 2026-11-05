#pragma once

#include "matrix.hpp"
#include "vector.hpp"
#include <stdexcept>

namespace electrical::math {

// Exception classes for Gaussian elimination
class GaussException : public std::runtime_error {
public:
    explicit GaussException(const std::string& msg) : std::runtime_error(msg) {}
};

class NoSolution : public GaussException {
public:
    NoSolution() : GaussException("Linear system has no solution") {}
};

class InfiniteSolutions : public GaussException {
public:
    InfiniteSolutions() : GaussException("Linear system has infinite solutions") {}
};

// Singularity flags
enum class SingularityFlag {
    NoSolution,
    CleanSolution,
    InfiniteSolutions
};

/**
 * Gaussian elimination solver for linear systems.
 * Optimized for complex number matrices using C++23.
 */
class Gauss {
public:
    /**
     * Solves a linear system using Gaussian elimination.
     * @param system The coefficient matrix with augmented column (row vectors contain RHS)
     * @return Solution vector
     * @throws NoSolution if the system has no solution
     * @throws InfiniteSolutions if the system has infinite solutions
     */
    [[nodiscard]] static Vector eliminate(Matrix system);
    
private:
    /**
     * Reduces matrix to reduced row echelon form.
     * @param M Matrix to reduce (will be modified)
     * @return Singularity flag indicating the type of solution
     */
    [[nodiscard]] static SingularityFlag reduce(Matrix& M);
    
    /**
     * Swaps two columns in a matrix.
     * @param M Matrix
     * @param col1 First column index
     * @param col2 Second column index
     */
    static void swap_columns(Matrix& M, int col1, int col2);
};

} // namespace electrical::math
