package org.servalproject.mappingservices.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

public class PacketCollector implements Runnable{
	
	/**
	 * the minimum allowed port number
	 */
	public final Integer MIN_PORT = 1024;
	
	/**
	 * the maximum allowed port number
	 */
	public final Integer MAX_PORT = 65535;
		
	/**
	 * the default buffer size used for incoming packets
	 */
	public final Integer DEFAULT_BUFFER_SIZE = 1024;
	
	/*
	 * private class variables
	 */
	private Integer port = null;
	private DatagramSocket socket = null;
	private volatile boolean keepGoing = true;
	private AtomicInteger packetCount = null;
	
	/*
	 * private class constants
	 */
	
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-PC";
	
	/**
	 * constructor for this class
	 * 
	 * @param port the port to bind to for packets
	 * @param count a variable to hold a count of packets received
	 * 
	 * @throws SocketException if a DatagramSocket could not be created
	 */
	public PacketCollector (Integer port, AtomicInteger count) throws SocketException {
		
		// check on the parameters
		if(port == null) {
			throw new IllegalArgumentException("the port parameter is required");
		}
		
		if(port < this.MIN_PORT || port > this.MAX_PORT) {
			throw new IllegalArgumentException("this port parameter must be between: " + this.MIN_PORT + " and " + this.MAX_PORT);
		}
		
		// instantiate the required objects
		this.port = port;
		socket = new DatagramSocket(this.port);
		
		packetCount = count;
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "packet collector configured (" + socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort() + ")");
		}
	}

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
			} catch (IOException e) {
				//System.err.println("an IO error occurred while reading a packet:\n" + e.toString());
			}
			
			// get info about the packet
			mHost = mPacket.getAddress().getHostName();
			mContent = new String(mPacketContent).trim();
			
			// increment the count
			packetCount.incrementAndGet();
			
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
