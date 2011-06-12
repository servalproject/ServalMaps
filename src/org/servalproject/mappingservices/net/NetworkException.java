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

/**
 * An exception thrown by one of the classes in the org.servalproject.mappingservices.net package
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class NetworkException extends RuntimeException {

	private static final long serialVersionUID = -3830819453912919728L;
	
	String error = null;
	
	public NetworkException() {
		super();
		error = "unknown";
	}
	
	public NetworkException(String err) {
		super(err);
		error = err;
	}
	
	public NetworkException(String err, Throwable throwable) {
		super(err, throwable);
		error = err;
	}
	
	public String getError() {
		return error;
	}
}
