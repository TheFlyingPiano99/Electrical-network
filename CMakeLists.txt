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

# JNI Library (self-contained linear system solver)
add_library(ElectricalJNI SHARED src/main/cpp/jni/network_jni.cpp)
set_target_properties(ElectricalJNI PROPERTIES WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
target_include_directories(ElectricalJNI PUBLIC ${JNI_INCLUDE_DIRS})
# Set output directories
set_target_properties(ElectricalJNI PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
    ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
)

# Testing
enable_testing()
add_subdirectory(src/test/cpp)
