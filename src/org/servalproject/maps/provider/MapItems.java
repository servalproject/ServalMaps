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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	private final int LOCATION_LATEST_LIST_URI = 2;
	
	private final int POI_LIST_URI = 10;
	private final int POI_ITEM_URI = 11;
	
	private final int TAG_LIST_URI = 20;
	private final int TAG_ITEM_URI = 21;
	private final int TAG_UNIQUE_LIST_URI = 22;
	
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
		
		uriMatcher.addURI(MapItems.AUTHORITY, TagsContract.CONTENT_URI_PATH, TAG_LIST_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, TagsContract.CONTENT_URI_PATH + "/#", TAG_ITEM_URI);
		uriMatcher.addURI(MapItems.AUTHORITY, TagsContract.CONTENT_URI_PATH + "/unique", TAG_UNIQUE_LIST_URI);
		
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
		case TAG_LIST_URI:
			//uri matches all of the table
			if(TextUtils.isEmpty(sortOrder) == true) {
				sortOrder = TagsContract.Table.TAG + " ASC";
			}
			mMatchedUri = TAG_LIST_URI;
			break;
		case TAG_ITEM_URI:
			// uri matches one record
			if(TextUtils.isEmpty(selection) == true) {
				selection = TagsContract.Table._ID + " = " + uri.getLastPathSegment();
			} else {
				selection += " AND " + TagsContract.Table._ID + " = " + uri.getLastPathSegment();
			}
			mMatchedUri = TAG_ITEM_URI;
			break;
		case TAG_UNIQUE_LIST_URI:
			// uri matches a request for the unique list
			mMatchedUri = TAG_UNIQUE_LIST_URI;
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
			
			mResults = database.query(
					LocationsContract.Table.TABLE_NAME, 
					mColumns, 
					null, 
					null, 
					LocationsContract.Table.PHONE_NUMBER, 
					null, 
					null);
			
		} else if (mMatchedUri == LOCATION_LIST_URI || mMatchedUri == LOCATION_ITEM_URI){
			// execute the query as provided
			mResults = database.query(LocationsContract.Table.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		} else if(mMatchedUri == POI_LIST_URI || mMatchedUri == POI_ITEM_URI) {
			// execute the query as provided
			mResults = database.query(PointsOfInterestContract.Table.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		} else if(mMatchedUri == TAG_LIST_URI || mMatchedUri == TAG_ITEM_URI) {
			mResults = database.query(TagsContract.Table.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		} else if(mMatchedUri == TAG_UNIQUE_LIST_URI) {
			
			String[] mColumns = new String[3];
			mColumns[0] = "MAX( " + TagsContract.Table._ID + ") AS " + TagsContract.Table._ID;
			mColumns[1] = TagsContract.Table.TAG;
			mColumns[2] = "COUNT(" + TagsContract.Table.TAG + ") AS " + TagsContract.Table._COUNT;
			
			if(sortOrder == null) {
				sortOrder = TagsContract.Table.TAG;
			}
			
			mResults = database.query(
					TagsContract.Table.TABLE_NAME, 
					mColumns,
					selection,
					selectionArgs,
					TagsContract.Table.TAG,
					null,
					sortOrder);
		}
				
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
		
		// check to see if we need to update the tag index
		if(uriMatcher.match(uri) == POI_LIST_URI && values.containsKey(PointsOfInterestContract.Table.TAGS)) {
			updateTagIndexOnInsert(mId, values.getAsString(PointsOfInterestContract.Table.TAGS));
		}
		
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
			
			if(selection == null && selectionArgs == null) {
				updateTagIndexOnDelete(null);
			} else {
				Log.w(TAG, "POI records were deleted with an unsupported selection criteria, the tag index may now be out of sync");
			}
			break;
		case POI_ITEM_URI:
			if(TextUtils.isEmpty(selection) == true) {
				selection = PointsOfInterestContract.Table._ID + " = ?";
				selectionArgs = new String[0];
				selectionArgs[0] = uri.getLastPathSegment();
			}
			count = database.delete(PointsOfInterestContract.Table.TABLE_NAME, selection, selectionArgs);
			updateTagIndexOnDelete(selectionArgs[0]);
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
	
	/*
	 * private method to update the tag index on an insert
	 */
	private void updateTagIndexOnInsert(long id, String tagList) {
		//TODO execute this on a separate thread so that the poi inserts return sooner
		
		// check to make sure there are tags to process
		if(TextUtils.isEmpty(tagList) == true) {
			return;
		}
		
		// get a list of individual tags
		String[] mTags = tagList.split(TagsContract.TAG_DELIMITER);
		
		// remove duplicate tags
		if(mTags.length > 1) {
			Set<String> mTempSet = new HashSet<String>(Arrays.asList(mTags));
			mTags = new String[mTempSet.size()];
			mTags = mTempSet.toArray(mTags);
		}
		
		// get a connection to the database
		SQLiteDatabase mDatabase = databaseHelper.getWritableDatabase();
		
		ContentValues mValues;
		
		// write the tags to the database
		for(String mTag: mTags) {
			mValues = new ContentValues();
			
			mValues.put(TagsContract.Table.POI_RECORD_ID, id);
			mValues.put(TagsContract.Table.TAG, mTag);
			
			mDatabase.insertOrThrow(TagsContract.Table.TABLE_NAME, null, mValues);
		}
		
		// play nice and tidy up
		mDatabase.close();
	}
	
	/*
	 * private method to update the tag index on a delete
	 */
	private void updateTagIndexOnDelete(String id) {
		
		//TODO execute on a new thread so deletes return faster
		
		// build the selection
		String mSelection = null;
		String[] mSelectionArgs = null;
		
		if(id != null) {
			mSelection = TagsContract.Table.POI_RECORD_ID + " = ? ";
			mSelectionArgs = new String[]{id};
		}
		
		SQLiteDatabase mDatabase = databaseHelper.getWritableDatabase();
		
		mDatabase.delete(PointsOfInterestContract.Table.TABLE_NAME, mSelection, mSelectionArgs);
		
		// play nice and tidy up
		mDatabase.close();
	}
}
