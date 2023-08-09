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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * The PctGpxLoader class is used within Android Studio as a test class to provide initial
 * processing of GPX data files when creating Graph objects. *
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class PctGpxLoader {

    private static final String DEBUG_TAG = "name.jdstew.uphillahead.PctGpxLoader";
    public static final String ROUTE_DIRECTORY = "C:\\tmp\\PCT\\tracks\\";
    public static final String WAYPOINT_DIRECTORY = "C:\\tmp\\PCT\\waypoints\\";
    public static final String GRAPH_DIRECTORY = "C:\\tmp\\PCT\\graphs\\";

    public static void loadPctGpxFiles() {

        File routesFileObj = new File(ROUTE_DIRECTORY);
        String[] routesFileNames = routesFileObj.list();
//        Log.i(DEBUG_TAG, "Files to load: " + Arrays.toString(routesFileNames));

        for (String f : routesFileNames) {
            if (f.endsWith(".gpx")) {
                try (FileInputStream fileIOStreamTrack = new FileInputStream(ROUTE_DIRECTORY + f)) {

                    PctGpxLoader.parseXml(fileIOStreamTrack);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File waypointFileObj = new File(WAYPOINT_DIRECTORY);
        String[] waypointFileNames = waypointFileObj.list();
        for (String f : waypointFileNames) {
            if (f.endsWith(".gpx")) {
                try (FileInputStream fileIOStreamWaypoint = new FileInputStream(WAYPOINT_DIRECTORY + f)) {

                    PctGpxLoader.parseXml(fileIOStreamWaypoint);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void parseXml(InputStream gpxFile) {
        GpxHandler handler = new GpxHandler();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
            saxParser.parse(gpxFile, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void processTestTrack() {
        File routesFileObj = new File("C:\\tmp\\pct\\tracks\\OR_Sec_G_tracks.gpx"); // C:\\tmp\\PCT\\test_tracks.gpx  C:\\tmp\\pct\\tracks\\OR_Sec_G_tracks.gpx

        try (FileInputStream fileIOStreamTrack = new FileInputStream(routesFileObj)) {

            PctGpxLoader.parseXml(fileIOStreamTrack);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//		PctGpxLoader.processTestTrack();
      PctGpxLoader.loadPctGpxFiles();
//		GraphManager.getInstance().exportHTML(GRAPH_DIRECTORY);
		GraphManager.getInstance().saveGraphs();
//		GraphManager.getInstance().clearGraphs();
//		GraphManager.getInstance().loadGraphs();

//		GraphManager.getInstance();
//		GraphManager.getInstance().loadGraphs();
		GraphManager.getInstance().saveInstance();
//		System.out.println(GraphManager.getInstance().toString());
    }
}