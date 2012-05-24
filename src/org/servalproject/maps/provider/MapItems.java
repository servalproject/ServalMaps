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
package org.servalproject.maps.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * A content provider that provides access to the map item data
 */
public class MapItems extends ContentProvider {
	
	/**
	 * authority string for the content provider
	 */
	public static final String AUTHORITY = "org.servalproject.maps.provider.items";
	
	// private class level constants
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	private final int LOCATION_LIST_URI = 0;
	private final int LOCATION_ITEM_URI = 1;
	private final int LOCATION_LATEST_LIST_URI = 3;
	
	private final int POI_LIST_URI = 4;
	private final int POI_ITEM_URI = 5;
	
	private final String TAG = "MapItems";
	//private final boolean V_LOG = true;
	
	// private class level variables
	private MainDatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	
	/*
	 * undertake initialisation tasks
	 * 
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		
		// define URis that we'll match against
		//uriMatcher.addURI(MapItemsContract.Locations.CONTENT_URI, LOCATION_DIR_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, LocationsContract.CONTENT_URI_PATH, LOCATION_LIST_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, LocationsContract.CONTENT_URI_PATH + "/#", LOCATION_ITEM_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, LocationsContract.CONTENT_URI_PATH + "/latest", LOCATION_LATEST_LIST_URI);
		
		uriMatcher.addURI(MapItems.AUTHORITY, PointsOfInterestContract.CONTENT_URI_PATH, POI_LIST_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, PointsOfInterestContract.CONTENT_URI_PATH + "/#", POI_ITEM_URI);
		
		// create the database connection
		databaseHelper = new MainDatabaseHelper(getContext());
		
		return true;
	}
	
	/*
	 * execute a query
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		int mMatchedUri = -1;
		Cursor mResults = null;
		
		// choose the table name and sort order based on the URI
		switch(uriMatcher.match(uri)) {
		case LOCATION_LIST_URI:
			// uri matches all of the table
			if(TextUtils.isEmpty(sortOrder) == true) {
				sortOrder = LocationsContract.Table._ID + " ASC";
			}
			mMatchedUri = LOCATION_LIST_URI;
			break;
		case LOCATION_ITEM_URI:
			// uri matches one record
			if(TextUtils.isEmpty(selection) == true) {
				selection = LocationsContract.Table._ID + " = " + uri.getLastPathSegment();
			} else {
				selection += " AND " + LocationsContract.Table._ID + " = " + uri.getLastPathSegment();
			}
			mMatchedUri = LOCATION_ITEM_URI;
			break;
		case LOCATION_LATEST_LIST_URI:
			// uri matches the group by for latest records
			mMatchedUri = LOCATION_LATEST_LIST_URI;
			break;
		case POI_LIST_URI:
			// uri matches all of the table
			if(TextUtils.isEmpty(sortOrder) == true) {
				sortOrder = PointsOfInterestContract.Table._ID + " ASC";
			}
			mMatchedUri = POI_LIST_URI;
			break;
		case POI_ITEM_URI:
			// uri matches one record
			if(TextUtils.isEmpty(selection) == true) {
				selection = PointsOfInterestContract.Table._ID + " = " + uri.getLastPathSegment();
			} else {
				selection += " AND " + PointsOfInterestContract.Table._ID + " = " + uri.getLastPathSegment();
			}
			mMatchedUri = POI_ITEM_URI;
			break;
		default:
			// unknown uri found
			Log.e(TAG, "unknown URI detected on query: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getReadableDatabase();
		
		if(mMatchedUri == LOCATION_LATEST_LIST_URI) {
			// get the latest location records
			
			String[] mColumns = new String[6];
			mColumns[0] = LocationsContract.Table._ID;
			mColumns[1] = LocationsContract.Table.PHONE_NUMBER;
			mColumns[2] = LocationsContract.Table.LATITUDE;
			mColumns[3] = LocationsContract.Table.LONGITUDE;
			mColumns[4] = LocationsContract.Table.TIMESTAMP;
			mColumns[5] = "MAX(" + LocationsContract.Table.TIMESTAMP + ")";
			
			mResults = database.query(LocationsContract.Table.TABLE_NAME, mColumns, null, null, LocationsContract.Table.PHONE_NUMBER, null, null);
			
		} else if (mMatchedUri == LOCATION_LIST_URI || mMatchedUri == LOCATION_ITEM_URI){
			// execute the query as provided
			mResults = database.query(LocationsContract.CONTENT_URI_PATH, projection, selection, selectionArgs, null, null, sortOrder);
		} else if(mMatchedUri == POI_LIST_URI || mMatchedUri == POI_ITEM_URI) {
			// execute the query as provided
			mResults = database.query(PointsOfInterestContract.CONTENT_URI_PATH, projection, selection, selectionArgs, null, null, sortOrder);
		}
		
		// play nice and tidy up
		//database.close();
				
		// return the results
		return mResults;
	}
	
	/*
	 * insert data into the database
	 * 
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		
		Uri mResults = null;
		String mTable = null;
		Uri mContentUri = null;
		
		// chose the table name
		switch(uriMatcher.match(uri)) {
		case LOCATION_LIST_URI:
			mTable = LocationsContract.CONTENT_URI_PATH;
			mContentUri = LocationsContract.CONTENT_URI;
			break;
		case POI_LIST_URI:
			mTable = PointsOfInterestContract.CONTENT_URI_PATH;
			mContentUri = PointsOfInterestContract.CONTENT_URI;
			break;
		default:
			// unknown uri found
			Log.e(TAG, "invalid URI detected for insert: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		
		long mId = database.insertOrThrow(mTable, null, values);
		
		// play nice and tidy up
		database.close();
		
		mResults = ContentUris.withAppendedId(mContentUri, mId);
		getContext().getContentResolver().notifyChange(mResults, null);
		
		return mResults;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public synchronized String getType(Uri uri) {
		
		// choose the mime type
		switch(uriMatcher.match(uri)) {
		case LOCATION_LIST_URI:
			return LocationsContract.CONTENT_TYPE_LIST;
		case LOCATION_ITEM_URI:
			return LocationsContract.CONTENT_TYPE_ITEM;
		case LOCATION_LATEST_LIST_URI:
			return LocationsContract.CONTENT_TYPE_LIST;
		case POI_LIST_URI:
			return PointsOfInterestContract.CONTENT_TYPE_LIST;
		case POI_ITEM_URI:
			return PointsOfInterestContract.CONTENT_TYPE_ITEM;
		default:
			// unknown uri found
			Log.e(TAG, "unknown URI detected on getType: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		int count;
		
		// determine what type of delete is required
		switch(uriMatcher.match(uri)) {
		case LOCATION_LIST_URI:
			count = database.delete(LocationsContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		case LOCATION_ITEM_URI:
			if(TextUtils.isEmpty(selection) == true) {
				selection = LocationsContract.Table._ID + " = ?";
				selectionArgs = new String[0];
				selectionArgs[0] = uri.getLastPathSegment();
			}
			count = database.delete(LocationsContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		case POI_LIST_URI:
			count = database.delete(PointsOfInterestContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		case POI_ITEM_URI:
			if(TextUtils.isEmpty(selection) == true) {
				selection = PointsOfInterestContract.Table._ID + " = ?";
				selectionArgs = new String[0];
				selectionArgs[0] = uri.getLastPathSegment();
			}
			count = database.delete(PointsOfInterestContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			// unknown uri found
			Log.e(TAG, "unknown URI detected on query: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public synchronized int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		//TODO implement code when required
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
