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

package org.servalproject.mappingservices.services;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.servalproject.mappingservices.MappingServicesApplication;
import org.servalproject.mappingservices.content.DatabaseUtils;
import org.servalproject.mappingservices.content.LocationProvider;
import org.servalproject.mappingservices.net.NetworkException;
import org.servalproject.mappingservices.net.PacketBuilder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

/**
 * save new location data to the database and sends new location packets
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class LocationSaver implements Runnable {
	
	/*
	 * public class constants
	 */
	/**
	 * the maximum allowed age of location information (in seconds)
	 */
	//public static final int MAX_LOCATION_AGE = 1000 * 60 * 2;
	public static final int MAX_LOCATION_AGE = 1000 * 30;
	
	
	/*
	 * private class level variables
	 */
	private LinkedBlockingQueue<Location> locationQueue;
	
	private volatile boolean keepGoing = true;
	
	private ContentResolver contentResolver;
	private Uri locationContentUri;
	
	private String timeZone = TimeZone.getDefault().getID();
	private String recordType = "1";
	
	private PacketBuilder packetBuilder;
	
	private Context context;
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-LS";
	
	/**
	 * constructor for this class
	 * 
	 * @param queue a LinkedBlockingQueue used to receive location data for further processing from the LocationCollector class
	 */
	public LocationSaver(LinkedBlockingQueue<Location> queue, Context context) {
		
		if(queue == null || context == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		locationQueue = queue;
		
		this.contentResolver = context.getContentResolver();
		locationContentUri = LocationProvider.CONTENT_URI;
		
		packetBuilder = new PacketBuilder(context);
		
		this.context = context;
		
		if(V_LOG) {
			Log.v(TAG, "location saver instantiated");
		}
		
	}
	
	/*
	 * when invoked, run for as long as possible and process new locations by
	 * saving them to the database and sending new packets
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		Location mNewLocation = null;
		Location mOldLocation = null;
		
		if(V_LOG) {
			Log.v(TAG, "location saver started");
		}
		
		while(keepGoing == true) {
			// get a new location from the queue
			try {
				mNewLocation = locationQueue.take();
				
				// see if this location is better than the one we had before
				if(isBetterLocation(mNewLocation, mOldLocation) == true) {
					
					saveAndSendLocation(mNewLocation);
					
					mOldLocation = mNewLocation;
					
				}
			} catch (InterruptedException e) {
				if(V_LOG) {
					Log.v(TAG, "thread was interrupted", e);
				}
			}
			
		}
		
	}
	
	/*
	 * private method to save details of this location
	 * 
	 * @param location details of the location to save
	 */
	private void saveAndSendLocation(Location location) {
		
		// get the timestamp
		Date mDate = new Date();
		long mSeconds = mDate.getTime();
		mSeconds = mSeconds / 1000;
		
		// start a new list of values
		ContentValues mValues = new ContentValues();
		mValues.put(LocationProvider.TYPE_FIELD, recordType);
		
		 // get the device phone number and SID
        MappingServicesApplication mApplication = (MappingServicesApplication)context.getApplicationContext();
        mValues.put(LocationProvider.PHONE_NUMBER_FIELD, mApplication.getPhoneNumber());
        mValues.put(LocationProvider.SID_FIELD,mApplication.getSid());
		
		mValues.put(LocationProvider.LATITUDE_FIELD, location.getLatitude());
		mValues.put(LocationProvider.LONGITUDE_FIELD, location.getLongitude());
		mValues.put(LocationProvider.TIMESTAMP_FIELD, mSeconds);
		mValues.put(LocationProvider.TIMEZONE_FIELD, timeZone);
		mValues.put(LocationProvider.SELF_FIELD, "y");
		mValues.put(LocationProvider.TIMESTAMP_UTC_FIELD, DatabaseUtils.getTimestampAsUtc(Long.toString(mSeconds), timeZone));
		
		// add the row
		try {
			Uri mNewRecord = contentResolver.insert(locationContentUri, mValues);
			
			if(mNewRecord != null) {
	        	try {
	        		packetBuilder.buildAndSendLocation(mNewRecord.getLastPathSegment(), true);
	        	} catch(NetworkException e) {
	        		Log.e(TAG, "unable to send new location packet", e);
	        	}
			}
			
		} catch (SQLException e) {
			Log.e(TAG, "unable to save new location data", e);
		}
		
		//status message
		if(V_LOG) {
			Log.v(TAG, "new location data saved to database and a new packet sent");
		}
	}
	
	/*
	 * private method to determine if the new location is better than the old location 
	 * adapted from the android code sample available here (2011-05-19):
	 * http://developer.android.com/guide/topics/location/obtaining-user-location.html
	 * and licensed under the Apache 2.0 License:
	 * http://www.apache.org/licenses/LICENSE-2.0
	 */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > MAX_LOCATION_AGE;
		boolean isSignificantlyOlder = timeDelta < -MAX_LOCATION_AGE;
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
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

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

	/*
	 * Checks whether two providers are the same 
	 * 
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	/* end adapted code */
	
	/**
	 * request that the thread stops
	 */
	public void requestStop() {
		keepGoing = false;
	}
}
