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
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.servalproject.mappingservices;

import org.servalproject.mappingservices.services.MappingDataService;

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

/**
 * Primary activity for the Serval Mapping Services app
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class MappingServices extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MV";
	
	/*
	 * private class level variables
	 */
	private Messenger serviceMessenger = null;
	private boolean isBound;
	
    /*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // associate the buttons with our event listener
        Button button = (Button)findViewById(R.id.btn_start_service);
        button.setOnClickListener(this);
        
        button = (Button)findViewById(R.id.btn_status_service);
        button.setOnClickListener(this);
        
        button = (Button)findViewById(R.id.btn_launch_map_activity);
        button.setOnClickListener(this);
    }
    
    /*
     * class to handle the incoming messages from the service
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MappingDataService.MSG_SERVICE_STATUS:
                	Bundle bundle = msg.getData();
                	//debug code
                	Log.v(TAG, "location thread: " + bundle.getString("locationThread"));
                	Log.v(TAG, "incident thread: " + bundle.getString("incidentThread"));
                	Log.v(TAG, "location packets: " + bundle.getInt("locationCount"));
                	Log.v(TAG, "incident packets: " + bundle.getInt("incidentCount"));
                    break;
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
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		// called when the connection is made
    		serviceMessenger = new Messenger(service);
    		
    	}

    	public void onServiceDisconnected(ComponentName className) {
    		// called when the connection is lost
    		serviceMessenger = null;
    	}
    };
    
    /*
     * a method to bind to the service
     */
    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MappingServices.this, MappingDataService.class), connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {

            // Detach our existing connection.
            unbindService(connection);
            isBound = false;
        }
    }

    /*
     * called when one of the buttons is touched
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "button touched");
		}
		
		//TODO work out a better way of handling these events
		
		// determine which button was clicked
		if(v.getId() == R.id.btn_start_service) {
			// start button was touched
			Intent intent = new Intent(this, org.servalproject.mappingservices.services.MappingDataService.class);
			startService(intent);
			
			
		} else if(v.getId() == R.id.btn_status_service) {
			// bind to the service
			doBindService();
			
			// only try to bind to the service if it is available
			if(serviceMessenger != null) {
				
	    		try {
	    			// send a message to the service to see what its status is
	    			Message msg = Message.obtain(null, MappingDataService.MSG_SERVICE_STATUS);
	    			msg.replyTo = messenger;
	    			serviceMessenger.send(msg);
	
	    		} catch (RemoteException e) {
	    			// only log while in development 
	    			if(V_LOG) {
	    				Log.v(TAG, "unable to send a message to the MappingDataService", e);
	    			}
	    		}
	    		
	    		// unbind the service
	    		doUnbindService();
			} else {
				// service is not bound at this time
				if(V_LOG) {
    				Log.v(TAG, "service is not bound at this time");
    			}
			}
		} else if(v.getId() == R.id.btn_launch_map_activity) {
			Intent intent = new Intent(MappingServices.this, MapActivity.class);
	        startActivity(intent);
		}
	}
}