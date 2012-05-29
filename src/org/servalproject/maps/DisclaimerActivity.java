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

package org.servalproject.maps;

import java.io.File;
import java.util.ArrayList;

import org.servalproject.maps.batphone.PhoneNumberReceiver;
import org.servalproject.maps.batphone.StateReceiver;
import org.servalproject.maps.parcelables.MapDataInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	//private final String  TAG = "DisclaimerActivity";

	private final int NO_FILES_DIALOG = 0;
	private final int MANY_FILES_DIALOG = 1;
	private final int NO_SERVAL_DIALOG = 2;
	private final int SERVAL_NOT_RUNNING_DIALOG = 3;

	/*
	 * private class level variables
	 */
	private PhoneNumberReceiver phoneNumberReceiver = null;
	private StateReceiver batphoneStateReceiver = null;
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
		
		// flag to indicate that all of the checks have completed successfully
		boolean mContinue = true;

		// capture the touch on the buttons
		Button mButton = (Button) findViewById(R.id.disclaimer_ui_btn_continue);
		mButton.setOnClickListener(this);

		// check on the state of the storage
		String mStorageState = Environment.getExternalStorageState();

		if(Environment.MEDIA_MOUNTED.equals(mStorageState) == false) {

			// show a dialog and disable the button
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_storage)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog mAlert = mBuilder.create();
			mAlert.show();
			
			mContinue = false;
		} 

		// check to see if the Serval Mesh software is installed
		try {
			getPackageManager().getApplicationInfo("org.servalproject", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			//Log.e(TAG, "serval maps was not found", e);
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_serval_mesh)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog mAlert = mBuilder.create();
			mAlert.show();

			mContinue = false;
		}

		if(mContinue) {
			// register the various receivers
			registerReceivers();
		} else {
			mButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		// check which button was touched
		if(v.getId() == R.id.disclaimer_ui_btn_continue) {

			// check to see if we know the device phone number
			ServalMaps mApplication = (ServalMaps) getApplication();

			if(mApplication.getPhoneNumber() == null || mApplication.getSid() == null) {
				// show the appropriate dialog
				showDialog(NO_SERVAL_DIALOG);
			} else if(mApplication.getBatphoneState() != ServalMaps.BatphoneState.On) {
				// show the appropriate dialog
				showDialog(SERVAL_NOT_RUNNING_DIALOG);
			}else {
				// check for files and go to the map activity
				Intent mIntent = new Intent("org.servalproject.maps.MAP_DATA");
				startService(mIntent);
			}
		}
	}
	
	/*
	 * create the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu based on the XML
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.disclaimer_activity, menu);
	    return true;
	}
	
	/*
	 * handle click events from the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent mIntent;
		
		switch(item.getItemId()){
		case R.id.menu_disclaimer_activity_export:
			// show the export activity
			mIntent = new Intent(this, org.servalproject.maps.ExportActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_delete:
			// show the delete activity
			mIntent = new Intent(this, org.servalproject.maps.DeleteActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_help_about:
			// show the help activity
			mIntent = new Intent(this, org.servalproject.maps.AboutActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_close:
			// close this activity
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		// unregister the various receivers
		unRegisterReceivers();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		// register the various receivers
		registerReceivers();
		super.onResume();
	}

	// private method to register the various broadcast receivers
	private void registerReceivers() {

		// register the map data receiver
		IntentFilter mBroadcastFilter = new IntentFilter();
		mBroadcastFilter.addAction("org.servalproject.maps.MAP_DATA_LIST");
		registerReceiver(MapDataReceiver, mBroadcastFilter);

		// register for the sticky broadcast from the main Serval Software
		if(phoneNumberReceiver == null) {
			phoneNumberReceiver = new PhoneNumberReceiver(getApplication());
		}

		mBroadcastFilter = new IntentFilter();
		mBroadcastFilter.addAction("org.servalproject.SET_PRIMARY");
		registerReceiver(phoneNumberReceiver, mBroadcastFilter);
		
		if(batphoneStateReceiver == null) {
			batphoneStateReceiver = new StateReceiver();
		}
		
		mBroadcastFilter = new IntentFilter();
		mBroadcastFilter.addAction("org.servalproject.ACTION_STATE_CHECK_UPDATE");
		mBroadcastFilter.addAction("org.servalproject.ACTION_STATE");
		registerReceiver(batphoneStateReceiver, mBroadcastFilter);
		
		// send off an intent to poll for the current state of the serval mesh
		Intent mIntent = new Intent("org.servalproject.ACTION_STATE_CHECK");
		startService(mIntent);
		
	}

	// private method to unregister the various broadcast receivers
	private void unRegisterReceivers() {

		unregisterReceiver(MapDataReceiver);

		if(phoneNumberReceiver != null) {
			unregisterReceiver(phoneNumberReceiver);
		}
		
		if(batphoneStateReceiver != null) {
			unregisterReceiver(batphoneStateReceiver);
		}
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
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showMapActivity(null);
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case MANY_FILES_DIALOG:

			mFileNames = new CharSequence[mapDataInfoList.size()];
			File mFile = null;

			for(int i = 0; i < mapDataInfoList.size(); i++) {
				MapDataInfo mInfo = mapDataInfoList.get(i);
				mFile = new File(mInfo.getFileName());
				mFileNames[i] = mFile.getName();
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
		case NO_SERVAL_DIALOG:
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_serval)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
			mDialog = mBuilder.create();
			break;
		case SERVAL_NOT_RUNNING_DIALOG:
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_serval_not_running)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// check for files and go to the map activity
					Intent mIntent = new Intent("org.servalproject.maps.MAP_DATA");
					startService(mIntent);
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
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