/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include "avcodec.h"
#include "avformat.h"
#include "swscale.h"
#include "BasicPlayer.h"
#include "BasicPlayerJni.h"
#include "mySDL.h"


AVFormatContext *gFormatCtx = NULL;

AVCodecContext *gVideoCodecCtx = NULL;
AVCodecContext *gAudioCodecCtx = NULL;

AVCodec *gVideoCodec = NULL;
AVCodec *gAudioCodec = NULL;

int gVideoStreamIdx = -1;
int gAudioStreamIdx = -1;

AVFrame *gFrame = NULL;
AVFrame *gFrameRGB = NULL;

struct SwsContext *gImgConvertCtx = NULL;

int gPictureSize = 0;
uint8_t *gVideoBuffer = NULL;


enum PixelFormat desiredPictureFormat = PIX_FMT_RGB565;



extern JNIEnv *g_Env;
extern jobject g_thiz;

char buff[1024];

void my_log(void* avcl, int level, const char *fmt, va_list vl) 
{
	sprintf(buff, fmt, vl);
	jstring msg = (*g_Env)->NewStringUTF(g_Env, buff);
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, msg);
}


typedef struct PacketQueue {
	AVPacketList *first_pkt, *last_pkt;
	int nb_packets;
	int size;
	SDL_mutex *mutex;
	SDL_cond *cond;
} PacketQueue;

PacketQueue audioq;
PacketQueue videoq;

int quit = 0;

void packet_queue_init(PacketQueue *q) {
	memset(q, 0, sizeof(PacketQueue));
	q->mutex = SDL_CreateMutex();
	q->cond = SDL_CreateCond();
}

int packet_queue_put(PacketQueue *q, AVPacket *pkt) 
{
	AVPacketList *pkt1;
	if(av_dup_packet(pkt) < 0) {
		return -1;
	}
	pkt1 = av_malloc(sizeof(AVPacketList));
	if (!pkt1)
		return -1;
	pkt1->pkt = *pkt;
	pkt1->next = NULL;
  
  
	SDL_LockMutex(q->mutex);
  
	if (!q->last_pkt)
		q->first_pkt = pkt1;
	else
		q->last_pkt->next = pkt1;
	q->last_pkt = pkt1;
	q->nb_packets++;
	q->size += pkt1->pkt.size;
	SDL_CondSignal(q->cond);
  
	SDL_UnlockMutex(q->mutex);
	return 0;
}


static int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block)
{
	AVPacketList *pkt1;
	int ret;
  
	SDL_LockMutex(q->mutex);
  
	for(;;) {
    
		if(quit) {
			ret = -1;
			LOGE("packet_queue_get, quit flag is set, return -1!!!");	
			break;
		}

		pkt1 = q->first_pkt;
		if (pkt1) {
			q->first_pkt = pkt1->next;
			if (!q->first_pkt)
				q->last_pkt = NULL;
			q->nb_packets--;
			q->size -= pkt1->pkt.size;
			*pkt = pkt1->pkt;
			av_free(pkt1);
			ret = 1;
			break;
		} else if (!block) {
			ret = 0;
			break;
		} else {
			LOGE("packet_queue_get, queue is empty, wait!!!");	
			SDL_CondWait(q->cond, q->mutex);
			LOGE("packet_queue_get, wait ended!!!");	
		}
	}
	SDL_UnlockMutex(q->mutex);
	return ret;
}

