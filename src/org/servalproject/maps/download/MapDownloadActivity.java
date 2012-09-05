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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.servalproject.maps.R;
import org.servalproject.maps.utils.HttpUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * activity used to start the download of a map data file
 */
public class MapDownloadActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "MapDownloadActivity";
	
	private final int NO_NETWORK_DIALOG = 1;
	private final int ERROR_IN_DOWNLOAD = 2;
	private final int UNABLE_TO_USE_FILE = 3;
	
	/*
	 * class level variables
	 */
	private String mirrorName = null;
	private static String sMirrorUrl = null;
	private ArrayList<Integer> downloadedIds = new ArrayList<Integer>();
	
	// private class level variables
	private MapFileDatabase databaseHelper;
	private SQLiteDatabase database;
	private String sqlString;
	private Cursor cursor;
	
	// store reference to ourself to gain access to activity methods in inner classes
	private final MapDownloadActivity REFERENCE_TO_SELF = this;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_download);
		
		// get connection to the database
		databaseHelper = new MapFileDatabase(this);
		database = databaseHelper.getWritableDatabase();
		
		// get the name and url of this mirror
		Bundle mBundle = this.getIntent().getExtras();
		
		mirrorName = mBundle.getString("name");
		
		sMirrorUrl = mBundle.getString("url");
		
		//TODO implement use of cache if same mirror
		
		// empty the database table
		sqlString = "DELETE FROM " + MapFileTableContract.TABLE_NAME;
		database.execSQL(sqlString);
		
		// check and see if a network connection is available
		if(HttpUtils.isOnline(this) == false) {
			// no network connection is available
			
			showDialog(NO_NETWORK_DIALOG);
			
			// hide any outstanding UI elements
			View mView = (View) findViewById(R.id.map_download_ui_progress_bar);
			mView.setVisibility(View.GONE);
			
			mView = (View) findViewById(R.id.map_download_ui_subheading);
			mView.setVisibility(View.GONE);
			
			return;
		}
		
		// update the subheading
		TextView mTextView = (TextView) findViewById(R.id.map_download_ui_subheading);
		mTextView.setText(
				String.format(
						getString(R.string.map_download_ui_lbl_subheading), 
						mirrorName, 
						getString(R.string.map_download_ui_lbl_subheading_part_a)
						)
				);
		
		// download the list of files
		new DownloadFileList().execute(sMirrorUrl + "index.json");
	}
	
	/*
	 * private class to download the list of files on a separate thread
	 */
	private class DownloadFileList extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... urls) {
			
			// get the data
			try {
				String mFileData = HttpUtils.downloadString(urls[0]);
				
				return mFileData;
				
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
			
			// build the list of files if available 
			try {
				
				JSONObject mJsonObject = new JSONObject(result);
				JSONArray mMapFileList = mJsonObject.getJSONArray("mapFiles");
				
				REFERENCE_TO_SELF.importList(mMapFileList);
				
			} catch (JSONException e) {
				Log.e(TAG, "unable to parse JSON '" + e.getMessage() + "'", e);
				showDialog(ERROR_IN_DOWNLOAD);
				return;
			}
		}
		
	};
	
	/*
	 * private method used to populate the list of mirrors
	 */
	private void importList(JSONArray fileList) {
		
		// update the UI
		TextView mTextView = (TextView) findViewById(R.id.map_download_ui_subheading);
		mTextView.setText(
				String.format(
						getString(R.string.map_download_ui_lbl_subheading), 
						mirrorName, 
						getString(R.string.map_download_ui_lbl_subheading_part_b)
						)
				);
		
		ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.map_download_ui_progress_bar);
		mProgressBar.setVisibility(View.GONE);
		
		mProgressBar = (ProgressBar) findViewById(R.id.map_download_ui_progress_bar_2);
		mProgressBar.setMax(fileList.length());
		mProgressBar.setVisibility(View.VISIBLE);
		
		ImportFileList importTask = new ImportFileList(database, mProgressBar);
		importTask.execute(fileList);
	}
	
	/*
	 * private class to import the list of files on a separate thread
	 */
	private class ImportFileList extends AsyncTask<JSONArray, Integer, Integer> {
		
		/*
		 * private class level variables
		 */
		private SQLiteDatabase database;
		private ProgressBar progressBar;
		private ContentValues contentValues;
		private JSONObject object;
		
		public ImportFileList(SQLiteDatabase database, ProgressBar progressBar) {
			this.database = database;
			this.progressBar = progressBar;
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Integer doInBackground(JSONArray... arrays) {
			
			JSONArray array = arrays[0];
			
			for(int i = 0; i < array.length(); i++) {
				
				try {
					// get the object at this index
					object = array.getJSONObject(i);
					contentValues = new ContentValues();
					
					// populate the list of values
					contentValues.put(MapFileTableContract.NAME, object.getString("fileName"));
					contentValues.put(MapFileTableContract.SIZE, Long.valueOf(object.getString("fileSize")));
					contentValues.put(MapFileTableContract.TIMESTAMP, Long.valueOf(object.getString("fileDate")));
					contentValues.put(MapFileTableContract.MAX_LATITUDE, Double.valueOf(object.getString("maxLatitude")));
					contentValues.put(MapFileTableContract.MAX_LONGITUDE, Double.valueOf(object.getString("maxLongitude")));
					contentValues.put(MapFileTableContract.MIN_LATITUDE, Double.valueOf(object.getString("minLatitude")));
					contentValues.put(MapFileTableContract.MIN_LONGITUDE, Double.valueOf(object.getString("minLongitude")));
					
					// insert the values
					database.insert(MapFileTableContract.TABLE_NAME, null, contentValues);
					
				} catch (JSONException e) {
					Log.e(TAG, "unable to process JSON object at index '" + i + "'");
				}
				
				publishProgress(Integer.valueOf(i));
			}
			
			return Integer.valueOf(array.length());
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
	    protected void onProgressUpdate(Integer... progress) {
			
			// update the progress bar
			progressBar.setProgress(progress[0]);
			
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {
			
			// populate the list
			REFERENCE_TO_SELF.populateList();
			
		}
	};
	
	/*
	 * private method used to populate the list of mirrors
	 */
	private void populateList() {
		
		// update the UI
		TextView mTextView = (TextView) findViewById(R.id.map_download_ui_subheading);
		mTextView.setText(
				String.format(
						getString(R.string.map_download_ui_lbl_subheading), 
						mirrorName, 
						getString(R.string.map_download_ui_lbl_subheading_part_c)
						)
				);
		
		ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.map_download_ui_progress_bar_2);
		mProgressBar.setVisibility(View.GONE);
		
		// populate the list
		ListView mListView = (ListView) getListView();
		
		// define a list of views to populate
		
		int[] mTo = new int[5];
		mTo[0] = R.id.map_download_ui_entry_title;
		mTo[1] = R.id.map_mirror_ui_entry_size_txt;
		mTo[2] = R.id.map_download_ui_entry_generated_txt;
		mTo[3] = R.id.map_download_ui_entry_bbox_top_txt;
		mTo[4] = R.id.map_download_ui_entry_bbox_bottom_txt;
		
		// get the data
		cursor = database.query(
				MapFileTableContract.TABLE_NAME, 
				MapFileTableContract.ALL_COLUMNS,
				null, 
				null,
				null, 
				null,
				MapFileTableContract.NAME);
		
		// setup the data adapter
		MapDownloadAdapter mAdapter = new MapDownloadAdapter(
				this,
				R.layout.map_download_entry,
				cursor,
				MapFileTableContract.ALL_COLUMNS,
				mTo);

		mListView.setAdapter(mAdapter);
		
		mListView.setVisibility(View.VISIBLE);
		mListView.setOnItemClickListener(this);
		
	}
	
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
			mBuilder.setMessage(R.string.map_download_ui_dialog_no_network)
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
			mBuilder.setMessage(R.string.map_download_ui_dialog_error_in_download)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
				}
			});
			mDialog = mBuilder.create();
			break;
		case UNABLE_TO_USE_FILE:
			mBuilder.setMessage(R.string.map_download_ui_dialog_unable_to_use_mirror)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
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

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		// lookup the details of the file
		cursor.moveToPosition(position);
		
		String mFileName = cursor.getString(cursor.getColumnIndex(MapFileTableContract.NAME));
		
		if(downloadedIds.contains(Integer.valueOf(position)) == true) {
			
			// show a toast that the download is underway
			Toast.makeText(getApplicationContext(), String.format(getString(R.string.map_download_ui_toast_download_already_queued), mFileName), Toast.LENGTH_LONG).show();
			return;
		}
		
			
		// make sure the downloads directory exists
		File mDownloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		
		mDownloadDirectory = new File(mDownloadDirectory.getPath() + getString(R.string.system_path_download_data) +  mFileName).getParentFile();
	    mDownloadDirectory.mkdirs();
		
		// setup the request
		Request mDownloadRequest = new Request(Uri.parse(sMirrorUrl + mFileName));
		mDownloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
		mDownloadRequest.setAllowedOverRoaming(false);
		mDownloadRequest.setTitle(getString(R.string.system_notification_title));
		mDownloadRequest.setDescription(getString(R.string.system_download_description));
		mDownloadRequest.setMimeType(getString(R.string.system_map_file_mime_type));
		
		mDownloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.system_path_download_data) +  mFileName);
		
		// enque the downloading of this file
		DownloadManager mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		
		mDownloadManager.enqueue(mDownloadRequest);
		
		// show a toast that the download has started
		Toast.makeText(getApplicationContext(), String.format(getString(R.string.map_download_ui_toast_download_started), mFileName), Toast.LENGTH_LONG).show();
		
		// add the id of this download
		downloadedIds.add(Integer.valueOf(position));			
	}
}
