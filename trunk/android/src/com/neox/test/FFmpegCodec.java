package com.neox.test;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class FFmpegCodec {

	private Thread mPacketReaderThread;
	private Thread mAudioDecodeThread;
	private static AudioTrack track;	
	
	
	
	private static FFmpegCodec thiz = null;

	
	private FFmpegCodec() {
		initAudio();
		jniInitBasicPlayer();
	};
	
	private void initAudio() {
		int bufSize = AudioTrack.getMinBufferSize(44100,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);

		track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
	}

	public static FFmpegCodec getInstance() {
		if(thiz  == null) {
			thiz = new FFmpegCodec();
		}
		return thiz;
	}
	
	public void openMovie() {
		String path = "/mnt/sdcard/tcloud/video/dd.mp4";
		jniOpenMovie(path);
	}

	public void closeMovie() {
		jniCloseMovie();
	}
	
	private boolean stopPacketReaderThread;	
	 
	Runnable packetReaderThread = new Runnable() {
		public void run() {
	        while (!stopPacketReaderThread) {
	        	if(jniReadPacket() < 0) {
	        		Log.e("ffmpeg", "jniReadPacket() failed, break");
	        		break;
	        	}
//	        	try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
	        }
	    }
	};	

	void startPacketReaderThread()
	{
		stopPacketReaderThread = false;
	    mPacketReaderThread = new Thread(packetReaderThread);
	    mPacketReaderThread.start();
	}
	
	void stopPacketReaderThread()
	{
	    stopPacketReaderThread = true;
	    
	    try {
	        mPacketReaderThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.d("ffmpeg", "PacketReaderThread End");
	}

	private boolean stopAudioDecodeThread;
	
	Runnable audioDecodeThread = new Runnable() {
		public void run() {
	        while (!stopAudioDecodeThread) {
	        	jniDecodeAudio();
//	        	try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
	        	Log.e("ffmpeg", "loop");
	        }
	    }
	};


	void startAudioDecodeThread()
	{
		stopAudioDecodeThread = false;
	    mAudioDecodeThread = new Thread(audioDecodeThread);
	    mAudioDecodeThread.start();
	}
	
	public void bridageDecodeAudio() {
		jniDecodeAudio();
	}

	void stopAudioDecodeThread()
	{
		track.stop();
		jniStopDecodeAudio();
		stopAudioDecodeThread = true;
	    
	    try {
	    	mAudioDecodeThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.d("ffmpeg", "audioDecodeThread End");
	}
		
	public void playAudioFrame(final byte[] audioData, final int size) {
		Log.d("ffmpeg", "java - playAudioFrame - [" + size + "]");
		
	    //android.util.Log.v("ROHAUPT", "RAH Playing"); 
	    if(track.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING) {   
	        track.play();
	    }
	    track.write(audioData, 0, size); 
	}
	
    static {
        System.loadLibrary("basicplayer");
    }
	
    public native int jniInitBasicPlayer();
	public native int jniOpenMovie(String filePath);
	
	public native int jniReadPacket();	
	public native void jniDecodeAudio();	
	public native void jniStopDecodeAudio();	
	
	public native int jniRenderFrame(Bitmap bitmap);
	public native int jniGetMovieWidth();
	public native int jniGetMovieHeight();
	public native void jniCloseMovie();
	
	public native void jniAudioFillStreamBuffer(short[] streamBuffer, int bufferSize);	
	
}
