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
package org.servalproject.maps.batphone;

import org.servalproject.maps.ServalMaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * a class used to receive the phone number of the device from the 
 * sticky intent provided by the main Serval Software
 */
public class PhoneNumberReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "PhoneNumberReceiver";
	
	/*
	 * private class level variables
	 */
	private ServalMaps application;
	
	/**
	 * instantiate the object to receive notification of 
	 * the phone number and sid from the main Serval Software 
	 * 
	 * @param context the application context
	 * @throws IllegalArgumentException if the context is null
	 */
	public PhoneNumberReceiver(Context context) {
		
		super();
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		application = (ServalMaps) context;
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d(TAG, "receiver called");
		
		// store the phone number and sid for later
		if(intent.getStringExtra("did") != null) {
			application.setPhoneNumber(intent.getStringExtra("did"));
		} else {
			Log.e(TAG, "unable to retrieve Serval Mesh phone number");
		}
		
		if(intent.getStringExtra("sid") != null ) {
			application.setSid(intent.getStringExtra("sid"));
		} else {
			Log.e(TAG, "unable to retrieve the Serval Mesh sid");
		}
	}

}
