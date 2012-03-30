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

import org.servalproject.maps.location.LocationCollector;
import org.servalproject.maps.provider.PointsOfInterestContract;
import org.servalproject.maps.utils.GeoUtils;
import org.servalproject.maps.utils.TimeUtils;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * a class used to provide data for the PoiListActivity
 */
public class PoiListAdapter extends SimpleCursorAdapter {
	
	/*
	 * private class level variables
	 */
	private String[] from;
	private int[] to;
	
	private Location location;

	public static final int PLACE_HOLDER = -1;
	
	public PoiListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.from = from;
		this.to = to;
		
		location = LocationCollector.getLocation();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView mTextView;
		
		// populate the view
		for(int i = 0; i < to.length; i++) {
			
			switch(to[i]) {
			case R.id.poi_list_ui_entry_age:
				mTextView = (TextView) view.findViewById(to[i]);
				
				mTextView.setText(
					TimeUtils.calculateAge(
						cursor.getLong(cursor.getColumnIndex(PointsOfInterestContract.Table.TIMESTAMP)),
						cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.TIMEZONE)),
						context
					)
				);
				break;
			case R.id.poi_list_ui_txt_distance:
				mTextView = (TextView) view.findViewById(to[i]);
				
				if(location != null) {
					mTextView.setText(
						GeoUtils.calculateDistanceWithDefaults(
							location.getLatitude(),
							location.getLongitude(),	
							cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.LATITUDE)),
							cursor.getDouble(cursor.getColumnIndex(PointsOfInterestContract.Table.LONGITUDE)),
							context)
					);
				} else {
					mTextView.setText(R.string.misc_not_available);
				}
				break;
			default:
				if(to[i] != PLACE_HOLDER) {
					mTextView = (TextView) view.findViewById(to[i]);
					mTextView.setText(cursor.getString(cursor.getColumnIndex(from[i])));
				}
				break;
			}
		}
		
	}

}
