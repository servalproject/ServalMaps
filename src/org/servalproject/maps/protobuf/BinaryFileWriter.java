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
package org.servalproject.maps.protobuf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.servalproject.maps.R;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.rhizome.Rhizome;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.MediaUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * write a google protocol buffer based binary file 
 * containing location or point of interest information
 */
public class BinaryFileWriter {
	
	/*
	 * private class level constants
	 */
	private static final String TAG = "BinaryFileWriter";
	//private static final boolean V_LOG = false;
	
	/**
	 * write a location message to the file
	 * 
	 * @param context a context object used to get a content resolver object
	 * @param recordId the unique record identifier for the location record
	 * 
	 * @throws IllegalArgumentException if the context parameter is null
	 * @throws IllegalArgumentException if a record cannot be found
	 */
	public static void writeLocation(Context context, String recordId) {
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// get the path for the output files
		String mOutputPath = Environment.getExternalStorageDirectory().getPath();
		mOutputPath += context.getString(R.string.system_path_binary_data);
		
		// test the path
		if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
			Log.e(TAG, "unable to access the required output directory");
			return;
		}
		
		// get the record
		ContentResolver mContentResolver = context.getContentResolver();
		
		Uri mContentUri = Uri.parse(LocationsContract.CONTENT_URI.toString() + "/" + recordId);
		
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// check on the content
		if(mCursor.getCount() == 0) {
			Log.e(TAG, "the supplied recordId does not match any records");
			return;
		}
		
		// build the message
		mCursor.moveToFirst();
		
		// determine the file name
		String mFileName = mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER));
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayWithHour() + BinaryFileContract.LOCATION_EXT;
		
		FileOutputStream mOutput = null;

		try {
			mOutput = new FileOutputStream(mOutputPath + mFileName, true);
			
			BinaryFileContract.writeLocationRecord(mCursor, mOutput);
			
			// add the file to rhizome
			Rhizome.addFile(context, mOutputPath + mFileName);
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create the output file", e);
		} catch (IOException e) {
			Log.e(TAG, "unable to write to the output file", e);
		} finally {
			// play nice and tidy up
			try {
				if(mOutput != null) {
					mOutput.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "unable to close the output file", e);
			}
			mCursor.close();
		}
	}
	
	/**
	 * write a POI message to the file
	 * 
	 * @param context a context object used to get a content resolver object
	 * @param recordId the unique record identifier for the location record
	 * 
	 * @throws IllegalArgumentException if the context parameter is null
	 * @throws IllegalArgumentException if a record cannot be found
	 */
	public static void writePointOfInterest(Context context, String recordId) {
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// get the path for the output files
		String mOutputPath = Environment.getExternalStorageDirectory().getPath();
		mOutputPath += context.getString(R.string.system_path_binary_data);
		
		// test the path
		if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
			Log.e(TAG, "unable to access the required output directory");
			return;
		}
		
		// get the record
		ContentResolver mContentResolver = context.getContentResolver();
		
		Uri mContentUri = Uri.parse(PointsOfInterestContract.CONTENT_URI.toString() + "/" + recordId);
		
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// check on the content
		if(mCursor.getCount() == 0) {
			Log.e(TAG, "unable to access the required output directory");
			return;
		}
		
		// build the message
		mCursor.moveToFirst();
		
		String mPhotoName = mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.PHOTO)); 
		
		// check to see if a photo is associated with this poi
		if(mPhotoName != null) {
			
			// add the image to Rhizome
			Rhizome.addFile(context, MediaUtils.getMediaStore() + File.separator + mPhotoName);
		}
		
		// determine the file name
		String mFileName = mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER));
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayWithHour() + BinaryFileContract.POI_EXT;
		
		// open the file and write the data
		FileOutputStream mOutput = null;
		try {
			mOutput = new FileOutputStream(mOutputPath + mFileName, true);
			
			BinaryFileContract.writeLocationRecord(mCursor , mOutput);
			
			// add the file to rhizome
			Rhizome.addFile(context, mOutputPath + mFileName);
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create the output file", e);
		} catch (IOException e) {
			Log.e(TAG, "unable to write to the output file", e);
		} finally {
			// play nice and tidy up
			try {
				if(mOutput != null) {
					mOutput.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "unable to close the output file", e);
			}
			mCursor.close();
		}
	}

}  