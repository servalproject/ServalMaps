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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.servalproject.maps.R;
import org.servalproject.maps.ServalMaps;
import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.protobuf.LocationReadWorker;
import org.servalproject.maps.protobuf.PointsOfInterestWorker;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.MediaUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * receives the broadcasts from Rhizome about new files being available
 */
public class RhizomeBroadcastReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = false;
	private final String TAG = "RhizomeBroadcastReceiver";
	
	/*
	 * private class level variables
	 */
	ExecutorService executor;
	
	/**
	 * constructor a new instance of this receiver providing it an executor service
	 * used to manage the data import threads
	 * 
	 * @param executor used to manage the execution of data import threads
	 * @throws IllegalArgumentException if executor is null
	 */
	public RhizomeBroadcastReceiver(ExecutorService executor) {
		
		if(executor == null) {
			throw new IllegalArgumentException("the executor parameter is required");
		}
		
		this.executor = executor;
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle mBundle = intent.getExtras();
		
		if(intent.getAction().equals("org.servalproject.rhizome.RECIEVE_FILE") == false) {
			Log.e(TAG, "called with an intent with an unexepcted intent action");
			return;
		}
		
		// see if the file is one we want to work with
		String mFilePath = mBundle.getString("path");
		
		if(mFilePath == null) {
			Log.e(TAG, "called with an intent missing the 'path' extra");
		}
		
		// check to see if the file path is to our own file
		ServalMaps mServalMaps = (ServalMaps) context.getApplicationContext();
		String mFileName = new File(mFilePath).getName();
		String[] mFileParts = mFileName.split("-");
		
		String mPhoneNumber = mServalMaps.getPhoneNumber();
		mPhoneNumber = mPhoneNumber.replace(" ", "");
		mPhoneNumber = mPhoneNumber.replace("-", "");
		
		if(mFileParts[0].equals(mServalMaps.getPhoneNumber()) == true) { 
			// this doesn't look like one of our own binary files
			return;
		}
		
		// is it one of our images?
		if(mFileName.startsWith(MediaUtils.PHOTO_FILE_PREFIX) && mFileName.endsWith(".jpg")) {
			// this is a serval maps photo
			try {
				FileUtils.copyFileToDir(mFilePath, MediaUtils.getMediaStore());
				Log.d(TAG, MediaUtils.getMediaStore());
			} catch (IOException e) {
				Log.e(TAG, "unable to copy file", e);
				return;
			}
		}
		
		// get the binary data directory
		String mDataPath = Environment.getExternalStorageDirectory().getPath();
		mDataPath += context.getString(R.string.system_path_binary_data);
		
		if(mFilePath.endsWith(BinaryFileContract.LOCATION_EXT) == true) {
			// this is a binary location file
			
			// copy and process the file
			try {
				
				String mDataFile = FileUtils.copyFileToDirWithTmpName(mFilePath, mDataPath);				
				LocationReadWorker mWorker = new LocationReadWorker(context, mDataFile);
				executor.submit(mWorker);

			} catch (IOException e) {
				Log.e(TAG, "unable to copy file", e);
				return;
			}

		} else if(mFilePath.endsWith(BinaryFileContract.POI_EXT) == true) {
			// this is a binary POI file
			
			// copy and process the file
			try {
				
				String mDataFile = FileUtils.copyFileToDirWithTmpName(mFilePath, mDataPath);
				PointsOfInterestWorker mWorker = new PointsOfInterestWorker(context, mDataFile);
				executor.submit(mWorker);

			} catch (IOException e) {
				Log.e(TAG, "unable to copy file", e);
				return;
			}
		}
		
		if(V_LOG) {
			Log.v(TAG, "received intent with action: " + intent.getAction());
			Log.v(TAG, "file name: " + mBundle.getString("path"));
			Log.v(TAG, "version: " + Long.toString(mBundle.getLong("version")));
			Log.v(TAG, "name: " + mBundle.getString("name"));
		}
	}
}
