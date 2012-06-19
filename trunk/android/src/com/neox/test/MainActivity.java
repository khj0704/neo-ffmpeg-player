package com.neox.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
    }
    
    public void onClicked(View view) {
    	Log.e("ffmpeg", "button clicked!!!");
    	this.startActivity(new Intent(this, FFmpegBasicActivity.class));
    }
}
