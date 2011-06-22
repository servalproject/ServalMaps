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
 *  If not, see <http://www.gnu.org/licenses/>
 */
package org.servalproject.mappingservices.net;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.servalproject.mappingservices.content.IncidentProvider;
import org.servalproject.mappingservices.content.LocationProvider;
import org.servalproject.mappingservices.services.CoreMappingService;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * A class used to build incident and location packets
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class PacketBuilder {
	
	/**
	 * the default separator between fields in a packet
	 */
	public static String DEFAULT_FIELD_SEPARATOR = "|";
	
	/**
	 * the default separator between fields as a regex
	 */
	public static String DEFAULT_FIELD_SEPARATOR_REGEX = "\\|";
	
	/**
	 * the default fake signature used in development
	 */
	public static String FAKE_SIGNATURE = "d74361f1d595c1b5bb58d8f0ae831d872248fa842c0ae0cfa4b49ae83aa2351bd74361f1d595c1b5bb58d8f0ae831d872248fa842c0ae0cfa4b49ae83aa2351bd74361f12595c1b5bb58d8f0ae831d872248fa842c0ae0cfa4b49ae83aa2351bd74361f1d595c1b5bb58d8f0ae831d872248fa842c0ae0cfa4b49ae83aa2351b";
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-PB";
	
	/*
	 * private class level variables
	 */
	private Context context;
	private BatmanPeerList peerList;
	
	/**
	 * Constructor for this class
	 * 
	 * @param context a context used to get application resources such as content resolvers
	 */
	public PacketBuilder(Context context, BatmanPeerList peerList) {
		
		if(context == null) {
			throw new IllegalArgumentException("all parameters to this constructor are required");
		}
		
		this.context = context;
		this.peerList = peerList;
	}
	
	/**
	 * build and send an incident packet using fields in the database
	 * 
	 * @param recordId the unique record id to use
	 * @param update a flag to indicate if the record should be updated with the signature
	 * 
	 */
	public void buildAndSendIncident(String recordId, boolean update) throws NetworkException {
		
		// check the parameters
		if(TextUtils.isEmpty(recordId) == true) {
			throw new IllegalArgumentException("the id parameter is required");
		}
		
		if(TextUtils.isDigitsOnly(recordId) == false) {
			throw new IllegalArgumentException("the id parameter must be a valid integer");
		}
		
		// get the details of the incident to send
		ContentResolver mContentResolver = context.getContentResolver();
		Uri mIncidentContentUri = ContentUris.withAppendedId(IncidentProvider.CONTENT_URI, Long.parseLong(recordId));
		Cursor mIncidentDetails = mContentResolver.query(mIncidentContentUri, null, null, null, null);
		
		// check to ensure something was returned
		if(mIncidentDetails.moveToFirst() == false) {
    		// no data was found
	       	 if(V_LOG) {
	       		 Log.v(TAG, "no incident data found with record id: " + recordId);
	       	 }
	       	 
	       	 throw new NetworkException("no incident data found with record id: " + recordId);
		}
		
		// build the packet
		String mPacketContent = null;
		
		if(update == true) {
			mPacketContent = buildIncidentFromCursor(mIncidentDetails, false);
			
			String mSignature = buildSignature(mPacketContent);
			
			mPacketContent = mPacketContent + DEFAULT_FIELD_SEPARATOR + mSignature;
			
			// update the record with the new signature
			ContentValues mValues = new ContentValues();
			mValues.put(IncidentProvider.SIGNATURE_FIELD, mSignature);
			mContentResolver.update(mIncidentContentUri, mValues, null, null);
			
		} else {
			mPacketContent = buildIncidentFromCursor(mIncidentDetails, true);
		}
		
		// play nice and tidy up
		mIncidentDetails.close();
		mIncidentDetails = null;
		
		try {
			String[] mPeers = peerList.getPeerList();
			
			for(int i = 0; i < mPeers.length; i++) {
				PacketSender.sendBroadcast(CoreMappingService.INCIDENT_PORT, mPacketContent, mPeers[i]);
			}
		} catch (UnknownHostException e) {
			throw new NetworkException("unable to send incident packet", e);
		} catch (SocketException e) {
			throw new NetworkException("unable to send incident packet", e);
		} catch (IOException e) {
			throw new NetworkException("unable to send incident packet", e);
		}
	}
	
	/**
	 * build a packet using data in the supplied cursor
	 * 
	 * @param cursor containing a record with incident data
	 * @param withSignature add the signature from the record to the packet
	 * 
	 * @return the packet content
	 */
	public String buildIncidentFromCursor(Cursor cursor, boolean withSignature) {
		
		// check the parameter
		if(cursor == null) {
			throw new IllegalArgumentException("the cursor must be a valid object");
		}
		
		if(cursor.moveToFirst() == false) {
			// no data is available
			throw new IllegalArgumentException("the cursor must contain at least one record");
		}
		
		// start to build the packet
		StringBuilder builder = new StringBuilder();
		
		// add the required fields
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.PHONE_NUMBER_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.SID_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.TITLE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.DESCRIPTION_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.CATEGORY_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.LATITUDE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.LONGITUDE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.TIMESTAMP_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(IncidentProvider.TIMEZONE_FIELD)));
		
		// add the signature field
		if(withSignature == true) {
			builder.append(DEFAULT_FIELD_SEPARATOR + cursor.getString(cursor.getColumnIndex(IncidentProvider.SIGNATURE_FIELD)));
		}
		
		// return without the signature field
		return builder.toString();
	}
	
	/**
	 * build and send a location packet using fields in the database
	 * 
	 * @param recordId the unique record id to use
	 * @param update a flag to indicate if the record should be updated with the signature
	 * 
	 */
	public void buildAndSendLocation(String recordId, boolean update) throws NetworkException {
		
		// check the parameters
		if(TextUtils.isEmpty(recordId) == true) {
			throw new IllegalArgumentException("the id parameter is required");
		}
		
		if(TextUtils.isDigitsOnly(recordId) == false) {
			throw new IllegalArgumentException("the id parameter must be a valid integer");
		}
		
		// get the details of the incident to send
		ContentResolver mContentResolver = context.getContentResolver();
		Uri mLocationContentUri = ContentUris.withAppendedId(LocationProvider.CONTENT_URI, Long.parseLong(recordId));
		Cursor mLocationDetails = mContentResolver.query(mLocationContentUri, null, null, null, null);
		
		// check to ensure something was returned
		if(mLocationDetails.moveToFirst() == false) {
    		// no data was found
	       	 if(V_LOG) {
	       		 Log.v(TAG, "no location data found with record id: " + recordId);
	       	 }
	       	 
	       	 throw new NetworkException("no location data found with record id: " + recordId);
		}
		
		// build the packet
		String mPacketContent = null;
		
		if(update == true) {
			mPacketContent = buildLocationFromCursor(mLocationDetails, false);
			
			String mSignature = buildSignature(mPacketContent);
			
			mPacketContent = mPacketContent + DEFAULT_FIELD_SEPARATOR + mSignature;

			// update the record with the new signature
			ContentValues mValues = new ContentValues();
			mValues.put(LocationProvider.SIGNATURE_FIELD, mSignature);
			mContentResolver.update(mLocationContentUri, mValues, null, null);
			
		} else {
			mPacketContent = buildLocationFromCursor(mLocationDetails, true);
		}
		
		// play nice and tidy up
		mLocationDetails.close();
		mLocationDetails = null;
		
		try {
			String[] mPeers = peerList.getPeerList();
		
			for(int i = 0; i < mPeers.length; i++) {
				PacketSender.sendBroadcast(CoreMappingService.LOCATION_PORT, mPacketContent, mPeers[i]);
			}
		} catch (UnknownHostException e) {
			throw new NetworkException("unable to send incident packet", e);
		} catch (SocketException e) {
			throw new NetworkException("unable to send incident packet", e);
		} catch (IOException e) {
			throw new NetworkException("unable to send incident packet", e);
		}
	}
	
	/**
	 * build a packet using data in the supplied cursor
	 * 
	 * @param cursor containing a record with incident data
	 * @param withSignature add the signature from the record to the packet
	 * 
	 * @return the packet content
	 */
	public String buildLocationFromCursor(Cursor cursor, boolean withSignature) {
		
		// check the parameter
		if(cursor == null) {
			throw new IllegalArgumentException("the cursor must be a valid object");
		}
		
		if(cursor.moveToFirst() == false) {
			// no data is available
			throw new IllegalArgumentException("the cursor must contain at least one record");
		}
		
		// start to build the packet
		StringBuilder builder = new StringBuilder();
		
		// add the required fields
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.TYPE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.SID_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.LATITUDE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.LONGITUDE_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.TIMESTAMP_FIELD)) + DEFAULT_FIELD_SEPARATOR);
		builder.append(cursor.getString(cursor.getColumnIndex(LocationProvider.TIMEZONE_FIELD)));
		
		// add the signature field
		if(withSignature == true) {
			builder.append(DEFAULT_FIELD_SEPARATOR + cursor.getString(cursor.getColumnIndex(LocationProvider.SIGNATURE_FIELD)));
		}
		
		// return without the signature field
		return builder.toString();
	}
	
	/**
	 * public method to build a signature using packet content
	 * 
	 * @param content the content of the packet
	 * 
	 * @return the signature for the packet
	 */
	//TODO update with a valid algorithm once it has been decided upon
	public String buildSignature(String content) {
		return FAKE_SIGNATURE;
	}

}
