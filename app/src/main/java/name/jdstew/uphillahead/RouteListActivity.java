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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The RouteListActivity class provides a container View for the RecyclerView of Graphs (Routes)
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class RouteListActivity extends Activity {

    private static final String DEBUG_TAG = "name.jdstew.uphillahead.RouteListActivity";

    private static final String APP_STATE_KEY = "appkey";

    // TODO: where is APP_STATE_KEY stored?
    // Some transient state for the activity instance.
    String appState;
    RouteListAdapter routeListAdapter;
    RecyclerView recyclerViewWidget;
    OnClickListenerString itemClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Call the superclass onCreate to complete the creation of
        // the activity, like the view hierarchy.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        List<String> graphs = GraphManager.getInstance(getApplicationContext()).getGraphNameList();

        // Recover the instance state.
        if (savedInstanceState != null) {
            appState = savedInstanceState.getString(APP_STATE_KEY);
        }

        // Initialize member view
        recyclerViewWidget = findViewById(R.id.route_recycler_view);
        recyclerViewWidget.setHasFixedSize(true);

        // add horizontal divider between each list item
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerViewWidget.addItemDecoration(itemDecoration);

        itemClickListener = s -> {

            // Notify adapter
            recyclerViewWidget.post(() -> routeListAdapter.notifyDataSetChanged());
// TODO: the following Toast was removed due to it firing waaaay too many times - why?
//                Toast.makeText(getApplicationContext(), "Selected : " + s, Toast.LENGTH_SHORT).show();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Config.ROUTE_KEY, s);
            editor.apply();

            this.finish();
        };

        // attach adapter to recycler
        routeListAdapter = new RouteListAdapter(graphs, itemClickListener);
        recyclerViewWidget.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWidget.setAdapter(routeListAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String graphName = prefs.getString("route_pref_key", getString(R.string.txt_route_title));
        int selectedPosition = graphs.lastIndexOf(graphName);
        recyclerViewWidget.scrollToPosition(selectedPosition);
    }
}