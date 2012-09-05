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
package org.servalproject.maps.download;

import android.provider.BaseColumns;

public class MapFileTableContract implements BaseColumns {
	
	/**
	 * table name
	 */
	public static final String TABLE_NAME = "mapfiles";
	
	/**
	 * unique id column
	 */
	public static final String _ID = BaseColumns._ID;
	
	/**
	 * name and relative path of the file
	 */
	public static final String NAME ="file_name";
	
	/**
	 * size of the file in bytes
	 */
	public static final String SIZE = "file_size";
	
	/**
	 * timestamp of when the file was generated
	 */
	public static final String TIMESTAMP = "timestamp";
	
	/**
	 * minimum latitude component of the bounding box
	 */
	public static final String MIN_LATITUDE = "min_latitude";
	
	/**
	 * minimum longitude component of the bounding box
	 */
	public static final String MIN_LONGITUDE = "min_longitude";
	
	/**
	 * maximum latitude of the bounding box
	 */
	public static final String MAX_LATITUDE = "max_latitude";
	
	/**
	 * maximum longitude of the bounding box
	 */
	public static final String MAX_LONGITUDE = "max_longitude";
	
	/**
	 * all columns
	 */
	public static final String[] ALL_COLUMNS = {_ID, NAME, SIZE, TIMESTAMP, MIN_LATITUDE, MIN_LONGITUDE, MAX_LATITUDE, MAX_LONGITUDE};

}
