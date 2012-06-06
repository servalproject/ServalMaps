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

import java.io.File;
import java.util.ArrayList;

import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
import org.mapsforge.core.GeoPoint;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.OverlayWay;
import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.mapsforge.NewPoiOverlay;
import org.servalproject.maps.mapsforge.OverlayItem;
import org.servalproject.maps.mapsforge.OverlayItems;
import org.servalproject.maps.mapsforge.OverlayList;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.FileUtils;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity to show a map
 */
public class MapActivity extends org.mapsforge.android.maps.MapActivity {
	
	/*
	 * public class level constants
	 */
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = false;
	private final String  TAG = "MapActivity";
	
	/*
	 * private class level variables
	 */
	
	private Intent coreServiceIntent;
	
	private Handler updateHandler = new Handler();
	
	// number of seconds to delay between map updates
	private int defaultUpdateDelay = 10 * 1000;
	private volatile int updateDelay = defaultUpdateDelay;
	private volatile boolean keepCentered = false;
	
	private long defaultPoiMaxAge = 43200 * 1000;
	private volatile long poiMaxAge = defaultPoiMaxAge;
	
	private long defaultLocationMaxAge = 43200 * 1000;
	private volatile long locationMaxAge = defaultLocationMaxAge;
	
	private SharedPreferences preferences = null;
	
	// drawables for marker icons
	private Drawable peerLocationMarker;
	private Drawable selfLocationMarker;
	private Drawable poiLocationMarker;
	
	// list of markers
	private OverlayList overlayList;
	private MapView mapView;
	private ArrayWayOverlay arrayWayOverlay = null;
	
	// phone number and sid
	private String meshPhoneNumber = null;
	
	// check to know if a map update is running
	private volatile boolean updateRunning = false;
	
	
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
        String mMapFileName = null;
        if(mBundle != null) {
        	mMapFileName = mBundle.getString("mapFileName");
        } else {
        	// finish if there is no file name
        	finish();
        }
        
		// instantiate mapsforge classes
		mapView = new MapView(this);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		
		if(mMapFileName != null) {
			
			if(FileUtils.isFileReadable(mMapFileName) == false) {
				String mMapDataPath = Environment.getExternalStorageDirectory().getPath();
				mMapDataPath += getString(R.string.system_path_map_data);
				mMapFileName = mMapDataPath + mMapFileName;
				if (FileUtils.isFileReadable(mMapFileName))
					mapView.setMapFile(new File(mMapFileName));
			} else {
				mapView.setMapFile(new File(mMapFileName));
			}
			
		}
		
		setContentView(mapView);
		
