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

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * The Edge class represents the edge between two Nodes. The edges are
 * bidirectional; however, edges created using this pattern:
 *
 * ['from' Node] <--previous Node-- [Edge] --next Node--> ['to' Node]
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Edge implements Serializable {

    private static final long serialVersionUID = 8892651119787302825L;
    private double hDist; // meters
    private double vDist; // meters
    private double tDist; // meters
    private final double slope;
    private transient Node nextNode;
    private transient Node prevNode;


    public Edge(Node a, Node b) {
        setHorizontalDistance(Calcs.getDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), true));
        setVerticalDistance(b.getElevation() - a.getElevation());
        slope = getHorizontalDistance() > 0.0 ? getVerticalDistance() / getHorizontalDistance() : 0.0;

        prevNode = a;
        nextNode = b;

        if (getHorizontalDistance() > 30.0 && getSlope() > 0.7 || getSlope() < -0.7) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumIntegerDigits(1);

//            System.out.println("Caution, significant slope change:");
//            System.out.println("   from: "  + prevNode);
//            System.out.println("   edge: " + nf.format(hDist) + "m, " + nf.format(slope * 100) + "%");
//            System.out.println("     to: "  + nextNode);
        }
    }

    /**
     * Instantiates an edge from two nodes.
     *
     * @param a The 'from' node.
     * @param b The 'to' node.
     */
    public Edge(Node a, Node b, double horizontalDistance) {
        setHorizontalDistance(horizontalDistance);
        setVerticalDistance(b.getElevation() - a.getElevation());
        slope = getHorizontalDistance() > 0.0 ? getVerticalDistance() / getHorizontalDistance() : 0.0;

        prevNode = a;
        nextNode = b;
    }

    /**
     * Sets the horizontal distance between two nodes, from a distance calculation.
     *
     * @param d Horizontal distance, in meters.
     */
    public void setHorizontalDistance(double d) {
        hDist = d;
        tDist = Math.sqrt(Math.pow(hDist, 2.0) + Math.pow(vDist, 2.0)) * Units.ROUTE_DIST_CORR;
    }

    /**
     * Gets the horizontal distance between two nodes, from a distance calculation.
     *
     * @return Horizontal distance, in meters.
     */
    public double getHorizontalDistance() {
        return hDist;
    }


    /**
     * Sets the horizontal distance between two nodes, from a Euclideo geometry calculation (presumed).
     *
     * @param d Vertical distance, in meters.
     */
    public void setVerticalDistance(double d) {
        vDist = d;
        tDist = Math.sqrt(Math.pow(hDist, 2.0) + Math.pow(vDist, 2.0)) * Units.ROUTE_DIST_CORR;
    }

    /**
     * Gets the horizontal distance between two nodes, from a Euclideo geometry calculation (presumed).
     *
     * @return Vertical distance, in meters.
     */
    public double getVerticalDistance() {
        return vDist;
    }

    /**
     * Get total distance (horizontal and vertical) between two nodes.
     *
     * @return Approximate distance using Pythagorean Theorem of horizontal and vertical distances.
     */
    public double getDistance() {
        return tDist; // always from previous to next
    }

    /**
     * Get the slope between two nodes.
     *
     * @return Slope between from-to nodes, in percentage (or grade)
     */
    public double getSlope() {
        return getHorizontalDistance() > 0.0 ? getVerticalDistance() / getHorizontalDistance() : 0.0;
    }

    /**
     * Get the next node (towards the end of this route).
     *
     * @return The to node
     */
    public Node getNextNode() {
        return nextNode;
    }

    /**
     * Set the next node (towards the end of this route).
     *
     * @param n The to node
     */
    public void setNextNode(Node n) {
        nextNode = n;
    }

    /**
     * Get the previous node (towards the start of this route).
     *
     * @return The from node
     */
    public Node getPrevNode() {
        return prevNode;
    }

    /**
     * Set the previous node (towards the start of this route).
     *
     * @param n The from node
     */
    public void setPrevNode(Node n) {
        prevNode = n;
    }

    @NonNull
    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumIntegerDigits(1);

        return "   Edge: " + nf.format(hDist) + " meters, " + nf.format(vDist)
                + " meters gain, " + nf.format(slope) + "%";
    }
}
