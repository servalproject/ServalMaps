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
package org.servalproject.maps.mapsforge;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.text.TextUtils;

/**
 * a class to hold utility methods for working with mapsforge classes and data
 */
public class MapUtils {
	
	/**
	 * extract metadata from a map file
	 * 
	 * @param filePath the path to the file
	 * @return a HashMap of metadata keys and values
	 * @throws IOException
	 */
	public static HashMap<String, String> getMetadata(String filePath) throws IOException {
		
		// check on the parameters
		if(TextUtils.isEmpty(filePath) == true) {
			throw new IllegalArgumentException("the filePath parameter is required");
		}
		
		if(!FileUtils.isFileReadable(filePath)) {
			throw new IOException("unable to access the specified file");
		}
		
		HashMap<String, String> mMetadata = new HashMap<String, String>();
		
		MapDatabase mMapDatabase = new MapDatabase();
		
		// open the database file
		FileOpenResult mFileOpenResult = mMapDatabase.openFile(new File(filePath));
		
		if(mFileOpenResult != FileOpenResult.SUCCESS) {
			throw new IOException("unable to open the following file: " + filePath +" (" + mFileOpenResult + ")");
		}
			
		// get the metadata
		MapFileInfo mMapFileInfo = mMapDatabase.getMapFileInfo();
		mMapDatabase.closeFile();
		
		// populate the hashmap
		mMetadata.put("date", TimeUtils.formatDate(mMapFileInfo.mapDate, TimeZone.getDefault().getID()));
		mMetadata.put("short-date", TimeUtils.formatDateSimple(mMapFileInfo.mapDate, TimeZone.getDefault().getID()));
		
		mMetadata.put("min-latitude", Double.toString(
				microDegreesToDegrees(
						mMapFileInfo.boundingBox.minLatitudeE6)
				)
			);
		
		mMetadata.put("min-longitude", Double.toString(
				microDegreesToDegrees(
						mMapFileInfo.boundingBox.minLongitudeE6)
				)
			);
		
		mMetadata.put("max-latitude", Double.toString(
				microDegreesToDegrees(
						mMapFileInfo.boundingBox.maxLatitudeE6)
				)
			);
		
		mMetadata.put("max-longitude", Double.toString(
				microDegreesToDegrees(
						mMapFileInfo.boundingBox.maxLongitudeE6)
				)
			);
		
		return mMetadata;
	}
	
	/**
	 * convert a latitude or longitude value from microdegrees to degrees
	 * 
	 * @param microDegrees a latitude or longitude value in microdegrees
	 * @return the value of microdegrees in degrees
	 */
	public static double microDegreesToDegrees(int microDegrees) {
	    return microDegrees / 1E6;
	}

}
