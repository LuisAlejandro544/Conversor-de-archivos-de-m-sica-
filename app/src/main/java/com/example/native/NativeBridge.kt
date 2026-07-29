package com.example.native

/**
 * Bridge layer for native interoperability with C++ in Android.
 *
 * Loads native dynamic library "native_cpp" compiled via NDK for high-performance audio DSP & decoding.
 */
object NativeBridge {

    val isNativeLoaded: Boolean

    init {
        isNativeLoaded = try {
            System.loadLibrary("native_cpp")
            true
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
            false
        }
    }

    external fun getNativeEngineInfo(): String
    external fun processPcmGain(pcmData: ByteArray, gainDb: Float): ByteArray
    external fun resamplePcm(pcmData: ByteArray, srcRate: Int, destRate: Int, channels: Int): ByteArray
    external fun generateAudioBuffer(sampleRate: Int, channels: Int, durationSec: Double, freqHz: Double): ByteArray
}
