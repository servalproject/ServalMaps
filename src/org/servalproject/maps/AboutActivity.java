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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * activity to display information about the application
 */
public class AboutActivity extends Activity implements OnClickListener {
	
	// private class level constants
	private final String TAG = "AboutActivity";
	
	// private class level variables
	private boolean licenses = false;
	private TextView aboutText;
	private Button licensesBtn;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        // linkify the about text
        aboutText = (TextView) findViewById(R.id.about_ui_txt);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        
        // associate click handlers with the buttons
        Button mButton = (Button) findViewById(R.id.about_ui_btn_stats);
        mButton.setOnClickListener(this);
        
        licensesBtn = (Button) findViewById(R.id.about_ui_btn_licenses);
        licensesBtn.setOnClickListener(this);
        
        // fill in the version label
    	TextView mTextView = (TextView) findViewById(R.id.about_ui_lbl_version);
    	
        try {
        	PackageInfo mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        	mTextView.setText(String.format(getString(R.string.about_ui_lbl_version), mPackageInfo.versionName));
        } catch (NameNotFoundException e) {
        	Log.e(TAG, "unable to determine version information", e);
        	mTextView.setText(String.format(getString(R.string.about_ui_lbl_version), getString(R.string.misc_not_available)));
        }
    }

    // respond to the touch events on the buttons
	@Override
	public void onClick(View v) {
        
		switch(v.getId()) {
		case R.id.about_ui_btn_stats:
			// show the stats activity
			startActivity(new Intent(this, org.servalproject.maps.StatsActivity.class));
			break;
		case R.id.about_ui_btn_licenses:
			// update the text
			if(licenses) {
				// show the about text
				aboutText.setText(R.string.about_ui_txt);
				licensesBtn.setText(R.string.about_ui_btn_licenses);
				licenses = false;
			} else {
				// show the licenses text
				aboutText.setText(R.string.about_ui_txt_licenses);
				licensesBtn.setText(R.string.about_ui_btn_about);
				licenses = true;
			}
		}
	}
}
