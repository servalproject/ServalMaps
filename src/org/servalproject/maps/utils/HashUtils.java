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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

/**
 * a class that exposes a number of utility methods related to hashing
 */
public class HashUtils {
	
	/**
	 * hash algorithm used by the various util methods
	 */
	public static final String HASH_ALGORITHM = "MD5";
	
	/**
	 * generate a hash of a location message
	 * 
	 * @param phone the phone number
	 * @param latitude the latitude coordinate
	 * @param longitude the longitude coordinate
	 * @param time the timestamp
	 * @return a string representation of the hash
	 */
	public static String hashLocationMessage(String phone, double latitude, double longitude, long time) {
		
		String mToHash = phone + Double.toString(latitude) + Double.toString(longitude) + Long.toString(time);
		
		return createHash(mToHash);
	}
	

	/**
	 * generate a hash of a point of interest message
	 * 
	 * @param phone the phone number
	 * @param latitude the latitude coordinate
	 * @param longitude the longitude coordinate
	 * @param title the title of the POI
	 * @param description the description of the POI
	 * @return a string representation of the hash
	 */
	public static String hashPointOfInterestMessage(String phone, double latitude, double longitude, String title, String description) {
		
		String mToHash = phone + Double.toString(latitude) + Double.toString(longitude) + title + description;
		
		return createHash(mToHash);
	}
	
	/*
	 * the following method is based on code found here:
	 * http://p-xr.com/android-snippet-making-a-md5-hash-from-a-string-in-java/
	 * which is considered to be in the public domain
	 */
	private static String createHash(String toHash) {
		
		String mResult = null;
		
		try {
			
			// instantiate and configure the digest class for use with the specified algorithm
			MessageDigest mDigest = MessageDigest.getInstance(HASH_ALGORITHM);
			mDigest.reset();
			
			// compile the string and digest it
			mDigest.update(toHash.getBytes());
			
			// convert the byte array of the digest into a string
			byte[] mBytes = mDigest.digest();
			int mLength = mBytes.length;
			
			StringBuilder mBuilder = new StringBuilder(mLength << 1);
			
			for (int i = 0; i < mLength; i++) {
				mBuilder.append(Character.forDigit((mBytes[i] & 0xf0) >> 4, 16));
				mBuilder.append(Character.forDigit(mBytes[i] & 0x0f, 16));
			}

			return mBuilder.toString();

		} catch (NoSuchAlgorithmException e) {
			Log.e("HashUtils", "unable to use md5 for hashing", e);
		}

		return mResult;
	}
}
