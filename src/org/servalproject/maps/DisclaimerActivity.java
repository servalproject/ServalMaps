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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.servalproject.maps.batphone.StateReceiver;
import org.servalproject.maps.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * activity to show the disclaimer, it is the start activity
 */
public class DisclaimerActivity extends Activity implements OnClickListener {

	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	//private final String  TAG = "DisclaimerActivity";

	private final int NO_FILES_DIALOG = 0;
	private final int MANY_FILES_DIALOG = 1;
	private final int NO_SERVAL_DIALOG = 2;
	private final int SERVAL_NOT_RUNNING_DIALOG = 3;
	private final int EXTERNAL_STORAGE_NOT_AVAILABLE_DIALOG = 4;
	private final int SERVAL_MESH_NOT_INSTALLED_DIALOG = 5;

	/*
	 * private class level variables
	 */
	private StateReceiver batphoneStateReceiver = null;
	private List<File> mapDataInfoList = new ArrayList<File>();
	private CharSequence[] mFileNames = null;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disclaimer);
		
		// flag to indicate that all of the checks have completed successfully
		boolean mContinue = true;

		// capture the touch on the buttons
		Button mButton = (Button) findViewById(R.id.disclaimer_ui_btn_continue);
		mButton.setOnClickListener(this);

		// check on the state of the storage
		String mStorageState = Environment.getExternalStorageState();

		if(Environment.MEDIA_MOUNTED.equals(mStorageState) == false) {

			// show a dialog and disable the button
			showDialog(EXTERNAL_STORAGE_NOT_AVAILABLE_DIALOG);
			mContinue = false;
		} 

		// check to see if the Serval Mesh software is installed
		try {
			getPackageManager().getApplicationInfo("org.servalproject", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {

			// show a dialog and disable the button
			showDialog(SERVAL_MESH_NOT_INSTALLED_DIALOG);
			mContinue = false;
		}

		if(mContinue) {
			// register the various receivers
			registerReceivers();
		} else {
			mButton.setEnabled(false);
		}
	}
	
	private void findMaps(){
		try{
			File mapFolder = new File(Environment.getExternalStorageDirectory(), 
					getString(R.string.system_path_map_data));
			mapFolder.mkdirs();
			
			ContentResolver resolver = this.getContentResolver();
			Uri manifests = Uri.parse("content://org.servalproject.files/");
			Cursor c = resolver.query(manifests, null, null, new String[]{"file","%.map"}, null);
			
			if (c==null)
				return;
			try{
				int name_col=c.getColumnIndexOrThrow("name");
				int id_col = c.getColumnIndexOrThrow("id");
				while(c.moveToNext()){
					try{
						String name=c.getString(name_col);
						
						File dest = new File(mapFolder, name);
						if (!dest.exists()){
							Log.v("FindMaps", "Copying "+name);
							byte []id=c.getBlob(id_col);
							String id_str = ServalMaps.binToHex(id,0,id.length);
							Uri uri = Uri.parse("content://org.servalproject.files/"+id_str);
							
							FileUtils.copyFile(resolver.openInputStream(uri), dest);
						}
					}catch (Exception e){
						Log.e("FileMaps",e.getMessage(),e);
					}
				}
			}finally{
				c.close();
			}
			Log.v("FindMaps","Building available map list");
			
			File files[] = mapFolder.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".map");
				}
			});
			
			Arrays.sort(files);
			MapDatabase mapDatabase = new MapDatabase();
			
			mapDataInfoList.clear();
			
			for (int i=0;i<files.length;i++){
				if (mapDatabase.openFile(files[i])==FileOpenResult.SUCCESS){
					mapDataInfoList.add(files[i]);
					mapDatabase.closeFile();
				}
			}
			
		}catch (Exception e){
			Log.e("Mapping", e.getMessage(),e);
		}
	}
	
	private void startOpenMap(){
		new AsyncTask<Void,Void,Void>(){
			@Override
			protected Void doInBackground(Void... arg0) {
				findMaps();
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				// show the appropriate dialog
				switch(mapDataInfoList.size()){
				case 0:
					showDialog(NO_FILES_DIALOG);
					return;
				case 1:
					// show the map activity
					showMapActivity(mapDataInfoList.get(0).getAbsolutePath());
					return;
				default:
					// show the map file chooser
					showDialog(MANY_FILES_DIALOG);
				}
			}
		}.execute();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		// check which button was touched
		if(v.getId() == R.id.disclaimer_ui_btn_continue) {

			// check to see if we know the device phone number
			ServalMaps mApplication = (ServalMaps) getApplication();

			if(mApplication.getPhoneNumber() == null || mApplication.getSid() == null) {
				// show the appropriate dialog
				showDialog(NO_SERVAL_DIALOG);
			} else if(mApplication.getBatphoneState() != ServalMaps.BatphoneState.On) {
				// show the appropriate dialog
				showDialog(SERVAL_NOT_RUNNING_DIALOG);
			}else {
				startOpenMap();
			}
		}
	}
	
	/*
	 * create the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu based on the XML
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.disclaimer_activity, menu);
	    return true;
	}
	
	/*
	 * handle click events from the menu
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent mIntent;
		
		switch(item.getItemId()){
		case R.id.menu_disclaimer_activity_download:
			// show the start of the map data download process
			mIntent = new Intent(this, org.servalproject.maps.download.MapMirrorActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_export:
			// show the export activity
			mIntent = new Intent(this, org.servalproject.maps.ExportActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_delete:
			// show the delete activity
			mIntent = new Intent(this, org.servalproject.maps.DeleteActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_help_about:
			// show the help activity
			mIntent = new Intent(this, org.servalproject.maps.AboutActivity.class);
			startActivity(mIntent);
			return true;
		case R.id.menu_disclaimer_activity_close:
			// close this activity
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		// unregister the various receivers
		unRegisterReceivers();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		// register the various receivers
		registerReceivers();
		super.onResume();
	}

	// private method to register the various broadcast receivers
	private void registerReceivers() {

		if(batphoneStateReceiver == null) {
			batphoneStateReceiver = new StateReceiver();
		}
		
		IntentFilter mBroadcastFilter = new IntentFilter();
		mBroadcastFilter.addAction("org.servalproject.ACTION_STATE_CHECK_UPDATE");
		mBroadcastFilter.addAction("org.servalproject.ACTION_STATE");
		registerReceiver(batphoneStateReceiver, mBroadcastFilter);
		
		// send off an intent to poll for the current state of the serval mesh
		Intent mIntent = new Intent("org.servalproject.ACTION_STATE_CHECK");
		startService(mIntent);
		
	}

	// private method to unregister the various broadcast receivers
	private void unRegisterReceivers() {
		if(batphoneStateReceiver != null) {
			unregisterReceiver(batphoneStateReceiver);
		}
	}

	/*
	 * dialog related methods
	 */

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
		case NO_FILES_DIALOG:
			// show an alert dialog
