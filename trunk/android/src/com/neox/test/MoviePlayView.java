package com.neox.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

public class MoviePlayView extends View {
    private Bitmap mBitmap;
	private Context mContext;
	private boolean isOpen;
	private boolean initialized;
	private FFmpegCodec ffmpeg;

    public MoviePlayView(Context context) {
        super(context);
        
        mContext = context;
        
        Log.d("ffmpeg", "MoviePlayView()");
        initialized = false;
        
//        if (initBasicPlayer() < 0) {
//        	Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
//        	
//        	((Activity)context).finish();
//        }
        
        
    }
    
    public void setFFmpegCodec(FFmpegCodec codec) {
    	ffmpeg = codec;
    }

    public void createBitmap(int width, int height) {
    	mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    	initialized = true;
    }
  
    public Bitmap getBitmap() {
    	return mBitmap;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	if(initialized) {
//	    	renderFrame(mBitmap);
//    		Log.e("ffmpeg", "draw!!!");
    		ffmpeg.getVideoFrame(mBitmap);
	        canvas.drawBitmap(mBitmap, 0, 0, null);
	        
	
//	        invalidate();
    	}
    }
//
//    public void playMovie(String path) {
//    	isOpen = false;
//        int openResult = openMovie(path);
//        if (openResult < 0) {
//        	Toast.makeText(mContext, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
//        	
////        	((Activity)mContext).finish();
//        }
//        else {
//        	mBitmap = Bitmap.createBitmap(getMovieWidth(), getMovieHeight(), Bitmap.Config.RGB_565);
//        	isOpen = true;
//        }
//    }
//    
//    public void stopMovie() {
//    	closeMovie();
//    }
    

}
