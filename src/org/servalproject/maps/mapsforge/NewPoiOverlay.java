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
package org.servalproject.maps.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.Projection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Vibrator;
import android.util.Log;

/**
 * a class to process the long tap events in empty areas
 * of the map to add a new point of interest
 */
public class NewPoiOverlay extends Overlay {
	
	/*
	 *  private class level constants
	 */
	private final boolean V_LOG = true;
	private String TAG = "NewPoiOverlay";
	
	private final long VIBRATE_DURATION = 300; // 300 milliseconds
	
	/*
	 * private class level variables
	 */
	private Activity context;
	
	/**
	 * construct a new instance of this class
	 */
	public NewPoiOverlay(Context context) {
		super();
		
		this.context = (Activity) context;
	}

	@Override
	protected void drawOverlayBitmap(Canvas canvas, Point drawPosition, Projection projection, byte drawZoomLevel) {
		// don't do anything as this overlay doesn't have any UI
	}
	
	@Override
	public boolean onLongPress(GeoPoint geoPoint, MapView mapView) {
		
		//debug code
		if(V_LOG) {
			Log.v(TAG, "onLongPress on NewPoiOverlay detected");
			Log.v(TAG, "Latitude: " + geoPoint.getLatitude());
			Log.v(TAG, "longitude: " + geoPoint.getLongitude());
		}
		
		// provide some haptic feedback using the vibrator
		Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mVibrator.vibrate(VIBRATE_DURATION);
		
		// create a new intent
		Intent mIntent = new Intent(context, org.servalproject.maps.NewPoiActivity.class);
		mIntent.putExtra("latitude", geoPoint.getLatitude());
		mIntent.putExtra("longitude", geoPoint.getLongitude());
		context.startActivity(mIntent);
		
		// return true to indicate the long press has been dealt with
		return true;
		
	}

}
