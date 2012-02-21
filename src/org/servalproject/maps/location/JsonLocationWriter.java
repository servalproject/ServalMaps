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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.servalproject.maps.R;
import org.servalproject.maps.ServalMaps;
import org.servalproject.maps.rhizome.Rhizome;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class JsonLocationWriter implements Runnable {
	
	// declare class level constants
	private final String TAG = "JsonLocationWriter";
	private final boolean V_LOG = true;
	
	// declare class level variables
	private volatile long updateDelay;
	private volatile boolean keepGoing = true;
	private String fileName = null;
	private String jsonTemplate = null;
	private Location previousLocation = null;
	private Context context = null;
	
	/**
	 * periodically write the current location of the device to a JSON file
	 * 
	 * @param context a context object in which to get a string resource
	 * @param updateDelay the delay between updates to the file (in milliseconds)
	 * @throws IOException if the output file cannot be created
	 */
	public JsonLocationWriter(Context context, long updateDelay) throws IOException {
		
		// check the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// get the path for the output files
		String mOutputPath = Environment.getExternalStorageDirectory().getPath();
		mOutputPath += context.getString(R.string.system_path_binary_data);
		
		// test the path
		if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
			throw new IOException("unable to access the required output directory");
		}
		
		// determine the file name
		ServalMaps mApplication = (ServalMaps) context.getApplicationContext();
		fileName = mApplication.getPhoneNumber();
		mApplication = null;
		
		fileName = fileName.replace(" ", "");
		fileName = fileName.replace("-", "");
		
		fileName = mOutputPath + fileName + "-locations-" + TimeUtils.getTodayAsString() + ".json";
		
		try {
			FileOutputStream mOutput = new FileOutputStream(fileName, true);
			mOutput.close();
		}catch (FileNotFoundException e) {
			throw new IOException("unable to open the output file");
		} catch (IOException e) {
			throw new IOException("unable to open the output file");
		}
		
		jsonTemplate = context.getString(R.string.misc_location_json_template);
		
		this.updateDelay = updateDelay;
		
		this.context = context;
	}
	
	/**
	 * update the delay between upates to the json file
	 * @param updateDelay the new delay in milliseconds
	 */
	public void setUpdateDelay(long updateDelay) {
		this.updateDelay = updateDelay;
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

	@Override
	public void run() {
		
		while(keepGoing) {
		
			// get the current location
			Location mLocation = LocationCollector.getLocation();
			
			if(mLocation != null) {
				
				// TODO undertake further validation of the location object
				if(mLocation == previousLocation) {
					if(V_LOG) {
						Log.v(TAG, "current location is same as previous location");
						Log.v(TAG, "thread sleeping for: " + updateDelay);
					}
					try {
						Thread.sleep(updateDelay);
					} catch (InterruptedException e) {
						if(keepGoing == false) {
							if(V_LOG) {
								Log.v(TAG, "thread was interrupted and is stopping");
							}
							return;
						} else {
							Log.w(TAG, "thread was interrupted without being requested to stop", e);
						}
					}
				}
				
				// write the output
				try {
					PrintWriter mOutput = new PrintWriter (new FileOutputStream(fileName, true));
					mOutput.println(String.format(jsonTemplate, mLocation.getLatitude(), mLocation.getLongitude()));
					mOutput.close();
					
					// add the file to rhizome
					Rhizome.addFile(context, fileName);
					
					if(V_LOG) {
						Log.v(TAG, "location values: '" + mLocation.getLatitude() + "','" +  mLocation.getLongitude() + "'");
						Log.v(TAG, "wrote new file entry: " + String.format(jsonTemplate, mLocation.getLatitude(), mLocation.getLongitude()));
					}
				}catch (FileNotFoundException e) {
					Log.e(TAG, "unable to open the output file");
					return;
				}
				
				// store reference to the location object
				previousLocation = mLocation;
			}
			
			// sleep the thread
			try {
				if(V_LOG) {
					Log.v(TAG, "thread sleeping for: " + updateDelay);
				}
				Thread.sleep(updateDelay);
			} catch (InterruptedException e) {
				if(keepGoing == false) {
					if(V_LOG) {
						Log.v(TAG, "thread was interrupted and is stopping");
					}
					return;
				} else {
					Log.w(TAG, "thread was interrupted without being requested to stop", e);
				}
			}
		}
	}
}
