/**
 * 
 */
package org.servalproject.mappingservices.service;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author techxplorer (corey@techxplorer.com)
 *
 */
public class MappingDataService extends Service {
	
	/**
	 * port that will be used to listen for incoming incident messages
	 */
	public static final Integer INCIDENT_PORT = 7001;
	
	/**
	 * port that will be used to listen for incoming location updates
	 */
	public static final Integer LOCATION_PORT = 7002;
	
	/**
	 * message to return the status of the service
	 */
	public static final int MSG_SERVICE_STATUS = 1;
	
	/**
     * Handler of incoming messages from clients.
     */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SERVICE_STATUS:
				// send service status back in response to the message
				Messenger mClient = msg.replyTo;
				try{
					Message mMessage = Message.obtain(null, MSG_SERVICE_STATUS, null);
					mMessage.setData(self.getServiceStatus());
					mClient.send(mMessage);
				} catch (RemoteException e) {

				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
    
    /**
     * target for clients to send messages to this service
     */
    public final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final MappingDataService self = this;
	
	/*
	 * class level variables
	 */
	private PacketCollector incidentCollector = null; 
	private PacketCollector locationCollector = null;
	
	private Thread incidentThread = null;
	private Thread locationThread = null;
	
	private AtomicInteger incidentCount = null;
	private AtomicInteger locationCount = null;
	
	/*
	 * private class constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-MDS";
	
	/*
	 * called when this service is initially created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		
		// set up the required objects
		try{
			incidentCollector = new PacketCollector(INCIDENT_PORT, incidentCount);
			locationCollector = new PacketCollector(LOCATION_PORT, locationCount);
			
			incidentCount = new AtomicInteger();
			locationCount = new AtomicInteger();
			
			if(V_LOG) {
				Log.v(TAG, "service created");
			}
			
		} catch(SocketException e) {
			if(V_LOG) {
				Log.v(TAG, "unable to create the packet collector objects");
			}
		}
	}

	/*
	 * called when a component starts this service
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//start the threads if required
		this.startThreads();
		
		// we don't want this service to be sticky so return the appropriate int to signify this
		return Service.START_NOT_STICKY;
	}
	
	/*
	 * start the threads if required
	 */
	private void startThreads() {
		// start the new threads as required
		if(incidentThread == null) {
			incidentThread = new Thread(incidentCollector);
			incidentThread.start();
		}
		
		if(locationThread == null) {
			locationThread = new Thread(locationCollector);
			locationThread.start();
		}
		
		if(V_LOG) {
			Log.v(TAG, "service started");
		}
	}
	
	/*
	 * called when the system destroys this service
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// play nice and tidy up as required
		if(incidentThread != null) {
			incidentCollector.requestStop();
			incidentThread.interrupt();
			incidentCollector = null;
			incidentThread = null;
		}
		
		if(locationThread != null) {
			locationCollector.requestStop();
			locationThread.interrupt();
			locationCollector = null;
			locationThread = null;
		}
		
		if(V_LOG) {
			Log.v(TAG, "service destroyed");
		}
	}

	/*
	 * called when a component wants to bind to this service
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		//start the threads if required
		this.startThreads();
		
		return mMessenger.getBinder();
	}
	
	/*
	 * determine the status of this service
	 */
	private Bundle getServiceStatus() {
		
		Bundle serviceStatus = new Bundle();
		
		//HashMap<String, String> serviceStatus = new HashMap<String, String>();
		
		// build the status bundle
		if(incidentThread != null) {
			if(incidentThread.isAlive() == true) {
				serviceStatus.putString("incidentThread", "running");
			} else {
				serviceStatus.putString("incidentThread", "stopped");
			}
		} else {
			serviceStatus.putString("incidentThread", "stopped");	
		}
		
		if(locationThread != null) {
			if(locationThread.isAlive() == true) {
				serviceStatus.putString("locationThread", "running");
			} else {
				serviceStatus.putString("locationThread", "stopped");
			}
		} else {
			serviceStatus.putString("locationThread", "stopped");	
		}
		
		serviceStatus.putInt("incidentCount", incidentCount.get());
		serviceStatus.putInt("locationCount", locationCount.get());
		
		return serviceStatus;
	}
}
