package eu.mihosoft.vcsg;

import eu.mihosoft.vvecmath.Vector3d;

import java.util.Objects;

/**
 * Axis aligned bounding box.
 */
public final class Bounds {

    private Vector3d min;
    private Vector3d max;

    private Vector3d dimensions;

    /**
     * Constructor.
     *
     * @param min minimum of this bounding box
     * @param max maximum of this bounding box
     */
    Bounds(Vector3d min, Vector3d max) {
        this.min = min;
        this.max = max;

        dimensions = max.subtracted(min);
    }

    /**
     * Returns the center of this bounding box.
     * @return center of this bounding box
     */
    public Vector3d getCenter() {
        return getMin().plus(getMax().minus(getMin()).multiplied(0.5));
    }

    /**
     * Returns the maximum of this bounding box.
     * @return maximum of this bounding box
     */
    public Vector3d getMax() {
        return max;
    }

    /**
     * Returns the minimum of this bounding box.
     * @return minimum of this bounding box
     */
    public Vector3d getMin() {
        return min;
    }

    /**
     * Returns the dimension of this bounding box (width, height, depth)
     * @return dimension of this bounding box (width, height, depth)
     */
    public Vector3d getDimensions() {
        return this.dimensions;
    }

    /**
     * Returns the width (x-axis) of this bounding box.
     * @return width (x-axis) of this bounding box.
     */
    public double getWidth() {
        return getDimensions().x();
    }

    /**
     * Returns the height (y-axis) of this bounding box.
     * @return width (y-axis) of this bounding box.
     */
    public double getHeight() {
        return getDimensions().y();
    }

    /**
     * Returns the depth (z-axis) of this bounding box.
     * @return depth (z-axis) of this bounding box.
     */
    public double getDepth() {
        return getDimensions().z();
    }

    /**
     * Returns this bounding box as CSG object.
     * @return this bounding box as CSG object
     */
    public CSG toCSG() {
        return CSG.box(min,max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounds bounds = (Bounds) o;
        return Objects.equals(getMin(), bounds.getMin()) &&
                Objects.equals(getMax(), bounds.getMax());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMin(), getMax());
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
