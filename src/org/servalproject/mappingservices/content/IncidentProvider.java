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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Manages access to the saved incident data used by the mapping service
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class IncidentProvider extends ContentProvider implements IncidentColumns {

	/*
	 * derived from a pattern in the "Hello, Android! 3e" ebook
	 * http://www.pragmaticprogrammer.com/titles/eband3  
	 */
	
	// use as part of a URI matcher to identify mime types
	private static final int INCIDENT = 1;
	private static final int INCIDENT_ID = 2;
	
	// declare mime types to identify the type of data requested
	private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.servalproject.mappingservice.incident"; // content type for a list of incidents
	private static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.servalproject.mappingservice.incident"; // content type for a single incident
	
	// declare other class level variables
	private UriMatcher uriMatcher;
	private IncidentOpenHelper incidentOpenHelper;
	
	@Override
	public boolean onCreate() {
		
		// declare a URI matcher class to identify and match the URIs used by this content provider
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "incidents", INCIDENT);
		uriMatcher.addURI(AUTHORITY, "incidents/#", INCIDENT_ID);
		
		// open the sqlite database
		incidentOpenHelper = new IncidentOpenHelper(getContext());
		
		// return true to indicate everything is OK
		return false;
	}
	
	/*
	 * match a given uri to a content type
	 * 
	 * @param uri the uri to identify the data
	 * @return the content type that matches the given uri
	 * @throws IllegalArgumentException if the uri doesn't match one of the uris expected
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		//determine which URI is provided and return the appropriate MIME type
		switch(uriMatcher.match(uri)) {
		case INCIDENT:
			return CONTENT_TYPE;
		case INCIDENT_ID:
			return CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	/*
	 * insert data into the locations database
	 * 
	 * @param uri the uri to identify the data
	 * @param value the values to insert
	 * @return a uri identifying the new record
	 * @throws IllegalArgumentException if the URI does not match
	 * @throws SQLException if an error occurs in the processing of the underlying SQL 
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		// validate the URI
		if(uriMatcher.match(uri) != INCIDENT) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		// get an instance of the database
		SQLiteDatabase database = incidentOpenHelper.getWritableDatabase();
		
		// insert the provided data into the database
		long id = database.insertOrThrow(TABLE_NAME, null, values);
		
		// notify anyone watching of the new data
		Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}
	
	/*
	 * delete data from the database
	 * 
	 * @param uri the uri to identify the data
	 * @param selection the where clause used to select the data
	 * @param selectionArgs the arguments to the where clause
	 * @throws IllegalArgumentException if the URI does not match
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		// get an instance of the database
		SQLiteDatabase database = incidentOpenHelper.getWritableDatabase();
		int count; // store the number of deleted rows
		
		// determine what type of delete is needed
		switch(uriMatcher.match(uri)) {
		case INCIDENT: // some other criteria
			count = database.delete(TABLE_NAME, selection, selectionArgs);
			break;
		case INCIDENT_ID: // by record id
			long id = Long.parseLong(uri.getPathSegments().get(1));
			count = database.delete(TABLE_NAME, ammendWhereClause(selection, id), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		// notify anyone watching of the change to the data
		getContext().getContentResolver().notifyChange(uri, null);
		
		// return the number of delete records
		return count;
	}

	/*
	 * execute a general query against the dataset
	 * @param uri the uri identifying the content type
	 * @param projection the list of columns to put in the result 
	 * @param selection the selection criteria
	 * @param selectionArgs the arguments used in matching against the selection criteria
	 * @param sortOrder the order in which to sort results
	 * 
	 * @throws IllegalArgumentExcpeption if the URI does not match
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		// check on which URI was used and adjust query accordingly
		if(uriMatcher.match(uri) == INCIDENT_ID) {
			long id = Long.parseLong(uri.getPathSegments().get(1));
			selection = ammendWhereClause(selection, id);
		}

		// as we're only reading no need to get a writable instance of the database
		SQLiteDatabase database = incidentOpenHelper.getReadableDatabase();

		// execute the query
		Cursor cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

		// configure the cursor to watch this URI so that it knows when data has changed
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		// return the cursor
		return cursor;
	}

	/*
	 * update the data in the database
	 * @param uri the uri identifying the content type
	 * @param values the values to update
	 * @param selection the selection criteria to match for the update
	 * 
	 * @throws IllegalArgumentExcpeption if the URI does not match
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		// get a writable instance of the database
		SQLiteDatabase database = incidentOpenHelper.getWritableDatabase();
	     int count;
		
		// check the URI and update the database accordingly
		switch (uriMatcher.match(uri)) {
		case INCIDENT:
			count = database.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case INCIDENT_ID:
			long id = Long.parseLong(uri.getPathSegments().get(1));
			count = database.update(TABLE_NAME, values, ammendWhereClause(selection, id), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		// Notify any watchers of the change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	/*
	 * append a row id test to the where clause provided by the calling code as 
	 * an extra measure to ensure everything works as we expect
	 */
	private String ammendWhereClause(String selection, long id) {
		
		String mWhereClause = "";
		
		if(selection.trim().equals("") == true) {
			mWhereClause = _ID + " = " + id;
		} else {
			mWhereClause = _ID + " = " + id + " AND (" + selection + ")";
		}
		
		return mWhereClause;
	}
}