package com.neox.test;

import com.neox.test.FFmpegCodec.VideoFrameDecoder;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class FFmpegCodec {

	public interface VideoFrameDecoder {
		public void decoded();
	}
	
	
	private Thread mPacketReaderThread;
	private Thread mAudioDecodeThread;
	private Thread mVideoDecodeThread;
	private VideoFrameDecoder mListener;
	
	private static AudioTrack track;	
	
	
	
	private static FFmpegCodec thiz = null;

	
	private FFmpegCodec() {
		initAudio();
		jniInitBasicPlayer();
	};

	public void setVideoDecodeListner(VideoFrameDecoder listender) {
		mListener = listender;
	}
	
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

	public void stopDecode() {
		jniStopDecode();
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
    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<<packetReaderThread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
	    }
	};	

	void startPacketReaderThread()
	{
		stopPacketReaderThread = false;
	    mPacketReaderThread = new Thread(packetReaderThread);
	    mPacketReaderThread.start();
	}
	
	
	void stopAllThread() {

		stopAudioDecodeThread = true;
	    stopVideoDecodeThread = true;
	    stopPacketReaderThread = true;
		
	    stopAudioDecodeThread();
	    stopVideoDecodeThread();
	    stopPacketReaderThread();
	}
	
	void stopPacketReaderThread()
	{
	    Log.e("ffmpeg", "try stop PacketReaderThread");
		
	    stopPacketReaderThread = true;
	    
	    try {
	        mPacketReaderThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.e("ffmpeg", "PacketReaderThread End");
	}

	private boolean stopAudioDecodeThread;
	
	Runnable audioDecodeThread = new Runnable() {
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	        while (!stopAudioDecodeThread) {
	        	jniDecodeAudio();
//	        	try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//	        	Log.e("ffmpeg", "loop");
	        }
    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<<audioDecodeThread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
	    }
	};
	private MoviePlayView mPlayView;


	void startAudioDecodeThread()
	{
		stopAudioDecodeThread = false;
	    mAudioDecodeThread = new Thread(audioDecodeThread);
	    mAudioDecodeThread.start();
	}
	
	void stopAudioDecodeThread()
	{
	    Log.e("ffmpeg", "try stop AudioDecodeThread");
		
		track.stop();
		stopAudioDecodeThread = true;
	    
	    try {
	    	mAudioDecodeThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.e("ffmpeg", "audioDecodeThread End");
	}
		
	public void playAudioFrame(final byte[] audioData, final int size) {
		Log.d("ffmpeg", "java - playAudioFrame - [" + size + "]");
		
	    //android.util.Log.v("ROHAUPT", "RAH Playing"); 
	    if(track.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING) {   
	        track.play();
	    }
//	    track.write(audioData, 0, size); 
	}

	
	public void displayVideoFrame(final byte[] videoData, final int size) {
//		Log.e("ffmpeg", "java - displayVideoFrame - [" + size + "]");
		if(mListener != null) {
			mListener.decoded();
		}
		
//		mPlayView.getHandler().post(new Runnable() {
//			@Override
//			public void run() {
//				mPlayView.invalidate();
//			}
//		});
	}
	
	
	public void setDisplay(MoviePlayView playView) {
		mPlayView = playView;
		jniSetBitmap(playView.getBitmap());
		
	}	
	
	private boolean stopVideoDecodeThread;
	
	Runnable videoDecodeThread = new Runnable() {
		public void run() {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	        while (!stopVideoDecodeThread) {
	        	jniDecodeVideo();
	        	try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
    		Log.e("ffmpeg", "<<<<<<<<<<<<<<<<videoDecodeThread Ended!!!!!>>>>>>>>>>>>>>>>>>>>");
	    }
	};


	void startVideoDecodeThread()
	{
		stopVideoDecodeThread = false;
	    mVideoDecodeThread = new Thread(videoDecodeThread);
	    mVideoDecodeThread.start();
	}
	
	void stopVideoDecodeThread()
	{
	    Log.e("ffmpeg", "try stop VideoDecodeThread");
		
	    stopVideoDecodeThread = true;
	    
	    try {
	    	mVideoDecodeThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.e("ffmpeg", "videoDecodeThread End");
	}
	
	public int getWidth() {
		return jniGetMovieWidth();
	}

	public int getHeight() {
		return jniGetMovieHeight();
	}

	public void getVideoFrame(Bitmap bitmap) {
		jniGetVideoFrame(bitmap);
	}
	
    static {
        System.loadLibrary("basicplayer");
    }
	
    public native int jniInitBasicPlayer();
	public native int jniOpenMovie(String filePath);
	
	public native int jniReadPacket();	
	public native void jniDecodeAudio();	
	public native void jniDecodeVideo();
	public native void jniStopDecode();
	
	public native int jniSetBitmap(Bitmap bitmap);
	
	public native int jniRenderFrame(Bitmap bitmap);
	public native int jniGetMovieWidth();
	public native int jniGetMovieHeight();
	public native void jniCloseMovie();
	
	public native void jniAudioFillStreamBuffer(short[] streamBuffer, int bufferSize);

	public native void jniGetVideoFrame(Bitmap bitmap);


}
