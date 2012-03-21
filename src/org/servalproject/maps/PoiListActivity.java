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
package org.servalproject.maps;

import org.servalproject.maps.provider.PointsOfInterestContract;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * an activity to display a list of points of interest
 */
public class PoiListActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "PoiListActivity";
	
	/*
	 * private class level variables
	 */
	private long defaultPoiMaxAge = 43200 * 1000;
	private volatile long poiMaxAge = defaultPoiMaxAge;
	
	private Cursor cursor;
	
	/*
	 * create the activity
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_list);
		
		// get the data
		cursor = getCursor();
		
		if(cursor == null) {
			Log.e(TAG, "a null cursor was returned when looking up POI info");
			Toast.makeText(getApplicationContext(), R.string.poi_list_ui_toast_missing_data, Toast.LENGTH_LONG).show();
			finish();
		}
		
		// define the map between columns and layout elements
		String[] mColumnNames = new String[1];
		mColumnNames[0] = PointsOfInterestContract.Table.TITLE;
		
		int[] mLayoutElements = new int[1];
		mLayoutElements[0] = R.id.poi_list_ui_enty_title;
		
		// create the data adapter
		SimpleCursorAdapter mDataAdapter = new SimpleCursorAdapter(
				this,
				R.layout.poi_list_entry, 
				cursor, 
				mColumnNames, 
				mLayoutElements);
		
		setListAdapter(mDataAdapter);
		
		// get a reference to the list view
		ListView mListView = getListView();
		mListView.setTextFilterEnabled(true); // allow filtering by the user by adding in content
		mListView.setOnItemClickListener(this);
	}
	
	/*
	 * get the required data and populate the cursor
	 */
	private Cursor getCursor() {
		
		// get the desired maximum age of the poi information
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        String mPreference = mPreferences.getString("preferences_map_max_poi_age", null);
		if(mPreference != null) {
			poiMaxAge = Long.parseLong(mPreference) * 1000;
		}
		
		// get the data
		String[] mProjection = new String[2];
		mProjection[0] = PointsOfInterestContract.Table._ID;
		mProjection[1] = PointsOfInterestContract.Table.TITLE;
		
		// determine if we need to restrict the list of POIs
		String mSelection = null;
		String[] mSelectionArgs = null;
		
		// restrict the poi content returned if required
		if(poiMaxAge != -1000) {
			mSelection = PointsOfInterestContract.Table.TIMESTAMP + " > ? ";
			mSelectionArgs = new String[1];
			mSelectionArgs[0] = Long.toString(System.currentTimeMillis() - poiMaxAge);
		}
		
		// determine the order by
		String mOrderBy = PointsOfInterestContract.Table.TITLE + " ASC";
		
		// get a content resolver
		ContentResolver mContentResolver = getApplicationContext().getContentResolver();
		
		// get the data
		return mContentResolver.query(
				PointsOfInterestContract.CONTENT_URI, 
				mProjection, 
				mSelection, 
				mSelectionArgs,
				mOrderBy);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		
		// play nice and close the cursor
		cursor.close();
		cursor = null;
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		
		// get the data
		cursor = getCursor();
		super.onResume();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// play nice and close the cursor if necessary
		if(cursor != null) {
			cursor.close();
			cursor = null;
		}
		
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		if(V_LOG) {
			Log.v(TAG, "item clicked at position: " + position);
		}
		
		// work out the id of the item
		if(cursor.moveToPosition(position) == true) {
			
			if(V_LOG) {
				Log.v(TAG, "item in cursor has id: " + cursor.getInt(cursor.getColumnIndex(PointsOfInterestContract.Table._ID)));
			}
			
			Intent mIntent = new Intent(this, org.servalproject.maps.PoiInfoActivity.class);
			mIntent.putExtra("recordId", cursor.getInt(cursor.getColumnIndex(PointsOfInterestContract.Table._ID)));
			startActivity(mIntent);
			
		} else {
			Log.e(TAG, "unable to match list item position to poi id");
			Toast.makeText(getApplicationContext(), R.string.poi_list_ui_tiast_missing_poi_id, Toast.LENGTH_LONG).show();
			finish();
		}
		
	}
}
