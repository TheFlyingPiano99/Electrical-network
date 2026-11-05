#pragma once

#include <complex>
#include <cmath>
#include <limits>
#include <stdexcept>
#include <iostream>
#include <string>

namespace electrical::math {

// Constants
constexpr double EPSILON = 1e-10;
constexpr double PI = 3.14159265358979323846;

/**
 * Complex number wrapper for circuit analysis.
 * Provides convenient arithmetic operations optimized for C++23.
 */
class Complex {
private:
    double real_part{0.0};
    double imag_part{0.0};

public:
    // Constructors
    constexpr Complex() noexcept = default;
    
    constexpr Complex(double r, double i = 0.0) noexcept 
        : real_part(r), imag_part(i) {}
    
    constexpr Complex(const std::complex<double>& c) noexcept
        : real_part(c.real()), imag_part(c.imag()) {}
    
    // Copy semantics
    constexpr Complex(const Complex&) noexcept = default;
    constexpr Complex& operator=(const Complex&) noexcept = default;
    
    // Accessors
    [[nodiscard]] constexpr double real() const noexcept { return real_part; }
    [[nodiscard]] constexpr double imag() const noexcept { return imag_part; }
    
    // Conversion to std::complex
    [[nodiscard]] constexpr std::complex<double> to_std() const noexcept {
        return std::complex<double>(real_part, imag_part);
    }
    
    // Magnitude and phase
    [[nodiscard]] double magnitude() const noexcept {
        return std::sqrt(real_part * real_part + imag_part * imag_part);
    }
    
    [[nodiscard]] double magnitude_squared() const noexcept {
        return real_part * real_part + imag_part * imag_part;
    }
    
    [[nodiscard]] double phase() const noexcept {
        return std::atan2(imag_part, real_part);
    }
    
    // Conjugate
    [[nodiscard]] constexpr Complex conjugate() const noexcept {
        return Complex(real_part, -imag_part);
    }
    
    // Inverse (1/this)
    [[nodiscard]] Complex inverse() const {
        double mag_sq = magnitude_squared();
        if (mag_sq < EPSILON * EPSILON) {
            throw std::domain_error("Cannot invert near-zero complex number");
        }
        return Complex(real_part / mag_sq, -imag_part / mag_sq);
    }
    
    // Arithmetic operations
    constexpr Complex operator+(const Complex& other) const noexcept {
        return Complex(real_part + other.real_part, imag_part + other.imag_part);
    }
    
    constexpr Complex operator-(const Complex& other) const noexcept {
        return Complex(real_part - other.real_part, imag_part - other.imag_part);
    }
    
    constexpr Complex operator*(const Complex& other) const noexcept {
        return Complex(
            real_part * other.real_part - imag_part * other.imag_part,
            real_part * other.imag_part + imag_part * other.real_part
        );
    }
    
    Complex operator/(const Complex& other) const {
        return *this * other.inverse();
    }
    
    constexpr Complex operator*(double scalar) const noexcept {
        return Complex(real_part * scalar, imag_part * scalar);
    }
    
    constexpr Complex operator/(double scalar) const {
        if (std::abs(scalar) < EPSILON) {
            throw std::domain_error("Division by near-zero scalar");
        }
        return Complex(real_part / scalar, imag_part / scalar);
    }
    
    // In-place operations
    constexpr Complex& operator+=(const Complex& other) noexcept {
        real_part += other.real_part;
        imag_part += other.imag_part;
        return *this;
    }
    
    constexpr Complex& operator-=(const Complex& other) noexcept {
        real_part -= other.real_part;
        imag_part -= other.imag_part;
        return *this;
    }
    
    constexpr Complex& operator*=(const Complex& other) noexcept {
        double new_real = real_part * other.real_part - imag_part * other.imag_part;
        double new_imag = real_part * other.imag_part + imag_part * other.real_part;
        real_part = new_real;
        imag_part = new_imag;
        return *this;
    }
    
    // Comparison
    [[nodiscard]] bool operator==(const Complex& other) const noexcept {
        return std::abs(real_part - other.real_part) < EPSILON &&
               std::abs(imag_part - other.imag_part) < EPSILON;
    }
    
    [[nodiscard]] bool operator!=(const Complex& other) const noexcept {
        return !(*this == other);
    }
    
    // Ordering (by magnitude, then real, then imaginary)
    [[nodiscard]] int compare_to(const Complex& other) const noexcept {
        double this_mag = magnitude();
        double other_mag = other.magnitude();
        
        if (std::abs(this_mag - other_mag) > EPSILON) {
            return this_mag < other_mag ? -1 : 1;
        }
        if (std::abs(real_part - other.real_part) > EPSILON) {
            return real_part < other.real_part ? -1 : 1;
        }
        if (std::abs(imag_part - other.imag_part) > EPSILON) {
            return imag_part < other.imag_part ? -1 : 1;
        }
        return 0;
    }
    
    // Polar form
    [[nodiscard]] static Complex from_polar(double magnitude, double phase) noexcept {
        return Complex(magnitude * std::cos(phase), magnitude * std::sin(phase));
    }
    
    // String representation (for debugging)
    [[nodiscard]] std::string to_string() const;
};

// Static helper functions
[[nodiscard]] inline Complex add(const Complex& a, const Complex& b) noexcept {
    return a + b;
}

[[nodiscard]] inline Complex subtract(const Complex& a, const Complex& b) noexcept {
    return a - b;
}

[[nodiscard]] inline Complex multiply(const Complex& a, const Complex& b) noexcept {
    return a * b;
}

[[nodiscard]] inline Complex divide(const Complex& a, const Complex& b) {
    return a / b;
}

} // namespace electrical::math
