package org.servalproject.mappingservices;

import org.servalproject.mappingservices.service.MappingDataService;

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

public class MappingServices extends Activity implements OnClickListener {
	
	/*
	 * private class constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MV";
	
	/*
	 * private class variables
	 */
	private Messenger mService = null;
	private boolean mIsBound;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // associate the buttons with our event listener
        Button button = (Button)findViewById(R.id.btn_start_service);
        button.setOnClickListener(this);
        
        button = (Button)findViewById(R.id.btn_status_service);
        button.setOnClickListener(this);
    }
    
    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MappingDataService.MSG_SERVICE_STATUS:
                	Bundle bundle = msg.getData();
                	//debug code
                	Log.v(TAG, bundle.getString("locationThread"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    public final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		// This is called when the connection with the service has been
    		// established, giving us the service object we can use to
    		// interact with the service.  We are communicating with our
    		// service through an IDL interface, so get a client-side
    		// representation of that from the raw service object.
    		mService = new Messenger(service);
    		
    	}

    	public void onServiceDisconnected(ComponentName className) {
    		// This is called when the connection with the service has been
    		// unexpectedly disconnected -- that is, its process crashed.
    		mService = null;
    	}
    };
    
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MappingServices.this, MappingDataService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
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
		
		// determine which button was clicked
		if(v.getId() == R.id.btn_start_service) {
			// start button was touched
			/*
			Intent intent = new Intent(this, org.servalproject.mappingservices.service.MappingDataService.class);
			startService(intent);
			*/
			doBindService();
			
		} else if(v.getId() == R.id.btn_status_service) {
			// status button was touched
			// We want to monitor the service for as long as we are
    		// connected to it.
    		try {
    			Message msg = Message.obtain(null, MappingDataService.MSG_SERVICE_STATUS);
    			msg.replyTo = mMessenger;
    			mService.send(msg);

    		} catch (RemoteException e) {
    			// In this case the service has crashed before we could even
    			// do anything with it; we can count on soon being
    			// disconnected (and then reconnected if it can be restarted)
    			// so there is no need to do anything here.
    		}
			
		}
	}
    
    
}