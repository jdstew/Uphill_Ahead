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

import android.content.SharedPreferences;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.preference.PreferenceManager;


/**
 * The GraphGestureDetector class is a custom implementation of the SimpleOnGestureListener for
 * Touch events within the graphics display for a Graph.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class GraphGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private static final String DEBUG_TAG = "name.jdstew.uphillahead.GraphGestureListener";
    /**
     * Default fling gesture for zooming and biasing the graph display
     */
    public static final double DEFAULT_FLING_PERCENT = 0.3333;

    private final GraphView graphView;
    private final SharedPreferences prefs;

    /**
     * Constructor for the implementaion of the SimpleOnGestureListener
     *
     * @param view is the SurfaceView to respond to touch events
     */
    public GraphGestureDetector(GraphView view) {
        graphView = view;
        prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
    }

    @Override
    public boolean onDown(MotionEvent event) {
        // do not delete this method, returning true
        // is required for all other functionality
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {

        int[] viewCoordinates = new int [2];
        graphView.getLocationOnScreen(viewCoordinates);

        // NOTE: only the first 'finger' will be used
        int xRelative = (int)event.getRawX() - viewCoordinates[0];
        int yRelative = (int)event.getRawY() - viewCoordinates[1];
        graphView.touchPoint.set(xRelative, yRelative);

//        Log.d(DEBUG_TAG, "single-tap triggered invalidate()");
        graphView.postInvalidate ();
        return true; // meaning, this listener handled it!
    }

/*
    /**
     * If a long-press is on a Node, then open the Node's detailed Activity display,
     * otherwise treat the event as a single tap

    @Override
    public void onLongPress(MotionEvent event) {

        // NOTE: only the first 'finger' will be used
        graphView.touchPoint.set((int)event.getRawX(), (int)event.getRawY());
        Node n = null; // graphView.getDetailedNode(graphView.touchPoint);
        if (n != null) {
            graphView.touchPoint.set(Integer.MAX_VALUE, Integer.MAX_VALUE);
            // TODO: fix this area...
            Intent i = new Intent(this, NodeDetails.class);
            i.putExtra(Config.NODE_HASHCODE_KEY, n.hashCode();
            graphView.startActivity(i);
        }

//        Log.d(DEBUG_TAG, "long-press triggered invalidate()");
        graphView.postInvalidate ();
    }
 */
/*
    /**
     * For a scrolling gesture:
     * 0. Set the tapPoint to null
     * 1. Calculate the dx/dy of the scroll as a percentage of the view width/height
     * 2. Zoom in/out (finger left/right) on trail relative to existing zoom level
     * 	Example: scrolling 50% of screen to left at 1000 meters zoom should zoom out
     * 	to 2000 meters (currentZoom * (1 + right%)).  Scrolling 66% of the screen to
     * 	the right at 1000 meters should zoom-in to 333 meters
     * 	(currentZoom * (1 - left%)).
     * 3. Bias up/down (finger up/down) is added to the existing bias, not exceeding the
     * 	bias min/max.
     * 	Example: scrolling 50% of screen from 0.0 bias up moves the bias to -0.5.
     * 	Scrolling 60% of the screen from 0.4 bias down moves te bias to 0.9 (limit of bias)
     * 4. set new preference values
     * 5. invalidate


    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {

        SharedPreferences.Editor prefsEditor = prefs.edit();

        // calculate change in horizontal zoom
        double xPercentDelta = (double)((event1.getRawX() - event2.getRawX()) / graphView.getWidth());
        double zoomDist = Double.parseDouble(prefs.getString("zoom_pref_key", Double.toString(Config.ZOOM_DEFAULT)));
        zoomDist = zoomDist + (xPercentDelta)* zoomDist * 0.1;
        Log.d(DEBUG_TAG, "new zoom distance is " + zoomDist);
        zoomDist = Math.max(Config.ZOOM_MIN, Math.min(Config.ZOOM_MAX, zoomDist));

        prefsEditor.putString("zoom_pref_key", Double.toString(zoomDist));

        // calculate change in vertical bias
        double yPercentDelta = (double)((event1.getRawY() - event2.getRawY()) / graphView.getHeight());
        double vertBias = Double.parseDouble(prefs.getString("vertical_bias_pref_key", Double.toString(Config.VERTICAL_BIAS_DEFAULT)));
        vertBias += yPercentDelta;
        vertBias = Math.min(Config.VERTICAL_BIAS_MIN, Math.max(Config.VERTICAL_BIAS_MAX, vertBias));
        prefsEditor.putString("vertical_bias_pref_key", Double.toString(vertBias));

        prefsEditor.apply();

        graphView.touchPoint.set(Integer.MAX_VALUE, Integer.MAX_VALUE); // so that a callout is not displayed
        Log.d(DEBUG_TAG, "scrolled " + xPercentDelta + "% horizontally; " + yPercentDelta + "% vertically");
        //graphView.invalidate();
        return true; // meaning, this listener handled it!
    }
*/
    /**
     * For a fling gesture this method will convert it into a psuedo-scroll action.
     * The resultant change will only be up/down or left/right, but not both
     * 1. Calculate the sign (pos/neg) for the dx/dy of the gesture
     * 2. If Abs(dx) > Abs(dy), then adjust the currentZoom (currentZoom * (1 + DEFAULT_FLING_PERCENT))
     * 	or (currentZoom * (1 - DEFAULT_FLING_PERCENT)) based upon the sign of dx
     * 3. If Abs(dx) < Abs(dy), then adjust the bias by DEFAULT_FLING_PERCENT = 0.3333
     * 	based upon the sign of dy, within the min/max of bias
     * 4. set new preference value
     * 5. invalidate
     */
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

        float dx = event2.getRawX() - event1.getRawX();
        float dy = event2.getRawY() - event1.getRawY();

        SharedPreferences.Editor prefsEditor = prefs.edit();

            double zoomDist = Double.parseDouble(prefs.getString("zoom_pref_key", Double.toString(Config.ZOOM_DEFAULT)));
            if (dx <= 0) {
                zoomDist += zoomDist * DEFAULT_FLING_PERCENT; // left fling zooms out
            } else {
                zoomDist -= zoomDist * DEFAULT_FLING_PERCENT; // right fling zooms in
            }
            zoomDist = Math.max(Config.ZOOM_MIN, Math.min(Config.ZOOM_MAX, zoomDist));
            prefsEditor.putString("zoom_pref_key", Double.toString(zoomDist));

            double vertBias = Double.parseDouble(prefs.getString("vertical_bias_pref_key", Double.toString(Config.VERTICAL_BIAS_DEFAULT)));
            vertBias += dy / graphView.getHeight();// * DEFAULT_FLING_PERCENT; // up fling moves graph up
            vertBias = Math.max(Config.VERTICAL_BIAS_MIN, Math.min(Config.VERTICAL_BIAS_MAX, vertBias));
            prefsEditor.putString("vertical_bias_pref_key", Double.toString(vertBias));

        prefsEditor.apply();

        graphView.touchPoint.set(Integer.MAX_VALUE, Integer.MAX_VALUE); // so that a callout is not displayed
//        Log.d(DEBUG_TAG, "fling triggered invalidate()");
        graphView.postInvalidate ();
        return true; // meaning, this listener handled it!
    }
}
