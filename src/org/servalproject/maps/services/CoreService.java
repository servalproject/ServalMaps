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
package org.servalproject.maps.services;

import java.io.IOException;

import org.servalproject.maps.R;
import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.location.MockLocations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

/**
 * The CoreService class undertakes the core activities that need to be 
 * undertaken even while activities are not present
 */
public class CoreService extends Service {
	
	// class level constants
	private final int STATUS_NOTIFICATION = 0;
	
	private final boolean V_LOG = true;
	private final String  TAG = "CoreService";
	
	private final boolean USE_MOCK_LOCATIONS = true;
	
	// class level variables
	private LocationCollector locationCollector;
	private LocationManager locationManager;
	private MockLocations mockLocations = null;
	
	/*
	 * called when the service is created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		
		// create the necessary supporting variables
		locationCollector = new LocationCollector(this.getApplicationContext());
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		if(USE_MOCK_LOCATIONS) {
			try{
				mockLocations = new MockLocations(this.getApplicationContext()); 
			} catch (IOException e) {
				Log.e(TAG, "unable to create MockLocations instance", e);
			}
		}
		
		if(V_LOG) {
			Log.v(TAG, "Service Created");
		}
		
	}
	
	/*
	 * called when the service is started
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(V_LOG) {
			Log.v(TAG, "Service Started");
		}
		
		// add the notification icon
		addNotification();
		
		if(USE_MOCK_LOCATIONS && mockLocations != null) {
			Thread mockLocationThread = new Thread(mockLocations, "MockLocations");
			mockLocationThread.start();
		}
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationCollector);
		
		// If service gets killed, after returning from here, restart
	    return START_STICKY;
	}
	
	// private method used to add the notification icon
	private void addNotification() {
		// add a notification icon
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		//TODO update this with a better icon
		//TODO update this with a custom notification with stats
		int mNotificationIcon = R.drawable.ic_launcher;
		CharSequence mTickerText = getString(R.string.system_notification_ticker_text);
		long mWhen = System.currentTimeMillis();
		
		// create the notification and set the flag so that it stays up
		Notification mNotification = new Notification(mNotificationIcon, mTickerText, mWhen);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		// get the content of the notification
		CharSequence mNotificationTitle = getString(R.string.system_notification_title);
		CharSequence mNotificationContent = getString(R.string.system_notification_content);
		
		// create the intent for the notification
		// set flags so that the user returns to this activity and not a new one
		Intent mNotificationIntent = new Intent(this, org.servalproject.maps.MapActivity.class);
		mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		// create a pending intent so that the system can use the above intent at a later time.
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// complete the setup of the notification
		mNotification.setLatestEventInfo(getApplicationContext(), mNotificationTitle, mNotificationContent, mPendingIntent);
		
		// add the notification
		mNotificationManager.notify(STATUS_NOTIFICATION, mNotification);
	}
	
	/*
	 * called when the service kills the service
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// tidy up any used resources etc.
		
		// clear the notification
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(STATUS_NOTIFICATION);
		
		// stop listening for location updates
		locationManager.removeUpdates(locationCollector);
		
		if(USE_MOCK_LOCATIONS && mockLocations != null) {
			mockLocations.requestStop();
		}
		
		super.onDestroy();
		
		if(V_LOG) {
			Log.v(TAG, "Service Destroyed");
		}
	}

	/*
	 * this isn't a bound service so we can safely return null here
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
