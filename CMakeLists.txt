cmake_minimum_required(VERSION 3.27)
project(ElectricalNetwork CXX)

# C++23 Configuration
set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS_RELEASE "/O2 /arch:AVX2")

# Platform Detection
if(WIN32)
    set(JNI_PLATFORM "win32")
else()
    message(FATAL_ERROR "This project requires Windows")
endif()

# Find Java and JNI
find_package(Java REQUIRED)
find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

# Source Files
set(MATH_SOURCES
    src/main/cpp/math/complex.cpp
    src/main/cpp/math/vector.cpp
    src/main/cpp/math/matrix.cpp
    src/main/cpp/math/coordinate.cpp
    src/main/cpp/math/line.cpp
    src/main/cpp/math/mymath.cpp
    src/main/cpp/math/gauss.cpp
    src/main/cpp/math/exceptions.cpp
)

set(NETWORK_SOURCES
    src/main/cpp/network/component.cpp
    src/main/cpp/network/component_node.cpp
    src/main/cpp/network/edge.cpp
    src/main/cpp/network/vertex.cpp
    src/main/cpp/network/wire.cpp
    src/main/cpp/network/resistance.cpp
    src/main/cpp/network/capacitor.cpp
    src/main/cpp/network/inductor.cpp
    src/main/cpp/network/dc_voltage_source.cpp
    src/main/cpp/network/sinusoidal_voltage_source.cpp
    src/main/cpp/network/square_voltage_source.cpp
    src/main/cpp/network/triangle_voltage_source.cpp
    src/main/cpp/network/sawtooth_voltage_source.cpp
    src/main/cpp/network/current_source.cpp
    src/main/cpp/network/analog_voltmeter.cpp
    src/main/cpp/network/analog_ammeter.cpp
    src/main/cpp/network/ground.cpp
    src/main/cpp/network/linear_system.cpp
    src/main/cpp/network/network.cpp
)

set(JNI_SOURCES
    src/main/cpp/jni/network_jni.cpp
)

# Math Library
add_library(ElectricalMath SHARED ${MATH_SOURCES})
set_target_properties(ElectricalMath PROPERTIES WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
target_include_directories(ElectricalMath PUBLIC 
    ${CMAKE_SOURCE_DIR}/src/main/cpp/include
)

# Network Library
add_library(ElectricalNetwork SHARED ${NETWORK_SOURCES})
set_target_properties(ElectricalNetwork PROPERTIES WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
target_link_libraries(ElectricalNetwork PRIVATE ElectricalMath)
target_include_directories(ElectricalNetwork PUBLIC 
    ${CMAKE_SOURCE_DIR}/src/main/cpp/include
)

# JNI Library
add_library(ElectricalJNI SHARED ${JNI_SOURCES})
set_target_properties(ElectricalJNI PROPERTIES WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
target_link_libraries(ElectricalJNI PRIVATE ElectricalNetwork ElectricalMath)
target_include_directories(ElectricalJNI PUBLIC 
    ${CMAKE_SOURCE_DIR}/src/main/cpp/include
    ${JNI_INCLUDE_DIRS}
)

# Set output directories
set_target_properties(ElectricalMath ElectricalNetwork ElectricalJNI PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
    ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
)

# Testing
enable_testing()
add_subdirectory(src/test/cpp)
