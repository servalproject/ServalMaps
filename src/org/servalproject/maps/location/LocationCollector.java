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

package org.servalproject.maps.location;

import java.util.TimeZone;

import org.servalproject.maps.ServalMaps;
import org.servalproject.maps.protobuf.BinaryFileWriter;
import org.servalproject.maps.provider.LocationsContract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Used to listen for location updates and store them in the database
 */
public class LocationCollector implements LocationListener {
	
	/*
	 * class level constants
	 */
	private boolean V_LOG = false;
	private String  TAG   = "LocationCollector";
	
	/*
	 * class level variables
	 */
	private Context context;
	
	
	/**
	 * the most recent and most accurate location information
	 */
	private static volatile Location currentLocation = null;
	private String timeZone = TimeZone.getDefault().getID();
	
	private String phoneNumber;
	private String subscriberId;
	
	private ContentResolver contentResolver;
	
	public LocationCollector(Context context) {

		super();
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		ServalMaps mApplication = (ServalMaps) context.getApplicationContext();
		phoneNumber = mApplication.getPhoneNumber();
		subscriberId = mApplication.getSid();
		mApplication = null;
		
		contentResolver = context.getContentResolver();
		
		this.context = context;
	}
	
	/**
	 * get the most recent and most accurate location information
	 */
	public static Location getLocation() {
		return currentLocation;
	}

	/*
	 * Called when the location has changed.
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		
		// process this new location
		if(V_LOG) {
			Log.v(TAG, "new location received");
		}
		
		// check to see if this location is better than the one we have already
		if(isBetterLocation(location, currentLocation) == true) {
			
			if(V_LOG) {
				Log.v(TAG, "new location is better than current location");
				Log.v(TAG, "Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
			}
			
			// save the location for later
			currentLocation = location;
			
			if(phoneNumber == null || subscriberId == null) {
				// these may be null but will be populated once the 
				// sticky to Serval Mesh returns
				return;
			}
			
			long mTime = System.currentTimeMillis();
			
			ContentValues mNewValues = new ContentValues();
			mNewValues.put(LocationsContract.Table.PHONE_NUMBER, phoneNumber);
			mNewValues.put(LocationsContract.Table.SUBSCRIBER_ID, subscriberId);
			mNewValues.put(LocationsContract.Table.LATITUDE, location.getLatitude());
			mNewValues.put(LocationsContract.Table.LONGITUDE, location.getLongitude());
			mNewValues.put(LocationsContract.Table.TIMEZONE, timeZone);
			mNewValues.put(LocationsContract.Table.TIMESTAMP, mTime);

			try {
				Uri newRecord = contentResolver.insert(LocationsContract.CONTENT_URI, mNewValues);
				if(V_LOG) {
					Log.v(TAG, "new location record created with id: " + newRecord.getLastPathSegment());
				}
				
				// functionality not required at this stage 
				//OutgoingMeshMS.sendLocationMessage(context, newRecord.getLastPathSegment());
				
				// write an entry to the binary log file
				BinaryFileWriter.writeLocation(context,  newRecord.getLastPathSegment());
			}catch (SQLException e) {
				Log.e(TAG, "unable to add new location record", e);
			}
		} else {
			if(V_LOG) {
				Log.v(TAG, "new location is not better than current location");
			}
		}

	}

	/*
	 * Called when the provider is disabled by the user.
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * Called when the provider is enabled by the user.
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * Called when the provider status changes.
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	/*
	 * The following two methods are sourced from:
	 * http://developer.android.com/guide/topics/location/obtaining-user-location.html#BestPerformance
	 * 
	 * They are used under the terms of the Apache 2.0 license
	 * http://www.apache.org/licenses/LICENSE-2.0
	 */
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

}
