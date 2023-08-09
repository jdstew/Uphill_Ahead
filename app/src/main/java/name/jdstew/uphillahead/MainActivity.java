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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The MainActivity class provides the entry Activity for this Android app.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "name.jdstew.uphillahead.MainActivity";
    public static final int PERMISSIONS_REQUEST_LOCATION_ID = 99;
    private SharedPreferences prefs;
    private TextView txtRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        txtRoute = findViewById(R.id.txtRoute);

        ImageView imgSettings = findViewById(R.id.icoSettings);
        imgSettings.setOnClickListener(ocl -> startActivitySettings());

        GraphView graphView = new GraphView(this);
        graphView.setZ(-1.0f);
        graphView.setWillNotDraw(false);
//        TODO: need to set background of GraphView based upon day/night setting
//        graphView.setBackgroundColor(getResources().getColor(R.color.green_usfs, null));

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imgSettings.getHeight()); //ViewGroup.LayoutParams.MATCH_PARENT);

        ConstraintLayout mainLayout = findViewById(R.id.layoutMainActivity);
        mainLayout.addView(graphView, params);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String graphName = prefs.getString("route_pref_key", getString(R.string.txt_route_title));
//        Log.d(DEBUG_TAG, "Stored route preference is " + graphName);


        // TODO: why can't I use CONFIG.<String> values for preference keys?
        String directionPref = prefs.getString("direction_pref_key", Config.DIRECTION_TO_DEFAULT);
        String directionText;

        // TODO: is the following killing the process?
        Graph graph = GraphManager.getInstance(getApplicationContext()).getGraph(graphName);
        if (graph != null && graph.getName() != null) { // confirms the graph is not virtually null
//            Log.d(DEBUG_TAG, "Graph is " + graph);
            if (directionPref.compareTo(Config.DIRECTION_TO_END) == 0) {
                directionText = graph.getStartDescription();
//                Log.d(DEBUG_TAG, "Graph direction is " + graph.getStartDescription());
            } else {
                directionText = graph.getEndDescription();
//                Log.d(DEBUG_TAG, "Graph direction is " + graph.getEndDescription());
            }
        } else {
            directionText = directionPref;
//            Log.d(DEBUG_TAG, "Direction pref is " + directionPref);
        }

        txtRoute.setText(graphName + ", " + directionText);
    }

    public boolean checkLocationPermission() {
        //check the location permissions and return true or false.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permissions granted
            Toast.makeText(getApplicationContext(), "Location services granted", Toast.LENGTH_LONG).show();
            return true;
        } else {
            //permissions NOT granted - if permissions are NOT granted, ask for permissions
            Toast.makeText(getApplicationContext(), "Please enable permissions", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permissions request rational")
                        .setMessage("Enabling GPS allows this app to locate you on the trail.")
                        .setPositiveButton("Ok, I agree", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSIONS_REQUEST_LOCATION_ID);
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION_ID);
            }
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_LOCATION_ID) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Cannot get the location!")
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        }
    }

    private void startActivitySettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivity(i);
    }
}