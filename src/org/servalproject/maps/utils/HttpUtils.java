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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

/**
 * utility class used to undertake HTTP related tasks
 */
public class HttpUtils {
	
	/*
	 * public class level constants
	 */
	
	/**
	 * tag used in debug logging
	 */
	public static final String TAG = "HttpUtils";
	
	/*
	 * private class level constants
	 */
	private static String LINE_END = "\r\n";
    private static String TWO_HYPHENS = "--";
    private static String BOUNDARY = "AaB03x87yxdkjnxvi7";
	
	/**
	 * 
	 * upload the contents of a file using a HTTP post
	 * 
	 * @param fileUpload the file to upload
	 * @param url the URL to use for the upload
	 * @return the response from the server
	 * @throws IOException if anything bad happens
	 */
	public static String doHttpUpload(File fileUpload, String url) throws IOException {
		
		// the code in this method is based on the code available here:
		// http://stackoverflow.com/questions/4966910/androidhow-to-upload-mp3-file-to-http-server#answer-6101149
		// and considered to be in the public domain
		
		HttpURLConnection mHttpConnection = null;
        DataOutputStream mDataOutput = null;
        DataInputStream mDataInput = null;
        FileInputStream mFileInput = null;
        
        byte[] mBuffer = new byte[1024];
        
        // open the upload file for reading
        try {
			mFileInput = new FileInputStream(fileUpload);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "unable to open the input file", e);
			throw new IOException("unable to open the input file");
		}
        
        // open a connection to the url
        URL mUrl;
		try {
			mUrl = new URL(url);
			
			mHttpConnection = (HttpURLConnection) mUrl.openConnection();
	        
	        // setup the connection
	        mHttpConnection.setDoInput(true);
	        mHttpConnection.setDoOutput(true);
	        mHttpConnection.setUseCaches(false);
	        
	        // use a POST method
	        mHttpConnection.setRequestMethod("POST");
	        mHttpConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
		} catch (MalformedURLException e) {
			Log.e(TAG, "supplied URL is invalid", e);
			throw new IOException("supplied URL is invalid");
		} catch (ProtocolException e) {
			Log.e(TAG, "unable to use the POST method", e);
			throw new IOException("unable to use the POST method");
		} catch (IOException e) {
			Log.e(TAG, "unable to connect to the server", e);
			throw new IOException("unable to connect to the server");
		}
        
        
        // upload the file
        try {
        
	        // add necessary boilerplate
	        mDataOutput = new DataOutputStream(mHttpConnection.getOutputStream());
	        
	        mDataOutput.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
	        
	        mDataOutput.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + fileUpload.getName() + "\"" + LINE_END);
	        mDataOutput.writeBytes(LINE_END);
	        
	        // add the content of the file
	        int mBytesRead;
	        
	        while ((mBytesRead = mFileInput.read(mBuffer)) != -1) {
	        	mDataOutput.write(mBuffer, 0, mBytesRead);
	        }
	        
	        // add necessary boilerplate
	        mDataOutput.writeBytes(LINE_END);
	        mDataOutput.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
	        mDataOutput.flush();
        } catch (IOException e) {
        	Log.e(TAG, "unable to upload file", e);
        	// TODO use improved IOException constructor when available
        	throw new IOException("unable to upload the file");
        } finally {
        	// play nice and close the streams
        	try {
	        	if(mFileInput != null) {
	        		mFileInput.close();
	        	}
        	}
	        catch(IOException ex) {
	        	Log.e(TAG, "unable to close input stream", ex);
	        }
        	
        	try {
	        	if(mDataOutput != null) {
	        		mDataOutput.close();
	        	}
        	}
	        catch(IOException ex) {
	        	Log.e(TAG, "unable to close output stream", ex);
	        }
        }
        
        /*
         * read the server response
         */
        try {
	        mDataInput = new DataInputStream(mHttpConnection.getInputStream());
	        
	        StringBuilder mResponse = new StringBuilder();
	        
	        String mLine;
	        
	        while((mLine = mDataInput.readLine()) != null) {
	        	mResponse.append(mLine).append('\n');
	        }
	
	        //Log.d(TAG, mResponse.toString());
	        
	        return mResponse.toString();
	        
        } catch (IOException e) {
        	Log.e(TAG, "unable to read response from server", e);
        	throw new IOException("unable to read response from the server");
        } finally {
        	try {
	        	if(mDataInput != null) {
	        		mDataInput.close();
	        	}
        	}
	        catch(IOException ex) {
	        	Log.e(TAG, "unable to close input stream", ex);
	        }
        }
	}
	
	
	
	/**
	 * a utility method that can be used to download data and return it is a string
	 * 
	 * @param url the url to return
	 * @return the data at the url as a string
	 * @throws IOException if anything bad happens
	 */
	public static String downloadString(String url) throws IOException {
		
		// check on the parameter
		if(TextUtils.isEmpty(url)) {
			throw new IllegalArgumentException("A url parameter is required");
		}
		
		// connect to the supplied URL
		URL mUrl = null;
		HttpURLConnection urlConnection = null;
		try {
			mUrl = new URL(url);
			urlConnection = (HttpURLConnection) mUrl.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
		} catch (MalformedURLException e) {
			Log.e(TAG, "invalid url", e);
			throw new IOException("Unable to connect to the given url");
		} catch (IOException e) {
			Log.e(TAG, "unbable to connect to the given url", e);
			throw new IOException("Unable to connect to the given url");
		}
		
		// get the data
		try {
		InputStream mInputStream = urlConnection.getInputStream();
		BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
		StringBuilder mBuilder = new StringBuilder();
		
		String mLine;
		
		while ((mLine = mBufferedReader.readLine()) != null) {
			mBuilder.append(mLine);
		}
		
		return mBuilder.toString();
		} catch (IOException e) {
			Log.e(TAG, "unable to download the data", e);
			throw new IOException("unable to download the required data");
		}
	}
	
	/**
	 * check to see if the device is online, ie. has a valid Internet connection
	 * @param context a context used to gain access to system resources
	 * 
	 * @return true if there is an Internet connection, false if there isn't
	 */
	public static boolean isOnline(Context context) {
		
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo[] mNetworkInfos = mConnectivityManager.getAllNetworkInfo();
		
		if(mNetworkInfos == null) {
			Log.w(TAG, "no network info could be retrived");
			return false;
		}
		
		for(NetworkInfo mInfo : mNetworkInfos) {
			
			if(mInfo.isConnected() == true) {
				return true;
			}
			
		}
		
		return false;	
	}
	
	
}
