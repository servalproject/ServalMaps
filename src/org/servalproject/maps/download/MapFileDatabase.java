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
package org.servalproject.maps.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * manage a database of all of the map files in a mirror
 */
public class MapFileDatabase extends SQLiteOpenHelper {
	
	/*
	 * private class level constants
	 */
	private static final String DB_NAME = "map-data-files.db";
	private static final int DB_VERSION = 1;
	
	// database tables
	private static final String MAP_FILES_TABLE = "CREATE TABLE "
			+ MapFileTableContract.TABLE_NAME + " ("
			+ MapFileTableContract._ID + " INTEGER PRIMARY KEY, "
			+ MapFileTableContract.NAME + " TEXT, "
			+ MapFileTableContract.SIZE + " INTEGER, "
			+ MapFileTableContract.TIMESTAMP + " INTEGER, "
			+ MapFileTableContract.MIN_LATITUDE + " REAL, "
			+ MapFileTableContract.MIN_LONGITUDE + " REAL, "
			+ MapFileTableContract.MAX_LATITUDE + " REAL, "
			+ MapFileTableContract.MAX_LONGITUDE + " REAL)";
	
	private static final String MAP_FILES_INDEX = "CREATE INDEX map_files_name_idx ON "
			+ MapFileTableContract.TABLE_NAME + " ("
			+ MapFileTableContract.NAME + " ASC)";
	
	/**
	 * Constructs a new MapFileDatabase object
	 * 
	 * @param context the context in which the database should be constructed
	 */
	public MapFileDatabase(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the database tables and indexes
		db.execSQL(MAP_FILES_TABLE);
		db.execSQL(MAP_FILES_INDEX);
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
