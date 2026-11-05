#include "electrical/math/gauss.hpp"
#include <algorithm>
#include <cmath>

namespace electrical::math {

Vector Gauss::eliminate(Matrix system) {
    // Clone the matrix to avoid modifying the original
    Matrix M = system;
    
    SingularityFlag flag = reduce(M);
    
    switch (flag) {
        case SingularityFlag::NoSolution:
            throw NoSolution();
        
        case SingularityFlag::InfiniteSolutions:
            throw InfiniteSolutions();
        
        case SingularityFlag::CleanSolution: {
            // Back substitution phase
            int rightSideRow = M.get_rows() - 1;
            
            for (int c = M.get_cols() - 1; c > 0; --c) {
                int r = c - 1;
                for (int ci = c - 1; ci >= 0; --ci) {
                    Complex new_val = M.at(rightSideRow, ci);
                    new_val -= (M.at(rightSideRow, c) * M.at(r, ci));
                    M.set_at(rightSideRow, ci, new_val);
                    M.set_at(r, ci, Complex(0.0, 0.0));
                }
            }
            
            // Extract solution
            Vector result(M.get_rows() - 1);
            for (int c = 0; c < M.get_cols(); ++c) {
                result.set_at(c, M.at(rightSideRow, c));
            }
            return result;
        }
        
        default:
            return Vector(0);
    }
}

SingularityFlag Gauss::reduce(Matrix& M) {
    if (M.get_cols() < M.get_rows() - 1) {
        return SingularityFlag::InfiniteSolutions;
    }
    
    int c = 0;
    int r = 0;
    
    while (true) {
        if (M.at(r, c) != Complex(0.0, 0.0)) {
            // Divide column c by M(r, c)
            Complex divider = M.at(r, c).inverse();
            for (int i = 0; i < M.get_rows(); ++i) {
                M.set_at(i, c, M.at(i, c) * divider);
            }
            
            if (c < M.get_cols() - 1) {
                // Eliminate this column from other columns
                for (int i = c + 1; i < M.get_cols(); ++i) {
                    Complex factor = M.at(r, i);
                    for (int j = 0; j < M.get_rows(); ++j) {
                        Complex new_val = M.at(j, i);
                        new_val -= (factor * M.at(j, c));
                        M.set_at(j, i, new_val);
                    }
                }
            }
            
            if ((r == M.get_rows() - 2) || (c == M.get_cols() - 1)) {
                break;
            } else {
                r++;
                c++;
            }
        } else {
            // M(r, c) == 0
            bool foundSwappable = false;
            
            if (c < M.get_cols() - 1) {
                // Search for a non-zero element in this row, to the right
                for (int i = c + 1; i < M.get_cols(); ++i) {
                    if (M.at(r, i) != Complex(0.0, 0.0)) {
                        swap_columns(M, c, i);
                        foundSwappable = true;
                        break;
                    }
                }
            }
            
            if (!foundSwappable) {
                if (r == M.get_rows() - 2) {
                    c--;
                    break;
                } else {
                    r++;
                }
            }
        }
    }
    
    if (c == -1) {
        return SingularityFlag::InfiniteSolutions;
    }
    
    // Check for forbidden columns
    if (c < M.get_cols() - 1) {
        for (int i = c; i < M.get_cols(); ++i) {
            bool foundNotZeroInColumn = false;
            for (int j = 0; j < M.get_rows(); ++j) {
                if (M.at(j, i) != Complex(0.0, 0.0)) {
                    if (j == M.get_rows() - 1) {
                        // Forbidden column: all zeros except last row
                        return SingularityFlag::NoSolution;
                    }
                    foundNotZeroInColumn = true;
                    break;
                }
            }
        }
        
        if (M.get_cols() - (c + 1) > 0) {
            if (M.get_cols() < M.get_rows() - 1) {
                return SingularityFlag::InfiniteSolutions;
            }
        }
    }
    
    return SingularityFlag::CleanSolution;
}

void Gauss::swap_columns(Matrix& M, int col1, int col2) {
    M.swap_columns(col1, col2);
}

} // namespace electrical::math
