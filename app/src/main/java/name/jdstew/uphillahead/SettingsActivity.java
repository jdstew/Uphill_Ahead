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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


/**
 * The SettingsActivity class provides the screen for setting user-controls settings. *
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class SettingsActivity extends AppCompatActivity {

    Button about;
    Button legal;
    ScrollView scrollView;
    TextView route;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        route = findViewById(R.id.txt_selected_route_text);
        route.setOnClickListener(ocl -> startActivityRouteSelection());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        about = findViewById(R.id.btn_about);
        about.setOnClickListener(ocl -> startActivityAbout());

        legal = findViewById(R.id.btn_legal);
        legal.setOnClickListener(ocl -> startActivityLegal());

        scrollView = findViewById(R.id.view_scroll_view);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String graphName = prefs.getString("route_pref_key", getString(R.string.route_selected_default));
        route.setText(graphName);

        scrollView.scrollTo(0,0);
    }

    private void startActivityRouteSelection() {
        Intent i = new Intent(this, RouteListActivity.class);
        this.startActivity(i);
    }

    private void startActivityAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        this.startActivity(i);
    }

    private void startActivityLegal() {
        Intent i = new Intent(this, LegalActivity.class);
        this.startActivity(i);
    }
}