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

import org.servalproject.maps.R;
import org.servalproject.maps.utils.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * used to add a file to the Rhizome repository
 */
public class Rhizome {
	
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
			throw new IllegalArgumentException("unable to access the specified file");
		}
		
		// get access to the preferences
		SharedPreferences mPreferences = context.getSharedPreferences("rhizome", Context.MODE_PRIVATE);
		
		String mVersionName = new File(filePath).getName() + "-version";
		
		Long mVersion = mPreferences.getLong(mVersionName, 0);
		
		// increment the version
		mVersion++;
		
		// get the name identifier
		String mName = context.getString(R.string.app_name);
		
		// build the intent
		Intent mIntent = new Intent("org.servalproject.rhizome.ADD_FILE");
		mIntent.putExtra("path", filePath);
		mIntent.putExtra("version", mVersion);
		mIntent.putExtra("author", mName);
		context.getApplicationContext().startService(mIntent);
		
		// update version
		Editor mEditor = mPreferences.edit();
		mEditor.putLong(mVersionName, mVersion);
		mEditor.commit();
	}
}
