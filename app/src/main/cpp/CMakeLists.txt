cmake_minimum_required(VERSION 3.18.1)

project("audiolabs_native")

# Support both C and C++
enable_language(C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)

# C native module
add_library(
    native_c_lib
    SHARED
    native_c.c
)

# C++ native module
add_library(
    native_cpp_lib
    SHARED
    native_cpp.cpp
)

find_library(
    log-lib
    log
)

find_library(
    OpenSLES-lib
    OpenSLES
)

target_link_libraries(
    native_c_lib
    ${log-lib}
    ${OpenSLES-lib}
)

target_link_libraries(
    native_cpp_lib
    ${log-lib}
    ${OpenSLES-lib}
)
