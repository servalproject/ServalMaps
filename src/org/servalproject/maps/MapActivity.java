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

import java.util.ArrayList;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
import org.servalproject.maps.mapsforge.OverlayItem;
import org.servalproject.maps.mapsforge.OverlayItems;
import org.servalproject.maps.mapsforge.OverlayList;
import org.servalproject.maps.provider.MapItemsContract;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
	
	// drawables for marker icons
	Drawable peerLocationMarker;
	Drawable selfLocationMarker;
	Drawable poiLocationMarker;
	
	// list of markers
	OverlayList overlayList;
	
	
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
		
		// get the drawables for the markers
		peerLocationMarker  = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.peer_location));
        selfLocationMarker  = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.peer_location_self));
        poiLocationMarker   = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.incident_marker));
        
        overlayList = new OverlayList(poiLocationMarker, this);
        mMapView.getOverlays().add(overlayList);
		
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
			
			//TODO improve the way information is retrieved and only get what is required
			// get the content
			Cursor mCursor = mContentResolver.query(MapItemsContract.Locations.LATEST_CONTENT_URI, null, null, null, null);
			
			if(mCursor == null) {
				Log.i(TAG, "a null cursor was returned");
				return;
			}
			
			if(V_LOG) {
				Log.v(TAG, "rows in cursor: " + mCursor.getCount());
			}
			
			if(mCursor.getCount() > 0) {
				// process the location records
				ArrayList<OverlayItem> mLocations = new ArrayList<OverlayItem>();
				GeoPoint mGeoPoint;
				String mPhoneNumber;
				OverlayItem mOverlayItem;
				
				while(mCursor.moveToNext()) {
					
					
					
					// get the basic information
					mPhoneNumber = mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.PHONE_NUMBER));
					
					// get the geographic coordinates
					mGeoPoint = new GeoPoint(mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LATITUDE)), mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LONGITUDE)));
					
					// determine what type of marker to create
					// TODO use actual value for phone number
					if(mPhoneNumber.equals("myphonenumber2") == true) {
						// this is a self marker
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, selfLocationMarker);
						mOverlayItem.setType(OverlayItems.SELF_LOCATION_ITEM);
						mOverlayItem.setRecordId(mCursor.getInt(mCursor.getColumnIndex(MapItemsContract.Locations.Table._ID)));
					} else {
						// this is a peer marker
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, peerLocationMarker);
						mOverlayItem.setType(OverlayItems.PEER_LOCATION_ITEM);
						mOverlayItem.setRecordId(mCursor.getInt(mCursor.getColumnIndex(MapItemsContract.Locations.Table._ID)));
					}
					
					mLocations.add(mOverlayItem);
				}
				
				// update the overlay
				overlayList.clear();
				overlayList.addItems(mLocations);
				overlayList.requestRedraw();
			}
			
			mCursor.close();
			
			// add the task back onto the queue
			updateHandler.postDelayed(updateMapTask, updateDelay);
		}
	};	
}
