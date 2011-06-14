package org.servalproject.mappingservices.services;

import java.io.File;
import java.io.FileFilter;

import org.servalproject.mappingservices.MapActivity;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MapDataService extends Service {
	
	/**
	 * message to return the number of files available
	 */
	public static final int MSG_FILES_AVAILABLE = 1;
	
	/**
	 * message to return the list of map files available
	 */
	public static final int MSG_FILE_LIST = 2;
	
	//TODO add another message type to get metadata info on map files
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MDS";
	
	/*
     * Handler of incoming messages from clients.
     */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			
			Messenger mClient;
			
			switch (msg.what) {
			case MSG_FILES_AVAILABLE:
				// send service status back in response to the message
				mClient = msg.replyTo;
				try{
					Message mMessage = Message.obtain(null, MSG_FILES_AVAILABLE, null);
					mMessage.setData(self.getMapFileCount());
					mClient.send(mMessage);
				} catch (RemoteException e) {
					Log.e(TAG, "Unable to send message back to calling component", e);
				}
				break;
			case MSG_FILE_LIST:
				mClient = msg.replyTo;
				try{
					Message mMessage = Message.obtain(null, MSG_FILE_LIST, null);
					mMessage.setData(self.getMapFileList());
					mClient.send(mMessage);
				} catch (RemoteException e) {
					Log.e(TAG, "Unable to send message back to calling component", e);
				}
				
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private final MapDataService self = this;
    
    /*
     * target for clients to send messages to this service
     */
    public final Messenger mMessenger = new Messenger(new IncomingHandler());
    

	@Override
	public IBinder onBind(Intent intent) {
		
		if(V_LOG) {
			Log.v(TAG, "someone bound to the service");
		}
		
		return mMessenger.getBinder();
	}
	
	// method to get the number of map files available
	private Bundle getMapFileCount() {
		
		// define a default bundle
		Bundle mBundle = new Bundle();
		mBundle.putInt("fileCount", 0);
		
		// get the file list
		String[] mFileList = getFileList();
		
		if(mFileList != null) {
			mBundle = new Bundle();
			mBundle.putInt("fileCount", mFileList.length);
		}
		
		// return the bundle
		return mBundle;
	}
	
	// method to get the list of files
	private Bundle getMapFileList() {
		
		// define the default bundle
		Bundle mBundle = new Bundle();
		mBundle.putInt("fileCount", 0);
		
		// get the file list
		String[] mFileList = getFileList();
		
		if(mFileList != null) {
			mBundle.putStringArray("fileList", mFileList);
		}
		
		return mBundle;
	}
	
	// method to get the list of files
	private String[] getFileList() {
		
		// set the default string
		String[] mFileList = null;
		
		// get a handle to the directory where the files are stored
		File mParentDir = new File(MapActivity.MAP_DATA_DIR);
		
		// check to see if we can access the directory
		if(mParentDir.canRead() == false || mParentDir.isDirectory() == false) {
			return mFileList;
		}
		
		// get a list of the available files
		File[] mDataFiles = mParentDir.listFiles(new MapFileFilter());
		
		if(mDataFiles.length > 0) {
			mFileList = new String[mDataFiles.length];
			
			for(int i = 0; i < mDataFiles.length; i++) {
				mFileList[i] = mDataFiles[i].getName();
			}
		}
		
		return mFileList;
		
	}
	
	// private class to filter a list of files
	// define the file filter class
	private class MapFileFilter implements FileFilter {
		public boolean accept(File pathname) {

			if (pathname.isDirectory()) {
				return false;
			}
			
			if (pathname.canRead() == false) {
				return false;
			}

			String name = pathname.getName().toLowerCase();
			
			if(name.endsWith(".map")) {
				return true;
			} else {
				return false;
			}
		}
	}
}