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
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The RouteListAdapter class is a RecyclerView Adapter for binding List of Graph (Route)
 * names to button items.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public RadioButton routeRadioButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            routeRadioButton = itemView.findViewById(R.id.route_button);
        }
    }

    private final List<String> graphs;
    OnClickListenerString itemClickListener;
    int selectedPosition = -1;

    public RouteListAdapter(List<String> graphs, OnClickListenerString itemClickListener) {
        this.graphs = graphs;
        this.itemClickListener = itemClickListener;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public RouteListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View routeListActivity = inflater.inflate(R.layout.activity_route_list_item, parent, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String graphName = prefs.getString("route_pref_key", context.getString(R.string.txt_route_title));
        selectedPosition = graphs.lastIndexOf(graphName);

        return new ViewHolder(routeListActivity);
    }

    @Override
    public void onBindViewHolder(RouteListAdapter.ViewHolder holder, int position) {
        String graphName = graphs.get(position);
        holder.routeRadioButton.setText(graphName);
        holder.routeRadioButton.setChecked(position == selectedPosition);

        holder.routeRadioButton.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    // check condition
                    if (isChecked) {
                        // When checked update selected position
                        selectedPosition = holder.getAdapterPosition();
                        // Call listener
                        itemClickListener.onClick(holder.routeRadioButton.getText().toString());
                    }
                }
        );
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return graphs.size();
    }
}
