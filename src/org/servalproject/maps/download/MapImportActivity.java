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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.servalproject.maps.R;
import org.servalproject.maps.mapsforge.MapUtils;
import org.servalproject.maps.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * an activity used to import a downloaded map data file into Serval Maps
 */
public class MapImportActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private static final String  TAG = "MapImportActivity";
	
	private static final int INVALID_FILE_DIALOG = 1;
	private static final int DELETE_FILE_DIALOG = 2;
	private static final int OVERWRITE_FILE_FAIL_DIALOG = 3;
	private static final int FILE_ALREADY_EXISTS_DIALOG = 4;
	private static final int IMPORT_SUCCESS_DIALOG = 5;
	private static final int IMPORT_FAIL_DIALOG = 6;
	
	// store reference to ourself to gain access to activity methods in inner classes
	private final MapImportActivity REFERENCE_TO_SELF = this;
	
	/*
	 * private class level variables
	 */
	private String fileUri;
	private long downloadId;
	private DownloadManager downloadManager;
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_import);
		
		// get the uri for this file
		Bundle mBundle = this.getIntent().getExtras();
		
		fileUri = mBundle.getString("file-uri");
		downloadId = mBundle.getLong("download-id");
		
		// get an instance of the download manager class
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		
		// get the metadata about the file
		File mMapDataFile = null;
		try {
			mMapDataFile = new File(new URI(fileUri));
		} catch (URISyntaxException e) {
			Log.e(TAG, "unable to parse file uri '" + fileUri + "'", e);
			finish();
		}
		
		/*
		 * test the open of the file
		 */
		HashMap<String, String> mMapMetadata = null;
		
		try {
			mMapMetadata = MapUtils.getMetadata(mMapDataFile.getPath());
		} catch (IOException e) {
			Log.e(TAG, "unable to open the map data file");
			
			// show a dialog
			showDialog(INVALID_FILE_DIALOG);
			
			// end the activity
			finish();
		}
		
		// get the file name
		TextView mTextView = (TextView) findViewById(R.id.map_import_file_name_txt);
		mTextView.setText(mMapDataFile.getName());
		
		// get the file size
		mTextView = (TextView) findViewById(R.id.map_import_file_size_txt);
		mTextView.setText(FileUtils.humanReadableByteCount(mMapDataFile.length(), true));
		
		// get the file date
		mTextView = (TextView) findViewById(R.id.map_import_file_date_txt);
		mTextView.setText(mMapMetadata.get("short-date"));
		
		// top coordinates
		mTextView = (TextView) findViewById(R.id.map_import_ui_bbox_top_txt);
		mTextView.setText(mMapMetadata.get("max-latitude") + "," + mMapMetadata.get("max-longitude"));
		
		// bottom coordinates
		mTextView = (TextView) findViewById(R.id.map_import_ui_bbox_bottom_txt);
		mTextView.setText(mMapMetadata.get("min-latitude") + "," + mMapMetadata.get("min-longitude"));
		
		// setup the buttons
		Button mButton = (Button) findViewById(R.id.map_import_ui_btn_import);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.map_import_ui_btn_delete);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.map_import_ui_btn_close);
		mButton.setOnClickListener(this);
	}
	
	/*
	 * private method to delete a file
	 */
	private boolean deleteFile() {
		
		try {
			File mMapDataFile = new File(new URI(fileUri));
			return mMapDataFile.delete();
		} catch (URISyntaxException e) {
			Log.e(TAG, "unable to parse file uri for delete'" + fileUri + "'", e);
			return false;
		}
	}
	
	/*
	 * private methods to move a map data file
	 */
	private void moveFile() {
		moveFile(false);
	}
	
	private void moveFile(boolean overWrite) {
		try {
			File mMapDataFile = new File(new URI(fileUri));

			// get the map data path
			String mMapDataPath = Environment.getExternalStorageDirectory().getPath();
			mMapDataPath += getString(R.string.system_path_map_data);
			
			// check to see if the file already exists
			if(FileUtils.isFileReadable(mMapDataPath + mMapDataFile.getName()) == true) {
				// file already exists
				if(overWrite == true) {
					File mDeleteFile = new File (mMapDataPath + mMapDataFile.getName());
					
					if(mDeleteFile.delete() == false) {
						showDialog(OVERWRITE_FILE_FAIL_DIALOG);
					}
				} else {
					showDialog(FILE_ALREADY_EXISTS_DIALOG);
				}
			}
			
			try {
				if(FileUtils.moveFileToDir(mMapDataFile.getPath(), mMapDataPath)) {
					showDialog(IMPORT_SUCCESS_DIALOG);
					downloadManager.remove(downloadId);
				} else {
					showDialog(IMPORT_FAIL_DIALOG);
				}
			} catch(IOException e) {
				Log.e(TAG, "unable to move the specified file to the new location", e);
			}
			
		} catch (URISyntaxException e) {
			Log.e(TAG, "unable to parse file uri for move'" + fileUri + "'", e);
		}
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
		
		//TODO add import success and fail dialogs

		switch(id) {
		case INVALID_FILE_DIALOG:
			mBuilder.setMessage(R.string.map_import_ui_dialog_invalid_file)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					deleteFile();
					REFERENCE_TO_SELF.finish();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
				}
			});
			mDialog = mBuilder.create();
			break;
		case DELETE_FILE_DIALOG:
			mBuilder.setMessage(R.string.map_import_ui_dialog_delete_file)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					deleteFile();
					REFERENCE_TO_SELF.finish();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
				}
			});
			mDialog = mBuilder.create();
			break;
		case OVERWRITE_FILE_FAIL_DIALOG: 
			mBuilder.setMessage(R.string.map_import_ui_dialog_overwrite_fail)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case FILE_ALREADY_EXISTS_DIALOG:
			mBuilder.setMessage(R.string.map_import_ui_dialog_file_already_exists)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					moveFile(true);
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case IMPORT_SUCCESS_DIALOG:
			mBuilder.setMessage(R.string.map_import_ui_dialog_success)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					REFERENCE_TO_SELF.finish();
				}
			});
			mDialog = mBuilder.create();
			break;
		case IMPORT_FAIL_DIALOG:
			mBuilder.setMessage(R.string.map_import_ui_dialog_fail)
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


	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		// work out which button was touched
		switch(view.getId()) {
		case R.id.map_import_ui_btn_import:
			// map import button touched
			moveFile();
			break;
		case R.id.map_import_ui_btn_delete:
			showDialog(DELETE_FILE_DIALOG);
			break;
		case R.id.map_import_ui_btn_close:
			// close the activity
			finish();
			break;
		default:
			Log.w(TAG, "unknown button fired click event");
		}
	}
}
