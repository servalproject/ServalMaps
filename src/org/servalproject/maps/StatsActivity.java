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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.services.CoreService;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.HttpUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	private String[] dataElems = new String[5];
	
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
        
        TextView mTextView = (TextView) findViewById(R.id.stats_ui_txt_locations);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[0] = Integer.toString(mCursor.getCount());
        
        mCursor.close();
        
        // poi records
        
        mProjection[0] = PointsOfInterestContract.Table._ID;
        
        mCursor = mContentResolver.query(PointsOfInterestContract.CONTENT_URI, mProjection, null, null, null);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_pois);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[1] = Integer.toString(mCursor.getCount());
        
        mCursor.close();
        
        // photos
        
        String mSelection = PointsOfInterestContract.Table.PHOTO + " IS NOT NULL";
        
        mCursor = mContentResolver.query(PointsOfInterestContract.CONTENT_URI, mProjection, mSelection, null, null);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_photos);
        mTextView.setText(Integer.toString(mCursor.getCount()));
        
        dataElems[2] = Integer.toString(mCursor.getCount());
        
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
	        
	        dataElems[3] = Integer.toString(mCursor.getCount());
	        
	        mCursor.close();
        } else {
        	mTextView.setText(R.string.misc_not_available);
        	
        	dataElems[3] = getString(R.string.misc_not_available);
        }
        
        // uptime
        SharedPreferences mPreferences = getSharedPreferences(CoreService.PREFERENCES_NAME, Context.MODE_PRIVATE);
        long mUptime = mPreferences.getLong(CoreService.PREFERENCES_VALUE, 0);
        
        mTextView = (TextView) findViewById(R.id.stats_ui_txt_uptime);
        
        if(mUptime > 0) {
        	mTextView.setText(TimeUtils.getMillisHumanReadable(mUptime, this));
        	
        	dataElems[4] = TimeUtils.getMillisHumanReadable(mUptime, this);
        } else {
        	mTextView.setText(String.format(getString(R.string.misc_age_calculation_seconds), 0));
        	dataElems[4] = "0";
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
		
		File mZipFile;
		
		// create the zip file
		try {
			
			File[] mFiles = new File[2];
			
			mFiles[0] = createStatsFile();
			
			mFiles[1] = createPrefsFile();
			
			mZipFile = createZipFile(mFiles);
			
			// copy the zip file to the export directory for transparency
			String mExportPath = Environment.getExternalStorageDirectory().getPath();
			mExportPath += getString(R.string.system_path_export_data);
			
			FileUtils.copyFileToDir(mZipFile.getCanonicalPath(), mExportPath);
			
		} catch(IOException e) {
			
			Log.e(TAG, "unable to create zip file", e);
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			
			mBuilder.setMessage(R.string.stats_ui_dialog_zip_fail)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mBuilder.create().show();
			
			return;
		}
		
		// upload the zip file
		
		try {
			String mResponse = HttpUtils.doHttpUpload(mZipFile, getString(R.string.system_url_file_upload));
			
			if(mResponse.contains("error") == false) {
				
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
				
				String mMessage = String.format(getString(R.string.stats_ui_dialog_upload_success), getString(R.string.system_path_export_data));
				
				mBuilder.setMessage(mMessage)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				mBuilder.create().show();
				
			}
		}catch (IOException e) {
			
			Log.e(TAG, "unable to upload the zip file", e);
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			
			mBuilder.setMessage(R.string.stats_ui_dialog_upload_fail)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mBuilder.create().show();
			
			return;
			
		}
		
		
	}
	
	private File createStatsFile() throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/stats.txt");
		
		PrintWriter mPrinter = new PrintWriter(new FileWriter(mOutputFile, false));
		
		mPrinter.println("Data Collected: " + TimeUtils.getTodayWithTime());
		
		mPrinter.println("Location records: " + dataElems[0]);
		mPrinter.println("POI records: " + dataElems[1]);
		mPrinter.println("Total photos: " + dataElems[2]);
		mPrinter.println("Photos taken by you: " + dataElems[3]);
		mPrinter.println("Core service uptime: " + dataElems[4]);
		
		mPrinter.close();
		
		Log.d(TAG, mOutputFile.getCanonicalPath());

		// return the file handle
		return mOutputFile;		
	}
	
	private File createPrefsFile() throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/prefs.txt");
		
		// get a handle on the important shared preferences
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Map<String, ?> mPrefMap = mPreferences.getAll();
		
		// open the file
		PrintWriter mPrinter = new PrintWriter(new FileWriter(mOutputFile, false));
		
		for (Map.Entry<String, ?> mEntry: mPrefMap.entrySet()) {
			mPrinter.println(mEntry.getKey() + ":" + mEntry.getValue().toString());
		}
		
		mPrinter.close();
		
		Log.d(TAG, mOutputFile.getCanonicalPath());

		// return the file handle
		return mOutputFile;	
		
	}

	private File createZipFile(File[] fileList) throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/statistics.zip");
		ZipOutputStream mOutputStream = new ZipOutputStream(new FileOutputStream(mOutputFile));
		
		for(File mFile : fileList) {
			// add the file to the zip file
			addFileToZip(mOutputStream, mFile);
		}
		
		mOutputStream.close();
		
		return mOutputFile;
	}
	
	private void addFileToZip(ZipOutputStream outputStream, File inputFile) throws IOException {
		
		int mLength;
		byte[] mBuffer = new byte[1024];
		
		// add the file
		outputStream.putNextEntry(new ZipEntry(inputFile.getName()));
		
		FileInputStream mInputStream = new FileInputStream(inputFile);
		
		 while((mLength = mInputStream.read(mBuffer)) > 0)
         {
			 outputStream.write(mBuffer, 0, mLength);
         }
		 
		 outputStream.closeEntry();
		 mInputStream.close();
	}
}