int openMovie(const char filePath[])
{
/*
	int i;

	if (gFormatCtx != NULL) {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "gFormatCtx != null");	
		return -1;
	}

	if (av_open_input_file(&gFormatCtx, filePath, NULL, 0, NULL) != 0) {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "av_open_input_file failed!!!");	
		return -2;
	}

	if (av_find_stream_info(gFormatCtx) < 0) {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "av_find_stream_info failed.");	
		return -3;
	}

	LOGE("audio format: %s", gFormatCtx->iformat->name);
	LOGI("audio bitrate: %d", gFormatCtx->bit_rate);	
	
	gVideoStreamIdx = -1;
	gAudioStreamIdx = -1;



	gAudioStreamIdx = av_find_best_stream(gFormatCtx, AVMEDIA_TYPE_AUDIO, -1, -1, &gAudioCodec, 0);
	LOGI("audioStreamIndex %d", gAudioStreamIdx);
	if (gAudioStreamIdx == AVERROR_STREAM_NOT_FOUND) {
		LOGE("cannot find a audio stream");
		exit(1);
	} else if (gAudioStreamIdx == AVERROR_DECODER_NOT_FOUND) {
		LOGE("audio stream found, but no decoder is found!");
		exit(1);
	}
	LOGI("audio codec: %s", gAudioCodec->name);

	gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIdx]->codec;
	if (avcodec_open(gAudioCodecCtx, gAudioCodec) < 0) {
		LOGE("cannot open the audio codec!");
		exit(1);
	}
	

	packet_queue_init(&audioq);

	return 0;
*/	


	int i;

	if (gFormatCtx != NULL) {
		LOGE("gFormatCtx != null");	
		return -1;
	}

	if (av_open_input_file(&gFormatCtx, filePath, NULL, 0, NULL) != 0) {
		LOGE("av_open_input_file failed!!!");	
		return -2;
	}

	if (av_find_stream_info(gFormatCtx) < 0) {
		LOGE("av_find_stream_info failed.");	
		return -3;
	}

//	av_log_set_callback(my_log);
	dump_format(gFormatCtx, 0, filePath, 0);

	LOGW("audio format: %s", gFormatCtx->iformat->name);
	LOGW("audio bitrate: %d", gFormatCtx->bit_rate);	
	
	gVideoStreamIdx = -1;
	gAudioStreamIdx = -1;

	for (i = 0; i < gFormatCtx->nb_streams; i++) {
		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO && gVideoStreamIdx < 0) {
			gVideoStreamIdx = i;
			LOGE("video codec index [%d]", gVideoStreamIdx);
		}
		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO && gAudioStreamIdx < 0) {
			gAudioStreamIdx = i;
			LOGE("audio codec index [%d]", gAudioStreamIdx);
		}
		
	}
	if (gVideoStreamIdx == -1) {
		LOGE("gVideoStreamIdx == -1");	
		return -4;
	}

	if (gAudioStreamIdx == -1) {
		LOGE("gAudioStreamIdx == -1");	
		return -4;
	}

	gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIdx]->codec;


	gVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
	if (gVideoCodec == NULL) {
		LOGE("avcodec_find_decoder failed!!!");	
		return -5;
	}


	gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIdx]->codec;
	gAudioCodec = avcodec_find_decoder(gAudioCodecCtx->codec_id);
	if (gAudioCodec == NULL) {
		LOGE("avcodec_find_decoder failed(audio)!!!");	
		return -5;
	}


	if (avcodec_open(gVideoCodecCtx, gVideoCodec) < 0) {
		LOGE("avcodec_open failed!!!");	
		return -6;
	}

	if (avcodec_open(gAudioCodecCtx, gAudioCodec) < 0) {
		LOGE("avcodec_open failed(audio)!!!");	
		return -6;
	}

	gFrame = avcodec_alloc_frame();
	if (gFrame == NULL) {
		LOGE("avcodec_alloc_frame failed!!!!");	
		return -7;
	}

	gFrameRGB = avcodec_alloc_frame();
	if (gFrameRGB == NULL) {
		LOGE("avcodec_alloc_frame failed!!!");	
		return -8;
	}


//	gPictureSize = avpicture_get_size(PIX_FMT_RGB565LE, gVideoCodecCtx->width, gVideoCodecCtx->height);
	gPictureSize = avpicture_get_size(desiredPictureFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);
	gVideoBuffer = (uint8_t *)av_malloc(gPictureSize * sizeof(uint8_t));
//	gVideoBuffer = (uint8_t*)(malloc(sizeof(uint8_t) * gPictureSize));

	avpicture_fill((AVPicture*)gFrameRGB, gVideoBuffer, desiredPictureFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);
//	avpicture_fill((AVPicture*)gFrameRGB, gVideoBuffer, PIX_FMT_RGB565LE, gVideoCodecCtx->width, gVideoCodecCtx->height);
	
	PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "openMovie Success!!!! [%s]", filePath);	


	packet_queue_init(&audioq);
	packet_queue_init(&videoq);

	return 0;
	
}

