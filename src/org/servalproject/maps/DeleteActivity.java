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

import org.servalproject.maps.delete.DeleteAsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
		
		// build the arrays
		ProgressBar[] progressBars = new ProgressBar[4];
		
		progressBars[0] = (ProgressBar) findViewById(R.id.delete_ui_progress_location_records);
		progressBars[1] = (ProgressBar) findViewById(R.id.delete_ui_progress_poi_records);
		progressBars[2] = (ProgressBar) findViewById(R.id.delete_ui_progress_data_files);
		progressBars[3] = (ProgressBar) findViewById(R.id.delete_ui_progress_photos);
		
		ImageView[] imageViews = new ImageView[4];
		
		imageViews[0] = (ImageView) findViewById(R.id.delete_ui_lbl_emo_location_records);
		imageViews[1] = (ImageView) findViewById(R.id.delete_ui_lbl_emo_poi_records);
		imageViews[2] = (ImageView) findViewById(R.id.delete_ui_lbl_emo_data_files);
		imageViews[3] = (ImageView) findViewById(R.id.delete_ui_lbl_emo_photos);
		
		DeleteAsyncTask deleteTask = new DeleteAsyncTask(this, progressBars, imageViews); 
		deleteTask.execute();	
	}
}
