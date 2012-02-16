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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.servalproject.maps.provider.MapItemsContract;
import org.servalproject.maps.utils.HashUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

/**
 * undertakes all of the necessary work to 
 * read locations messages 
 * from the binary file and write the messages to the database
 */
public class LocationReadWorker implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "LocationReadWorker";
	private final boolean V_LOG = true;
	
	/*
	 * private class level variables
	 */
	Context context;
	String filePath;
	
	/**
	 * construct a new location read worker
	 * 
	 * @param context the context object used to access a content resolver
	 * @param filePath the path to the binary file
	 */
	public LocationReadWorker(Context context, String filePath) {
		
		// check the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		if(TextUtils.isEmpty(filePath) == true) {
			throw new IllegalArgumentException("the filePath parameter is required");
		}
		
		this.context = context;
		this.filePath = filePath;
		
	}

	@Override
	public void run() {
		
		// try and open the file
		FileInputStream mInput = null;
		try {
			mInput = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to open file: " + filePath, e);
			return;
		}
		
		// prepare helper variables
		ContentResolver mContentResolver = context.getContentResolver();
		ContentValues mNewValues = null;
		Cursor mCursor = null;
		org.servalproject.maps.protobuf.LocationMessage.Message.Builder mMessageBuilder = LocationMessage.Message.newBuilder();
		String mHash = null;
		
		String[] mProjection = {MapItemsContract.Locations.Table._ID};
		String mSelection = MapItemsContract.Locations.Table.HASH + " = ?";
		String[] mSelectionArgs = new String[1];
		
		// loop through the data
		try {
			while(mMessageBuilder.mergeDelimitedFrom(mInput) == true) {
			
				// build the hash of the message
				mHash = HashUtils.hashLocationMessage(
						mMessageBuilder.getPhoneNumber(), 
						mMessageBuilder.getLatitude(),
						mMessageBuilder.getLongitude(),
						mMessageBuilder.getTimestamp());
				
				mSelectionArgs[0] = mHash;
				
				mCursor = mContentResolver.query(
						MapItemsContract.Locations.CONTENT_URI,
						mProjection,
						mSelection,
						mSelectionArgs,
						null);
				
				// check to see what was returned and add record as required
				if(mCursor.getCount() == 0) {
					
					// play nice and tidy up
					mCursor.close();
					mCursor = null;
					
					// add new record
					mNewValues = new ContentValues();
					
					mNewValues.put(MapItemsContract.Locations.Table.PHONE_NUMBER, mMessageBuilder.getPhoneNumber());
					mNewValues.put(MapItemsContract.Locations.Table.SUBSCRIBER_ID, mMessageBuilder.getSubsciberId());
					mNewValues.put(MapItemsContract.Locations.Table.LATITUDE, mMessageBuilder.getLatitude());
					mNewValues.put(MapItemsContract.Locations.Table.LONGITUDE, mMessageBuilder.getLongitude());
					mNewValues.put(MapItemsContract.Locations.Table.TIMESTAMP, mMessageBuilder.getTimestamp());
					mNewValues.put(MapItemsContract.Locations.Table.TIMEZONE, mMessageBuilder.getTimeZone());
					mNewValues.put(MapItemsContract.Locations.Table.HASH, mHash);
					
					mContentResolver.insert(
							MapItemsContract.Locations.CONTENT_URI,
							mNewValues);
					
					if(V_LOG) {
						Log.v(TAG, "added new location record to the database");
					}
				} else {
					if(V_LOG) {
						Log.v(TAG, "skipped an existing location record");
					}
				}
				
				// play nice and tidy up
				if(mCursor != null) {
					mCursor.close();
					mCursor = null;
				}
				
				mNewValues = null;
			}
		} catch (IOException e) {
			Log.e(TAG, "unable to read from the input file", e);
			return;
		} finally {
			try {
				mInput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "unable to close input file", e);
			}
		}
	}

}
