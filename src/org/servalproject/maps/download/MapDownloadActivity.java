package org.servalproject.maps.download;

import java.io.File;
import java.io.IOException;

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
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MapDownloadActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "MapDownloadActivity";
	
	private final int NO_NETWORK_DIALOG = 1;
	private final int ERROR_IN_DOWNLOAD = 2;
	private final int UNABLE_TO_USE_FILE = 3;
	
	// store reference to ourself to gain access to activity methods in inner classes
	private final MapDownloadActivity REFERENCE_TO_SELF = this;
	
	private static JSONArray sMapFileList = null;
	
	/*
	 * class level variables
	 */
	private String mirrorName = null;
	private static String sMirrorUrl = null;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_download);
		
		// get the name and url of this mirror
		Bundle mBundle = this.getIntent().getExtras();
		
		mirrorName = mBundle.getString("name");
		
		// invalidate the cached data if this is a different mirror
		if(sMirrorUrl == null) {
			sMirrorUrl = mBundle.getString("url");
		} if(sMirrorUrl != mBundle.getString("url")) {
			sMirrorUrl = mBundle.getString("url");
			sMapFileList = null;
		}
		
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
		
		if(sMapFileList == null) {
			// get the mirror list and update the ui
			new DownloadFileList().execute(sMirrorUrl + "index.json");
		} else {
			populateList(sMapFileList);
		}
	}
	
	/*
	 * private class to update the UI with the list of files on a separate thread
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
				sMapFileList = mJsonObject.getJSONArray("mapFiles");
				
				REFERENCE_TO_SELF.populateList(sMapFileList);
				
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
	private void populateList(JSONArray fileList) {
		
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
		
		ListView mListView = (ListView) getListView();
		
		MapDownloadAdapter mAdapter = new MapDownloadAdapter(this, fileList);
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
		
		// get the details of the file
		JSONObject mItem = null;
		
		try {
			mItem = (JSONObject) sMapFileList.get(position);
			
			String mFileName =  mItem.getString("fileName");
			
			// make sure the downloads directory exists
			File mDownloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			
			Log.v(TAG, mDownloadDirectory.getPath());
			
			mDownloadDirectory = new File(mDownloadDirectory.getPath() + getString(R.string.system_path_download_data) +  mFileName).getParentFile();
		    mDownloadDirectory.mkdirs();
		    
		    Log.v(TAG, mDownloadDirectory.getPath());
			
			// setup the request
			Request mDownloadRequest = new Request(Uri.parse(sMirrorUrl + mFileName));
			mDownloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
			mDownloadRequest.setAllowedOverRoaming(false);
			mDownloadRequest.setTitle(getString(R.string.system_notification_title));
			mDownloadRequest.setDescription(getString(R.string.system_download_description));
			
			mDownloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.system_path_download_data) +  mFileName);
			
			// enque the downloading of this file
			DownloadManager mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			
			mDownloadManager.enqueue(mDownloadRequest);
			
		} catch (JSONException e) {
			showDialog(UNABLE_TO_USE_FILE);
			Log.e(TAG, "mirror list item at position '" + position + "' could not be used");
			return;
		}		
	}
}
