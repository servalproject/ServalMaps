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

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity that shows the about message
 * 
 */
public class AboutActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-AA";
	
	/*
	 * Called when the activity is first created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		
		// associate the buttons with our event listener
        Button button = (Button)findViewById(R.id.btn_about_continue);
        button.setOnClickListener(this);
        
        // setup the text view so links in it are clickable
        TextView mAboutText = (TextView)findViewById(R.id.lbl_about_text);
        mAboutText.setMovementMethod(LinkMovementMethod.getInstance());
        //mAboutText.setText(Html.fromHtml(this.getString(R.string.lbl_about_text)));
        
		
		if(V_LOG) {
			Log.v(TAG, "activity started");
   	 	}
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		
		// determine which button was touched
		if(v.getId() == R.id.btn_about_continue) {
			// go back to the calling activity
			onBackPressed(); 
		}

	}
	
	/*
	 * intercept the back key press
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// set a result that indicates to the calling activity to stop everything
		setResult(0);
		finish();
		return;
	}

}
