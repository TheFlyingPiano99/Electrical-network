#pragma once

#include "complex.hpp"
#include "vector.hpp"
#include <vector>
#include <stdexcept>
#include <memory>
#include <algorithm>

namespace electrical::math {

/**
 * Column-major matrix class for complex numbers.
 * Optimized for electrical network analysis.
 * Storage: elements[col * row + row_index]
 */
class Matrix {
private:
    std::vector<Complex> elements;
    int rows{0};
    int cols{0};

public:
    // Constructors
    Matrix() = default;
    
    Matrix(int r, int c) : rows(r), cols(c), elements(r * c) {}
    
    Matrix(const Matrix&) = default;
    Matrix& operator=(const Matrix&) = default;
    
    Matrix(Matrix&&) noexcept = default;
    Matrix& operator=(Matrix&&) noexcept = default;
    
    // Factory methods
    [[nodiscard]] static Matrix zeros(int r, int c) {
        return Matrix(r, c);
    }
    
    [[nodiscard]] static Matrix identity(int n) {
        Matrix m(n, n);
        for (int i = 0; i < n; ++i) {
            m.at(i, i) = Complex(1.0, 0.0);
        }
        return m;
    }
    
    // Accessors
    [[nodiscard]] int get_rows() const noexcept { return rows; }
    [[nodiscard]] int get_cols() const noexcept { return cols; }
    
    // Get element at (row, col) - column major storage
    [[nodiscard]] Complex at(int row, int col) const {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw std::out_of_range("Matrix index out of range");
        }
        return elements[col * rows + row];
    }
    
    // Set element at (row, col)
    void set_at(int row, int col, const Complex& value) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw std::out_of_range("Matrix index out of range");
        }
        elements[col * rows + row] = value;
    }
    
    // Matrix operations
    [[nodiscard]] Matrix transpose() const {
        Matrix result(cols, rows);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                result.set_at(j, i, at(i, j));
            }
        }
        return result;
    }
    
    [[nodiscard]] Matrix conjugate_transpose() const {
        Matrix result(cols, rows);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                result.set_at(j, i, at(i, j).conjugate());
            }
        }
        return result;
    }
    
    // Matrix addition
    [[nodiscard]] Matrix operator+(const Matrix& other) const {
        if (rows != other.rows || cols != other.cols) {
            throw std::invalid_argument("Matrix dimensions must match for addition");
        }
        Matrix result(rows, cols);
        for (size_t i = 0; i < elements.size(); ++i) {
            result.elements[i] = elements[i] + other.elements[i];
        }
        return result;
    }
    
    // Matrix subtraction
    [[nodiscard]] Matrix operator-(const Matrix& other) const {
        if (rows != other.rows || cols != other.cols) {
            throw std::invalid_argument("Matrix dimensions must match for subtraction");
        }
        Matrix result(rows, cols);
        for (size_t i = 0; i < elements.size(); ++i) {
            result.elements[i] = elements[i] - other.elements[i];
        }
        return result;
    }
    
    // Matrix multiplication
    [[nodiscard]] Matrix operator*(const Matrix& other) const {
        if (cols != other.rows) {
            throw std::invalid_argument("Matrix dimensions incompatible for multiplication");
        }
        Matrix result(rows, other.cols);
        
        // Optimized multiplication loop with column-major access
        for (int j = 0; j < other.cols; ++j) {
            for (int i = 0; i < rows; ++i) {
                Complex sum(0.0, 0.0);
                for (int k = 0; k < cols; ++k) {
                    sum += at(i, k) * other.at(k, j);
                }
                result.set_at(i, j, sum);
            }
        }
        return result;
    }
    
    // Matrix-vector multiplication
    [[nodiscard]] Vector operator*(const Vector& vec) const {
        if (cols != vec.get_dimension()) {
            throw std::invalid_argument("Matrix columns must match vector dimension");
        }
        Vector result(rows);
        for (int i = 0; i < rows; ++i) {
            Complex sum(0.0, 0.0);
            for (int j = 0; j < cols; ++j) {
                sum += at(i, j) * vec.at(j);
            }
            result.set_at(i, sum);
        }
        return result;
    }
    
    // Scalar operations
    [[nodiscard]] Matrix operator*(const Complex& scalar) const {
        Matrix result(rows, cols);
        for (size_t i = 0; i < elements.size(); ++i) {
            result.elements[i] = elements[i] * scalar;
        }
        return result;
    }
    
    [[nodiscard]] Matrix operator/(const Complex& scalar) const {
        return *this * scalar.inverse();
    }
    
    // In-place operations
    Matrix& operator+=(const Matrix& other) {
        if (rows != other.rows || cols != other.cols) {
            throw std::invalid_argument("Matrix dimensions must match");
        }
        for (size_t i = 0; i < elements.size(); ++i) {
            elements[i] += other.elements[i];
        }
        return *this;
    }
    
    Matrix& operator-=(const Matrix& other) {
        if (rows != other.rows || cols != other.cols) {
            throw std::invalid_argument("Matrix dimensions must match");
        }
        for (size_t i = 0; i < elements.size(); ++i) {
            elements[i] -= other.elements[i];
        }
        return *this;
    }
    
    Matrix& operator*=(const Complex& scalar) {
        for (auto& e : elements) {
            e *= scalar;
        }
        return *this;
    }
    
    // Comparison
    [[nodiscard]] bool operator==(const Matrix& other) const noexcept {
        if (rows != other.rows || cols != other.cols) return false;
        return std::equal(elements.begin(), elements.end(), other.elements.begin());
    }
    
    [[nodiscard]] bool operator!=(const Matrix& other) const noexcept {
        return !(*this == other);
    }
    
    // Resize
    void resize(int new_rows, int new_cols) {
        rows = new_rows;
        cols = new_cols;
        elements.resize(new_rows * new_cols);
    }
    
    // Copy with resize
    void copy_with_resize(const Matrix& other) {
        if (this == &other) return;
        rows = other.rows;
        cols = other.cols;
        elements = other.elements;
    }
    
    // Get column as vector
    [[nodiscard]] Vector get_column(int col) const {
        if (col < 0 || col >= cols) {
            throw std::out_of_range("Column index out of range");
        }
        Vector result(rows);
        for (int i = 0; i < rows; ++i) {
            result.set_at(i, at(i, col));
        }
        return result;
    }
    
    // Set column from vector
    void set_column(int col, const Vector& vec) {
        if (col < 0 || col >= cols) {
            throw std::out_of_range("Column index out of range");
        }
        if (vec.get_dimension() != rows) {
            throw std::invalid_argument("Vector dimension must match matrix rows");
        }
        for (int i = 0; i < rows; ++i) {
            set_at(i, col, vec.at(i));
        }
    }
    
    // Swap columns
    void swap_columns(int col1, int col2) {
        if (col1 < 0 || col1 >= cols || col2 < 0 || col2 >= cols) {
            throw std::out_of_range("Column index out of range");
        }
        for (int i = 0; i < rows; ++i) {
            std::swap(elements[col1 * rows + i], elements[col2 * rows + i]);
        }
    }
};

} // namespace electrical::math
