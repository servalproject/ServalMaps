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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * utility methods related to the use of media
 */
public class MediaUtils {

	/*
	 * class level constants
	 */
	private static final String TAG = "MediaUtils";
	private static final String PHOTO_FOLDER = "ServalMaps";
	
	/**
	 * common prefix for all Serval Maps photos
	 */
	public static final String PHOTO_FILE_PREFIX = "smaps-photo-";

	/**
	 * get the path to the media store, 
	 * if the path doesn't exist this method will try to create it
	 * 
	 * @return the path to the media store
	 */
	public static String getMediaStore() {

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), PHOTO_FOLDER);

		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.e(TAG, "failed to create directory");
				return null;
			}
		}

		try {
			return mediaStorageDir.getCanonicalPath() + File.separator;
		} catch (IOException e) {
			Log.e(TAG, "unable to determine media store path", e);
			return null;
		}

	}

	/*
	 * The following two methods are adapted from:
	 * http://developer.android.com/guide/topics/media/camera.html#saving-media
	 * 
	 * They are used under the terms of the Apache 2.0 license
	 * http://www.apache.org/licenses/LICENSE-2.0
	 */
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(getMediaStore());

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separatorChar + "IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separatorChar + "VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
}
