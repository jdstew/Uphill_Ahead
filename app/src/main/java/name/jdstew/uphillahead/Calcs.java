/**
 * Copyright 2023 Jeffrey D. Stewart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package name.jdstew.uphillahead;

import android.content.Context;
import android.util.TypedValue;

import java.text.NumberFormat;
import java.util.Objects;

/**
 * The Calcs class provides static methods for basic operations, including: measuring the distance
 * between coordinates, converting Android-specific values, providing easy to read measurement
 * String values, and such.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Calcs {

    /**
     * Converts dp measurement to pixels
     *
     * @param dp the DP value to convert to pixels
     * @param context of the app
     * @return pixel count for dp value
     */
	public static int dpToPx(float dp, Context context) {

		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}

    /**
     * Converts sp measurement to pixels
     *
     * @param sp the SP value to convert to pixels
     * @param context of the app
     * @return pixel count for sp value
     */
    public static int spToPx(float sp, Context context) {

		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
				context.getResources().getDisplayMetrics());
	}

    /**
     * Major axis of Earth, in meters
     */
    private static final double MAJOR_AXIS_RADIUS = 6_378_137.0; // meters, Semi-major axis

    /**
     * Average axis of Earth, in meters
     */
    private static final double AVERAGE_RADIUS = 6_371_008.8; // meters, average

    /**
     * Minor axis of Earth, in meters
     */
    private static final double MINOR_AXIS_RADIUS = 6_356_752.314_245; // meters, Semi-minor axis

    /**
     * Inverse flattening, measure of compression between circle to ellipse or
     * sphere to an ellipsoid of revolution
     */
    private static final double INVERSE_FLATTENING = 1.0 / 298.257_223_563; // Inverse flattening

    /**
     * Get miles from meters
     *
     * @param meters distance in meters to convert
     * @return miles from the meters value
     */
    public static double getMetersToMiles(double meters) {
        return meters / 1000.0 * Units.KILOMETERS_TO_MILES;
    }

    /**
     * Get feet from meters
     *
     * @param meters distance in meters to convert
     * @return feet from the meters value
     */
    public static double getFeet(double meters) {
        return meters * Units.METERS_TO_FEET;
    }

    /**
     * Get distance between two degree coordinates, defaults to precise measurement
     *
     * @param lat1 Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2 Latitude of first coordinate
     * @param lon2 Longitude of first coordinate
     * @return Distance between the two coordinates, in meters
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        return getPreciseDist(lat1, lon1, lat2, lon2);
    }

    /**
     * Get distance between two degree coordinates, defaults to precise measurement
     *
     * @param lat1    Latitude of first coordinate
     * @param lon1    Longitude of first coordinate
     * @param lat2    Latitude of first coordinate
     * @param lon2    Longitude of first coordinate
     * @param precise True returns a Vincenty-based calculation, false returns a
     *                Haversine-based calculation
     * @return Distance between the two coordinates, in meters
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2, boolean precise) {
        if (precise) {
            return getPreciseDist(lat1, lon1, lat2, lon2);
        } else {
            return getRoughDist(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Get rough distance between two degree coordinates, defaults to precise
     * measurement
     *
     * @param lat1 Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2 Latitude of first coordinate
     * @param lon2 Longitude of first coordinate
     * @return Distance between the two coordinates, in meters
     */
    private static double getRoughDist(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaLambda = Math.toRadians(lon2 - lon1);
        return Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1) * Math.cos(phi2) * Math.cos(deltaLambda))
                * AVERAGE_RADIUS;
    }

    /**
     * Get precise distance between two degree coordinates, defaults to precise
     * measurement. This class was derived from Vincenty's formulas provided at this
     * Wikipedia web page:
     * <a href="https://en.wikipedia.org/wiki/Vincenty%27s_formulae">Vincenty's_formulae</a>
     *
     * Assumptions were made to simplify this class, including:
     * - The coordinates will not be the same
     * - The coordinates will not be anitpodal (i.e., exact oppposite sides of Earth)
     * - The coordinates will not both lie on the equator
     *
     * @param lat1 Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2 Latitude of first coordinate
     * @param lon2 Longitude of first coordinate
     * @return Distance between the two coordinates, in meters (as the reference)
     */
    public static double getPreciseDist(double lat1, double lon1, double lat2, double lon2) {

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double lambda1 = Math.toRadians(lon1);
        double lambda2 = Math.toRadians(lon2);

        double L = lambda2 - lambda1;
        double U1 = Math.atan((1.0 - INVERSE_FLATTENING) * Math.tan(phi1));
        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double U2 = Math.atan((1.0 - INVERSE_FLATTENING) * Math.tan(phi2));
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambda = L;
        double sinLambda;
        double cosLambda;
        double sigma = 0.0;
        double sinSigma = 0.0;
        double cosSigma = 0.0;
        double cos2SigmaM = 0.0;
        double cosSqAlfa = 0.0;
        double sinSqsigma;
        double sinAlfa;
        double C;

        double lambdaPrime = 0.0;
        while (Math.abs(lambda - lambdaPrime) > 1e-9) {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSqsigma = Math.pow((cosU2 * sinLambda), 2.0)
                    + Math.pow((cosU1 * sinU2 - sinU1 * cosU2 * cosLambda), 2.0);
            sinSigma = Math.sqrt(sinSqsigma);
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlfa = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlfa = 1.0 - sinAlfa * sinAlfa;
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlfa;
            C = INVERSE_FLATTENING / 16.0 * cosSqAlfa * (4.0 + INVERSE_FLATTENING * (4.0 - 3.0 * cosSqAlfa));
            lambdaPrime = lambda;
            lambda = L + (1.0 - C) * INVERSE_FLATTENING * sinAlfa
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
        }

        double uSq = cosSqAlfa * (MAJOR_AXIS_RADIUS * MAJOR_AXIS_RADIUS - MINOR_AXIS_RADIUS * MINOR_AXIS_RADIUS)
                / (MINOR_AXIS_RADIUS * MINOR_AXIS_RADIUS);
        double A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)));
        double B = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                - B / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));

        return MINOR_AXIS_RADIUS * A * (sigma - deltaSigma);
    }

    /**
     * Returns the adjusted Euclidean space used for small distance measurements,
     * see <a href="https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line">
     *     distance from a point to a line</a>
     *
     * @param n Node to measure to
     * @param e Edge to measure from
     * @return
     */
    public static double getNodeToEdgeDist(Node n, Edge e) {
        Node a = e.getPrevNode();
        Node b = e.getNextNode();

        double lonCorr = Math.cos(Math.toRadians((a.getLatitude() + b.getLatitude()) / 2.0));

        // _____/\n
        // ____/__\
        // ___/____\
        // __/______\
        // a/________\b
        //
        // an and nb must be less than ab
        double ab = Math.sqrt(Math.pow(a.getLatitude() - b.getLatitude(), 2.0)
                + Math.pow((a.getLongitude() - b.getLongitude()) * lonCorr, 2.0));
        double an = Math.sqrt(Math.pow(a.getLatitude() - n.getLatitude(), 2.0)
                + Math.pow((a.getLongitude() - n.getLongitude()) * lonCorr, 2.0));
        double nb = Math.sqrt(Math.pow(n.getLatitude() - b.getLatitude(), 2.0)
                + Math.pow((n.getLongitude() - b.getLongitude()) * lonCorr, 2.0));
        if (an > ab || nb > ab) {
            return Double.MAX_VALUE; // meaning, angle n is acute
        }

        return Math
                .abs((b.getLongitude() - a.getLongitude()) * lonCorr * (a.getLatitude() - n.getLatitude())
                        - (a.getLongitude() - n.getLongitude()) * lonCorr * (b.getLatitude() - a.getLatitude()))
                / ab * Units.DEGREE_TO_METER;
    }

    /**
     * Get miles from kilometers
     *
     * @param kilometers the distance in kilometers to convert to miles
     * @return miles from the distance in kilometers
     */
    public static double getKmToMile(double kilometers) {
        return kilometers * Units.KILOMETERS_TO_MILES;
    }

    /**
     * Get feet from meters
     *
     * @param meters the distance in meters to convert to feet
     * @return feet from the distance in meters
     */
    public static double getMetersToFeet(double meters) {
        return meters * Units.METERS_TO_FEET;
    }

    /**
     * Calculates hiker pace at slope and elevation
     *
     * @param elev  Elevation of hiker, in meters
     * @param slope Slope of trail ahead
     * @return Pace of hiker, reduced by percent of oxygen compared to sea level, in kilometers per hour (km/hr)
     */
    public static double getPace(double slope, double elev) {
        return getPaceAtElev(getPaceAtSlope(slope), elev, slope);
    }

    /**
     * Calculates the pace of a hiker given a slope. This equation is derived from:
     * <a href="https://en.wikipedia.org/wiki/Tobler%27s_hiking_function">...</a>
     *
     * @param slope Slope, in percentage
     * @return Pace, in kilometers per hour (km/hr)
     */
    public static double getPaceAtSlope(double slope) {
        // from Excel formula:  =6*EXP(-3.5*ABS(TAN(RADIANS(A17)) + 0.05))
        return 6.0 * Math.exp(-3.5 * Math.abs(slope + 0.05));
    }

    /**
     * Calculates a corrected pace of a hiker given the typical percentage of oxygen
     * at a specified elevation. This equation is derived from data at:
     * <a href="https://wildsafe.org/resources/ask-the-experts/altitude-safety-101/oxygen-levels/">...</a>
     *
     * @param pace  Pace of hiker in kilometers per hour (km/hr)
     * @param elev  Elevation of hiker, in meters
     * @param slope Slope of trail ahead
     * @return Pace of hiker, reduced by percent of oxygen compared to sea level
     */
    public static double getPaceAtElev(double pace, double elev, double slope) {
        if (slope > 0.0) {
            return pace * Math.exp(-0.0001 * elev);
        } else {
            return pace;
        }
    }

    /**
     * Calculates the duration given speed and distance.
     *
     * @param pace Pace in kilometers per hour (km/hr)
     * @param dist Distance in meters
     * @return Duration in hours
     */
    public static double getDuration(double pace, double dist) {
        return pace / dist;
    }

    /**
     * Returns a printable display of time in hours:minutes format.
     *
     * @param time in decimal hours
     * @param system see Config class for metric or imperial
     * @return displayed time
     */
    public static String getDisplayedTime(double time, String system) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumIntegerDigits(2);

        // examples: "+0:45", "+1:46"
        if (system.compareTo(Config.SYSTEM_IMPERIAL) == 0) {
            time = time / Units.KILOMETERS_TO_MILES;
            int hours = (int)time;
            int minutes = (int)((time - (double)hours) * 60);

            return hours + ":" + nf.format(minutes);
        } else {
            int hours = (int)time;
            int minutes = (int)((time - (double)hours) * 60);

            return hours + ":" + nf.format(minutes);
        }
    }

    /**
     * Returns a printable display of distance in a system-appropriate format.
     *
     * @param dist in meters
     * @param system see Config class for metric or imperial
     * @return displayed distance
     */
    public static String getDisplayedDist(double dist, String system) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumIntegerDigits(1);

        if (system.compareTo(Config.SYSTEM_IMPERIAL) == 0) {
            dist = Calcs.getMetersToMiles(dist);

            if (dist >= 1.0) {
                nf.setMinimumFractionDigits(1);
                return nf.format(dist) + "mi";
            } else if (dist >= 0.25) { // 100 yards
                nf.setMaximumFractionDigits(2);
                return nf.format(dist) + "mi";
            } else {
                nf.setMaximumFractionDigits(0);
                return nf.format(dist * 1_760.0) + "yds";
            }
        } else {
            if (dist >= 1_000.0) {
                nf.setMinimumFractionDigits(1);
                return nf.format(dist / 1_000.0) + "km"; // 1,000 meters
            } else {
                nf.setMaximumFractionDigits(0);
                return nf.format(dist) + "m";
            }
        }
    }

    /**
     * Returns a printable display of elevation in a system-appropriate format.
     *
     * @param elev in meters
     * @param system see Config class for metric or imperial
     * @return displayed distance
     */
    public static String getDisplayedElev(double elev, String system) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);

        if (Objects.equals(system, Config.SYSTEM_IMPERIAL)) {
            elev = Calcs.getMetersToFeet(elev);
            return nf.format(elev) + "ft";
        } else {
            return nf.format(elev) + "m";
        }
    }
}
