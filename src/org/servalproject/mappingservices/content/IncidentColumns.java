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

package org.servalproject.mappingservices.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * An interface defining the constants that are used for accessing incident data
 * 
 * @author corey.wallis@servalproject.org
 *
 */

public interface IncidentColumns {
	
	/**
	 * Version of the database supported by this class
	 */
	public static final int DATABASE_VERSION = 1;
	
	/**
	 * Name of the database file
	 */
	public static final String DATABASE_NAME = "serval-maps-incidents.db";
	
	/**
	 * Name of the locations table
	 */
	public static final String TABLE_NAME = "incidents";
	
	/**
	 * Name of the id field
	 */
	public static final String _ID = BaseColumns._ID;
	
	/**
	 * Name of the phone number field
	 */
	public static final String PHONE_NUMBER_FIELD = "phone_number";
	
	/**
	 * Name of the SID field
	 */
	public static final String SID_FIELD = "sid";
	
	/**
	 * Name of the ip address field
	 */
	public static final String IP_ADDRESS_FIELD = "ip_address";
	
	/**
	 * Name of the title field
	 */
	public static final String TITLE_FIELD = "title";
	
	/**
	 * Name of the description field
	 */
	public static final String DESCRIPTION_FIELD = "description";
	
	/**
	 * Name of the category field
	 */
	public static final String CATEGORY_FIELD = "category";
	
	/**
	 * Name of the latitude field
	 */
	public static final String LATITUDE_FIELD = "latitude";
	
	/**
	 * Name of the longitude field
	 */
	public static final String LONGITUDE_FIELD = "longitude";
	
	/**
	 * Name of the time stamp field
	 */
	public static final String TIMESTAMP_FIELD = "timestamp";
	
	/**
	 * Name of the time zone field
	 */
	public static final String TIMEZONE_FIELD = "timezone";
	
	/**
	 * Name of the signature field
	 */
	public static final String SIGNATURE_FIELD = "signature";
	
	public static final String AUTHORITY = "org.servalproject.mappingservices.content.incidentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://org.servalproject.mappingservices.content.incidentprovider" + "/" + TABLE_NAME);

}