/**
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * A class used to represent information about the map data files which can 
 * be sent across processes
 */
public class MapDataInfo implements Parcelable {
	
	/*
	 * TODO add more information about map data files as required
	 */
	
	// class level variables
	private String fileName;
	
	/**
	 * constructor for this class
	 * 
	 * @param fileName the name of the map data file
	 */
	public MapDataInfo(String fileName) {
		
		if(TextUtils.isEmpty(fileName) == true) {
			throw new IllegalArgumentException("the fileName parameter is required");
		}
		
		this.fileName = fileName;
	}
	
	/*
	 * get and set methods
	 */
	
	/**
	 * return the name of the map data file
	 */
	public String getFileName() {
		return fileName;
	}
	
	/* 
	 * parcelable specific methods
	 */

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fileName);
	}
	
	/*
	 * This defines how to regenerate the object
	 */
	public static final Parcelable.Creator<MapDataInfo> CREATOR = new Parcelable.Creator<MapDataInfo>() {
        public MapDataInfo createFromParcel(Parcel in) {
            return new MapDataInfo(in);
        }

        public MapDataInfo[] newArray(int size) {
            return new MapDataInfo[size];
        }
    };
    
    /*
     * undertake the process of regenerating the object
     */
    private MapDataInfo(Parcel in) {
    	fileName = in.readString();
    }
}
