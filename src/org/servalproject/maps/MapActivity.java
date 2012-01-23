/**
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
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

import org.mapsforge.android.maps.MapView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * An activity to show a map
 */
public class MapActivity extends org.mapsforge.android.maps.MapActivity {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "MapActivity";
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        // get the map data file name
        Bundle mBundle = this.getIntent().getExtras();
        String mMapFileName = mBundle.getString("mapFileName");
        
		// instantiate mapsforge classes
		MapView mMapView = new MapView(this);
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		
		if(mMapFileName != null) {
			mMapView.setMapFile(mMapFileName);
		}
		
		setContentView(mMapView);
		
		// add a notification icon
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		//TODO update this with a better icone
		int mNotificationIcon = R.drawable.ic_launcher;
		CharSequence mTickerText = getString(R.string.system_notification_ticker_text);
		long mWhen = System.currentTimeMillis();
		
		Notification mNotification = new Notification(mNotificationIcon, mTickerText, mWhen);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		CharSequence mNotificationTitle = getString(R.string.system_notification_title);
		CharSequence mNotificationContent = getString(R.string.system_notification_content);
		
		Intent mNotificationIntent = new Intent(this, org.servalproject.maps.MapActivity.class);
		mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // make sure we come back to this instance
		
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		mNotification.setLatestEventInfo(getApplicationContext(), mNotificationTitle, mNotificationContent, mPendingIntent);
		
		mNotificationManager.notify(0, mNotification);
		
		
	}
}
