package com.neox.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String LOG_TAG = MainActivity.class.getSimpleName();

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Cursor c = getVideoCursor();
        startManagingCursor(c);
        ContactListItemAdapter adapter = new ContactListItemAdapter(this, R.layout.video_item, c);
        ListView list = (ListView) findViewById(R.id.VideoList);
        list.setAdapter(adapter);
        
        
    }
	final String[] proj = { 
			BaseColumns._ID, 
			MediaColumns.DATA, 
			MediaColumns.DISPLAY_NAME, 
			MediaColumns.TITLE,
			MediaColumns.SIZE, 
			MediaColumns.DATE_ADDED, 
			VideoColumns.DURATION, 
			VideoColumns.RESOLUTION, 
			VideoColumns.DATE_TAKEN 
		};

    static final int VIDEO_ID_COLUMN_INDEX 				= 0;
    static final int VIDEO_DATA_COLUMN_INDEX 			= 1;
    static final int VIDEO_DISPLAY_NAME_COLUMN_INDEX 	= 2;
    static final int VIDEO_TITLE_COLUMN_INDEX 			= 3;
    static final int VIDEO_SIZE_COLUMN_INDEX 			= 4;
    static final int VIDEO_DATE_ADDED_COLUMN_INDEX 		= 5;
    static final int VIDEO_DURATION_COLUMN_INDEX 		= 6;
    static final int VIDEO_RESOLUTION_COLUMN_INDEX 		= 7;
    static final int VIDEO_DATE_TAKEN_COLUMN_INDEX 		= 8;


	private Cursor getVideoCursor() {
		
		final Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		
		final String ascending = " DESC";  //" ASC", " DESC";

		final String dateExpr =
			"case ifnull(" + VideoColumns.DATE_TAKEN + ",0)" +
			" when 0 then " + MediaColumns.DATE_ADDED + "*1000" +
			" else " + VideoColumns.DATE_TAKEN +
			" end";

		final String sortOrder = dateExpr + ascending + ", _id" + ascending;
		
		final Cursor cursor = getContentResolver().query(uri, proj, null, null, sortOrder);

//		if (cursor != null) {
//			if (cursor.moveToFirst()) {
//				final int idCol = cursor.getColumnIndex(BaseColumns._ID);
//				final int dataCol = cursor.getColumnIndex(MediaColumns.DATA);
//				final int displayNameCol = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
//				final int titleCol = cursor.getColumnIndex(MediaColumns.TITLE);
//				final int sizeCol = cursor.getColumnIndex(MediaColumns.SIZE);
//				final int dateCol = cursor.getColumnIndex(MediaColumns.DATE_ADDED);
//				final int durationCol = cursor.getColumnIndex(VideoColumns.DURATION);
//				final int resolutionCol = cursor.getColumnIndex(VideoColumns.RESOLUTION);
//				final int dateTakenCol = cursor.getColumnIndex(VideoColumns.DATE_TAKEN);
//				
//				list.ensureCapacity(cursor.getCount());
//				
//				do {
//					final long id = cursor.getLong(idCol);
//					final String path = cursor.getString(dataCol);
//					final String name = cursor.getString(displayNameCol);
//					final String title = cursor.getString(titleCol);
//					final long size = cursor.getLong(sizeCol);
//					final long date = cursor.getLong(dateCol) * 1000;
//					final long duration = cursor.getLong(durationCol);
//					final String resolution = cursor.getString(resolutionCol);
//					final long dateTaken = cursor.getLong(dateTakenCol);
//					
//					if (name != null) {
//						final LocalFile file = new LocalFile(id, name, path, title, size, date);
//						file.setDuration(duration);
//						file.setResolution(resolution);
//						file.setDateTaken(dateTaken);
//						file.setFileType(FileType.VIDEO);
//						list.add(file);
//					}
//				}
//				while (cursor.moveToNext());
//			}
//			cursor.close();
//		}

		return cursor;
	}
    
    public void startVideo(String path) {
    	LogUtil.e(LOG_TAG, "path : " + path);
    	Intent intent = new Intent(this, FFmpegBasicActivity.class);
    	intent.putExtra("path", path);
    	this.startActivity(intent);
    }
    
    private final class ContactListItemAdapter extends ResourceCursorAdapter {
        public ContactListItemAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameView = (TextView) view.findViewById(R.id.name);

            String name = cursor.getString(VIDEO_DISPLAY_NAME_COLUMN_INDEX);
            String path = cursor.getString(VIDEO_DATA_COLUMN_INDEX);
            String title = cursor.getString(VIDEO_TITLE_COLUMN_INDEX);
            
            LogUtil.e(LOG_TAG, "video name : " + name + ", path : " + path + ", title : " + title);
            nameView.setText(name);
            view.setTag(path);
            
//			final long id = cursor.getLong(idCol);
//			final String path = cursor.getString(dataCol);
//			final String name = cursor.getString(displayNameCol);
//			final String title = cursor.getString(titleCol);
//			final long size = cursor.getLong(sizeCol);
//			final long date = cursor.getLong(dateCol) * 1000;
//			final long duration = cursor.getLong(durationCol);
//			final String resolution = cursor.getString(resolutionCol);
//			final long dateTaken = cursor.getLong(dateTakenCol);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = super.newView(context, cursor, parent);
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText("test");
            view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String path = (String)view.getTag();
					LogUtil.e(LOG_TAG, "path : " + path);
					startVideo(path);
				}
			});

            return view;
        }
    }    
}
