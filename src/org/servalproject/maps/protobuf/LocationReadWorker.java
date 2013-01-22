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

import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.MapItems;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
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
	
	private final long sSleepTime = 300;
	
	/*
	 * private class level variables
	 */
	private Context context;
	private Uri dataFile;
	
	/**
	 * construct a new location read worker
	 * 
	 * @param context the context object used to access a content resolver
	 * @param filePath the path to the binary file
	 */
	public LocationReadWorker(Context context, Uri dataFile) {
		
		this.context = context;
		this.dataFile = dataFile;
		
	}

	@Override
	public void run() {
		
		try {
			// try and open the file
			if(V_LOG) {
				Log.v(TAG, "reading location data from: " + dataFile);
			}
			
			ContentResolver mContentResolver = context.getContentResolver();
			
			InputStream mInputStream = mContentResolver.openInputStream(dataFile);
			
			try{
				// prepare helper variables
				LocationMessage.Message mMessage;
				
				long mLatestTimeStamp = -1;
				ArrayList<ContentProviderOperation> operations=new ArrayList<ContentProviderOperation>();
				
				// loop through the data
				while((mMessage = LocationMessage.Message.parseDelimitedFrom(mInputStream)) != null) {
					
					// check to see if we need to get the latest time stamp
					if(mLatestTimeStamp == -1) {
						
						String[] mProjection = {LocationsContract.Table.TIMESTAMP};
						String mSelection = LocationsContract.Table.PHONE_NUMBER + " = ?";
						String[] mSelectionArgs = new String[1];
						mSelectionArgs[0] = mMessage.getPhoneNumber();
						String mOrderBy = LocationsContract.Table.TIMESTAMP + " DESC";
						
						Cursor mCursor = mContentResolver.query(
								LocationsContract.CONTENT_URI,
								mProjection,
								mSelection,
								mSelectionArgs,
								mOrderBy);
						
						if(mCursor.getCount() != 0) {
							mCursor.moveToFirst();
							
							mLatestTimeStamp = mCursor.getLong(mCursor.getColumnIndex(LocationsContract.Table.TIMESTAMP));
						} else {
							mLatestTimeStamp = 0;
						}
						
						mCursor.close();
					}
					
					if(mMessage.getTimestamp() > mLatestTimeStamp) {
						
						// add new record
						ContentValues mNewValues = new ContentValues();
						
						mNewValues.put(LocationsContract.Table.PHONE_NUMBER, mMessage.getPhoneNumber());
						mNewValues.put(LocationsContract.Table.SUBSCRIBER_ID, mMessage.getSubsciberId());
						mNewValues.put(LocationsContract.Table.LATITUDE, mMessage.getLatitude());
						mNewValues.put(LocationsContract.Table.LONGITUDE, mMessage.getLongitude());
						mNewValues.put(LocationsContract.Table.TIMESTAMP, mMessage.getTimestamp());
						mNewValues.put(LocationsContract.Table.TIMEZONE, mMessage.getTimeZone());
						mNewValues.put(LocationsContract.Table.ALTITUDE, mMessage.getAltitude());
						mNewValues.put(LocationsContract.Table.ACCURACY, mMessage.getAccuracy());
						
						ContentProviderOperation o=
							ContentProviderOperation
							.newInsert(LocationsContract.CONTENT_URI)
							.withValues(mNewValues).build();
						operations.add(o);
						
					}
				}
				
				if (operations.size()>0){
					mContentResolver.applyBatch(MapItems.AUTHORITY, operations);
					if(V_LOG)
						Log.v(TAG, "Added "+operations.size()+" new location record(s) to the database");
				}
			}finally{
				mInputStream.close();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

}
