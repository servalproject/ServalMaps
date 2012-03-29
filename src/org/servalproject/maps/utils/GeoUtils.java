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

/**
 * a utility class that contains geographic utility methods
 */
public class GeoUtils {
	
	
	/*
	 * declare public class level constants
	 */
	
	/**
	 * flag to indicate use of the vincenty formula for distance calculations
	 */
	public static final int VINCENTY_FORMULA = 0;
	
	/**
	 * flag to indicate use of the haversine formula for distance calculations
	 */
	public static final int HAVERSINE_FORMULA = 1;
	
	/**
	 * flag to indicate that the distance must be in metres
	 */
	public static final int METRE_UNITS = 10;
	
	/**
	 * flag to indicate that the distance must be in miles
	 */
	public static final int MILE_UNITS  = 11;
	
	/**
	 * flag to indicate the the distance must be in nautical miles
	 */
	public static final int NAUTICAL_MILE_UNITS = 12;
	
	/*
	 * declare private class level constants
	 */
	public static final double MILE_CONVERT_FACTOR = 0.000621371192;
	public static final double NAUTICAL_MILE_CONVERT_FACTOR = 0.000539956803;
	
	/**
	 * calculate the distance between two sets of latitude and longitude coordinate pairs
	 * 
	 * @param lat1 the latitude of the first coordinate pair
	 * @param lon1 the longitude of the first coordinate pair
	 * @param lat2 the latitude of the second coordinate pair
	 * @param lon2 the longitude of the second coordinate pair
	 * @param formula a flag to indicate which formula to use
	 * @param units a flag to indicate the units of the result
	 * 
	 * @return the distance between the coordinate pairs in the units specified calculated with the specified formula
	 *         may return Double.NaN if the vincenty formula is used and the formula fails
	 */
	public static double calculateDistance(double lat1, double lon1, double lat2, double lon2, int formula, int units) {
		
		double mDistance = 0;
		
		// calculate the distance
		switch(formula){
		case VINCENTY_FORMULA:
			mDistance = vincentyFormula(lat1, lon1, lat2, lon2);
			break;
		case HAVERSINE_FORMULA:
			mDistance = haversineFormula(lat1, lon1, lat2, lon2);
			//mDistance = mDistance / 1000;
			break;
		default:
			throw new IllegalArgumentException("unknown formula flag detected");
		}
		
		// check to make sure the conversion went ok
		if(mDistance == Double.NaN) {
			return mDistance;
		}
		
		// convert distance to the required units
		switch(units) {
		case METRE_UNITS:
			return mDistance;
		case MILE_UNITS:
			return mDistance * MILE_CONVERT_FACTOR;
		case NAUTICAL_MILE_UNITS:
			return mDistance * NAUTICAL_MILE_CONVERT_FACTOR;
		default:
			throw new IllegalArgumentException("unknown unit flag detected");
		}
		
	}
	
	/* 
	 * calculate the distance between a coordinate pair using the haversine formula
	 * adapted from code found at the URL below:
	 * http://rosettacode.org/wiki/Haversine_formula#Java
	 */
	private static double haversineFormula(double lat1, double lon1, double lat2, double lon2) {
		
		double R = 6372.8; // In kilometers
		
		double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
	}

	/*
	 * calculate the distance between a coordinate pair using the vincenty formula
	 * adapted from code found at the URL below:
	 * http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java#9822531
	 * 
	 * Original JavaScript implementation of the formula available here:
	 * http://www.movable-type.co.uk/scripts/latlong-vincenty.html
	 * 
	 * Returns the distance in metres
	 */
	private static double vincentyFormula(double lat1, double lon1, double lat2, double lon2) {
		double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84 ellipsoid params
		double L = Math.toRadians(lon2 - lon1);
		double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
		double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

		double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
		double lambda = L, lambdaP, iterLimit = 100;
		do {
			sinLambda = Math.sin(lambda);
			cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
					+ (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
			if (sinSigma == 0)
				return 0; // co-incident points
				cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
				sigma = Math.atan2(sinSigma, cosSigma);
				sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
				cosSqAlpha = 1 - sinAlpha * sinAlpha;
				cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
				if (Double.isNaN(cos2SigmaM))
					cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (§6)
				double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
				lambdaP = lambda;
				lambda = L + (1 - C) * f * sinAlpha
						* (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
		} while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

		if (iterLimit == 0)
			return Double.NaN; // formula failed to converge

		double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
		double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
		double deltaSigma = B
				* sinSigma
				* (cos2SigmaM + B
						/ 4
						* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
								* (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		double dist = b * A * (sigma - deltaSigma);

		return dist;
	}

}
