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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.servalproject.maps.protobuf.BinaryFileContract;

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
	
	/**
	 * copies a file into a directory
	 * 
	 * @param filePath path to the source file
	 * @param dirPath path to the destination directory
	 * @return the full path of the destination file
	 * @throws IOException 
	 */
	public static String copyFileToDir(String filePath, String dirPath) throws IOException {
		
		// check the parameters
		if(TextUtils.isEmpty(filePath) == true) {
			throw new IllegalArgumentException("the filePath parameter is required");
		}
		
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}
		
		if(isFileReadable(filePath) == false) {
			throw new IOException("unable to access the source file");
		}
		
		if(isDirectoryWritable(dirPath) == false) {
			throw new IOException("unable to access the destination directory");
		}
		
		String mFileName = new File(filePath).getName();
		
		// copy the file
		// based on code found at the URL below and considered to be in the public domain
		// http://stackoverflow.com/questions/1146153/copying-files-from-one-directory-to-another-in-java#answer-1146195
		FileChannel mInputChannel = new FileInputStream(filePath).getChannel();
		FileChannel mOutputChannel = new FileOutputStream(dirPath + mFileName).getChannel();
		
		mOutputChannel.transferFrom(mInputChannel, 0, mInputChannel.size());
		
		// play nice and tidy up
		mInputChannel.close();
		mOutputChannel.close();	
		
		return dirPath + mFileName;
	}
	
	/**
	 * copies a file into a directory
	 * 
	 * @param filePath path to the source file
	 * @param dirPath path to the destination directory
	 * @return the full path of the destination file
	 * @throws IOException 
	 */
	public static String copyFileToDirWithTmpName(String filePath, String dirPath) throws IOException {
		
		// check the parameters
		if(TextUtils.isEmpty(filePath) == true) {
			throw new IllegalArgumentException("the filePath parameter is required");
		}
		
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}
		
		if(isFileReadable(filePath) == false) {
			throw new IOException("unable to access the source file");
		}
		
		if(isDirectoryWritable(dirPath) == false) {
			throw new IOException("unable to access the destination directory");
		}
		
		String mFileName = new File(filePath).getName();
		
		File mOutputFile = null;
		
		if(mFileName.endsWith(BinaryFileContract.LOCATION_EXT)) {
			mOutputFile = File.createTempFile(BinaryFileContract.LOCATION_EXT, null, new File(dirPath));
		} else {
			mOutputFile = File.createTempFile(BinaryFileContract.POI_EXT, null, new File(dirPath));
		}
		
		// copy the file
		// based on code found at the URL below and considered to be in the public domain
		// http://stackoverflow.com/questions/1146153/copying-files-from-one-directory-to-another-in-java#answer-1146195
		FileChannel mInputChannel = new FileInputStream(filePath).getChannel();
		FileChannel mOutputChannel = new FileOutputStream(mOutputFile).getChannel();
		
		mOutputChannel.transferFrom(mInputChannel, 0, mInputChannel.size());
		
		// play nice and tidy up
		mInputChannel.close();
		mOutputChannel.close();	
		
		return mOutputFile.getCanonicalPath();
	}
}
