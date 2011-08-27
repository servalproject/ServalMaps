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
 *  If not, see <http://www.gnu.org/licenses/>
 */
package org.servalproject.mappingservices;

import java.util.Date;
import java.util.TimeZone;

import org.servalproject.mappingservices.content.IncidentProvider;
import org.servalproject.mappingservices.content.DatabaseUtils;
import org.servalproject.mappingservices.net.BatmanPeerList;
import org.servalproject.mappingservices.net.NetworkException;
import org.servalproject.mappingservices.net.PacketBuilder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity that allows a user to add a new incident
 * 
 */
public class AddIncidentActivity extends Activity implements OnClickListener {
	
	/**
	 * the maximum allowed length of the incident title (in chars)
	 */
	public static final int MAX_TITLE_LENGTH = 30;
	
	/**
	 * the maximum allowed length of the incident description (in chars)
	 */
	public static final int MAX_DESCRIPTION_LENGTH = 480;
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-AIA";
	
	/*
	 * private class level variables
	 */
	private PacketBuilder packetBuilder;
	private MappingServicesApplication application;
	
	/*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.add_incident_activity);
         
         if(V_LOG) {
    		 Log.v(TAG, "activity started");
    	 }
         
         // associate the buttons with our event listener
         Button button = (Button)findViewById(R.id.btn_save_incident);
         button.setOnClickListener(this);
         
         application = (MappingServicesApplication)this.getApplicationContext();
         
         BatmanPeerList peerList = application.getBatmanPeerList();
         
         packetBuilder = new PacketBuilder(this.getApplicationContext(), peerList);
         
    }
    
    /*
     * called when one of the buttons is touched
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "button touched");
		}
		
		// determine which button was touched
		if(v.getId() == R.id.btn_save_incident) {
			// save the incident
			
			// validate the input fields
			EditText mTitleText = (EditText)findViewById(R.id.txt_incident_title);
			EditText mDescriptionText = (EditText)findViewById(R.id.txt_incident_description);
			
			CharSequence mTitle = mTitleText.getText();
			CharSequence mDescription = mDescriptionText.getText();
			
			// check for empty input
			if(TextUtils.isEmpty(mTitle) == true) {
				// show an error and bail out
				Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_empty_title, Toast.LENGTH_LONG).show();
				return;
			}
			
			if(TextUtils.isEmpty(mDescription) == true) {
				// show an error and bail out
				Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_empty_description, Toast.LENGTH_LONG).show();
				return;
			}
			
			// check for input that is too long
			if(mTitle.length() > MAX_TITLE_LENGTH) {
				// show an error and bail out
				Toast.makeText(this.getApplicationContext(), String.format(this.getString(R.string.incident_add_error_title_length), MAX_TITLE_LENGTH), Toast.LENGTH_LONG).show();
				return;
			}
			
			if(mDescription.length() > MAX_DESCRIPTION_LENGTH) {
				// show an error and bail out
				Toast.makeText(this.getApplicationContext(), String.format(this.getString(R.string.incident_add_error_description_length), MAX_TITLE_LENGTH), Toast.LENGTH_LONG).show();
				return;
			}
			
			
			// get the location
			LocationManager mlocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	        Location mLocation = mlocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        
	        if(mLocation == null) {
	        	// show an error and bail out
				Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_null_location, Toast.LENGTH_LONG).show();
				return;
	        }
	        
	        // get the device phone number and SID
	        String mPhoneNumber = application.getPhoneNumber();
	        String mSid = application.getSid();
	        
	        // save the incident
	        Uri mNewRecord = saveIncident(mPhoneNumber, mSid, mLocation, mTitle, mDescription);
	        
	        if(mNewRecord != null) {
	        	try {
	        		packetBuilder.buildAndSendIncident(mNewRecord.getLastPathSegment(), true);
	    	        
	    	        // go back to the map
	        		setResult(0);
	    	        finish();
	    	        
	        	} catch(NetworkException e) {
	        		Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_send_failed, Toast.LENGTH_LONG).show();
	        		Log.e(TAG, "unable to send new incident packet", e);
	        	}
	        } else {
				Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_no_send, Toast.LENGTH_LONG).show();
	        }

		}
	}
	
	// private method to save the new incident to the database
	private Uri saveIncident(String phoneNumber, String sid, Location location, CharSequence title, CharSequence description) {
		
		ContentResolver mContentResolver = this.getContentResolver();
		Uri mIncidentContentUri = IncidentProvider.CONTENT_URI;
		Uri mNewRecord = null;
		
		// get the timestamp
		Date mDate = new Date();
		long mSeconds = mDate.getTime();
		mSeconds = mSeconds / 1000;
		
		String mTimeZone = TimeZone.getDefault().getID();
		
		// start a new list of values
		ContentValues mValues = new ContentValues();
		mValues.put(IncidentProvider.PHONE_NUMBER_FIELD, phoneNumber);
		mValues.put(IncidentProvider.SID_FIELD, sid);
		mValues.put(IncidentProvider.TITLE_FIELD, title.toString());
		mValues.put(IncidentProvider.DESCRIPTION_FIELD, description.toString());
		mValues.put(IncidentProvider.CATEGORY_FIELD, "1");
		mValues.put(IncidentProvider.LATITUDE_FIELD, Double.toString(location.getLatitude()));
		mValues.put(IncidentProvider.LONGITUDE_FIELD, Double.toString(location.getLongitude()));
		mValues.put(IncidentProvider.TIMESTAMP_FIELD, Long.toString(mSeconds));
		mValues.put(IncidentProvider.TIMEZONE_FIELD, mTimeZone);
		mValues.put(IncidentProvider.TIMESTAMP_UTC_FIELD, DatabaseUtils.getTimestampAsUtc(Long.toString(mSeconds), mTimeZone));
		
		// add the row
		try {
			mNewRecord = mContentResolver.insert(mIncidentContentUri, mValues);
		} catch (SQLException e) {
			Log.e(TAG, "unable to save new incident data", e);
			Toast.makeText(this.getApplicationContext(), R.string.incident_add_error_sql_ex, Toast.LENGTH_LONG).show();
			return null;
		}
		
		//status message
		if(V_LOG) {
			Log.v(TAG, "new incident data saved to database");
			Log.v(TAG, mNewRecord.toString() + "!@!" + mNewRecord.getLastPathSegment());
		}
		
		return mNewRecord;
	}
}
