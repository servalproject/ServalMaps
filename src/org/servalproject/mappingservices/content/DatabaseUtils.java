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

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * a class used to rerieve information about the databases used by the 
 * application
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class DatabaseUtils {
	
	/**
	 * count the number of records that match the provided record type
	 * 
	 * @param recordType the record type, as defined in the RecordTypes class
	 * @param context the context with which to open the database
	 * 
	 * @return the number of records matching the specific record type
	 * 
	 * @throws IllegalArgumentException if the record type is not valid
	 * @throws IllegalArgumentException if the context is null
	 */
	public static int getRecordCount(int recordType, Context context) {
		
		// check the parameter
		if(RecordTypes.isValidType(recordType) == false ) {
			throw new IllegalArgumentException("the supplied record type was invalid");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter cannot be null");
		}
		
		int mRecordCount = -1;
		
		if(recordType == RecordTypes.INCIDENT_RECORD_TYPE) {
			
			IncidentOpenHelper mHelper = new IncidentOpenHelper(context);
			SQLiteDatabase mDatabase = mHelper.getReadableDatabase();
			
			String mSql = "SELECT COUNT(" + IncidentOpenHelper._ID + ") FROM " + IncidentOpenHelper.TABLE_NAME;
			
			Cursor mCursor = mDatabase.rawQuery(mSql, null);
			mCursor.moveToFirst();
			
			mRecordCount = mCursor.getInt(0);
			
			mCursor.close();
			mDatabase.close();
			mHelper.close();
			
		} else if(recordType == RecordTypes.LOCATION_RECORD_TYPE) {
			
			LocationOpenHelper mHelper = new LocationOpenHelper(context);
			SQLiteDatabase mDatabase = mHelper.getReadableDatabase();
			
			String mSql = "SELECT COUNT(" + LocationOpenHelper._ID + ") FROM " + LocationOpenHelper.TABLE_NAME;
			
			Cursor mCursor = mDatabase.rawQuery(mSql, null);
			mCursor.moveToFirst();
			
			mRecordCount = mCursor.getInt(0);
			
			mCursor.close();
			mDatabase.close();
			mHelper.close();
		}
		
		return mRecordCount;
	}
	
	/**
	 * convert a timestamp field from the supplied timezone to UTC
	 * 
	 * @param timestamp the original timestamp (in seconds)
	 * @param timezone  the timezone id
	 * 
	 * @return the timestamp (in seconds) according to UTC
	 * 
	 * @throws IllegalArgumentException if the timestamp field isn't recognised as a long
	 * @throws IllegalArgumentException if the timezone field is empty
	 */
	public static String getTimestampAsUtc(String timestamp, String timezone) {
		
		// check the parameters
		long mTime = -1;
		try {
			mTime = Long.parseLong(timestamp);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("the timestamp must be a valid long", e);
		}
		
		if(TextUtils.isEmpty(timezone) == true) {
			throw new IllegalArgumentException("the timezone field is required");
		}
		
		Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
		mCalendar.setTimeInMillis((mTime * 1000));
		
		Calendar mUtcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		mUtcCalendar.setTimeInMillis(mCalendar.getTimeInMillis());
		
		mTime = mUtcCalendar.getTimeInMillis(); 
		mTime = mTime / 1000;
		
		return Long.toString(mTime);
	}
	
	/**
	 * 
	 * get the current time in seconds as UTC
	 * 
	 * @return the current time, as determined by the device, in UTC
	 * 
	 */
	public static long getCurrentTimeAsUtc() {
		
		Calendar mDeviceCal = Calendar.getInstance();
    	long mDeviceTimeAsLong = mDeviceCal.getTimeInMillis();
    	mDeviceTimeAsLong = mDeviceTimeAsLong / 1000;
    	mDeviceTimeAsLong = Long.parseLong(DatabaseUtils.getTimestampAsUtc(Long.toString(mDeviceTimeAsLong), mDeviceCal.getTimeZone().getID()));
    	
    	return mDeviceTimeAsLong;
		
	}
}