package name.jdstew.uphillahead;

import android.graphics.drawable.VectorDrawable;

/**
 * This class extends android.graphics.Point to add additional data for rendering icons in the display.
 *
 * @since 1.2
 */
public class NodePoint extends android.graphics.Point {

    private final VectorDrawable icon;
    private final double distance;
    private final double time;
    private final double gain;
    private final double loss;

    /**
     * @param i the icon to display
     * @param x the x-axis location of the icon
     * @param y the y-axis location of the icon
     * @param d the distance, in meters, from the observer
     * @param t the time, in hours, from the observer
     * @param g the gain, in meters, from the observer
     * @param l the loss, in meters, from the observer
     */
    public NodePoint (VectorDrawable i, double x, double y, double d, double t, double g, double l) {
        super((int) x, (int) y);

        icon = i;
        distance = d;
        time = t;
        gain = g;
        loss = l;
    }

    /**
     * Gets the icons for this NodePoint
     *
     * @return icon
     */
    public VectorDrawable getIcon() {
        return icon;
    }

    /**
     * Gets the distance from the observer.
     *
     * @return the distance, in meters, from the observer
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Gets the estimated time from the observer to this icon
     *
     * @return the time, in hours, from the observer
     */
    public double getTime() {
        return time;
    }

    /**
     * Gets the total gain from the observer to the icon
     *
     * @return the gain, in meters, from the observer
     */
    public double getGain() {
        return gain;
    }

    /**
     * Gets the total loss from the observer to the icon
     *
     * @return the loss, in meters, from the observer
     */
    public double getLoss() {
        return loss;
    }
}
