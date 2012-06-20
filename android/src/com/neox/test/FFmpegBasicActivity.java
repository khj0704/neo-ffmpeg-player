package com.neox.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

public class FFmpegBasicActivity extends Activity {
	private static final String LOG_TAG = FFmpegBasicActivity.class.getSimpleName();
	
	
	VideoView videoView;
	FFmpegCodec ffmpeg;
	private float prevX;
	private float prevY;
	
	static Handler _handler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ffmpeg", "FFmpegBasicActivity onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        

        Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		LogUtil.e(LOG_TAG, "path : " + path);

        
        ffmpeg = FFmpegCodec.getInstance(getApplicationContext());
        
//		String path = "/mnt/sdcard/tcloud/video/dd.mp4";
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
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			prevX = x;
			prevY = y;
		}
		else if(event.getAction() == MotionEvent.ACTION_UP) {
			int delta = (int) (x - prevX); 
			if(Math.abs(delta) < 20) {
				ffmpeg.toggle();
			}
			else {
				if(!ffmpeg.isPaused()) {
					if(delta > 0) {
						ffmpeg.seek(10);
					}
					else {
						ffmpeg.seek(-10);
					}
				}
			}
		}
		return super.onTouchEvent(event);
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
