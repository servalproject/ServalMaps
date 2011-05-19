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

import java.util.concurrent.LinkedBlockingQueue;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * Class used to receive notifications of new location data from the
 * android location subsystem
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class LocationCollector implements LocationListener {
	
	/*
	 * private class level variables
	 */
	private LinkedBlockingQueue<Location> locationQueue;
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-LC";
	
	/**
	 * constructor for this class
	 * 
	 * @param queue a LinkedBlockingQueue used to store location data for further processing
	 */
	public LocationCollector(LinkedBlockingQueue<Location> queue) {
		
		if(queue == null) {
			throw new IllegalArgumentException("the queue parameter cannot be null");
		}
		
		locationQueue = queue;
		
		if(V_LOG) {
			Log.v(TAG, "location collector instantiated");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		// add the location to the queue for later processing
		locationQueue.add(location);
		
		if(V_LOG) {
			Log.v(TAG, "new location added to the queue");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		// TODO inform the user somehow that the location provider is disabled
		if(V_LOG) {
			Log.v(TAG, "the '" + provider + "' location provider is disabled");
		}

	}

	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		if(V_LOG) {
			Log.v(TAG, "the '" + provider + "' location provider is enabled");
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		if (status == LocationProvider.AVAILABLE) {
			if(V_LOG) {
				Log.v(TAG, "the '" + provider + "' location provider is now available");
			}
		} else if (status == LocationProvider.OUT_OF_SERVICE) {
			if(V_LOG) {
				Log.v(TAG, "the '" + provider + "' location provider is now out of service");
			}
		} else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			if(V_LOG) {
				Log.v(TAG, "the '" + provider + "' location provider is now temporarily unavailable");
			}
		} else {
			if(V_LOG) {
				Log.v(TAG, "the '" + provider + "' location provider reported unknown status '" + status + "'");
			}
		}

	}

}