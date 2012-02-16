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
package org.servalproject.maps.meshms;

import org.servalproject.maps.R;
import org.servalproject.maps.provider.MapItemsContract;
import org.servalproject.maps.utils.TimeUtils;
import org.servalproject.meshms.SimpleMeshMS;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

/**
 * used to send messages across the network using MeshMS messages
 */
public class OutgoingMeshMS {
	
	/**
	 * send a MeshMS message containing location information
	 * 
	 * @param context a context object used to get a content resolver object
	 * @param recordId the unique record identifier for the location record
	 * 
	 * @throws IllegalArgumentException if the context parameter is null
	 * @throws IllegalArgumentException if a record cannot be found
	 */
	public static void sendLocationMessage(Context context, String recordId) {
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		// get the record
		ContentResolver mContentResolver = context.getContentResolver();
		
		Uri mContentUri = Uri.parse(MapItemsContract.Locations.CONTENT_URI.toString() + "/" + recordId);
		
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// check on the content
		if(mCursor.getCount() == 0) {
			throw new IllegalArgumentException("the recordId does not match any records");
		}
		
		// build the message
		mCursor.moveToFirst();
		
		String mNiceTime = TimeUtils.formatDate(
				mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.TIMESTAMP)),
				mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.TIMEZONE)));
		
		//TODO add machine readable message content
		String mContent = String.format(context.getString(R.string.meshms_template_location),
				mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LATITUDE)),
				mCursor.getString(mCursor.getColumnIndex(MapItemsContract.Locations.Table.LONGITUDE)),
				mNiceTime);
		
		// prepare the MeshMS
		SimpleMeshMS mMessage = new SimpleMeshMS("*", mContent);
		
		Intent mMeshMSIntent = new Intent("org.servalproject.meshms.SEND_MESHMS");

		// add the SimpleMeshMS parcelable to the intent as an extra
		mMeshMSIntent.putExtra("simple", mMessage);

		// send the intent
		context.startService(mMeshMSIntent);
		
		// play nice and tidy up
		mCursor.close();
	}
}
