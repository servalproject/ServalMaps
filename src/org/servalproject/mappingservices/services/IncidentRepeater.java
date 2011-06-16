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

import java.util.Random;

import org.servalproject.mappingservices.content.IncidentOpenHelper;
import org.servalproject.mappingservices.net.NetworkException;
import org.servalproject.mappingservices.net.PacketBuilder;

import android.content.Context;
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
	 * the amount of time (in seconds) that this thread sleeps before sending a new packet
	 */
	public static final int SLEEP_TIME = 30;
	
	/*
	 * private class level variables
	 */
	private volatile boolean keepGoing = true;
	private SQLiteDatabase database = null;
	
	private Random randomNumbers = null;
	
	private PacketBuilder packetBuilder;
	
	
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
	public IncidentRepeater(SQLiteDatabase database, Context context) {
		
		// double check the parameters
		if(database == null) {
			throw new IllegalArgumentException("all parameters to this constructor cannot be null");
		}
		
		this.database = database;
		randomNumbers = new Random();
		
		packetBuilder = new PacketBuilder(context);
		
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
		
		// loop until requested to stop
		while(keepGoing == true) {
			
			// work out the maximum record number
			mSql = "SELECT MAX(" + IncidentOpenHelper._ID + ") FROM " + IncidentOpenHelper.TABLE_NAME;
			
			mCursor = database.rawQuery(mSql, null);
			mCursor.moveToFirst();
			
			if(mCursor.getCount() > 0) {
				mMaxId = mCursor.getInt(0);
				
				// play nice and close the cursor
				mCursor.close();
				mCursor = null;
				
				if(mMaxId > 0) {
					// get a random number between 1 and the maximum to use as an id in the select
					mSelectId = randomNumbers.nextInt(mMaxId);
					
					// adjust the value if we get a zero
					if(mSelectId == 0) {
						mSelectId = 1;
					}
					
					// build and send the packet
					try {
						packetBuilder.buildAndSendIncident(Integer.toString(mSelectId), false);
					} catch (NetworkException e) {
						Log.e(TAG, "unable to send the incident packet", e);
					}
				}
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
