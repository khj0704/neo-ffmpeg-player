package com.neox.test;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

public class FFmpegBasicActivity extends Activity {
	
	MoviePlayView playView;
	private boolean mStopAudioThreads;
	Thread mStreamThread;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("ffmpeg", "FFmpegBasicActivity onCreate()");
        
        playView = new MoviePlayView(this);
        setContentView(playView);
    }

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ffmpeg", "FFmpegBasicActivity onResume()");
//        String path = "/mnt/sdcard/tcloud/video/dd.mp4";
//		playView.playMovie(path);
//		startAudioThreads();
        startPacketReaderThread();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("ffmpeg", "FFmpegBasicActivity onPause()");
//		playView.stopMovie();
//		stopAudioThreads();
		stopPacketReaderThread();
	}
	
	Runnable mStreams = new Runnable()
	{
	    private AudioTrack mMusicTrack;

		public void run()
	    {
	        // Create a streaming AudioTrack for music playback
	        int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
	        int bufferSize = 4 * minBufferSize;
	        short[] streamBuffer = new short[bufferSize / 2];
	        mMusicTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
	        mMusicTrack.play();

	        while (!mStopAudioThreads)
	        {
	            // Fill buffer with PCM data from C++
	            audioFillStreamBuffer(streamBuffer, bufferSize);

	            // Stream PCM data into the music AudioTrack
	            mMusicTrack.write(streamBuffer, 0, bufferSize / 2);
	        }

	        mMusicTrack.flush();
	        mMusicTrack.stop();
	    }
	};
	protected boolean stopPacketReaderThread;	
 
	Runnable packetReaderThread = new Runnable() {
		public void run() {
	        while (!stopPacketReaderThread) {
	        	jniReadPacket();
	        	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	    }
	};	
	
	void startAudioThreads()
	{
	    mStopAudioThreads = false;
	    mStreamThread = new Thread(mStreams);
	    mStreamThread.start();
	}
	
	void stopAudioThreads()
	{
//	    mStopAudioThreads = true;
	    stopPacketReaderThread = true;
	    
	    try {
	        mStreamThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	    }
	}

	
	void startPacketReaderThread()
	{
		stopPacketReaderThread = false;
	    mStreamThread = new Thread(packetReaderThread);
	    mStreamThread.start();
	}
	
	void stopPacketReaderThread()
	{
	    stopPacketReaderThread = true;
	    
	    try {
	        mStreamThread.join();
	    }
	    catch (final Exception ex) {
	        ex.printStackTrace();
	        Log.d("ffmpeg", ex.getMessage());
	    }
	    Log.d("ffmpeg", "PacketReaderThread End");
	}	
	public static native void audioFillStreamBuffer(short[] streamBuffer, int bufferSize);	
	public static native void jniReadPacket();	
	
}
