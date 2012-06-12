package com.neox.test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

public class FFmpegBasicActivity extends Activity {
	
	MoviePlayView playView;
//	private boolean mStopAudioThreads;
	FFmpegCodec ffmpeg;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("ffmpeg", "FFmpegBasicActivity onCreate()");
        
        playView = new MoviePlayView(this);
        ffmpeg = FFmpegCodec.getInstance();
        ffmpeg.openMovie();
        
        setContentView(playView);
    }

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ffmpeg", "FFmpegBasicActivity onResume()");
//        String path = "/mnt/sdcard/tcloud/video/dd.mp4";
//		playView.playMovie(path);
//		startAudioThreads();
		ffmpeg.startPacketReaderThread();
		ffmpeg.startAudioDecodeThread();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("ffmpeg", "FFmpegBasicActivity onPause()");
//		playView.stopMovie();
//		stopAudioThreads();
		ffmpeg.stopPacketReaderThread();
		ffmpeg.stopAudioDecodeThread();
	}

	@Override
	protected void onDestroy() {
		ffmpeg.closeMovie();
		super.onDestroy();
	}
	
	
	
	
//	Runnable mStreams = new Runnable()
//	{
//	    private AudioTrack mMusicTrack;
//
//		public void run()
//	    {
//	        // Create a streaming AudioTrack for music playback
//	        int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
//	        int bufferSize = 4 * minBufferSize;
//	        short[] streamBuffer = new short[bufferSize / 2];
//	        mMusicTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
//	        mMusicTrack.play();
//
//	        while (!mStopAudioThreads)
//	        {
//	            // Fill buffer with PCM data from C++
//	            audioFillStreamBuffer(streamBuffer, bufferSize);
//
//	            // Stream PCM data into the music AudioTrack
//	            mMusicTrack.write(streamBuffer, 0, bufferSize / 2);
//	        }
//
//	        mMusicTrack.flush();
//	        mMusicTrack.stop();
//	    }
//	};

	
	
}
