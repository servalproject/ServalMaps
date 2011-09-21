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

import java.util.Arrays;

import org.servalproject.mappingservices.services.MapDataService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * An activity that displays a disclaimer when the application starts
 *
 */
public class DisclaimerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-DA";
	
	/*
	 * private class level variables
	 */
	private Button continueButton;
	private DisclaimerActivity self;
	private String mapFileName = null;
	private String[] mapFileNames = null;
	
	/*
	 * private level constants
	 */
	private final int DIALOG_OK_CONTINUE = 0;
	private final int DIALOG_MAP_FILE_CHOOSER = 1;
	
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
        
        // get the phone number and sid from the Serval sticky
        BatphoneBroadcast mBroadcast = new BatphoneBroadcast(this.getApplicationContext());
        IntentFilter mBroadcastFilter = new IntentFilter("org.servalproject.SET_PRIMARY");
        //mBroadcastFilter.addDataScheme("tel");
        this.getApplicationContext().registerReceiver(mBroadcast, mBroadcastFilter);
    }
	
	@Override
	public void onClick(View v) {
		// determine which button was touched
		if(v.getId() == R.id.btn_disclaimer_continue) {
			
			// determine what action to take based on the number of files available
			if(mapFileName == null && mapFileNames == null) {
				// show a dialog asking for confirmation to continue when no map files are available
				showDialog(DIALOG_OK_CONTINUE);
			} else if(mapFileName != null) {
				// show the map with the only file name available
				showMapActivity(mapFileName);
			} else {
				// show a dialog to select which map data file to use
				showDialog(DIALOG_MAP_FILE_CHOOSER);
			}
		}
	}
	
	/*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog mDialog = null;
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
    	
    	// determine which dialog to create
    	switch(id) {
    	case DIALOG_OK_CONTINUE:
    		// show the OK to continue without data dialog
    		mBuilder.setMessage(this.getString(R.string.disclaimer_no_map_file_msg));
    		mBuilder.setCancelable(false);
    		
    		mBuilder.setPositiveButton(this.getString(R.string.map_alert_yes), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				DisclaimerActivity.this.showMapActivity(null);
    			}
    		});
    		
    		mBuilder.setNegativeButton(this.getString(R.string.map_alert_no), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				dialog.cancel();
    			}
    		});
    		
    		mDialog = mBuilder.create();
    		break;
    	case DIALOG_MAP_FILE_CHOOSER:
    		// show the list of files to choose from
    		mBuilder.setTitle(this.getString(R.string.disclaimer_choose_map_file_msg));
    		mBuilder.setCancelable(false);
    		
    		mBuilder.setItems(mapFileNames, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		    	DisclaimerActivity.this.showMapActivity(mapFileNames[item]);
    		    }
    		});
    	
    		mDialog = mBuilder.create();
    		break;
	    default:
			mDialog = null;
		}
		
		return mDialog;
	}
	
	// private method to go to the map activity
	private void showMapActivity(String mapFileName) {
		
		// start the services
		Intent mServiceIntent = new Intent(this, org.servalproject.mappingservices.services.CoreMappingService.class);
		startService(mServiceIntent);
		
		// show the map
		Intent mMapIntent = new Intent(this, org.servalproject.mappingservices.MapActivity.class);
		mMapIntent.putExtra("mapFileName", mapFileName); // pass the name of the map data file to the activity
		startActivityForResult(mMapIntent, 0); // be informed when the activity finishes
		
		
		// start the map activity and be informed when it finishes 
//		Intent mMapIntent = new Intent(this, org.servalproject.mappingservices.MapActivity.class);
//        startActivityForResult(mMapIntent, 0);
		
		
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
            	
            	// see how many possible files are available
            	if(mBundle.getInt("fileCount") == 1) {
            		
            		String[] mFileList = mBundle.getStringArray("fileList");
            		mapFileName = mFileList[0];
            		
            	} else if(mBundle.getInt("fileCount") > 0) {
            		
            		mapFileNames = mBundle.getStringArray("fileList");
            		
            		if(V_LOG) {
            			Log.v(TAG, Arrays.toString(mapFileNames));
            		}
            		
            	}
            	
            	/*
            	
            	if(mBundle.getInt("fileCount") > 0) {
            		// check to see if the list contains the file we expect
            		boolean mFound = false;
            		String[] mFileList = mBundle.getStringArray("fileList");
            		
            		
//            		
//            		for(int i = 0; i < mFileList.length; i++) {
//            			if(mFileList[i].equals(MapActivity.MAP_DATA_FILE) == true) {
//            				mFound = true;
//            				continue;
//            			}
//            		}
//            		
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
            	*/
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
    
    /*
     * broadcast receiver to receive the sticky broadcast from Serval batphone
     * containing the phone number and sid
     */
    private class BatphoneBroadcast extends BroadcastReceiver {
    	
    	/*
    	 * private class level variables
    	 */
    	private MappingServicesApplication context;
    	
    	/**
    	 * construct a new BatphoneBroadcast class which receives the sticky broadbast from
    	 * the serval batphone software
    	 * 
    	 * @param context the application context
    	 */
    	public BatphoneBroadcast(Context context) {
    		super();
    		this.context = (MappingServicesApplication)context;
    	}
    	
    	/*
    	 * (non-Javadoc)
    	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
    	 */
		@Override
		public void onReceive(Context context, Intent intent) {
			
			this.context.setPhoneNumber(intent.getStringExtra("did"));
			this.context.setSid(intent.getStringExtra("sid"));
		}
        
    }

}
