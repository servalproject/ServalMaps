package org.servalproject.mappingservices.services;

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
	 * message to return information about the available map data
	 */
	public static final int MSG_MAP_COVERAGE = 1;
	
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
			switch (msg.what) {
			case MSG_MAP_COVERAGE:
				// send service status back in response to the message
				Messenger mClient = msg.replyTo;
				try{
					Message mMessage = Message.obtain(null, MSG_MAP_COVERAGE, null);
					mMessage.setData(self.getMapCoverage());
					mClient.send(mMessage);
				} catch (RemoteException e) {

				}
				break;
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
	
	// method to get the map data coverage
	private Bundle getMapCoverage() {
		
		// TODO finish the method once the mapsforge library has been updates
		// to support the required functionality
		
		Bundle mBundle = getEmptyBundle();
		
		return mBundle;
		
		/*
		
		// get a handle to the directory where the files are stored
		File mParentDir = new File(MapActivity.MAP_DATA_DIR);
		
		// check to see if we can access the file
		if(mParentDir.canRead() == false || mParentDir.isDirectory() == false) {
			return mBundle;
		}
		
		// get a list of the available files
		File[] mDataFiles = mParentDir.listFiles(new FileFilter());
		
		if(mDataFiles == null || mDataFiles.length == 0) {
			mBundle = new Bundle();
			mBundle.putString("status", "no map data");
			return mBundle;
		}
		
		// loop through the file and process them for info
		MapDatabase mMapData;
		for(int i = 0; i < mDataFiles.length; i++) {
			//mMapData = new MapDatabase();
			//mMapData.openFile(mDataFiles[i].getAbsolutePath());
		}
	
		return null;
		*/
	}
	
	//private method to build an empty bundle
	private Bundle getEmptyBundle() {
		Bundle mBundle = new Bundle();
		mBundle.putString("status", "unknown");
		return mBundle;
	}
}
