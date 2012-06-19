package com.neox.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

public class FFmpegBasicActivity extends Activity {
	VideoView videoView;
	FFmpegCodec ffmpeg;
	
	static Handler _handler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ffmpeg", "FFmpegBasicActivity onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        ffmpeg = FFmpegCodec.getInstance(getApplicationContext());
        
		String path = "/mnt/sdcard/tcloud/video/dd.mp4";
        if(!ffmpeg.openVideo(path)) {
        	Log.e("ffmpeg", "ffmpeg.openVideo Failed [" + path + "]");
        	finish();
        	return;
        }
        
        ffmpeg.setVideoDecodeEndListener(new FFmpegCodec.VideoDecodeEndListener() {
			
			@Override
			public void decodeEnded() {
				Log.e("ffmpeg", "Video Decode Ended, finish Activity!!!!!!");
				_handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						ffmpeg.closeVideo();
						finish();
					}
				}, 1000);
				
			}
		});
        
        VideoView videoView = new VideoView(getApplicationContext(), ffmpeg);
        setContentView(videoView);

		ffmpeg.startDecodeThread();
		ffmpeg.startAudioThread();
		ffmpeg.startVideoThread();
		ffmpeg.setVideoDisplayTimer(40, 0);
		
    }

	@Override
	protected void onResume() {
		Log.e("ffmpeg", "FFmpegBasicActivity onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.e("ffmpeg", "FFmpegBasicActivity onPause()");
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		Log.e("ffmpeg", "FFmpegBasicActivity onBackPressed()");
		ffmpeg.closeVideo();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		Log.e("ffmpeg", "FFmpegBasicActivity onDestroy()");
		super.onDestroy();
	}
	
}
