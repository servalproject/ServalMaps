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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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
	private  OverlayList markerOverlay;
	private ContentResolver contentResolver;
	private HashMap<String, OverlayItem> peerLocations;
	
	private Drawable peerLocationMarker;
    private Drawable incidentLocationMarker;
    
    //TODO add incident markers
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
        
        peerLocationMarker     = getResources().getDrawable(R.drawable.android_logo_marker);
        incidentLocationMarker = getResources().getDrawable(R.drawable.cupcake_logo_marker);
        
        peerLocations = new HashMap<String, OverlayItem>();
        
        contentResolver = getContentResolver();
        
        markerOverlay = new OverlayList(peerLocationMarker, this);

        mapView.getOverlays().add(markerOverlay);
        
        Thread thread = new Thread(this);
        thread.start();
        
        if(V_LOG) {
        	Log.v(TAG, "initial map population complete");
        }
    }


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true) {
			
			// declare helper variables
			long mMaximumAge;
			Uri mContentUri;
			Cursor mCursor;
			
			String[] mColumns;
			String   mSelection;
			String[] mSelectionArgs;
			String   mOrderBy;
			
			OverlayItem mOverlayItem;
			GeoPoint mGeoPoint;
				
			// calculate the maximum age
			mMaximumAge = DatabaseUtils.getCurrentTimeAsUtc() - (MAX_AGE * 60);

			// start with the peer location data
			mContentUri = LocationProvider.CONTENT_URI;

			mColumns = new String[4];
			mColumns[0] = LocationProvider._ID;
			mColumns[1] = LocationProvider.PHONE_NUMBER_FIELD;
			mColumns[2] = LocationProvider.LATITUDE_FIELD;
			mColumns[3] = LocationProvider.LONGITUDE_FIELD;

			mSelection = LocationProvider.TIMESTAMP_UTC_FIELD + " > ? ";

			mSelectionArgs = new String[1];
			mSelectionArgs[0] = Long.toString(mMaximumAge);

			mOrderBy = LocationProvider.TIMESTAMP_UTC_FIELD + " DESC";

			mCursor = contentResolver.query(mContentUri, mColumns, mSelection, mSelectionArgs, mOrderBy);
			
			// reset the list of markers
			peerLocations.clear();

			// check to see if data was returned and if so process it
			if(mCursor.moveToFirst() == true) {

				// check to see if we've seen a location for this phone number before
				if(peerLocations.containsKey(mCursor.getString(mCursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD))) == false) {
					// not in key list so create a new overlay item

					mGeoPoint = new GeoPoint(Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LATITUDE_FIELD))), Double.parseDouble(mCursor.getString(mCursor.getColumnIndex(LocationProvider.LONGITUDE_FIELD))));
					mOverlayItem = new OverlayItem(mGeoPoint, "Peer Location", "Location for: " + mCursor.getString(mCursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD)), ItemizedOverlay.boundCenterBottom(peerLocationMarker));
					mOverlayItem.setRecordId(mCursor.getString(mCursor.getColumnIndex(LocationProvider._ID)));
					mOverlayItem.setRecordType(RecordTypes.LOCATION_RECORD_TYPE);

					peerLocations.put(mCursor.getString(mCursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD)), mOverlayItem);
					
					if(V_LOG) {
						Log.v(TAG, mCursor.getString(mCursor.getColumnIndex(LocationProvider.LATITUDE_FIELD)) + " - " + mCursor.getString(mCursor.getColumnIndex(LocationProvider.LONGITUDE_FIELD)));
					}
					
				}
			}

			if(V_LOG) {
				Log.v(TAG, "found '" + peerLocations.size() + "' location markers");
			}
			
			// play nice and tidy up
			mCursor.close();

			//TODO add code to get incidents

			// update the map
			markerOverlay.clear();
			markerOverlay.addItems(peerLocations.values());
			markerOverlay.requestRedraw();

			try {
				Thread.sleep(SLEEP_TIME * 1000);
			} catch (InterruptedException e) {
				if(V_LOG) {
					Log.v(TAG, "interrupted while sleeping", e);
				}
			}
		}
		
	}
}
