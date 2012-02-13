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
package org.servalproject.maps.location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * a runnable class that sends mock locations for testing purposes
 */
public class MockLocations implements Runnable {
	
	/*
	 * private class level constants
	 */
	private final String TAG = "MockLocations";
	private final boolean V_LOG = false;
	
	private final String LOCATION_FILE = "mock-locations.txt";
	private final String LOCATION_ZIP_FILE = "mock-locations.zip";
	
	/*
	 * private class level variables
	 */
	private String locations = null;
	
	private LocationManager locationManager;
	
	private volatile boolean keepGoing = true;
	
	/**
	 * create the MockLocations class and open the zip file for the required 
	 * location data
	 * 
	 * @param context the application context
	 * 
	 * @throws IllegalArgumentException if the context field is null
	 * @throws IOException  if opening the zip file fails
	 */
	public MockLocations(Context context) throws IOException {
		
		if(V_LOG) {
			Log.v(TAG, "opening the zip file");
		}
		
		// open the zip file and get the required file inside
		ZipInputStream mZipInput = new ZipInputStream(context.getAssets().open(LOCATION_ZIP_FILE));
		ZipEntry mZipEntry;
		
		// look for the required file
		while((mZipEntry = mZipInput.getNextEntry())!= null) {
			
			if(V_LOG) {
				Log.v(TAG, "ZipEntry: " + mZipEntry.getName());
			}
			
			// read the bytes from the file and convert them to a string
			if(mZipEntry.getName().equals(LOCATION_FILE)) {
				
				if(V_LOG) {
					Log.v(TAG, "required file found inside zip file");
				}
				
				ByteArrayOutputStream mByteStream = new ByteArrayOutputStream();
				byte[] mBuffer = new byte[1024];
				int mCount;
				
				while((mCount = mZipInput.read(mBuffer)) != -1) {
					mByteStream.write(mBuffer, 0, mCount);
				}
				
				locations = new String(mByteStream.toByteArray(), "UTF-8");
			}
			
			if(V_LOG) {
				Log.v(TAG, "location file successfully read");
			}
			
			mZipInput.closeEntry();
		}
		
		mZipInput.close();
		
		// check to make sure everything was read successfully
		if(locations == null) {
			throw new IOException("unable to read the required file from the zip file");
		}
		
		// get an instance of the LocationManager class
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		// add a reference to our test provider
		// use the standard provider name so the rest of the code still works	
		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, true, true, 0, 5);
	}
	
	/**
	 * request that this thread stops
	 */
	public void requestStop() {
		
		if(V_LOG) {
			Log.v(TAG, "thread requested to stop");
		}
		
		keepGoing = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		if(V_LOG) {
			Log.v(TAG, "thread started");
		}
		
		String[] mParts;
		
		int mLineCount = -1; 
		
		int mSleepTime;
		double mLatitude;
		double mLongitude;
		
		Location mLocation;
		
		// loop through each of the locations
		for(String mToken : locations.split("\\n")) {
			
			if(keepGoing == false)  {
				
				if(V_LOG) {
					Log.v(TAG, "thread stopped");
				}
				
				return;
			}
			
			mLineCount++;
			
			// only process lines that aren't comments
			if(mToken.startsWith("#") == true) {
				continue;
			}
			
			mParts = mToken.split("\\|");
			
			/*
			 *  validate the line
			 */
			if(mParts.length != 3) {
				Log.e(TAG, "expected 3 data elements found '" + mParts.length + "' on line: " + mLineCount);
			}
			
			try {
				mSleepTime = Integer.parseInt(mParts[0]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the sleep time element on line: " + mLineCount);
				continue;
			}
			
			try {
				mLatitude = Double.parseDouble(mParts[1]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the latitude element on line: " + mLineCount);
				continue;
			}

			try {
				mLongitude = Double.parseDouble(mParts[2]);
			} catch (NumberFormatException e) {
				Log.e(TAG, "unable to parse the longitude element on line: " + mLineCount);
				continue;
			}
			
			// create the new location
			mLocation = new Location(LocationManager.GPS_PROVIDER);
			mLocation.setLatitude(mLatitude);
			mLocation.setLongitude(mLongitude);
			mLocation.setTime(System.currentTimeMillis());

			// send the new location
			locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mLocation);
			
			if(V_LOG) {
				Log.v(TAG, "new location sent");
			}

			// sleep the thread
			try {
				Thread.sleep(mSleepTime * 1000);
			} catch (InterruptedException e) {
				Log.i(TAG, "thread was interrupted", e);
			}
		}
	}
}
