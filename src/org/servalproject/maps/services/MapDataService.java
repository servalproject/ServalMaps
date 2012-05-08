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

package org.servalproject.maps.services;

import java.io.IOException;
import java.util.ArrayList;

import org.servalproject.maps.R;
import org.servalproject.maps.parcelables.MapDataInfo;
import org.servalproject.maps.utils.FileUtils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

/**
 * A service used to prepare information about the available map data files
 */
public class MapDataService extends IntentService {
	
	/*
	 * private constants
	 */
	//private final boolean V_LOG = true;
	private final String TAG = "MapDataService";
	
	private final String[] EXTENSIONS = {".map"}; 

	/**
	 * constructor for this class
	 * required by the use of the parent IntentService class
	 */
	public MapDataService() {
		super("MapDataService");
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent arg0) {
		
		// build a path to the location of the map data
		String mMapDataPath = Environment.getExternalStorageDirectory().getPath();
		mMapDataPath += getString(R.string.system_path_map_data);
		
		String[] mMapDataFiles = new String[0];
		Intent mBroadcastIntent = new Intent("org.servalproject.maps.MAP_DATA_LIST");
		
		// check to see if the path is available
		if(FileUtils.isDirectoryReadable(mMapDataPath) == false) {
			Log.e(TAG, "unable to access the map data directory");
		} else {
			// get the list of map data files
			try {
				mMapDataFiles = FileUtils.listFilesInDir(mMapDataPath, EXTENSIONS);
			} catch (IOException e) {
				Log.e(TAG, "unable to get a list of files", e);
				mMapDataFiles = new String[0];
			}
		}
		
		// populate the intent the intent
		mBroadcastIntent.putExtra("count", mMapDataFiles.length);
		
		if(mMapDataFiles.length > 0) {
			// build a list of parcelables
			ArrayList<MapDataInfo> mMapDataInfoList = new ArrayList<MapDataInfo>();
			
			for(String mMapDataFile : mMapDataFiles) {
				MapDataInfo mMapDataInfo = new MapDataInfo(mMapDataFile);
				mMapDataInfoList.add(mMapDataInfo);
			}
			
			mBroadcastIntent.putParcelableArrayListExtra("files", mMapDataInfoList);
		}
		
		// send the broadcast intent
		this.sendBroadcast(mBroadcastIntent, "org.servalproject.maps.MAP_DATA");
	}
}