int audio_decode_frame(AVCodecContext *aCodecCtx, uint8_t *audio_buf, int buf_size) 
{

	AVPacket pkt; // 이유는 잘 모르弱愍립� static 이면 안 됨.
//	static uint8_t *audio_pkt_data = NULL;
//	static int audio_pkt_size = 0;

	int len, data_size;
  
	for(;;) {
		if(packet_queue_get(&audioq, &pkt, 1) < 0) {
			LOGE("packet_queue_get failed!!!");
			return -1;
		}
		PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "packet_queue_get, Audio Queue Size[%d], total data size[%d]", audioq.nb_packets, audioq.size);	

		while(pkt.size > 0) {
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "audio_decode_frame, packet size [%d]!!!", pkt.size);	
			data_size = buf_size;
			len = avcodec_decode_audio3(aCodecCtx, (int16_t *)audio_buf, &data_size, &pkt);
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "avcodec_decode_audio3, return [%d]!!!", len);	
					  
			if(len < 0) {
				// if error, skip frame 
				break;
			}
			pkt.data += len;
			pkt.size -= len;
			
			if(data_size <= 0) {
				// No data yet, get more frames 
				continue;
			}
			// We have data, return it and come back for more later 
			return data_size;
			LOGI("audio_decode_frame loop 1");
		}
		if(pkt.data)
			av_free_packet(&pkt);

		if(quit) {
			LOGE("audio_decode_frame, quit flag is set, return -1!!!");	
			return -1;
		}
		LOGI("audio_decode_frame loop 2");
	}


/*
	static AVPacket pkt = {0,};
	static uint8_t *audio_pkt_data = NULL;
	static int audio_pkt_size = 0;

	int len, data_size;
  
	for(;;) {
		while(pkt.size > 0) {
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "audio_decode_frame, packet size [%d]!!!", pkt.size);	
			data_size = buf_size;
			len = avcodec_decode_audio3(aCodecCtx, (int16_t *)audio_buf, &data_size, &pkt);
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "avcodec_decode_audio3, return [%d]!!!", len);	
					  
			if(len < 0) {
				// if error, skip frame 
				break;
			}
            pkt.data += len;
            pkt.size -= len;
			
			if(data_size <= 0) {
				// No data yet, get more frames 
				continue;
			}
			// We have data, return it and come back for more later 
			return data_size;
		}
		if(pkt.data)
			av_free_packet(&pkt);

		if(quit) {
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "audio_decode_frame, quit flag is set, return -1!!!");	
			return -1;
		}

		if(packet_queue_get(&audioq, &pkt, 1) < 0) {
			return -1;
		}
		PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "packet_queue_get, Audio Queue Size[%d], total data size[%d]", audioq.nb_packets, audioq.size);	
	}
*/	
}


int readPacket()
{
	AVPacket        packet;
	if(av_read_frame(gFormatCtx, &packet)>=0) {
	// Is this a packet from the video stream?
		if(packet.stream_index==gVideoStreamIdx) {
			// Decode video frame
//			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "VideoFrame!!!!, packet data size[%d]", packet.size);	
			packet_queue_put(&videoq, &packet);
		} else if(packet.stream_index==gAudioStreamIdx) {
//			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "AudioFrame!!!!, packet data size[%d]", packet.size);	
			packet_queue_put(&audioq, &packet);
//			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "AudioFrame!!!!, queue size[%d], total data size[%d]", audioq.nb_packets, audioq.size);	
		} else {
			PRINT(ANDROID_LOG_DEBUG, LOG_TAG, "ElseFrame!!!![%d]", packet.stream_index);	
			av_free_packet(&packet);
		}
		return 0;
	}
	else {
		 if(url_ferror(&gFormatCtx->pb) == 0) {
			LOGE("no ferror!!!!");	
			SDL_Delay(100); /* no error; wait for user input */
			return 0;
		  } 
		 else {
			LOGE("av_read_frame failed!!!!");	
			return -1;
		  }
	}
}



static void pushAudioData(uint8_t *audioData, int audioSize)
{
	jobject obj = g_thiz;
	jclass cls = 0;
	jmethodID mid = 0;


//	if ((*g_Env)->PushLocalFrame(g_Env, 16) < 0)
//		return;

	cls = (*g_Env)->GetObjectClass(g_Env, obj); 
	if (cls)
	{
		mid = (*g_Env)->GetMethodID(g_Env, cls, "playAudioFrame", "([BI)V");
		if (mid)
		{
			jbyteArray jAudioBuffer;
			jbyte *jAudioArray;

			jAudioBuffer = (*g_Env)->NewByteArray(g_Env, audioSize);

			jAudioArray = (*g_Env)->GetByteArrayElements(g_Env, jAudioBuffer, NULL);
			memcpy(jAudioArray, audioData, audioSize); 
			(*g_Env)->ReleaseByteArrayElements(g_Env, jAudioBuffer, jAudioArray, 0);

			(*g_Env)->CallObjectMethod(g_Env, obj, mid, jAudioBuffer, audioSize);


			// 에뮬레이터에서는 아래 문장 넣어야 정상동작함. 에뮬 버그로 보임.
//			(*g_Env)->DeleteGlobalRef(g_env, aVCard); // JNI 버그인지 모르겠으나 Global Ref Table에도 추가된다. 그래서 명시적으로 Delete 해준다.

		}
		else {
			LOGE("GetMethodID() failed.");
		}	
	}
	else {
		LOGE("GetObjectClass() failed.");
	}	

//	(*g_Env)->PopLocalFrame(g_Env, NULL);
}

