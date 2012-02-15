/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Maps Software
 *
 * Serval Maps Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.maps;

import android.app.Application;
import android.text.TextUtils;

/**
 * extends the base application to enable sharing of information
 * between components
 */
public class ServalMaps extends Application {

	/*
	 * class level variables
	 */
	private String phoneNumber = null;
	private String sid = null;
	
	/**
	 * set the phone number as reported by Serval
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
	 * set the sid as reported by Serval 
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
	 * Get the phone number of the device as reported by Serval 
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Get the sid of the device as reported by Serval 
	 * 
	 * @return the sid
	 */
	public String getSid() {
		return sid;
	}
	
}
