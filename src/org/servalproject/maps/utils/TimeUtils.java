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
package org.servalproject.maps.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.servalproject.maps.R;

import android.content.Context;
import android.text.TextUtils;

/**
 * a class that exposes a number of utility methods related to time
 */
public class TimeUtils {
	
	/**
	 * calculate the amount of time passed between now and the given time
	 * 
	 * @param time the time to compare against in milliseconds
	 * @param timeZone the timezone associated with the provided time
	 * @param context  the context from the calling activity
	 * 
	 * @return a string representation of the amount of time passed
	 */
	public static String calculateAge(long time, String timeZone, Context context) {
		
		// check the parameters
		if(TextUtils.isEmpty(timeZone) == true) {
			throw new IllegalArgumentException("the timeZone parameter is required");
		}
		
		if(context == null) {
			throw new IllegalArgumentException("the context parameter is required");
		}
		
		String mReturn = null;
		
		// get the current time
		long mCurrentTime = System.currentTimeMillis();
		
		// convert the supplied time to the device time zone
		Calendar mFromCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		mFromCalendar.setTimeInMillis(time);
		
		Calendar mToCalendar = Calendar.getInstance(TimeZone.getDefault());
		mToCalendar.setTimeInMillis(mFromCalendar.getTimeInMillis());
		
		// calculate the different
		long mTimeDifference = mCurrentTime - mToCalendar.getTimeInMillis();
		
		// convert the different into human readable representation
		int mTime = (int) mTimeDifference / 60000;
		
		if(mTime < 1) {
			// less than one minute
			mTime = (int) (mTimeDifference % 60);
			mReturn = String.format(context.getString(R.string.misc_age_calculation_seconds), mTime);
		} else if(mTime > 60) { 
			// more than an hour
    		mTime = (int) (mTimeDifference / 3600);
    		
    		if(mTime > 24) { // more than 24 hours
    			mReturn = String.format(context.getString(R.string.misc_age_calculation_more_than_a_day), mTime);
    		} else {
    			mReturn = String.format(context.getString(R.string.misc_age_calculation_hours), mTime);
    		}
    	} else { // minutes
    		mReturn = String.format(context.getString(R.string.misc_age_calculation_minutes), mTime);
    	}
		
		return mReturn;
	}
	
	/**
	 * format the given time and timezone into a more human friendly format
	 * 
	 * @param time the time to format
	 * @param timeZone the time zone associated with the time
	 * @return
	 */
	public static String formatDate(long time, String timeZone) {
		
		// check the parameters
		if(TextUtils.isEmpty(timeZone) == true) {
			throw new IllegalArgumentException("the timeZone parameter is required");
		}
		
		// get a calendar instance to help with the formating of the date
		Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		mCalendar.setTimeInMillis(time);
		
		SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a z");
		
		return mFormat.format(mCalendar.getTime());
	}
	
	/**
	 * format the given time and timezone into a more human friendly format
	 * 
	 * @param time the time to format
	 * @param timeZone the time zone associated with the time
	 * @return
	 */
	public static String formatDate(String time, String timeZone) {
		
		// check the parameters
		if(TextUtils.isEmpty(time) == true) {
			throw new IllegalArgumentException("the time parameter is required");
		}
		
		if(TextUtils.isEmpty(timeZone) == true) {
			throw new IllegalArgumentException("the timeZone parameter is required");
		}
		
		return formatDate(Long.parseLong(time), timeZone);
		
	}

}
