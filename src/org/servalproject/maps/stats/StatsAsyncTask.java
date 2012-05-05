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

package org.servalproject.maps.stats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.servalproject.maps.R;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.HttpUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * a class to undertake the collection and upload of statistics
 */
public class StatsAsyncTask extends AsyncTask<Void, Integer, Integer> {
	
	/*
	 * private class level constants
	 */
	private final int COLLECTING_STATS = 0;
	private final int UPLOADING_STATS = 1;
	
	private final int COLLECTION_FAILED = 100;
	private final int UPLOAD_FAILED = 101;
	private final int UPLOAD_SUCCESS = 102;
	
	private final boolean V_LOG = true;
	private final String  TAG = "StatsTask";
	
	/*
	 * private class level variables
	 */
	private ProgressBar progressBar;
	private TextView progressText;
	private String[] stats;
	private String[] labels;
	private Context context;
	
	/**
	 * construct a new StatsAsyncTask
	 * 
	 * @param progressBar a ProgressBar used to indicate that the task is running
	 * @param progressText a TextView used to display progress text
	 * @param stats an array containing statistics
	 * @param labels an array containing labels for the statistics values
	 * @param context a context to be used to gain access to system resources
	 * 
	 * @throws IllegalArgumentException if any of the parameters is missing
	 */
	public StatsAsyncTask(ProgressBar progressBar, TextView progressText, String[] stats, String[] labels, Context context) {
		super();
		
		// check on the parameters
		if(progressBar == null) {
			throw new IllegalArgumentException("the progressBar parameter is required");
		}
		
		if(progressText == null) {
			throw new IllegalArgumentException("the progressText parameter is required");
		}
		
		if(stats == null || stats.length == 0) {
			throw new IllegalArgumentException("the stats array parameter is required");
		}
		
		if(labels == null || labels.length == 0) {
			throw new IllegalArgumentException("the labels array parameter is required");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		this.progressBar = progressBar;
		this.progressText = progressText;
		this.stats = stats;
		this.labels = labels;
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
 
		// update the progress related views
		super.onProgressUpdate(progress[0]);
		
		switch(progress[0]) {
		case COLLECTING_STATS:
			progressBar.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.VISIBLE);
			progressText.setText(context.getString(R.string.stats_ui_progress_collecting));
			break;
		case UPLOADING_STATS:
			progressText.setText(context.getString(R.string.stats_ui_progress_uploading));
			break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
    protected void onPostExecute(Integer result) {
		
		if(V_LOG) {
			Log.v(TAG, "onPostExecute called: ");
		}
		
		// hide the progress related views
		progressBar.setVisibility(View.INVISIBLE);
		progressText.setVisibility(View.INVISIBLE);
		
		String mMessage = null;
		
		switch(result) {
		case COLLECTION_FAILED:
			mMessage = context.getString(R.string.stats_ui_dialog_zip_fail);
			break;
		case UPLOAD_FAILED:
			mMessage = context.getString(R.string.stats_ui_dialog_upload_fail);
			break;
		case UPLOAD_SUCCESS:
			//mMessage = context.getString(R.string.stats_ui_dialog_upload_success);
			mMessage = String.format(
					context.getString(R.string.stats_ui_dialog_upload_success), 
					context.getString(R.string.system_path_export_data)
				);
			break;
		}
		
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
		
		mBuilder.setMessage(mMessage)
		.setCancelable(false)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		mBuilder.create().show();
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(Void... arg0) {
		
		// create the zip file
		File mZipFile;
		publishProgress(COLLECTING_STATS);
		
		try {
			
			File[] mFiles = new File[2];
			
			mFiles[0] = createStatsFile();
			
			mFiles[1] = createPrefsFile();
			
			mZipFile = createZipFile(mFiles);
			
			// copy the zip file to the export directory for transparency
			String mExportPath = Environment.getExternalStorageDirectory().getPath();
			mExportPath += context.getString(R.string.system_path_export_data);
			
			FileUtils.copyFileToDir(mZipFile.getCanonicalPath(), mExportPath);
			
		} catch(IOException e) {
			Log.e(TAG, "creation of the zip file failed", e);
			return COLLECTION_FAILED;
		}
		
		// upload the zip file
		publishProgress(UPLOADING_STATS);
		
		try {
			String mResponse = HttpUtils.doHttpUpload(mZipFile, context.getString(R.string.system_url_file_upload));
			
			if(mResponse.contains("error") == true) {
				Log.e(TAG, "upload of the zip file failed");
				return UPLOAD_FAILED;
			}
		}catch (IOException e) {
			Log.e(TAG, "upload of the zip file failed", e);
			return UPLOAD_FAILED;
		}
		
		return UPLOAD_SUCCESS;
	}
	
	private File createStatsFile() throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = context.getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/stats.txt");
		
		PrintWriter mPrinter = new PrintWriter(new FileWriter(mOutputFile, false));
		
		mPrinter.println("Data Collected: " + TimeUtils.getTodayWithTime());
		
		for(int i = 0; i < stats.length; i++) {
			mPrinter.println(labels[i] + " " + stats[i]);
		}
		
		mPrinter.close();
		
		Log.d(TAG, mOutputFile.getCanonicalPath());

		// return the file handle
		return mOutputFile;		
	}
	
	private File createPrefsFile() throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = context.getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/prefs.txt");
		
		// cast the context to an activity to use getBaseContext method
		Activity mActivity = (Activity) context;
		
		// get a handle on the important shared preferences
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
		Map<String, ?> mPrefMap = mPreferences.getAll();
		
		// open the file
		PrintWriter mPrinter = new PrintWriter(new FileWriter(mOutputFile, false));
		
		for (Map.Entry<String, ?> mEntry: mPrefMap.entrySet()) {
			mPrinter.println(mEntry.getKey() + ": " + mEntry.getValue().toString());
		}
		
		mPrinter.close();
		
		Log.d(TAG, mOutputFile.getCanonicalPath());

		// return the file handle
		return mOutputFile;	
		
	}

	private File createZipFile(File[] fileList) throws IOException {
		
		// get a temp directory to create the file
		File mCacheDir = context.getCacheDir();
		
		// get the output file
		File mOutputFile = new File(mCacheDir.getCanonicalPath() + "/statistics.zip");
		ZipOutputStream mOutputStream = new ZipOutputStream(new FileOutputStream(mOutputFile));
		
		for(File mFile : fileList) {
			// add the file to the zip file
			addFileToZip(mOutputStream, mFile);
		}
		
		mOutputStream.close();
		
		return mOutputFile;
	}
	
	private void addFileToZip(ZipOutputStream outputStream, File inputFile) throws IOException {
		
		int mLength;
		byte[] mBuffer = new byte[1024];
		
		// add the file
		outputStream.putNextEntry(new ZipEntry(inputFile.getName()));
		
		FileInputStream mInputStream = new FileInputStream(inputFile);
		
		 while((mLength = mInputStream.read(mBuffer)) > 0)
         {
			 outputStream.write(mBuffer, 0, mLength);
         }
		 
		 outputStream.closeEntry();
		 mInputStream.close();
	}
}
