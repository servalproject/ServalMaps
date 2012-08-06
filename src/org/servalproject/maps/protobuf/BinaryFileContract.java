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

import java.io.IOException;
import java.io.OutputStream;

import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;

import android.database.Cursor;

/**
 * declare various constants related to the processing of binary files
 */
public class BinaryFileContract {
	
	/**
	 * the file extension for the location binary file
	 */
	public static final String LOCATION_EXT = ".smapl";
	
	/**
	 * the file extension for the POI binary file
	 */
	public static final String POI_EXT = ".smapp";
	
	/**
	 * an array containing the list of binary file extensions
	 */
	public static final String[] EXTENSIONS = {LOCATION_EXT, POI_EXT};
	
	/**
	 * write a binary location record to an output stream
	 * 
	 * @param cursor the cursor containing the record to write
	 * @param output the output stream
	 * @throws IOException if the write operation fails
	 */
	public static void writeLocationRecord(Cursor cursor, OutputStream output) throws IOException {
		
		org.servalproject.maps.protobuf.LocationMessage.Message.Builder mMessageBuilder = LocationMessage.Message.newBuilder();
		
		// populate the message
		mMessageBuilder.setPhoneNumber(cursor.getString(cursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER)));
		mMessageBuilder.setSubsciberId(cursor.getString(cursor.getColumnIndex(LocationsContract.Table.SUBSCRIBER_ID)));
		mMessageBuilder.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationsContract.Table.LATITUDE)));
		mMessageBuilder.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationsContract.Table.LONGITUDE)));
		mMessageBuilder.setAltitude(cursor.getDouble(cursor.getColumnIndex(LocationsContract.Table.ALTITUDE)));
		mMessageBuilder.setAccuracy(cursor.getDouble(cursor.getColumnIndex(LocationsContract.Table.ACCURACY)));
		mMessageBuilder.setTimestamp(cursor.getLong(cursor.getColumnIndex(LocationsContract.Table.TIMESTAMP)));
		mMessageBuilder.setTimeZone(cursor.getString(cursor.getColumnIndex(LocationsContract.Table.TIMEZONE)));
		
		// write the message
		mMessageBuilder.build().writeDelimitedTo(output);
		
		// play nice and tidy up
		mMessageBuilder = null;
	}
	
	/**
	 * write a binary point of interest record to an output stream
	 * 
	 * @param cursor the cursor containing the record to write
	 * @param output the output stream
	 * @throws IOException if the write operation fails
	 */
	public static void writePointOfInterestRecord(Cursor cursor, OutputStream output) throws IOException {
		
		org.servalproject.maps.protobuf.PointOfInterestMessage.Message.Builder mMessageBuilder = PointOfInterestMessage.Message.newBuilder();
		
		// populate the message
		mMessageBuilder.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.PHONE_NUMBER)));
		mMessageBuilder.setSubsciberId(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.SUBSCRIBER_ID)));
		mMessageBuilder.setLatitude(cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.LATITUDE)));
		mMessageBuilder.setLongitude(cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.LONGITUDE)));
		mMessageBuilder.setTimestamp(cursor.getLong(cursor.getColumnIndex(PointsOfInterestContract.Table.TIMESTAMP)));
		mMessageBuilder.setTimeZone(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.TIMEZONE)));
		mMessageBuilder.setTitle(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.TITLE)));
		mMessageBuilder.setDescription(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.DESCRIPTION)));
		mMessageBuilder.setAccuracy(cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.ACCURACY)));
		mMessageBuilder.setAltitude(cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.ALTITUDE)));
		mMessageBuilder.setTags(cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.TAGS)));
		
		// check to see if this POI has a photo associated with it
		String mPhotoName = cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.PHOTO)); 
		
		// check to see if a photo is associated with this poi
		if(mPhotoName != null) {
			mMessageBuilder.setPhoto(mPhotoName);
		}
		
		// write the message
		mMessageBuilder.build().writeDelimitedTo(output);
	}
}
