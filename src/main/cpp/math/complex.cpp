#include "electrical/math/complex.hpp"
#include <sstream>

namespace electrical::math {

std::string Complex::to_string() const {
    std::ostringstream oss;
    oss << "(" << real_part << (imag_part >= 0 ? "+" : "") << imag_part << "i)";
    return oss.str();
}

} // namespace electrical::math
