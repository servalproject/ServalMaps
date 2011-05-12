/*
 * This file is part of the Serval Mapping Services app.
 *
 *  Serval Mapping Services app is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  Serval Mapping Services app is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Serval Mapping Services app.  
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.servalproject.mappingservices.mapsforge;

import org.mapsforge.android.maps.GeoPoint;
import org.servalproject.mappingservices.content.RecordTypes;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Extend the OverlayItem class from the mapsforge package to include
 * info required by the app
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class OverlayItem extends org.mapsforge.android.maps.OverlayItem {
	
	// class level variables
	private String recordId = null;
	private int    recordType;

	public OverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
	}

	public OverlayItem(GeoPoint point, String title, String snippet,
			Drawable marker) {
		super(point, title, snippet, marker);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * set the record id value for looking up the details of the record
	 * that is the basis for this overlay item
	 * 
	 * @param value the record id
	 * @throws IllegalArgumentException if the value is empty or null
	 */
	public void setRecordId(String value) {
		if(TextUtils.isEmpty(value) == false) {
			recordId = value;
		} else {
			throw new IllegalArgumentException("the record id param is required");
		}
	}
	
	/**
	 * get the record id value for looking up the details of the record
	 * that is the basis for this overlay item
	 * 
	 * @return the record id
	 */
	public String getRecordId() {
		return recordId;
	}
	
	/**
	 * the record type that associates this overlay item with the type
	 * of record
	 * 
	 * @param value the record type from the .content.RecordTypes class
	 * @throws IllegalArgumentException if the value is not defined in the .content.RecordTypes class
	 */
	public void setRecordType(int value) {
		// TODO expand validation for other record types
		if(value != RecordTypes.INCIDENT_RECORD_TYPE && value != RecordTypes.LOCATION_RECORD_TYPE) {
			throw new IllegalArgumentException("the record type is invalid");
		} else {
			recordType = value;
		}
	}
	
	/**
	 * the record type that associated this overlay item with the type
	 * of record
	 * 
	 * @return the record type matching one of the values in the .content.RecordTypes class
	 */
	public int getRecordType() {
		return recordType;
	}
}