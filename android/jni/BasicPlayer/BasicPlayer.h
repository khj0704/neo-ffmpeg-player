/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#ifndef __BASIC_PLAYER_H__
#define __BASIC_PLAYER_H__


int openVideo(const char *filePath);

void decode();

int getWidth();
int getHeight();

int videoThread() ;

int getPicture(jobject jbitmap) ;

int video_refresh_timer(jobject jbitmap);

void streamSeek(int pos);


// return: == 0 - success
//          < 0 - error code
//int openMovie(const char filePath[]);

// return: == 0 - success
//         != 0 then end of movie or fail

/*
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

*/

#endif
