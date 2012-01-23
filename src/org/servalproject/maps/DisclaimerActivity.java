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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * activity to show the disclaimer, it is the start activity
 */
public class DisclaimerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "DisclaimerActivity";
	
	private final int NO_FILES_DIALOG = 0;
	private final int MANY_FILES_DIALOG = 1;
	
	/*
	 * private class level variables
	 */
    private IntentFilter BroadcastFilter;
    private int mapFileCount = 0;
	private ArrayList<MapDataInfo> mapDataInfoList = null;
	private CharSequence[] mFileNames = null;
	
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
			
			// get the list of files
			mapFileCount = intent.getIntExtra("count", 0);
			if(mapFileCount > 0){
				mapDataInfoList = intent.getParcelableArrayListExtra("files");
			}
			
			// show the appropriate dialog
			if(mapFileCount == 0) {
				showDialog(NO_FILES_DIALOG);
			} else if(mapFileCount == 1) {
				// show the map activity
				MapDataInfo mInfo = mapDataInfoList.get(0);
				showMapActivity(mInfo.getFileName());
			} else {
				// show the map file chooser
				showDialog(MANY_FILES_DIALOG);
			}
		}
	};
	
	/*
	 * dialog related methods
	 */

	/*
	 * callback method used to construct the required dialog
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
	
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;
			
		switch(id) {
		case NO_FILES_DIALOG:
			// show an alert dialog
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_files)
			.setCancelable(false)
			.setPositiveButton(R.string.misc_dialog_yes_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showMapActivity(null);
				}
			})
			.setNegativeButton(R.string.misc_dialog_no_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case MANY_FILES_DIALOG:
			
			mFileNames = new CharSequence[mapDataInfoList.size()];
			
			for(int i = 0; i < mapDataInfoList.size(); i++) {
				MapDataInfo mInfo = mapDataInfoList.get(i);
				mFileNames[i] = mInfo.getFileName();
			}
			
			mBuilder.setTitle(R.string.disclaimer_ui_dialog_many_files_title)
			.setCancelable(false)
			.setItems(mFileNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					showMapActivity(mFileNames[item].toString());
				}
			});
			mDialog = mBuilder.create();
			break;
		default:
			mDialog = null;
		}
	
		// return the created dialog
		return mDialog;	
	}
	
	/*
	 * prepare and show the map activity
	 */
	private void showMapActivity(String mapDataFile) {
		// show the map activity
		Intent mMapIntent = new Intent(this, org.servalproject.maps.MapActivity.class);
		mMapIntent.putExtra("mapFileName", mapDataFile);
		startActivityForResult(mMapIntent, 0);
	}
}