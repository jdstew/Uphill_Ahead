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

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The Observer class provides the location of the user, which is consumed by the GraphView.  The
 * Observer can provide simulated, and rough and fine precision locations based upon the user's
 * device capabilities and permissions.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Observer extends Thread implements Runnable, OnSuccessListener<Location>, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DEBUG_TAG = "name.jdstew.uphillahead.Observer";
    /**
     * Minimum accepted accuracy (within stated 68% percentile), in meters
     */
    public static final double GPS_MIN_LKP_ACCURACY = 10.0; // in meters
    /**
     * Maximum age of last-known-position (LKP), in milliseconds
     */
    public static final long GPS_MIN_LKP_AGE = 60_000; // 1 minute in milliseconds
    /**
     * Preferred location update period, in milliseconds
     */
    public static final long UPDATE_INTERVAL_PREFERRED = 10_000; // 10-second preferred update interval
    /**
     * Minimum location update period, in milliseconds
     */
    public static final long UPDATE_INTERVAL_MINIMUM = 3_000; // 6-second update minimum interval
    /**
     * Maximum period for location updates before stopping (to conserve battery health), in millisecons
     */
    public static final long UPDATE_STAY_ALIVE_PERIOD = 30_000; // automatically shut down GPS after 30-seconds
    /**
     * Maximum number of location updates before stopping (to conserve battery health)
     */
    public static final int UPDATE_COUNT_MAX = 3; // automatically shut down GPS after 3 successful updates
    /**
     * Maximum acceptable delay, to determine if the recent update is old or not, in nanoseconds
     * This period is when the active precise location is provided, versus LKP
     */
    public static final long UPDATE_FIX_MAX_AGE = 500_000_000; // 0.5 seconds in nanoseconds

    private final GraphView graphView;
    private final MainActivity mainActivity;
    String locationSource;
    private long gpsSleepPeriod;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private boolean fineLocationGranted;
    private boolean coarseLocationGranted;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locRequest;
    private final ObserverLocationListener locListener;
    private Location bestGpsLocation;
    private static Location nonGpsLocation;
    private final SharedPreferences prefs;

    private CountDownLatch countDownLatch;

    private static Observer observerSingleton;

    public static Observer getInstance() {
        return observerSingleton;
    }

    public static Observer createInstance(GraphView graphView, MainActivity mainActivity) {
        observerSingleton = new Observer(graphView, mainActivity);

        return observerSingleton;
    }

    /**
     * Constructor of Observer, which uses simulated and Fused Location Provider
     *
     * @param mainActivity should be the SurfaceView within the main activity of the apply
     */
    private Observer(GraphView graphView, MainActivity mainActivity) {
        this.graphView = graphView;
        this.mainActivity = mainActivity;

        prefs = PreferenceManager.getDefaultSharedPreferences(graphView.getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        locationSource = prefs.getString("source_pref_key", Config.SOURCE_DEFAULT);

        nonGpsLocation = new Location("Default");
        nonGpsLocation.setLatitude(Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE))));
        nonGpsLocation.setLongitude(Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE))));

        String gpsUpdate = prefs.getString("gps_update_pref_key", Config.GPS_UPDATE_DEFAULT);
        gpsSleepPeriod = Config.getGpsUpdateValue(gpsUpdate);
