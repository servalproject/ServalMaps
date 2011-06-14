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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DisclaimerActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ServalMaps-DA";
	
	/*
     * Called when the activity is first created
     * 
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer_activity);
        
        // associate the button with our event listener
        Button button = (Button)findViewById(R.id.btn_disclaimer_continue);
        button.setOnClickListener(this);
    }
	
	@Override
	public void onClick(View v) {
		// determine which button was touched
		if(v.getId() == R.id.btn_disclaimer_continue) {
			
			// check to see if the map data is available
			
			
			// start the service and show the map
			Intent mServiceIntent = new Intent(this, org.servalproject.mappingservices.services.CoreMappingService.class);
			startService(mServiceIntent);
			
			// start the map activity and be informed when it finishes 
			Intent mMapIntent = new Intent(this, org.servalproject.mappingservices.MapActivity.class);
	        startActivityForResult(mMapIntent, 0);
		}
	}
	
	/*
	 * Receive notification when the map activity finishes
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// get the result code back from the map activity
	    if(resultCode == 0) {
	    	// shutdown the service
	    	Intent mServiceIntent = new Intent(this, org.servalproject.mappingservices.services.CoreMappingService.class);
			stopService(mServiceIntent);
	    	
	        finish();
	    }
	}

}