static uint8_t audio_buf[(AVCODEC_MAX_AUDIO_FRAME_SIZE * 3) / 2];

void decodeAudio() 
{
	int audio_size;
	static unsigned int audio_buf_size = 0;
	static unsigned int audio_buf_index = 0;
	
	
	quit = 0;


	LOGI("start audio_decode_frame");	
	audio_size = audio_decode_frame(gAudioCodecCtx, audio_buf, sizeof(audio_buf));

	LOGI("end audio_decode_frame, decoded audio size[%d]", audio_size);	
	
	if(audio_size < 0) {
		/* If error, output silence */
		LOGE("decoded audio size is minus");	
		audio_buf_size = 1024; // arbitrary?
		memset(audio_buf, 0, audio_buf_size);
	} 
	else {
		audio_buf_size = audio_size;
		LOGI("pushAudioData[%d]", audio_size);	
		pushAudioData(audio_buf, audio_size);
    }
    audio_buf_index = 0;
}

void stopDecode()
{
	quit = 1;
	SDL_Delay(1000);
	SDL_CondSignal(audioq.cond);
	SDL_CondSignal(videoq.cond);
}

static void pushVideoData(uint8_t *videoData, int videoSize)
{
	jobject obj = g_thiz;
	jclass cls = 0;
	jmethodID mid = 0;


//	if ((*g_Env)->PushLocalFrame(g_Env, 16) < 0)
//		return;

	cls = (*g_Env)->GetObjectClass(g_Env, obj); 
	if (cls)
	{
		mid = (*g_Env)->GetMethodID(g_Env, cls, "displayVideoFrame", "([BI)V");
		if (mid)
		{
			jbyteArray jVideoBuffer;
			jbyte *jVideoArray;

			jVideoBuffer = (*g_Env)->NewByteArray(g_Env, videoSize);

			jVideoArray = (*g_Env)->GetByteArrayElements(g_Env, jVideoBuffer, NULL);
			memcpy(jVideoArray, videoData, videoSize); 
			(*g_Env)->ReleaseByteArrayElements(g_Env, jVideoBuffer, jVideoArray, 0);

			(*g_Env)->CallObjectMethod(g_Env, obj, mid, jVideoBuffer, videoSize);


			// 에뮬레이터에서는 아래 문장 넣어야 정상동작함. 에뮬 버그로 보임.
//			(*g_Env)->DeleteGlobalRef(g_env, aVCard); // JNI 버그인지 모르겠으나 Global Ref Table에도 추가된다. 그래서 명시적으로 Delete 해준다.

		}
		else {
			LOGE("GetMethodID() failed.");
		}	
	}
	else {
		LOGE("GetObjectClass() failed.");
	}	

//	(*g_Env)->PopLocalFrame(g_Env, NULL);
}

int getVideoFrame(jobject jbitmap) 
{
//	jbyteArray videoFrameDataArray;
//	jbyte *videoFrameDataPtr;
/*
	videoFrameData = (*g_Env)->NewByteArray(g_Env, gPictureSize);
	(*g_Env)->SetByteArrayRegion(g_Env, videoFrameData, 0, gPictureSize, gFrameRGB->data[0]);
	(*g_Env)->ReleaseByteArrayElements(g_Env, videoFrameData, gFrameRGB->data[0], 0);
*/


//	videoFrameDataArray = (*g_Env)->NewByteArray(g_Env, gPictureSize);
//	videoFrameDataPtr = (*g_Env)->GetByteArrayElements(g_Env, videoFrameDataArray, NULL);
//	memcpy(videoFrameDataPtr, gFrameRGB->data[0], gPictureSize); 
//	(*g_Env)->ReleaseByteArrayElements(g_Env, videoFrameDataArray, videoFrameDataPtr, 0);

	

//	return videoFrameDataArray;	

	void *pixels;
	if ( AndroidBitmap_lockPixels(g_Env, jbitmap, &pixels) < 0) {
		LOGE("jni-AndroidBitmap_lockPixels failed!!!");
		return -1;
	}

	copyPixels((uint8_t*)pixels);

	AndroidBitmap_unlockPixels(g_Env, jbitmap);

	return 0;
}

