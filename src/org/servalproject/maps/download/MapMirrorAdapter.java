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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * class used to map the JSON objects in the mirror list to views for display
 */
public class MapMirrorAdapter extends BaseAdapter implements ListAdapter {
	
	/*
	 * public class level constants
	 */
	public static final String TAG = "MapMirrorAdapter";
	
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
	public MapMirrorAdapter(Activity activity, JSONArray items) {
		
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
//		LinearLayout mItemLayout;
//		
//		if(convertView == null) {
//			
//			mItemLayout = new LinearLayout(activity);
//			LayoutInflater mLayoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			mLayoutInflater.inflate(R.layout.map_mirror_entry, mItemLayout);
//
//		} else {
//			
//			mItemLayout = (LinearLayout) convertView;
//		}
		
		if(convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.map_mirror_entry, null);
		}
		
		// get the data
		JSONObject mItem = getItem(position);
		
		// populate the view
		// mirror name
		TextView mTextView = (TextView) convertView.findViewById(R.id.map_mirror_ui_entry_title);
		
		try {
			mTextView.setText(mItem.getString("name"));
		} catch (JSONException e) {
			Log.e(TAG, "mirror list item at id '" + position + "' was missing the name element");
			mTextView.setText(R.string.misc_not_available);
			
		}
		
		// mirror description
		mTextView = (TextView) convertView.findViewById(R.id.map_mirror_ui_entry_desc);
		
		try {
			mTextView.setText(mItem.getString("description"));
		} catch (JSONException e) {
			Log.e(TAG, "mirror list item at id '" + position + "' was missing the description element");
			mTextView.setText(R.string.misc_not_available);
		}
		
		// mirror location
		mTextView = (TextView) convertView.findViewById(R.id.map_mirror_ui_entry_location);
		
		try {
			mTextView.setText(mItem.getString("location"));
		} catch (JSONException e) {
			Log.e(TAG, "mirror list item at id '" + position + "' was missing the location element");
			mTextView.setText(R.string.misc_not_available);
		}
		
		return convertView;
	}

}
