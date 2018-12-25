package eu.mihosoft.vcsg;

import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * A simple but effective CSG API based on the occ-csg command line tool.
 */
public final class CSG {
    private File file;

    private static String defaultFileType = ".stp";
    private String fileType = defaultFileType;
    private double fuzzyValue = 0;

    /**
     * Sets the BREP file format as internal representation of this CSG object.
     *
     * @return this CSG object
     */
    public CSG useBREP() {
        this.fileType = ".brep";
        return this;
    }

    /**
     * Sets the STEP file format as internal representation of this CSG object.
     *
     * @return this CSG object
     */
    public CSG useSTEP() {
        this.fileType = ".stp";
        return this;
    }

    /**
     * Sets the BREP file format as default for internal representation of all CSG object.
     *
     * @return this CSG object
     */
    public static void useBREPAsDefault() {
        defaultFileType = ".brep";
    }

    /**
     * Sets the STEP file format as default for internal representation of all CSG object.
     */
    public static void useSTEPAsDefault() {
        defaultFileType = ".stp";
    }

    CSG(String fileType) {
        try {
            this.setFileType(fileType);
            file = Files.createTempFile("_vcsg_", "." + getFileType()).toFile();
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    private CSG(File f, String fileType) {
        this.file = f;
        this.setFileType(fileType);
    }

    /**
     * Returns the file type of this CSG object.
     * @return file type of this CSG object as String
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Sets the file type of this CSG object.
     * @param fileType file type to set
     * @return this CSG object
     */
    public CSG setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    /**
     * Sets the fuzzy value used for boolean operations (see OCC documentation).
     * @param fuzzyValue the fuzzy value to set
     * @return this CSG object
     */
    public CSG setFuzzyValue(double fuzzyValue) {
        this.fuzzyValue = fuzzyValue;
        return this;
    }

    /**
     * Returns the fuzzy value used for boolean operations.
     * @see #setFuzzyValue(double)
     * @return the fuzzy value used for boolean operations
     */
    public double getFuzzyValue() {
        return fuzzyValue;
    }

    /**
     * Returns a deep clone of this CSG object.
     * @return a deep clone of this CSG object
     */
    public CSG clone() {
        return new CSG(getFile(), getFileType());
    }

    /**
     * Computes the difference between this CSG object and the specified CSG objects.
     * @param others CSG objects to be removed from this CSG.
     * @return difference between this CSG object and the specified CSG objects
     */
    public CSG difference(CSG... others) {

        CSG result = new CSG(fileType);
        CSG union = new CSG(fileType).union(others);

        String[] exeArgs;

        if (Double.compare(getFuzzyValue(), 0) == 0) {
            exeArgs = new String[]{"--csg", "difference",
                    getFile().getAbsolutePath(),
                    union.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath()};
        } else {
            exeArgs = new String[]{"--csg", "difference",
                    getFile().getAbsolutePath(),
                    union.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath(),
                    "0.1",
                    "" + getFuzzyValue()};
        }

        int exitValue = VCSG.execute(exeArgs).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Computes the difference between this CSG object and the specified CSG object.
     * @param other CSG object to be removed from this CSG.
     * @return difference between this CSG object and the specified CSG object
     */
    public CSG difference(CSG other) {

        CSG result = new CSG(fileType);

        String[] exeArgs;

        if (Double.compare(getFuzzyValue(), 0) == 0) {
            exeArgs = new String[]{"--csg", "difference",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath()};
        } else {
            exeArgs = new String[]{"--csg", "difference",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath(),
                    "0.1",
                    "" + getFuzzyValue()};
        }

        int exitValue = VCSG.execute(exeArgs).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns the union of this CSG object and the specified CSG objects.
     * @param others CSG objects to unify with this CSG object
     * @return the union of this CSG object and the specified CSG objects
     */
    public CSG union(CSG... others) {
        return union(Arrays.asList(others));
    }

    /**
     * Returns the union of this CSG object and the specified CSG objects.
     * @param others CSG objects to unify with this CSG object
     * @return the union of this CSG object and the specified CSG objects
     */
    public CSG union(List<CSG> others) {

        CSG result = this.clone();
        for (CSG csg : others) {
            result = csg.union(csg);
        }

        return result;
    }

    /**
     * Returns the union of this CSG object and the specified CSG object.
     * @param other CSG object to unify with this CSG object
     * @return the union of this CSG object and the specified CSG object
     */
    public CSG union(CSG other) {

        CSG result = new CSG(fileType);

        String[] exeArgs;

        if (Double.compare(getFuzzyValue(), 0) == 0) {
            exeArgs = new String[]{"--csg", "union",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath()};
        } else {
            exeArgs = new String[]{"--csg", "union",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath(),
                    "0.1",
                    "" + getFuzzyValue()};
        }

        int exitValue = VCSG.execute(exeArgs).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns the intersection of this CSG object and the specified CSG object.
     * @param other CSG objects to intersect with this CSG object
     * @return the intersect of this CSG object and the specified CSG objects
     */
    public CSG intersect(CSG other) {

        CSG result = new CSG(fileType);

        String[] exeArgs;

        if (Double.compare(getFuzzyValue(), 0) == 0) {
            exeArgs = new String[]{"--csg", "intersection",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath()};
        } else {
            exeArgs = new String[]{"--csg", "intersection",
                    getFile().getAbsolutePath(),
                    other.getFile().getAbsolutePath(),
                    result.getFile().getAbsolutePath(),
                    "0.1",
                    "" + getFuzzyValue()};
        }

        int exitValue = VCSG.execute(exeArgs).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    File getFile() {
        return file;
    }

    /**
     * Splits this CSG object into faces (each represented by a CSG object).
     * @return faces of this CSG object (each represented by a CSG object)
     */
    public List<CSG> split() {

        try {
            File tmpDir = Files.createTempDirectory("_vcsg").toFile();

            File shapeF = new File(tmpDir, "shape." + getFileType());

            Files.copy(getFile().toPath(), shapeF.toPath());

            String[] exeArgs = {"--edit", "split-shape",
                    shapeF.getAbsolutePath(), "brep"};

            int exitValue = VCSG.execute(
                    tmpDir, exeArgs
            ).print(null, System.err).getProcess().exitValue();

            if (exitValue != 0) {
                throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
            }

            return Files.list(tmpDir.toPath()).filter(f -> !f.equals(shapeF.toPath())).map(f -> new CSG(f.toFile(), getFileType())).
                    collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException("Cannot create tmp folder", e);
        }

        return Collections.emptyList();
    }

    /**
     * Returns a CSG with rounded edges (rounding with specified radius).
     * @param radius radius for edge rounding
     * @return a CSG with rounded edges
     */
    public CSG round(double radius) {
        CSG result = new CSG(fileType);

        String[] exeArgs = {"--edit", "round-edges",
                "" + radius,
                getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns the version string of the occ-csg binary used by this CSG object.
     * @return version string
     */
    public String getVersion() {
        StringPrintStream ps = new StringPrintStream();

        String[] exeArgs = {
                "--version",
                getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(ps, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        String output = ps.toString();

        output = output.substring("Version ".length());

        return output;
    }

    /**
     * Returns the axis aligned bounding box of this CSg object.
     * @return axis aligned bounding box
     */
    public Bounds getBounds() {
        StringPrintStream ps = new StringPrintStream();

        String[] exeArgs = {"--bounds",
                getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(ps, System.err).getProcess().exitValue();

        System.out.println(ps);

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        String output = ps.toString();

        String[] lines = output.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.contains("-> bounds")) {
                String[] boundStrings = l.replace("-> bounds:", "").trim().split(",");

                if (boundStrings.length != 6) {
                    throw new RuntimeException("Wrong number of bound values, expected 6, got " + boundStrings.length);
                }

                double[] bounds = new double[boundStrings.length];

                for (int j = 0; j < bounds.length; j++) {
                    try {
                        bounds[j] = Double.parseDouble(boundStrings[j]);
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Cannot convert bounds entry " + j, ex);
                    }
                }

                return new Bounds(
                        Vector3d.xyz(bounds[0], bounds[1], bounds[2]),
                        Vector3d.xyz(bounds[3], bounds[4], bounds[5])
                );
            }
        }

        throw new RuntimeException("Cannot compute bounds");
    }

    /**
     * Returns a box CSG with the specified min and max coordinates.
     * @param min minimum
     * @param max maximum
     * @return box CSG
     */
    public static CSG box(Vector3d min, Vector3d max) {
        CSG result = new CSG(defaultFileType);

        String coords = min.x() + "," + min.y() + "," + min.z() + "," + max.x() + "," + max.y() + "," + max.z();

        String[] exeArgs = {"--create", "box", coords,
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns a box CSG with the specified size (x,y,z) at origin (0,0,0).
     * @param size size
     * @return box CSG
     */
    public static CSG box(double size) {
        Vector3d min = Vector3d.xyz(-size/2.0,-size/2.0,-size/2.0);
        Vector3d max = Vector3d.xyz(size/2.0,size/2.0,size/2.0);

        return CSG.box(min, max);
    }

    /**
     * Returns a box CSG with the specified width, height and depth at origin (0,0,0).
     * @param w width (x-axis)
     * @param h height (y-axis)
     * @param d depth (z-axis)
     * @return box CSG
     */
    public static CSG box(double w, double h, double d) {
        Vector3d min = Vector3d.xyz(-w/2.0,-h/2.0,-d/2.0);
        Vector3d max = Vector3d.xyz(w/2.0,h/2.0,d/2.0);

        return CSG.box(min, max);
    }

    /**
     * Returns a box CSG at the specified origin with the specified width, height and depth.
     * @param origin center location of the box
     * @param w width (x-axis)
     * @param h height (y-axis)
     * @param d depth (z-axis)
     * @return box CSG
     */
    public static CSG box(Vector3d origin, double w, double h, double d) {
        Vector3d min = Vector3d.xyz(-w/2.0,-h/2.0,-d/2.0);
        Vector3d max = Vector3d.xyz(w/2.0,h/2.0,d/2.0);

        return CSG.box(min, max).transformed(Transform.unity().translate(origin));
    }

    /**
     * Returns a sphere CSG with the specified radius at origin (0,0,0).
     * @param radius radius of the sphere
     * @return sphere CSG
     */
    public static CSG sphere(double radius) {
        return sphere(Vector3d.ZERO, radius);
    }

    /**
     * Returns a sphere CSG at the specified origin with the specified radius.
     * @param origin center location of the sphere
     * @param radius radius of the sphere
     * @return sphere CSG
     */
    public static CSG sphere(Vector3d origin, double radius) {
        CSG result = new CSG(defaultFileType);

        String coords = origin.x() + "," + origin.y() + "," + origin.z() + "," + radius;

        String[] exeArgs = {"--create", "sphere", coords,
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns a cylinder CSG with the specified radius and height.
     * @param radius radius of the cylinder
     * @param height height of the cylinder
     * @return cylinder CSG
     */
    public static CSG cyl(double radius, double height) {
        return cyl(Vector3d.ZERO, radius, height);
    }

    /**
     * Returns a cylinder CSG with the specified origin, radius and height.
     * @param origin origin of the cylinder
     * @param radius radius of the cylinder
     * @param height height of the cylinder
     * @return cylinder CSG
     */
    public static CSG cyl(Vector3d origin, double radius, double height) {
        CSG result = new CSG(defaultFileType);

        String coords = origin.x() + "," + origin.y() + "," + origin.z() + "," + radius + "," + height;

        String[] exeArgs = {"--create", "cyl", coords,
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    /**
     * Returns a (truncated) cone CSG with the specified origin, upper radius, lower radius and height
     * @param origin origin of this cylinder
     * @param r1 upper radius
     * @param r2 lower radius
     * @param height height of the cone
     * @return cone CSG
     */
    public static CSG cone(Vector3d origin, double r1, double r2, double height) {

        if (Double.compare(r1, r2) == 0) {
            System.err.println("WARNING: radii of cone are identical. Converting it to a cylinder tp prevent OCC to crash.");
            return cyl(origin, r1, height);
        }

        CSG result = new CSG(defaultFileType);

        String coords = origin.x() + "," + origin.y() + "," + origin.z() + "," + r1 + "," + r2 + "," + height;

        String[] exeArgs = {
                "--create", "cone", coords,
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Returns a transformed copy of this CSG.
     * @param transform transform to apply
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {

        CSG result = new CSG(fileType);

        double[] v = transform.to();

        String values =
                v[0] + "," + v[1] + "," + v[2] + "," + v[3] + "," +
                        v[4] + "," + v[5] + "," + v[6] + "," + v[7] + "," +
                        v[8] + "," + v[9] + "," + v[10] + "," + v[11];

        String[] exeArgs = {
                "--transform", "matrix", values,
                this.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Saves this CSG as STEP file.
     * @param f destination file (must end with {@code .stp})
     * @return this CSG
     */
    public CSG toSTEP(File f) {

        if (!f.getAbsolutePath().toLowerCase().endsWith(".stp")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stp'");
        }

        String[] exeArgs = {
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return this;
    }

    /**
     * Saves this CSG as BREP file.
     * @param f destination file (must end with {@code .brep})
     * @return this CSG
     */
    public CSG toBREP(File f) {

        if (!f.getAbsolutePath().toLowerCase().endsWith(".brep")) {
            throw new RuntimeException("Cannot convert file. File must end with '.brep'");
        }

        String[] exeArgs = {
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return this;
    }

    /**
     * Saves this CSG as STL file.
     * @param f destination file (must end with {@code .stl})
     * @param tol tolerance for triangulation ({@code tol > 0}, lower values result in more accurate triangulation)
     * @return this CSG
     */
    public CSG toSTL(File f, double tol) {

        if (!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        String[] exeArgs = {"--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath(),
                "" + tol};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return this;
    }

    /**
     * Saves this CSG as STL file.
     * @param f destination file (must end with {@code .stl})
     * @return this CSG
     */
    public CSG toSTL(File f) {

        if (!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        String[] exeArgs = {
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()};

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return this;
    }

    /**
     * Creates a CSG object from the specified BREP file.
     * @param f source file (must end with {@code .brep})
     * @return CSG object
     */
    public static CSG fromBREP(File f) {
        if (!f.getAbsolutePath().toLowerCase().endsWith(".brep")) {
            throw new RuntimeException("Cannot convert file. File must end with '.brep'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", defaultFileType).toFile();
            new CSG(f, defaultFileType).toSTEP(dest);
            return new CSG(dest, "step");
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    /**
     * Creates a CSG object from the specified STEP file.
     * @param f source file (must end with {@code .stp})
     * @return CSG object
     */
    public static CSG fromSTEP(File f) {
        if (!f.getAbsolutePath().toLowerCase().endsWith(".stp")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stp'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", defaultFileType).toFile();
            new CSG(f, defaultFileType).toSTEP(dest);
            return new CSG(dest, defaultFileType);
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    /**
     * Creates a CSG object from the specified STL file.
     * Be aware that STL to BREP conversion can cause performance issues. Prefer STEP or BREP import.
     * @param f source file (must end with {@code .stl})
     * @return CSG object
     * @see #fromBREP(File)
     * @see #fromSTEP(File)
     */
    public static CSG fromSTL(File f) {
        if (!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", defaultFileType).toFile();
            new CSG(f, defaultFileType).toSTEP(dest);
            return new CSG(dest, defaultFileType);
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    /**
     * Extrudes the specified polygon.
     * @param dir extrusion direction
     * @param vertices polygon vertices
     * @return extruded polygon CSG
     */
    public static CSG extrude(Vector3d dir, Vector3d... vertices) {
        return extrude(dir, Arrays.asList(vertices));
    }

    /**
     * Extrudes the specified polygon.
     * @param dir extrusion direction
     * @param vertices polygon vertices
     * @return extruded polygon CSG
     */
    public static CSG extrude(Vector3d dir, List<Vector3d> vertices) {

        CSG result = new CSG(defaultFileType);

        String coords = "";

        for (Vector3d v : vertices) {
            coords += "," + v.x() + "," + v.y() + "," + v.z();
        }

        String[] exeArgs = {
                "--create", "extrusion:polygon",
                dir.x() + "," + dir.y() + "," + dir.z() + coords,
                result.getFile().getAbsolutePath()
        };

        int exitValue = VCSG.execute(
                exeArgs
        ).print(null, System.err).getProcess().exitValue();

        if (exitValue != 0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue + ", command: occ-csg " + String.join("",exeArgs));
        }

        return result;
    }

    /**
     * Creates a regular prism CSG with the specified origin, number of corners, radius and height.
     * @param origin origin
     * @param n number of corners
     * @param r radius
     * @param h height
     * @return prism CSG
     */
    public static CSG prism(Vector3d origin, int n, double r, double h) {

        double anglePerStep = 2*Math.PI / n;

        List<Vector3d> vertices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            double x = origin.x() + r * Math.sin(i * anglePerStep);
            double y = origin.y() + r * Math.cos(i * anglePerStep);

            vertices.add(Vector3d.xy(x,y));
        }

        return extrude(Vector3d.z(h), vertices).
                transformed(Transform.unity().translateZ(origin.z()));

    }

    /**
     * Computes and returns the volume of this CSG based on a triangle mesh that approximates the 
     * surface of this CSG.
     * @return volume of this csg
     */
    public double computeVolume() {
        return computeVolume(0.1);
    }

    /**
     * Computes and returns the volume of this CSG based on a triangle mesh that approximates the 
     * surface of this CSG.
     * @param tol tolerance for the mesh approximation (double > 0, smaller values give more accurate results, default is 0.1)
     * @return volume of this csg
     */
    public double computeVolume(double tol) {

        File stlApprox;
        try {
            stlApprox = Files.createTempFile("_vcsg_", ".stl").toFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot compute volume of CSG", ex);
        }

        toSTL(stlApprox, tol);

        STLLoader loader = new STLLoader();

        Mesh mesh;
        try {
            mesh = loader.loadMesh(stlApprox);      

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot compute volume of CSG", ex);
        }

        // compute sum over signed volumes of triangles
        // we use parallel streams for larger meshes
        // see http://chenlab.ece.cornell.edu/Publication/Cha/icip01_Cha.pdf
        Stream<Triangle> triangleStream = IntStream.range(0, mesh.getNumberOfTriangles()).
          mapToObj(triIndex -> mesh.getTriangle(triIndex));
        if(mesh.getNumberOfTriangles()>200) {
            triangleStream = triangleStream.parallel();
        }

        double volume = triangleStream.mapToDouble(tri-> {
            Vector3d p1 = tri.getPoint1();
            Vector3d p2 = tri.getPoint2();
            Vector3d p3 = tri.getPoint3();

            return p1.dot(p2.crossed(p3)) / 6.0;
        }).sum();

        volume = Math.abs(volume);

        return volume;    
    }

//    public CSG transformed(Transform t) {
//        int exitValue = VCSG.execute(
//                "--convert",
//                getFile().getAbsolutePath(),
//                f.getAbsolutePath()
//        ).print(null,System.err).getProcess().exitValue();
//
//        if(exitValue!=0) {
//            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
//        }
//
//        return this;
//    }
}

class Triangle {
    private Vector3d p1;
    private Vector3d p2;
    private Vector3d p3;

    static Triangle newInstance(double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2) {
        Triangle triangle = new Triangle();

        triangle.p1 = Vector3d.xyz(x0, y0, z0);
        triangle.p2 = Vector3d.xyz(x1, y1, z1);
        triangle.p3 = Vector3d.xyz(x2, y2, z2);

        return triangle;
    }

    public Vector3d getPoint1() {
        return p1;
    }
    public Vector3d getPoint2() {
        return p2;
    }
    public Vector3d getPoint3() {
        return p3;
    }
}

class Mesh {

    private float[] vertices;
    private int[] indices;

    static Mesh newInstance(float[] vertices, int[] indices) {
        Mesh mesh = new Mesh();
        mesh.vertices = vertices;
        mesh.indices  = indices;

        return mesh;
    }

    public int getNumberOfTriangles() {
        return indices.length / 3;
    }

    public Triangle getTriangle(int i) {

        int vIndexX0 = i*3 + 0;
        int vIndexY0 = i*3 + 0;
        int vIndexZ0 = i*3 + 0;
        int vIndexX1 = i*3 + 1;
        int vIndexY1 = i*3 + 1;
        int vIndexZ1 = i*3 + 1;
        int vIndexX2 = i*3 + 2;
        int vIndexY2 = i*3 + 2;
        int vIndexZ2 = i*3 + 2;

        return Triangle.newInstance(
            vertices[indices[vIndexX0]*3+0], vertices[indices[vIndexY0]*3+1], vertices[indices[vIndexZ0]*3+2],
            vertices[indices[vIndexX1]*3+0], vertices[indices[vIndexY1]*3+1], vertices[indices[vIndexZ1]*3+2],
            vertices[indices[vIndexX2]*3+0], vertices[indices[vIndexY2]*3+1], vertices[indices[vIndexZ2]*3+2]
        );
    }

    public int getNumberVertices() {
        return vertices.length;
    }
}

/**
 * Very fast STL loader for binary and ASCII STL files.
 */
class STLLoader {

    /**
     * Loads a mesh from the specified STL file (binary & ASCII supported) and deduplicates
     * the vertices after loading.
     *
     * @param file mesh file
     * @return mesh object
     * @throws IOException if an i/o error occurs during loading
     */
    public Mesh loadMesh(File file) throws IOException {

        // parse STL file (binary or ascii)
        List<Vertex> vertexList = parse(file);
        
        return deduplicateTriangleVertices(vertexList);
    }

    /**
     * Deduplicates the specified triangle vertices.
     *
     * @param vertexList vertices to deduplicate
     * @return mesh containing the deduplicated vertices and index list
     */
    public Mesh deduplicateTriangleVertices(List<Vertex> vertexList) {
        // init indices
        for (int i = 0; i < vertexList.size(); i++) {
            vertexList.get(i).index = i;
        }

        // in case of an empty file we just return an empty mesh object
        if (vertexList.isEmpty()) {
            return Mesh.newInstance(new float[0], new int[0]);
        }

        // start deduplication
        Vertex[] sortedVerts = new Vertex[vertexList.size()];
        sortedVerts = vertexList.toArray(sortedVerts);

        // sort vertices:
        // - duplicate vertices will be adjacent to each other
        //   if sortedVerts[i] != sortedVerts[i-1] then we know
        //   that it is unique among all vertices in the list
        // - parallel sort is done via fork-/join
        Arrays.parallelSort(sortedVerts, Vertex::compareVerts);

        // we create the index array (will be filled with indices below)
        int[] indices = new int[vertexList.size()];

        // we add each vertex once and filter out the duplicates
        // note: we use original vertex count as capacity to prevent
        //       unnecessary allocations & copying
        List<Vertex> newVerts = new ArrayList<>(vertexList.size());
        for (Vertex v : sortedVerts) {
            if (newVerts.isEmpty() // we can always add the first vertex (empty list)
                    || !v.equals(newVerts.get(newVerts.size() - 1))) {
                // or if the previous vertex is not equal to this one
                // see 'sort vertices:' above
                newVerts.add(v);
            }
            // set the index to the new location of vertex v
            indices[v.index] = newVerts.size() - 1;
        }

        // create final vertex array (flat float array)
        float[] finalVertices = new float[newVerts.size() * 3];
        for (int i = 0; i < newVerts.size(); i++) {
            finalVertices[i * 3 + 0] = newVerts.get(i).x;
            finalVertices[i * 3 + 1] = newVerts.get(i).y;
            finalVertices[i * 3 + 2] = newVerts.get(i).z;
        }

        // finally return the mesh
        return Mesh.newInstance(finalVertices, indices);
    }

    /**
     * Simple vertex class. This class is only useful/tested
     * in the context of this loader.
     */
    private final static class Vertex {
        float x, y, z;
        int index;

        /**
         * Creates a new vertex.
         * @param x x coord
         * @param y y coord
         * @param z z coord
         */
        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {

            if (!(obj instanceof Vertex)) return false;

            Vertex other = (Vertex) obj;

            // we don't check for numerical equality because
            // in STL files duplicate vertices are exact clones
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public String toString() {
            return "Vertex {" +
                    "index=" + index +
                    ", x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }

        /**
         * Compares the specified vertices.
         * @param v1 first vertex
         * @param v2 second vertex
         * @return {@code -1} for {@code v1 < v2}, {@code 0} for {@code v1 == v2} and {@code +1} for {@code v1 > v2}
         */
        static int compareVerts(Vertex v1, Vertex v2) {
            if (v1.x != v2.x) return Float.compare(v1.x, v2.x);
            else if (v1.y != v2.y) return Float.compare(v1.y, v2.y);
            else if (v1.z != v2.z) return Float.compare(v1.z, v2.z);
            else return 0;
        }
    }

    /**
     * Parses the specified STL file (binary and ASCII STL is supported).
     * @param f file to parse
     * @return list of vertices in this file
     * @throws IOException if an i/o error occurs during parsing
     */
    private List<Vertex> parse(File f) throws IOException {

        // determine if this is a binary or ASCII STL
        // and call either the binary or ascii parsing method

        // check whether the file is an ASCII STL
        if (isASCIISTLFile(f)) {
            return parseAscii(f);
        }

        // the specified is no ASCII STL: we assume binary STL
        int numberOfTriangles = getNumberOfTriangles(f);
        if (isBinarySTLFile(f,numberOfTriangles)) {
            return parseBinary(f, numberOfTriangles);
        }

        throw new IOException("Unknown file format: " + f.getAbsolutePath());
    }

    /**
     * Indicates whether the specified file is an ASCII STL file.
     * @param f file to analyze
     * @return {@code true} if this file is an ASCII STL file; {@code false} otherwise
     * @throws IOException if an io error occurs
     */
    private boolean isASCIISTLFile(File f) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            String[] words = line.trim().split("\\s+");
            return line.indexOf('\0') < 0 && words[0].equalsIgnoreCase("solid");
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Indicates whether the specified file is a binary STL file.
     * @param f file to analyze
     * @return {@code true} if this file is a binary STL file; {@code false} otherwise
     * @throws IOException if an i/o error occurs
     */
    private boolean isBinarySTLFile(File f) throws IOException {
        return ((f.length() - 84) / 50) == getNumberOfTriangles(f);
    }

    /**
     * Indicates whether the specified file is a binary STL file.
     * @param f file to analyze
     * @param numberOfTriangles number of triangles in this file
     * @return {@code true} if this file is a binary STL file; {@code false} otherwise
     * @throws IOException if an i/o error occurs
     */
    private boolean isBinarySTLFile(File f, int numberOfTriangles) throws IOException {
        return ((f.length() - 84) / 50) == numberOfTriangles;
    }

    /**
     * Returns the number of triangles in the specified binary STL file.
     * @param f file to analyze
     * @return the number of triangles in the specified binary STL file
     * @throws IOException if an i/o error occurs
     */
    private int getNumberOfTriangles(File f) throws IOException {
        try(FileInputStream fs = new FileInputStream(f)) {

            // based on ImageJ/Fuji STL loader:
            //
            // bytes 80, 81, 82 and 83 form a little-endian int
            // that contains the number of triangles
            byte[] buffer = new byte[84];
            fs.read(buffer, 0, 84);
            return (int) (((buffer[83] & 0xff) << 24)
                    | ((buffer[82] & 0xff) << 16) | ((buffer[81] & 0xff) << 8) | (buffer[80] & 0xff));
        } catch(IOException ex) {
            throw ex;
        }
    }

    /**
     * Parses the specified ASCII STL file.
     * @param f file to parse
     * @return vertex list
     * @throws IOException if parsing fails
     */
    private List<Vertex> parseAscii(File f) throws IOException {
        List<Vertex> vertices = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] numbers = line.trim().split("\\s+");
                if (numbers[0].equals("vertex")) {
                    float x = Float.parseFloat(numbers[1]);
                    float y = Float.parseFloat(numbers[2]);
                    float z = Float.parseFloat(numbers[3]);

                    Vertex vector3f = new Vertex(x, y, z);
                    vertices.add(vector3f);
                } else if (numbers[0].equals("facet") && numbers[1].equals("normal")) {
                    // for now we ignore the normals
//                    normal.x = Float.parseFloat(numbers[2]);
//                    normal.y = Float.parseFloat(numbers[3]);
//                    normal.z = Float.parseFloat(numbers[4]);
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return vertices;
    }

    /**
     * Parses the specified binary STL file.
     * @param f file to parse
     * @param numTriangles number of triangles to read
     * @return vertex list
     * @throws IOException if parsing fails
     */
    private List<Vertex> parseBinary(File f, int numTriangles) throws IOException {
        // initialize vertex list with the exact number of entries to prevent
        // unnecessary allocations & copying
        List<Vertex> vertices = new ArrayList<>(numTriangles*3);
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f))) {

            // the following code is based on ImageJ/Fuji STL loader
            for (int h = 0; h < 84; h++) {
                fis.read();// skip the header bytes
            }

            // read triangles
            for (int t = 0; t < numTriangles; t++) {
                byte[] tri = new byte[50];
                for (int tb = 0; tb < 50; tb++) {
                    tri[tb] = (byte) fis.read();
                }
//                normal.x = leBytesToFloat(tri[0], tri[1], tri[2], tri[3]);
//                normal.y = leBytesToFloat(tri[4], tri[5], tri[6], tri[7]);
//                normal.z = leBytesToFloat(tri[8], tri[9], tri[10], tri[11]);

                for (int i = 0; i < 3; i++) {
                    final int j = i * 12 + 12;
                    float x = leBytesToFloat(tri[j], tri[j + 1], tri[j + 2],
                            tri[j + 3]);
                    float y = leBytesToFloat(tri[j + 4], tri[j + 5],
                            tri[j + 6], tri[j + 7]);
                    float z = leBytesToFloat(tri[j + 8], tri[j + 9],
                            tri[j + 10], tri[j + 11]);

                    Vertex vector3f = new Vertex(x, y, z);
                    vertices.add(vector3f);
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return vertices;
    }

    private float leBytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        return Float.intBitsToFloat((((b3 & 0xff) << 24) | ((b2 & 0xff) << 16)
                | ((b1 & 0xff) << 8) | (b0 & 0xff)));
    }

}