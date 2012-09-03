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
package org.servalproject.maps.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * receive notification that a user has touched a download item in the DownloadManager UI
 */
public class DownloadNotificationReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private static final String TAG = "DownloadNotificationReceiver";

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// make sure this is the right intent
		if(intent == null) {
			Log.w(TAG, "receiver called without intent");
			return;
		}
		
		String mIntentAction = intent.getAction();
		
		if(mIntentAction.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED) == true) {
			// this is the right intent
			
			// launch standard download manager interface
			Intent mIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mIntent);
			
		} else {
			Log.w(TAG, "receiver called with wrong intent");
		}
	}
}
