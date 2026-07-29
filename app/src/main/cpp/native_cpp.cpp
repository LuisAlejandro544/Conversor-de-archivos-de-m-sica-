#include "native_cpp.h"
#include <android/log.h>
#include <cmath>
#include <vector>
#include <algorithm>
#include <string>

#define LOG_TAG "NativeCPP"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_native_NativeBridge_getNativeEngineInfo(JNIEnv *env, jobject /* thiz */) {
    std::string info = "AudioLabs C++ Native DSP Engine v2.0 (NDK C++17 SIMD / High-Performance AudioTrack Engine)";
    return env->NewStringUTF(info.c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_processPcmGain(JNIEnv *env, jobject /* thiz */,
                                                      jbyteArray pcmData, jfloat gainDb) {
    if (!pcmData) return nullptr;

    jsize len = env->GetArrayLength(pcmData);
    if (len <= 0) return pcmData;

    jbyte* buffer = env->GetByteArrayElements(pcmData, nullptr);
    if (!buffer) return pcmData;

    float factor = std::pow(10.0f, gainDb / 20.0f);
    int numSamples = len / 2;
    int16_t* samples = reinterpret_cast<int16_t*>(buffer);

    for (int i = 0; i < numSamples; ++i) {
        float sampleVal = static_cast<float>(samples[i]) * factor;
        if (sampleVal > 32767.0f) sampleVal = 32767.0f;
        if (sampleVal < -32768.0f) sampleVal = -32768.0f;
        samples[i] = static_cast<int16_t>(sampleVal);
    }

    env->ReleaseByteArrayElements(pcmData, buffer, 0);
    return pcmData;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_resamplePcm(JNIEnv *env, jobject /* thiz */,
                                                   jbyteArray pcmData, jint srcRate,
                                                   jint destRate, jint channels) {
    if (!pcmData || srcRate <= 0 || destRate <= 0 || channels <= 0) return pcmData;

    jsize len = env->GetArrayLength(pcmData);
    if (len <= 0) return pcmData;

    if (srcRate == destRate) return pcmData;

    jbyte* srcBytes = env->GetByteArrayElements(pcmData, nullptr);
    if (!srcBytes) return pcmData;

    int totalSrcSamples = len / 2;
    int srcFrames = totalSrcSamples / channels;
    double ratio = static_cast<double>(destRate) / static_cast<double>(srcRate);
    int destFrames = static_cast<int>(srcFrames * ratio);
    int totalDestSamples = destFrames * channels;

    std::vector<int16_t> destSamples(totalDestSamples);
    const int16_t* srcSamples = reinterpret_cast<const int16_t*>(srcBytes);

    for (int f = 0; f < destFrames; ++f) {
        double srcFrameIdx = f / ratio;
        int frame0 = static_cast<int>(srcFrameIdx);
        int frame1 = std::min(frame0 + 1, srcFrames - 1);
        double alpha = srcFrameIdx - frame0;

        for (int c = 0; c < channels; ++c) {
            int16_t s0 = srcSamples[frame0 * channels + c];
            int16_t s1 = srcSamples[frame1 * channels + c];
            double interp = (1.0 - alpha) * s0 + alpha * s1;
            destSamples[f * channels + c] = static_cast<int16_t>(std::clamp(interp, -32768.0, 32767.0));
        }
    }

    env->ReleaseByteArrayElements(pcmData, srcBytes, JNI_ABORT);

    jbyteArray result = env->NewByteArray(totalDestSamples * 2);
    if (result) {
        env->SetByteArrayRegion(result, 0, totalDestSamples * 2, reinterpret_cast<const jbyte*>(destSamples.data()));
    }
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_generateAudioBuffer(JNIEnv *env, jobject /* thiz */,
                                                          jint sampleRate, jint channels,
                                                          jdouble durationSec, jdouble freqHz) {
    int totalFrames = static_cast<int>(sampleRate * durationSec);
    int totalSamples = totalFrames * channels;
    int byteSize = totalSamples * 2;

    std::vector<int16_t> samples(totalSamples);
    double twoPiF = 2.0 * M_PI * freqHz;

    for (int f = 0; f < totalFrames; ++f) {
        double t = static_cast<double>(f) / sampleRate;
        double val = std::sin(twoPiF * t) * 0.8;
        int16_t pcmVal = static_cast<int16_t>(val * 32767.0);

        for (int c = 0; c < channels; ++c) {
            samples[f * channels + c] = pcmVal;
        }
    }

    jbyteArray result = env->NewByteArray(byteSize);
    if (result) {
        env->SetByteArrayRegion(result, 0, byteSize, reinterpret_cast<const jbyte*>(samples.data()));
    }
    return result;
}

} // extern "C"
