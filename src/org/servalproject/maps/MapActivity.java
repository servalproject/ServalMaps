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

import org.mapsforge.android.maps.MapView;
import org.servalproject.maps.provider.MapItemsContract;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * An activity to show a map
 */
public class MapActivity extends org.mapsforge.android.maps.MapActivity {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "MapActivity";
	
	private Intent coreServiceIntent;
	
	private Handler updateHandler = new Handler();
	
	// number of seconds to delay between map updates
	private int updateDelay = 10 * 1000;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //setContentView(R.layout.map);
        
        // start the core service
		coreServiceIntent = new Intent(this, org.servalproject.maps.services.CoreService.class);
        startService(coreServiceIntent);
        
        // get the map data file name
        Bundle mBundle = this.getIntent().getExtras();
        String mMapFileName = mBundle.getString("mapFileName");
        
		// instantiate mapsforge classes
		MapView mMapView = new MapView(this);
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		
		if(mMapFileName != null) {
			mMapView.setMapFile(mMapFileName);
		}
		
		setContentView(mMapView);
		
		// update the map without delay
		updateHandler.post(updateMapTask);
		
		if(V_LOG) {
			Log.v(TAG, "activity created");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mapsforge.android.maps.MapActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// stop the core service
		stopService(coreServiceIntent);
		
		// top the handle / runnable looping action
		updateHandler.removeCallbacks(updateMapTask);
		
		super.onDestroy();
		
		if(V_LOG) {
			Log.v(TAG, "activity destroyed");
		}
		
	}
	
	/*
	 *  methods and variables used to update the class
	 */
	
	// task used to update the map ui with new markers
	private Runnable updateMapTask = new Runnable() {
		
		public void run() {
			if(V_LOG){
				Log.v(TAG, "update map task running");
			}
			
			// resolve the content uri
			ContentResolver mContentResolver = getApplicationContext().getContentResolver();
			
			// get the content
			Cursor mCursor = mContentResolver.query(MapItemsContract.Locations.LATEST_CONTENT_URI, null, null, null, null);
			
			if(V_LOG) {
				Log.v(TAG, "rows in cursor: " + mCursor.getCount());
			}
			
			mCursor.close();
			
			// add the task back onto the queue
			updateHandler.postDelayed(updateMapTask, updateDelay);
		}
	};
	
}
