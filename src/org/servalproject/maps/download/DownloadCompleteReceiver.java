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

import org.servalproject.maps.R;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

/**
 * receive notification that a download has completed and if the file is the correct mimetype
 * show an activity to handle it
 */
public class DownloadCompleteReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private static final String TAG = "DownloadCompleteReceiver";
	
	
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// make sure this is the right intent
		if(intent == null) {
			Log.w(TAG, "receiver called without intent");
			return;
		}
		
		String mIntentAction = intent.getAction();
		
		if(mIntentAction.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) == true) {
			// this is the right intent
			
			// use the unique id of the download to get information about it
			long mDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			
			if(mDownloadId == -1) {
				// download id is missing for some reason
				Log.w(TAG, "download id was missing");
				return;
			}
			
			// get the details of the download
			DownloadManager mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query mQuery = new DownloadManager.Query();
			mQuery.setFilterById(mDownloadId);
			
			Cursor mCursor = mDownloadManager.query(mQuery);
			
			// check to see what was returned
			if(mCursor.moveToFirst() == false) {
				Log.w(TAG, "unable to lookup details of download with id '" + mDownloadId + "'");
				return;
			}
			
			// check to see if this was a successful download
			int mStatus = mCursor.getInt(mCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			
			if(mStatus != DownloadManager.STATUS_SUCCESSFUL) {
				return;
			}
			
			// check to see if this was a download of a map data file
			String mMimeType = mCursor.getString(mCursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
			
			if(mMimeType != null && mMimeType.equals(context.getString(R.string.system_map_file_mime_type))) {
				// mime type is present and matches so start activity
				
				// get the uri to the local file name
				String mLocalUri = mCursor.getString(mCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				
				//double check the uri and if present start the import activity
				if(mLocalUri != null && TextUtils.isEmpty(mLocalUri) == false) {
					Intent mIntent = new Intent(context, org.servalproject.maps.download.MapImportActivity.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // use this flag as we're starting an activity outside the context of an activity
					mIntent.putExtra("file-uri", mLocalUri);
					context.startActivity(mIntent);
				}
			}
			
		} else {
			Log.w(TAG, "receiver called with wrong intent");
		}

	}

}
