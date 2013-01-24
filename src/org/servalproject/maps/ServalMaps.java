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
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.protobuf.LocationReadWorker;
import org.servalproject.maps.protobuf.PointsOfInterestWorker;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * extends the base application to enable sharing of information
 * between components
 */
public class ServalMaps extends Application {
	
	public final String TAG = "ServalMaps";
	private static final int THREAD_POOL_SIZE = 2;
	private final boolean V_LOG = true;
	
	// keep a static weak reference to a thread pool
	// this should allow it to shutdown when there are no files being processed
	private static volatile WeakReference<ExecutorService> executorRef;
	
	
	/**
	 * an enum representing the different states of the Serval Mesh software
	 * derived from the org.serval.project.ServalBatphoneApplication class
	 */
	public static enum BatphoneState{
		Installing,
		Upgrading,
		Off,
		Starting,
		On,
		Stopping,
		Broken
	}

	/*
	 * class level variables
	 */
	private String phoneNumber = null;
	private String sid = null;
	private BatphoneState state = null;

	/**
	 * set the current state of the Serval Mesh
	 * @param state the current state of the Serval Mesh
	 * @throws IllegalArgumentException if the new state value is null
	 */
	public void setBatphoneState(BatphoneState state) {
		if(state == null) {
			throw new IllegalArgumentException("the state parameter cannot be null");
		}
		
		this.state = state;
		
	}
	
	private void getPrimaryDetails(){
		if (phoneNumber!=null)
			return;
		IntentFilter filter = new IntentFilter();
		filter.addAction("org.servalproject.SET_PRIMARY");
		Intent intent=this.registerReceiver(null, filter);
		phoneNumber = intent.getStringExtra("did");
		sid = intent.getStringExtra("sid");
		
		if (phoneNumber==null)
			Log.e(TAG, "unable to retrieve Serval Mesh phone number");
		if (sid==null)
			Log.e(TAG, "unable to retrieve the Serval Mesh sid");
	}
	/**
	 * Get the phone number of the device as reported by Serval 
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		getPrimaryDetails();
		return phoneNumber;
	}

	/**
	 * Get the sid of the device as reported by Serval 
	 * 
	 * @return the sid
	 */
	public String getSid() {
		getPrimaryDetails();
		return sid;
	}
	
	/**
	 * return the current known state of the Serval Mesh
	 * @return the current known state of the Serval Mesh
	 */
	public BatphoneState getBatphoneState() {
		return state;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Application#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		
		Log.v(TAG, "onLowMemory method called");
		
	}
	
	public void setLastRefresh(long value){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = prefs.edit();
		e.putLong("last_refresh", value);
		e.commit();
	}
	
	public long getLastRefresh(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getLong("last_refresh", -1);
	}
	
	public static String binToHex(byte[] buff, int offset, int len) {
		char[] ret = new char[len*2];
		int j=0;
		for (int i = 0; i < len; i++) {
			ret[j++]=Character.forDigit(((buff[i + offset]) & 0xf0) >> 4, 16);
			ret[j++]=Character.forDigit((buff[i + offset]) & 0x0f, 16);
		}
		return new String(ret);
	}
	
	public Uri findPhoto(String name){
		Uri manifests = Uri.parse("content://org.servalproject.files/");
		Cursor c = this.getContentResolver().query(manifests, null, null, new String[]{"file",name}, null);
		if (c==null)
			return null;
		try{
			int id_col = c.getColumnIndexOrThrow("id");
			if (!c.moveToNext())
				return null;
			byte []id=c.getBlob(id_col);
			String id_str = binToHex(id,0,id.length);
			return Uri.parse("content://org.servalproject.files/"+id_str);
		}finally{
			c.close();
		}
	}
	
	private void refreshType(String type){
		Uri manifests = Uri.parse("content://org.servalproject.files/");
		Cursor c = this.getContentResolver().query(manifests, null, null, new String[]{"file","%"+type}, null);
		if (c==null)
			return;
		try{
			int name_col=c.getColumnIndexOrThrow("name");
			int id_col = c.getColumnIndexOrThrow("id");
			while(c.moveToNext()){
				String name=c.getString(name_col);
				byte []id=c.getBlob(id_col);
				String id_str = binToHex(id,0,id.length);
				Uri uri = Uri.parse("content://org.servalproject.files/"+id_str);
				processFile(name, uri);
			}
		}finally{
			c.close();
		}
	}
	
	public void fullRefresh(){
		this.setLastRefresh(System.currentTimeMillis());
		refreshType(BinaryFileContract.POI_EXT);
		refreshType(BinaryFileContract.LOCATION_EXT);
	}
	
	public void processFile(String fileName, Uri uri){
		// see if the file is one we want to work with
		
		if (fileName==null){
			Log.e(TAG, "filename is null");
			return;
		}
		
		// skip files if we have the log file we produced.
		File dataPath= new File(Environment.getExternalStorageDirectory().getPath(), this.getString(R.string.system_path_binary_data));
		if (dataPath.exists())
			return;
		
		// is this a location binary data file?
		if(fileName.endsWith(BinaryFileContract.LOCATION_EXT)) {
			if(V_LOG) {
				Log.v(TAG, "Queing location reader for "+fileName);
			}
			
			// queue the reading of the file
			queue(new LocationReadWorker(this, uri));
			return;
		}
		
		// is this is a POI binary data file?
		if(fileName.endsWith(BinaryFileContract.POI_EXT) == true) {
			if(V_LOG) {
				Log.v(TAG, "Queing POI reader for "+fileName);
			}
			
			// queue the reading of the file
			queue(new PointsOfInterestWorker(this, uri));
			return;
		}
	}
	
	/*
	 * queue the runnable that will process the file
	 */
	private void queue(Runnable r){
		ExecutorService mExecutorService = null;
		
		// use existing service if 
		if (executorRef != null){
			mExecutorService = executorRef.get();
		}
		
		if (mExecutorService == null){
			mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
			executorRef = new WeakReference<ExecutorService>(mExecutorService);
		}
		
		mExecutorService.submit(r);
	}
}
