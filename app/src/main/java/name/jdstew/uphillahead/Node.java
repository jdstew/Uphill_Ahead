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
import java.util.Objects;

/**
 * The Node class represents both track points and waypoints from a GPX file.
 *
 * Nodes are created using this pattern:
 *
 * ['from' node]<--[previous Edge]-- [Node] --[next Edge]-->['to' Node]
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class Node implements Serializable {

    private static final long serialVersionUID = 6437697758827173265L;
    private double latitude; // (Y-degrees)
    private double longitude; // (X-degrees)
    private double elevation; // (Z-meters)
    private String name = null;
    private String description = null;
    private String symbol = null;
    private Edge nextEdge = null;
    private Edge prevEdge = null;

    /**
     * Creates a new node.  Nodes should be assumed to be
     * immutable, except when detecting signifcant errors when
     * adding relevant waypoints.
     *
     * @param lat  Latitude of node, in degrees
     * @param lon  Longitude of node, in degrees
     * @param elev Elevation of node, in meters
     */
    Node(double lat, double lon, double elev) {
        latitude = lat;
        longitude = lon;
        elevation = elev;
    }

    /**
     * Resets the values of this node. This method should only be
     * used to correct significant errors in order to match relevant
     * waypoints along an existing route.
     * 	 *
     * @param lat latitude of node, in degrees
     * @param lon longitude of node, in degrees
     * @param elev elevation of node, in meters
     */
    public void changeLocation(double lat, double lon, double elev) {
        latitude = lat;
        longitude = lon;
        elevation = elev;
        name = null;
        description = null;
        symbol = null;
        prevEdge = null;
        nextEdge = null;
    }

    /**
     * Get the latitude for this node.
     *
     * @return Latitude of node, in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude for this node.
     *
     * @return Longitude of node, in degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Get the elevation for this node.
     *
     * @return Elevation of node, in meters
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Set the text name of this node.
     *
     * @param name Name for node
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the text name of this node.
     *
     * @return Name for node
     */
    public String getName() {
        return name;
    }

    /**
     * Set the text description of this node.
     *
     * @param desc Description of node
     */
    public void setDescription(String desc) {
        description = desc;
    }

    /**
     * Get the text description of this node.
     *
     * @return Description of node
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the symbol text for this node.
     *
     * @param symbol Symbol for node
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Get the symbol text for this node.
     *
     * @return Symbol for node
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Set the next edge (toward the destination) from this node.
     *
     * @param e Edge to the next node
     */
    public void setNextEdge(Edge e) {
        nextEdge = e;
    }

    /**
     * Get the next edge (toward the destination) from this node.
     *
     * @return Edge to the next node
     */
    public Edge getNextEdge() {
        return nextEdge;
    }

    /**
     * Set the edge before (toward the start) from this node.
     *
     * @param e Edge from the previous node
     */
    public void setPrevEdge(Edge e) {
        prevEdge = e;
    }

    /**
     * Get the edge before (toward the start) from this node.
     *
     * @return the previous node
     */
    public Edge getPrevEdge() {
        return prevEdge;
    }

    @NonNull
    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumIntegerDigits(1);

        StringBuilder sb = new StringBuilder();
        sb.append("Node: ");
        sb.append(nf.format(latitude));
        sb.append(", ");
        sb.append(nf.format(longitude));
        sb.append(", ");
        sb.append(nf.format(elevation));
        sb.append("m ");
        sb.append(nf.format(Calcs.getMetersToFeet(elevation)));
        sb.append("ft");
        if (name != null) {
            sb.append(", ");
            sb.append(name);
        }
        if (symbol != null) {
            sb.append("(");
            sb.append(symbol);
            sb.append(")");
        }
        if (description != null) {
            sb.append(", ");
            sb.append(description);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;

        double d = Calcs.getDistance(this.getLatitude(), this.getLongitude(), other.getLatitude(), other.getLongitude(), false);
        return d < Units.NODE_EQUALS_MIN;
    }
}
