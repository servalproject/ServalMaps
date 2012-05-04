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

package org.servalproject.maps.delete;

import java.io.IOException;

import org.servalproject.maps.R;
import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.MediaUtils;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

/*
 * task to delete the data to free up the UI thread
 */
public class DeleteAsyncTask extends AsyncTask<Void, Integer, Boolean> {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "DeleteTask";
	
	/*
	 * private class level variables
	 */
	private ProgressBar[] progressBars;
	private ImageView[]   imageViews;
	private Context       context;
	
	/**
	 * construct a new DeleteAsyncTask object
	 * 
	 * elements in the array are assumed to be in the same order as the delete tasks undertaken
	 * 
	 * @param progressBars an array of progress bars used to keep the user informed
	 * @param imageViews an array of image views used to keep the user informed
	 * @param context a context used to retrieve content resolvers etc. 
	 */
	public DeleteAsyncTask(Context context, ProgressBar[] progressBars, ImageView[] imageViews) {
		
		this.progressBars = progressBars;
		this.imageViews = imageViews;
		this.context = context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
    protected void onProgressUpdate(Integer... progress) {
		
		if(V_LOG) {
			Log.v(TAG, "onProgressUpdate called: " + progress[0].toString());
		}
 
		// update the progress bar
		super.onProgressUpdate(progress[0]);
		
		//determine what action to undertake
		switch(progress[0]) {
		case 1:
			progressBars[0].setVisibility(View.VISIBLE);
			break;
		case 2:
			progressBars[0].setVisibility(View.INVISIBLE);
			imageViews[0].setVisibility(View.VISIBLE);
			break;
		case 3:
			progressBars[1].setVisibility(View.VISIBLE);
			break;
		case 4:
			progressBars[1].setVisibility(View.INVISIBLE);
			imageViews[1].setVisibility(View.VISIBLE);
			break;
		case 5:
			progressBars[2].setVisibility(View.VISIBLE);
			break;
		case 6:
			progressBars[2].setVisibility(View.INVISIBLE);
			imageViews[2].setVisibility(View.VISIBLE);
			break;
		case 7:
			progressBars[3].setVisibility(View.VISIBLE);
			break;
		case 8:
			progressBars[3].setVisibility(View.INVISIBLE);
			imageViews[3].setVisibility(View.VISIBLE);
			break;
		default:
			Log.w(TAG, "unknown progress indicator detected");
		}
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
    protected void onPostExecute(Boolean result) {
		
		if(V_LOG) {
			Log.v(TAG, "onPostExecute called: ");
		}
		
		// ensure all progress bars are hidden
		for(ProgressBar progressBar: progressBars) {
			progressBar.setVisibility(View.INVISIBLE);
		}
		
		// TODO check to see if everything went OK
		String mMessage = null;
		
		if(result) {
			// success
			mMessage = context.getString(R.string.delete_ui_dialog_delete_data_success);
		} else {
			// failure
			mMessage = context.getString(R.string.delete_ui_dialog_delete_data_fail);
		}
		
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setMessage(mMessage)
               .setCancelable(false)
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dialog.cancel();
                   }
               });
        AlertDialog mAlert = mBuilder.create();
        mAlert.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		// delete the location records
		boolean status01 = deleteLocationRecords();
		
		// delete the poi records
		boolean status02 = deletePoiRecords();
		
		// delete the data files
		boolean status03 = deleteDataFiles();
		
		// delete the photo files
		boolean status04 = deletePhotoFiles();
		
		if(status01 && status02 && status03 && status04) {
			return true;
		} else {
			return false;
		}
	}
	
	// delete the location records
	private boolean deleteLocationRecords() {
		
		// update the UI
		publishProgress(1);
		
		// delete the location records
		ContentResolver mContentResolver = context.getContentResolver();
		
		try {
			mContentResolver.delete(
					LocationsContract.CONTENT_URI, 
					null, 
					null);
		} catch (SQLException e) {
			Log.e(TAG, "unable to delete location records", e);
			return false;
		}
		
		// update the UI
		publishProgress(2);
		
		return true;
	}
	
	// delete the poi records
	private boolean deletePoiRecords() {
		
		// update the UI
		publishProgress(3);
		
		// delete the location records
		ContentResolver mContentResolver = context.getContentResolver();
		
		try {
			mContentResolver.delete(
					LocationsContract.CONTENT_URI, 
					null, 
					null);
		} catch (SQLException e) {
			Log.e(TAG, "unable to delete poi records", e);
			return false;
		}
		
		// update the UI
		publishProgress(4);
		
		return true;
	}
	
	// delete the data files
	private boolean deleteDataFiles() {
		
		// update the UI
		publishProgress(5);
		
		try {
			
// TODO reconsider how data is removed from rhizome			
//			// delete the files in Rhizome
//			if(deleteFromRhizome() == false) {
//				
//				return false;
//				
//			}
			
			String mExternal = Environment.getExternalStorageDirectory().getCanonicalPath() + "/";
			
			// build a list of extensions
			String[] mExtensions = new String[BinaryFileContract.EXTENSIONS.length + 2];
			
			for(int i = 0; i < BinaryFileContract.EXTENSIONS.length; i++) {
				mExtensions[i] = BinaryFileContract.EXTENSIONS[i];
			}
			
			mExtensions[mExtensions.length -1] = ".json";
			mExtensions[mExtensions.length -2] = ".zip";
			
			FileUtils.deleteFilesInDir(mExternal + context.getString(R.string.system_path_binary_data), mExtensions);
			
		} catch(IOException e) {
			Log.e(TAG, "unable to delete a data file", e);
			
			return false;
		}
		
		// update the UI
		publishProgress(6);
		
		return true;
	}
	
	//delete the photo files
	private boolean deletePhotoFiles() {
		
		// update the UI
		publishProgress(7);
		
		try {
			
			FileUtils.deleteFilesInDir(MediaUtils.getMediaStore(), null);
			
		} catch(IOException e) {
			Log.e(TAG, "unable to delete a data file", e);
			
			return false;
		}
		
		// update the UI
		publishProgress(8);
		return true;
	}

}
