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
			+ LocationsContract.Table.TIMESTAMP + " INTEGER, "
			+ LocationsContract.Table.TIMEZONE + " TEXT)";
	
	private final String POI_CREATE = "CREATE TABLE " +
			PointsOfInterestContract.CONTENT_URI_PATH + " ("
			+ PointsOfInterestContract.Table._ID +" INTEGER PRIMARY KEY, "
			+ PointsOfInterestContract.Table.PHONE_NUMBER + " TEXT, "
			+ PointsOfInterestContract.Table.SUBSCRIBER_ID + " TEXT, "
			+ PointsOfInterestContract.Table.LATITUDE + " REAL, "
			+ PointsOfInterestContract.Table.LONGITUDE + " REAL, "
			+ PointsOfInterestContract.Table.TIMESTAMP + " INTEGER, "
			+ PointsOfInterestContract.Table.TIMEZONE + " TEXT, "
			+ PointsOfInterestContract.Table.TITLE + " TEXT, "
			+ PointsOfInterestContract.Table.DESCRIPTION + " TEXT, "
			+ PointsOfInterestContract.Table.CATEGORY + " INTEGER DEFAULT " + PointsOfInterestContract.DEFAULT_CATEGORY + ")";
	
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
			
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the database tables
		db.execSQL(LOCATIONS_CREATE);
		db.execSQL(POI_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

}
