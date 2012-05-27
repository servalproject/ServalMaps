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
package org.servalproject.maps.rhizome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.servalproject.maps.R;
import org.servalproject.maps.utils.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * used to add a file to the Rhizome repository
 */
public class Rhizome {
	
	/*
	 * class level constants
	 */
	private static final String TAG = "Rhizome";
	
	private static File getManifestPath(String path){
		File file = new File(path);
		File manifest = new File(file.getParent(), ".manifest-"+file.getName());
		return manifest;
	}
	/**
	 * add a file to the Rhizome repository
	 * 
	 * @param context a context object used to get access to system resources
	 * @param filePath the full path to the file
	 */
	public static void addFile(Context context, String filePath) {
		
		// check on the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		if(FileUtils.isFileReadable(filePath) == false) {
			throw new IllegalArgumentException("unable to access the specified file '" + filePath + "'");
		}
		
		// build the intent
		Intent mIntent = new Intent("org.servalproject.rhizome.ADD_FILE");
		
		mIntent.putExtra("path", filePath);
		
		File manifest = getManifestPath(filePath);
		if (manifest.exists()){
			// pass in the previous manifest, so rhizome can update it
			mIntent.putExtra("previous_manifest", manifest.getAbsolutePath());
		}
		// ask rhizome to save the new manifest here
		mIntent.putExtra("save_manifest", manifest.getAbsolutePath());
		context.getApplicationContext().startService(mIntent);
		
	}
	
	/**
	 * check to see if a file is in Rhizome
	 * 
	 * @param fileNname the name of the file to look for
	 * @return the full path to the file
	 */
	public static String checkForFile(Context context, String fileName) throws FileNotFoundException{
		
		// check on the parameters
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		if(TextUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("the file name parameter is required");
		}
		
		// get the rhizome path
		String mRhizomePath = context.getString(R.string.system_path_rhizome_data);
		try {
			String mExternal = Environment.getExternalStorageDirectory().getCanonicalPath();
			mRhizomePath = mExternal + mRhizomePath;
		} catch (IOException e) {
			Log.e(TAG, "unable to determine the full path to the Rhizome data store", e);
			throw new FileNotFoundException("unable to determine the full path to the Rhizome data store");
		}
		
		// check on the rhizome path
		if(FileUtils.isDirectoryReadable(mRhizomePath) == false) {
			Log.e(TAG, "unable to access the rhizome directory: " + mRhizomePath);
			throw new FileNotFoundException("unable to access the rhizome directory: " + mRhizomePath);
		}
		
		// check to see if the file is available
		if(FileUtils.isFileReadable(mRhizomePath + fileName) == true) {
			return mRhizomePath + fileName;
		} else {
			throw new FileNotFoundException("unable to find the specified file: " + mRhizomePath + fileName);
		}
	}
}
