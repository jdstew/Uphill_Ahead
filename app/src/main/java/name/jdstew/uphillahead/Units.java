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
 * The Units class provides unit-of-measurement and conversion methods used within the app.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Units {

    /**
     * Minimum distance, in meters, when a location matches another Node
     */
    public static final double NODE_EQUALS_MIN = 3.33; // meters (or 11 feet)

    /**
     * Minimum distance, in meters, when a location is "on" an existing Edge
     */
    public static final double NODE_TO_EDGE_MATCH = 10.0; // meters (or 33 feet)

    /**
     * A relative distance for generating debug comments when a location is
     * close-to but not within the parameters to match a Node or Edge. This
     * measurement is used to gauge correcting data.
     */
    public static final double NODE_TO_EDGE_CLOSE = 100.0;// 50.0; // meters

    /**
     * Conversion value from kilometers to miles (statute)
     */
    public static final double KILOMETERS_TO_MILES = 0.621371;

    /**
     * Conversion value from meters to feet
     */
    public static final double METERS_TO_FEET = 3.280840000;

    /**
     * Conversion value from degrees at average Earth circumference to meters
     */
    public static final double DEGREE_TO_METER = 111_195.0;

    /**
     * Correction factor used to match other measured distances of the PCT
     * to the values calculated by the data used in this app.
     */
    public static final double ROUTE_DIST_CORR = 1.02147882;
}