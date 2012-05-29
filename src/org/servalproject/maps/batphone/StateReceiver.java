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

import org.servalproject.maps.R;
import org.servalproject.maps.ServalMaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 *  a class used to respond to state changes, or polls in state, of the 
 *  Serval Mesh application
 */
public class StateReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String sTag = "StateReceiver";


	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// check on the intent that was recieved
		if(V_LOG) {
			Log.v(sTag, "receiver was called");
		}

		// check on the intent action
		if(intent.getAction().equals("org.servalproject.ACTION_STATE_CHECK_UPDATE")) {
			// this is a response to the polling of the status of the serval mesh
			int mStateIndicator = intent.getIntExtra("state", -1);
			
			if(mStateIndicator == -1) {
				Log.w(sTag, "receover called with missing 'state' extra");
				return;
			}
			
			ServalMaps.BatphoneState mState = ServalMaps.BatphoneState.values()[mStateIndicator];
			
			// store the state for use later
			ServalMaps mApplication = (ServalMaps) context.getApplicationContext();
			mApplication.setBatphoneState(mState);
			mApplication = null;
			
			
		} else if(intent.getAction().equals("org.servalproject.ACTION_STATE")) {
			// this is an automatic change in state
			int mStateIndicator = intent.getIntExtra("state", -1);
			
			if(mStateIndicator == -1) {
				Log.w(sTag, "receover called with missing 'state' extra");
				return;
			}
			
			ServalMaps.BatphoneState mState = ServalMaps.BatphoneState.values()[mStateIndicator];
			
			// store the state for use later
			ServalMaps mApplication = (ServalMaps) context.getApplicationContext();
			mApplication.setBatphoneState(mState);
			mApplication = null;
			
			switch(mState) {
			case On:
				// serval mesh is now on
				Toast.makeText(context.getApplicationContext(), R.string.system_toast_batphone_on, Toast.LENGTH_LONG).show();
				break;
			case Off:
				// serval mesh is now off
				Toast.makeText(context.getApplicationContext(), R.string.system_toast_batphone_off, Toast.LENGTH_LONG).show();
				break;
			case Broken:
				// Serval Mesh is broken
				Toast.makeText(context.getApplicationContext(), R.string.system_toast_batphone_off, Toast.LENGTH_LONG).show();
				break;
			}
		} else {
			// wrong intent action, so exit early
			Log.w(sTag, "receiver called with an unknown intent action, '" + intent.getAction() + "'");
			return;
		}
	}
}
