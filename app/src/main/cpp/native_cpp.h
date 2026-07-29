#ifndef NATIVE_CPP_H
#define NATIVE_CPP_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL
Java_com_example_native_NativeBridge_getNativeEngineInfo(JNIEnv *env, jobject thiz);

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_processPcmGain(JNIEnv *env, jobject thiz, jbyteArray pcmData, jfloat gainDb);

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_resamplePcm(JNIEnv *env, jobject thiz, jbyteArray pcmData, jint srcRate, jint destRate, jint channels);

JNIEXPORT jbyteArray JNICALL
Java_com_example_native_NativeBridge_generateAudioBuffer(JNIEnv *env, jobject thiz, jint sampleRate, jint channels, jdouble durationSec, jdouble freqHz);

#ifdef __cplusplus
}
#endif

#endif // NATIVE_CPP_H
