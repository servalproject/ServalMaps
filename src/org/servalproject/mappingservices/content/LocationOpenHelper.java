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
 */
public class LocationOpenHelper extends SQLiteOpenHelper implements LocationColumns{
	
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
	private final String TAG = "ServalMaps-LOH";
	
	public LocationOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
		this.context = context;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "class instantiated");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// build the sql to create the table
		String mSql = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		            + PHONE_NUMBER_FIELD + " text, " + SID_FIELD + " text, " + TYPE_FIELD + " int, " + IP_ADDRESS_FIELD + " text, " + LATITUDE_FIELD + " text, " + LONGITUDE_FIELD + " text, "
		            + TIMESTAMP_FIELD + " int, " + TIMEZONE_FIELD + " text, " + SIGNATURE_FIELD + " text, "+ SELF_FIELD + " text, "
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
			Log.e(TAG, "unable to create phone number index", e);
		}
		
		mSql = "CREATE INDEX idx_ip_address ON " + TABLE_NAME + " (" + IP_ADDRESS_FIELD + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create has field index", e);
		}
		
		mSql = "CREATE INDEX idx_type_ip_time ON " + TABLE_NAME + " (" + TYPE_FIELD + ", " + IP_ADDRESS_FIELD + ", " + TIMESTAMP_FIELD + ")";
		
		try {
			db.execSQL(mSql);
		} catch (SQLException e) {
			Log.e(TAG, "unable to create type_ip_time index", e);
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
	
	/**
	 * List of all field names as as an array
	 * 
	 * @return an array containing a list of field names
	 */
	public String[] getFieldList() {
		
		String[] mFieldList = new String[12];
		
		mFieldList[0] = _ID;
		mFieldList[1] = PHONE_NUMBER_FIELD;
		mFieldList[2] = SID_FIELD;
		mFieldList[3] = TYPE_FIELD;
		mFieldList[4] = IP_ADDRESS_FIELD;
		mFieldList[5] = LATITUDE_FIELD;
		mFieldList[6] = LONGITUDE_FIELD;
		mFieldList[7] = TIMESTAMP_FIELD;
		mFieldList[8] = TIMEZONE_FIELD;
		mFieldList[9] = SIGNATURE_FIELD;
		mFieldList[10] = SELF_FIELD;
		mFieldList[11] = TIMESTAMP_UTC_FIELD;

		return mFieldList;
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO upgrade tables when necessary

	}

}