// version 0.3.0 of the mapsforge library doesn't work when rendering a map
// without a map data file
//
// this is different to the behaviour in the of the previous version
//
//			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_files)
//			.setCancelable(false)
//			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int id) {
//					showMapActivity(null);
//				}
//			})
//			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int id) {
//					dialog.cancel();
//				}
//			});
//			mDialog = mBuilder.create();
//			break;
			// show an alert dialog and stop user from continuing
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_files)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
			mDialog = mBuilder.create();
			break;
			
		case MANY_FILES_DIALOG:
			
			mFileNames = new CharSequence[mapDataInfoList.size()];
			for(int i = 0; i < mapDataInfoList.size(); i++) {
				mFileNames[i]=mapDataInfoList.get(i).getName();
			}
			
			// do not show an empty list of files
			if(mFileNames.length != 0) {
				mBuilder.setTitle(R.string.disclaimer_ui_dialog_many_files_title)
				.setCancelable(false)
				.setItems(mFileNames, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						showMapActivity(mFileNames[item].toString());
					}
				});
				mDialog = mBuilder.create();
			}
			break;
		case NO_SERVAL_DIALOG:
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_serval)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
			mDialog = mBuilder.create();
			break;
		case SERVAL_NOT_RUNNING_DIALOG:
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_serval_not_running)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					startOpenMap();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case EXTERNAL_STORAGE_NOT_AVAILABLE_DIALOG:
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_storage)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		case SERVAL_MESH_NOT_INSTALLED_DIALOG:
			mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(R.string.disclaimer_ui_dialog_no_serval_mesh)
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
	 * prepare and show the map activity
	 */
	private void showMapActivity(String mapDataFile) {
		// show the map activity
		Intent mMapIntent = new Intent(this, org.servalproject.maps.MapActivity.class);
		mMapIntent.putExtra("mapFileName", mapDataFile);
		startActivityForResult(mMapIntent, 0);
	}
}