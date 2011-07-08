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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.servalproject.mappingservices.services.CoreMappingService;

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
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity that shows the status of the core mapping services
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class StatusActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-SA";
	
	/*
	 * private class level variables
	 */
	private TextView statusText;
	private Messenger serviceMessenger = null;
	
	/*
	 * Called when the activity is first created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_activity);
		
		// associate the buttons with our event listener
        Button button = (Button)findViewById(R.id.btn_status_continue);
        button.setOnClickListener(this);
        
        // setup the text view so it is scrollable
        statusText = (TextView)findViewById(R.id.lbl_status_text);
        statusText.setMovementMethod(ScrollingMovementMethod.getInstance());
        
		if(V_LOG) {
			Log.v(TAG, "activity started");
   	 	}
		
		// bind to the service
		doBindService();
	}
	
	/*
     * class to handle the incoming messages from the service
     */
    private class IncomingHandler extends Handler {
        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
    	@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CoreMappingService.MSG_SERVICE_STATUS:
                	Bundle mBundle = msg.getData();
                	
                	Set<String> mKeys = mBundle.keySet();
                	TreeSet<String> mSortedKeys = new TreeSet<String>(mKeys);
                	Iterator<String> mIterator = mSortedKeys.iterator();
                	
                	String mThreads  = "<br/><b>Threads</b><br/>";
                	String mPackets  = "<br/><b>Packets Received</b><br/>";
                	String mBatman   = "<br/><b>Serval</b><br/>";
                	String mDatabase = "<br/><b>Databases</b><br/>";
                	
                	String mKey;
                	String mTokens[];
                	
                	
                	while (mIterator.hasNext()) {
                		mKey = mIterator.next();
                		
                		if(mKey.startsWith("thread")) {
                			mTokens = mKey.split("-");
                			mThreads = mThreads + mTokens[1].replace("_", " ") + ": " + mBundle.getString(mKey) + "<br/>";
                		}
                		
                		if(mKey.startsWith("packets")) {
                			mTokens = mKey.split("-");
                			mPackets = mPackets + mTokens[1].replace("_", " ") + ": " + mBundle.getString(mKey) + "<br/>";
                		}
                		
                		if(mKey.startsWith("batman")) {
                			mTokens = mKey.split("-");
                			mBatman = mBatman + mTokens[1].replace("_", " ") + ": " + mBundle.getString(mKey) + "<br/>";
                		}
                		
                		if(mKey.startsWith("database")) {
                			mTokens = mKey.split("-");
                			mDatabase = mDatabase + mTokens[1].replace("_", " ") + ": " + mBundle.getString(mKey) + "<br/>";
                		}
                	}
                	
                	statusText.setText(Html.fromHtml(mThreads + mPackets + mBatman + mDatabase));
                	
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
    		
    		try {
    			// send a message to the service to see what its status is
    			Message mMessage = Message.obtain(null, CoreMappingService.MSG_SERVICE_STATUS);
    			mMessage.replyTo = messenger;
    			serviceMessenger.send(mMessage);

    		} catch (RemoteException e) {
    			// only log while in development 
    			if(V_LOG) {
    				Log.v(TAG, "unable to send a message to the MappingDataService", e);
    			}
    		}
    		
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
        bindService(new Intent(StatusActivity.this, CoreMappingService.class), connection, Context.BIND_AUTO_CREATE);
    }

    // unbind from the service
    private void doUnbindService() {
        if(serviceMessenger != null) {
            // Detach our existing connection.
            unbindService(connection);
        }
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// determine which button was touched
		if(v.getId() == R.id.btn_status_continue) {
			// go back to the calling activity
			onBackPressed(); 
		}

	}
	
	/*
	 * intercept the back key press
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// set a result that indicates to the calling activity to stop everything
		doUnbindService();
		setResult(0);
		finish();
		return;
	}

}
