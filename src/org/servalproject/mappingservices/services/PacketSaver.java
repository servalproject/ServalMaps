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

package org.servalproject.mappingservices.services;

import java.net.DatagramPacket;
import java.util.concurrent.LinkedBlockingQueue;

import org.servalproject.mappingservices.content.IncidentProvider;
import org.servalproject.mappingservices.content.LocationProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

/**
 * Save the data carried by the packets into the databases for further processing
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class PacketSaver implements Runnable {
	
	/*
	 * private class level variables
	 */
	private LinkedBlockingQueue<DatagramPacket> packetQueue = null;
	private int locationPort;
	private int incidentPort;
	private volatile boolean keepGoing = true;
	
	private ContentResolver contentResolver;
	private Uri locationContentUri;
	private Uri incidentContentUri;
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-PS";
	
	/**
	 * constructor for this class
	 * 
	 * @param locationPort the port that location packets arrive on
	 * @param incidentPort the port that incident packets arrive on
	 * @param packetQueue  a queue containing DatagramPackets
	 * @param locationDatabase a valid writable SQLiteDatabase object
	 */
	public PacketSaver(Integer locationPort, Integer incidentPort, LinkedBlockingQueue<DatagramPacket> packetQueue, ContentResolver contentResolver) {
		
		// validate the parameters
		if(locationPort == null || incidentPort == null || packetQueue == null || contentResolver == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		if(locationPort < PacketCollector.MIN_PORT || locationPort > PacketCollector.MAX_PORT) {
			throw new IllegalArgumentException("locationPort parameter must be between: " + PacketCollector.MIN_PORT + " and " + PacketCollector.MAX_PORT);
		}
		
		if(incidentPort < PacketCollector.MIN_PORT || incidentPort > PacketCollector.MAX_PORT) {
			throw new IllegalArgumentException("incidentPort parameter must be between: " + PacketCollector.MIN_PORT + " and " + PacketCollector.MAX_PORT);
		}
		
		// store these for later
		this.locationPort     = locationPort;
		this.incidentPort     = incidentPort;
		this.packetQueue      = packetQueue;
		
		// get uris for the content providers
		locationContentUri = LocationProvider.CONTENT_URI;
		incidentContentUri = IncidentProvider.CONTENT_URI;
		this.contentResolver = contentResolver;
	}

	/*
	 * When invoked run for as long as possible and save the data from packets into the databases
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		// declare local variables
		DatagramPacket mPacket = null;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet saver started");
		}
		
		// loop until requested to stop
		while(keepGoing == true) {
			
			// get a new packet from the queue
			try {
				mPacket = packetQueue.take();
				
				// determine which method to use to save the data
				if(mPacket.getPort() == locationPort) {
					// this is a location data packet
					saveLocation(mPacket);
				} else if(mPacket.getPort() == incidentPort) {
					// this is an incident data packet
					saveIncident(mPacket);
				} else {
					// this is an unexpected packet
					Log.e(TAG, "unexpected packet detected in the queue: " + mPacket.getPort());
				}
			} catch (InterruptedException e) {
				// thread was interrupted while waiting for a packet to process
				if(V_LOG) {
					Log.v(TAG, "thread was interrupted", e);
				}
			}
			
		}
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet saver stopped");
		}

	}
	
	// private method to save location data
	private void saveLocation(DatagramPacket packet) {
		
		// get the content of the packet
		String mContent = new String(packet.getData());
		mContent = mContent.trim();
		
		// get the fields from the packet
		String[] mFields = mContent.split("\\|");
		
		//validate the packet according to business rules
		try {
			PacketValidator.isValidLocation(mFields);
		} catch (ValidationException e) {
			if(V_LOG) {
				Log.v(TAG, "packet didn't pass validation", e);
			}
			
			// exit the method early as the validation failed
			return;
		}
		
		// packet passed validation so continue
		// declare other helper variables
		Cursor mCursor          = null;
		String[] mColumns       = null;
		String   mSelection     = null;
		String[] mSelectionArgs = null;
		
		// check to make sure we haven't saved this packet already as duplicates are expected
		
		// columns to return
		mColumns = new String[1];
		mColumns[0] = LocationProvider._ID;
		
		// where statement
		mSelection = LocationProvider.TYPE_FIELD + " = ? AND " + LocationProvider.IP_ADDRESS_FIELD + " = ? AND " + LocationProvider.TIMESTAMP_FIELD + " = ?";
		
		// values to match against
		mSelectionArgs = new String[3];
		mSelectionArgs[0] = mFields[0];
		mSelectionArgs[1] = packet.getAddress().getHostAddress();
		mSelectionArgs[2] = mFields[3];
		
		// execute the query
		mCursor = contentResolver.query(locationContentUri, mColumns, mSelection, mSelectionArgs, null);
	
		if(mCursor.getCount() == 0) {
			
			// values weren't found so we can store this new packet
			ContentValues mValues = new ContentValues();
			mValues.put(LocationProvider.TYPE_FIELD, mFields[0]);
			mValues.put(LocationProvider.LATITUDE_FIELD, mFields[1]);
			mValues.put(LocationProvider.LONGITUDE_FIELD, mFields[2]);
			mValues.put(LocationProvider.TIMESTAMP_FIELD, mFields[3]);
			mValues.put(LocationProvider.TIMEZONE_FIELD, mFields[4]);
			mValues.put(LocationProvider.IP_ADDRESS_FIELD, packet.getAddress().getHostAddress());
			
			// add the row
			try {
				contentResolver.insert(locationContentUri, mValues);
			} catch (SQLException e) {
				Log.e(TAG, "unable to save new location data", e);
			}
			
			//status message
			if(V_LOG) {
				Log.v(TAG, "new location data saved to database");
			}
		} else {
			if(V_LOG) {
				Log.v(TAG, "duplicate location data detected");
			}
		}
		
		// play nice and tidy up
		mCursor.close();
	}
	
	// private method to save incident data
	private void saveIncident(DatagramPacket packet) {
		
		// get the content of the packet
		String mContent = new String(packet.getData());
		mContent = mContent.trim();
		
		// get the fields from the packet
		String[] mFields = mContent.split("\\|");
		
		//validate the packet according to business rules
		try {
			PacketValidator.isValidIncident(mFields);
		} catch (ValidationException e) {
			if(V_LOG) {
				Log.v(TAG, "packet didn't pass validation", e);
			}
			
			// exit the method early as the validation failed
			return;
		}
		
		// packet passed validation so continue
		// declare other helper variables
		Cursor mCursor          = null;
		String[] mColumns       = null;
		String   mSelection     = null;
		String[] mSelectionArgs = null;
		
		// check to make sure we haven't saved this packet already as duplicates are expected
		
		// columns to return
		mColumns = new String[1];
		mColumns[0] = IncidentProvider._ID;
		
		// where statement
		mSelection = IncidentProvider.PHONE_NUMBER_FIELD + " = ? AND " + IncidentProvider.TIMESTAMP_FIELD + " = ?";
		
		// values to match against
		mSelectionArgs = new String[2];
		mSelectionArgs[0] = mFields[0];
		mSelectionArgs[1] = mFields[6];
		
		// execute the query
		mCursor = contentResolver.query(incidentContentUri, mColumns, mSelection, mSelectionArgs, null);
	
		if(mCursor.getCount() == 0) {
			
			// values weren't found so we can store this new packet
			ContentValues mValues = new ContentValues();
			mValues.put(IncidentProvider.PHONE_NUMBER_FIELD, mFields[0]);
			mValues.put(IncidentProvider.TITLE_FIELD, mFields[1]);
			mValues.put(IncidentProvider.DESCRIPTION_FIELD, mFields[2]);
			mValues.put(IncidentProvider.CATEGORY_FIELD, mFields[3]);
			mValues.put(IncidentProvider.LATITUDE_FIELD, mFields[4]);
			mValues.put(IncidentProvider.LONGITUDE_FIELD, mFields[5]);
			mValues.put(IncidentProvider.TIMESTAMP_FIELD, mFields[6]);
			mValues.put(IncidentProvider.TIMEZONE_FIELD, mFields[7]);
			mValues.put(IncidentProvider.IP_ADDRESS_FIELD, packet.getAddress().getHostAddress());
			
			// add the row
			try {
				contentResolver.insert(incidentContentUri, mValues);
			} catch (SQLException e) {
				Log.e(TAG, "unable to save new incident data", e);
			}
			
			//status message
			if(V_LOG) {
				Log.v(TAG, "new incident data saved to database");
			}
		} else {
			if(V_LOG) {
				Log.v(TAG, "duplicate incident data detected");
			}
		}
		
		// play nice and tidy up
		mCursor.close();
	}
	
	/**
	 * request that the thread stops
	 */
	public void requestStop() {
		keepGoing = false;
	}

}
