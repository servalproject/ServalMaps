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

package org.servalproject.mappingservices.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

/**
 * Collect UDP packets and keep a count of the number received
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class PacketCollector implements Runnable{
	
	/**
	 * the minimum allowed port number
	 */
	public static final Integer MIN_PORT = 1024;
	
	/**
	 * the maximum allowed port number
	 */
	public static final Integer MAX_PORT = 65535;
		
	/**
	 * the default buffer size used for incoming packets
	 */
	public static final Integer DEFAULT_BUFFER_SIZE = 1024;
	
	/**
	 * set the amount of time, in seconds, that the socket will wait for data
	 */
	public static final Integer DEFAULT_SOCKET_TIMEOUT = 30;
	
	/*
	 * private class level variables
	 */
	private Integer port = null;
	private DatagramSocket socket = null;
	private volatile boolean keepGoing = true;
	private AtomicInteger packetCount = null;
	private LinkedBlockingQueue<DatagramPacket> packetQueue = null;
	
	/*
	 * private class level constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-PC";
	
	/**
	 * constructor for this class
	 * 
	 * @param port the port to bind to for packets
	 * @param count a variable to hold a count of packets received
	 * 
	 * @throws SocketException if unable to bind the specified socket
	 */
	public PacketCollector (Integer port, AtomicInteger count, LinkedBlockingQueue<DatagramPacket> queue) throws SocketException {
		
		// check on the parameters
		if(port == null || count == null || queue == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		if(port < MIN_PORT || port > MAX_PORT) {
			throw new IllegalArgumentException("the port parameter must be between: " + MIN_PORT + " and " + MAX_PORT);
		}
		
		// instantiate the required objects
		this.port = port;
		socket = new DatagramSocket(this.port);
		socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT * 1000);
		
		packetCount = count;
		packetQueue = queue;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet collector configured (" + socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort() + ")");
		}
	}
	
	/*
	 * When invoked run as long as possible and collect packets from the network
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		// declare local variables
		byte[] mPacketContent = null;
		String mContent = null;
		String mHost = null;
		DatagramPacket mPacket = null;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet collector started");
		}
		
		// loop until requested to stop
		while(keepGoing == true) {
			
			// instantiate a packet object to store the packet once it comes in
			mPacketContent = new byte[DEFAULT_BUFFER_SIZE];
			mPacket = new DatagramPacket(mPacketContent, mPacketContent.length);
			
			// wait for a packet
			try {
				socket.receive(mPacket);
			} catch (java.net.SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				Log.e(TAG, "io exception occured while receiving packet", e);
			}
			
			// get info about the packet
			mHost = mPacket.getAddress().getHostName();
			mContent = new String(mPacketContent).trim();
			
			// increment the count
			packetCount.incrementAndGet();
			
			// override the port number to the destination port and not the source port
			mPacket.setPort(port);
			
			// add the packet to the queue
			packetQueue.add(mPacket);
			
			if(V_LOG) {
				Log.v(TAG, "new packet from: " + mHost + "(" + mContent + ")");
			}			
		}
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet collector stopped");
		}
		
	}
	
	/**
	 * get the port number that this PacketCollector is bound to
	 */
	public Integer getPort() {
		return port;
	}
	
	/**
	 * request that packet collection stops and so stop the thread
	 */
	public void requestStop() {
		keepGoing = false;
	}

}
