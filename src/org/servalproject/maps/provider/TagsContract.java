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
package org.servalproject.maps.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * define the contract for the POI tag data stored in the provider
 */
public class TagsContract {
	
	/**
	 * path component of the URI
	 */
	public static final String CONTENT_URI_PATH = "tags";
	
	/**
	 * content URI for the tags data
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + MapItems.AUTHORITY + "/" + CONTENT_URI_PATH);
	
	/**
	 * content type for a list of items
	 */
	public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
	
	/**
	 * content type for an individual item
	 */
	public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.org.servalproject.maps.provider.items." + CONTENT_URI_PATH;
	
	/**
	 * content URI for the unique list of tags
	 */
	public static final Uri UNIQUE_CONTENT_URI = Uri.parse("content://" + MapItems.AUTHORITY + "/" + CONTENT_URI_PATH + "/unique");
	
	/**
	 * the delimiter between tags
	 */
	public static final String TAG_DELIMITER = " ";
	
	/**
	 * table definition
	 */
	public static final class Table implements BaseColumns {
		
		/**
		 * name of the table
		 */
		public static final String TABLE_NAME = TagsContract.CONTENT_URI_PATH;
		
		/**
		 * unique id column
		 */
		public static final String _ID = BaseColumns._ID;
		
		/**
		 * unique id of the POI record
		 */
		public static final String POI_RECORD_ID = "poi_record";
		
		/**
		 * the tag associated with this record
		 */
		public static final String TAG = "tag";
		
		/**
		 * a list of all of the columns
		 */
		public static final String[] COLUMNS = {_ID, POI_RECORD_ID, TAG};
	}
}
