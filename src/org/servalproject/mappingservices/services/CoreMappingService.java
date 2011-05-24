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

import org.servalproject.mappingservices.content.DatabaseUtils;
import org.servalproject.mappingservices.content.IncidentOpenHelper;
import org.servalproject.mappingservices.content.RecordTypes;
import org.servalproject.mappingservices.net.PacketCollector;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
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
public class CoreMappingService extends Service {
	
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
    private final CoreMappingService self = this;
	
	/*
	 * private class level variables
	 */
	private PacketCollector incidentCollector = null; 
	private PacketCollector locationCollector = null;
	private PacketSaver     packetSaver       = null;
	private IncidentRepeater incidentRepeater = null;
	private LocationCollector incomingLocations = null;
	private LocationSaver     incomingLocationsSaver = null;
	
	private Thread incidentThread    = null;
	private Thread locationThread    = null;
	private Thread packetSaverThread = null;
	private Thread incidentRepeaterThread = null;
	private Thread incomingLocationsThread = null;
	
	private AtomicInteger incidentCount = null;
	private AtomicInteger locationCount = null;
	
	private LinkedBlockingQueue<DatagramPacket> packetQueue = null;
	private LinkedBlockingQueue<Location> incomingLocationQueue = null;

	private ContentResolver contentResolver = null;
	
	private LocationManager locationManager = null;
	
	
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
			incomingLocationQueue = new LinkedBlockingQueue<Location>();
			
			// initialise the packet collection objects
			incidentCollector = new PacketCollector(INCIDENT_PORT, incidentCount, packetQueue);
			locationCollector = new PacketCollector(LOCATION_PORT, locationCount, packetQueue);
			
			// initialise the packet saving objects
			contentResolver = this.getContentResolver();
			packetSaver = new PacketSaver(LOCATION_PORT, INCIDENT_PORT, packetQueue, contentResolver);
			
			// initialise the incident repeater object
			SQLiteOpenHelper incidentOpenHelper = new IncidentOpenHelper(this.getApplicationContext());
			incidentRepeater = new IncidentRepeater(incidentOpenHelper.getReadableDatabase());
			
			// initialise the geo location objects
			incomingLocations = new LocationCollector(incomingLocationQueue);
			incomingLocationsSaver = new LocationSaver(incomingLocationQueue, contentResolver);
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			
			if(V_LOG) {
				Log.v(TAG, "service created");
			}
			
		} catch(SocketException e) {
			if(V_LOG) {
				Log.v(TAG, "unable to create the necessary objects");
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
		
		if(incomingLocationsThread == null) {
			incomingLocationsThread = new Thread(incomingLocationsSaver);
			incomingLocationsThread.start();
			
			// listen for both GPS and Network locations
			//TODO see if the time internal and minimum distance parameters need to be adjusted
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, incomingLocations);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, incomingLocations);
			
		}
		
		if(V_LOG) {
			Log.v(TAG, "threads started");
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
		
		if(incidentRepeaterThread != null) {
			incidentRepeater.requestStop();
			incidentRepeaterThread.interrupt();
			incidentRepeater = null;
			incidentRepeaterThread = null;
		}
		
		if(incomingLocationsThread != null) {
			incomingLocationsSaver.requestStop();
			incomingLocationsThread.interrupt();
			incomingLocationsSaver = null;
			incomingLocationsThread = null;
			locationManager.removeUpdates(incomingLocations);
			incomingLocationQueue = null;
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
		
		// get the status of all of the threads
		serviceStatus.putString("incidentThread", getThreadStatus(incidentThread));
		serviceStatus.putString("locationThread", getThreadStatus(incidentThread));
		serviceStatus.putString("packetSaverThread", getThreadStatus(packetSaverThread));
		serviceStatus.putString("incidentRepeaterThread", getThreadStatus(incidentRepeaterThread));
		serviceStatus.putString("deviceLocationThread", getThreadStatus(incomingLocationsThread));
		
		// add the count of packets received
		serviceStatus.putString("incidentPacketCount", Integer.toString(incidentCount.get()));
		serviceStatus.putString("locationPacketCount", Integer.toString(locationCount.get()));
		
		// add the count of records
		serviceStatus.putString("incidentRecordCount", Integer.toString(DatabaseUtils.getRecordCount(RecordTypes.INCIDENT_RECORD_TYPE, this.getBaseContext())));
		serviceStatus.putString("locationRecordCount", Integer.toString(DatabaseUtils.getRecordCount(RecordTypes.LOCATION_RECORD_TYPE, this.getBaseContext())));
		
		return serviceStatus;
	}
	
	/*
	 * determine the status of a thread
	 */
	private String getThreadStatus(Thread thread) {
		if(thread != null) {
			if(thread.isAlive() == true) {
				return "running";
			} else {
				return "stopped";
			}
		} else {
			return "stopped";	
		}
	}
}