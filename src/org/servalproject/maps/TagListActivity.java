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

import org.servalproject.maps.provider.TagsContract;
import org.servalproject.maps.tags.TagsListAdapter;

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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 *  a class to provide a list of Tags that the user can select to
 *  restrict the list of POI information display
 */
public class TagListActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  sTag = "TagListActivity";
	
	// allow user to sort list by tag or count
	private final String sPreferenceName = "preferences_tag_list_sort";
	private final String sDefaultSortField = TagsContract.Table.TAG;
	private final String sCountSortField = "COUNT(" + TagsContract.Table.TAG + ")";
	
	/*
	 * private class level variables
	 */
	private Cursor cursor;
	private String[] columnNames;
	private int[] layoutElements;
	private ListView listView;
	
	TagsListAdapter dataAdapter;
	
	/*
	 * create the activity
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags_list);
        
        // get the data
        cursor = getCursor();
        
        if(cursor == null) {
			Log.e(sTag, "a null cursor was returned when looking up tag info");
			Toast.makeText(getApplicationContext(), R.string.tags_list_ui_toast_missing_data, Toast.LENGTH_LONG).show();
			finish();
		}
        
        // get a data adapter
        dataAdapter = getDataAdapter(cursor);
        
        // use the data adapter with this activity
        setListAdapter(dataAdapter);
        
        // listen for touching on list items
		// get a reference to the list view
		listView = getListView();

		listView.setOnItemClickListener(this);
	}
	
	/*
	 * get a cursor containing the tag data and default sort order
	 */
	private Cursor getCursor() {
		return getCursor(getPreference());
	}

	/* 
	 * get a cursor containing the tag data using the specified sort order
	 */
	private Cursor getCursor(String sortField) {
		
		ContentResolver mContentResolver = getContentResolver();
		
		// determine the order by
		String mOrderBy = null;
		
		if(sortField.equals(sDefaultSortField) == true) {
			mOrderBy = sortField + " ASC";
		} else {
			mOrderBy = sortField + " DESC";
		}
		
		if(V_LOG) {
			Log.v(sTag, "order by statement: " + mOrderBy);
		}
		
		return mContentResolver.query(
				TagsContract.UNIQUE_CONTENT_URI,
				null,
				null,
				null,
				mOrderBy);
		
	}
	
	/*
	 * get a populated data datapter
	 */
	private TagsListAdapter getDataAdapter(Cursor cursor) { 
		
		// define the column and layout mapping
        columnNames = new String[2];
        columnNames[0] = TagsContract.Table.TAG;
        columnNames[1] = TagsContract.Table._COUNT;
        
        layoutElements = new int[2];
        layoutElements[0] = R.id.tags_list_ui_entry_tag;
        layoutElements[1] = R.id.tags_list_ui_entry_used;
        
        // get a data adapter
        return new TagsListAdapter(
        		this,
        		R.layout.tags_list_entry,
        		cursor,
        		columnNames,
        		layoutElements);
		
	}
	
	/*
	 * private method to update a preference on the sort order
	 */
	private void updatePreference(String prefValue) {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		SharedPreferences.Editor mEditor = mPreferences.edit();
		
		mEditor.putString(sPreferenceName, prefValue);

		mEditor.apply();
	}
	
	/*
	 * private method to get the preference on the sort order
	 */
	private String getPreference() {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		return mPreferences.getString(sPreferenceName, sDefaultSortField);
		
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
	    inflater.inflate(R.menu.tag_list_activity, menu);
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
		case R.id.menu_tag_list_activity_sort_alpha:
			// sort list of items by title alphabetically
			cursor.close();
			cursor = null;
			cursor = getCursor(sDefaultSortField);
			
			updatePreference(sDefaultSortField);
			
			dataAdapter = getDataAdapter(cursor);
			
			listView.setAdapter(dataAdapter);

			return true;
		case R.id.menu_tag_list_activity_sort_count:
			// sort list of items by time
			cursor.close();
			cursor = null;
			cursor = getCursor(sCountSortField);
			
			updatePreference(sCountSortField);
			
			dataAdapter = getDataAdapter(cursor);
			
			listView.setAdapter(dataAdapter);
			
			return true;
		case R.id.menu_tag_list_activity_close:
			// close this activity
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		if(V_LOG) {
			Log.v(sTag, "item clicked at position: " + position);
		}
		
		// work out the id of the item
		if(cursor.moveToPosition(position) == true) {
			
			if(V_LOG) {
				Log.v(sTag, "selected tag: " + cursor.getString(cursor.getColumnIndex(TagsContract.Table.TAG)));
			}
			
			// return back to the POI List with the chosen tag
			Intent mIntent = new Intent();
			mIntent.putExtra("tag", cursor.getString(cursor.getColumnIndex(TagsContract.Table.TAG)));
			
			setResult(PoiListActivity.TAG_SELECTED_RESULT, mIntent);
			finish();
			
		} else {
			Log.e(sTag, "unable to match list item position to tag");
			Toast.makeText(getApplicationContext(), R.string.tag_list_ui_toast_missing_poi_id, Toast.LENGTH_LONG).show();
			finish();
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		setResult(PoiListActivity.TAG_SELECTED_RESULT, null);
		finish();
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
}
