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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.servalproject.mappingservices.net.PacketBuilder;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * a class used to rerieve information about the databases used by the 
 * application
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class DatabaseUtils {
	
	/*
	 * private class level constants
	 */
	
	//private static final boolean V_LOG = true;
	private static final String TAG = "ServalMaps-DU";
	
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
	 * get the current time in seconds as UTC, useful to compare with the UTC time retrieved from the database
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
	
	/**
	 * empty the database tables of the specified database
	 * 
	 * @param recordType the record type, as defined in the RecordTypes class
	 * @param context the context with which to open the database
	 * 
	 * @return true if the database was emptied, false if an error occurred
	 * 
	 * @throws IllegalArgumentException if the record type is not valid
	 * @throws IllegalArgumentException if the context is null
	 * 
	 */
	public static boolean emptyDatabase(int recordType, Context context) {
		
		// check the parameter
		if(RecordTypes.isValidType(recordType) == false ) {
			throw new IllegalArgumentException("the supplied record type was invalid");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter cannot be null");
		}
		
		if(recordType == RecordTypes.INCIDENT_RECORD_TYPE) {
			
			IncidentOpenHelper mHelper = new IncidentOpenHelper(context);
			SQLiteDatabase mDatabase = mHelper.getWritableDatabase();
			
			try {
				mDatabase.delete(IncidentOpenHelper.TABLE_NAME, null, null);
			} catch (SQLException e) {
				Log.e(TAG, "deletion of incident data failed", e);
				return false;
			}

			mDatabase.close();
			mHelper.close();
			
		} else if(recordType == RecordTypes.LOCATION_RECORD_TYPE) {
			
			LocationOpenHelper mHelper = new LocationOpenHelper(context);
			SQLiteDatabase mDatabase = mHelper.getWritableDatabase();
			
			try {
				mDatabase.delete(LocationOpenHelper.TABLE_NAME, null, null);
			} catch (SQLException e) {
				Log.e(TAG, "deletion of location data failed", e);
				return false;
			}

			mDatabase.close();
			mHelper.close();
		}
		
		return true;
	}
	
	/**
	 * export the specified database into the specified file
	 * 
	 * @param recordType the record type, as defined in the RecordTypes class
	 * @param context the context with which to open the database
	 * @param exportPath the path to the export file
	 * 
	 * @return true if the database was empty, false if an error occurred
	 * 
	 * @throws IllegalArgumentException if the record type is not valid
	 * @throws IllegalArgumentException if the context is null
	 * @throws IllegalArgumentException if the exportPath is null
	 * @throws IOException              if something bad happens during an IO opperation
	 * 
	 */
	public static void exportDatabase(int recordType, Context context, String exportPath) throws IOException {
		
		// check the parameter
		if(RecordTypes.isValidType(recordType) == false ) {
			throw new IllegalArgumentException("the supplied record type was invalid");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter cannot be null");
		}
		
		if(TextUtils.isEmpty(exportPath)) {
			throw new IllegalArgumentException("the exportPath parameter cannot be null");
		}
		
		// check to see if the supplied export path is available
		File mExportFile = new File(exportPath);
		
		if(mExportFile.exists() == false) {
			// try to create the directory
			if(mExportFile.mkdirs() == false) {
				throw new IOException("unable to create the required export path");
			}
		} else if(mExportFile.isDirectory() == false) {
			throw new IOException("the specified export path already exists and is not a directory");
		} else if(mExportFile.canWrite() == false) {
			throw new IOException("the specified export path is not writeable");
		}
		
		SQLiteDatabase   mDatabase;
		String[]         mFieldList;
		PrintWriter      mPrintWriter = null;
		
		StringBuilder mRecord = null;
		
		try {
			mPrintWriter = openExportFile(recordType, exportPath);
		} catch (IOException ex) {
			// TODO update this throw when we're on API level 9
			//throw new IOException("unable to open the export file", ex);
			throw new IOException("unable to open the export file");
		}
		
		if(recordType == RecordTypes.INCIDENT_RECORD_TYPE) {
			
			IncidentOpenHelper mHelper = new IncidentOpenHelper(context);
			mFieldList = mHelper.getFieldList();
			mDatabase = mHelper.getReadableDatabase();
			
			// output the file header
			writeFileHeader(mPrintWriter, recordType, mFieldList);
			
			Cursor mCursor = mDatabase.query(IncidentOpenHelper.TABLE_NAME, null, null, null, null, null, IncidentOpenHelper._ID);
			
			while(mCursor.moveToNext()) {
				
				mRecord = new StringBuilder();
				
				for(int i = 0; i < mFieldList.length; i++) {
					
					mRecord.append(mCursor.getString(mCursor.getColumnIndex(mFieldList[i])));
					mRecord.append(PacketBuilder.DEFAULT_FIELD_SEPARATOR);
				}
				
				mRecord.deleteCharAt(mRecord.length() -1);
				
				mPrintWriter.println(mRecord.toString());
			}
			
			// play nice and tidy up
			mRecord = null;
			mCursor.close();
			mDatabase.close();
			mHelper.close();
			mPrintWriter.close();
			
		} else if(recordType == RecordTypes.LOCATION_RECORD_TYPE) {
			
			LocationOpenHelper mHelper = new LocationOpenHelper(context);
			mFieldList = mHelper.getFieldList();
			mDatabase = mHelper.getReadableDatabase();
			
			// output the file header
			writeFileHeader(mPrintWriter, recordType, mFieldList);

			Cursor mCursor = mDatabase.query(LocationOpenHelper.TABLE_NAME, null, null, null, null, null, LocationOpenHelper._ID);
			
			while(mCursor.moveToNext()) {
				
				mRecord = new StringBuilder();
				
				for(int i = 0; i < mFieldList.length; i++) {
					
					mRecord.append(mCursor.getString(mCursor.getColumnIndex(mFieldList[i])));
					mRecord.append(PacketBuilder.DEFAULT_FIELD_SEPARATOR);
				}
				
				mRecord.deleteCharAt(mRecord.length() -1);
				
				mPrintWriter.println(mRecord.toString());
			}
			
			// play nice and tidy up
			mRecord = null;
			mCursor.close();
			mDatabase.close();
			mHelper.close();
			mPrintWriter.close();
		}
		
	}
	
	private static PrintWriter openExportFile(int recordType, String exportPath) throws IOException {
		
		PrintWriter mPrinter;
		
		String mFileName = null;
		
		if(recordType == RecordTypes.INCIDENT_RECORD_TYPE) {
			mFileName = "incidents-";
		} else if(recordType == RecordTypes.LOCATION_RECORD_TYPE) {
			mFileName = "locations-";
		}
		
		mFileName = mFileName + Long.toString(getCurrentTimeAsUtc()) + ".txt";
		
		mPrinter = new PrintWriter(new File(exportPath + mFileName));
		
		return mPrinter;
	}
	
	private static void writeFileHeader(PrintWriter writer, int recordType, String[] fieldList) {
		
		writer.println("# export of data from the ServalMappingService commenced: " + getCurrentDateAndTime());
		writer.print("# data contained in this file: ");
		
		if(recordType == RecordTypes.INCIDENT_RECORD_TYPE) {
			writer.print("incidents\n");
		} else if(recordType == RecordTypes.LOCATION_RECORD_TYPE) {
			writer.print("locations\n");
		}
		
		writer.print("# ");
		
		for(int i = 0; i < fieldList.length; i++) {
			writer.print(fieldList[i]);
			
			if(i < fieldList.length -1) {
				writer.print(PacketBuilder.DEFAULT_FIELD_SEPARATOR);
			}
		}
	}
	
	private static String getCurrentDateAndTime() {

		GregorianCalendar mCalendar = new GregorianCalendar();
		DateFormat mFormatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

		return mFormatter.format(mCalendar.getTime());
	}
}