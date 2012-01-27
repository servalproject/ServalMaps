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
