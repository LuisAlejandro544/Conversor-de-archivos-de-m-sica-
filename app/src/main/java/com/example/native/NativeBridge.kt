package com.example.native

/**
 * Bridge layer for native interoperability with C, C++, and Rust in Android.
 *
 * Loads native dynamic libraries (.so) compiled via NDK / Rust JNI tooling:
 * 1. "native_c"   -> C module (Direct system calls & OS ABI)
 * 2. "native_cpp" -> C++ module (High-performance audio processing)
 * 3. "native_rust" -> Rust module (Memory-safe concurrency & DSP)
 */
object NativeBridge {

    init {
        try {
            System.loadLibrary("native_c")
            System.loadLibrary("native_cpp")
            System.loadLibrary("native_rust")
        } catch (e: UnsatisfiedLinkError) {
            // Log or handle missing library loading in development environment
            e.printStackTrace()
        }
    }

    // Native JNI declarations will be registered here.
}
