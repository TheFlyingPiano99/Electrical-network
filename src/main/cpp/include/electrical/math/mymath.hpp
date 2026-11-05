#pragma once

#include <cmath>

namespace electrical::math {

// Utility math functions
class MyMath {
public:
    static double max(double a, double b) {
        return a > b ? a : b;
    }
    
    static double min(double a, double b) {
        return a < b ? a : b;
    }
};

} // namespace electrical::math
