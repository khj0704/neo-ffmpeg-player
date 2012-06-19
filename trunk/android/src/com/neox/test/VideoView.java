package com.neox.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class VideoView extends View {
    private Bitmap mBitmap;
	private boolean initialized;
	private FFmpegCodec ffmpeg;
	private Handler mHandler;

    private VideoView(Context context) {
        super(context);
        Log.d("ffmpeg", "MoviePlayView()");
        initialized = false;
    }
    
    public VideoView(Context context, FFmpegCodec ffmpeg) {
		this(context);
		setFFmpegCodec(ffmpeg);
		mHandler = new Handler(context.getMainLooper());
	}

	public void setFFmpegCodec(FFmpegCodec codec) {
    	ffmpeg = codec;
    	mBitmap = Bitmap.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight(), Bitmap.Config.RGB_565);
    	ffmpeg.setVideoView(this);
    	initialized = true;
    }
	
	Runnable refreshRunnable = new Runnable() {
		@Override
		public void run() {
			if(initialized) {
//				Log.i("ffmpeg", "refreshRunnable is called, invalidate!!!");
				invalidate();
			}
			else {
				Log.e("ffmpeg", "refreshRunnable is called, not initialized!!!");
				scheduleRefresh(100, 1);
			}
		}
	};
	
	Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			if(initialized) {
//				Log.i("ffmpeg", "timerRunnable is called, refresh!!!");
				ffmpeg.refreshVideo(mBitmap);
			}
			else {
				Log.e("ffmpeg", "timerRunnable is called, not initialized!!!");
				scheduleRefresh(100, 1);
			}
		}
	};
	
	public void scheduleRefresh(int delay, int invalidate) {
//		Log.i("ffmpeg", "refreshVideo is called with delay [" + delay + "]ms");
		mHandler.removeCallbacks(refreshRunnable);
		mHandler.removeCallbacks(timerRunnable);
		mHandler.postDelayed(invalidate==1?refreshRunnable:timerRunnable, delay);
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	if(initialized) {
//    		Log.i("ffmpeg", "draw!!!, width[" + mBitmap.getWidth() + "], height[" + mBitmap.getHeight() + "]");
    		if(ffmpeg.getWidth() != mBitmap.getWidth() || ffmpeg.getHeight() != mBitmap.getHeight()) {
    			mBitmap.recycle();
    			mBitmap = Bitmap.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight(), Bitmap.Config.RGB_565);
    		}
    		
    		ffmpeg.refreshVideo(mBitmap); 
   	        canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), 
   					new Rect(0, 0, 800, 480), null);
	        
//	        SDL_Rect rect;
//	        VideoPicture *vp;
//	        AVPicture pict;
//	        float aspect_ratio;
//	        int w, h, x, y;
//	        int i;
//
//	        vp = &is->pictq[is->pictq_rindex];
//	        if(vp->bmp) {
//	          if(is->video_st->codec->sample_aspect_ratio.num == 0) {
//	            aspect_ratio = 0;
//	          } else {
//	            aspect_ratio = av_q2d(is->video_st->codec->sample_aspect_ratio) *
//	      	is->video_st->codec->width / is->video_st->codec->height;
//	          }
//	          if(aspect_ratio <= 0.0) {
//	            aspect_ratio = (float)is->video_st->codec->width /
//	      	(float)is->video_st->codec->height;
//	          }
//	          // apparently this assumption is bad
//	          h = screen->h;
//	          w = ((int)rint(h * aspect_ratio)) & -3;
//	          if(w > screen->w) {
//	            w = screen->w;
//	            h = ((int)rint(w / aspect_ratio)) & -3;
//	          }
//	          x = (screen->w - w) / 2;
//	          y = (screen->h - h) / 2;
//	          rect.x = x;
//	          rect.y = y;
//	          rect.w = w;
//	          rect.h = h;
//	          SDL_DisplayYUVOverlay(vp->bmp, &rect);	        
    	}
    }

}
