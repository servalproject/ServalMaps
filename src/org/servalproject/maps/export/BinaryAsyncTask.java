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
package org.servalproject.maps.export;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.servalproject.maps.R;
import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.protobuf.LocationMessage;
import org.servalproject.maps.protobuf.PointOfInterestMessage;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * class to undertake an async task to export in binary format
 */
public class BinaryAsyncTask extends AsyncTask<String, Integer, Integer> {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "BinaryExportTask";
	
	/*
	 * private class level variables
	 */
	private ProgressBar progressBar;
	private TextView    progressLabel;
	private Context     context;
	
	public BinaryAsyncTask(Context context, ProgressBar progressBar, TextView progressLabel) {
		
		// check the parameters
		if(context == null || progressBar == null || progressLabel == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		this.context = context;
		this.progressBar = progressBar;
		this.progressLabel = progressLabel;
		
		if(V_LOG) {
			Log.v(TAG, "class instantiated");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		
		if(V_LOG) {
			Log.v(TAG, "onPreExecute called");
		}
		
		progressLabel.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
    protected void onProgressUpdate(Integer... progress) {
		
		if(V_LOG) {
			Log.v(TAG, "onProgressUpdate called: " + progress[0].toString());
		}
 
		// update the progress bar
		super.onProgressUpdate(progress[0]);
		
		progressBar.setProgress(progress[0]);
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
    protected void onPostExecute(Integer result) {
		
		if(V_LOG) {
			Log.v(TAG, "onPostExecute called: ");
		}
		
		// finalse the results
		progressBar.setVisibility(View.INVISIBLE);
		progressBar.setProgress(0);
		progressLabel.setVisibility(View.INVISIBLE);
	}
  
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(String... taskType) {
		
		if(V_LOG) {
			Log.v(TAG, "doInBackground called: " + taskType[0]);
		}
		
		Integer recordCount = -1;
		
		// determine which export task to undertake
		if(taskType[0].equals("All Data") == true) {
			recordCount = doAllExport();
		} else if(taskType[0].equals("All Location Records") == true) {
			recordCount = doLocationExport();
		} else {
			recordCount = doPoiExport();
		}

		return recordCount;
	}
	
	// private method to undertake all exports
	private Integer doAllExport() {
		
		if(V_LOG) {
			Log.v(TAG, "doAllExport called: ");
		}
		
		Integer recordCount = doLocationExport();
		recordCount += doPoiExport();
		
		return recordCount;
	}
	
	// private method to undertake a location export
	private Integer doLocationExport() {
		
		if(V_LOG) {
			Log.v(TAG, "doLocationExport called: ");
		}
		
		// reset the progress bar
		progressBar.setProgress(0);
		
		// get all of the location data
		ContentResolver mContentResolver = context.getApplicationContext().getContentResolver();
		
		// get the content
		Cursor mCursor = mContentResolver.query(
				LocationsContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		// check on what was returned
		if(mCursor.getCount() > 0) {
			
			progressBar.setMax(mCursor.getCount());
			progressLabel.setText(R.string.export_ui_progress_location);
			
			// get the export directory 
			// get the path for the output files
			String mOutputPath = Environment.getExternalStorageDirectory().getPath();
			mOutputPath += context.getString(R.string.system_path_export_data);
			
			if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
				Log.e(TAG, "unable to access the required output directory");
				mCursor.close();
				return -1;
			}
			
			// build the output file name
			String mFileName = "/serval-maps-export-" + TimeUtils.getToday() + BinaryFileContract.LOCATION_EXT;
			
			try {
				OutputStream mOutput = new BufferedOutputStream(new FileOutputStream(mOutputPath + mFileName, false));
				
				org.servalproject.maps.protobuf.LocationMessage.Message.Builder mMessageBuilder = LocationMessage.Message.newBuilder();
				
				while(mCursor.moveToNext()) {
					
					mMessageBuilder.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER)));
					mMessageBuilder.setSubsciberId(mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.SUBSCRIBER_ID)));
					mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)));
					mMessageBuilder.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)));
					mMessageBuilder.setTimestamp(mCursor.getLong(mCursor.getColumnIndex(LocationsContract.Table.TIMESTAMP)));
					mMessageBuilder.setTimeZone(mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.TIMEZONE)));
					
					mMessageBuilder.build().writeDelimitedTo(mOutput);
					
					publishProgress(mCursor.getPosition());
				}
				
				// play nice and tidy up
				mOutput.close();
				mCursor.close();
				
			} catch (FileNotFoundException e) {
				Log.e(TAG, "unable to open the output file", e);
				mCursor.close();
				return -1;
			} catch (IOException e) {
				Log.e(TAG, "unable to write the message at '" + mCursor.getPosition() + "' in the cursor", e);
			}
		}
		
		return -1;
	}
	
	// private method to undetake a POI export
	private Integer doPoiExport() {
		
		// reset the progress bar
		progressBar.setProgress(0);
		
		if(V_LOG) {
			Log.v(TAG, "doPoiExport called: ");
		}
		
		// get all of the location data
		ContentResolver mContentResolver = context.getApplicationContext().getContentResolver();
		
		// get the content
		Cursor mCursor = mContentResolver.query(
				PointsOfInterestContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		// check on what was returned
		if(mCursor.getCount() > 0) {
			
			progressBar.setMax(mCursor.getCount());
			progressLabel.setText(R.string.export_ui_progress_location);
			
			// get the export directory 
			// get the path for the output files
			String mOutputPath = Environment.getExternalStorageDirectory().getPath();
			mOutputPath += context.getString(R.string.system_path_export_data);
			
			if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
				Log.e(TAG, "unable to access the required output directory");
				mCursor.close();
				return -1;
			}
			
			// build the output file name
			String mFileName = "/serval-maps-export-" + TimeUtils.getToday() + BinaryFileContract.POI_EXT;
			
			try {
				OutputStream mOutput = new BufferedOutputStream(new FileOutputStream(mOutputPath + mFileName, false));
				
				org.servalproject.maps.protobuf.PointOfInterestMessage.Message.Builder mMessageBuilder = PointOfInterestMessage.Message.newBuilder();
				
				while(mCursor.moveToNext()) {
					
					mMessageBuilder.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.PHONE_NUMBER)));
					mMessageBuilder.setSubsciberId(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.SUBSCRIBER_ID)));
					mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(PointsOfInterestContract.Table.LATITUDE)));
					mMessageBuilder.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(PointsOfInterestContract.Table.LONGITUDE)));
					mMessageBuilder.setTimestamp(mCursor.getLong(mCursor.getColumnIndex(PointsOfInterestContract.Table.TIMESTAMP)));
					mMessageBuilder.setTimeZone(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TIMEZONE)));
					mMessageBuilder.setTitle(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TITLE)));
					mMessageBuilder.setDescription(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.DESCRIPTION)));
					mMessageBuilder.setCategory(mCursor.getLong(mCursor.getColumnIndex(PointsOfInterestContract.Table.CATEGORY)));
					
					String mPhotoName = mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.PHOTO)); 
					
					// check to see if a photo is associated with this poi
					if(mPhotoName != null) {
						mMessageBuilder.setPhoto(mPhotoName);
					}
					
					mMessageBuilder.build().writeDelimitedTo(mOutput);
					
					publishProgress(mCursor.getPosition());
				}
				
				// play nice and tidy up
				mOutput.close();
				mCursor.close();
				
			} catch (FileNotFoundException e) {
				Log.e(TAG, "unable to open the output file", e);
				mCursor.close();
				return -1;
			} catch (IOException e) {
				Log.e(TAG, "unable to write the message at '" + mCursor.getPosition() + "' in the cursor", e);
			}
		}
		
		return -1;
	}
}
