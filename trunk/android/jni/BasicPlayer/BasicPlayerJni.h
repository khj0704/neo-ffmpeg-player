#ifndef _SVA_JNI_H
#define _SVA_JNI_H

#include <android/log.h>

#define _ANDROID_LOG_	// 디버그 로그를 찍는다.

#ifdef _ANDROID_LOG_
	#define LOG_TAG	"ffmpeg" // 로그 출력을 위한 태그
	#define LOG		__android_log_write
	#define PRINT	__android_log_print
#endif

#endif // _SVA_JNI_H
