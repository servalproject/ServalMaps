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
package org.servalproject.maps.export;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.servalproject.maps.R;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * class to undertake an async task to export in binary format
 */
public class CsvAsyncTask extends AsyncTask<String, Integer, Integer> {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String  TAG = "CsvExportTask";
	
	/*
	 * private class level variables
	 */
	private ProgressBar progressBar;
	private TextView    progressLabel;
	private Activity    context;
	
	private boolean updateUI = true;
	private boolean updateForLocation = false;
	private boolean updateForPoi = false;
	
	private CSVFormat csvFormat;
	
	private Integer recordCount = -1;
	
	public CsvAsyncTask(Activity context, ProgressBar progressBar, TextView progressLabel) {
		
		// check the parameters
		if(context == null || progressBar == null || progressLabel == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		this.context = context;
		this.progressBar = progressBar;
		this.progressLabel = progressLabel;
		
		this.csvFormat = CSVFormat.DEFAULT;
		csvFormat.withEscape('\\');
		csvFormat.withCommentStart('#');
		
		if(V_LOG) {
			Log.v(TAG, "class instantiated");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		
		if(V_LOG) {
			Log.v(TAG, "onPreExecute called");
		}
		
		progressLabel.setText("");
		progressLabel.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		
		Button mButton = (Button) context.findViewById(R.id.export_ui_btn_export);
        mButton.setEnabled(false);
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
		
		progressBar.setProgress(progress[0]);
		
		if(updateUI) {
			if(updateForLocation) {
				progressLabel.setText(R.string.export_ui_progress_location);
				updateForLocation = false;
				updateUI = false;
			}
			
			if(updateForPoi) {
				progressLabel.setText(R.string.export_ui_progress_poi);
				updateForPoi = false;
				updateUI = false;
			}
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
		
		// finalse the results
		progressBar.setVisibility(View.INVISIBLE);
		progressBar.setProgress(0);
		progressLabel.setVisibility(View.INVISIBLE);
		
		Button mButton = (Button) context.findViewById(R.id.export_ui_btn_export);
        mButton.setEnabled(true);
        
        String mMessage = String.format(
        		context.getString(R.string.export_ui_finished_msg),
        		recordCount,
        		context.getString(R.string.system_path_export_data));
        
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
  
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Integer doInBackground(String... taskType) {
		
		if(V_LOG) {
			Log.v(TAG, "doInBackground called: " + taskType[0]);
		}
		
		// determine which export task to undertake
		if(taskType[0].equals("All Data") == true) {
			recordCount = doAllExport();
		} else if(taskType[0].equals("All Location Records") == true) {
			recordCount = doLocationExport();
		} else {
			recordCount = doPoiExport();
		}
		
		return recordCount;
	}
	
	// private method to undertake all exports
	private Integer doAllExport() {
		
		if(V_LOG) {
			Log.v(TAG, "doAllExport called: ");
		}
		
		Integer recordCount = doLocationExport();
		recordCount += doPoiExport();
		
		return recordCount;
	}
	
	// private method to undertake a location export
	private Integer doLocationExport() {
		
		if(V_LOG) {
			Log.v(TAG, "doLocationExport called: ");
		}
		
		// reset the progress bar
		progressBar.setProgress(0);
		Integer mRecordCount = 0;
		
		updateUI = true;
		updateForLocation = true;
		
		// get all of the location data
		ContentResolver mContentResolver = context.getApplicationContext().getContentResolver();
		
		// get the content
		Cursor mCursor = mContentResolver.query(
				LocationsContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		// check on what was returned
		if(mCursor.getCount() > 0) {
			
			progressBar.setMax(mCursor.getCount());
			mRecordCount = mCursor.getCount();
			
			// get the export directory 
			// get the path for the output files
			String mOutputPath = Environment.getExternalStorageDirectory().getPath();
			mOutputPath += context.getString(R.string.system_path_export_data);
			
			if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
				Log.e(TAG, "unable to access the required output directory");
				mCursor.close();
				return 0;
			}
			
			// build the output file name
			String mFileName = "serval-maps-export-locations-" + TimeUtils.getToday() + ".csv";
			
			// write the data to the file
			BufferedWriter mOutput = null;
			
			String[] mLine = new String[LocationsContract.Table.COLUMNS.length];
			
			try {
				//mOutput = new BufferedOutputStream(new FileOutputStream(mOutputPath + mFileName, false));
				mOutput = new BufferedWriter(new FileWriter(mOutputPath + mFileName, false));
				
				CSVPrinter mPrinter = new CSVPrinter(mOutput, csvFormat);
				
				// write the comment line
				mPrinter.printComment("Location data sourced from the Serval Maps application");
				mPrinter.printComment("File created: " + TimeUtils.getToday());
				mPrinter.printComment(Arrays.toString(LocationsContract.Table.COLUMNS));
				
				while(mCursor.moveToNext()) {
					
					for(int i = 0; i < LocationsContract.Table.COLUMNS.length; i++) {
						mLine[i] = mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.COLUMNS[i]));
					}
					
					mPrinter.println(mLine);
					
					publishProgress(mCursor.getPosition());
					
					// check to see if we need to cancel this task
					if(isCancelled() == true) {
						break;
					}
				}
				
			} catch (FileNotFoundException e) {
				Log.e(TAG, "unable to open the output file", e);
			} catch (IOException e) {
				Log.e(TAG, "unable to write the message at '" + mCursor.getPosition() + "' in the cursor", e);
			} finally {
				// play nice and tidy up
				try {
					if(mOutput != null) {
						mOutput.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "unable to close the output file", e);
				}
				mCursor.close();
			}
		}
		
		return mRecordCount;
	}
	
	// private method to undetake a POI export
	private Integer doPoiExport() {
		
		// reset the progress bar
		progressBar.setProgress(0);
		Integer mRecordCount = 0;
		
		updateUI = true;
		updateForPoi = true;
		
		if(V_LOG) {
			Log.v(TAG, "doPoiExport called: ");
		}
		
		// get all of the location data
		ContentResolver mContentResolver = context.getApplicationContext().getContentResolver();
		
		// get the content
		Cursor mCursor = mContentResolver.query(
				PointsOfInterestContract.CONTENT_URI, 
				null, 
				null, 
				null, 
				null);
		
		// check on what was returned
		if(mCursor.getCount() > 0) {
			
			progressBar.setMax(mCursor.getCount());
			mRecordCount = mCursor.getCount();
			
			// get the export directory 
			// get the path for the output files
			String mOutputPath = Environment.getExternalStorageDirectory().getPath();
			mOutputPath += context.getString(R.string.system_path_export_data);
			
			if(FileUtils.isDirectoryWritable(mOutputPath) == false) {
				Log.e(TAG, "unable to access the required output directory");
				mCursor.close();
				return 0;
			}
			
			// build the output file name
			String mFileName = "serval-maps-export-pois-" + TimeUtils.getToday() + ".csv";
			
			// write the data to the file
			BufferedWriter mOutput = null;
			
			String[] mLine = new String[PointsOfInterestContract.Table.COLUMNS.length];
			
			try {
				//mOutput = new BufferedOutputStream(new FileOutputStream(mOutputPath + mFileName, false));
				mOutput = new BufferedWriter(new FileWriter(mOutputPath + mFileName, false));
				
				CSVPrinter mPrinter = new CSVPrinter(mOutput, csvFormat);
				
				// write the comment line
				mPrinter.printComment("Location data sourced from the Serval Maps application");
				mPrinter.printComment("File created: " + TimeUtils.getToday());
				mPrinter.printComment(Arrays.toString(PointsOfInterestContract.Table.COLUMNS));
				
				while(mCursor.moveToNext()) {
					
					for(int i = 0; i < PointsOfInterestContract.Table.COLUMNS.length; i++) {
						mLine[i] = mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.COLUMNS[i]));
					}
					
					mPrinter.println(mLine);
					
					publishProgress(mCursor.getPosition());
					
					// check to see if we need to cancel this task
					if(isCancelled() == true) {
						break;
					}
				}
				
			} catch (FileNotFoundException e) {
				Log.e(TAG, "unable to open the output file", e);
			} catch (IOException e) {
				Log.e(TAG, "unable to write the message at '" + mCursor.getPosition() + "' in the cursor", e);
			} finally {
				// play nice and tidy up
				try {
					if(mOutput != null) {
						mOutput.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "unable to close the output file", e);
				}
				mCursor.close();
			}
		}
		
		return mRecordCount;
	}
}
