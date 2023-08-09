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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Graph class contains the Nodes and Edges, along with other summary details for a
 * route (or trail).
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Graph implements Serializable {

    private static final long serialVersionUID = 5323932677215095043L;
    private double maxLatitude;
    private double minLatitude;
    private double maxLongitude;
    private double minLongitude;
    private final List<Node> nodes; // has no setter or getter
    private final Set<Edge> edges; // has no setter or getter
    private String openLocCode = null; // open location code / plus code
    private String name = null;
    private String startDescription = null;
    private String endDescription = null;

    /**
     * Default constructor
     */
    Graph() {
        maxLatitude = -90.0;
        minLatitude = 90.0;
        maxLongitude = -180.0;
        minLongitude = 180.0;
        nodes = new ArrayList<>();
        edges = new HashSet<>();
    }

    /**
     * Re-connects Nodes with Edges after deserialization of stored Java objects.
     */
    public void relinkEdges() {
        nodes.forEach(n -> {
            Edge prevEdge = n.getPrevEdge();
            if (prevEdge != null) {
                prevEdge.setNextNode(n);
            }

            Edge nextEdge = n.getNextEdge();
            if (nextEdge != null) {
                nextEdge.setPrevNode(n);
            }
        });
    }

    /**
     * Adds String name values to the first and last Nodes for a graph, delineating the
     * start and end of a route (trail).
     */
    public void bookendGraph() {
        if (nodes.size() > 0) {
            nodes.get(0).setName("to end");
            nodes.get(nodes.size() - 1).setName("to start");
        }
    }

    private void setMaxLatitude(double lat) {
        maxLatitude = lat;
    }

    /**
     * Returns the maximum latitude of the Graph.
     *
     * @returnthe maximum latitude of the Graph
     */
    public double getMaxLatitude() {
        return maxLatitude;
    }

    private void setMinLatitude(double lat) {
        minLatitude = lat;
    }

    /**
     * Returns the minimum latitude of the Graph.
     *
     * @return minimum latitude of the Graph
     */
    public double getMinLatitude() {
        return minLatitude;
    }

    private void setMaxLongitude(double lon) {
        maxLongitude = lon;
    }

    /**
     * Returns the maximum longitude of the Graph.
     *
     * @return maximum longitude of the Graph
     */
    public double getMaxLongitude() {
        return maxLongitude;
    }

    private void setMinLongitude(double lon) {
        minLongitude = lon;
    }

    /**
     * Returns the minimum longitude of the Graph.
     *
     * @return minimum longitude of the Graph
     */
    public double getMinLongitude() {
        return minLongitude;
    }

    /**
     * Sets the open code location for the Graph, in general
     *
     * @param code open code location
     */
    public void setOpenCodeLocation(String code) {
        openLocCode = code;
    }

    /**
     * Gets the open code location for the Graph, in general
     *
     * @return open code location
     */
    public String getOpenCodeLocation() {
        return openLocCode;
    }

    /**
     * Sets the String name for the Graph
     *
     * @param name String name for the Graph
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the String name for the Graph
     *
     * @return  String name for the Graph
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the starting description of the Graph.
     *
     * @param desc starting description of the Graph
     */
    public void setStartDescription(String desc) {
        startDescription = desc;
    }

    /**
     * Gets the starting description of the Graph.
     *
     * @return starting description of the Graph
     */
    public String getStartDescription() {
        return startDescription;
    }

    /**
     * Returns the first Node (at the trailhead)
     *
     * @return first Node
     */
    public Node getStartNode() { return nodes.get(0);}

    /**
     * Sets the ending description of the Graph.
     *
     * @param desc starting description of the Graph
     */
    public void setEndDescription(String desc) {
        endDescription = desc;
    }

    /**
     * Gets the ending description of the Graph.
     *
     * @return starting description of the Graph
     */
    public String getEndDescription() {
        return endDescription;
    }

    /**
     * Returns the last Node (at the destination)
     *
     * @return last Node
     */
    public Node getLastNode() { return nodes.get(nodes.size() - 1);}

    public void appendNode(Node n) {
        appendNode(n, -1.0);
    }

    /**
     * Adds node to end of node list
     *
     * @param n Node to append
     */
    public void appendNode(Node n, double distanceToPreviousNode) {
        // check latitude extents
        if (n.getLatitude() > this.getMaxLatitude()) {
            this.setMaxLatitude(n.getLatitude());
        }
        if (n.getLatitude() < this.getMinLatitude()) {
            this.setMinLatitude(n.getLatitude());
        }

        // check longitude extents
        if (n.getLongitude() > this.getMaxLongitude()) {
            this.setMaxLongitude(n.getLongitude());
        }
        if (n.getLongitude() < this.getMinLongitude()) {
            this.setMinLongitude(n.getLongitude());
        }

        nodes.add(n);
        if (nodes.size() > 1) {
            Edge e;
            if (distanceToPreviousNode < 0.0) {
                e = new Edge(nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1));
            } else {
                e = new Edge(nodes.get(nodes.size() - 2), nodes.get(nodes.size() - 1), distanceToPreviousNode);
            }
            edges.add(e);
            nodes.get(nodes.size() - 2).setNextEdge(e);
            nodes.get(nodes.size() - 1).setPrevEdge(e);
        }
    }

    /**
     * Returns whether a Node is within the extents of the Graph.
     *
     * @param n Node to check if within extents of the Graph
     * @return true if inside this Graph's extents
     */
    public boolean isNodeInExtents(Node n) {
        return n.getLatitude() <= this.getMaxLatitude() && n.getLatitude() >= this.getMinLatitude()
                && n.getLongitude() <= this.getMaxLongitude() && n.getLongitude() >= this.getMinLongitude();
    }

    /**
     * Returns the index of the closest Node within all Nodes in a Graph.
     *
     * @param n Node to compare
     * @return index of closest Node
     */
    private int getClosestNodeIndex(Node n) {
        int minIndex = -1;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < nodes.size(); ++i) {
            Node n0 = nodes.get(i);
            double d = Calcs.getDistance(n0.getLatitude(), n0.getLongitude(), n.getLatitude(), n.getLongitude(), false);
            if (d < minDist) {
                minDist = d;
                minIndex = i;
            }
        }

        return minIndex;
    }

    /**
     * Graph is searched for its proximity to the Node provided.
     * <p>
     * If the Node equals and existing node, then the node's name, description, and
     * symbol values are replaced with the inserted node.
     * <p>
     * If the Node is along an existing Edge, then the Node is inserted along with
     * the creation of two new edges in between the closest nodes.
     *
     * @param n the Node to insert into this Graph
     * @return whether the Node was inserted or not
     */
    public boolean insertNode(Node n) {
        /*
         * 1. search all nodes, find the closest node (d1) 2. if d1 is really small (~10
         * ft), then update that node 3. calculate the distance to the edges (all,
         * within +/-3 of the node) 4. if smallest of the distances to the edges is
         * within a reasonable distance (~30 feet), insert node and create new edges 5.
         * if not at node or edge, report the distance to the closes node and distance
         * to the closest edge
         */

        int cli = this.getClosestNodeIndex(n);
        Node closestNode = nodes.get(cli);
        if (closestNode.equals(n)) {
            if (n.getName() != null) {
                closestNode.setName(n.getName());
            }
            if (n.getDescription() != null) {
                closestNode.setDescription(n.getDescription());
            }
            if (n.getSymbol() != null) {
                closestNode.setSymbol(n.getSymbol());
            }
            return true;
        }

        Edge closestEdge = null;
        double minDist = Double.MAX_VALUE;
        for (int i = Math.max(cli - 3, 0); i < Math.min(cli + 4, nodes.size()); ++i) {
            Edge prevEdge = nodes.get(i).getPrevEdge();
            if (prevEdge != null) {
                double d = Calcs.getNodeToEdgeDist(n, prevEdge);
                if (d < minDist) {
                    minDist = d;
                    closestEdge = prevEdge;
                }
            }
            Edge nextEdge = nodes.get(i).getNextEdge();
            if (nextEdge != null) {
                double d = Calcs.getNodeToEdgeDist(n, nextEdge);
                if (d < minDist) {
                    minDist = d;
                    closestEdge = nextEdge;
                }
            }
        }
        if (closestEdge != null && minDist < Units.NODE_TO_EDGE_MATCH) {
            Node prevNode = closestEdge.getPrevNode();
            Node nextNode = closestEdge.getNextNode();

            if (n.getElevation() < Math.min(prevNode.getElevation(), nextNode.getElevation())
                    - Config.getSnapToTrailValue(Config.SNAP_TO_TRAIL_DEFAULT)
                    || n.getElevation() > Math.max(prevNode.getElevation(), nextNode.getElevation())
                    + Config.getSnapToTrailValue(Config.SNAP_TO_TRAIL_DEFAULT)) {

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(1);
                nf.setMinimumIntegerDigits(1);

//                System.out.println(
//                        "WARNING: node " + n.getName() + " is along an edge by not between adjacent node elevations.");
//                System.out.println("  prev: " + nf.format(prevNode.getElevation()));
//                System.out.println("     n: " + nf.format(n.getElevation()));
//                System.out.println("  next: " + nf.format(nextNode.getElevation()));

                double revisedElev = Graph.getElevBtwnNodes(prevNode, n);
//                System.out.println("  ... " + nf.format(revisedElev) + " will be assigned to this waypoint");

                n.changeLocation(n.getLatitude(), n.getLongitude(), revisedElev);
            }
            edges.remove(closestEdge);

            Edge edgeToNode = new Edge(prevNode, n);
            n.setPrevEdge(edgeToNode);
            prevNode.setNextEdge(edgeToNode);
            edges.add(edgeToNode);

            Edge edgeFromNode = new Edge(prevNode, n);
            n.setNextEdge(edgeFromNode);
            nextNode.setPrevEdge(edgeFromNode);
            edges.add(edgeFromNode);

            nodes.add(nodes.indexOf(nextNode), n);
            return true;
        }

        if (closestEdge != null && minDist < Units.NODE_TO_EDGE_CLOSE) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setMinimumIntegerDigits(1);

            String sb = "ERROR: waypoint " + n.getName() +
                    " (" + n.getLatitude() + ", " + n.getLongitude() + ")" +
                    " was " + nf.format(minDist) + "m away (but less than "
                    + nf.format(Units.NODE_TO_EDGE_CLOSE) + "m)" +
                    " and was not inserted.";
//            System.out.println(sb);
        }
        return false;
    }

    /**
     * Returns the elevation between two Nodes, given the preceeding Node and the Node in question.
     *
     * @param prevNode Node preceeding the Node in question
     * @param n The Node to estimate an elevation for.
     * @return the elevation, in meters
     */
    public static double getElevBtwnNodes(Node prevNode, Node n) {
        double d = Calcs.getDistance(prevNode.getLatitude(), prevNode.getLongitude(), n.getLatitude(), n.getLongitude(), false);
        return Graph.getElevBtwnNodes(prevNode.getNextEdge(), d, true);
    }

    /**
     * Returns the elevation of a Node, for a give distance along an Edge.
     *
     * @param e the Edge to determine the elevation of the Node in question
     * @param distance the distance along the Edge (in a From/To manner)
     * @param toEnd the direction, true for from start-to-end manner
     * @return the estimated elevation of the Node in question
     */
    public static double getElevBtwnNodes(Edge e, double distance, boolean toEnd) {
        if (toEnd) {
            return e.getPrevNode().getElevation() - e.getVerticalDistance() / e.getHorizontalDistance() * distance;
        } else {
            return e.getPrevNode().getElevation() - (e.getVerticalDistance() / e.getHorizontalDistance()) * (e.getHorizontalDistance() - distance);
        }
    }

    /**
     * Finds the closest node within the graph to the observer node. The returned
     * node will be the observer node with temporary the start or end of the graph,
     * then the returned node is that closest node.
     */
    public Node getEntryEdge(Node node, int snapToTrail, boolean toEnd) {
        // is node within this graph's extent
        if (!this.isNodeInExtents(node)) {
            return null;
        }


        int closestNodeIndex = this.getClosestNodeIndex(node);
        Node closestNode = nodes.get(closestNodeIndex);
        if (closestNode.equals(node)) {
            return closestNode;
        }

        Edge closestEdge = null;
        double minDist = Double.MAX_VALUE;
        for (int i = Math.max(closestNodeIndex - 3, 0); i < Math.min(closestNodeIndex + 4, nodes.size()); ++i) {
            Edge nextEdge = nodes.get(i).getNextEdge();
            if (nextEdge != null) {
                double d = Calcs.getNodeToEdgeDist(node, nextEdge);
                if (d < minDist) {
                    minDist = d;
                    closestEdge = nextEdge;
                }
            }
        }

        // if close to trail, set entry edge along nearest
        if (minDist < (double)snapToTrail) {
            Edge joiningEdge;

            if (toEnd) {
                double d = Calcs.getDistance(closestEdge.getPrevNode().getLatitude(), closestEdge.getPrevNode().getLongitude(),
                        node.getLatitude(), node.getLongitude(), false);
                double e = closestEdge.getPrevNode().getElevation() + d/closestEdge.getDistance() *
                        (closestEdge.getNextNode().getElevation() - closestEdge.getPrevNode().getElevation());
                if (e == Double.NaN || e == -Double.NaN) {
                    e = closestEdge.getPrevNode().getElevation();
                }

                node.changeLocation(node.getLatitude(), node.getLongitude(), e);
                joiningEdge = new Edge(node, closestEdge.getNextNode());
                node.setNextEdge(joiningEdge);
            } else {
                double d = Calcs.getDistance(closestEdge.getNextNode().getLatitude(), closestEdge.getNextNode().getLongitude(),
                        node.getLatitude(), node.getLongitude(), false);
                double e = closestEdge.getNextNode().getElevation() + d/closestEdge.getDistance() *
                        (closestEdge.getPrevNode().getElevation() - closestEdge.getNextNode().getElevation());
                if (e == Double.NaN || e == -Double.NaN) {
                    e = closestEdge.getNextNode().getElevation();
                }

                node.changeLocation(node.getLatitude(), node.getLongitude(), e);
                joiningEdge = new Edge(closestEdge.getPrevNode(), node);
                node.setPrevEdge(joiningEdge);
            }
            return node;
        } else {
            return null;
        }
    }

    /**
     * Returns a full printout of all the Nodes within the Graph
     *
     * @return printable trace of the Nodes within the Graph
     */
    public String getNodeTrace() {
        StringBuilder sb = new StringBuilder();
        nodes.forEach(n -> {
            sb.append(n);
            if (n.getNextEdge() != null) {
                sb.append(n.getNextEdge());
                sb.append(System.lineSeparator());
            }
        });

        return sb.toString();
    }

    /**
     * Creates an HTML-ready XML content to display a Graph as a web page.
     *
     * @param pathName for the file to create
     * @param width pixel count for the displayed output
     * @param height pixel count for th displayed output
     */
    public void renderHTML(File pathName, int width, int height) {
        // determine hDist of Graph
        AtomicReference<Double> hDistTotal = new AtomicReference<>(0.0);
        edges.forEach(e -> {
            hDistTotal.set(hDistTotal.get() + e.getHorizontalDistance());
        });

        double hScale = (double) width / hDistTotal.get();
        double vScale = hScale * Config.EXAGGERATION_DEFAULT;

        double x = 0.0;
        double y = height / 2.0;

        try (FileWriter fw = new FileWriter(pathName, false); // clobber existing file
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("<!DOCTYPE html>");
            bw.newLine();
            bw.write("<html>");
            bw.newLine();
            bw.write("<body>");
            bw.newLine();
            bw.write("<canvas id='myCanvas' width='" + width + "' height='" + height
                    + "' style='border:1px solid #d3d3d3;'/>");
            bw.newLine();
            bw.write("<script>");
            bw.newLine();
            bw.write("var c = document.getElementById('myCanvas');");
            bw.newLine();
            bw.write("var ctx = c.getContext('2d');");
            bw.newLine();
            bw.write("ctx.strokeStyle = 'red';");
            bw.newLine();
            bw.write("ctx.beginPath();");
            bw.newLine();
            bw.write("ctx.moveTo(" + x + ", " + y + ");");

            for (int i = 0; i < nodes.size(); ++i) {
                Edge e = nodes.get(i).getNextEdge();

                if (e != null) {
                    x += e.getHorizontalDistance() * hScale;
                    y += e.getVerticalDistance() * vScale;

                    bw.write("ctx.lineTo(" + (int) x + ", " + (int) y + ");");
                    bw.newLine();
                }
            }

            bw.write("ctx.stroke();");
            bw.newLine();
            bw.write("</script> ");
            bw.newLine();
            bw.write("</body>");
            bw.newLine();
            bw.write("</html>");
//            System.out.println("Completed writing " + pathName);

        } catch (IOException ioException) {
//            System.out.println(ioException);
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph: ");
        sb.append(name);
        sb.append(", ");
        sb.append(this.getStartDescription());
        sb.append(" / ");
        sb.append(this.getEndDescription());
        sb.append(", ");
        sb.append(nodes.size()).append(" nodes");
        sb.append(", ");
        sb.append(edges.size()).append(" edges");

        // calculate total graph distance
        double tDist = 0.0;
        double gain = 0.0;
        double lost = 0.0;
        Node n = nodes.get(0);
        while (n != null && n.getNextEdge() != null) {
            Edge e = n.getNextEdge();
            tDist += e.getDistance();
            if (e.getVerticalDistance() > 0) {
                gain += e.getVerticalDistance();
            } else {
                lost -= e.getVerticalDistance();
            }
            n = e.getNextNode();
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumIntegerDigits(1);

        sb.append(", ");
        sb.append(nf.format(tDist / 1000.0));
        sb.append("km (");
        sb.append(nf.format(Calcs.getMetersToMiles(tDist)));
        sb.append("mi), +");
        sb.append(nf.format(gain));
        sb.append("m (+");
        sb.append(nf.format(Calcs.getFeet(gain)));
        sb.append("ft), -");
        sb.append(nf.format(lost));
        sb.append("m (-");
        sb.append(nf.format(Calcs.getFeet(lost)));
        sb.append("ft)");

        sb.append(System.lineSeparator());
        sb.append("       [TL: ");
        sb.append(this.getMaxLatitude());
        sb.append(",");
        sb.append(this.getMinLongitude());
        sb.append(" to ");
        sb.append(this.getMinLatitude());
        sb.append(", ");
        sb.append(this.getMaxLongitude());
        sb.append(":BR] ");
        sb.append(Integer.toHexString(this.hashCode()));

        // to-do add open code location
        return sb.toString();
    }
}
