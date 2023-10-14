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
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
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
public class Observer extends Thread implements OnCompleteListener, OnSuccessListener<Location>, SharedPreferences.OnSharedPreferenceChangeListener {

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
     * Maximum acceptable delay, to determine if the recent update is old or not, in nanoseconds
     * This period is when the active precise location is provided, versus LKP
     */
    public static final long UPDATE_FIX_MAX_AGE = 500_000_000; // 0.5 seconds in nanoseconds

    private final GraphView graphView;
    private final MainActivity mainActivity;
    String locationSource;
    private long locationServicesUpdatePeriod;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest.Builder locBuilder;
    private final LocationRequest locRequest;
    private final ObserverLocationListener locListener;
    private final SharedPreferences prefs;

    private CountDownLatch countDownLatch;

    private static Observer observerSingleton;

    public static Observer getInstance(GraphView graphView, MainActivity mainActivity) {
        observerSingleton = new Observer(graphView, mainActivity);

        return observerSingleton;
    }
    public static Observer getInstance() {
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

        String updatePeriod = prefs.getString("location_update_pref_key", Config.LOCATION_UPDATE_DEFAULT);
        locationServicesUpdatePeriod = Config.getGpsUpdateValue(updatePeriod);
//        Log.d(DEBUG_TAG, "Source: " + locationSource + "; sleep period is: " + gpsUpdate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(graphView.getContext());

        locBuilder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3L);
        locBuilder.setGranularity(Granularity.GRANULARITY_FINE);
        locBuilder.setIntervalMillis(locationServicesUpdatePeriod * 60 * 1_000);
        locBuilder.setMaxUpdateAgeMillis(0L); // only the most recent location
        locBuilder.setMinUpdateIntervalMillis(10_000); // can accept locations every 10 seconds
        locBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locRequest = locBuilder.build();
        locListener = new Observer.ObserverLocationListener(this);

        if (locationSource.compareTo(Config.SOURCE_LOCATION_SERVICES) == 0) {
            startDeviceLocationServices();
        } else if (locationSource.compareTo(Config.SOURCE_SIMULATED) == 0) {
            double latitude = Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE)));
            double longitude = Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE)));
            double elevation = Double.parseDouble(prefs.getString("location_elevation_pref_key", Double.toString(Config.LOCATION_DEFAULT_ELEVATION)));

            setSimulatedLocation(latitude, longitude, elevation);
        } else {
//            Log.e(DEBUG_TAG, "Location source of " + locationSource + " is not understood");
        }
    }

    public void startDeviceLocationServices() {

        // get LKP if available, get it
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> lkpTask;
            lkpTask = fusedLocationClient.getLastLocation();
            lkpTask.addOnCompleteListener(this);
//            Log.d(DEBUG_TAG, "Last location requested");
        }

        // start device updates
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locRequest, locListener, Looper.getMainLooper());
//            Log.d(DEBUG_TAG, "Precise/fine location updates requested...");
        } else {
//            Log.e(DEBUG_TAG, "Insufficient permissions to get precise position.");
        }
    }

    public void stopDeviceLocationServices() {
        fusedLocationClient.removeLocationUpdates(locListener);
    }

    public void setSimulatedLocation(double latitude, double longitude, double elevation) {
        stopDeviceLocationServices();
        locationSource = Config.SOURCE_SIMULATED;
//        Log.d(DEBUG_TAG, "Observer set to simulated position");

        // editing the preferences will trigger onSharedPreferenceChanged() within this class
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("source_pref_key", Config.SOURCE_SIMULATED);
        editor.putString("location_latitude_pref_key", Double.toString(latitude));
        editor.putString("location_longitude_pref_key", Double.toString(longitude));
        editor.putString("location_elevation_pref_key", Double.toString(elevation));
        editor.apply();

        Location location = new Location("Simulated");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        location.setTime(System.currentTimeMillis());
        onSuccess(location);
    }

    @Override
    public void onComplete(@NonNull Task task) {
        Location location = (Location)task.getResult();
        if (location != null) {
//            Log.i(DEBUG_TAG, "LKP is: " + location);
            onSuccess(location);
        } else {
//            Log.e(DEBUG_TAG, "Last known position (LKP) Location was NULL");
        }
    }

    @Override
    public void onSuccess(Location location) {
//        Log.i(DEBUG_TAG, "Device location services update via onSuccess() from " + location.getProvider());
        graphView.onSuccess(location);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefs_key) {
//        Log.d(DEBUG_TAG, "preferences change listener called");

        // change to the newly set location source
        if (prefs_key.compareTo("source_pref_key") == 0) {
            locationSource = sharedPreferences.getString("source_pref_key", Config.SOURCE_DEFAULT);

            if (locationSource.compareTo(Config.SOURCE_LOCATION_SERVICES) == 0) {
                startDeviceLocationServices();
            } else if (locationSource.compareTo(Config.SOURCE_SIMULATED) == 0) {
                double latitude = Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE)));
                double longitude = Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE)));
                double elevation = Double.parseDouble(prefs.getString("location_elevation_pref_key", Double.toString(Config.LOCATION_DEFAULT_ELEVATION)));

                setSimulatedLocation(latitude, longitude, elevation);
            } else {
//                Log.e(DEBUG_TAG, "Location source of " + locationSource + " is not understood (at shared preference change)");
            }

        // change to the newly set periodicity of location
        } else if (prefs_key.compareTo("location_update_pref_key") == 0) {
            String updatePeriod = sharedPreferences.getString("location_update_pref_key", Config.LOCATION_UPDATE_DEFAULT);
            locationServicesUpdatePeriod = Config.getGpsUpdateValue(updatePeriod);
            locBuilder.setIntervalMillis(locationServicesUpdatePeriod * 60 * 1_000);
            if (locationSource.compareTo(Config.SOURCE_LOCATION_SERVICES) == 0) {
                stopDeviceLocationServices();
                startDeviceLocationServices();
            }
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

        public void onLocationChanged(@NonNull Location location) {
//            Log.i(DEBUG_TAG, "Device (GPS) is: " + location);
            observer.onSuccess(location);
        }
    }
}
