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

package org.servalproject.maps.rhizome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * receives the broadcasts from Rhizome about new files being available
 */
public class RhizomeBroadcastReceiver extends BroadcastReceiver {
	
	// private class level constants
	private final boolean V_LOG = true;
	private final String TAG = "RhizomeBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle mBundle = intent.getExtras();
		
		if(V_LOG) {
			Log.v(TAG, "received intent with action: " + intent.getAction());
			Log.v(TAG, "file name: " + mBundle.getString("path"));
			Log.v(TAG, "version: " + Long.toString(mBundle.getLong("version")));
			Log.v(TAG, "name: " + mBundle.getString("name"));
		}
	}

}
