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

import org.mapsforge.android.maps.ArrayItemizedOverlay;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * implement a Serval Maps specific overlay list
 */
public class OverlayList extends ArrayItemizedOverlay {
	
	// declare class level constants
	private final String TAG = "OverlayList";
	private final boolean V_LOG = true;
	
	// declare class level variables
	private Context context;
	
	
	/**
	 * construct a new overlay list
	 * 
	 * @param defaultMarker - the default marker (may be null). This marker is aligned to the center of its bottom line to allow for conical symbols such as a pin or a needle.
	 * @param context - the reference to the application context.
	 */
	public OverlayList(Drawable defaultMarker, Context context) {
		super(defaultMarker, context);
		
		this.context = context;
	}
	
	@Override
	public boolean onTap(int index) {
		
		OverlayItem mItem = (OverlayItem)this.createItem(index);
		
		//determine what item was chosen
		switch(mItem.getType()) {
		case OverlayItems.SELF_LOCATION_ITEM:
			if(V_LOG){
				Log.v(TAG, "user touched their own marker");
			}
			break;
		case OverlayItems.PEER_LOCATION_ITEM:
			if(V_LOG){
				Log.v(TAG, "user touched a peer location marker");
			}
			break;
		case OverlayItems.POI_ITEM:
			if(V_LOG){
				Log.v(TAG, "user touched a poi marker");
			}
			break;
		default:
			Log.e(TAG, "unknown marker type");
			return false;
		}
		
		
		
		return true;
	}
	
	/*
	 * make sure we don't hold onto a reference to the context
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */

	@Override
	protected void finalize() throws Throwable{
		context = null;
		super.finalize();
	}

}
