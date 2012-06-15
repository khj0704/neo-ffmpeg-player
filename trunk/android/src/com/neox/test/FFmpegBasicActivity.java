package com.neox.test;

import static android.opengl.GLES10.GL_TEXTURE_2D;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1Util;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import com.neox.test.FFmpegCodec.VideoFrameDecoder;

public class FFmpegBasicActivity extends Activity implements VideoFrameDecoder {
	
	MoviePlayView playView;
//	private boolean mStopAudioThreads;
	FFmpegCodec ffmpeg;
	private GLSurfaceView mGLView;
	private Bitmap mBitmap;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("ffmpeg", "FFmpegBasicActivity onCreate()");
        super.onCreate(savedInstanceState);
//        mGLView = new GLSurfaceView(this);
//        mGLView.setEGLConfigChooser(false);
//        StaticTriangleRenderer.TextureLoader loader;
//        loader = new SyntheticCompressedTextureLoader();
//        mGLView.setRenderer(new StaticTriangleRenderer(this, loader));
//        setContentView(mGLView);
        
        
        
        
        
        ffmpeg = FFmpegCodec.getInstance();
        ffmpeg.setVideoDecodeListner(this);
        
        ffmpeg.openMovie();
        
        
        playView = new MoviePlayView(this);
        setContentView(playView);
        playView.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight());
        playView.setFFmpegCodec(ffmpeg);
//        ffmpeg.setDisplay(playView);

//    	mBitmap = Bitmap.createBitmap(ffmpeg.getWidth(), ffmpeg.getHeight(), Bitmap.Config.RGB_565);
        
		ffmpeg.startAudioDecodeThread();
		ffmpeg.startVideoDecodeThread();
		ffmpeg.startPacketReaderThread();
		
    }

	@Override
	public void decoded() {
//		Log.e("ffmpeg", "listener.decoded called");
		
//    	ffmpeg.getVideoFrame(playView.getBitmap());
		playView.getHandler().post(new Runnable() {
			@Override
			public void run() {
				playView.invalidate();
			}
		});
    	
		
//		mGLView.requestRender();
	}
    
	@Override
	protected void onResume() {
		Log.e("ffmpeg", "FFmpegBasicActivity onResume()");
		super.onResume();
//		mGLView.onResume();
//        String path = "/mnt/sdcard/tcloud/video/dd.mp4";
//		playView.playMovie(path);
//		startAudioThreads();
	}

	@Override
	protected void onPause() {
		Log.e("ffmpeg", "FFmpegBasicActivity onPause()");
		super.onPause();
//		mGLView.onPause();
//		playView.stopMovie();
//		stopAudioThreads();
	}

	@Override
	public void onBackPressed() {
		Log.e("ffmpeg", "FFmpegBasicActivity onBackPressed()");
		super.onBackPressed();
		ffmpeg.closeMovie();
		
		ffmpeg.stopAllThread();
//		ffmpeg.stopDecode();
		
//		ffmpeg.stopPacketReaderThread();
//		ffmpeg.stopAudioDecodeThread();
//		ffmpeg.stopVideoDecodeThread();
		
	}

	@Override
	protected void onDestroy() {
		Log.e("ffmpeg", "FFmpegBasicActivity onDestroy()");
		super.onDestroy();
	}
	

    /**
     * Demonstrate how to create a compressed texture on the fly.
     */
    private class SyntheticCompressedTextureLoader implements StaticTriangleRenderer.TextureLoader {
        public void load(GL10 gl) {
        	
        	ffmpeg.getVideoFrame(mBitmap);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, mBitmap, 0);
        	
             // Test the ETC1Util APIs for reading and writing compressed textures to I/O streams.
//            try {
//                byte[] videoData = ffmpeg.getVideoFrame();
//                Log.e("ffmpeg", "!!!!!!!!!!!!!!ffmpeg.getVideoFrame Buffer Size : " + videoData.length);
//                Bitmap bmp = BitmapFactory.decodeByteArray(videoData, 0, videoData.length);
//                if(bmp != null) {
//	                GLUtils.texImage2D(GL_TEXTURE_2D, 0, bmp, 0);
//	                bmp.recycle();
//                }
//                else {
//                    Log.e("ffmpeg", "bmp is null!!!!!!!!!!!!!!!!!");
//                }
                
                
//                ByteArrayInputStream bis = null;
//                if(videoData != null) {
////                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//                    bis = new ByteArrayInputStream(videoData);
//                }
//                ETC1Util.loadTexture(GLES10.GL_TEXTURE_2D, 0, 0,
//                        GLES10.GL_RGB, GLES10.GL_UNSIGNED_SHORT_5_6_5, bis);
                
                
                
                
//            } catch (IOException e) {
//                Log.w("ffmpeg", "Could not load texture: " + e);
//            }
        }

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
