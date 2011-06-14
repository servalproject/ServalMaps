/*
 * This file is part of the Serval Mapping Services app.
 *
 *  Serval Mapping Services app is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  Serval Mapping Services app is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Serval Mapping Services app.  
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.servalproject.mappingservices;

import java.util.HashMap;

import org.servalproject.mappingservices.content.DatabaseUtils;
import org.servalproject.mappingservices.content.IncidentProvider;
import org.servalproject.mappingservices.content.LocationProvider;
import org.servalproject.mappingservices.content.RecordTypes;
import org.servalproject.mappingservices.mapsforge.OverlayItem;
import org.servalproject.mappingservices.mapsforge.OverlayList;

//import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
//import org.mapsforge.android.maps.OverlayItem;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity that displays the map to the user
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class MapActivity extends org.mapsforge.android.maps.MapActivity implements Runnable{
	
	/**
	 * directory where map data is stored
	 */
	public static final String MAP_DATA_DIR = "/sdcard/serval/mapping-services/";
	
	/**
	 * delay between updates (in seconds)
	 */
	public static final int SLEEP_TIME = 30;
	
	/**
	 * maximum age of an incident or location (in minutes)
	 */
	public static final int MAX_AGE = 30;
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MA";
	
	
	/*
	 * private class level variables
	 */
	//private MapActivityThread mapUpdater;
	private OverlayList markerOverlay;
	private ContentResolver contentResolver;
	private HashMap<String, OverlayItem> peerLocations;
	private HashMap<String, OverlayItem> incidentLocations;
	
	private Drawable peerLocationMarker;
	private Drawable selfLocationMarker;
    private Drawable incidentLocationMarker;
    
    private volatile boolean keepGoing = true;
    
    Thread updateThread = null;
    MapActivity self;
    
    //TODO work out how to stop thread gracefully
	
	
	/*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        
        if(V_LOG) {
        	Log.v(TAG, "activity created");
        }
        
        // instantiate mapsforge classes
        
        MapView mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMapFile(MAP_DATA_DIR + "map-data.map");
        setContentView(mapView);
        
        // load map marker images
        peerLocationMarker     = getResources().getDrawable(R.drawable.peer_location);
        selfLocationMarker     = getResources().getDrawable(R.drawable.peer_location_self);
        incidentLocationMarker = getResources().getDrawable(R.drawable.incident_marker);
        
        peerLocations = new HashMap<String, OverlayItem>();
        incidentLocations = new HashMap<String, OverlayItem>();
        
        contentResolver = getContentResolver();
        
        markerOverlay = new OverlayList(peerLocationMarker, this);

        mapView.getOverlays().add(markerOverlay);
        
        self = this;
        
//        updateThread = new Thread(self);
//        updateThread.start();
        
        if(V_LOG) {
        	Log.v(TAG, "initial map population complete");
        }
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// build the menu using the XML file as a template
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	boolean status;
    	
    	// respond to the user selecting a menu item
    	switch (item.getItemId()) {
        case R.id.map_menu_new_incident:
            // start entering a new incident
        	Intent mIntent = new Intent(MapActivity.this, AddIncidentActivity.class);
			startActivity(mIntent);
            status = true;
        case R.id.menu_shutdown:
        	// shutdown the services
        	status = true;
        case R.id.menu_about:
        	// show the about activity
        	status = true;
        default:
        	status = super.onOptionsItemSelected(item);
        }
    	
    	return status;
    }

    /*
     * run the activity as a thread updating the overlay with new information
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	@Override
	public void run() {
		
		while(keepGoing) {
			
			// declare helper variables
			//long mMaximumAge;
			Uri mContentUri;
			Cursor mCursor;
			
			String[] mColumns;
			String   mSelection;
			String[] mSelectionArgs;
			String   mOrderBy;
			
			OverlayItem mOverlayItem;
			GeoPoint mGeoPoint;
				
			// calculate the maximum age
			long mMaximumAge = DatabaseUtils.getCurrentTimeAsUtc() - (MAX_AGE * 60);

			// start with the peer location data
			mContentUri = LocationProvider.CONTENT_URI;

			mColumns = new String[5];
			mColumns[0] = LocationProvider._ID;
			mColumns[1] = LocationProvider.PHONE_NUMBER_FIELD;
			mColumns[2] = LocationProvider.LATITUDE_FIELD;
			mColumns[3] = LocationProvider.LONGITUDE_FIELD;
			mColumns[4] = LocationProvider.SELF_FIELD;

			mSelection = LocationProvider.TIMESTAMP_UTC_FIELD + " > ? ";

			mSelectionArgs = new String[1];
			mSelectionArgs[0] = Long.toString(mMaximumAge);

			mOrderBy = LocationProvider.TIMESTAMP_UTC_FIELD + " DESC";

			mCursor = contentResolver.query(mContentUri, mColumns, mSelection, mSelectionArgs, mOrderBy);

			// check to see if data was returned and if so process it
			while(mCursor.moveToNext() == true) {

				// check to see if we've seen a location for this phone number before
				if(peerLocations.containsKey(mCursor.getString(mCursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD))) == false) {
					// not in key list so create a new overlay item

					mGeoPoint = new GeoPoint(Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LATITUDE_FIELD))), Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LONGITUDE_FIELD))));
					
					// determine which icon to use
					if(mCursor.getString(mCursor.getColumnIndex(LocationProvider.SELF_FIELD)) == null) {
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, ItemizedOverlay.boundCenterBottom(peerLocationMarker));
						mOverlayItem.setRecordId(mCursor.getString(mCursor.getColumnIndex(LocationProvider._ID)));
						mOverlayItem.setRecordType(RecordTypes.LOCATION_RECORD_TYPE);
					} else {
						mOverlayItem = new OverlayItem(mGeoPoint, null, null, ItemizedOverlay.boundCenterBottom(selfLocationMarker));
						//mOverlayItem.setRecordId(mCursor.getString(mCursor.getColumnIndex(LocationProvider._ID)));
						mOverlayItem.setRecordType(RecordTypes.SELF_LOCATION_RECORD_TYPE);
					}

					peerLocations.put(mCursor.getString(mCursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD)), mOverlayItem);
				}
			}

			if(V_LOG) {
				Log.v(TAG, "found '" + peerLocations.size() + "' location markers");
			}
			
			// play nice and tidy up
			mCursor.close();

			// get the incidents
			mContentUri = IncidentProvider.CONTENT_URI;

			mColumns = new String[4];
			mColumns[0] = IncidentProvider._ID;
			mColumns[1] = IncidentProvider.PHONE_NUMBER_FIELD;
			mColumns[2] = IncidentProvider.LATITUDE_FIELD;
			mColumns[3] = IncidentProvider.LONGITUDE_FIELD;

			mSelection = IncidentProvider.TIMESTAMP_UTC_FIELD + " > ? ";

			mSelectionArgs = new String[1];
			mSelectionArgs[0] = Long.toString(mMaximumAge);

			mOrderBy = IncidentProvider.TIMESTAMP_UTC_FIELD + " DESC";

			mCursor = contentResolver.query(mContentUri, mColumns, mSelection, mSelectionArgs, mOrderBy);
			
			String mIncidentIndex;

			// check to see if data was returned and if so process it
			while(mCursor.moveToNext() == true) {
				
				//TODO fix this hack into something more elegant
				//explore how the mapsforge library deals with overlay items at the exact same location
				mIncidentIndex = mCursor.getString(mCursor.getColumnIndex(IncidentProvider.LATITUDE_FIELD));
				mIncidentIndex += mCursor.getString(mCursor.getColumnIndex(IncidentProvider.LONGITUDE_FIELD));
				mIncidentIndex.replace(".","").replace(",", "").replace("-","");
				mIncidentIndex.replace(".","").replace(",", "").replace("-","");

				// check to see if we've seen a location for this phone number before
				if(incidentLocations.containsKey(mIncidentIndex) == false) {
					// not in key list so create a new overlay item

					mGeoPoint = new GeoPoint(Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LATITUDE_FIELD))), Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LONGITUDE_FIELD))));
					
					mOverlayItem = new OverlayItem(mGeoPoint, null, null, ItemizedOverlay.boundCenterBottom(incidentLocationMarker));
					mOverlayItem.setRecordId(mCursor.getString(mCursor.getColumnIndex(LocationProvider._ID)));
					mOverlayItem.setRecordType(RecordTypes.INCIDENT_RECORD_TYPE);
					

					incidentLocations.put(mIncidentIndex, mOverlayItem);
					
				}
			}

			if(V_LOG) {
				Log.v(TAG, "found '" + incidentLocations.size() + "' incident markers");
			}
			
			// play nice and tidy up
			mCursor.close();

			// update the map
			markerOverlay.clear();
			markerOverlay.addItems(peerLocations.values());
			markerOverlay.addItems(incidentLocations.values());
			markerOverlay.requestRedraw();
			
			// play nice and tidy up
			peerLocations.clear();
			incidentLocations.clear();

			try {
				Thread.sleep(SLEEP_TIME * 1000);
			} catch (InterruptedException e) {
//				if(V_LOG) {
//					Log.v(TAG, "interrupted while sleeping", e);
//				}
			}
		}
		
	}
	
	/**
	 * request that the thread stops
	 */
	public void requestStop() {
		keepGoing = false;
	}
	
	/**
	 * request the the thread starts again
	 */
	public void requestStart() {
		keepGoing = true;
	}
	
	/*
	 * activity life cycle methods overridden here to better manage the update thread 
	 */
	@Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        if(updateThread == null) {
    		self.requestStart();
    		updateThread = new Thread(self);
    	    updateThread.start();
        }
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        if(updateThread == null) {
    		self.requestStart();
    		updateThread = new Thread(self);
    	    updateThread.start();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        if(updateThread != null) {
        	if(updateThread.isAlive() == true) {
        		self.requestStop();
        		updateThread.interrupt();
        		updateThread = null;
        	}
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        if(updateThread != null) {
        	if(updateThread.isAlive() == true) {
        		self.requestStop();
        		updateThread.interrupt();
        		updateThread = null;
        	}
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        if(updateThread != null) {
        	if(updateThread.isAlive() == true) {
        		self.requestStop();
        		updateThread.interrupt();
        		updateThread = null;
        	}
        }
    }
}
