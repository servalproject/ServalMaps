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

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import org.servalproject.mappingservices.content.IncidentOpenHelper;
import org.servalproject.mappingservices.net.PacketSender;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Periodically randomly selects an incident report and sends it on the network
 * as a way of ensuring peers get as a complete set of incidents as possible
 *  
 * @author corey.wallis@servalproject.org
 *
 */

public class IncidentRepeater implements Runnable{
	
	/**
	 * the amount of time, in seconds, that this thread sleeps before sending a new incident
	 */
	public static final int SLEEP_TIME = 30;
	
	/*
	 * private class level variables
	 */
	private volatile boolean keepGoing = true;
	private SQLiteDatabase database = null;
	
	private Random randomNumbers = null;
	
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-IR";
	
	/**
	 * constructor for this class
	 * 
	 * @param database a handle to the incident database
	 * 
	 */
	public IncidentRepeater(SQLiteDatabase database) {
		
		// double check the parameters
		if(database == null) {
			throw new IllegalArgumentException("all parameters to this constructor cannot be null");
		}
		
		this.database = database;
		randomNumbers = new Random();
		
	}

	@Override
	public void run() {
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "incident repeater started");
		}
		
		//declare other helper variables
		Cursor mCursor = null;
		String mSql = null;
		int mMaxId = 0;
		int mSelectId = 0;
		StringBuilder mPacketContent = null;
		
		// loop until requested to stop
		while(keepGoing == true) {
			
			// work out the maximum record number
			mSql = "SELECT MAX(" + IncidentOpenHelper._ID + ") FROM " + IncidentOpenHelper.TABLE_NAME;
			
			mCursor = database.rawQuery(mSql, null);
			mCursor.moveToFirst();
			
			if(mCursor.getCount() > 0) {
				mMaxId = mCursor.getInt(0);
			} else {
				mMaxId = -1;
			}
			
			// play nice and close the cursor
			mCursor.close();
			mCursor = null;
			
			// check to see if we should continue
			if(mMaxId > 0) {
				// there is at least one record that we can repeat
				
				// get a random number between 1 and the maximum to use as an id in the select
				mSelectId = randomNumbers.nextInt(mMaxId);
				
				// adjust the value if we get a zero
				if(mSelectId == 0) {
					mSelectId = 1;
				}
				
				// get the selected record
				mCursor = database.query(IncidentOpenHelper.TABLE_NAME, null, IncidentOpenHelper._ID + " = " + mSelectId, null, null, null, null);
				mCursor.moveToFirst();
				
				// build the content of the packet
				mPacketContent = new StringBuilder();
				mPacketContent.append(mCursor.getString(1) + "|");
				mPacketContent.append(mCursor.getString(3) + "|");
				mPacketContent.append(mCursor.getString(4) + "|");
				mPacketContent.append(mCursor.getString(5) + "|");
				mPacketContent.append(mCursor.getString(6) + "|");
				mPacketContent.append(mCursor.getString(7) + "|");
				mPacketContent.append(mCursor.getString(8) + "|");
				mPacketContent.append(mCursor.getString(9));
				
				// send the packet
				try {
					PacketSender.sendBroadcast(MappingDataService.INCIDENT_PORT, mPacketContent.toString());
					
					// output some debug text
					if(V_LOG) {
						Log.v(TAG, "incident repeater sent an incident");
					}
					
				} catch (UnknownHostException e) {
					Log.e(TAG, "unable to send an incident packet", e);
				} catch (SocketException e) {
					Log.e(TAG, "unable to send an incident packet", e);
				} catch (IOException e) {
					Log.e(TAG, "unable to send an incident packet", e);
				}
				
				// play nice and tidy up
				mCursor.close();
				mCursor = null;	
			}
			
			// sleep for specified time
			try {
				Thread.sleep(SLEEP_TIME * 1000);
			} catch (InterruptedException e) {
				if(V_LOG) {
					Log.v(TAG, "incident repeater was interrupted while sleeping", e);
				}
			}
		}
	}
	
	/**
	 * request that the thread stops
	 */
	public void requestStop() {
		keepGoing = false;
	}

}
