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
package org.servalproject.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.rhizome.Rhizome;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.MediaUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * allow the user to delete Serval Maps files
 */
public class DeleteActivity extends Activity implements OnClickListener{
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "DeleteActivity";
	
	private final int CONFIRM_DIALOG = 0;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete);
		
		// listen for touches on the button
		Button mButton = (Button) findViewById(R.id.delete_ui_btn_delete);
		mButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// double check to make sure the user wants to do this
		
		switch(v.getId()) {
		case R.id.delete_ui_btn_delete:
			// show the confirm dialog
			showDialog(CONFIRM_DIALOG);
		default:
			Log.w(TAG, "unknown view fired the onClick event");
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

		switch(id) {
		case CONFIRM_DIALOG:
			mBuilder.setMessage(R.string.delete_ui_dialog_delete_confirm)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					undertakeDelete();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		default:
			Log.w(TAG, "unknown dialog requested");
		}
		
		// return the created dialog
		return mDialog;	
	}
	
	/*
	 * method to undertake the delete process
	 */
	private void undertakeDelete() {
		
		// delete the location records
		deleteLocationRecords();
		
		// delete the poi records
		deletePoiRecords();
		
		// delete the data files
		deleteDataFiles();
		
		// delete the photo files
		deletePhotoFiles();
	}
	
	// delete the location records
	private void deleteLocationRecords() {
		
		// show the progress bar
		ProgressBar mProgress = (ProgressBar) findViewById(R.id.delete_ui_progress_location_records);
		mProgress.setVisibility(View.VISIBLE);
		
		// delete the location records
		ContentResolver mContentResolver = getContentResolver();
		
		mContentResolver.delete(
				LocationsContract.CONTENT_URI, 
				null, 
				null);
		
		// hide the progress bar
		mProgress.setVisibility(View.INVISIBLE);
		
		ImageView mImageView = (ImageView) findViewById(R.id.delete_ui_lbl_emo_location_records);
		mImageView.setVisibility(View.VISIBLE);	
	}
	
	// delete the poi records
	private void deletePoiRecords() {
		
		// show the progress bar
		ProgressBar mProgress = (ProgressBar) findViewById(R.id.delete_ui_progress_poi_records);
		mProgress.setVisibility(View.VISIBLE);
		
		// delete the location records
		ContentResolver mContentResolver = getContentResolver();
		
		mContentResolver.delete(
				PointsOfInterestContract.CONTENT_URI, 
				null, 
				null);
		
		// hide the progress bar
		mProgress.setVisibility(View.INVISIBLE);
		
		ImageView mImageView = (ImageView) findViewById(R.id.delete_ui_lbl_emo_poi_records);
		mImageView.setVisibility(View.VISIBLE);	
	}
	
	// delete the data files
	private void deleteDataFiles() {
		
		// show the progress bar
		ProgressBar mProgress = (ProgressBar) findViewById(R.id.delete_ui_progress_data_files);
		mProgress.setVisibility(View.VISIBLE);
		
		try {
			
			// delete the files in Rhizome
			if(deleteFromRhizome() == false) {
				
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		        mBuilder.setMessage(R.string.delete_ui_dialog_delete_rhizome_data)
		               .setCancelable(false)
		               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                	   dialog.cancel();
		                   }
		               });
		        AlertDialog mAlert = mBuilder.create();
		        mAlert.show();
				
			}
			
			String mExternal = Environment.getExternalStorageDirectory().getCanonicalPath() + "/";
			
			// build a list of extensions
			String[] mExtensions = new String[BinaryFileContract.EXTENSIONS.length + 1];
			
			for(int i = 0; i < BinaryFileContract.EXTENSIONS.length; i++) {
				mExtensions[i] = BinaryFileContract.EXTENSIONS[i];
			}
			
			mExtensions[mExtensions.length -1] = ".json";
			
			FileUtils.deleteFilesInDir(mExternal + getString(R.string.system_path_binary_data), mExtensions);
			
			// hide the progress bar
			mProgress.setVisibility(View.INVISIBLE);
			
			ImageView mImageView = (ImageView) findViewById(R.id.delete_ui_lbl_emo_data_files);
			mImageView.setVisibility(View.VISIBLE);	
			
		} catch(IOException e) {
			Log.e(TAG, "unable to delete a data file", e);
			
			// hide the progress bar
			mProgress.setVisibility(View.INVISIBLE);
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
	        mBuilder.setMessage(R.string.delete_ui_dialog_delete_data_fail)
	               .setCancelable(false)
	               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   dialog.cancel();
	                   }
	               });
	        AlertDialog mAlert = mBuilder.create();
	        mAlert.show();
		}
	}
	
	// delete the files from Rhizome
	private boolean deleteFromRhizome() {
		
		// use a flag for the status
		boolean status = true;
		
		// get a temp directory to create the file
		File mCacheDir = getCacheDir();
		
		ServalMaps mApplication = (ServalMaps) getApplication();
		
		// create a temporary binary location file
		String mFileName = mApplication.getPhoneNumber();
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayWithHour() + BinaryFileContract.LOCATION_EXT;
		
		// create the empty file
		try {
			
			// get the full path
			File mFile = new File(mCacheDir.getCanonicalPath() + "/" + mFileName);
						
			// create the empty file
			FileOutputStream mFileStream = new FileOutputStream(mFile);
			mFileStream.close();
			
			// add the file to rhizome
			Rhizome.addFile(this, mFile.getCanonicalPath());
			
			// delete the file
			mFile.delete();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create temporary location binary file", e);
			status = false;
		} catch (IOException e) {
			Log.e(TAG, "unable to create temporary location binary file", e);
			status = false;
		}
		
		// create a temporary binary poi file file
		mFileName = mApplication.getPhoneNumber();
		mFileName = mFileName.replace(" ", "");
		mFileName = mFileName.replace("-", "");
		
		mFileName = mFileName + "-" + TimeUtils.getTodayWithHour() + BinaryFileContract.POI_EXT;
		
		// create the empty file
		try {
			
			// get the full path
			File mFile = new File(mCacheDir.getCanonicalPath() + "/" + mFileName);
			
			// create the empty file
			FileOutputStream mFileStream = new FileOutputStream(mFile);
			mFileStream.close();
			
			// add the file to rhizome
			Rhizome.addFile(this, mFile.getCanonicalPath());
			
			// delete the file
			mFile.delete();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to create temporary poi binary file", e);
			status = false;
		} catch (IOException e) {
			Log.e(TAG, "unable to create temporary poi binary file", e);
			status = false;
		}
		
		// get the list of photo files
		ContentResolver mContentResolver = getContentResolver();
		
		String[] mProjection = new String[1];
		mProjection[0] = PointsOfInterestContract.Table.PHOTO;
		
		String mSelection = PointsOfInterestContract.Table.PHOTO + " != null AND "
				+ PointsOfInterestContract.Table.PHONE_NUMBER + " = ?";
		
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = mApplication.getPhoneNumber();
		
		// get the content
		Cursor mCursor = mContentResolver.query(
				PointsOfInterestContract.CONTENT_URI,
				mProjection,
				mSelection, 
				mSelectionArgs, 
				null);
		
		if(mCursor != null) {
			while(mCursor.moveToNext()) {
				
				// create the empty file
				try {
					
					// get the full path
					File mFile = new File(mCacheDir.getCanonicalPath() + "/" + mCursor.getString(
							mCursor.getColumnIndex(
									PointsOfInterestContract.Table.PHOTO)
								)
							);
								
					// create the empty file
					FileOutputStream mFileStream = new FileOutputStream(mFile);
					mFileStream.close();
					
					// add the file to rhizome
					Rhizome.addFile(this, mFile.getCanonicalPath());
					
					// delete the file
					mFile.delete();
					
				} catch (FileNotFoundException e) {
					Log.e(TAG, "unable to create temporary location binary file", e);
					status = false;
				} catch (IOException e) {
					Log.e(TAG, "unable to create temporary location binary file", e);
					status = false;
				}
			}
			
			mCursor.close();
		}

		return status;

	}
	
	//delete the photo files
	private void deletePhotoFiles() {
		
		// show the progress bar
		ProgressBar mProgress = (ProgressBar) findViewById(R.id.delete_ui_progress_photos);
		mProgress.setVisibility(View.VISIBLE);
		
		try {
			
			FileUtils.deleteFilesInDir(MediaUtils.getMediaStore(), null);
			
			// hide the progress bar
			mProgress.setVisibility(View.INVISIBLE);
			
			ImageView mImageView = (ImageView) findViewById(R.id.delete_ui_lbl_emo_photos);
			mImageView.setVisibility(View.VISIBLE);	
			
		} catch(IOException e) {
			Log.e(TAG, "unable to delete a data file", e);
			
			// hide the progress bar
			mProgress.setVisibility(View.INVISIBLE);
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
	        mBuilder.setMessage(R.string.delete_ui_dialog_delete_data_fail)
	               .setCancelable(false)
	               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   dialog.cancel();
	                   }
	               });
	        AlertDialog mAlert = mBuilder.create();
	        mAlert.show();
		}
	}	
}
