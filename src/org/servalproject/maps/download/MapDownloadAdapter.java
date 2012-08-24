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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.servalproject.maps.R;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * class used to map the JSON objects in the mirror list to views for display
 */
public class MapDownloadAdapter extends BaseAdapter implements ListAdapter {
	
	/*
	 * public class level constants
	 */
	public static final String TAG = "MapDownloadAdapter";
	
	/*
	 * private class level variables
	 */
	private Activity activity;
	private JSONArray items;
	
	/**
	 * constructs a new array adapter using the items in the supplied JSON Array as source elements
	 * 
	 * @param activity a reference to the parent activity
	 * @param items the items on the JSONArray
	 */
	public MapDownloadAdapter(Activity activity, JSONArray items) {
		
		this.activity = activity;
		this.items = items;
		
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return items.length();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public JSONObject getItem(int arg0) {
		// return the item at this position
		return items.optJSONObject(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int arg0) {
		// return the id of the item at this position
		JSONObject jsonObject = getItem(arg0);
        return jsonObject.optLong("id");
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// check to see if there is a view that can be reused
		if(convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.map_download_entry, null);
		}
		
		// get the data
		JSONObject mItem = getItem(position);
	
		// populate the view
		// file name
		TextView mTextView = (TextView) convertView.findViewById(R.id.map_download_ui_entry_title);
		
		try{
			mTextView.setText(mItem.getString("fileName"));
		} catch (JSONException e) {
			Log.e(TAG, "file list item at id '" + position + "' was missing the fileName element");
			mTextView.setText(R.string.misc_not_available);
		}
		
		// file size
		mTextView = (TextView) convertView.findViewById(R.id.map_mirror_ui_entry_size);
		
		try{
			
			Long mFileSize = Long.valueOf(mItem.getString("fileSize"));
			
			mTextView.setText(FileUtils.humanReadableByteCount(mFileSize, true));
		} catch (JSONException e) {
			Log.e(TAG, "file list item at id '" + position + "' was missing the fileSize element");
			mTextView.setText(R.string.misc_not_available);
		} catch (NumberFormatException e) {
			Log.e(TAG, "file list item at id '" + position + "' had invalid fileSize element");
			mTextView.setText(R.string.misc_not_available);
		}
		
		// generated date
		mTextView = (TextView) convertView.findViewById(R.id.map_download_ui_entry_generated);
		
		try{
			mTextView.setText(TimeUtils.formatDateSimple(mItem.getString("fileDate")));
		} catch (JSONException e) {
			Log.e(TAG, "file list item at id '" + position + "' was missing the fileDate element");
			mTextView.setText(R.string.misc_not_available);
		}
		
		// top coordinates
		mTextView = (TextView) convertView.findViewById(R.id.map_download_ui_entry_bbox_top);
		String mCoordinates = activity.getString(R.string.misc_not_available);
		
		try{
			mCoordinates = mItem.getString("maxLatitude") + "," + mItem.getString("maxLongitude");
		} catch (JSONException e) {
			Log.e(TAG, "file list item at id '" + position + "' was missing the min coordinate elements");
		}
		
		mTextView.setText(mCoordinates);
		
		// bottom coordinates
		mTextView = (TextView) convertView.findViewById(R.id.map_download_ui_entry_bbox_bottom);
		mCoordinates = activity.getString(R.string.misc_not_available);
		
		try{
			
			mCoordinates = mItem.getString("minLatitude") + "," + mItem.getString("minLongitude");
		} catch (JSONException e) {
			Log.e(TAG, "file list item at id '" + position + "' was missing the min coordinates elements");
		}
		
		mTextView.setText(mCoordinates);
		
		return convertView;
	}

}
