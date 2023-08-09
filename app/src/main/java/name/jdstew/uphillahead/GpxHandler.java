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

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The GpxHandler class provides and XML stream reader handler to process GPX-XML elements.
 *
 * GPX file logic:
 *
 * wpt - waypoints
 *
 * rte - routes - an ordered list of waypoints
 *
 * ____rtept - route points
 *
 * ________wpt - waypoints
 *
 * trk - tracks - an ordered list of points
 *
 * ____trkseg - track segments, points that are logically connected in order
 *
 * ________wpt - waypoints
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class GpxHandler extends DefaultHandler {
    /**
     * Logcat identifier
     */
    private static final String DEBUG_TAG = "name.jdstew.uphillahead.GpxHandler";
    /**
     * GPX trigger XML element for creating a new graph.
     */
    public static final String TRACK = "trk";

    /**
     * GPX trigger XML element for creating a new graph.
     */
    public static final String ROUTE = "rte";

    /**
     * Element for GPX name
     */
    public static final String NAME = "name";

    /**
     * GPX element to trigger new node object
     */
    public static final String TRACK_POINT = "trkpt"; // creates a new Node

    /**
     * GPX element to trigger new node object
     */
    public static final String ROUTE_POINT = "rtept"; // creates a new Node

    /**
     * GPX element to trigger new node, potentially to be inserted into existing Graph
     */
    public static final String WAYPOINT = "wpt"; // creates a new Node

    /**
     * GPX element for elevation
     */
    public static final String ELEV = "ele";

    /**
     * GPX element for track or route description
     */
    public static final String DESC = "desc";

    /**
     * Custom element to describe the destination of the track or route
     */
    public static final String DESC_TO = "upahead:toDesc";

    /**
     * Custom element to describe the start of the track or route
     */
    public static final String DESC_FROM = "upahead:fromDesc";

    /**
     * GPX element for symbol of the waypoint
     */
    public static final String SYM = "sym";

    private String currentGraphType = null;
    private String currentElement = null;

    private double lat = 0.0;
    private double lon = 0.0;
    private double ele = 0.0;
    private String name;
    private String desc;
    private String sym;

    Graph graph;

    @Override
    public void startDocument() {
//		Log.i(DEBUG_TAG, "document start");
    }

    @Override
    public void endDocument() {
        Log.i(DEBUG_TAG, "document end");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
//        Log.i(DEBUG_TAG, "element " + qName + " start");

        switch (qName) {
            case TRACK: // flows through to next
                currentGraphType = TRACK;
            case ROUTE:
                if (currentGraphType == null) {
                    currentGraphType = ROUTE;
                }
                graph = new Graph();
                break;
            case NAME:
                currentElement = NAME;
                break;
            case DESC:
                currentElement = DESC;
                break;
            case DESC_TO:
                currentElement = DESC_TO;
                break;
            case DESC_FROM:
                currentElement = DESC_FROM;
                break;
            case SYM:
                currentElement = SYM;
                break;
            case TRACK_POINT: // flows through to next
            case ROUTE_POINT: // flows through to next
            case WAYPOINT:
                lat = Double.parseDouble(attributes.getValue(0));
                lon = Double.parseDouble(attributes.getValue(1));
                name = null;
                desc = null;
                sym = null;
                break;

            case ELEV:
                currentElement = ELEV;
                break;
            default:
                // ignore elements: gpx, trkseg, metadata, desc, bounds, extensions, gpxx
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) {
//        Log.i(DEBUG_TAG, "element " + qName + " end");

        switch (qName) {
            case TRACK: // flows through to next
            case ROUTE:
                graph.bookendGraph(); // adds text to the start and end nodes
                GraphManager.getInstance().addGraph(graph);
//                Log.i(DEBUG_TAG, "Loaded graph " + graph.getName());
                break;
            case TRACK_POINT:
            case ROUTE_POINT:
            case WAYPOINT: {
                Node n = new Node(lat, lon, ele);
                n.setName(name);
                n.setDescription(desc);
                n.setSymbol(sym);
                // compute open location code
                // insert node into graph

                if (currentGraphType != null) {
                    graph.appendNode(n);
                } else {
                    // try to insert into existing graphs
                    if (n.getDescription() != null && !n.getDescription().equals(Config.WAYPOINT_SKIP_1)) {
                        GraphManager.getInstance().insertNode(n);
                    } else {
//                        Log.i(DEBUG_TAG, "halfmile marker skipped");
                    }
                }
            }
            break;
            case ELEV:
            case NAME:
            case DESC:
            case DESC_TO:
            case DESC_FROM:
            case SYM:
                currentElement = null;
                break;
            default:
                // ignore elements: gpx, trkseg, metadata, desc, bounds, extensions, gpxx
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (currentElement == null)
            return;

        switch (currentElement) {
            case ELEV:
                ele = Double.parseDouble(String.valueOf(ch, start, length));
                currentElement = null;
                break;
            case NAME:
                name = String.valueOf(ch, start, length);
                if (graph != null && graph.getName() == null) {
                    graph.setName(name);
                }
                currentElement = null;
                break;
            case DESC:
                desc = String.valueOf(ch, start, length);
                currentElement = null;
                break;
            case DESC_TO:
                graph.setStartDescription(String.valueOf(ch, start, length));
                currentElement = null;
                break;
            case DESC_FROM:
                graph.setEndDescription(String.valueOf(ch, start, length));
                currentElement = null;
                break;
            case SYM:
                sym = String.valueOf(ch, start, length);
                currentElement = null;
                break;
            default:
                // ignore elements: gpx, metadata
        }
    }
}