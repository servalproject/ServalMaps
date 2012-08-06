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

package org.servalproject.maps.rhizome;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.servalproject.maps.ServalMaps;
import org.servalproject.maps.protobuf.BinaryFileContract;
import org.servalproject.maps.protobuf.LocationReadWorker;
import org.servalproject.maps.protobuf.PointsOfInterestWorker;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.MediaUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * receives the broadcasts from Rhizome about new files being available
 */
public class RhizomeBroadcastReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "RhizomeBroadcastReceiver";
	
	/*
	 * private class level variables
	 */
	private static final int THREAD_POOL_SIZE = 2;
	
	// keep a static weak reference to a thread pool
	// this should allow it to shutdown when there are no files being processed
	private static volatile WeakReference<ExecutorService> executorRef;
	
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
			
		if(!intent.getAction().equals("org.servalproject.rhizome.RECEIVE_FILE")) {
			Log.e(TAG, "called with an intent with an unexepcted intent action");
			return;
		}
	
		// see if the file is one we want to work with
		String mFileName = intent.getStringExtra("name");
		Uri uri = intent.getData();
		
		if(mFileName == null) {
			Log.e(TAG, "called with an intent missing the 'name' extra");
			return;
		}
	
		// skip files that we sent
		ServalMaps mServalMaps = (ServalMaps) context.getApplicationContext();
		String[] mFileParts = mFileName.split("-");
	
		String mPhoneNumber = mServalMaps.getPhoneNumber();
		mPhoneNumber = mPhoneNumber.replace(" ", "");
		mPhoneNumber = mPhoneNumber.replace("-", "");
		
		if(mFileParts[0].equals(mServalMaps.getPhoneNumber())) { 
			// this doesn't look like one of our own binary files
			return;
		}
	
		// is it one of our images?
		if(mFileName.startsWith(MediaUtils.PHOTO_FILE_PREFIX) && mFileName.endsWith(".jpg")) {
			// this is a serval maps photo
			try {
				// get the destination file name
				File mFileDestination = new File(MediaUtils.getMediaStore(), mFileName);
				
				if(V_LOG) {
					Log.v(TAG, "Extracting image to: " + mFileDestination.getAbsolutePath());
				}
				
				// copy the file
				InputStream mInputStream = context.getContentResolver().openInputStream(uri);
				FileUtils.copyFile(mInputStream, mFileDestination);
				
			} catch (IOException e) {
				Log.e(TAG, "unable to copy file", e);
			}
			return;
		}

		// is this a location binary data file?
		if(mFileName.endsWith(BinaryFileContract.LOCATION_EXT)) {
			if(V_LOG) {
				Log.v(TAG, "Queing location reader for "+mFileName);
			}
			
			// queue the reading of the file
			queue(new LocationReadWorker(context, uri));
			return;
		}
		
		// is this is a POI binary data file?
		if(mFileName.endsWith(BinaryFileContract.POI_EXT) == true) {
			if(V_LOG) {
				Log.v(TAG, "Queing POI reader for "+mFileName);
			}
			
			// queue the reading of the file
			queue(new PointsOfInterestWorker(context, uri));
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
