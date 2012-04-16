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

import org.servalproject.maps.export.BinaryAsyncTask;
import org.servalproject.maps.export.CsvAsyncTask;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * activity to manage the export of data into other formats
 */
public class ExportActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "ExportActivity";
	
	/*
	 * private class level variables
	 */
	private String selectedFormat = null;
	private String selectedData   = null;
	private ProgressBar progressBar;
	private TextView progressLabel;
	
	private BinaryAsyncTask binaryTask = null;
	private CsvAsyncTask csvTask = null;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export);
        
        // populate the export format spinner
        Spinner mSpinner = (Spinner) findViewById(R.id.export_ui_spinner_format);
        
        // create an array adapter containing the choices
        ArrayAdapter<CharSequence> mFormatEdapter = 
        		ArrayAdapter.createFromResource(
        				this, 
        				R.array.export_ui_formats_intervals, 
        				android.R.layout.simple_spinner_item);
        
        // define how each item will look
        mFormatEdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // add the data to the spinner
        mSpinner.setAdapter(mFormatEdapter);
        
        // use the activity to respond to which item selected
        mSpinner.setOnItemSelectedListener(this);

        // populate the export data spinner
        mSpinner = (Spinner) findViewById(R.id.export_ui_spinner_data);
        
        mFormatEdapter = ArrayAdapter.createFromResource(
        		this, 
        		R.array.export_ui_data_intervals, 
        		android.R.layout.simple_spinner_item);
        
        mFormatEdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mFormatEdapter);
        mSpinner.setOnItemSelectedListener(this);
        
        // capture the touch on the buttons
        Button mButton = (Button) findViewById(R.id.export_ui_btn_export);
        mButton.setOnClickListener(this);
        
        progressBar = (ProgressBar) findViewById(R.id.export_ui_progress_bar);
        progressLabel = (TextView) findViewById(R.id.export_ui_lbl_progress);
        
    }
    
    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		// determine which button was selected
		switch(v.getId()) {
		case R.id.export_ui_btn_export:
			// undertake the export
			if(selectedFormat.equals("Serval Maps Binary File") == true) {
				binaryTask = new BinaryAsyncTask(this, progressBar, progressLabel);
				binaryTask.execute(selectedData);
			} else {
				// export in csv format
				csvTask = new CsvAsyncTask(this, progressBar, progressLabel);
				csvTask.execute(selectedData);
			}
			Log.v(TAG, "selectedFormat: " + selectedFormat);
			Log.v(TAG, "selectedData:   " + selectedData);
			break;
		default:
			Log.w(TAG, "unknown view called onClick method");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if(binaryTask != null) {
			if(binaryTask.getStatus() != AsyncTask.Status.FINISHED) {
				binaryTask.cancel(true);
			}
		}
		
		if(csvTask != null) {
			if(csvTask.getStatus() != AsyncTask.Status.FINISHED) {
				csvTask.cancel(true);
			}
		}
		
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		
		// store the selected value
		switch(parent.getId()) {
		case R.id.export_ui_spinner_format:
			selectedFormat = parent.getItemAtPosition(pos).toString();
			break;
		case R.id.export_ui_spinner_data:
			selectedData = parent.getItemAtPosition(pos).toString();
			break;
		default:
			Log.w(TAG, "unknown view called onItemSelected method");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
