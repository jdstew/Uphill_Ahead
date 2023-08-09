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
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The GraphManager class provides management of graphs used by the user (e.g., loading, holding,
 * providing list-of, and saving during initial processing).
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public final class GraphManager implements Serializable {

    private static final String DEBUG_TAG = "name.jdstew.uphillahead.GraphManager";

    private static final long serialVersionUID = -6508936450047954999L;
    private static final String GRAPH_DIRECTORY = "C:\\tmp\\PCT\\res\\";
    private static final String GRAPH_FILE_NAME = "graph";
    private static final String GRAPH_MANAGER_FILE_NAME = "manager";
    private static final String SERIALIZED_FILE_TYPE = ".ser";

    private static GraphManager gm;
    private transient Context c;

    private transient List<Graph> graphs;

    // String: name, String: hashcode
    private final Map<String, String> graphMap;

    private GraphManager() {
        graphs = new ArrayList<>();
        graphMap = new HashMap<>();
    }


    public static GraphManager getInstance() {
        if (gm != null) {
            return gm;
        }

        // else load GraphManager
        long startTime = System.currentTimeMillis();

        try (FileInputStream fileInputStream = new FileInputStream(GRAPH_DIRECTORY + GRAPH_MANAGER_FILE_NAME + SERIALIZED_FILE_TYPE);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            gm = (GraphManager) objectInputStream.readObject();
            objectInputStream.close();
            long finishTime = System.currentTimeMillis();
//            System.out.println("GraphManager loading took " + (finishTime - startTime) + " milliseconds");
            gm.graphs = new ArrayList<>();
        }
        catch (ClassNotFoundException cnfe) {
//            System.out.println(cnfe);
            gm = new GraphManager();
//            System.out.println("GraphManager class not found, created new");
        }
        catch (IOException ioe) {
//            System.out.println(ioe);
            gm = new GraphManager();
//            System.out.println("GraphManager not found, created new");
        }

        return gm;
    }
    public static GraphManager getInstance(Context c) {
        if (gm != null) {
            return gm;
        }

        // else load GraphManager
        long startTime = System.currentTimeMillis();

        AssetManager assetManager = c.getAssets();
        try (InputStream inputStream = assetManager.open(GRAPH_MANAGER_FILE_NAME + SERIALIZED_FILE_TYPE, AssetManager.ACCESS_BUFFER);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            gm = (GraphManager) objectInputStream.readObject();
            gm.c = c;
            objectInputStream.close();
            long finishTime = System.currentTimeMillis();
//            Log.d(DEBUG_TAG, "GraphManager loading took " + (finishTime - startTime) + " milliseconds");
            gm.graphs = new ArrayList<>();
            // assetManager.close();
        }
        catch (ClassNotFoundException cnfe) {
//            Log.d(DEBUG_TAG, cnfe.getMessage());
            gm = new GraphManager();
//            Log.d(DEBUG_TAG, "GraphManager class not found, created new");
        }
        catch (IOException ioe) {
//            Log.d(DEBUG_TAG, ioe.getMessage());
            gm = new GraphManager();
//            Log.d(DEBUG_TAG, "GraphManager not found, created new");
        }

        return gm;
    }

    public List<String> getGraphNameList() {
        List<String> list = new ArrayList<>(graphMap.keySet());
        Collections.sort(list);
        return list;
    }

    public void saveInstance() {
        long startTime = System.currentTimeMillis();

        String fileName = GRAPH_DIRECTORY + GRAPH_MANAGER_FILE_NAME + SERIALIZED_FILE_TYPE;

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(gm);
            objectOutputStream.flush();
            objectOutputStream.close();
            long finishTime = System.currentTimeMillis();
//            Log.d(DEBUG_TAG, "GraphManager saving took " + (finishTime - startTime) + " milliseconds");
        } catch (IOException ioe) {
//            Log.d(DEBUG_TAG, ioe.getMessage());
        }
    }

    public boolean insertNode(Node n) {
        AtomicBoolean nodeInGraph = new AtomicBoolean(false);
        graphs.forEach(g -> {
            if (g.isNodeInExtents(n)) {
                if (g.insertNode(n)) {
                    nodeInGraph.set(true);
                }
            }
        });

        return nodeInGraph.get();
    }

    public void addGraph(Graph g) {
        graphs.add(g);
        graphMap.put(g.getName(), Integer.toString(g.hashCode()));
    }

    public void removeGraph(Graph g) {
        graphs.remove(g);
        graphMap.remove(g.getName());
    }

    public Graph getGraph(String graphName) {
        for (Graph g : graphs) {
            if (g.getName().compareTo(graphName) == 0) {
                return g;
            }
        }

        return loadGraph(graphName);
    }
    private Graph loadGraph(String graphName) {
        String hashCodeString = graphMap.get(graphName);
        if (hashCodeString == null) {
//            Log.w(DEBUG_TAG, "Graph not found by GraphManager (" + hashCodeString + ")");
            return null;
        }

        // TODO: fix the data stored in GraphManager regarding file names
        String fileName = hashCodeString.substring(15);
//        Log.d(DEBUG_TAG, "Graph file to load is " + fileName);

        long startTime = System.currentTimeMillis();
        Graph g = null;
        AssetManager assetManager = c.getAssets();
        try (InputStream inputStream = assetManager.open(fileName, AssetManager.ACCESS_BUFFER);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            g = (Graph) objectInputStream.readObject();
            g.relinkEdges();
            GraphManager.getInstance(c).addGraph(g);
            objectInputStream.close();
            long finishTime = System.currentTimeMillis();
//            Log.d(DEBUG_TAG, "Graph loading took " + (finishTime - startTime) + " milliseconds");
        }
        catch (ClassNotFoundException | IOException cnfe) {
//            Log.d(DEBUG_TAG, cnfe.getMessage());
        }

        return g;
    }

    public void saveGraph(Graph g) {
        long startTime = System.currentTimeMillis();

        String fileName = GRAPH_DIRECTORY + GRAPH_FILE_NAME + g.hashCode() + SERIALIZED_FILE_TYPE;

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(g);
            objectOutputStream.flush();
            objectOutputStream.close();
            long finishTime = System.currentTimeMillis();
//            Log.d(DEBUG_TAG, "Graph saving cumulative is " + (finishTime - startTime) + " milliseconds");
        } catch (IOException ioe) {
//            Log.d(DEBUG_TAG, ioe.getMessage());
        }
    }

    public void clearGraphs() {
        graphs.clear();
    }

    public void loadGraphs() {

        File graphsFileObj = new File(GRAPH_DIRECTORY);
        String[] graphFileNames = graphsFileObj.list();

        long startTime = System.currentTimeMillis();
        assert graphFileNames != null;
        for (String f : graphFileNames) {
            if (f.startsWith(GRAPH_FILE_NAME) && f.endsWith(SERIALIZED_FILE_TYPE)) {

                try (FileInputStream fileInputStream = new FileInputStream(GRAPH_DIRECTORY + f);
                     ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    Graph g = (Graph) objectInputStream.readObject();
                    g.relinkEdges();
                    GraphManager.getInstance(c).addGraph(g);
                    objectInputStream.close();
                    long finishTime = System.currentTimeMillis();
//                    Log.d(DEBUG_TAG, "Graph loading cumulative is " + (finishTime - startTime) + " milliseconds");
                }
                catch (ClassNotFoundException | IOException cnfe) {
//                    Log.d(DEBUG_TAG, cnfe.getMessage());
                }
            }
        }
    }

    public void saveGraphs() {
        long startTime = System.currentTimeMillis();
        graphs.forEach(g -> {
            String fileName = GRAPH_DIRECTORY + GRAPH_FILE_NAME + g.hashCode() + SERIALIZED_FILE_TYPE;
            graphMap.put(g.getName(), fileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(g);
                objectOutputStream.flush();
                objectOutputStream.close();
                long finishTime = System.currentTimeMillis();
//                Log.d(DEBUG_TAG, "Graph saving took " + (finishTime - startTime) + " milliseconds");
            } catch (IOException ioe) {
//                Log.d(DEBUG_TAG, ioe.getMessage());
            }
        });
    }

    public void exportHTML(String folder) {
        graphs.forEach(g -> g.renderHTML(new File (folder + g.getName() + ".html"), 1800, 800));
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("graphMap objects:");
        sb.append(System.lineSeparator());
        graphMap.forEach((k, v) -> {
            sb.append(k).append(" (");
            sb.append(v).append(")");
            sb.append(System.lineSeparator());
        });

        sb.append("--------------");

        sb.append("graphs objects:");
        sb.append(System.lineSeparator());
        graphs.forEach(g -> {
            sb.append(g.toString());
            sb.append(System.lineSeparator());
        });
        return sb.toString();
    }
}
