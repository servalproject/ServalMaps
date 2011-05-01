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
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.servalproject.mappingservices.services.PacketCollector;

import android.util.Log;

/**
 * A class used to send packets on the network to 
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class PacketSender {
	
	/*
	 * private class level constants
	 */
	private static final boolean V_LOG = true;
	private static final String TAG = "ServalMaps-PS";
	
	// use the address that is valid for use in the emulator
	private static final boolean IN_EMULATOR = true;
	private static final String EMULATOR_ADDRESS = "10.0.2.2";
	
	/**
	 * Sends a broadcast packet to the specified port
	 * 
	 * @param port the port number to use
	 * @param content the content of the packet
	 * 
	 * @throws UnknownHostException if the host name cannot be used
	 * @throws SocketException if a UDP socket cannot be created
	 * @throws IOException if an IOException occurs while sending the packet
	 */
	public static void sendBroadcast(Integer port, String content) throws UnknownHostException, SocketException, IOException {
		
		// validate the parameters
		if(port == null || content == null) {
			throw new IllegalArgumentException("all parameters are required");
		}
		
		if(port < PacketCollector.MIN_PORT || port > PacketCollector.MAX_PORT) {
			throw new IllegalArgumentException("port parameter must be between: " + PacketCollector.MIN_PORT + " and " + PacketCollector.MAX_PORT);
		}
		
		if(isValidString(content) == false) {
			throw new IllegalArgumentException("content must be a valid non zero length string");
		}
		
		// TODO add a method to calculate the broadcast address based on the device address
		InetAddress mAddress = null;
		
		if(IN_EMULATOR) {
			mAddress = InetAddress.getByName(EMULATOR_ADDRESS);
		}
		
		// get a new datagram socket
		DatagramSocket socket = new DatagramSocket();
		
		// prepare the content array
		byte[] bytes = content.getBytes();
		
		// check the byte array
		if(bytes.length > PacketCollector.DEFAULT_BUFFER_SIZE) {
			throw new IllegalArgumentException("the content is too long expected max '" + PacketCollector.DEFAULT_BUFFER_SIZE + "' found '" + bytes.length + "'");
		}
		
		// prepare the packet
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mAddress, port);
		
		// send the packet
		socket.send(packet);
		
		//output some debug info
		if(V_LOG) {
			Log.v(TAG, "a packet was sent to " + mAddress.getHostAddress() + ":" + port);
		}
		
		// play nice and tidy up
		socket.close();
		socket = null;
		packet = null;
	}
	
	/*
	 * private method to validate a string field
	 */
	private static boolean isValidString(String value) {
		if(value.trim().length() > 0) {
			return true;
		} else {
			return false;
		}
	}

}
