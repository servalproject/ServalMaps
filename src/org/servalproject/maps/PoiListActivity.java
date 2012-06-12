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

import java.util.Arrays;

import org.servalproject.maps.provider.PointsOfInterestContract;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * an activity to display a list of points of interest
 */
public class PoiListActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * public class level constants
	 */
	public final static int TAG_SELECTED_RESULT = 1;
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "PoiListActivity";
	
	private final String PREFERENCE_NAME = "preferences_poi_list_sort";
	private final String DEFAULT_SORT_FIELD = PointsOfInterestContract.Table.TITLE;
	
	/*
	 * private class level variables
	 */
	private long defaultPoiMaxAge = 43200 * 1000;
	private volatile long poiMaxAge = defaultPoiMaxAge;
	
	private Cursor cursor;
	private PoiListAdapter dataAdapter;
	private ListView listView;
	private String[] columnNames;
	
	private String hasTag = null;
	
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
        
        // define the list of columns
		columnNames = new String[6];
		columnNames[0] = PointsOfInterestContract.Table.TITLE;
		columnNames[1] = PointsOfInterestContract.Table.TIMESTAMP;
		columnNames[2] = PointsOfInterestContract.Table.TIMEZONE;
		columnNames[3] = PointsOfInterestContract.Table.LATITUDE;
		columnNames[4] = PointsOfInterestContract.Table.LONGITUDE;
		columnNames[5] = PointsOfInterestContract.Table.TAGS;
		
		// get the desired maximum age of the poi information
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        String mPreference = mPreferences.getString("preferences_map_max_poi_age", null);
		if(mPreference != null) {
			poiMaxAge = Long.parseLong(mPreference) * 1000;
		}
		
		// get the data
		cursor = getCursor();
		
		if(cursor == null) {
			Log.e(TAG, "a null cursor was returned when looking up POI info");
			Toast.makeText(getApplicationContext(), R.string.poi_list_ui_toast_missing_data, Toast.LENGTH_LONG).show();
			finish();
		}
		
		dataAdapter = getDataAdapter(cursor);
		
		setListAdapter(dataAdapter);
		
		// get a reference to the list view
		listView = getListView();
		
		listView.setOnItemClickListener(this);
	}
	
	/*
	 * get the data adapter
	 */
	private PoiListAdapter getDataAdapter(Cursor cursor) {
		
		// define the map between columns and layout elements
		// ensure that this is the same size as the list of columns
		int[] mLayoutElements = new int[6];
		mLayoutElements[0] = R.id.poi_list_ui_entry_title;
		mLayoutElements[1] = R.id.poi_list_ui_entry_age;
		mLayoutElements[2] = PoiListAdapter.PLACE_HOLDER;
		mLayoutElements[3] = R.id.poi_list_ui_txt_distance;
		mLayoutElements[4] = PoiListAdapter.PLACE_HOLDER;
		mLayoutElements[5] = R.id.poi_list_ui_txt_tags;
		
		return new PoiListAdapter(
			this,
			R.layout.poi_list_entry, 
			cursor, 
			columnNames, 
			mLayoutElements);
	}
	
	
	/*
	 * get the required data and populate the cursor using the default sort order
	 */
	private Cursor getCursor() {
		return getCursor(getPreference(), null);
	}
	
	/*
	 * get the required data and populate the cursor
	 */
	private Cursor getCursor(String sortField, String selectedTag) {
		
		// get the data
		String[] mProjection = new String[columnNames.length + 1];
		mProjection[0] = PointsOfInterestContract.Table._ID;

		for(int i = 1; i < mProjection.length; i++) {
			mProjection[i] = columnNames[i-1];
		}
		
		// determine if we need to restrict the list of POIs
		String mSelection = null;
		String[] mSelectionArgs = null;
		
		// restrict the poi content returned if required
		if(poiMaxAge != -1000) {
			mSelection = PointsOfInterestContract.Table.TIMESTAMP + " > ? ";
			mSelectionArgs = new String[1];
			mSelectionArgs[0] = Long.toString(System.currentTimeMillis() - poiMaxAge);
		}
		
		// add the tag filter if necessary
		if(selectedTag != null && mSelection != null) {
			mSelection = mSelection + " AND " + PointsOfInterestContract.Table.TAGS + " GLOB ? ";
			String mTemp = new String();
			mTemp = mSelectionArgs[0];
			mSelectionArgs = new String[2];
			mSelectionArgs[0] = mTemp;
			mSelectionArgs[1] = "*" + selectedTag + "*";
		} else if (selectedTag != null ) {
			mSelection = PointsOfInterestContract.Table.TAGS + " GLOB ? ";
			mSelectionArgs = new String[1];
			mSelectionArgs[0] = "*" + selectedTag + "*";
		}
		
		if(V_LOG) {
			Log.v(TAG, "selection statement: " + mSelection);
			Log.v(TAG, "selection args: " + Arrays.toString(mSelectionArgs));
		}
		
		// determine the order by
		String mOrderBy = null;
		
		if(sortField.equals(PointsOfInterestContract.Table.TITLE) == true) {
			mOrderBy = sortField + " ASC";
		} else {
			mOrderBy = sortField + " DESC";
		}
		
		if(V_LOG) {
			Log.v(TAG, "order by statement: " + mOrderBy);
		}
		
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
	 * create the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu based on the XML
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.poi_list_activity, menu);
	    return true;
	}
	
	/*
	 * handle click events from the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.menu_poi_list_activity_sort_alpha:
			// sort list of items by title alphabetically
			cursor.close();
			cursor = null;
			cursor = getCursor(PointsOfInterestContract.Table.TITLE, hasTag);
			
			updatePreference(PointsOfInterestContract.Table.TITLE);
			
			dataAdapter = getDataAdapter(cursor);
			
			listView.setAdapter(dataAdapter);

			return true;
		case R.id.menu_poi_list_activity_sort_time:
			// sort list of items by time
			cursor.close();
			cursor = null;
			cursor = getCursor(PointsOfInterestContract.Table.TIMESTAMP, hasTag);
			
			updatePreference(PointsOfInterestContract.Table.TIMESTAMP);
			
			dataAdapter = getDataAdapter(cursor);
			
			listView.setAdapter(dataAdapter);
			
			return true;
		case R.id.menu_poi_list_activity_view_tags:
			// view a list of tags
			Intent mIntent = new Intent(this, org.servalproject.maps.TagListActivity.class);
			startActivityForResult(mIntent, 0);
			return true;
		case R.id.menu_poi_list_activity_close:
			// close this activity
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * private method to update a preference on the sort order
	 */
	private void updatePreference(String prefValue) {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		SharedPreferences.Editor mEditor = mPreferences.edit();
		
		mEditor.putString(PREFERENCE_NAME, prefValue);
		
		// TODO once on API 9 or above use apply not commit
		mEditor.commit();
	}
	
	/*
	 * private method to get the preference on the sort order
	 */
	private String getPreference() {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		return mPreferences.getString(PREFERENCE_NAME, DEFAULT_SORT_FIELD);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		// check to see if we're coming back from the tag list
		if(resultCode == TAG_SELECTED_RESULT) {
			
			TextView mTextView = (TextView) findViewById(R.id.poi_list_ui_lbl_subheading);
			String mTag = null;
			
			if(data != null) {
			
				mTag = data.getStringExtra("tag");
				
				Log.d(TAG, mTag);
			} 
			
			// check to see if a tag is present
			if(mTag != null) {
				
				// filter the list of POIs based on the tag
				
				if(cursor != null) {
					cursor.close();
					cursor = null;
				}
				
				cursor = getCursor(getPreference(), mTag);
				
				dataAdapter = getDataAdapter(cursor);
				
				listView.setAdapter(dataAdapter);
				
				mTextView.setText(String.format(getString(R.string.poi_list_ui_lbl_subheading), mTag));
				mTextView.setVisibility(View.VISIBLE);
				
				// set a flag to indicate that there is a tag filter in place
				hasTag = mTag;
				
			} else {
				
				if(cursor != null) {
					cursor.close();
					cursor = null;
				}
				
				cursor = getCursor(getPreference(), null);
				
				dataAdapter = getDataAdapter(cursor);
				
				listView.setAdapter(dataAdapter);
				
				// hide the tag filter label
				mTextView.setText("");
				mTextView.setVisibility(View.GONE);
				
				// set the flag to indicate that there is no tag filter in place
				hasTag = null;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		
		// is a tag filter in place
		if(hasTag != null) {
			// go back to the tag list
			Intent mIntent = new Intent(this, org.servalproject.maps.TagListActivity.class);
			startActivityForResult(mIntent, 0);
		} else {
			// leave this activity
			finish();
		}
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
			Toast.makeText(getApplicationContext(), R.string.poi_list_ui_toast_missing_poi_id, Toast.LENGTH_LONG).show();
			finish();
		}
		
	}
}
