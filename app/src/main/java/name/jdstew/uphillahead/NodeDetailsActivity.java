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

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * The PopupDetailsActivity class provides a view of node details within the graph's view with
 * other options for the user to select from.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class NodeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String description = extras.getString("description");
            double latitude = extras.getDouble("latitude");
            double longitude = extras.getDouble("longitude");

            TextView txtDescription = findViewById(R.id.node_details_text);
            txtDescription.setText(description);

            Button btnSimulate = findViewById(R.id.simulate_button);
            btnSimulate.setOnClickListener(ocl -> {
                Observer observer = Observer.getInstance();
                if (observer != null) {
                    observer.setSimulatedLocation(latitude, longitude);
                }
                this.finish();
            });

            Button btnGoogleMaps = findViewById(R.id.google_map_button);
            btnGoogleMaps.setOnClickListener(ocl -> {
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                this.startActivity(mapIntent);
            });
        }
    }
}