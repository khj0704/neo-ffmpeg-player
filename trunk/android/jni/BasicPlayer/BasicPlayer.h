/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#ifndef BASICPLAYER_H__INCED__110326
#define BASICPLAYER_H__INCED__110326


// return: == 0 - success
//          < 0 - error code
int openMovie(const char filePath[]);

// return: == 0 - success
//         != 0 then end of movie or fail
int decodeFrame();

void copyPixels(uint8_t *pixels);

int getWidth();
int getHeight();

void closeMovie();

int readPacket();
void decodeAudio();
void decodeVideo() ;
void stopDecode();
int getVideoFrame(jobject jbitmap); 

#define LOGD(...) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
#define LOGI(...) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGW(...) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
#define LOGE(...) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}


#endif
