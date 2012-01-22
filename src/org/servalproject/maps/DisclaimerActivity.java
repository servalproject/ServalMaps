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

import java.util.ArrayList;

import org.servalproject.maps.services.MapDataInfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DisclaimerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "DisclaimerActivity";
	
	/*
	 * private class level variables
	 */
    IntentFilter BroadcastFilter;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer);
        
        // capture the touch on the buttons
        Button mButton = (Button) findViewById(R.id.disclaimer_ui_btn_continue);
        mButton.setOnClickListener(this);
        
        // register the broadcast receiver
        BroadcastFilter = new IntentFilter();
        BroadcastFilter.addAction("org.servalproject.maps.MAP_DATA_LIST");
        registerReceiver(MapDataReceiver, BroadcastFilter);
    }
    
    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		// check which button was touched
		if(v.getId() == R.id.disclaimer_ui_btn_continue) {
			// show the send a message activity
			Intent mSendActivityIntent = new Intent("org.servalproject.maps.MAP_DATA");
			startService(mSendActivityIntent);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		// unregister the receiver when the activity is no longer active
		unregisterReceiver(MapDataReceiver);
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		// register the receiver again when the activity becomes active
		registerReceiver(MapDataReceiver, BroadcastFilter);
		super.onResume();
	}
	
	/*
	 * a broadcast receiver to get the information from the activity
	 */
	private BroadcastReceiver MapDataReceiver = new BroadcastReceiver() {
		
		// listen for the appropriate broadcast
		@Override
		public void onReceive(Context context, Intent intent) {
			
			int mMapFileCount = intent.getIntExtra("count", 0);
			//MapDataInfo[] mMapDataInfoList = new MapDataInfo[0];
			ArrayList<MapDataInfo> mMapDataInfoList = null;
			
			if(mMapFileCount > 0){
				mMapDataInfoList = intent.getParcelableArrayListExtra("files");
			}
			
			if(V_LOG) {
				Log.v(TAG, "File Count: " + mMapFileCount);
				if(mMapDataInfoList != null) {
					Log.v(TAG, "Array Count: " + mMapDataInfoList.size());
				}
			}
			
		}
		
	};
}