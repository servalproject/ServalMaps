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
import java.util.ArrayList;

import org.servalproject.maps.provider.MapItems;
import org.servalproject.maps.provider.PointsOfInterestContract;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PointsOfInterestWorker implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "PointsOfInterestWorker";
	private final boolean V_LOG = true;
	
	/*
	 * private class level variables
	 */
	private Context context;
	private Uri dataFile;
	
	/**
	 * construct a new location read worker
	 * 
	 * @param context the context object used to access a content resolver
	 * @param uri the path to the binary file
	 */
	public PointsOfInterestWorker(Context context, Uri dataFile) {
		this.context = context;
		this.dataFile = dataFile;
	}

	@Override
	public void run() {
		try{
			// try and open the file
			if(V_LOG) {
				Log.v(TAG, "reading POI data from: " + dataFile);
			}
			
			ContentResolver mContentResolver = context.getContentResolver();
			
			InputStream mInputStream = mContentResolver.openInputStream(dataFile);
			
			try{
				// prepare helper variables
				PointOfInterestMessage.Message mMessage;
				
				long mLatestTimeStamp = -1;
				ArrayList<ContentProviderOperation> operations=new ArrayList<ContentProviderOperation>();
				
				// loop through the data
				while((mMessage = PointOfInterestMessage.Message.parseDelimitedFrom(mInputStream)) != null) {
					
					// check to see if we need to get the latest time stamp
					if(mLatestTimeStamp == -1) {
						
						String[] mProjection = {PointsOfInterestContract.Table.TIMESTAMP};
						String mSelection = PointsOfInterestContract.Table.SRC_FILE + " = ?";
						String[] mSelectionArgs = new String[]{dataFile.getLastPathSegment()};
						String mOrderBy = PointsOfInterestContract.Table.TIMESTAMP + " DESC";
						
						Cursor mCursor = mContentResolver.query(
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
					}
					
					if(mMessage.getTimestamp() > mLatestTimeStamp) {
						
						// add new record
						ContentValues mNewValues = new ContentValues();
						
						mNewValues.put(PointsOfInterestContract.Table.PHONE_NUMBER, mMessage.getPhoneNumber());
						mNewValues.put(PointsOfInterestContract.Table.SUBSCRIBER_ID, mMessage.getSubsciberId());
						mNewValues.put(PointsOfInterestContract.Table.LATITUDE, mMessage.getLatitude());
						mNewValues.put(PointsOfInterestContract.Table.LONGITUDE, mMessage.getLongitude());
						mNewValues.put(PointsOfInterestContract.Table.TIMESTAMP, mMessage.getTimestamp());
						mNewValues.put(PointsOfInterestContract.Table.TIMEZONE, mMessage.getTimeZone());
						mNewValues.put(PointsOfInterestContract.Table.TITLE, mMessage.getTitle());
						mNewValues.put(PointsOfInterestContract.Table.DESCRIPTION, mMessage.getDescription());
						mNewValues.put(PointsOfInterestContract.Table.PHOTO, mMessage.getPhoto());
						mNewValues.put(PointsOfInterestContract.Table.TAGS, mMessage.getTags());
						mNewValues.put(PointsOfInterestContract.Table.ACCURACY, mMessage.getAccuracy());
						mNewValues.put(PointsOfInterestContract.Table.ALTITUDE, mMessage.getAltitude());
						mNewValues.put(PointsOfInterestContract.Table.SRC_FILE, dataFile.getLastPathSegment());
						
						ContentProviderOperation o=
							ContentProviderOperation
							.newInsert(PointsOfInterestContract.CONTENT_URI)
							.withValues(mNewValues).build();
						operations.add(o);
					}
				}
				
				if (operations.size()>0){
					mContentResolver.applyBatch(MapItems.AUTHORITY, operations);
					if(V_LOG)
						Log.v(TAG, "Added "+operations.size()+" new POI record(s) to the database");
				}
			}finally{
				mInputStream.close();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