		// get the drawables for the markers
		peerLocationMarker  = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.peer_location));
        selfLocationMarker  = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.peer_location_self));
        poiLocationMarker   = ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.incident_marker));
        
        overlayList = new OverlayList(poiLocationMarker, this);
        mapView.getOverlays().add(overlayList);
        
        // add the long press detecting overlay for adding new POIs
        mapView.getOverlays().add(new NewPoiOverlay(this));
        
        // get the preferences
     	preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     	
     	// set the update delay
 		String mPreference = preferences.getString("preferences_map_update_interval", null);
 		
 		if(mPreference == null) {
 			updateDelay = defaultUpdateDelay;
 		} else {
 			updateDelay = Integer.parseInt(mPreference);
 		}
 		
 		// get the max poi and location age preferences
 		mPreference = preferences.getString("preferences_map_max_poi_age", null);
		if(mPreference != null) {
			poiMaxAge = Long.parseLong(mPreference) * 1000;
		}
		
		mPreference = preferences.getString("preferences_map_max_location_age", null);
		if(mPreference != null) {
			locationMaxAge = Long.parseLong(mPreference) * 1000;
		}
 		
 		// set flag to keep the map centered
 		keepCentered = preferences.getBoolean("preferences_map_follow", false);
 		
 		// determine if we need to show the users GPS trace
 		if(preferences.getBoolean("preferences_map_show_track", false)) {
 			
 			// will be drawing a line not a polygon at this stage
 			Paint mDefaultFill = null;
 			Paint mDefaultLine = new Paint(Paint.ANTI_ALIAS_FLAG);
 			mDefaultLine.setARGB(255, 85, 140, 248);
 			mDefaultLine.setStyle(Paint.Style.STROKE);
 			mDefaultLine.setStrokeWidth(2);
 			
 			arrayWayOverlay = new ArrayWayOverlay(mDefaultFill, mDefaultLine);
 			mapView.getOverlays().add(arrayWayOverlay);
 		}
     	
     	// listen for changes in the preferences
     	preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
     	
     	// get the phone number and sid
     	ServalMaps mApplication = (ServalMaps) getApplication();
     	meshPhoneNumber = mApplication.getPhoneNumber();
     	mApplication = null;
     	
		// update the map without delay
		updateHandler.post(updateMapTask);
		
		if(V_LOG) {
			Log.v(TAG, "activity created");
		}
	}
	
	/*
	 * object to listen for changes in the preferences
	 */
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

		/* 
		 * (non-Javadoc)
		 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
			
			if(V_LOG) {
				Log.v(TAG, "a change in shared preferences has been deteceted");
				Log.v(TAG, "preference change: '" + key + "'");
			}
			
			if(key.equals("preferences_map_update_interval") == true) {
				
				String mPreference = preferences.getString("preferences_map_update_interval", null);
				if(mPreference != null) {
					updateDelay = Integer.parseInt(mPreference);
				}
				if(V_LOG) {
					Log.v(TAG, "new map update delay is '" + updateDelay + "'");
				}
				
			} else if(key.equals("preferences_map_follow") == true) {
				keepCentered = preferences.getBoolean("preferences_category_map", false);
				
				if(V_LOG) {
					if(keepCentered) {
						Log.v(TAG, "map will keep centered on the users location");
					} else {
						Log.v(TAG, "map will not keep centered on the users location");
					}
				}
			} else if(key.equals("preferences_map_max_poi_age") == true) {
				
				String mPreference = preferences.getString("preferences_map_max_poi_age", null);
				if(mPreference != null) {
					poiMaxAge = Long.parseLong(mPreference) * 1000;
				}
				
				if(V_LOG) {
					Log.v(TAG, "new max POI age is '" + poiMaxAge + "'");
				}
			} else if(key.equals("preferences_map_max_location_age") == true) {
				
				String mPreference = preferences.getString("preferences_map_max_location_age", null);
				if(mPreference != null) {
					locationMaxAge = Long.parseLong(mPreference) * 1000;
				}
				
				if(V_LOG) {
					Log.v(TAG, "new max location age is '" + locationMaxAge + "'");
				}
			} else if(key.equals("preferences_map_show_track") == true) {
				
				// add the way overlay
				// TODO add appropriate constants / private methods
				if(preferences.getBoolean("preferences_map_show_track", false)) {
		 			
		 			// will be drawing a line not a polygon at this stage
		 			Paint mDefaultFill = null;
		 			Paint mDefaultLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		 			mDefaultLine.setARGB(255, 85, 140, 248);
		 			mDefaultLine.setStyle(Paint.Style.STROKE);
		 			mDefaultLine.setStrokeWidth(2);
		 			
		 			arrayWayOverlay = new ArrayWayOverlay(mDefaultFill, mDefaultLine);
		 			mapView.getOverlays().add(arrayWayOverlay);
		 		} else {
		 			mapView.getOverlays().remove(arrayWayOverlay);
		 			arrayWayOverlay.clear();
		 			arrayWayOverlay = null;
		 		}
				
			}
			
			
		}
	};
	
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
	    inflater.inflate(R.menu.map_activity, menu);
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
		
		Intent mIntent;
		
		switch(item.getItemId()){
		case R.id.menu_map_activity_preferences:
			// show the preferences activity
			mIntent = new Intent(this, org.servalproject.maps.SettingsActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_map_activity_add_poi:
			// show the add POI activity
			mIntent = new Intent(this, org.servalproject.maps.NewPoiActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_map_activity_centre_map:
			// recentre the map on the current location
			Location mLocation = LocationCollector.getLocation();
			if(mLocation != null) {
				GeoPoint mGeoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
				mapView.getController().setCenter(mGeoPoint);
			} else {
				Toast.makeText(getApplicationContext(), R.string.map_ui_toast_location_unavailable, Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.menu_map_activity_poi_list:
			// show the list of poi
			mIntent = new Intent(this, org.servalproject.maps.PoiListActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_map_activity_help_about:
			// show the help text
			mIntent = new Intent(this, org.servalproject.maps.AboutActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_map_activity_close:
			// close this activity
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
		
		// stop the handle / runnable looping action
		updateHandler.removeCallbacks(updateMapTask);
		
		super.onDestroy();
		
		if(V_LOG) {
			Log.v(TAG, "activity destroyed");
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mapsforge.android.maps.MapActivity#onPause()
	 */
	@Override
	public void onPause() {
		
		// stop the updating of the map
		updateHandler.removeCallbacks(updateMapTask);
		
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mapsforge.android.maps.MapActivity#onResume()
	 */
	public void onResume() {
		
		// restart the updating of the map
		updateHandler.post(updateMapTask);
		
		super.onResume();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
	/*
	 *  methods and variables used to update the map
	 */
	
	// task used to update the map ui with new markers
	private Runnable updateMapTask = new Runnable() {
		
		public void run() {
			if(V_LOG){
				Log.v(TAG, "update map task running");
			}
			
			if(updateRunning) {
				// an update is already running so just return
				return;
			}
			
			// indicate the map update is underway
			updateRunning = true;
			
			// resolve the content uri
			ContentResolver mContentResolver = getApplicationContext().getContentResolver();
			
			// get the location marker content
			Cursor mCursor = mContentResolver.query(LocationsContract.LATEST_CONTENT_URI, null, null, null, null);
			
			// store the list of items
			ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
			
			if(mCursor == null) {
				Log.i(TAG, "a null cursor was returned when looking up location info");
				return;
			}
			
			if(V_LOG) {
				Log.v(TAG, "rows in location info cursor: " + mCursor.getCount());
			}
			
			if(mCursor.getCount() > 0) {
				// process the location records
				GeoPoint mGeoPoint;
				String mPhoneNumber;
				OverlayItem mOverlayItem;
				long mLocationAge;
				long mCompareTime = System.currentTimeMillis() - locationMaxAge;
				
				while(mCursor.moveToNext()) {
					
					// check on the age of the info if required
					if(locationMaxAge != -1000) {
						mLocationAge = mCursor.getLong(mCursor.getColumnIndex(LocationsContract.Table.TIMESTAMP));
						
						if(mLocationAge < mCompareTime) {
							// skip this record
							continue;
						}
					}

					// get the basic information
					mPhoneNumber = mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER));
					
					// get the geographic coordinates
					mGeoPoint = new GeoPoint(mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)), mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)));
					
					// determine what type of marker to create
					if(mPhoneNumber.equals(meshPhoneNumber) == true) {
						// this is a self marker
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, selfLocationMarker);
						mOverlayItem.setType(OverlayItems.SELF_LOCATION_ITEM);
						mOverlayItem.setRecordId(mCursor.getInt(mCursor.getColumnIndex(LocationsContract.Table._ID)));
						
						// recenter the map if required
						if(keepCentered) {
							mapView.getController().setCenter(mGeoPoint);
							if(V_LOG) {
								Log.v(TAG, "map was recentered");
							}
						}
					} else {
						// this is a peer marker
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, peerLocationMarker);
						mOverlayItem.setType(OverlayItems.PEER_LOCATION_ITEM);
						mOverlayItem.setRecordId(mCursor.getInt(mCursor.getColumnIndex(LocationsContract.Table._ID)));

					}
					
					mItems.add(mOverlayItem);
					
				}
				
			}
			
			// play nice and tidy up
			mCursor.close();
			
			// get the POI content
			String[] mProjection = new String[3];
			mProjection[0] = PointsOfInterestContract.Table._ID;
			mProjection[1] = PointsOfInterestContract.Table.LATITUDE;
			mProjection[2] = PointsOfInterestContract.Table.LONGITUDE;
			
			// determine if we need to restrict the list of POIs
			String mSelection = null;
			String[] mSelectionArgs = null;
			
			// restrict the poi content returned if required
			if(poiMaxAge != -1000) {
				mSelection = PointsOfInterestContract.Table.TIMESTAMP + " > ? ";
				mSelectionArgs = new String[1];
				mSelectionArgs[0] = Long.toString(System.currentTimeMillis() - poiMaxAge);
			}
			
			mCursor = mContentResolver.query(
					PointsOfInterestContract.CONTENT_URI, 
					mProjection, 
					mSelection, 
					mSelectionArgs,
					null);
			
			if(mCursor == null) {
				Log.i(TAG, "a null cursor was returned when looking up POI info");
				return;
			}
			
			if(V_LOG) {
				Log.v(TAG, "rows in POI cursor: " + mCursor.getCount());
			}
			
			// process the list of poi records
			if(mCursor.getCount() > 0) {
				// process the location records
				GeoPoint mGeoPoint;
				OverlayItem mOverlayItem;
				
				while(mCursor.moveToNext()) {
					
					// get the geographic coordinates
					mGeoPoint = new GeoPoint(mCursor.getDouble(mCursor.getColumnIndex(PointsOfInterestContract.Table.LATITUDE)), mCursor.getDouble(mCursor.getColumnIndex(PointsOfInterestContract.Table.LONGITUDE)));
					
					mOverlayItem = new OverlayItem(mGeoPoint, null, null, poiLocationMarker);
					mOverlayItem.setType(OverlayItems.POI_ITEM);
					mOverlayItem.setRecordId(mCursor.getInt(mCursor.getColumnIndex(PointsOfInterestContract.Table._ID)));
					
					mItems.add(mOverlayItem);
				}
			}
			
			// play nice and tidy up
			mCursor.close();
			
			// build the gps track overlay if required
			if(arrayWayOverlay != null) {
				
				// determine which fields to return
				mProjection = new String[2];
				mProjection[0] = LocationsContract.Table.LATITUDE;
				mProjection[1] = LocationsContract.Table.LONGITUDE;
				
				// check if we need to take into account the age of the information
				if(locationMaxAge != -1000) {
				
					mSelection = LocationsContract.Table.PHONE_NUMBER + " = ? AND "
							+ LocationsContract.Table.TIMESTAMP + " > ?";
					
					mSelectionArgs = new String[2];
					mSelectionArgs[0] = meshPhoneNumber;
					mSelectionArgs[1] = Long.toString(System.currentTimeMillis() - locationMaxAge);
				} else {
					
					mSelection = LocationsContract.Table.PHONE_NUMBER + " = ?";
					
					mSelectionArgs = new String[1];
					mSelectionArgs[0] = meshPhoneNumber;
				}
				
				// get the data
				mCursor = mContentResolver.query(
						LocationsContract.CONTENT_URI, 
						mProjection, 
						mSelection,
						mSelectionArgs,
						LocationsContract.Table.TIMESTAMP);
				
				if(mCursor.getCount() > 0) {
					if(V_LOG) {
						Log.v(TAG, "gps track contains: '" + mCursor.getCount() + "' points");
					}
					
					// declare array to hold our list of points
					GeoPoint[][] mWayPoints = new GeoPoint[1][mCursor.getCount()];
					int mCount = 0;
					GeoPoint mGeoPoint;
					
					// populate the array
					while(mCursor.moveToNext()) {
						mGeoPoint = new GeoPoint(mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)), mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)));
						mWayPoints[0][mCount] = mGeoPoint;
						mCount++;
					}
					
					// play nice and tidy up
					mCursor.close();
					
					OverlayWay mOverlayWay = new OverlayWay(mWayPoints, null, null);
					
					// update the overlay
					arrayWayOverlay.clear();
					arrayWayOverlay.addWay(mOverlayWay);
				}
				
			}
			
			// update and redraw the overlay
			overlayList.clear();
			overlayList.addItems(mItems);
			overlayList.requestRedraw();
			
			if(arrayWayOverlay != null) {
				arrayWayOverlay.requestRedraw();
			}
			
			// indicate that a map update is finished
			updateRunning = false;
			
			// add the task back onto the queue
			updateHandler.postDelayed(updateMapTask, updateDelay);
		}
	};	
}
