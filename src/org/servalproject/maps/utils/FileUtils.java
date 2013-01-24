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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.servalproject.maps.protobuf.BinaryFileContract;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
	 * tests to see if the given path is a directory and can be written to
	 * 
	 * @param path the full path to test
	 * @return true if the path is a directory and be be written to
	 */
	public static boolean isDirectoryReadable(String path) {
		
		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}
		
		File mPath = new File(path);
		
		if(mPath.isDirectory() && mPath.canRead()) {
			return true;
		} else {
			return false;
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
	 * copy the contents of one file into another file
	 * @param inputStream an InputStream which is the source of the data
	 * @param destination the destination file
	 * @throws IOException
	 */
	public static void copyFile(InputStream inputStream, File destination) throws IOException {
		try{
			OutputStream mOutputStream = new FileOutputStream(destination);
			try{
				byte buff[] = new byte[64*1024];
				int read=0;
				while((read = inputStream.read(buff)) >=0){
					mOutputStream.write(buff, 0, read);
				}
			}finally{
				mOutputStream.close();
			}
		}finally{
			inputStream.close();
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
		File mSourceFile = new File(filePath);
		File mDestinationFile = new File(dirPath, mSourceFile.getName());
		copyFile(new FileInputStream(mSourceFile), mDestinationFile);
		return mDestinationFile.getAbsolutePath();
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
	
	/**
	 * delete all files in a directory
	 * 
	 * @param path the path to the directory
	 * @param extensions a list of extensions to match against
	 * @throws IOException
	 */
	public static int deleteFilesInDir(String dirPath, String[] extensions) throws IOException {
		
		int count = 0;
		
		// check the parameters
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}
		
		if(isDirectoryWritable(dirPath) == false) {
			throw new IOException("unable to access the required directory: " + dirPath);
		}
		
		// get a list of filee
		File mDir = new File(dirPath);
		
		File[] mFiles = mDir.listFiles(new ExtensionFileFilter(extensions));
		
		for(File mFile: mFiles) {
			if(mFile.delete()) {
				count++;
			} else {
				throw new IOException("unable to delete file: " + mFile.getCanonicalPath());
			}
		}

		return count;
		
	}
	
	/**
	 * delete the specified directory
	 * @param dirPath the full path to the directory
	 * @throws IOException
	 */
	public static void deleteDirectory(String dirPath) throws IOException {
		
		// check the parameters
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}
		
		if(isDirectoryWritable(dirPath) == false) {
			throw new IOException("unable to access the required directory: " + dirPath);
		}
		
		if(listFilesInDir(dirPath, null) != null) {
			Log.d("FileUtils", Arrays.toString(listFilesInDir(dirPath, null)));
			throw new IOException("unable to delete the directory, it isn't empty");
		}
		
		// delete the directory
		File mDirectory = new File(dirPath);
		
		if(!mDirectory.delete()) {
			throw new IOException("unable to delete the specified directory");
		}
	}
	
	/**
	 * get a list of files in a directory
	 * 
	 * @param dirPath the directory to search for files
	 * @param extensions a list of extensions to filter the list of files, if null all files are returns
	 * @return an array of file names or null if no files match
	 * @throws IOException
	 */
	
	public static String[] listFilesInDir(String dirPath, String[] extensions) throws IOException {
		
		String[] mFileList = null;
		
		// check the parameters
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}
		
		if(isDirectoryWritable(dirPath) == false) {
			throw new IOException("unable to access the required directory: " + dirPath);
		}
		
		// get a list of files
		File mDir = new File(dirPath);
		
		File[] mFiles = mDir.listFiles(new ExtensionFileFilter(extensions));
		
		if(mFiles != null && mFiles.length > 0) {
			
			mFileList = new String[mFiles.length];
			
			for(int i = 0; i < mFiles.length; i++) {
				mFileList[i] = mFiles[i].getName();
			}
			
			Arrays.sort(mFileList);
		}
		
		return mFileList;
	}
	
	// file filter using extensions
	private static class ExtensionFileFilter implements FileFilter {
		
		private String[] extensions;
		
		public ExtensionFileFilter(String[] extensions) {
			this.extensions = extensions;
		}
		
		public boolean accept(File pathname) {

			if (pathname.isDirectory()) {
				return false;
			}

			if (pathname.canRead() == false) {
				return false;
			}

			String name = pathname.getName().toLowerCase();
			
			if(extensions == null) {
				if(!name.equals("..") || !name.equals(".")) {
					return true;
				}
			} else {
				for(String mExtension: extensions) {
					if(name.endsWith(mExtension)) {
						return true;
					}
				}
				
				return false;
			}
			
			return false;
		}
	}
	
	/**
	 * get the extension component of a file name
	 * 
	 * @param fileName the name of the file
	 * @return the extension of the file, or null
	 */
	public static String getExtension(String fileName) {
		
		if(fileName == null) {
			return null;
		}
		
		int mLocation =  fileName.lastIndexOf(".");
		
		if(mLocation == -1) {
			return null;
		} else {
			return fileName.substring(mLocation + 1);
		}
	}
	
	/**
	 * write the provided string to a temp file
	 * 
	 * @param context a context object used to gain access to system resources
	 * @param contents the contents of the file
	 * @return the full path to the temp file
	 * @throws IOException if something bad happens
	 */
	public static String writeTempFile(Context context, String contents) throws IOException {
		
		// get the external cache directory
		File mTempFile = new File(context.getExternalFilesDir(null), System.currentTimeMillis() + ".tmp");
		
		// write the provided string to the file
		PrintWriter mPrintWriter = new PrintWriter(new FileOutputStream(mTempFile)); 
		
		mPrintWriter.print(contents);
		
		mPrintWriter.close();
		
		return mTempFile.getCanonicalPath();	
	}
	
	/**
	 * format the size of a file in a human readable format
	 * 
	 * code is sourced from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java/3758880#3758880
	 * and considered to be in the public domain
	 * 
	 * @param bytes the size of the file
	 * @param binary output the size using binary units
	 * @return the human readable representation of the size of the file
	 */
	public static String humanReadableByteCount(long bytes, boolean binary) {
		int unit = binary ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (binary ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (binary ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * move a file from one location to another
	 * 
	 * @param filePath full path to the file
	 * @param destDir full path to the destination directory
	 * @return true on success and false on failure
	 */
	public static boolean moveFileToDir(String filePath, String destDir) throws IOException{
		
		if(isFileReadable(filePath) == false) {
			throw new IOException("unable to access the specified source file");
		}
		
		if(isDirectoryWritable(destDir) == false) {
			throw new IOException("unable to access the specified destination directory '" + destDir + "'");
		}
		
		File mSourceFile = new File(filePath);
		
		if(isFileReadable(destDir + mSourceFile.getName())) {
			throw new IOException("destination file already exists");
		}
		
		return mSourceFile.renameTo(new File(destDir + mSourceFile.getName()));
		
	}

}
