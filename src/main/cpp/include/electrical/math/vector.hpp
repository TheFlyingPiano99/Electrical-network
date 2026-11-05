#pragma once

#include "complex.hpp"
#include <vector>
#include <stdexcept>
#include <memory>

namespace electrical::math {

/**
 * Vector class for complex numbers.
 * Optimized for electrical network calculations.
 */
class Vector {
private:
    std::vector<Complex> elements;
    int dimension{0};

public:
    // Constructors
    explicit Vector(int d = 0) : dimension(d), elements(d) {}
    
    Vector(const Vector&) = default;
    Vector& operator=(const Vector&) = default;
    
    Vector(Vector&&) noexcept = default;
    Vector& operator=(Vector&&) noexcept = default;
    
    // Factory methods
    [[nodiscard]] static Vector zeros(int dimension) {
        return Vector(dimension);
    }
    
    [[nodiscard]] static Vector ones(int dimension) {
        Vector v(dimension);
        for (int i = 0; i < dimension; ++i) {
            v.elements[i] = Complex(1.0, 0.0);
        }
        return v;
    }
    
    // Accessors
    [[nodiscard]] int get_dimension() const noexcept { return dimension; }
    
    [[nodiscard]] Complex at(int index) const {
        if (index < 0 || index >= dimension) {
            throw std::out_of_range("Vector index out of range");
        }
        return elements[index];
    }
    
    [[nodiscard]] Complex& at(int index) {
        if (index < 0 || index >= dimension) {
            throw std::out_of_range("Vector index out of range");
        }
        return elements[index];
    }
    
    void set_at(int index, const Complex& value) {
        if (index < 0 || index >= dimension) {
            throw std::out_of_range("Vector index out of range");
        }
        elements[index] = value;
    }
    
    // Vector operations
    [[nodiscard]] double magnitude() const noexcept {
        double sum = 0.0;
        for (const auto& e : elements) {
            double mag = e.magnitude();
            sum += mag * mag;
        }
        return std::sqrt(sum);
    }
    
    [[nodiscard]] Complex dot_product(const Vector& other) const {
        if (dimension != other.dimension) {
            throw std::invalid_argument("Vector dimensions must match for dot product");
        }
        Complex result(0.0, 0.0);
        for (int i = 0; i < dimension; ++i) {
            result += elements[i] * other.elements[i];
        }
        return result;
    }
    
    // Arithmetic operations
    [[nodiscard]] Vector operator+(const Vector& other) const {
        if (dimension != other.dimension) {
            throw std::invalid_argument("Vector dimensions must match for addition");
        }
        Vector result(dimension);
        for (int i = 0; i < dimension; ++i) {
            result.elements[i] = elements[i] + other.elements[i];
        }
        return result;
    }
    
    [[nodiscard]] Vector operator-(const Vector& other) const {
        if (dimension != other.dimension) {
            throw std::invalid_argument("Vector dimensions must match for subtraction");
        }
        Vector result(dimension);
        for (int i = 0; i < dimension; ++i) {
            result.elements[i] = elements[i] - other.elements[i];
        }
        return result;
    }
    
    [[nodiscard]] Vector operator*(const Complex& scalar) const {
        Vector result(dimension);
        for (int i = 0; i < dimension; ++i) {
            result.elements[i] = elements[i] * scalar;
        }
        return result;
    }
    
    [[nodiscard]] Vector operator/(const Complex& scalar) const {
        return *this * scalar.inverse();
    }
    
    // In-place operations
    Vector& operator+=(const Vector& other) {
        if (dimension != other.dimension) {
            throw std::invalid_argument("Vector dimensions must match");
        }
        for (int i = 0; i < dimension; ++i) {
            elements[i] += other.elements[i];
        }
        return *this;
    }
    
    Vector& operator-=(const Vector& other) {
        if (dimension != other.dimension) {
            throw std::invalid_argument("Vector dimensions must match");
        }
        for (int i = 0; i < dimension; ++i) {
            elements[i] -= other.elements[i];
        }
        return *this;
    }
    
    Vector& operator*=(const Complex& scalar) {
        for (auto& e : elements) {
            e *= scalar;
        }
        return *this;
    }
    
    // Comparison
    [[nodiscard]] bool operator==(const Vector& other) const noexcept {
        if (dimension != other.dimension) return false;
        for (int i = 0; i < dimension; ++i) {
            if (elements[i] != other.elements[i]) return false;
        }
        return true;
    }
    
    [[nodiscard]] bool operator!=(const Vector& other) const noexcept {
        return !(*this == other);
    }
    
    // Resize
    void resize(int new_dimension) {
        dimension = new_dimension;
        elements.resize(new_dimension);
    }
    
    // Copy with resize
    void copy_with_resize(const Vector& other) {
        if (this == &other) return;
        dimension = other.dimension;
        elements = other.elements;
    }
};

} // namespace electrical::math
