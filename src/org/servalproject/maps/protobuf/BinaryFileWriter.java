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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.servalproject.maps.R;
import org.servalproject.maps.provider.MapItemsContract;
import org.servalproject.maps.rhizome.Rhizome;
import org.servalproject.maps.utils.FileUtils;
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
	//private static final boolean V_LOG = true;
	
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
		
		Uri mContentUri = Uri.parse(MapItemsContract.Locations.CONTENT_URI.toString() + "/" + recordId);
		
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// check on the content
		if(mCursor.getCount() == 0) {
			throw new IllegalArgumentException("the recordId does not match any records");
		}
		
		// build the message
		mCursor.moveToFirst();
		
		org.servalproject.maps.protobuf.LocationMessage.Message.Builder mMessageBuilder = LocationMessage.Message.newBuilder();
		
		// populate the message
		mMessageBuilder.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.PHONE_NUMBER)));
		mMessageBuilder.setSubsciberId(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.SUBSCRIBER_ID)));
		mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LATITUDE)));
		mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LONGITUDE)));
		mMessageBuilder.setTimestamp(mCursor.getLong(mCursor.getColumnIndex(MapItemsContract.Locations.Table.TIMESTAMP)));
		mMessageBuilder.setTimeZone(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.TIMEZONE)));
		
		// determine the file name
		String mFileName = mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.PHONE_NUMBER));
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayAsString() + ".smapl";
		
		// play nice and tidy up
		mCursor.close();
		
		// open the file
		try {
			FileOutputStream mOutput = new FileOutputStream(mOutputPath + mFileName, true);
			mMessageBuilder.build().writeDelimitedTo(mOutput);
			mOutput.close();
			
			// add the file to rhizome
			Rhizome.addFile(context, mOutputPath + mFileName);
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create the output file", e);
			return;
		} catch (IOException e) {
			Log.e(TAG, "unable to write to the output file", e);
			return;
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
		
		Uri mContentUri = Uri.parse(MapItemsContract.PointsOfInterest.CONTENT_URI.toString() + "/" + recordId);
		
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// check on the content
		if(mCursor.getCount() == 0) {
			throw new IllegalArgumentException("the recordId does not match any records");
		}
		
		// build the message
		mCursor.moveToFirst();
		
		org.servalproject.maps.protobuf.PointOfInterestMessage.Message.Builder mMessageBuilder = PointOfInterestMessage.Message.newBuilder();
		
		mMessageBuilder.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.PHONE_NUMBER)));
		mMessageBuilder.setSubsciberId(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.SUBSCRIBER_ID)));
		mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.LATITUDE)));
		mMessageBuilder.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.LONGITUDE)));
		mMessageBuilder.setTimestamp(mCursor.getLong(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.TIMESTAMP)));
		mMessageBuilder.setTimeZone(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.TIMEZONE)));
		mMessageBuilder.setTitle(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.TITLE)));
		mMessageBuilder.setDescription(mCursor.getString(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.DESCRIPTION)));
		mMessageBuilder.setTimestamp(mCursor.getLong(mCursor.getColumnIndex(MapItemsContract.PointsOfInterest.Table.CATEGORY)));
		
		// determine the file name
		String mFileName = mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.PHONE_NUMBER));
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayAsString() + ".smapp";
		
		// play nice and tidy up
		mCursor.close();
		
		// open the file and write the data
		try {
			FileOutputStream mOutput = new FileOutputStream(mOutputPath + mFileName, true);
			mMessageBuilder.build().writeDelimitedTo(mOutput);
			mOutput.close();
			
			// add the file to rhizome
			Rhizome.addFile(context, mOutputPath + mFileName);
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create the output file", e);
			return;
		} catch (IOException e) {
			Log.e(TAG, "unable to write to the output file", e);
			return;
		}
	}

}  