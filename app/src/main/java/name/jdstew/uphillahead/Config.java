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

/**
 * The Config class provides app-wide configuration data.  While this application does make
 * best use of the Android Studio specific strings.xml class, this class provides tools needed
 * to convert those Strings into usable values.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Config {
    /**
     * Default stroke with representing Earth's surface in the displayed graph
     */
    public static final float SURFACE_STROKE_WIDTH = 3.0f;
    /**
     * Preferences key for the current Node's hash code value
     */
    public static final String NODE_HASHCODE_KEY = "node_hashcode_pref_key";
    /**
     * Waypoint's in Halfmile's data that contains this name are not included in the Node's
     * detailed information.
     */
    public static final String WAYPOINT_SKIP_1 = "Triangle, Red";

    /**
     * Observer latitude preference
     */
    public static final String LOCATION_LATITUDE_DEFAULT = "location_latitude_pref_key";
    /**
     * Observer longitude preference
     */
    public static final String LOCATION_LONGITUDE_DEFAULT = "location_longitude_pref_key";
    public static final String LOCATION_ELEVATION_DEFAULT = "location_elevation_pref_key";
    /**
     * Observer default latitude
     */
    public static final double LOCATION_DEFAULT_LATITUDE = 40.0;
    /**
     * Observer default longitude
     */
    public static final double LOCATION_DEFAULT_LONGITUDE = -121.0;
    public static final double LOCATION_DEFAULT_ELEVATION = 0.0;

    /**
     * The selected Graph (Route)
     */
    public static final String ROUTE_KEY = "route_pref_key";
    /**
     * The default selected Graph (Route)
     */
    public static final String ROUTE_DEFAULT = "CA Sec A";
    /**
     * The direction along a Graph (Route)
     */
    public static final String DIRECTION_KEY = "direction_pref_key";
    /**
     * Graph in the direction toward the end of the Graph (Route), aka the destination
     */
    public static final String DIRECTION_TO_END = "Northbound";
    /**
     * Graph in the direction toward the start of the Graph (Route), aka the trailhead
     */
    public static final String DIRECTION_TO_START = "Southbound";
    /**
     * The default direction of the displayed graph
     */
    public static final String DIRECTION_TO_DEFAULT = DIRECTION_TO_END;

    /**
     * Observer's source of positioning
     */
    public static final String SOURCE_KEY = "source_pref_key";
    /**
     * Use GPS for precise positioning, at the periodicity set by another preference
     */
    public static final String SOURCE_LOCATION_SERVICES = "Device location services";
    /**
     * Use simulated position
     */
    public static final String SOURCE_SIMULATED = "Simulated";
    /**
     * Default setting for using the Observer's position
     */
    public static final String SOURCE_DEFAULT = SOURCE_LOCATION_SERVICES;

    /**
     * Default setting for determining if a location update is recent
     */
    public static final long LOCATION_RECENT = 180_000L;

    /**
     * Preference for how often the GPS is used to get a precise location.
     */
    public static final String LOCATION_UPDATE_KEY = "location_update_pref_key";
    /**
     * GPS precise update period of 1 minute.
     */
    public static final String LOCATION_UPDATE_1_MIN = "Once per minute";
    /**
     * GPS precise update period of 6 minutes.
     */
    public static final String LOCATION_UPDATE_6_MIN = "Every 6 minutes";
    /**
     * GPS precise update period of 15 minutes.
     */
    public static final String LOCATION_UPDATE_15_MIN = "Every 15 minutes";
    /**
     * GPS precise update period default.
     */
    public static final String LOCATION_UPDATE_DEFAULT = LOCATION_UPDATE_6_MIN;

    /**
     * When GPS in enabled for precise positioning, Person (Observer's)
     * update period.
     * @param value in String representation
     * @return update period in integer minutes
     */
    public static long getGpsUpdateValue(String value) {
        switch (value) {
            case LOCATION_UPDATE_1_MIN:
                return 1;
            case LOCATION_UPDATE_15_MIN:
                return 15;
            case LOCATION_UPDATE_6_MIN:
            default:
                return 6;
        }
    }

    public static final double MAX_DIST_TO_GRAPH_EDGE	= 75.0;

    /**
     * Person (Observer's) minimum distance to "snap-to" nearest Graph
     * Node or Edge.
     */
    public static final String SNAP_TO_TRAIL_KEY = "snap_to_trail_pref_key";
    /**
     * Snap to trail distance of not more than 15 meters.
     */
    public static final String SNAP_TO_TRAIL_15_M = "15 meters / 49 feet";
    /**
     * Snap to trail distance of not more than 30 meters.
     */
    public static final String SNAP_TO_TRAIL_30_M = "30 meters / 98 feet";
    /**
     * Snap to trail distance of not more than 45 meters.
     */
    public static final String SNAP_TO_TRAIL_45_M = "45 meters / 148 feet";
    /**
     * Snap to trail distance of not more than 60 meters.
     */
    public static final String SNAP_TO_TRAIL_60_M = "60 meters / 197 feet";
    /**
     * Snap to trail distance of not more than 75 meters.
     */
    public static final String SNAP_TO_TRAIL_75_M = "75 meters / 246 feet";
    /**
     * Snap to trail distance default value.
     */
    public static final String SNAP_TO_TRAIL_DEFAULT = SNAP_TO_TRAIL_15_M;

    /**
     * Get distance of snap-to-trail based upon static String values
     * @param value of snap-to-trail description
     * @return snap-to-trail in integer value
     */
    public static int getSnapToTrailValue(String value) {
        switch (value) {
            case SNAP_TO_TRAIL_30_M:
                return 30;
            case SNAP_TO_TRAIL_45_M:
                return 45;
            case SNAP_TO_TRAIL_60_M:
                return 60;
            case SNAP_TO_TRAIL_75_M:
                return 75;
            case SNAP_TO_TRAIL_DEFAULT:
            default:
                return 15;
        }
    }

    /**
     * Exaggeration multiplier of vertical elevations relative to horizontal distances.
     */
    public static final String EXAGGERATION_KEY = "exaggeration_pref_key";
    /**
     * Exaggeration minimum multiplier of vertical elevations.
     */
    public static final double EXAGGERATION_MIN = 1.0;
    /**
     * Exaggeration default multiplier of vertical elevations.
     */
    public static final double EXAGGERATION_DEFAULT = 10.0;
    /**
     * Exaggeration maximum multiplier of vertical elevations.
     */
    public static final double EXAGGERATION_MAX = 20.0;

    /**
     * Pace bias for time calculations; that is, a multiplier based upon a normal pace
     * of approximately 5 Km/hr
     */
    public static final String PACE_KEY = "pace_pref_key";
    /**
     * The minimum pace for an easy trail, in km/hr
     */
    public static final double DIFFICULTY_EASY_MIN = 4.2;
    /**
     * The maximum pace for a difficult trail, in km/hr
     */
    public static final double DIFFICULTY_HARD_MAX = 3.2f;
    /**
     * Pace multiplier used to convert seek bar widget integer values to actual computation values.
     */
    public static final float PACE_PREFS_MULTIPLIER = 0.1f;
    /**
     * Minimum pace bias limit, or one-half a normal person's pace.
     */
    public static final float PACE_BIAS_MIN = 0.5f;
    /**
     * Default pace bias, or normal pace.
     */
    public static final float PACE_BIAS_DEFAULT = 1.0f;
    /**
     * Maximum pace bias limit, or twice a normal person's pace.
     */
    public static final float PACE_BIAS_MAX = 2.0f;

    /**
     * Distance and speed measurement system.
     */
    public static final String SYSTEM_KEY = "system_pref_key";
    /**
     * Metric system for distance and speed measurement.
     */
    public static final String SYSTEM_METRIC = "Metric (m, km, km/hr)";
    /**
     * Imperial (aka English, Standard) system for distance and speed measurement.
     */
    public static final String SYSTEM_IMPERIAL = "Imperial (ft, mi, mi/hr)";
    /**
     * Default distance and speed measurement system.
     */
    public static final String SYSTEM_DEFAULT = SYSTEM_IMPERIAL;

    /**
     * Graphical zoom level, in meters.
     */
    public static final String ZOOM_KEY = "zoom_pref_key";
    /**
     * Graphical zoom minimum, in meters.
     */
    public static final double ZOOM_MIN = 500.0f; // 0.3 miles
    /**
     * Graphical zoom default, in meters.
     */
    public static final double ZOOM_DEFAULT = 5_000.0f; // 3.1 miles
    /**
     * Graphical zoom maximum, in meters.
     */
    public static final double ZOOM_MAX = 40_000.0f; // 24.8 miles

    /**
     * Vertical display bias; negative values moves the displayed Graph 'up' and positive values
     * moves the displayed Graph 'down'.
     */
    public static final String VERTICAL_BIAS_KEY = "vertical_bias_pref_key";
    /**
     * Vertical bias minimum, or 90% up
     */
    public static final double VERTICAL_BIAS_MIN = -0.85;
    /**
     * Vertical bias default, centered vertically
     */
    public static final double VERTICAL_BIAS_DEFAULT = 0.0;
    /**
     * Vertical bias maximum, or 90% down
     */
    public static final double VERTICAL_BIAS_MAX = 0.85;
}
