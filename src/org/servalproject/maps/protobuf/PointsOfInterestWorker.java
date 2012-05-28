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

import java.io.InputStream;

import org.servalproject.maps.provider.PointsOfInterestContract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class PointsOfInterestWorker implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "PointsOfInterestWorker";
	private final boolean V_LOG = false;
	
	private final long sleepTime = 300;
	
	/*
	 * private class level variables
	 */
	Context context;
	Uri uri;
	
	/**
	 * construct a new location read worker
	 * 
	 * @param context the context object used to access a content resolver
	 * @param uri the path to the binary file
	 */
	public PointsOfInterestWorker(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
	}

	@Override
	public void run() {
		try{
			// try and open the file
			if(V_LOG) {
				Log.v(TAG, "reading POI data from: " + uri);
			}
			
			ContentResolver mContentResolver = context.getContentResolver();
			InputStream mInputStream = mContentResolver.openInputStream(uri);
			try{
				// prepare helper variables
				ContentValues mNewValues = null;
				Cursor mCursor = null;
				PointOfInterestMessage.Message mMessage;
				
				long mLatestTimeStamp = -1;
				
				// loop through the data
				while((mMessage = PointOfInterestMessage.Message.parseDelimitedFrom(mInputStream)) != null) {
					
					// check to see if we need to get the latest time stamp
					if(mLatestTimeStamp == -1) {
						
						String[] mProjection = {PointsOfInterestContract.Table.TIMESTAMP};
						String mSelection = PointsOfInterestContract.Table.PHONE_NUMBER + " = ?";
						String[] mSelectionArgs = new String[1];
						mSelectionArgs[0] = mMessage.getPhoneNumber();
						String mOrderBy = PointsOfInterestContract.Table.TIMESTAMP + " DESC";
						
						mCursor = mContentResolver.query(
								PointsOfInterestContract.CONTENT_URI,
								mProjection,
								mSelection,
								mSelectionArgs,
								mOrderBy);
						
						if(mCursor.getCount() != 0) {
							mCursor.moveToFirst();
							
							mLatestTimeStamp = mCursor.getLong(mCursor.getColumnIndex(PointsOfInterestContract.Table.TIMESTAMP));
							
						} else {
							mLatestTimeStamp = 0;
						}
						
						mCursor.close();
						mCursor = null;
					}
					
					if(mMessage.getTimestamp() > mLatestTimeStamp) {
						
						// add new record
						mNewValues = new ContentValues();
						
						mNewValues.put(PointsOfInterestContract.Table.PHONE_NUMBER, mMessage.getPhoneNumber());
						mNewValues.put(PointsOfInterestContract.Table.SUBSCRIBER_ID, mMessage.getSubsciberId());
						mNewValues.put(PointsOfInterestContract.Table.LATITUDE, mMessage.getLatitude());
						mNewValues.put(PointsOfInterestContract.Table.LONGITUDE, mMessage.getLongitude());
						mNewValues.put(PointsOfInterestContract.Table.TIMESTAMP, mMessage.getTimestamp());
						mNewValues.put(PointsOfInterestContract.Table.TIMEZONE, mMessage.getTimeZone());
						mNewValues.put(PointsOfInterestContract.Table.TITLE, mMessage.getTitle());
						mNewValues.put(PointsOfInterestContract.Table.DESCRIPTION, mMessage.getDescription());
						mNewValues.put(PointsOfInterestContract.Table.CATEGORY, mMessage.getCategory());
						mNewValues.put(PointsOfInterestContract.Table.PHOTO, mMessage.getPhoto());
						
						try {
							mContentResolver.insert(
									PointsOfInterestContract.CONTENT_URI,
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
						
						// don't hit the CPU so hard so sleep for a bit
						try {
							Thread.sleep(sleepTime);
						}catch (InterruptedException e) {
							Log.w(TAG, "thread was interrupted unexepectantly");
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
			}finally{
				mInputStream.close();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
