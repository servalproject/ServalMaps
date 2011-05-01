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

import java.util.TimeZone;

/**
 * A class that can validate packet content before saving to the database
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class PacketValidator {
	
	/**
	 * number of expected fields in a location packet
	 */
	public static final int LOCATION_FIELD_COUNT = 6;
	
	/**
	 * number of expected fields in an incident packet
	 */
	public static final int INCIDENT_FIELD_COUNT = 8;
	
	/**
	 * Validate the content of a location packet once it has been broken up into the individual fields
	 * 
	 * @param packetContent an array of fields derived from the packet content
	 * 
	 * @return true if the packet passes validation, false if it is fails
	 */
	public static boolean isValidLocation(String[] packetContent) throws ValidationException {
		
		// validate the packet content
		
		// ensure the required number of fields
		if(packetContent.length != LOCATION_FIELD_COUNT) {
			throw new ValidationException("incorrect number of fields found '" + packetContent.length + "' expected '" + LOCATION_FIELD_COUNT + "'");
		}
		
		/* 
		 * fields are:
		 * 
		 * type - expected to be an integer, currently only 1 is valid
		 * phone number - expected to be text
		 * latitude - expected to be a float
		 * longitude - expected to be a float
		 * timestamp - expected to be an integer
		 * timezone - expected to be a valid timezone identifier
		 * 
		 * for more info see:
		 * http://developer.servalproject.org/twiki/bin/view/Main/PublicAlphaMappingServiceLocationPackets
		 */
		
		if(isInteger(packetContent[0]) != true) {
			throw new ValidationException("type field is not an integer");
		}
		
		// TODO expand validation once more packet types are in use
		int value = Integer.parseInt(packetContent[0]);
		if(value != 1) {
			throw new ValidationException("type field is not valid. Found '" + value + "' expected '1'");
		}
		
		if(isValidString(packetContent[1]) == true) {
			throw new ValidationException("the phone number field cannot be empty");
		}
		
		if(isFloat(packetContent[2]) != true) {
			throw new ValidationException("latitude field is not a valid float");
		}
		
		if(isFloat(packetContent[3]) != true) {
			throw new ValidationException("longitude field is not a valid float");
		}
		
		if(isInteger(packetContent[4]) != true) {
			throw new ValidationException("timestamp field is not a valid integer");
		}
		
		if(isTimezoneId(packetContent[5]) != true) {
			throw new ValidationException("timezone field is not a valid timezone identifier");
		}
		
		return true;
	}
	
	/**
	 * Validate the content of an incident packet once it has been broken up into the individual fields
	 * 
	 * @param packetContent an array of fields derived from the packet content
	 * 
	 * @return true if the packet passes validation, false if it is fails
	 */
	public static boolean isValidIncident(String[] packetContent) throws ValidationException {
		
		// ensure the required number of fields
		if(packetContent.length != INCIDENT_FIELD_COUNT) {
			throw new ValidationException("incorrect number of fields found '" + packetContent.length + "' expected '" + INCIDENT_FIELD_COUNT + "'");
		}
		
		/* 
		 * fields are:
		 * 
		 * title - title of the incident
		 * description - description of the incident
		 * category - incident category
		 * latitude - expected to be a float
		 * longitude - expected to be a float
		 * timestamp - expected to be an integer
		 * timezone - expected to be a valid timezone identifier
		 * 
		 * for more info see:
		 * http://developer.servalproject.org/twiki/bin/view/Main/PublicAlphaMappingServiceIncidentPackets
		 */
		
		if(isValidString(packetContent[0]) != true) {
			throw new ValidationException("phone number field cannot be empty");
		}
		
		if(isValidString(packetContent[1]) != true) {
			throw new ValidationException("title field cannot empty");
		}
		
		if(isValidString(packetContent[2]) != true) {
			throw new ValidationException("description field cannot empty");
		}
		
		if(isInteger(packetContent[3]) != true) {
			throw new ValidationException("category field is not an integer");
		}
		
		// TODO expand validation once more categories are in use
		// most likely swap this static method for one that is non static and gets
		// the list of categories from the incident database or some other storage mechanism
		
		int value = Integer.parseInt(packetContent[3]);
		if(value != 1) {
			throw new ValidationException("category field is not valid. Found '" + value + "' expected '1'");
		}
		
		if(isFloat(packetContent[4]) != true) {
			throw new ValidationException("latitude field is not a valid float");
		}
		
		if(isFloat(packetContent[5]) != true) {
			throw new ValidationException("longitude field is not a valid float");
		}
		
		if(isInteger(packetContent[6]) != true) {
			throw new ValidationException("timestamp field is not a valid integer");
		}
		
		if(isTimezoneId(packetContent[7]) != true) {
			throw new ValidationException("timezone field is not a valid timezone identifier");
		}
		
		return true;
	}
	
	/*
	 * private method to validate an integer
	 */
	private static boolean isInteger(String value) {
		
		try{
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}
	
	/*
	 * private method to validate a float
	 */
	private static boolean isFloat(String value) {
		try{
			Float.parseFloat(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/*
	 * private method to validate a timezone
	 */
	private static boolean isTimezoneId(String value) {
		
		String[] mValidIds = TimeZone.getAvailableIDs();
		boolean mFound = false;
		
		//TODO use a better search algorithm
		for(int i = 0; i < mValidIds.length; i++) {
			if(mValidIds[i].equals(value) == true) {
				mFound = true;
				break;
			}
		}
		
		return mFound;
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
