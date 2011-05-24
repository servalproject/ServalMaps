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

import org.servalproject.mappingservices.content.RecordTypes;
import org.servalproject.mappingservices.mapsforge.OverlayItem;
import org.servalproject.mappingservices.mapsforge.OverlayList;

//import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapView;
//import org.mapsforge.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity that displays the map to the user
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class MapActivity extends org.mapsforge.android.maps.MapActivity {
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MA";
	
	
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
      
        // /sdcard/serval/mapping-services/map-data.map
        
        MapView mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMapFile("/sdcard/serval/mapping-services/map-data.map");
        setContentView(mapView);
        
        // create some markers
        Drawable mPeerLocationMarker     = getResources().getDrawable(R.drawable.android_logo_marker);
        Drawable mIncidentLocationMarker = getResources().getDrawable(R.drawable.cupcake_logo_marker);
        
        GeoPoint mLocationPoint = new GeoPoint(-35.026513, 138.571664);
        GeoPoint mIncidentPoint = new GeoPoint(-35.026454, 138.572377);
        
        OverlayList markerOverlay = new OverlayList(mPeerLocationMarker, this);
        
        OverlayItem item = new OverlayItem(mLocationPoint, "Test location", "This is a test location");
        item.setRecordType(RecordTypes.LOCATION_RECORD_TYPE);
        item.setRecordId("1");
        markerOverlay.addItem(item);
        
        item = new OverlayItem(mIncidentPoint, "Test incident", "This is a test incident", ItemizedOverlay.boundCenterBottom(mIncidentLocationMarker));
        item.setRecordType(RecordTypes.INCIDENT_RECORD_TYPE);
        item.setRecordId("2");
        markerOverlay.addItem(item);
        
        mapView.getOverlays().add(markerOverlay);
        
        if(V_LOG) {
        	Log.v(TAG, "initial map populattion complete");
        }
    }
}
