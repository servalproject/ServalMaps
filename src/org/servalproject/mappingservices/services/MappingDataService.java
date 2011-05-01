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

package org.servalproject.mappingservices.services;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.servalproject.mappingservices.content.IncidentOpenHelper;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Service to manage the collection of incoming location and incident packets
 * and put them in the appropriate database
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class MappingDataService extends Service {
	
	/**
	 * port that will be used to listen for incoming incident messages
	 */
	public static final Integer INCIDENT_PORT = 4103;
	
	/**
	 * port that will be used to listen for incoming location updates
	 */
	public static final Integer LOCATION_PORT = 4102;
	
	/**
	 * message to return the status of the service
	 */
	public static final int MSG_SERVICE_STATUS = 1;
	
	/*
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
    
    /*
     * target for clients to send messages to this service
     */
    public final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final MappingDataService self = this;
	
	/*
	 * private class level variables
	 */
	private PacketCollector incidentCollector = null; 
	private PacketCollector locationCollector = null;
	private PacketSaver     packetSaver       = null;
	private IncidentRepeater incidentRepeater = null;
	
	private Thread incidentThread    = null;
	private Thread locationThread    = null;
	private Thread packetSaverThread = null;
	private Thread incidentRepeaterThread = null;
	
	private AtomicInteger incidentCount = null;
	private AtomicInteger locationCount = null;
	
	private LinkedBlockingQueue<DatagramPacket> packetQueue = null;

	private ContentResolver contentResolver = null;
	
	
	/*
	 * private class level constants
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
			// initialise helper variables
			incidentCount = new AtomicInteger();
			locationCount = new AtomicInteger();
			packetQueue   = new LinkedBlockingQueue<DatagramPacket>();
			
			// initialise the packet collection objects
			incidentCollector = new PacketCollector(INCIDENT_PORT, incidentCount, packetQueue);
			locationCollector = new PacketCollector(LOCATION_PORT, locationCount, packetQueue);
			
			// initialise the packet saving objects
			contentResolver = this.getContentResolver();
			packetSaver = new PacketSaver(LOCATION_PORT, INCIDENT_PORT, packetQueue, contentResolver);
			
			// initialise the incident repeater object
			SQLiteOpenHelper incidentOpenHelper = new IncidentOpenHelper(this.getApplicationContext());
			incidentRepeater = new IncidentRepeater(incidentOpenHelper.getReadableDatabase());
			
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
		
		// debug message
		if(V_LOG) {
			Log.v(TAG, "service onStartCommand called");
		}
		
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
		
		if(packetSaverThread == null) {
			packetSaverThread = new Thread(packetSaver);
			packetSaverThread.start();
		}
		
		if(incidentRepeaterThread == null) {
			incidentRepeaterThread = new Thread(incidentRepeater);
			incidentRepeaterThread.start();
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
			incidentCount = null;
		}
		
		if(locationThread != null) {
			locationCollector.requestStop();
			locationThread.interrupt();
			locationCollector = null;
			locationThread = null;
			locationCount =  null;
		}
		
		if(packetSaverThread != null) {
			packetSaver.requestStop();
			packetSaverThread.interrupt();
			packetSaver = null;
			packetSaverThread = null;
		}
		
		if(incidentRepeaterThread == null) {
			incidentRepeater.requestStop();
			incidentRepeaterThread.interrupt();
			incidentRepeater = null;
			incidentRepeaterThread = null;
		}
		
		if(packetQueue != null) {
			packetQueue = null;
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
		
		// use a bundle for the info
		Bundle serviceStatus = new Bundle();
		
		//TODO add a reusable private method to determine a thread status
		//TODO use the reusable method to report status of all threads
		
		// add status info for the incident thread
		if(incidentThread != null) {
			if(incidentThread.isAlive() == true) {
				serviceStatus.putString("incidentThread", "running");
			} else {
				serviceStatus.putString("incidentThread", "stopped");
			}
		} else {
			serviceStatus.putString("incidentThread", "stopped");	
		}
		
		// add status info for the location thread
		if(locationThread != null) {
			if(locationThread.isAlive() == true) {
				serviceStatus.putString("locationThread", "running");
			} else {
				serviceStatus.putString("locationThread", "stopped");
			}
		} else {
			serviceStatus.putString("locationThread", "stopped");	
		}
		
		// add the count of packets received
		serviceStatus.putInt("incidentCount", incidentCount.get());
		serviceStatus.putInt("locationCount", locationCount.get());
		
		return serviceStatus;
	}
}