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

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.servalproject.maps.R;
import org.servalproject.maps.utils.HttpUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * class representing the starting point for downloading map data by downloading the list of mirrors
 */
public class MapMirrorActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "DownloadMapMirrorActivity";
	
	private final int NO_NETWORK_DIALOG = 1;
	private final int ERROR_IN_DOWNLOAD = 2;
	
	// store reference to ourself to close activity if required
	private final MapMirrorActivity REFERENCE_TO_SELF = this;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_mirror);
		
		
		// check and see if a network connection is available
		if(HttpUtils.isOnline(this) == false) {
			// no network connection is available
			
			showDialog(NO_NETWORK_DIALOG);
			
			// hide any outstanding UI elements
			View mView = (View) findViewById(R.id.map_mirror_ui_progress_bar);
			mView.setVisibility(View.GONE);
			
			mView = (View) findViewById(R.id.map_mirror_ui_subheading);
			mView.setVisibility(View.GONE);
			
			return;
		}
		
		// get the mirror list and update the ui
		new DownloadMirrorList().execute(getString(R.string.system_url_map_mirrors));
	}
	
	/*
	 * private class to update the UI with the list of mirrors on a separate thread
	 */
	private class DownloadMirrorList extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... urls) {
			
			// get the data
			try {
				String mMirrorData = HttpUtils.downloadString(urls[0]);
				
				return mMirrorData;
				
			} catch (IOException e) {
				Log.e(TAG, "unable to download data '" + e.toString() + "'");
				return null;
			}
			
		}
		
		protected void onPostExecute(String result) {
			if(result == null) {
				// no data came through
				showDialog(ERROR_IN_DOWNLOAD);
				return;
			}
			
			// build the data
			JSONArray mirrorList = null;
			try {
				mirrorList = new JSONArray(result);
			} catch (JSONException e) {
				Log.e(TAG, "unable to parse JSON", e);
				showDialog(ERROR_IN_DOWNLOAD);
				return;
			}
			
			//debug code
			Log.d(TAG, "lenght of mirror list: " + mirrorList.length());
		}
		
	};
	
	/*
	 * callback method used to construct the required dialog
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;

		switch(id) {
		case NO_NETWORK_DIALOG: 
			mBuilder.setMessage(R.string.map_mirror_ui_dialog_no_network)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
				}
			});
			mDialog = mBuilder.create();
			break;
		case ERROR_IN_DOWNLOAD:
			mBuilder.setMessage(R.string.map_mirror_ui_dialog_error_in_download)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		default:
			mDialog = null;
		}

		// return the created dialog
		return mDialog;	
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
}