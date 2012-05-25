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

import java.io.File;
import java.util.TimeZone;

import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.protobuf.BinaryFileWriter;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.HashUtils;
import org.servalproject.maps.utils.MediaUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * an activity to solicit information about a POI
 */
public class NewPoiActivity extends Activity implements OnClickListener{
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = false;
	private final String  TAG = "NewPoiActivity";
	
	private final int MAX_DESCRIPTION_CHARACTERS = 250;
	private final int MAX_TITLE_CHARACTERS = 50;
	
	private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	
	/*
	 * private class level variables
	 */
	private TextView txtCharacters;
	
	private double latitude = -1;
	private double longitude = -1;
	
	private String phoneNumber;
	private String subscriberId; 
	
	private Uri photoFileUri;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.new_poi);
        
        // see if a latitude and longitude has been passed across
        Bundle mBundle = this.getIntent().getExtras();
        
        // if a bundle has been passed 
        // extract the latitude and longitude for use later
        if(mBundle != null) {
        	latitude = mBundle.getDouble("latitude");
        	longitude = mBundle.getDouble("longitude");
        } else {
        	
        	Location mLocation = LocationCollector.getLocation();
        	
        	if(mLocation != null) {
        		latitude = mLocation.getLatitude();
        		longitude = mLocation.getLongitude();
        	}
        }
        
        // inform user of the character limit
        txtCharacters = (TextView) findViewById(R.id.new_poi_ui_txt_characters);
        txtCharacters.setText(Integer.toString(MAX_DESCRIPTION_CHARACTERS));
        
        // watch for changes in the text of the description
        TextView mTextView = (TextView) findViewById(R.id.new_poi_ui_txt_description);
        mTextView.addTextChangedListener(descriptionWatcher);
        
        mTextView = (TextView) findViewById(R.id.new_poi_ui_txt_latitude);
        mTextView.setText(Double.toString(latitude));
        
        mTextView = (TextView) findViewById(R.id.new_poi_ui_txt_longitude);
        mTextView.setText(Double.toString(longitude));
        
        // listen for button presses
        Button mButton = (Button) findViewById(R.id.new_poi_ui_btn_save);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.new_poi_ui_btn_photo);
        mButton.setOnClickListener(this);
        
        // get the mesh phone number and sid
        ServalMaps mApplication = (ServalMaps) getApplicationContext();
		phoneNumber = mApplication.getPhoneNumber();
		subscriberId = mApplication.getSid();
		mApplication = null;
		
		// disable the manual entry of geo coordinates
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean mPreference = mPreferences.getBoolean("preferences_map_new_poi_geocoords", false);
         
        View mLayout = (View) findViewById(R.id.new_poi_ui_geocord_layout);
        
		if(mPreference == false) {
			mLayout.setVisibility(View.GONE);
		} else {
			mLayout.setVisibility(View.VISIBLE);
		}

    }
    
    // keep track of the number of characters remaining in the description
    private final TextWatcher descriptionWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			int mCharsRemaining = MAX_DESCRIPTION_CHARACTERS - s.length();
			txtCharacters.setText(Integer.toString(mCharsRemaining));
		}
    	
    };

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		String mMessage;
		
		// work out which element was clicked
		switch(v.getId()) {
		case R.id.new_poi_ui_btn_save:
			// save the new POI
			if(V_LOG) {
				Log.v(TAG, "save new poi button touched");
			}
			
			// validate the title
			TextView mView = (TextView) findViewById(R.id.new_poi_ui_txt_title);
			
			if(TextUtils.isEmpty(mView.getText()) == true) {
				Toast.makeText(this, R.string.new_poi_toast_title_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			if(mView.getText().length() > MAX_TITLE_CHARACTERS) {
				mMessage = getString(R.string.new_poi_toast_title_too_long);
				Toast.makeText(this, String.format(mMessage, MAX_TITLE_CHARACTERS), Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			String mTitle = mView.getText().toString();
			
			// validate the description
			mView = (TextView) findViewById(R.id.new_poi_ui_txt_description);
			
			if(TextUtils.isEmpty(mView.getText()) == true) {
				Toast.makeText(this, R.string.new_poi_toast_description_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			if(mView.getText().length() > MAX_DESCRIPTION_CHARACTERS) {
				mMessage = getString(R.string.new_poi_toast_description_too_long);
				Toast.makeText(this, String.format(mMessage, MAX_DESCRIPTION_CHARACTERS), Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			String mDescription = mView.getText().toString();
			
			// validate the latitude
			mView = (TextView) findViewById(R.id.new_poi_ui_txt_latitude);
			
			if(TextUtils.isEmpty(mView.getText()) == true) {
				Toast.makeText(this, R.string.new_poi_toast_latitude_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			try {
				latitude = Double.parseDouble(mView.getText().toString());
			} catch (NumberFormatException e) {
				Toast.makeText(this, R.string.new_poi_toast_latitude_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			// validate the longitude
			mView = (TextView) findViewById(R.id.new_poi_ui_txt_longitude);
			
			if(TextUtils.isEmpty(mView.getText()) == true) {
				Toast.makeText(this, R.string.new_poi_toast_longitude_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			try {
				longitude = Double.parseDouble(mView.getText().toString());
			} catch (NumberFormatException e) {
				Toast.makeText(this, R.string.new_poi_toast_longitude_missing, Toast.LENGTH_SHORT).show();
				mView.requestFocus();
				return;
			}
			
			// add the new POI
			if(addNewPoi(mTitle, mDescription) == true) {
				finish();
			}
			break;
		case R.id.new_poi_ui_btn_photo:
			// use the inbuilt camera app to take a photo
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		    photoFileUri = MediaUtils.getOutputMediaFileUri(MediaUtils.MEDIA_TYPE_IMAGE); // create a file to save the image
		    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri); // set the image file name

		    // start the image capture Intent
		    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == Activity.RESULT_OK) {
	            // Image captured and saved to fileUri specified in the Intent
	        	// check on the returned intent data
	        	if(data == null) {
	        		// no return intent so check the uri that was supplied
	        		if(FileUtils.isFileReadable(photoFileUri.getPath()) == false) {
	        			photoFileUri = null;
	        		} 
	        	} else {
	        		if(FileUtils.isFileReadable(data.getData().getPath()) == false) {
	        			photoFileUri = null;
	        		} 
	        	}
	        	
	        	if(photoFileUri == null) {
	        		// no photo available, inform user
	        		Toast.makeText(this, R.string.new_poi_toast_no_photo, Toast.LENGTH_SHORT).show();
	        	}
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        	photoFileUri = null;
	        } else {
	            // Image capture failed, advise user
	        	Toast.makeText(this, R.string.new_poi_toast_no_photo, Toast.LENGTH_SHORT).show();
	        	photoFileUri = null;
	        }
	        
	        // check to see if we need to update the text of the photo
	        if(photoFileUri != null) {
	        	Button mButton = (Button) findViewById(R.id.new_poi_ui_btn_photo);
	        	mButton.setText(getString(R.string.new_poi_ui_btn_photo_replace));
	        }
	    }
	}
	
	// add the new POI to the database
	private boolean addNewPoi(String title, String description) {
		
		// add the new POI to the database
		ContentValues mValues = new ContentValues();
		
		// check on the coordinates
		if(latitude == -1 || longitude == -1) {
			// show an error message
			Toast.makeText(this, R.string.new_poi_toast_location_error, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		// add rest of the fields
		mValues.put(PointsOfInterestContract.Table.PHONE_NUMBER, phoneNumber);
		mValues.put(PointsOfInterestContract.Table.SUBSCRIBER_ID, subscriberId);
		mValues.put(PointsOfInterestContract.Table.LATITUDE, latitude);
		mValues.put(PointsOfInterestContract.Table.LONGITUDE, longitude);
		mValues.put(PointsOfInterestContract.Table.TIMESTAMP, System.currentTimeMillis());
		mValues.put(PointsOfInterestContract.Table.TIMEZONE, TimeZone.getDefault().getID());
		mValues.put(PointsOfInterestContract.Table.TITLE, title);
		mValues.put(PointsOfInterestContract.Table.DESCRIPTION, description);
		
		// check to see if a photo is available
		if(photoFileUri != null) {
			// process the photo
			// generate a hash of this poi
			String mHash = HashUtils.hashPointOfInterestMessage(
					phoneNumber, 
					latitude, 
					longitude, 
					title, 
					description);
			
			// store the name of the photo
			String mPhotoName = MediaUtils.PHOTO_FILE_PREFIX + mHash + ".jpg";
			
			// rename the file
			File mPhotoFile = new File(photoFileUri.getPath());
			
			mPhotoName = mPhotoFile.getParent() + File.separator + mPhotoName;
			
			Log.v(TAG, mPhotoName);
			
			mPhotoFile.renameTo(new File(mPhotoName));
			
			mValues.put(PointsOfInterestContract.Table.PHOTO, new File(mPhotoName).getName());
		}
		
		try {
			Uri newRecord = getContentResolver().insert(PointsOfInterestContract.CONTENT_URI, mValues);
			
			BinaryFileWriter.writePointOfInterest(this, newRecord.getLastPathSegment());
			if(V_LOG) {
				Log.v(TAG, "new POI record created with id: " + newRecord.getLastPathSegment());
			}
		}catch (SQLException e) {
			Log.e(TAG, "unable to add new POI record", e);
			Toast.makeText(this, R.string.new_poi_toast_save_error, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

	
}
