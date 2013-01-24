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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MainDatabaseHelper extends SQLiteOpenHelper {
	
	// declare private class constants
	private final String LOCATIONS_CREATE = "CREATE TABLE " + 
			LocationsContract.CONTENT_URI_PATH + " ("
			+ LocationsContract.Table._ID + " INTEGER PRIMARY KEY, "
			+ LocationsContract.Table.PHONE_NUMBER + " TEXT, "
			+ LocationsContract.Table.SUBSCRIBER_ID + " TEXT, "
			+ LocationsContract.Table.LATITUDE + " REAL, "
			+ LocationsContract.Table.LONGITUDE + " REAL, "
			+ LocationsContract.Table.ALTITUDE + " REAL, "
			+ LocationsContract.Table.ACCURACY + " REAL, "
			+ LocationsContract.Table.TIMESTAMP + " INTEGER, "
			+ LocationsContract.Table.TIMEZONE + " TEXT, "
			+ LocationsContract.Table.SRC_FILE + " TEXT)";
	
	private final String POI_CREATE = "CREATE TABLE " +
			PointsOfInterestContract.CONTENT_URI_PATH + " ("
			+ PointsOfInterestContract.Table._ID +" INTEGER PRIMARY KEY, "
			+ PointsOfInterestContract.Table.PHONE_NUMBER + " TEXT, "
			+ PointsOfInterestContract.Table.SUBSCRIBER_ID + " TEXT, "
			+ PointsOfInterestContract.Table.LATITUDE + " REAL, "
			+ PointsOfInterestContract.Table.LONGITUDE + " REAL, "
			+ PointsOfInterestContract.Table.ALTITUDE + " REAL, "
			+ PointsOfInterestContract.Table.ACCURACY + " REAL, "
			+ PointsOfInterestContract.Table.TIMESTAMP + " INTEGER, "
			+ PointsOfInterestContract.Table.TIMEZONE + " TEXT, "
			+ PointsOfInterestContract.Table.TITLE + " TEXT, "
			+ PointsOfInterestContract.Table.DESCRIPTION + " TEXT, "
			+ PointsOfInterestContract.Table.PHOTO + " TEXT, "
			+ PointsOfInterestContract.Table.TAGS + " TEXT, "
			+ PointsOfInterestContract.Table.SRC_FILE + " TEXT)";
	
	private final String LOCATIONS_INDEX = "CREATE INDEX locations_timestamp_desc ON "
			+ LocationsContract.CONTENT_URI_PATH + " ("
			+ LocationsContract.Table.PHONE_NUMBER + " ASC, "
			+ LocationsContract.Table.TIMESTAMP + " DESC)";
	
	private final String POI_INDEX = "CREATE INDEX poi_file_timestamp_desc ON "
			+ PointsOfInterestContract.CONTENT_URI_PATH + " ("
			+ PointsOfInterestContract.Table.SRC_FILE + " ASC, "
			+ PointsOfInterestContract.Table.TIMESTAMP + " DESC)";
	
	private final String TAGS_CREATE = "CREATE TABLE " +
			TagsContract.Table.TABLE_NAME + " ("
			+ TagsContract.Table._ID + " INTEGER PRIMARY KEY, "
			+ TagsContract.Table.POI_RECORD_ID + " INTEGER, "
			+ TagsContract.Table.TAG + " TEXT)";
	
	private final String TAGS_INDEX_1 = "CREATE INDEX tag_poi ON "
			+ TagsContract.Table.TABLE_NAME + " ("
			+ TagsContract.Table.POI_RECORD_ID + " ASC)";
	
	private final String TAGS_INDEX_2 = "CREATE INDEX tag_tag_poi ON "
			+ TagsContract.Table.TABLE_NAME + " ("
			+ TagsContract.Table.TAG + " ASC, "
			+ TagsContract.Table.POI_RECORD_ID + " ASC)";
	
	// declare public class constants
	public static final String DB_NAME = "serval-maps.db";
	public static final int DB_VERSION = 1;
	
	/**
	 * Constructs a new MainDatabaseHelper object
	 * 
	 * @param context the context in which the database should be constructed
	 */
	MainDatabaseHelper(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);
	}
		
	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the database tables
		db.execSQL(LOCATIONS_CREATE);
		db.execSQL(POI_CREATE);
		
		db.execSQL(LOCATIONS_INDEX);
		db.execSQL(POI_INDEX);
		
		db.execSQL(TAGS_CREATE);
		db.execSQL(TAGS_INDEX_1);
		db.execSQL(TAGS_INDEX_2);
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO add onUpgrade code if DB tables change once multiple release version are in the wild
	}
}
