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

import org.mapsforge.android.maps.ArrayItemizedOverlay;

import org.servalproject.mappingservices.ContactPeerActivity;
import org.servalproject.mappingservices.R;
import org.servalproject.mappingservices.ViewIncidentActivity;
import org.servalproject.mappingservices.content.RecordTypes;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Extend the ArrayItemizedOverlay class from the mapsforge package to include
 * functionality required by the app
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class OverlayList extends ArrayItemizedOverlay {
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-OL";
	
	/*
	 * private class level variables
	 */
	
	// store a reference to the content of the parent activity 
	private Context parentContext = null;

	public OverlayList(Drawable defaultMarker, Context context) {
		super(defaultMarker, context);
		parentContext = context;
	}
	
	/*
	 * respond to the tap event on a marker
	 * (non-Javadoc)
	 * @see org.mapsforge.android.maps.ArrayItemizedOverlay#onTap(int)
	 */
	@Override
	public boolean onTap(int index) {
		
		OverlayItem item = (OverlayItem)this.createItem(index);
		
		// see what was tapped
		if(item.getRecordType() == RecordTypes.INCIDENT_RECORD_TYPE) {
			
			if(V_LOG) {
				Log.v(TAG, "incident marker tapped with id:" + item.getRecordId());
			}
			
			// this is an incident marker so start the ViewIncidentActivity 
			// and provide it with the incident record number
			Intent mIntent = new Intent(parentContext, ViewIncidentActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("id", item.getRecordId());
			mIntent.putExtras(mBundle);
			parentContext.startActivity(mIntent);
			
		} else if(item.getRecordType() == RecordTypes.SELF_LOCATION_RECORD_TYPE) {
			// this is a self location marker
			Toast.makeText(parentContext, R.string.overlay_list_self_location, Toast.LENGTH_SHORT).show();
		} else {
			// this is a peer location marker
			if(V_LOG) {
				Log.v(TAG, "peer marker tapped with id:" + item.getRecordId());
			}
			
			// this is an incident marker so start the ViewIncidentActivity 
			// and provide it with the incident record number
			Intent mIntent = new Intent(parentContext, ContactPeerActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("id", item.getRecordId());
			mIntent.putExtras(mBundle);
			parentContext.startActivity(mIntent);
			
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
		parentContext = null;
		super.finalize();
	}

}
