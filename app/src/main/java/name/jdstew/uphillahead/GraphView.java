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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import 	android.view.GestureDetector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The GraphView class  provides the visual display of the current Graph (aka route or trail).
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class GraphView extends SurfaceView implements SharedPreferences.OnSharedPreferenceChangeListener, OnSuccessListener<Location> {
    /**
     * Logcat identifier
     */
    private static final String DEBUG_TAG = "name.jdstew.uphillahead.GraphView";

    private static final int POPUP_DETAILS_CHAR_THRESHOLD = 64;
    public static final int DEFAULT_NODE_ICON_SIZE = 36;
    public static final int DEFAULT_NODE_ICON_BUFFER = 4;

    /**
     * The dp length of dashes for a dashed line
     */
    public static final int DASHED_DASHES_DP = 10;
    /**
     * The dp width of a dashed line
     */
    public static final int DASHED_LINE_DP = 2;
    /**
     * The dp length and width of a SVG icon
     */
    public static final int DRAWN_ICON_DP = 48;
    public static final int FONT_SP_SIZE = 18;

    private final Observer observer;
    private final Node observerNode;
    private Location observerLocation;
    private boolean isOffGraphAcceptable;
    private final HashMap<NodePoint, Node> iconMap;
    private final SharedPreferences prefs;
    protected Point touchPoint;
    private final Path surfacePath; // A line Path of elevations
    private final Path earthPolygon; // The surfacePath filled in below
    private final Path linePath; // horizontal and vertical site lines
    private final StringBuffer zoomLevelText;
    private final StringBuffer nodeDetailsText;
    private final VectorDrawable drawableGreenHiker;
    private final VectorDrawable drawableYellowHiker;
    private final VectorDrawable drawableGrayHiker;
    private final VectorDrawable drawableWater;
    private final VectorDrawable drawableTent;
    private final VectorDrawable drawableInfo;

    private final GestureDetector gestureDetector;
    AppCompatActivity parentActivity;

    /**
     * Constructor of the custom SurfaceView of the graph.
     *
     * @param parentActivity the parent activity, which is the main activity of the app
     */
    public GraphView(AppCompatActivity parentActivity) {
        super(parentActivity);
        this.parentActivity = parentActivity;


        prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
        prefs.registerOnSharedPreferenceChangeListener(this);

        double storedLatitude = Double.parseDouble(prefs.getString("location_latitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LATITUDE)));
        double storedLongitude = Double.parseDouble(prefs.getString("location_longitude_pref_key", Double.toString(Config.LOCATION_DEFAULT_LONGITUDE)));
        double storedElevation = Double.parseDouble(prefs.getString("location_elevation_pref_key", Double.toString(0.0)));
        observerNode = new Node(storedLatitude, storedLongitude, storedElevation);
        observer = Observer.getInstance(this, (MainActivity)parentActivity);
        isOffGraphAcceptable = false;

        iconMap = new HashMap<>();

        // NOTE: a touchPoint outside the GraphView will not be displayed.
        touchPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

        surfacePath = new Path();
        earthPolygon = new Path();
        linePath = new Path();

        zoomLevelText = new StringBuffer();
        nodeDetailsText = new StringBuffer();

        drawableGreenHiker = (VectorDrawable) getContext().getDrawable(R.drawable.hiker_green_24);
        drawableYellowHiker = (VectorDrawable) getContext().getDrawable(R.drawable.hiker_yellow_24);
        drawableGrayHiker = (VectorDrawable) getContext().getDrawable(R.drawable.hiker_gray_24);
        drawableWater = (VectorDrawable) getContext().getDrawable(R.drawable.water_24);
        drawableTent = (VectorDrawable) getContext().getDrawable(R.drawable.tent_24);
        drawableInfo = (VectorDrawable) getContext().getDrawable(R.drawable.info_24);

        gestureDetector = new GestureDetector(parentActivity, new GraphGestureDetector(this));

        setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }


    @Override
    public void onSuccess(Location location) {
        observerNode.changeLocation(location.getLatitude(), location.getLongitude(), location.getAltitude());
        observerLocation = location;
        isOffGraphAcceptable = false;
//        Log.d(DEBUG_TAG, "Location update triggered invalidate()");
        invalidate();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefs_key) {
//        Log.d(DEBUG_TAG, "preferences change listener called");

        if (prefs_key.compareTo("route_pref_key") == 0) {
            isOffGraphAcceptable = false; // reset if route changed
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Log.i(DEBUG_TAG, "GraphView.onDraw() started");
//        Log.i(DEBUG_TAG, "Canvas is (" + canvas.getWidth() + " by " + canvas.getHeight() + ")");

        iconMap.clear();

        surfacePath.rewind();
        earthPolygon.rewind();
        linePath.rewind();

        zoomLevelText.delete(0, zoomLevelText.length());
        nodeDetailsText.delete(0, nodeDetailsText.length());

        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
//        Log.i("name.jdstew.uphillahead.GraphView", "Is night mode? " + isNightMode);


        String directionPref = prefs.getString("direction_pref_key", Config.DIRECTION_TO_DEFAULT);
        boolean isDirectionToEnd = directionPref.compareTo(Config.DIRECTION_TO_END) == 0;
//        Log.i("name.jdstew.uphillahead.GraphView", "Is direction to end? " + isDirectionToEnd);
        String system = prefs.getString("system_pref_key", Config.SYSTEM_DEFAULT);
//        Log.i("name.jdstew.uphillahead.GraphView", "Measurement system is " + system);
        double zoomDist = Double.parseDouble(prefs.getString("zoom_pref_key", Double.toString(Config.ZOOM_DEFAULT)));
//        Log.i("name.jdstew.uphillahead.GraphView", "Zoom distance is " + String.format("%2.1f", zoomDist));
        double vertBias = Double.parseDouble(prefs.getString("vertical_bias_pref_key", Double.toString(Config.VERTICAL_BIAS_DEFAULT)));
//        Log.i("name.jdstew.uphillahead.GraphView", "Vertical bias is " + String.format("%2.1f", vertBias));
        double vertExag = prefs.getInt("exaggeration_pref_key", (int) Config.EXAGGERATION_DEFAULT);
//        Log.i("name.jdstew.uphillahead.GraphView", "Vertical exaggeration is " + vertExag);
        double paceBias = prefs.getInt("pace_pref_key", (int) Config.PACE_BIAS_DEFAULT) * Config.PACE_PREFS_MULTIPLIER;
//        Log.i("name.jdstew.uphillahead.GraphView", "Pace bias is " + paceBias);

        // compute scale of graph to display View
//        Log.i("name.jdstew.uphillahead.GraphView", "Canvas width is " + getWidth());
        double horiScale = (double)getWidth() / zoomDist; // equals number of pixels per meter
//        Log.i("name.jdstew.uphillahead.GraphView", "horizontal scale is " + horiScale);
        double vertScale = horiScale * vertExag; // equals number of pixel per meter by exaggeration

        // initialize starting position of Paths on screen
        double currentX = 0.0;
        double currentY = (double)getHeight() / 2.0 * (1.0 + vertBias);
        double startingY = currentY;
        double previousX;

        double touchPointDist = 0.0;
        double touchPointTime = 0.0;
        double touchPointGain = 0.0;
        double touchPointLoss = 0.0;

        double cumDist = 0.0;
        double cumTime = 0.0;
        double cumGain = 0.0;
        double cumLoss = 0.0;
        double pace = 0.0;

        surfacePath.moveTo((float) currentX, (float) currentY);
        earthPolygon.moveTo((float) currentX, (float) currentY);

        Graph graph = GraphManager.getInstance(getContext()).getGraph(prefs.getString("route_pref_key", String.valueOf(R.string.txt_route_title)));
        if (graph == null) {
//            Log.i("name.jdstew.uphillahead.GraphView", "Cannot render graph - Graph object is null.");
            return;
        }

        // todo: get snap-to-distance
        int snapToTrail = Config.getSnapToTrailValue(prefs.getString("snap_to_trail_pref_key", Config.SNAP_TO_TRAIL_DEFAULT));
//        Log.i("name.jdstew.uphillahead.GraphView", "Snap-to-trail is " + snapToTrail);

        double distToTrail = graph.setEntryEdge(observerNode, isDirectionToEnd);
//        Log.i(DEBUG_TAG, "observerNode is " + observerNode);
        Log.i(DEBUG_TAG, "observerNode distance to trail is " + Calcs.getDisplayedDist(distToTrail, system));
        // is the current location off trail?
        if (distToTrail > (double)snapToTrail) {
            // has the user accepted the location as being off trial?
            if (isOffGraphAcceptable) {
                // todo: display distance to trail and time of last update
                return; // meaning do not graph
            } else { // ask the user if they'd like to simulate location at the closest Graph Node?
                StringBuilder sb = new StringBuilder();
                sb.append("Currently ");
                sb.append(Calcs.getDisplayedDist(distToTrail, system));
                sb.append(" from trail. Simulate closet point on trail?");

                // Use the Builder class for convenient dialog construction
                // Note: AlertDialog is non-blocking by design
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(sb.toString())
                        .setPositiveButton(R.string.opt_simulate, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Node closestNode = null;
                                // note: only one edge of the observer node is null
                                if (observerNode.getNextEdge() != null) {
                                    closestNode =  observerNode.getNextEdge().getNextNode();
                                } else if (observerNode.getPrevEdge() != null)  {
                                    closestNode =  observerNode.getPrevEdge().getPrevNode();
                                } else {
//                                    Log.e(DEBUG_TAG, "closestNode has no non-null edge!");
                                }
//                                Log.i(DEBUG_TAG, "setting simulated node to " + closestNode);
                                observer.setSimulatedLocation(closestNode.getLatitude(), closestNode.getLongitude(), closestNode.getElevation());
                            }
                        })
                        .setNegativeButton(R.string.opt_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                isOffGraphAcceptable = true;
                                Toast.makeText(getContext(), "Location off-trail - nothing to display", Toast.LENGTH_LONG).show();
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();

                return; // meaning, do not graph until the dialog is answered
            }
        }
//        Log.i(DEBUG_TAG, "cursorNode at: " + cursorNode.toString());

        Node cursorNode = observerNode;
        while (cursorNode != null && (int)currentX < getWidth()) {
//          Log.i(DEBUG_TAG, "Cursor at (" + currentX + ", " + currentY + ")");
            previousX = currentX;

            Edge edge;
            if (isDirectionToEnd) { // direction forward
                edge = cursorNode.getNextEdge();
                if (edge == null) {
                    break; // however, this shouldn't happen
                }

                if (edge.getVerticalDistance() > 0.0) {
                    cumGain += edge.getVerticalDistance();
                } else {
                    cumLoss += edge.getVerticalDistance();
                }
            } else {
                edge = cursorNode.getPrevEdge();
                if (edge == null) {
                    break; // however, this shouldn't happen
                }

                if (edge.getVerticalDistance() > 0.0) {
                    cumLoss -= edge.getVerticalDistance();
                } else {
                    cumGain -= edge.getVerticalDistance();
                }
            }
//            Log.i("name.jdstew.uphillahead.GraphView", "cumGain: " + cumGain + ", cumLoss: " + cumLoss);

            // plot by distance change formula
            currentX += edge.getHorizontalDistance() * horiScale;
            // plot by distance change formula
//              currentY = startingY + ((startingElev - cursorNode.getElevation()) * vertScale);
//              Log.i("name.jdstew.uphillahead.GraphView", "node elevation is " + cursorNode.getElevation());

            // plot by elevation change formula
            if (isDirectionToEnd) {
                currentY -= edge.getVerticalDistance() * vertScale;
            } else {
                currentY += edge.getVerticalDistance() * vertScale;
            }
//              Log.i("name.jdstew.uphillahead.GraphView", "currently at (" + (int)currentX + ", " + (int)currentY + ")");
            surfacePath.lineTo((float) currentX, (float) currentY);

            // calculate pace based upon this edge, for use in partial and cumulative calculations
            pace = Calcs.getPace(edge.getSlope(), cursorNode.getElevation());

            // Is the touch point along this Edge?  If so, calculate the partial distance and time
            if (touchPoint.x >= (int)previousX && touchPoint.x < (int)currentX) {
                double partialEdgePercent = (touchPoint.x - previousX) / (currentX - previousX);
                touchPointDist = cumDist + partialEdgePercent * edge.getDistance();
                touchPointTime = cumTime + (partialEdgePercent * edge.getDistance() / 1_000.0) / (pace * paceBias); // t = d/s

                if (isDirectionToEnd) { // direction forward
                    if (edge.getVerticalDistance() >= 0.0) {
                        touchPointGain = cumGain + (edge.getVerticalDistance() * partialEdgePercent);
                        touchPointLoss = cumLoss;
                    } else {
                        touchPointGain = cumGain;
                        touchPointLoss = cumLoss + (edge.getVerticalDistance() * partialEdgePercent);
                    }
                } else {
                    if (edge.getVerticalDistance() > 0.0) {
                        touchPointGain = cumGain;
                        touchPointLoss = cumLoss - (edge.getVerticalDistance() * partialEdgePercent);
                    } else {
                        touchPointGain = cumGain - (edge.getVerticalDistance() * partialEdgePercent);
                        touchPointLoss = cumLoss;
                    }
                }
            }

            // calculate pace and cumulative time
            cumDist += edge.getDistance(); // meters
            cumTime += (edge.getDistance() / 1_000.0) / (pace * paceBias); // time = speed / distance, in Km/hr

            // Z-order-10. Horizontal Green-Yellow-Red rectangles [LIGHT ONLY]
            if (!isNightMode) {
                earthPolygon.lineTo((float) currentX, (float) currentY);

                if (pace > Config.DIFFICULTY_EASY_MIN) { // easy
                    canvas.drawRect((float) previousX, 0.0f, (float) currentX, getHeight(), GraphView.getEazyPaint(this.getContext()));
                } else if (pace < Config.DIFFICULTY_HARD_MAX) {  // hard
                    canvas.drawRect((float) previousX, 0.0f, (float) currentX, getHeight(), GraphView.getHardPaint(this.getContext()));
                } else { // medium
                    canvas.drawRect((float) previousX, 0.0f, (float) currentX, getHeight(), GraphView.getMediumPaint(this.getContext()));
                }
            }

            // Is the node a source of water, campsite, or information?
            if (cursorNode.getDescription() != null) {
                NodePoint np;
                if (cursorNode.getName().contains("WA") || cursorNode.getName().contains("WR")) {
                    np = new NodePoint(drawableWater, currentX, currentY, cumDist, cumTime, cumGain, cumLoss);
                    iconMap.put(np, cursorNode);
                } else if (cursorNode.getName().contains("CS")) {
                    np = new NodePoint(drawableTent, currentX, currentY, cumDist, cumTime, cumGain, cumLoss);
                    iconMap.put(np, cursorNode);
                } else if (cursorNode.getName() != null) {
                    np = new NodePoint(drawableInfo, currentX, currentY, cumDist, cumTime, cumGain, cumLoss);
                    iconMap.put(np, cursorNode);
                }
            }

            if (isDirectionToEnd) { // direction forward
                cursorNode = cursorNode.getNextEdge().getNextNode();
            } else {
                cursorNode = cursorNode.getPrevEdge().getPrevNode();
            }
        }

        // Z-order-20. Brown "earth" filled polygon [Path earthPolygon; LIGHT ONLY]
        if (!isNightMode) {
            // go to bottom right
            earthPolygon.lineTo((float)currentX, (float)getHeight());
            // go to bottom left
            earthPolygon.lineTo(0.0f, (float)getHeight());
            // return to start, to close Path
            earthPolygon.lineTo(0.0f, (float)startingY);

            canvas.drawPath(earthPolygon, GraphView.getEarthPaint(this.getContext()));
        }
        earthPolygon.rewind();

        // Z-order-30. Black/gray "surface" path# [Path surfacePath;]
        canvas.drawPath(surfacePath, GraphView.getSurfacePaint(isNightMode, this.getContext()));
        surfacePath.rewind();

        // Z-order-40. Gray horizontal ‘level’ line
        canvas.drawLine(0.0f, (float)startingY, (float)getWidth(), (float)startingY, GraphView.getDashedLinePaint(getContext()));

        // Z-order-50. Observer (person) icon
        int iconPixels = Calcs.dpToPx(DRAWN_ICON_DP, getContext());
        // was location obtain recently?
        if (observerLocation == null || observerLocation.getProvider().compareTo(Config.SOURCE_SIMULATED) == 0) {
            drawableGrayHiker.setBounds(0, ((int)startingY - (iconPixels / 2)), iconPixels, ((int)startingY - (iconPixels / 2)) + iconPixels);
            drawableGrayHiker.draw(canvas);
        } else if (observerLocation.getTime() >= System.currentTimeMillis() - Config.LOCATION_RECENT) {
            drawableGreenHiker.setBounds(0, ((int)startingY - (iconPixels / 2)), iconPixels, ((int)startingY - (iconPixels / 2)) + iconPixels);
            drawableGreenHiker.draw(canvas);
        } else {
            drawableYellowHiker.setBounds(0, ((int)startingY - (iconPixels / 2)), iconPixels, ((int)startingY - (iconPixels / 2)) + iconPixels);
            drawableYellowHiker.draw(canvas);

            DateFormat timeFormatter = new SimpleDateFormat("H:mm");
            Date locationTime = new Date(observerLocation.getTime());
            String displayTime = timeFormatter.format(locationTime);

            TextPaint locationTimeTextPaint = new TextPaint();
            locationTimeTextPaint.setAntiAlias(true);
            locationTimeTextPaint.setTextSize(Calcs.spToPx(FONT_SP_SIZE, getContext()));
            if (!isNightMode) {
                locationTimeTextPaint.setColor(getResources().getColor(R.color.black, null));
            } else {
                locationTimeTextPaint.setColor(getResources().getColor(R.color.gray_light, null));
            }
            float locationTimeX = 0;
            float locationTimeY = (int)startingY + (2 * Calcs.spToPx(FONT_SP_SIZE, getContext()));

            canvas.drawText(displayTime, locationTimeX, locationTimeY, locationTimeTextPaint);
        }

        // Z-order-60. Visible distance text [lower right]
        // get: system preference
        zoomLevelText.append(Calcs.getDisplayedDist(zoomDist, system));
        TextPaint zoomTextPaint = new TextPaint();
        zoomTextPaint.setAntiAlias(true);
        zoomTextPaint.setTextSize(Calcs.spToPx(FONT_SP_SIZE, getContext()));
        if (!isNightMode) {
            zoomTextPaint.setColor(getResources().getColor(R.color.black, null));
        } else {
            zoomTextPaint.setColor(getResources().getColor(R.color.gray_light, null));
        }
        float zoomX = getWidth() - zoomTextPaint.measureText(zoomLevelText.toString()) - Calcs.spToPx(FONT_SP_SIZE, getContext());
        float zoomY = getHeight() - 2.0f * Calcs.spToPx(FONT_SP_SIZE, getContext());// (float)startingY + (-zoomTextPaint.ascent() + zoomTextPaint.descent()) + (float)(getHeight() * 0.10); //canvas.getHeight() - (-zoomTextPaint.ascent() + zoomTextPaint.descent());
        canvas.drawText(zoomLevelText.toString(), zoomX, zoomY, zoomTextPaint);

        // Z-order-70. Next WA and CS text box [lower left]
        // TODO: consider adding this at some later time

        // Z-order-80. Water, camp, and info icons#
        // water icons are drawn below the Node
        iconMap.forEach((np, n) -> {
            float x, y;
            if (np.getIcon().equals(drawableWater)) {
                x = (float) (np.x - (double) (iconPixels / 2.0));
                y = (float) np.y;
            } else {
                x = (float) (np.x - (double) (iconPixels / 2.0));
                y = (float) (np.y - iconPixels);
            }
            np.getIcon().setBounds((int)x, (int)y, (int)(x + iconPixels), (int)(y + iconPixels));
            np.getIcon().draw(canvas);
        });

        // Z-order-90. Gray vertical line (finger placement)
        if (touchPoint.x <= getWidth()) {
            canvas.drawLine((float)touchPoint.x, 0.0f,
                    (float)touchPoint.x, (float)getHeight(),
                    GraphView.getDashedLinePaint(getContext()));
        }

        // Z-order-100. Pop-up distance and ETA only
        NodePoint np = getNodePoint(touchPoint);
        if (np != null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View nodeDetailView = layoutInflater.inflate(R.layout.activity_node_details, null); // null because no applicable ViewGroup

            Node n = iconMap.get(np);

            TextView detailsTextView = nodeDetailView.findViewById(R.id.node_details_text);
            nodeDetailsText.append(n.getName());
            nodeDetailsText.append(System.lineSeparator());
            nodeDetailsText.append(Calcs.getDisplayedDist(np.getDistance(), system));
            nodeDetailsText.append(", ");
            nodeDetailsText.append(Calcs.getDisplayedTime(np.getTime(), system));
            nodeDetailsText.append(System.lineSeparator());
            nodeDetailsText.append("+");
            nodeDetailsText.append(Calcs.getDisplayedElev(np.getGain(), system));
            nodeDetailsText.append(" / ");
            nodeDetailsText.append(Calcs.getDisplayedElev(np.getLoss(), system));
            nodeDetailsText.append(System.lineSeparator());
            nodeDetailsText.append(n.getDescription());
            detailsTextView.setText(nodeDetailsText.toString());
            if (!detailsTextView.isInLayout()) {
                detailsTextView.requestLayout();  // else call forceLayout()
            } else {
//                Log.w(DEBUG_TAG , "unable to request layout of node details popup - in current layout pass" );
            }

            StringBuilder uriString = new StringBuilder();
            uriString.append("geo:");
            uriString.append(n.getLatitude());
            uriString.append(',');
            uriString.append(n.getLongitude());
            uriString.append("?q=");
            uriString.append(n.getLatitude());
            uriString.append(',');
            uriString.append(n.getLongitude());
            uriString.append('(');
            uriString.append(n.getName());
            uriString.append(')');

            if (nodeDetailsText.length() < POPUP_DETAILS_CHAR_THRESHOLD) {

                int w = LinearLayout.LayoutParams.WRAP_CONTENT;
                int h = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                PopupWindow popupWindow = new PopupWindow(nodeDetailView, w, h, focusable);

                int popupX;
                int popupY;

                if (touchPoint.x + w < getWidth()) {
                    popupX = touchPoint.x; // right of touch point
                } else if (touchPoint.x - w > 0) {
                    popupX = touchPoint.x - w; // left of touch point
                } else {
                    popupX = (getWidth() - w) / 2; // horizontal middle
                }

                if (touchPoint.y + h < getHeight()) {
                    popupY = touchPoint.y; // below touch point
                } else if (touchPoint.y - h > 0) {
                    popupY = touchPoint.y - h; // above touch point
                } else {
                    popupY = (getHeight() - h) / 2 - (int) (getHeight() * 0.10); // vertical center
                }

                Button btnSimulate = nodeDetailView.findViewById(R.id.simulate_button);
                btnSimulate.setOnClickListener(ocl -> {
                    touchPoint.set(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    observer.setSimulatedLocation(n.getLatitude(), n.getLongitude(), n.getElevation());
                    popupWindow.dismiss();
                    invalidate();
                });
                // template:   geo:<lat>,<long>?q=<lat>,<long>(Label+Name)
                Button btnGoogleMaps = nodeDetailView.findViewById(R.id.google_map_button);
                btnGoogleMaps.setOnClickListener(ocl -> {
                    touchPoint.set(Integer.MAX_VALUE, Integer.MAX_VALUE);

                    Uri gmmIntentUri = Uri.parse(uriString.toString());
                  Log.e("name.jdstew.uphillahead.GraphView", "gmmIntentUri: " + gmmIntentUri );
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    popupWindow.dismiss();
                    parentActivity.startActivity(mapIntent);
                });
//            Log.i("name.jdstew.uphillahead.GraphView", "Popup window at (" + w + ", " + h + ")" );

                popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, popupX, popupY);
            } else {
                Intent i = new Intent(parentActivity, NodeDetailsActivity.class);
                i.putExtra("description", nodeDetailsText.toString());
                i.putExtra("latitude", n.getLatitude());
                i.putExtra("longitude", n.getLongitude());
                i.putExtra("uriString", uriString.toString());
                parentActivity.startActivity(i);
            }
            touchPoint.x = Integer.MAX_VALUE;
            touchPoint.y = Integer.MAX_VALUE;
        } else {

            if (touchPoint.x <= getWidth() && touchPoint.y <= getHeight()) {

                TextPaint tapTextPaint = new TextPaint();
                tapTextPaint.setAntiAlias(true);
                tapTextPaint.setTextSize(Calcs.spToPx(FONT_SP_SIZE, getContext()));
                if (!isNightMode) {
                    tapTextPaint.setColor(getResources().getColor(R.color.black, null));
                } else {
                    tapTextPaint.setColor(getResources().getColor(R.color.gray_light, null));
                }

                nodeDetailsText.delete(0, nodeDetailsText.length());
                nodeDetailsText.append(Calcs.getDisplayedDist(touchPointDist, system));
                nodeDetailsText.append(", ");
                nodeDetailsText.append(Calcs.getDisplayedTime(touchPointTime, system));

                float buffer = Calcs.dpToPx(4.0f, getContext());
                float x;
                if (touchPoint.x > getWidth() / 2) {
                    x = touchPoint.x - tapTextPaint.measureText(nodeDetailsText.toString()) - buffer;
                } else {
                    x = touchPoint.x + buffer;
                }
                float y = touchPoint.y + tapTextPaint.ascent() - tapTextPaint.descent() - (float)(getHeight() * 0.10);
                canvas.drawText(nodeDetailsText.toString(), x, y, tapTextPaint);

                nodeDetailsText.delete(0, nodeDetailsText.length());
                nodeDetailsText.append("+");
                nodeDetailsText.append(Calcs.getDisplayedElev(touchPointGain, system));
                nodeDetailsText.append(" / ");
                nodeDetailsText.append(Calcs.getDisplayedElev(touchPointLoss, system));

                if (touchPoint.x > getWidth() / 2) {
                    x = touchPoint.x - tapTextPaint.measureText(nodeDetailsText.toString()) - buffer;
                } else {
                    x = touchPoint.x + buffer;
                }
                y -= tapTextPaint.ascent() - tapTextPaint.descent();

                canvas.drawText(nodeDetailsText.toString(), x, y, tapTextPaint);
            }
        }

        if ((int)currentX < getWidth()) {
            if (isDirectionToEnd) { // direction forward
                Toast.makeText(getContext(), "At or near end of trail.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "At or near start of trail.", Toast.LENGTH_LONG).show();
            }
        }
//        Log.d(DEBUG_TAG, "GraphView.onDraw() finished (" + iterations + " iterations)");
    }

    void onStopPsuedo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("location_latitude_pref_key", Double.toString(observerNode.getLatitude()));
        editor.putString("location_longitude_pref_key", Double.toString(observerNode.getLongitude()));
        editor.putString("location_elevation_pref_key", Double.toString(observerNode.getElevation()));
        editor.apply();
    }

    public NodePoint getNodePoint(Point touchPt) {
//        Log.i("name.jdstew.uphillahead.GraphView", "Touch point at (" + p.x + ", " + p.y + ")");
        AtomicReference<NodePoint> returnedNode = new AtomicReference<>();

        int icon_size = Calcs.dpToPx(DEFAULT_NODE_ICON_SIZE, this.getContext());
        int icon_buffer = Calcs.dpToPx(DEFAULT_NODE_ICON_BUFFER, this.getContext());

        // perform basic square search for a Node with detailed information
        // campsites and info display above the node point
        // water is assumed display below the node point
        int iconSquare = icon_size + icon_buffer;
        if (iconMap != null) {
            iconMap.forEach((NodePoint nP, Node nV) -> {
                int minX = nP.x - iconSquare / 2;
                int maxX = nP.x + iconSquare / 2;
                int minY, maxY;
                if (nP.getIcon().equals(drawableWater)) {
                    minY = nP.y;
                    maxY = nP.y + iconSquare;
                } else {
                    minY = nP.y - iconSquare;
                    maxY = nP.y;
                }

                if (touchPt.x <= maxX && touchPt.x >= minX && touchPt.y <= maxY && touchPt.y >= minY) {
//                  Log.i("name.jdstew.uphillahead.GraphView", "Touch point found node");
                    returnedNode.set(nP);
                }
            });
        }

        return returnedNode.get();
    }

    public static Paint getViewBackgroundPaint(boolean isNight, Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        if (isNight) {
            p.setColor(context.getResources().getColor(R.color.gray_dark, null));
        } else {
            p.setColor(context.getResources().getColor(R.color.gray_light, null));
        }
        p.setStrokeWidth(Calcs.dpToPx(Config.SURFACE_STROKE_WIDTH, context));
        return p;
    }

    /**
     * Get the Paint object for the "Earth" below the trail line
     *
     * @return the Paint object for the "Earth" below the trail line
     */
    public static Paint getEarthPaint(Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(context.getResources().getColor(R.color.khaki, null));
        return p;
    }

    /**
     * Get the Paint object for the hiking trail
     *
     * @param context from the parent's Activity
     * @return the Paint object for the hiking trail
     */
    public static Paint getSurfacePaint(boolean isNight, Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        if (isNight) {
            p.setColor(context.getResources().getColor(R.color.white, null));
        } else {
            p.setColor(context.getResources().getColor(R.color.black, null));
        }
        p.setStrokeWidth(Calcs.dpToPx(Config.SURFACE_STROKE_WIDTH, context));
        return p;
    }

    /**
     * Get the Paint object for vertical and horizontal indicator lines
     *
     * @param context from the parent's Activity
     * @return the Paint object for vertical and horizontal indicator lines
     */
    public static Paint getDashedLinePaint(Context context) {
        // equal length on and off dashes, starting with a dash
        float dashLength = Calcs.dpToPx(DASHED_DASHES_DP, context);
        float[] dashEffect = {dashLength, dashLength};
        DashPathEffect dpe = new DashPathEffect(dashEffect, 0.0f);

        Paint p = new Paint();
        p.setPathEffect(dpe);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(context.getResources().getColor(R.color.gray_dark, null));
        p.setStrokeWidth(Calcs.dpToPx(DASHED_LINE_DP, context));
        return p;
    }

    /**
     * Get the Paint object for easy-difficultly slope
     *
     * @return the Paint object for easy-difficultly slope
     */
    public static Paint getEazyPaint(Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(context.getResources().getColor(R.color.color_easy, null));
        return p;
    }

    /**
     * Get the Paint object for medium-difficultly slope
     *
     * @return the Paint object for medium-difficultly slope
     */
    public static Paint getMediumPaint(Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(context.getResources().getColor(R.color.color_medium, null));
        return p;
    }

    /**
     * Get the Paint object for hard-difficultly slope
     *
     * @return the Paint object for hard-difficultly slope
     */
    public static Paint getHardPaint(Context context) {
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(context.getResources().getColor(R.color.color_hard, null));
        return p;
    }

    /**
     * Converts a SVG icon into a bitmap image
     *
     * @param drawableId from the resources images
     * @param context    from the parent's Activity
     * @return a bitmap of the SVG icon
     */
    public static Bitmap getSvgToBmp(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable != null) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else {
//          Log.w(DEBUG_TAG, "Unable to convert SVG icon to Bitmap");
            return null;
        }
    }
}
