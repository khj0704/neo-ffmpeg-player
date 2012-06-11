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

jint Java_com_neox_test_MoviePlayView_initBasicPlayer(JNIEnv *env, jobject thiz)
{
	if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM && (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
		av_register_all();
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "av_register_all()");	
		return 0;
	}
	else {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "family not match!!!");	
		return -1;
	}
}

jint Java_com_neox_test_MoviePlayView_openMovie(JNIEnv *env, jobject thiz, jstring filePath)
{
	const jbyte *str;
	int result;

	
	str = (*env)->GetStringUTFChars(env, filePath, NULL);

	result = openMovie(str);

	(*env)->ReleaseStringUTFChars(env, filePath, str);

	return result;
}

jint Java_com_neox_test_MoviePlayView_renderFrame(JNIEnv *env, jobject thiz, jobject bitmap)
{
    void *pixels;
	int result;

	if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
		return result;

	decodeFrame();
	copyPixels((uint8_t*)pixels);

	AndroidBitmap_unlockPixels(env, bitmap);
}

jint Java_com_neox_test_MoviePlayView_getMovieWidth(JNIEnv *env, jobject thiz)
{
	return getWidth();
}

jint Java_com_neox_test_MoviePlayView_getMovieHeight(JNIEnv *env, jobject thiz)
{
	return getHeight();
}

void Java_com_neox_test_MoviePlayView_closeMovie(JNIEnv *env, jobject thiz)
{
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "MoviePlayView closeMovie()");	
	closeMovie();
}

void Java_com_neox_test_FFmpegBasicActivity_jniReadPacket(JNIEnv *env, jobject thiz)
{
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "jniReadPacket()");	
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
