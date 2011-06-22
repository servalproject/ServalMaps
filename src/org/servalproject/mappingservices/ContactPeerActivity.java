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

import org.servalproject.mappingservices.content.LocationProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that allows a user to contact a new incident
 * 
 * @author corey.wallis@servalproject.org
 *
 */
public class ContactPeerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-CPA";
	
	/*
	 * private class level variables
	 */
	private String phoneNumber;
	
	/*
	 * Called when the activity is first created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_peer_activity);

		// get the incident record id
		Bundle mBundle = this.getIntent().getExtras();
		String mRecordId = mBundle.getString("id");

		if(mRecordId != null) {
			// validate and use the id to populate the activity
			try {
				long mId = Long.parseLong(mRecordId);
				this.populateActivity(mId);
			} catch (NumberFormatException ex) {
				// id isn't in the format expected
				// do something with the error

				// show a toast and redirect back to calling activity
				if(V_LOG) {
					Log.v(TAG, "activity started with an invalid record id");
				}
				Toast.makeText(this.getApplicationContext(), R.string.contact_peer_error_invalid_id, Toast.LENGTH_LONG).show();
				finish();

			} 
		} else {
			// show a toast and redirect back to calling activity
			if(V_LOG) {
				Log.v(TAG, "activity started without a record id");
			}
			Toast.makeText(this.getApplicationContext(), R.string.contact_peer_error_missing_id, Toast.LENGTH_LONG).show();
			finish();
		}
		
		// associate the buttons with our event listener
        Button mButton = (Button)findViewById(R.id.btn_call_peer);
        mButton.setOnClickListener(this);
        
        mButton = (Button)findViewById(R.id.btn_txt_peer);
        mButton.setOnClickListener(this);
	}

	/*
	 * private method use to populate the activity
	 */
	private void populateActivity(long recordId) {

		if(V_LOG) {
			Log.v(TAG, "activity started with record id: " + recordId);
		}
		
		// get the details of the incident
    	Uri mLocationUri = ContentUris.withAppendedId(LocationProvider.CONTENT_URI, recordId);
    	ContentResolver mResolver = getContentResolver();
    	Cursor mLocationDetails = mResolver.query(mLocationUri, null, null, null, null);
    	
    	// check to make sure details have been returned
    	if(mLocationDetails.moveToFirst() == false) {
    		// no data was found
    		// show a toast and redirect back to calling activity
	       	 if(V_LOG) {
	       		 Log.v(TAG, "no incident data found with record id: " + recordId);
	       	 }
	       	 Toast.makeText(this.getApplicationContext(), R.string.contact_peer_error_no_data_found, Toast.LENGTH_LONG).show();
	       	 finish();
    	} else {
    		// populate the activity
            
            phoneNumber = mLocationDetails.getString(mLocationDetails.getColumnIndex(LocationProvider.PHONE_NUMBER_FIELD));
            TextView mPhoneNumber = (TextView)findViewById(R.id.lbl_contact_number_fld);
            mPhoneNumber.setText(phoneNumber);
            
            // TODO add age information to the contact activity?
            /*
            TextView mIncidentAgeView = (TextView)findViewById(R.id.lbl_incident_age);
            mIncidentAgeView.setText(this.getIncidentAge(mIncidentDetails.getString(mIncidentDetails.getColumnIndex(IncidentProvider.TIMESTAMP_UTC_FIELD))));
            */
    	}
    	
    	// play nice and release any resources
    	mLocationDetails.close();
    	mLocationDetails = null;
	}

	@Override
	public void onClick(View v) {
		// output some debug text
		if(V_LOG) {
			Log.v(TAG, "button touched");
			Log.v(TAG, "phone number: " + phoneNumber);
		}
		
		// determine which button was touched
		if(v.getId() == R.id.btn_call_peer) {
			// call the peer 
			Intent mIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
			this.startActivityForResult(mIntent, 0);
			
		} else if(v.getId() == R.id.btn_txt_peer) {
			// message the peer
			Intent mIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + phoneNumber));
			this.startActivityForResult(mIntent, 0);
		}
		
	}

}
