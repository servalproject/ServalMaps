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
import java.io.IOException;

import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.provider.LocationsContract;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.FileUtils;
import org.servalproject.maps.utils.GeoUtils;
import org.servalproject.maps.utils.MediaUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * an activity used to display information about a point of interest
 */
public class PoiInfoActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	//private final boolean V_LOG = true;
	private final String  TAG = "PoiInfoActivity";
	
	/*
	 * private class level variables
	 */
	private String photoName = null;
	
	/*
	 * create the activity
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_info);
        
        Intent mIntent = getIntent();
        
		// resolve the content uri
		ContentResolver mContentResolver = getApplicationContext().getContentResolver();
		
		Uri mContentUri = Uri.parse(PointsOfInterestContract.CONTENT_URI.toString() + "/" + mIntent.getIntExtra("recordId", -1));
		
		// get the content
		Cursor mCursor = mContentResolver.query(mContentUri, null, null, null, null);
		
		// populate the activity
		if(mCursor.getCount() == 1) {
			
			mCursor.moveToFirst();
			
			TextView mView = (TextView) findViewById(R.id.poi_info_ui_txt_title);
			mView.setText(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TITLE)));
			
			mView = (TextView) findViewById(R.id.poi_info_ui_txt_description);
			mView.setText(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.DESCRIPTION)));
			
			mView = (TextView) findViewById(R.id.poi_info_ui_txt_age);
			mView.setText(
					TimeUtils.calculateAge(
						mCursor.getLong(mCursor.getColumnIndex(PointsOfInterestContract.Table.TIMESTAMP)),
						mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TIMEZONE)),
						getApplicationContext()));
			
			// show the tag list
			mView = (TextView) findViewById(R.id.poi_info_ui_txt_tags);
			mView.setText(mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TAGS)));
			
			// check to see if we need to show the view photo button
			photoName = mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.PHOTO));
			Button mButton = (Button) findViewById(R.id.poi_info_ui_btn_photo);
			
			if(TextUtils.isEmpty(photoName)) {
				mButton.setVisibility(View.INVISIBLE);
			} else {
				mButton.setOnClickListener(this);
			}
			
			mButton = (Button) findViewById(R.id.poi_info_ui_btn_back);
			mButton.setOnClickListener(this);
			
			// calculate the distance between user and POI if possible
			Location mLocation = LocationCollector.getLocation();
			
			mView = (TextView) findViewById(R.id.poi_info_ui_txt_distance);
			
			// calculate the distance
			if(mLocation != null) {
				
				// calculate the distance using the default preferences
				String mDistanceAsString = GeoUtils.calculateDistanceWithDefaults(
						mLocation.getLatitude(), 
						mLocation.getLongitude(),
						mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LATITUDE)),
						mCursor.getDouble(mCursor.getColumnIndex(LocationsContract.Table.LONGITUDE)),
						this);
				
				mView.setText(mDistanceAsString);
			} else {
				mView.setText(R.string.misc_not_available);
			}
			
		} else {
			// show error
			Toast.makeText(getApplicationContext(), R.string.poi_info_toast_no_record_error, Toast.LENGTH_LONG).show();
			Log.e(TAG, "Unable to load records, supplied id: " + mIntent.getIntExtra("recordId", -1));
			mCursor.close();
			finish();
		}
		
		// play nice and tidy up
		mCursor.close();
    }

	@Override
	public void onClick(View v) {
		
		// check which button was pressed
		switch(v.getId()) {
		case R.id.poi_info_ui_btn_photo:
			// show the photo to the user
			File mFile = new File(MediaUtils.getMediaStore() + photoName);
			
			try {
				if(FileUtils.isFileReadable(mFile.getCanonicalPath()) == true) {
					// show the file
					Intent mIntent = new Intent();
					mIntent.setAction(android.content.Intent.ACTION_VIEW);
					mIntent.setDataAndType(Uri.fromFile(mFile), "image/jpg");
					startActivity(mIntent);
				} else {
					// report an error
					Toast.makeText(getApplicationContext(), R.string.poi_into_toast_no_photo, Toast.LENGTH_LONG).show();
				}
			} catch (IOException e) {
				// report an error
				Toast.makeText(getApplicationContext(), R.string.poi_into_toast_no_photo, Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.poi_info_ui_btn_back:
			finish();
			break;
		default:
			// unknown view id
			Log.w(TAG, "unkown view id in onClick: " + v.getId());
		}
	}

}
