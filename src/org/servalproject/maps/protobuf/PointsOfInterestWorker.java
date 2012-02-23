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

import org.servalproject.maps.protobuf.PointOfInterestMessage.Message.Builder;
import org.servalproject.maps.provider.MapItemsContract;
import org.servalproject.maps.utils.HashUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class PointsOfInterestWorker implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "PointsOfInterestWorker";
	private final boolean V_LOG = true;
	
	private final long sleepTime = 300;
	
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
	public PointsOfInterestWorker(Context context, String filePath) {
		
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
		Builder mMessageBuilder = PointOfInterestMessage.Message.newBuilder();
		
		String mHash = null;
		
		long mLatestTimeStamp = -1;
		
		// loop through the data
		try {
			while(mMessageBuilder.mergeDelimitedFrom(mInput) == true) {
				
				// check to see if we need to get the latest time stamp
				if(mLatestTimeStamp == -1) {
					
					String[] mProjection = {MapItemsContract.PointsOfInterest.Table.TIMESTAMP};
					String mSelection = MapItemsContract.PointsOfInterest.Table.PHONE_NUMBER + " = ?";
					String[] mSelectionArgs = new String[1];
					mSelectionArgs[0] = mMessageBuilder.getPhoneNumber();
					String mOrderBy = MapItemsContract.PointsOfInterest.Table.TIMESTAMP + " DESC";
					
					mCursor = mContentResolver.query(
							MapItemsContract.Locations.CONTENT_URI,
							mProjection,
							mSelection,
							mSelectionArgs,
							mOrderBy);
					
					if(mCursor.getCount() != 0) {
						mCursor.moveToFirst();
						
						mLatestTimeStamp = mCursor.getLong(mCursor.getColumnIndex(MapItemsContract.Locations.Table.TIMESTAMP));
						
					} else {
						mLatestTimeStamp = 0;
					}
					
					mCursor.close();
					mCursor = null;
				}
				
				if(mMessageBuilder.getTimestamp() > mLatestTimeStamp) {
				
					// compute the hash
					mHash = HashUtils.hashPointOfInterestMessage(
							mMessageBuilder.getPhoneNumber(),
							mMessageBuilder.getLatitude(),
							mMessageBuilder.getLongitude(),
							mMessageBuilder.getTitle(),
							mMessageBuilder.getDescription());
					
					// add new record
					mNewValues = new ContentValues();
					
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.PHONE_NUMBER, mMessageBuilder.getPhoneNumber());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.SUBSCRIBER_ID, mMessageBuilder.getSubsciberId());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.LATITUDE, mMessageBuilder.getLatitude());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.LONGITUDE, mMessageBuilder.getLongitude());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.TIMESTAMP, mMessageBuilder.getTimestamp());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.TIMEZONE, mMessageBuilder.getTimeZone());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.TITLE, mMessageBuilder.getTitle());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.DESCRIPTION, mMessageBuilder.getDescription());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.CATEGORY, mMessageBuilder.getCategory());
					mNewValues.put(MapItemsContract.PointsOfInterest.Table.HASH, mHash);
					
					try {
						mContentResolver.insert(
								MapItemsContract.PointsOfInterest.CONTENT_URI,
								mNewValues);
					} catch (SQLiteException e) {
						Log.e(TAG, "an error occurred while inserting data", e);
						break;
					}
					
					if(V_LOG) {
						Log.v(TAG, "added new POI record to the database");
					}
				} else {
					if(V_LOG) {
						Log.v(TAG, "skipped an existing POI record");
					}
				}
				
				mNewValues = null;
				
				// don't hit the database so hard with writes to sleep for a bit
				try {
					Thread.sleep(sleepTime);
				}catch (InterruptedException e) {
					Log.w(TAG, "thread was interrupted unexepectantly");
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "unable to read from the input file", e);
			return;
		} catch (SQLiteException e) {
			Log.e(TAG, "an error occurred while interfacing with the database", e);
			return;	
		}finally {
			try {
				mInput.close();
			} catch (IOException e) {
				Log.e(TAG, "unable to close input file", e);
			}
		}
	}
}
