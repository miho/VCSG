package eu.mihosoft.vcsg;

import eu.mihosoft.vvecmath.Vector3d;

import java.util.Objects;

public class Bounds {
    private Vector3d min;
    private Vector3d max;

    Bounds(Vector3d min, Vector3d max) {
        this.min = min;
        this.max = max;
    }

    public Vector3d getMax() {
        return max;
    }

    public Vector3d getMin() {
        return min;
    }

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
