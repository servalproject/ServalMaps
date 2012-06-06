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
import org.servalproject.maps.utils.FileUtils;

import android.content.Context;
import android.content.Intent;


/**
 * used to add a file to the Rhizome repository
 */
public class Rhizome {
	
	/*
	 * class level constants
	 */
	//private static final String TAG = "Rhizome";

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
		
		File mManifestFile = getManifestPath(filePath);
		if (mManifestFile.exists()){
			// pass in the previous manifest, so rhizome can update it
			mIntent.putExtra("previous_manifest", mManifestFile.getAbsolutePath());
		}
		
		// ask rhizome to save the new manifest here
		mIntent.putExtra("save_manifest", mManifestFile.getAbsolutePath());
		context.getApplicationContext().startService(mIntent);
		
	}
	
	/*
	 * get the path to a manifest based on the path to the content
	 * @path the path to the content file
	 */
	private static File getManifestPath(String path){
		File mManifestPath = new File(path);
		File mManifestFile = new File(mManifestPath.getParent(), ".manifest-" + mManifestPath.getName());
		return mManifestFile;
	}
}