//        Log.d(DEBUG_TAG, "Source: " + locationSource + "; sleep period is: " + gpsUpdate);

        if (mainActivity.checkLocationPermission()) {
            this.setCoarseLocationGranted(true);
            this.setFineLocationGranted(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(graphView.getContext());

		/*
			> requests a tradeoff that favors highly accurate locations at the possible expense
			  of additional power usage
			> will delay delivery of initial low accuracy locations for a small amount of time
			  in case a high accuracy location can be delivered instead
			> will not update every 6 seconds
		*/
        LocationRequest.Builder locBuilder = new LocationRequest.Builder(UPDATE_INTERVAL_PREFERRED);
        locBuilder.setMinUpdateIntervalMillis(UPDATE_INTERVAL_MINIMUM);
        locBuilder.setDurationMillis(UPDATE_STAY_ALIVE_PERIOD);
        locBuilder.setMaxUpdates(UPDATE_COUNT_MAX); // automatically shut down GPS after 3 successful updates
        locRequest = locBuilder.build();
        locListener = new Observer.ObserverLocationListener(this);

        int updateCount = 0;
        double bestAccuracy = Double.MAX_VALUE;
        bestGpsLocation = null;
//        Log.d(DEBUG_TAG, "Observer starting...");
        start();
    }

    /**
     * Trigger the Observer to obtain a last known position (LKP) for its listener.
     */
    public Location getLocation() {
        if (locationSource.compareTo(Config.SOURCE_GPS) == 0) {
            if (bestGpsLocation != null) {
                return bestGpsLocation;
            } else {
//                Log.d(DEBUG_TAG, "GPS position not available yet");
                nonGpsLocation.setLatitude(Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE))));
                nonGpsLocation.setLongitude(Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE))));
                return nonGpsLocation;
            }
        } else if (locationSource.compareTo(Config.SOURCE_LKP) == 0) {
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> lkpTask;
                lkpTask = fusedLocationClient.getLastLocation();
                while (!lkpTask.isComplete()) {
                    // wait - this should be nearly instantaneous
                }
                try {
                    Location location = lkpTask.getResult();
                    if (location != null) {
//                        Log.i(DEBUG_TAG, "LKP is: " + location);
                        return location;
                    } else {
//                        Log.e(DEBUG_TAG, "Last known position Location return NULL");
                    }
                } catch (RuntimeExecutionException e) {
//                    Log.e(DEBUG_TAG, "Error in obtaining Location from Task: " + e);
                }
            } else {
//                Log.e(DEBUG_TAG, "Course location permission denied");
            }
            // assumes nonGpsLocation is simulated or defaulted
