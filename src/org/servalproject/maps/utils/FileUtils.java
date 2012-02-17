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
package org.servalproject.maps.utils;

import java.io.File;

import android.text.TextUtils;

/**
 * a class that exposes a number of reusable methods related to files
 */
public class FileUtils {
	
	/**
	 * tests to see if the given path is a directory and can be written to
	 * 
	 * @param path the full path to test
	 * @return true if the path is a directory and be be written to
	 */
	public static boolean isDirectoryWritable(String path) {
		
		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}
		
		File mPath = new File(path);
		
		if(mPath.isDirectory() && mPath.canWrite()) {
			return true;
		} else {
			return mPath.mkdirs();
		}
	}
	
	/**
	 * tests to see if the given path is a file and is readable
	 * 
	 * @param path the full path to test
	 * @return true if the path is a file and is readable
	 */
	// private method to test a path
	public static boolean isFileReadable(String path) {
		
		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}
		
		File mFile = new File(path);
		
		if(mFile.isFile() && mFile.canRead()) {
			return true;
		} else {
			return false;
		}
	}

}
