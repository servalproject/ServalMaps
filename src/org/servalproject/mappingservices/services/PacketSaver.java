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

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

import org.servalproject.mappingservices.content.LocationOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	private MessageDigest digester = null;
	
	private SQLiteDatabase locationDatabase;
	
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
	public PacketSaver(Integer locationPort, Integer incidentPort, LinkedBlockingQueue<DatagramPacket> packetQueue, SQLiteDatabase locationDatabase) {
		
		// validate the parameters
		if(locationPort == null || incidentPort == null || packetQueue == null || locationDatabase == null) {
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
		this.locationDatabase = locationDatabase;
		
		// setup the message digester
		try {
			digester = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "unable to create the SHA-1 message digester", e);
			digester = null;
		}
		
		// debug status messages
		Log.v(TAG, "locationDatabase location: " + locationDatabase.getPath());
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
		
		// declare other helper variables
		Cursor mCursor          = null;
		String[] mColumns       = null;
		String   mSelection     = null;
		String[] mSelectionArgs = null;
		
		// check to make sure we have the required number of fields
		// see: http://developer.servalproject.org/twiki/bin/view/Main/PublicAlphaMappingServiceLocationPackets
		
		/*
		 * TODO remove phone number as it isnt needed and adjust rest of the code to compensate
		 */
		
		if(mFields.length == 5) {
			// required number of fields found
			
			//TODO packet content validation
			
			// get a hash of the packet content
			String mHash = getHash(mContent);
			
			// check to see if the hashing worked
			if(mHash != null) {
				
				// setup the DB objects
				mColumns = new String[1];
				mColumns[0] = LocationOpenHelper.HASH_INDEX_FIELD;
				mSelection  = LocationOpenHelper.HASH_INDEX_FIELD + " = ?"; 
				mSelectionArgs = new String[1];
				mSelectionArgs[0] = mHash;
				
				// check to see if this packet is already stored
				mCursor = locationDatabase.query(LocationOpenHelper.TABLE_NAME, mColumns, mSelection, mSelectionArgs, null, null, null, null);
				
				if(mCursor.getCount() == 0) {
					// add the packet to the database
					
					// create a collection of new values
					ContentValues mValues = new ContentValues();
					mValues.put(LocationOpenHelper.PHONE_NUMBER_FIELD, mFields[0]);
					mValues.put(LocationOpenHelper.IP_ADDRESS_FIELD, packet.getAddress().getHostAddress());
					mValues.put(LocationOpenHelper.LATITUDE_FIELD, mFields[1]);
					mValues.put(LocationOpenHelper.LONGITUDE_FIELD, mFields[2]);
					mValues.put(LocationOpenHelper.TIMESTAMP_FIELD, mFields[3]);
					mValues.put(LocationOpenHelper.TIMEZONE_FIELD, mFields[4]);
					
					// add the row
					locationDatabase.insert(LocationOpenHelper.TABLE_NAME, null, mValues);
					
					// play nice and tidy up
					mValues = null;
					
					//status message
					if(V_LOG) {
						Log.v(TAG, "new location data saved to database");
					}
				} 
				
				// play nice and tidy up
				mCursor.close();
				
			} else {
				// hashing didn't work
				// use multiple fields to check if a record already exists
			}
			
		} else {
			if(V_LOG) {
				Log.v(TAG, "location packet didn't have required number of fields");
			}
		}
	}
	
	// private method to save incident data
	private void saveIncident(DatagramPacket packet) {
		
	}
	
	//private method to generate a hash for comparison purposes
	private String getHash(String input) {
		
		// declare local variables
		String mHashString = null;
		
		if(digester != null) {
			// reset the digester
			digester.reset();
			
			// convert the string to bytes and hash those bytes
			digester.update(input.getBytes());
			
			// convert the hashed bytes into an hex encoded string
			BigInteger mHash = new BigInteger(1, digester.digest());
			mHashString = mHash.toString(16);
			
			// check to ensure if a leading 0 is required
			if((mHashString.length() % 2) != 0) {
				mHashString = "0" + mHashString;
			}
		}

		return mHashString;
	}
	
	/**
	 * request that packet collection stops and so stop the thread
	 */
	public void requestStop() {
		keepGoing = false;
	}

}
