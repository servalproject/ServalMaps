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
package org.servalproject.mappingservices.content;

/**
 * Define constants to identify record types
 * 
 */
public class RecordTypes {
		
	/*
	 * don't forget to update the isValidType method when adding record types
	 */
	
	/**
	 * Constant to identify the location record type
	 */
	public static int LOCATION_RECORD_TYPE = 0;
	
	/**
	 * Constant to identify the incident record type
	 */
	public static int INCIDENT_RECORD_TYPE = 1;
	
	/**
	 * Constant to identify the self location record type
	 */
	public static int SELF_LOCATION_RECORD_TYPE = 2;
	
	/**
	 * a method to determine if a type is a valid record type
	 * 
	 * @param type the type to valid
	 * @return true if the type is valid, false if it is invalid
	 */
	public static boolean isValidType(int type) {
		
		/*
		 * don't forget to update this when adding record types
		 */
		if(type >= 0 && type <= 2) {
			return true;
		} else {
			return false;
		}
	}

}
