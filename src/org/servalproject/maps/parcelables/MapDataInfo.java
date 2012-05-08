/*
 * Copyright (c) 2012, The Serval Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the The Serval Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SERVAL PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.servalproject.maps.parcelables;

import java.util.HashMap;

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
	private String createDate;
	private double minLongitude;
	private double minLatitude;
	private double maxLongitude;
	private double maxLatitude;
	
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
	
	/**
	 * set the metadata for this file
	 * @param data the metadata hashmap
	 */
	public void setMetadata(HashMap<String, String> data) {
		
		this.createDate = data.get("date");
		this.minLongitude = Double.parseDouble(data.get("min-longitude"));
		this.minLatitude = Double.parseDouble(data.get("min-latitude"));
		this.maxLongitude = Double.parseDouble(data.get("max-longitude"));
		this.maxLatitude = Double.parseDouble(data.get("max-latitude"));
		
	}
	
	/**
	 * get the create date
	 * @return the date that the map file was created
	 */
	public String getCreateDate() {
		return createDate;
	}
	
	/**
	 * get minimum longitude
	 * @return the minimum longitude of the bounding box of the data in this map data file
	 */
	public double getMinLongitude() {
		return minLongitude;
	}
	
	/**
	 * get minimum latitude
	 * @return the minimum latitude of the bounding box of the data in this map data file
	 */
	public double getMinLatitude() {
		return minLatitude;
	}
	
	/**
	 * get maximum longitude
	 * @return the maximum longitude of the bounding box of the data in this map data file
	 */
	public double getMaxLongitude() {
		return maxLongitude;
	}
	
	/**
	 * get maximum latitude
	 * @return the maximum latitude of the bounding box of the data in this map data file
	 */
	public double getMaxLatitude() {
		return maxLatitude;
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
		dest.writeString(createDate);
		dest.writeDouble(minLongitude);
		dest.writeDouble(minLatitude);
		dest.writeDouble(maxLongitude);
		dest.writeDouble(maxLatitude);
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
    	createDate = in.readString();
    	minLongitude = in.readDouble();
    	minLatitude = in.readDouble();
    	maxLongitude = in.readDouble();
    	maxLatitude = in.readDouble();
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	
    	StringBuilder mBuilder = new StringBuilder();
    	mBuilder.append("File: " + fileName + "\n");
    	mBuilder.append("Created: " + createDate + "\n");
    	mBuilder.append("Min lat/lng: " + Double.toString(minLatitude) + "," + Double.toString(minLatitude) + "\n");
    	mBuilder.append("Max lat/lng: " + Double.toString(maxLatitude) + "," + Double.toString(maxLatitude) + "\n");
    	
    	return mBuilder.toString();
    }
}
