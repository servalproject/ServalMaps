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

/**
 * An exception thrown when the content of a packet doesn't pass validation
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 5609767631943419269L;
	
	String error = null;
	
	public ValidationException() {
		super();
		error = "unknown";
	}
	
	public ValidationException(String err) {
		super(err);
		error = err;
	}
	
	public String getError() {
		return error;
	}
}
