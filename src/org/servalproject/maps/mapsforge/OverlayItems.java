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

/**
 * an interface that defines constants Overlay Items
 */
public interface OverlayItems {

	/**
	 * an overlay item representing the users location
	 */
	public static final int SELF_LOCATION_ITEM = 0;
	
	/**
	 * an overlay item representing a peer location
	 */
	public static final int PEER_LOCATION_ITEM = 1;
	
	/**
	 * an overlay item representing a point of interest
	 */
	public static final int POI_ITEM = 2;
	
}
