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

import org.servalproject.mappingservices.net.BatmanPeerList;

import android.app.Application;
import android.text.TextUtils;

/**
 * extend the base Android Application class so that data can be shared between activities
 * 
 */
public class MappingServicesApplication extends Application {
	
	/*
	 * public class constants
	 */
	/**
	 * Fake phone number used for development purposes
	 */
	public static final String FAKE_PHONE_NUMBER = "555 555 555";
	
	/**
	 * Fake SID used for development purposes
	 */
	public static final String FAKE_SID = "537f22babff853058ef4e7a7e67a487217e6f17fa2409052de432cdfd6f64ba9";
	
	/*
	 * class level variables
	 */
	private String phoneNumber = FAKE_PHONE_NUMBER;
	private String sid = FAKE_SID;
	
	private BatmanPeerList peerList = null;
	
	/**
	 * set the phone number so that it can be shared with other application components
	 * 
	 * @param value the new mobile phone value
	 * @throws IllegalArgumentException if the value is not valid
	 */
	public void setPhoneNumber(String value) throws IllegalArgumentException {
		
		if(TextUtils.isEmpty(value) == true) {
			throw new IllegalArgumentException("the value parameter must not be empty");
		}
		
		phoneNumber = value;
	}
	
	/**
	 * set the sid so that it can be shared with other application components
	 * 
	 * @param value the new sid value
	 * @throws IllegalArgumentException if the value is not valid
	 */
	public void setSid(String value) throws IllegalArgumentException {
		
		if(TextUtils.isEmpty(value) == true) {
			throw new IllegalArgumentException("the value parameter must not be empty");
		}
		
		sid = value;
	}
	
	/**
	 * A method to get the previously set phone number
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	/**
	 * get the previously set sid
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}
	
	/**
	 * a method to get the Batman peer list used when sending packets
	 * 
	 * @return a batman peer list object
	 */
	public BatmanPeerList getBatmanPeerList() {
		return peerList;
	}
	
	/**
	 * a method to set the batman peer list used when sending packets
	 * 
	 * @param peerList the valid BatmanPeerList object
	 * @throws IllegalArgumentException when the peerlist parameter is null
	 */
	public void setBatmanPeerList(BatmanPeerList peerList) {
		if(peerList == null) {
			throw new IllegalArgumentException("the peerList parameter cannot be null");
		}
		
		this.peerList = peerList;
	}
}
