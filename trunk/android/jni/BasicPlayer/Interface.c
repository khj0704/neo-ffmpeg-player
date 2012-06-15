/*
 * jni for Android
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#include <jni.h>
#include <android/bitmap.h>
#include <cpu-features.h>
#include "BasicPlayer.h"
#include "BasicPlayerJni.h"

JNIEnv *g_Env = NULL;
jobject g_thiz = NULL;
jobject g_bitmap = NULL;

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniInitBasicPlayer(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM && (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
		avcodec_init();
		av_register_all();
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "av_register_all()");	
		return 0;
	}
	else {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "family not match!!!");	
		return -1;
	}
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniOpenMovie(JNIEnv *env, jobject thiz, jstring filePath)
{
	const jbyte *str;
	int result;

	g_Env = env;
	g_thiz = thiz;
	

	str = (*env)->GetStringUTFChars(env, filePath, NULL);

	result = openMovie(str);

	(*env)->ReleaseStringUTFChars(env, filePath, str);
	
	
	return result;
	
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniRenderFrame(JNIEnv *env, jobject thiz, jobject bitmap)
{
    void *pixels;
	int result;

	g_Env = env;
	g_thiz = thiz;

	if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
		return result;

	decodeFrame();
	copyPixels((uint8_t*)pixels);

	AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniGetMovieWidth(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	return getWidth();
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniGetMovieHeight(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	return getHeight();
}

JNIEXPORT void JNICALL Java_com_neox_test_FFmpegCodec_jniCloseMovie(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "MoviePlayView closeMovie()");	
	closeMovie();
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniReadPacket(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
//	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "jniReadPacket()");	
	return readPacket();
}

JNIEXPORT void JNICALL Java_com_neox_test_FFmpegCodec_jniDecodeAudio(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;

	decodeAudio();
}

JNIEXPORT void JNICALL Java_com_neox_test_FFmpegCodec_jniDecodeVideo(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;

	decodeVideo();
}

JNIEXPORT void JNICALL Java_com_neox_test_FFmpegCodec_jniStopDecode(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	stopDecode();
}

JNIEXPORT jint JNICALL Java_com_neox_test_FFmpegCodec_jniSetBitmap(JNIEnv *env, jobject thiz, jobject bitmap)
{
    void *pixels;
	int result;

	g_Env = env;
	g_thiz = thiz;

	g_bitmap = bitmap;

	if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
		return result;

	decodeFrame();
	copyPixels((uint8_t*)pixels);

	AndroidBitmap_unlockPixels(env, bitmap);
}

JNIEXPORT jbyteArray JNICALL Java_com_neox_test_FFmpegCodec_jniGetVideoFrame(JNIEnv *env, jobject thiz, jobject bitmap)
{
	g_Env = env;
	g_thiz = thiz;

	getVideoFrame(bitmap);
}


/*
void Java_com_neox_test_FFmpegBasicActivity_audioFillStreamBuffer(JNIEnv* env, void* reserved, jshortArray location, jint size)
{
    // Get the short* pointer from the Java array
    jboolean isCopy = JNI_TRUE;
    jshort* dst = env->GetShortArrayElements(location, &isCopy);

    gAudioManager.FillStreamBuffer(dst, size);

    // Release the short* pointer
    env->ReleaseShortArrayElements(location, dst, 0);
}

*/