//            Log.e(DEBUG_TAG, "Unable to obtain a LKP");
            nonGpsLocation.setLatitude(Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE))));
            nonGpsLocation.setLongitude(Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE))));
            return nonGpsLocation;
        } else {
            // assumes nonGpsLocation is simulated or defaulted
            double savedLatitude = Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE)));
            double savedLongitude = Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE)));
            Graph graph = GraphManager.getInstance(graphView.getContext()).getGraph(prefs.getString("route_pref_key", String.valueOf(R.string.txt_route_title)));
            if(graph.isNodeInExtents(new Node(savedLatitude, savedLongitude, 0.0))) {
                nonGpsLocation.setLatitude(savedLatitude);
                nonGpsLocation.setLongitude(savedLongitude);
            } else if (prefs.getString("direction_pref_key", Config.DIRECTION_TO_DEFAULT).compareTo(Config.DIRECTION_TO_END) == 0) {
//                Log.i(DEBUG_TAG, "Observer setting simulated location to trail start");
                nonGpsLocation.setLatitude(graph.getStartNode().getLatitude());
                nonGpsLocation.setLongitude(graph.getStartNode().getLongitude());
            } else {
//                Log.i(DEBUG_TAG, "Observer setting simulated location to trail end");
                nonGpsLocation.setLatitude(graph.getLastNode().getLatitude());
                nonGpsLocation.setLongitude(graph.getLastNode().getLongitude());
            }
            nonGpsLocation.setTime(0L);
            return nonGpsLocation;
        }
    }

    public void setSimulatedLocation(double latitude, double longitude) {
        locationSource = Config.SOURCE_SIM;

        // editing the preferences will trigger onSharedPreferenceChanged() within this class
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("source_pref_key", Config.SOURCE_SIM);
        editor.putString("location_latitude_pref_key", Double.toString(latitude));
        editor.putString("location_longitude_pref_key", Double.toString(longitude));
        editor.apply();

//        Log.d(DEBUG_TAG, "Observer set to simulated position");
    }


    /**
     * Implementation of the Runnable interface, which looks for a last-known-position, then turns on
     * the Fused Location Provider if the corresponding GPS setting and the app permissions are set
     */
    public void run() {
//        Log.d(DEBUG_TAG, "Observer entered run status.");

        if (locationSource.compareTo(Config.SOURCE_GPS) == 0) {
            if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locRequest, locListener, Looper.getMainLooper());
//                Log.d(DEBUG_TAG, "Location update requested...");
            } else {
//                Log.e(DEBUG_TAG, "Insufficient permissions to get precise position.");
            }
        } else {
//            Log.d(DEBUG_TAG, "GPS not currently in use");
        }

        countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await(gpsSleepPeriod, TimeUnit.MINUTES); // 2713 2973 3333
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();
        run();
    }

    @Override
    public void onSuccess(Location location) {
        // test code to short circuit the rest
        graphView.onSuccess(location);
        bestGpsLocation = location;
//        Log.d(DEBUG_TAG, "...onSuccess() location received at " + location.toString());


//        // track most accurate of ~3 fixes within ~30 seconds
//        ++updateCount;
//        Log.d(DEBUG_TAG, "Location update #" + updateCount + " received");
//        if (location.getAccuracy() < bestAccuracy) {
//            bestAccuracy = location.getAccuracy();
//            bestGpsLocation = location;
//        }
//
//        // update GraphView if fix is accurate, or with the most accurate one
//        // then go to sleep
//        if (updateCount == UPDATE_COUNT_MAX || bestAccuracy < GPS_MIN_LKP_ACCURACY) {
//            updateCount = 0;
//            bestAccuracy = Double.MAX_VALUE;
//            bestGpsLocation = null;
//
//            graphView.onSuccess(location);
//            fusedLocationClient.removeLocationUpdates(locListener);
//        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefs_key) {
//        Log.d(DEBUG_TAG, "preferences change listener called");

        // start location updates cycle if preferences switched to GPS
        // stop location updates cycle if preferences switched to simulate
        if (prefs_key.compareTo("source_pref_key") == 0) {
            locationSource = sharedPreferences.getString("source_pref_key", Config.SOURCE_DEFAULT);

            if (locationSource.compareTo(Config.SOURCE_GPS) == 0) {
                if (mainActivity.checkLocationPermission()) {
                    interrupt();
                }
            }
        // change to the newly set periodicity of GPS updates
        } else if (prefs_key.compareTo("gps_update_pref_key") == 0) {
            String gpsUpdate = sharedPreferences.getString("sourcePref", Config.GPS_UPDATE_DEFAULT);
            gpsSleepPeriod = Config.getGpsUpdateValue(gpsUpdate);
        }
    }

    /**
     * Subclass used to provide a location provider created by the Fused Location Provider
     */
    class ObserverLocationListener implements LocationListener {
        Observer observer;

        public ObserverLocationListener(Observer obs) {
            this.observer = obs;
        }

        public void onLocationChanged(Location location) {
//            Log.d(DEBUG_TAG, "...LocationListener location received at " + location.toString());
            // update location, if new location is less than 0.5 seconds old
            // re: API, "does not imply that it will always represent the current location"
//            if (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos () < UPDATE_FIX_MAX_AGE) {
                observer.onSuccess(location);
//            }
        }
    }

    public Boolean getFineLocationGranted() {
        return fineLocationGranted;
    }

    public void setFineLocationGranted(Boolean fineLocationGranted) {
        this.fineLocationGranted = fineLocationGranted;
    }

    public Boolean getCoarseLocationGranted() {
        return coarseLocationGranted;
    }

    public void setCoarseLocationGranted(Boolean coarseLocationGranted) {
        this.coarseLocationGranted = coarseLocationGranted;
    }
}
