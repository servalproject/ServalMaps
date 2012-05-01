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

import org.servalproject.maps.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

/**
 * receives broadcasts about the battery level
 */
public class BatteryLevelReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "BatteryLevelReceiver";
	
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// check to see what level we're being informed about
		if(V_LOG) {
			Log.v(TAG, "onReceive method called");
		}
		
		// check on the action associated with the intent
		if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW) == true) {
			// notification that the battery is low
			if(V_LOG) {
				Log.v(TAG, "received notification that battery is low");
			}
			
			// shutdown the service
			context.stopService(new Intent(context, org.servalproject.maps.services.CoreService.class));
			
			// inform the user
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
	        mBuilder.setMessage(R.string.system_battery_status_low)
	               .setCancelable(false)
	               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   dialog.cancel();
	                   }
	               });
	        AlertDialog mAlert = mBuilder.create();
	        mAlert.show();
			
		} else if(intent.getAction().equals(Intent.ACTION_BATTERY_OKAY) == true) {
			// notification that the battery is ok after being low
			// restart the GPS
			if(V_LOG) {
				Log.v(TAG, "received notification that battery is ok");
			}
			
			// start the service
			context.startService(new Intent(context, org.servalproject.maps.services.CoreService.class));
			
			// inform the user
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
	        mBuilder.setMessage(R.string.system_battery_status_ok)
	               .setCancelable(false)
	               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   dialog.cancel();
	                   }
	               });
	        AlertDialog mAlert = mBuilder.create();
	        mAlert.show();
		}
	}
}
