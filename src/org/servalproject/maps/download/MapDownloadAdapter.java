/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Maps Software
 *
 * Serval Maps Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.maps.download;

import org.servalproject.maps.R;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * class used to map the JSON objects in the mirror list to views for display
 */
public class MapDownloadAdapter extends SimpleCursorAdapter {
	
	/*
	 * public class level constants
	 */
	public static final String TAG = "MapDownloadAdapter";
	
	/*
	 * private class level variables
	 */
	private int[] to;
	
	
	public MapDownloadAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.to = to;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView mTextView;
		
		// populate the view
		for(int i = 0; i < to.length; i++) {
			
			switch(to[i]) {
			case R.id.map_download_ui_entry_title:
				mTextView = (TextView) view.findViewById(R.id.map_download_ui_entry_title);
				mTextView.setText(cursor.getString(cursor.getColumnIndex(MapFileTableContract.NAME)));
				break;
			case R.id.map_mirror_ui_entry_size_txt:
				mTextView = (TextView) view.findViewById(R.id.map_mirror_ui_entry_size_txt);
				
				Long mFileSize = cursor.getLong(cursor.getColumnIndex(MapFileTableContract.SIZE));
				mTextView.setText(FileUtils.humanReadableByteCount(mFileSize, true));
				break;
			case R.id.map_download_ui_entry_generated_txt:
				mTextView = (TextView) view.findViewById(R.id.map_download_ui_entry_generated_txt);
				
				Long mTimestamp = cursor.getLong(cursor.getColumnIndex(MapFileTableContract.TIMESTAMP));
				mTextView.setText(TimeUtils.formatDateSimple(mTimestamp));
				break;
			case R.id.map_download_ui_entry_bbox_top_txt:
				mTextView = (TextView) view.findViewById(R.id.map_download_ui_entry_bbox_top_txt);
				
				Double maxLat = cursor.getDouble(cursor.getColumnIndex(MapFileTableContract.MAX_LATITUDE));
				Double maxLng = cursor.getDouble(cursor.getColumnIndex(MapFileTableContract.MAX_LONGITUDE));
				
				mTextView.setText(maxLat + "," + maxLng);
				break;
			case R.id.map_download_ui_entry_bbox_bottom_txt:
				mTextView = (TextView) view.findViewById(R.id.map_download_ui_entry_bbox_bottom_txt);
				
				Double minLat = cursor.getDouble(cursor.getColumnIndex(MapFileTableContract.MIN_LATITUDE));
				Double minLng = cursor.getDouble(cursor.getColumnIndex(MapFileTableContract.MIN_LONGITUDE));
				
				mTextView.setText(minLat + "," + minLng);
				break;
			}
	
		}
	}
}
