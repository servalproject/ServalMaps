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
package org.servalproject.maps.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * contains constant definitions for the URIs, column names, MIME types, and other meta-data that 
 * pertains to the map items provider
 */
public final class MapItemsContract {
	
	/**
	 * authority string for the content provider
	 */
	public static final String AUTHORITY = "org.servalproject.maps.provider.items";
	
	/**
	 * meta data about the locations table
	 */
	public static final class Locations {
		
		/**
		 * path component of the URI
		 */
		public static final String CONTENT_URI_PATH = "locations";
		
		/**
		 * content URI for the locations data
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_URI_PATH);
		
		/**
		 * content URI for the most recent locations data, grouped by phone number
		 */
		public static final Uri LATEST_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_URI_PATH + "/latest");
		
		
		/**
		 * content type for a list of items
		 */
		public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
		
		/**
		 * content type for an individual item
		 */
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
		
		/**
		 * table definition
		 */
		public static final class Table implements BaseColumns {
			
			/**
			 * table name
			 */
			public static final String TABLE_NAME = Locations.CONTENT_URI_PATH;
			
			/**
			 * unique id column
			 */
			public static final String _ID = BaseColumns._ID;
			
			/**
			 * phone number of the device
			 */
			public static final String PHONE_NUMBER = "phone_number";
			
			/**
			 * subscriber id of the device
			 */
			public static final String SUBSCRIBER_ID = "subscriber_id";
			
			/**
			 * latitude geo-coordinate
			 */
			public static final String LATITUDE = "latitude";
			
			/**
			 * longitude geo-coordinate
			 */
			public static final String LONGITUDE = "longitude";
			
			/**
			 * timestamp of when the information was saved
			 */
			public static final String TIMESTAMP = "timestamp";
			
			/**
			 * local timezone when the information was saved
			 */
			public static final String TIMEZONE = "timezone";
			
			/**
			 * a list of all of the columns
			 */
			public static final String[] COLUMNS = {_ID, PHONE_NUMBER, SUBSCRIBER_ID, LATITUDE, LONGITUDE, TIMESTAMP, TIMEZONE};
		}
	}
	
	/**
	 * metadata about the poi table
	 */
	public static final class PointsOfInterest {
		
		/**
		 * path component of the URI
		 */
		public static final String CONTENT_URI_PATH = "poi";
		
		/**
		 * content URI for the locations data
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_URI_PATH);
		
		/**
		 * content type for a list of items
		 */
		public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
		
		/**
		 * content type for an individual item
		 */
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
		
		/**
		 * default category indicator
		 */
		public static final int DEFAULT_CATEGORY = 0;
		
		/**
		 * table definition
		 */
		public static final class Table implements BaseColumns {
			
			/**
			 * table name
			 */
			public static final String TABLE_NAME = PointsOfInterest.CONTENT_URI_PATH;
			
			/**
			 * unique id column
			 */
			public static final String _ID = BaseColumns._ID;
			
			/**
			 * phone number of the device
			 */
			public static final String PHONE_NUMBER = "phone_number";
			
			/**
			 * subscriber id of the device
			 */
			public static final String SUBSCRIBER_ID = "subscriber_id";
			
			/**
			 * latitude geo-coordinate
			 */
			public static final String LATITUDE = "latitude";
			
			/**
			 * longitude geo-coordinate
			 */
			public static final String LONGITUDE = "longitude";
			
			/**
			 * timestamp of when the information was saved
			 */
			public static final String TIMESTAMP = "timestamp";
			
			/**
			 * local timezone when the information was saved
			 */
			public static final String TIMEZONE = "timezone";
			
			/**
			 * title of the POI
			 */
			public static final String TITLE = "title";
			
			/**
			 * description of the POI
			 */
			public static final String DESCRIPTION = "description";
			
			/**
			 * category of the POI
			 */
			public static final String CATEGORY = "category";
			
			/**
			 * a list of all of the columns
			 */
			public static final String[] COLUMNS = {_ID, PHONE_NUMBER, SUBSCRIBER_ID, LATITUDE, LONGITUDE, TIMESTAMP, TIMEZONE, TITLE, DESCRIPTION, CATEGORY};
			
		}
	}
}
