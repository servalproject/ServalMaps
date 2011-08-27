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
package org.servalproject.mappingservices.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * use mock locations to populate the locations via the GPS_Provider
 * 
 */

public class MockLocationCreator implements Runnable {
	
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MLC";
	
	/*
	 * private class level variables
	 */
	private Context context;
	private volatile boolean keepGoing = true;
	
	private InputStream input;
	private BufferedReader reader;
	
	LocationManager locationManager;
	
	
	/**
	 * constructor for this object
	 * @param context a valid context object
	 */
	public MockLocationCreator (Context context) {
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter cannot be null");
		}
		
		this.context = context;
		
		locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		
		// add a reference to our test provider
		// use the standard provider name so the rest of the code still works
		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, true, true, 0, 5);
		
		if(V_LOG) {
			Log.v(TAG, "object has been constructed");
		}
	}
	
	/**
	 * open the location list file which is stored in the assets directory
	 * 
	 * @throws IOException if the file cannot be open or read
	 */
	public void openLocationList() throws IOException {
		
		try {
			input = context.getAssets().open("test-locations.txt");
			reader = new BufferedReader(new InputStreamReader(input));
		} catch (IOException e) {
			Log.e(TAG, "unable to open the 'test-locations.txt' file", e);
			// TODO update to this when API level 9 is default level
			//throw new IOException("unable to open the required file", e);
			throw new IOException("unable to open the locations list file");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		if(V_LOG) {
			Log.v(TAG, "thread is running");
		}
		
		String mLine = null;
		String[] mTokens;
		
		Integer mSleepTime;
		Double mLatitude;
		Double mLongitude;
		
		Location mLocation;
		
		int mLineCount = 0;
		
		while(keepGoing) {
			
			// get a line from the file
			try {
				mLine = reader.readLine();
				
				mLineCount++;
				
				if(mLine == null) {
					break; // exit the loop
				}
			} catch (IOException e) {
				Log.e(TAG, "unable to read a line from the 'test-locations.txt file", e);
				break; // exit the loop
			}
			
			// make sure this isn't a comment line
			if(mLine.startsWith("#") == true) {
				continue;
			}
			
			// process the line
			mTokens = mLine.split("\\|");
			
			if(mTokens.length != 3) {
				Log.e(TAG, "expected 3 data elements found '" + mTokens.length + "' on line: " + mLineCount);
				continue;
			}
			
			try {
				mSleepTime = Integer.parseInt(mTokens[0]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the sleep time element on line: " + mLineCount);
				continue;
			}
			
			try {
				mLatitude = Double.parseDouble(mTokens[1]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the latitude element on line: " + mLineCount);
				continue;
			}
			
			try {
				mLongitude = Double.parseDouble(mTokens[2]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the longitude element on line: " + mLineCount);
				continue;
			}
			
			// build a new location 
			mLocation = new Location(LocationManager.GPS_PROVIDER);
			mLocation.setLatitude(mLatitude);
			mLocation.setLongitude(mLongitude);
			mLocation.setTime(System.currentTimeMillis());
			
			// send the new location
			locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mLocation);
			
			// sleep the thread
			try {
				Thread.sleep(mSleepTime * 1000);
			} catch (InterruptedException e) {
			}
		}
		
		// play nice and tidy up
		if(keepGoing) {
			playNiceAndTidyUp();
		}
		
		if(V_LOG) {
			Log.v(TAG, "thread has stopped");
		}

	}
	
	private void playNiceAndTidyUp() {
		try {
			reader.close();
			input.close();
		} catch (IOException e) {
			Log.e(TAG, "unable to close the 'test-locations.txt file", e);
		}
	}
	
	/**
	 * request that the thread stops
	 */
	public void requestStop() {
		keepGoing = false;
		playNiceAndTidyUp();
	}

}