extern jobject g_bitmap;

void decodeVideo() 
{
	AVPacket pkt; // 이유는 잘 모르弱愍립� static 이면 안 됨.
	int frameFinished = 0;
	
	for(;;) {
		if(quit) {
			LOGE("decodeVideo, quit flag is set, return -1!!!");	
			return -1;
		}
		
		if(packet_queue_get(&videoq, &pkt, 1) < 0) {
			LOGE("packet_queue_get failed!!!");
			return -1;
		}

		avcodec_decode_video2(gVideoCodecCtx, gFrame, &frameFinished, &pkt);
		
		if (frameFinished) {
			gImgConvertCtx = sws_getCachedContext(gImgConvertCtx,
				gVideoCodecCtx->width, gVideoCodecCtx->height, gVideoCodecCtx->pix_fmt,
				gVideoCodecCtx->width, gVideoCodecCtx->height, desiredPictureFormat, SWS_BICUBIC, NULL, NULL, NULL);
//				gVideoCodecCtx->width, gVideoCodecCtx->height, PIX_FMT_RGB565LE, SWS_BICUBIC, NULL, NULL, NULL);
			
			sws_scale(gImgConvertCtx, gFrame->data, gFrame->linesize, 0, gVideoCodecCtx->height, gFrameRGB->data, gFrameRGB->linesize);
			
			av_free_packet(&pkt);

			pushVideoData(gFrameRGB->data[0], gPictureSize);

/*			

			void *pixels;
			if ( AndroidBitmap_lockPixels(g_Env, g_bitmap, &pixels) < 0) {
				LOGE("AndroidBitmap_lockPixels failed!!!");
				return -1;
			}

			copyPixels((uint8_t*)pixels);

			AndroidBitmap_unlockPixels(g_Env, g_bitmap);

			
*/
			return 0;
		}
		av_free_packet(&pkt);

		
	}

}


void copyPixels(uint8_t *pixels)
{
	memcpy(pixels, gFrameRGB->data[0], gPictureSize);
}

int getWidth()
{
	return gVideoCodecCtx->width;
}

int getHeight()
{
	return gVideoCodecCtx->height;
}

void closeMovie()
{
	LOGE("jni-closeMovie() called, exit");

	exit(1); // when didn't call exit, it doesn't work!!

	if (gVideoBuffer != NULL) {
		av_free(gVideoBuffer);
		gVideoBuffer = NULL;
	}
	
	if (gFrame != NULL)
		av_freep(gFrame);
	if (gFrameRGB != NULL)
		av_freep(gFrameRGB);



	if (gVideoCodecCtx != NULL) {
		avcodec_close(gVideoCodecCtx);
		gVideoCodecCtx = NULL;
	}
	
	if (gAudioCodecCtx != NULL) {
		avcodec_close(gAudioCodecCtx);
		gAudioCodecCtx = NULL;
	}
	
	if (gFormatCtx != NULL) {
		av_close_input_file(gFormatCtx);
		gFormatCtx = NULL;
	}

	LOGE("jni-closeMovie() ended!!!");

//	exit(1);
}



int decodeFrame()
{
	int frameFinished = 0;
	AVPacket packet;
	
	while (av_read_frame(gFormatCtx, &packet) >= 0) {
		if (packet.stream_index == gVideoStreamIdx) {
			avcodec_decode_video2(gVideoCodecCtx, gFrame, &frameFinished, &packet);
			
			if (frameFinished) {
				gImgConvertCtx = sws_getCachedContext(gImgConvertCtx,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gVideoCodecCtx->pix_fmt,
					gVideoCodecCtx->width, gVideoCodecCtx->height, PIX_FMT_RGB565LE, SWS_BICUBIC, NULL, NULL, NULL);
				
				sws_scale(gImgConvertCtx, gFrame->data, gFrame->linesize, 0, gVideoCodecCtx->height, gFrameRGB->data, gFrameRGB->linesize);
				
				av_free_packet(&packet);
		
				return 0;
			}
		}
		
		av_free_packet(&packet);
	}

	return -1;
}


