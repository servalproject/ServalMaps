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
		
		// get the current time
		long mCurrentTime = System.currentTimeMillis();
		
		// convert the supplied time to the device time zone
		Calendar mFromCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		mFromCalendar.setTimeInMillis(time);
		
		Calendar mToCalendar = Calendar.getInstance(TimeZone.getDefault());
		mToCalendar.setTimeInMillis(mFromCalendar.getTimeInMillis());
		
		// calculate the different
		long mTimeDifference = mCurrentTime - mToCalendar.getTimeInMillis();
		
		return getMillisHumanReadable(mTimeDifference, context);
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
	
	/**
	 * format the given time into a more friendly format using the default time zone
	 * 
	 * @param time the time format
	 * @return
	 */
	public static String formatDate(String time) {
		
		// check the parameters
		if(TextUtils.isEmpty(time) == true) {
			throw new IllegalArgumentException("the time parameter is required");
		}
		
		return formatDate(Long.parseLong(time), TimeZone.getDefault().getID());	
	}
	
	/**
	 * format the given time into a more friendly format using the default time zone
	 * 
	 * @param time the time format
	 * @return
	 */
	public static String formatDate(long time) {
		
		return formatDate(time, TimeZone.getDefault().getID());	
	}
	
	/**
	 * format the given time and timezone into a more human friendly format
	 * 
	 * @param time the time to format
	 * @param timeZone the time zone associated with the time
	 * @return
	 */
	public static String formatDateSimple(long time, String timeZone) {
		
		// check the parameters
		if(TextUtils.isEmpty(timeZone) == true) {
			throw new IllegalArgumentException("the timeZone parameter is required");
		}
		
		// get a calendar instance to help with the formating of the date
		Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		mCalendar.setTimeInMillis(time);
		
		SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		return mFormat.format(mCalendar.getTime());
	}
	
	/**
	 * format the given time and timezone into a more human friendly format
	 * 
	 * @param time the time to format
	 * @param timeZone the time zone associated with the time
	 * @return
	 */
	public static String formatDateSimple(String time, String timeZone) {
		
		// check the parameters
		if(TextUtils.isEmpty(time) == true) {
			throw new IllegalArgumentException("the time parameter is required");
		}
		
		if(TextUtils.isEmpty(timeZone) == true) {
			throw new IllegalArgumentException("the timeZone parameter is required");
		}
		
		return formatDateSimple(Long.parseLong(time), timeZone);
		
	}
	
	/**
	 * format the given time into a more friendly format using the default time zone
	 * 
	 * @param time the time format
	 * @return
	 */
	public static String formatDateSimple(String time) {
		
		// check the parameters
		if(TextUtils.isEmpty(time) == true) {
			throw new IllegalArgumentException("the time parameter is required");
		}
		
		return formatDateSimple(Long.parseLong(time), TimeZone.getDefault().getID());	
	}
	
	/**
	 * format the given time into a more friendly format using the default time zone
	 * 
	 * @param time the time format
	 * @return
	 */
	public static String formatDateSimple(long time) {
		
		return formatDateSimple(time, TimeZone.getDefault().getID());	
	}
	
	
	/**
	 * return today's date as a string
	 */
	public static String getTodayWithHour() {
		
		// get a calendar instance to help with the formating of the date
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
		
		return mFormat.format(mCalendar.getTime());
	}
	
	/**
	 * return today's date as a string
	 */
	public static String getToday() {
		
		// get a calendar instance to help with the formating of the date
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		return mFormat.format(mCalendar.getTime());
	}
	
	/**
	 * return today's date including time as a string
	 */
	public static String getTodayWithTime() {
		
		// get a calendar instance to help with the formating of the date
		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		
		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return mFormat.format(mCalendar.getTime());
	}

	/**
	 * convert a time interval in milliseconds into a human readable string
	 * 
	 * @param milliseconds the time interval to convert
	 * @param context a context object used to retrieve strings from R
	 * 
	 * @return a human readable string
	 */
	public static String getMillisHumanReadable(long milliseconds, Context context) {
		
		int mTime = (int) milliseconds / 60000;
		String mReturn;
		
		if(mTime < 1) {
			// less than one minute
			mTime = (int) (mTime % 60);
			mReturn = String.format(context.getString(R.string.misc_age_calculation_seconds), mTime);
		} else if(mTime > 60) { 
			// more than an hour
			// round the number of hours for display
    		double mHours = mTime / 60.0;
    		//mHours = (double)Math.round(mHours * 10) / 10;
    		
    		if(mHours > 24) { // more than 24 hours
    			mReturn = String.format(context.getString(R.string.misc_age_calculation_more_than_a_day), mHours);
    		} else {
    			mReturn = String.format(context.getString(R.string.misc_age_calculation_hours), mHours);
    		}
    	} else { // minutes
    		mReturn = String.format(context.getString(R.string.misc_age_calculation_minutes), mTime);
    	}
		
		return mReturn;
	}
}
