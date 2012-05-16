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

import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.services.CoreService;
import org.servalproject.maps.stats.StatsAsyncTask;
import org.servalproject.maps.utils.HttpUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * activity to show various statistics for Serval Maps
 */
public class StatsActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private String TAG = "StatsActivity";
	
	/*
	 * private class level variables
	 */
	private String[] dataElems = new String[6];

	private String[] dataLabels = {
			"Version number:",
			"Location records:", 
			"POI records:", 
			"Total photos:", 
			"Photos taken by you:", 
			"Core service uptime:"};
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);
        
        /*
         * populate the statistics
         */
        
        // location records
        ContentResolver mContentResolver = getApplicationContext().getContentResolver();
        
        String[] mProjection = new String[1];
        mProjection[0] = LocationsContract.Table._ID;
        
        Cursor mCursor = mContentResolver.query(LocationsContract.CONTENT_URI, mProjection, null, null, null);
        
        TextView mTextView = (TextView) findViewById(R.id.stats_ui_txt_version);
        
        try {
        	PackageInfo mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        	mTextView.setText(mPackageInfo.versionName);
        	dataElems[0] = mPackageInfo.versionName;
        } catch (NameNotFoundException e) {
        	Log.e(TAG, "unable to determine version information", e);
        	mTextView.setText(String.format(getString(R.string.about_ui_lbl_version), getString(R.string.misc_not_available)));
        }	
        		
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_locations);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[1] = Integer.toString(mCursor.getCount());
        
        mCursor.close();
        
        // poi records
        
        mProjection[0] = PointsOfInterestContract.Table._ID;
        
        mCursor = mContentResolver.query(PointsOfInterestContract.CONTENT_URI, mProjection, null, null, null);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_pois);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[2] = Integer.toString(mCursor.getCount());
        
        mCursor.close();
        
        // photos
        
        String mSelection = PointsOfInterestContract.Table.PHOTO + " IS NOT NULL";
        
        mCursor = mContentResolver.query(PointsOfInterestContract.CONTENT_URI, mProjection, mSelection, null, null);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_photos);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[3] = Integer.toString(mCursor.getCount());
        
        mCursor.close();
        
        // photos by user
        String[] mSelectionArgs = new String[1];
        ServalMaps mApplication = (ServalMaps) getApplication();
        mSelectionArgs[0] = mApplication.getPhoneNumber();
        mApplication = null;
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_photos_by_you);
        
        if(mSelectionArgs[0] != null) {
        
	        mSelection = PointsOfInterestContract.Table.PHOTO + " = ?";
	        
	        mCursor = mContentResolver.query(PointsOfInterestContract.CONTENT_URI, mProjection, mSelection, mSelectionArgs, null);
	        mTextView.setText(Integer.toString(mCursor.getCount()));
	        
	        dataElems[4] = Integer.toString(mCursor.getCount());
	        
	        mCursor.close();
        } else {
        	mTextView.setText(R.string.misc_not_available);
        	
        	dataElems[4] = getString(R.string.misc_not_available);
        }
        
        // uptime
        SharedPreferences mPreferences = getSharedPreferences(CoreService.PREFERENCES_NAME, Context.MODE_PRIVATE);
        long mUptime = mPreferences.getLong(CoreService.PREFERENCES_VALUE, 0);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_uptime);
        
        if(mUptime > 0) {
        	mTextView.setText(TimeUtils.getMillisHumanReadable(mUptime, this));
        	
        	dataElems[5] = TimeUtils.getMillisHumanReadable(mUptime, this);
        } else {
        	mTextView.setText(String.format(getString(R.string.misc_age_calculation_seconds), 0));
        	dataElems[5] = "0";
        }
        
        // add a click listener to the button
        Button mButton = (Button) findViewById(R.id.stats_ui_btn_send);
        mButton.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.stats_ui_btn_send:
			// the send to serval button has been clicked
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			
			mBuilder.setMessage(R.string.stats_ui_dialog_zip_and_send)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					zipAndSendFiles();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mBuilder.create().show();
			break;
			
		default:
			Log.w(TAG, "unknown view fired an onclick event");
		}
	}
	
	// zip up the files and send them
	private void zipAndSendFiles() {
		
		// disable the button
		
		
		// check and see if a network connection is available
		if(HttpUtils.isOnline(this) == false) {
			// no network connection is available
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			
			mBuilder.setMessage(R.string.stats_ui_dialog_no_network)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mBuilder.create().show();
			
			return;
		}
		
		// undertake the task
		StatsAsyncTask task = new StatsAsyncTask(
				(ProgressBar) findViewById(R.id.stats_ui_progress_bar),
				(TextView) findViewById(R.id.stats_ui_txt_progress), 
				dataElems, 
				dataLabels, 
				this);
		task.execute();
	}
}
