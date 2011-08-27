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

import org.servalproject.mappingservices.content.IncidentProvider;
import org.servalproject.mappingservices.content.DatabaseUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that displays details of an incident to the user
 * 
 */
public class ViewIncidentActivity extends Activity {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-VIA";

	
	/*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.view_incident_activity);
         
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
        		 Toast.makeText(this.getApplicationContext(), R.string.incident_view_error_invalid_id, Toast.LENGTH_LONG).show();
        		 finish();

        	 } 
         } else {
        	 // show a toast and redirect back to calling activity
        	 if(V_LOG) {
        		 Log.v(TAG, "activity started without a record id");
        	 }
        	 Toast.makeText(this.getApplicationContext(), R.string.incident_view_error_missing_id, Toast.LENGTH_LONG).show();
        	 finish();
         }
    }
    
    /*
     * private method use to populate the activity
     */
    private void populateActivity(long recordId) {
    	
    	if(V_LOG) {
    		Log.v(TAG, "activity started with record id: " + recordId);
    	}
    	
    	// get the details of the incident
    	Uri mIncidentUri = ContentUris.withAppendedId(IncidentProvider.CONTENT_URI, recordId);
    	ContentResolver mResolver = getContentResolver();
    	Cursor mIncidentDetails = mResolver.query(mIncidentUri, null, null, null, null);
    	
    	// check to make sure details have been returned
    	if(mIncidentDetails.moveToFirst() == false) {
    		// no data was found
    		// show a toast and redirect back to calling activity
	       	 if(V_LOG) {
	       		 Log.v(TAG, "no incident data found with record id: " + recordId);
	       	 }
	       	 Toast.makeText(this.getApplicationContext(), R.string.incident_view_error_no_data_found, Toast.LENGTH_LONG).show();
	       	 finish();
    	} else {
    		// populate the activity
    		
    		String mTitle = mIncidentDetails.getString(mIncidentDetails.getColumnIndex(IncidentProvider.TITLE_FIELD));
    		TextView mTitleView = (TextView)findViewById(R.id.lbl_incident_title);
    		mTitleView.setText(mTitle);
    		
    		String mDescription = mIncidentDetails.getString(mIncidentDetails.getColumnIndex(IncidentProvider.DESCRIPTION_FIELD));
            TextView mDescriptionView = (TextView)findViewById(R.id.lbl_incident_description);
            mDescriptionView.setMovementMethod(new ScrollingMovementMethod()); // setup scrolling for the description text view
            mDescriptionView.setText(mDescription);
            
            String mPhoneNumber = mIncidentDetails.getString(mIncidentDetails.getColumnIndex(IncidentProvider.PHONE_NUMBER_FIELD));
            TextView mAddedByView = (TextView)findViewById(R.id.lbl_incident_added_by);
            mAddedByView.setText(mPhoneNumber);
            
            TextView mIncidentAgeView = (TextView)findViewById(R.id.lbl_incident_age);
            mIncidentAgeView.setText(this.getIncidentAge(mIncidentDetails.getString(mIncidentDetails.getColumnIndex(IncidentProvider.TIMESTAMP_UTC_FIELD))));
    	}
    	
    	// play nice and release any resources
    	mIncidentDetails.close();
    	mIncidentDetails = null;

    }
    
    // private method to calculate the incident age
    private String getIncidentAge(String timeStampAsUtc) {
    	
    	String mIncidentAge = null;
    	
    	// get device time in UTC
    	long mDeviceTimeAsLong = DatabaseUtils.getCurrentTimeAsUtc();
    	
    	// convert timestamp string to long
    	long mTimeStampAsLong = Long.parseLong(timeStampAsUtc);
    	
    	// get the difference
    	long mTimeDifference = mDeviceTimeAsLong - mTimeStampAsLong;
    	
    	// convert to a human readable representation
    	
    	int mTime = (int) (mTimeDifference / 60);
    	
    	if(mTime < 1) { // less than one minute
    		mTime = (int) (mTimeDifference % 60);
    		mIncidentAge = String.format(this.getString(R.string.incident_view_seconds), mTime);
    	} else if(mTime > 60) { // more than an hour
    		mTime = (int) (mTimeDifference / 3600);
    		
    		if(mTime > 24) { // more than 24 hours
    			mIncidentAge = String.format(this.getString(R.string.incident_view_more_than_a_day), mTime);
    		} else {
    			mIncidentAge = String.format(this.getString(R.string.incident_view_hours), mTime);
    		}
    	} else { // minutes
    		mIncidentAge = String.format(this.getString(R.string.incident_view_minutes), mTime);
    	}
 
    	return mIncidentAge;
    }

}
