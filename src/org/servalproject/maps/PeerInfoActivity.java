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

import java.text.DecimalFormat;

import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.utils.GeoUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * activity to show information about a peer
 */
public class PeerInfoActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
//	private final boolean V_LOG = true;
	private final String  TAG = "PeerInfoActivity";
	private String phoneNumber = null;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.peer_info);
        
        Intent mIntent = getIntent();
        
		// resolve the content uri
		ContentResolver mContentResolver = getApplicationContext().getContentResolver();
		
		Uri mContentUri = Uri.parse(LocationsContract.CONTENT_URI.toString() + "/" + mIntent.getIntExtra("recordId", -1));
		
		// get the content
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// populate the activity
		if(mCursor.getCount() == 1) {
			
			mCursor.moveToFirst();
			
			TextView mView = (TextView) findViewById(R.id.peer_info_ui_txt_phone_number);
			phoneNumber = mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.PHONE_NUMBER));
			
			// display the contacts name or their phone number
			String mDisplayName = getDisplayName(phoneNumber);
			
			if(mDisplayName == null) {
				mView.setText(phoneNumber);
			} else {
				mView.setText(mDisplayName);
			}
			
			mView = (TextView) findViewById(R.id.peer_info_ui_txt_latitude);
			mView.setText(mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)));
			
			mView = (TextView) findViewById(R.id.peer_info_ui_txt_longitude);
			mView.setText(mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)));
			
			mView = (TextView) findViewById(R.id.peer_info_ui_txt_age);
			mView.setText(
					TimeUtils.calculateAge(
						mCursor.getLong(mCursor.getColumnIndex(LocationsContract.Table.TIMESTAMP)),
						mCursor.getString(mCursor.getColumnIndex(LocationsContract.Table.TIMEZONE)),
						getApplicationContext()));
			
			// calculate the distance between user and POI if possible
			Location mLocation = LocationCollector.getLocation();
			
			// calculate the location
			if(mLocation != null) {
			
				// get the preferences for the distance calculation
				SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				
				String mPreference = mPreferences.getString("preferences_measurement_units", null);
				
				int mUnits = GeoUtils.METRE_UNITS;
				
				if(mPreference != null) {
					mUnits = Integer.parseInt(mPreference);
				}
				
				mPreference = mPreferences.getString("preferences_measurement_algorithm", null);
				
				int mAlgorithm = GeoUtils.HAVERSINE_FORMULA;
				
				if(mPreference != null) {
					mAlgorithm = Integer.parseInt(mPreference);
				}
				
				double mDistance = GeoUtils.calculateDistance(
						mLocation.getLatitude(), 
						mLocation.getLongitude(),
						mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)),
						mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)),
						mAlgorithm,
						mUnits);
				
				//debug code
				Log.d(TAG, "distance: " + mDistance);
				
				String mDistanceAsString = null;
				
				mView = (TextView) findViewById(R.id.peer_info_ui_txt_distance);
				
				if(mDistance != Double.NaN) {
					
					// round to two decimal places
					DecimalFormat mFormat = new DecimalFormat("#.##");
					
					switch(mUnits){
					case GeoUtils.METRE_UNITS:
						// use metres string
						if(mDistance > 1) {
							mDistanceAsString = String.format(getString(R.string.misc_disance_kms), mFormat.format(mDistance));
						} else {
							mDistance = mDistance * 1000;
							mDistanceAsString = String.format(getString(R.string.misc_disance_metres), mFormat.format(mDistance));
						}
						break;
					case GeoUtils.MILE_UNITS:
						// use mile units
						mDistanceAsString = String.format(getString(R.string.misc_disance_miles), mFormat.format(mDistance));
						break;
					case GeoUtils.NAUTICAL_MILE_UNITS:
						// use nautical mile units
						mDistanceAsString = String.format(getString(R.string.misc_disance_nautical_miles), mFormat.format(mDistance));
						break;
					}
					
					mView.setText(mDistanceAsString);
				} else {
					mView.setText(R.string.misc_not_available);
				}
				
			} else {
				mView = (TextView) findViewById(R.id.peer_info_ui_txt_distance);
				mView.setText(R.string.misc_not_available);
			}
			
		} else {
			// show an error
			Toast.makeText(getApplicationContext(), R.string.peer_info_toast_no_record_error, Toast.LENGTH_LONG).show();
			Log.e(TAG, "Unable to load records, supplied id: " + mIntent.getIntExtra("recordId", -1));
			mCursor.close();
			finish();
		}
		
		// play nice and tidy up
		mCursor.close();
		
		// capture the touch on the buttons
		Button mButton = (Button) findViewById(R.id.peer_info_ui_btn_call);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.peer_info_ui_btn_msg);
		mButton.setOnClickListener(this);
		
		mButton = (Button) findViewById(R.id.peer_info_ui_btn_back);
		mButton.setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		Intent mIntent;
		
		// determine which button was clicked
		switch(v.getId()) {
		case R.id.peer_info_ui_btn_back:
			// back button was pressed
			finish();
			break;
		case R.id.peer_info_ui_btn_call:
			// call button was pressed
			mIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
			startActivityForResult(mIntent, 0);
			break;
		case R.id.peer_info_ui_btn_msg:
			// message button was pressed
			mIntent = new Intent(Intent.ACTION_VIEW);
			mIntent.setType("vnd.android-dir/mms-sms");
			mIntent.putExtra("address", phoneNumber);
			startActivityForResult(mIntent, 0);
		default:
			// unknown view id
			Log.w(TAG, "unkown view id in onClick: " + v.getId());
		}
	}
	
	// method to get the contact name from the phone number
	// assumes user has populated their address book from Serval Mesh
	// TODO integrate with Serval Mesh contact lookup if possible
	private String getDisplayName(String phoneNumber) {
		
		String displayName = null;
		
		// build a URI to use as the search
		Uri mContactsUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		
		// do the database lookup
		ContentResolver mContentResolver = getContentResolver();
		
		// build the projection
		String[] mProjection = new String[2];
		mProjection[0] = ContactsContract.PhoneLookup._ID;
		mProjection[1] = ContactsContract.PhoneLookup.DISPLAY_NAME;
		
		// get the data
		Cursor mCursor = mContentResolver.query(
				mContactsUri,
				mProjection,
				null,
				null,
				null);
		
		// check to see what was returned
		if(mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			
			displayName = mCursor.getString(mCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			mCursor.close();
		}

		return displayName;
	}

}
