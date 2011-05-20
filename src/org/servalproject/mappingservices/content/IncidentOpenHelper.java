/*
 * This file is part of the Serval Mapping Services app.
 *
 *  Serval Mapping Services app is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  Serval Mapping Services app is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Serval Mapping Services app.  
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package org.servalproject.mappingservices.content;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Manages the database used to store location information
 * 
 * @author corey.wallis@servalproject.org
 */
public class IncidentOpenHelper extends SQLiteOpenHelper implements IncidentColumns {
	
	/*
	 * private class variables
	 */
	@SuppressWarnings("unused")
	//TODO find out if this can be used here for anything or if we can just safely discard it
	private Context context;
	
	/*
	 * private class constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-IOH";
	
	public IncidentOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
		this.context = context;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "class instantiated");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the incident table and related indices
		
		String mSql = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		            + PHONE_NUMBER_FIELD + " text, " + SID_FIELD + " text, " + IP_ADDRESS_FIELD + " text, "
		            + TITLE_FIELD + " text, " + DESCRIPTION_FIELD + " text, " + CATEGORY_FIELD + " text, "
		            + LATITUDE_FIELD + " real, " + LONGITUDE_FIELD + " real, "
		            + TIMESTAMP_FIELD + " int, " + TIMEZONE_FIELD + " text, " + SIGNATURE_FIELD + " text, "
		            + TIMESTAMP_UTC_FIELD + " int)";
		
		// execute the sql
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create table", e);
		}
		
		// build the sql to create the indexes
		mSql = "CREATE INDEX idx_phone_number ON " + TABLE_NAME + " (" + PHONE_NUMBER_FIELD + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create phone_number index", e);
		}
		
		mSql = "CREATE INDEX idx_ip_address ON " + TABLE_NAME + " (" + IP_ADDRESS_FIELD + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create idx_ip_address index", e);
		}
		
		mSql = "CREATE INDEX idx_phone_time ON " + TABLE_NAME + " (" + PHONE_NUMBER_FIELD + ", " + TIMESTAMP_FIELD + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create idx_phone_time index", e);
		}
		
		mSql = "CREATE INDEX idx_timestamp_utc_id ON " + TABLE_NAME + " (" + TIMESTAMP_UTC_FIELD + ", " + _ID + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create idx_timestamp_utc_id index", e);
		}
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "database tables and indexes created");
			Log.v(TAG, "database path:" + db.getPath());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO upgrade tables when necessary

	}

}
