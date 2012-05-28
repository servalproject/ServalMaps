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
import org.servalproject.maps.location.JsonLocationWriter;
import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.location.MockLocations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The CoreService class undertakes the core activities that need to be 
 * undertaken even while activities are not present
 */
public class CoreService extends Service {
	
	/*
	 * public class level constants
	 */
	public static final String PREFERENCES_NAME = "core-service";
	public static final String PREFERENCES_VALUE = "uptime";

	// class level constants
	private final int STATUS_NOTIFICATION = 0;

	private final String JSON_UPDATE_DELAY_DEFAULT = "60000";

	private final boolean V_LOG = false;
	private final String  TAG = "CoreService";

	// class level variables
	private LocationCollector locationCollector;
	private LocationManager locationManager;
	private MockLocations mockLocations = null;
	private JsonLocationWriter jsonLocationWriter = null;
	private Thread mockLocationsThread = null;
	private Thread jsonLocationWriterThread = null;

	private SharedPreferences preferences = null;

	private Long uptimeStart;

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

		// get the preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		// determine if mock locations should be used
		if(preferences.getBoolean("preferences_developer_mock_locations", false) == true ) {
			try {
				mockLocations = new MockLocations(this.getApplicationContext()); 
			} catch (IOException e) {
				Log.e(TAG, "unable to create MockLocations instance", e);
			}
		}

		// determine of JSON output should be created
		if(preferences.getBoolean("preferences_map_output_json", false) == true) {
			String updateDelay = preferences.getString("preferences_map_output_json_interval", null);

			if(updateDelay == null) {
				updateDelay = JSON_UPDATE_DELAY_DEFAULT;
			}

			try {
				jsonLocationWriter = new JsonLocationWriter(getApplicationContext(), Long.parseLong(updateDelay));
			} catch (IOException e) {
				Log.e(TAG, "unable to create jsonLocationWriter instance", e);
			}
		}

		// listen for changes in the preferences
		preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		if(V_LOG) {
			if(mockLocations == null) {
				Log.v(TAG, "mock locations are not used");
			} else {
				Log.v(TAG, "mock locations are being used");
			}

			if(jsonLocationWriter == null) {
				Log.v(TAG, "JSON location writing is not occuring");
			} else {
				Log.v(TAG, "JSON location writing is occuring");
			}

			Log.v(TAG, "Service Created");
		}
		
	}

	// listen for changes to the shared preferences
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

		/* 
		 * (non-Javadoc)
		 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

			if(V_LOG) {
				Log.v(TAG, "a change in shared preferences has been deteceted");
				Log.v(TAG, "preference changed: '" + key + "'");
			}

			// check to see if this is the preference that is of interest
			if(key.equals("preferences_developer_mock_locations") == true) {
				if(V_LOG) {
					Log.v(TAG, "preference changed: 'preferences_developer_mock_locations'");
				}

				// see if the preference is true
				if(preferences.getBoolean("preferences_developer_mock_locations", false) == true ) {
					// preference is true so start using mock locations if required
					if(mockLocations == null) {
						try{
							mockLocations = new MockLocations(getApplicationContext());
							Thread mockLocationThread = new Thread(mockLocations, "MockLocations");
							mockLocationThread.start();
						} catch (IOException e) {
							Log.e(TAG, "unable to create MockLocations instance", e);
						}
					}
				} else {
					// preference is false if of we're doing it stop
					if(mockLocations != null) {
						mockLocations.requestStop();
						mockLocations = null;
					}
				}
			} else if(key.equals("preferences_map_output_json") == true) {
				if(V_LOG) {
					Log.v(TAG, "preference changed: 'preferences_map_output_json'");
				}

				if(preferences.getBoolean("preferences_map_output_json", false) == true) {
					// preference is true so start outputing json if required
					if(jsonLocationWriter == null) {
						String updateDelay = preferences.getString("preferences_map_output_json_interval", null);

						if(updateDelay == null) {
							updateDelay = JSON_UPDATE_DELAY_DEFAULT;
						}
						try {
							jsonLocationWriter = new JsonLocationWriter(getApplicationContext(), Long.parseLong(updateDelay));
							jsonLocationWriterThread = new Thread(jsonLocationWriter, "JsonLocationWriter");
							jsonLocationWriterThread.start();
						} catch (IOException e) {
							Log.e(TAG, "unable to create jsonLocationWriter instance", e);
						}
					}
				}
			} else if(key.equals("preferences_map_output_json_interval") == true) {
				String updateDelay = preferences.getString("preferences_map_output_json_interval", null);

				if(updateDelay == null) {
					updateDelay = JSON_UPDATE_DELAY_DEFAULT;
				}

				if(jsonLocationWriter != null) {
					jsonLocationWriter.setUpdateDelay(Long.parseLong(updateDelay));
				}
			}

		}
	};

	/*
	 * called when the service is started
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		uptimeStart = System.currentTimeMillis();

		if(V_LOG) {
			Log.v(TAG, "Service Started");
		}

		// add the notification icon
		addNotification();

		if(mockLocations != null) {
			mockLocationsThread = new Thread(mockLocations, "MockLocations");
			mockLocationsThread.start();
		}

		if(jsonLocationWriter != null) {
			jsonLocationWriterThread = new Thread(jsonLocationWriter, "JsonLocationWriter");
			jsonLocationWriterThread.start();
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
		int mNotificationIcon = R.drawable.ic_notification;
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

		if(mockLocations != null) {
			mockLocations.requestStop();
			if(mockLocationsThread != null) {
				mockLocationsThread.interrupt();
			}

		}

		if(jsonLocationWriter != null) {
			jsonLocationWriter.requestStop();
			if(jsonLocationWriterThread != null) {
				jsonLocationWriterThread.interrupt();
			}
		}

		// update the uptime count
		long mUptime = System.currentTimeMillis() - uptimeStart;
		
		SharedPreferences mPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		mUptime = mUptime + mPreferences.getLong(PREFERENCES_VALUE, 0);
		
		Editor mEditor = mPreferences.edit();
		mEditor.putLong(PREFERENCES_VALUE, mUptime);
		mEditor.commit();
		
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
