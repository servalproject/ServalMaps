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

import java.util.HashMap;

import org.mapsforge.android.maps.GeoPoint;

import android.graphics.drawable.Drawable;

/**
 * a class used to represent an overlay item
 */
public class OverlayItem extends org.mapsforge.android.maps.OverlayItem implements OverlayItems {
	
	/*
	 * public class level variables
	 */
	private int itemType = -1;
	private HashMap<String, String> extraInfo = new HashMap<String, String>();

	/**
	 * default constructor for this class
	 */
	public OverlayItem() {
		super();
	}

	/**
	 * construct a new Overlay Item
	 * 
	 * @param point the geographical position of the item (may be null).
	 * @param title the title of the item (may be null).
	 * @param snippet the short description of the item (may be null).
	 */
	public OverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}

	/**
	 * construct a new Overlay Item
	 * 
	 * @param point the geographical position of the item (may be null).
	 * @param title the title of the item (may be null).
	 * @param snippet the short description of the item (may be null).
	 * @param marker  the marker that is drawn for the item (may be null)
	 */
	public OverlayItem(GeoPoint point, String title, String snippet, Drawable marker) {
		super(point, title, snippet, marker);
	}
	
	/**
	 * set the type of overlay item
	 * @param type the type of overlay item
	 */
	public void setType(int type) {
		switch(type) {
		case OverlayItems.SELF_LOCATION_ITEM:
			itemType = type;
			break;
		case OverlayItems.PEER_LOCATION_ITEM:
			itemType = type;
			break;
		case OverlayItems.POI_ITEM:
			itemType = type;
			break;
		default:
			throw new IllegalArgumentException("unknwon item type specified");
		}
	}

	/**
	 * get the type of overlay item
	 * @return the type of overlay item
	 */
	public int getType() {
		return itemType;
	}

	/**
	 * add additional extra information to this item
	 * @param extras the extra information as a string indexed hashmap
	 * 
	 * @throws IllegalArgumentException of the extras parameter is empty or null
	 */
	public void setExtraDetails(HashMap<String, String> extras) {
		if(extras == null) {
			throw new IllegalArgumentException("the extras parameter cannot be null");
		}
		
		if(extras.size() == 0) {
			throw new IllegalArgumentException("the extras parameter cannot be empty");
		}
		extraInfo = extras;
	}

	/**
	 * get the additional extra information about this item
	 * @param extras the extra information as a string indexed hashmap
	 */
	public HashMap<String, String> getExtraInfo() {
		return extraInfo;
	}
}
