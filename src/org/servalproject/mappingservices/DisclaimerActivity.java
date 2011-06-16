/*
 * This file is part of the Serval Mapping Services app.
 *
 *  Serval Mapping Services app is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  Serval Mapping Services app is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Serval Mapping Services app.  
 *  If not, see <http://www.gnu.org/licenses/>
 */
package org.servalproject.mappingservices;

import org.servalproject.mappingservices.services.MapDataService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DisclaimerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
//	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-DA";
	
	/*
	 * private class level variables
	 */
	private Button continueButton;
	private DisclaimerActivity self;
	
	/*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer_activity);
        
        self = this;
        
        // associate the button with our event listener
        continueButton = (Button)findViewById(R.id.btn_disclaimer_continue);
        continueButton.setClickable(false);
        continueButton.setOnClickListener(self);
        
        // bind to the service
        bindService(new Intent(this, MapDataService.class), connection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
	public void onClick(View v) {
		// determine which button was touched
		if(v.getId() == R.id.btn_disclaimer_continue) {
			
			// start the service and show the map
			Intent mServiceIntent = new Intent(this, org.servalproject.mappingservices.services.CoreMappingService.class);
			startService(mServiceIntent);
			
			// start the map activity and be informed when it finishes 
			Intent mMapIntent = new Intent(this, org.servalproject.mappingservices.MapActivity.class);
	        startActivityForResult(mMapIntent, 0);
		}
	}
	
	/*
	 * Receive notification when the map activity finishes
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// get the result code back from the map activity
	    if(resultCode == 0) {
	    	// shutdown the service
	    	Intent mServiceIntent = new Intent(this, org.servalproject.mappingservices.services.CoreMappingService.class);
			stopService(mServiceIntent);
			
			unbindService(connection);
	    	
	        finish();
	    }
	}
	
	/*
     * class to handle the incoming messages from the service
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MapDataService.MSG_FILE_LIST:
            	Bundle mBundle = msg.getData();
            	
            	if(mBundle.getInt("fileCount") > 0) {
            		// check to see if the list contains the file we expect
            		boolean mFound = false;
            		String[] mFileList = mBundle.getStringArray("fileList");
            		
            		for(int i = 0; i < mFileList.length; i++) {
            			if(mFileList[i].equals(MapActivity.MAP_DATA_FILE) == true) {
            				mFound = true;
            				continue;
            			}
            		}
            		
            		if(mFound == false) {
            			//TODO be gentler about the fact that map data cannot be found
                		Toast.makeText(self.getApplicationContext(), String.format(self.getString(R.string.disclaimer_missing_map_file_error), MapActivity.MAP_DATA_FILE) , Toast.LENGTH_LONG).show();
                		self.onActivityResult(0, 0, null);
            		}
            	} else {
            		// show toast and exit
            		//TODO be gentler about the fact that map data cannot be found
            		Toast.makeText(self.getApplicationContext(), R.string.disclaimer_no_map_file_error, Toast.LENGTH_LONG).show();
            		self.onActivityResult(0, 0, null);
            	}
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /*
     * Messenger object that the service can use to send replies
     */
    private final Messenger messenger = new Messenger(new IncomingHandler());
    
    /*
     * ServiceConnection class that represents a collection to the MappindDataService
     */
    private ServiceConnection connection = new ServiceConnection() {
    	/*
    	 * (non-Javadoc)
    	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
    	 */
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		// called when the connection is made
    		Messenger mMessenger = new Messenger(service);
    		
    		try {
    			// send a message to the service to see what its status is
    			Message msg = Message.obtain(null, MapDataService.MSG_FILE_LIST);
    			msg.replyTo = messenger;
    			mMessenger.send(msg);

    		} catch (RemoteException e) {
    			Log.e(TAG, "unable to send a message to the MapDataService", e);
    		}
    	}

    	/*
    	 * (non-Javadoc)
    	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
    	 */
    	@Override
    	public void onServiceDisconnected(ComponentName className) {
    		
    	}
    };

}
