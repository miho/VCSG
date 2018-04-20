package eu.mihosoft.vcsg;

import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public final class CSG {
    private File file;

    CSG() {
        try {
            file = Files.createTempFile("_vcsg_", ".brep").toFile();
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    private CSG(File f) {
        this.file = f;
    }

    public CSG clone() {
        return new CSG(getFile());
    }

    public CSG difference(CSG... others) {

        CSG result = new CSG();
        CSG union = new CSG().union(others);

        int exitValue = VCSG.execute(
                "--csg", "difference",
                getFile().getAbsolutePath(),
                union.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public CSG difference(CSG other) {

        CSG result = new CSG();

        int exitValue = VCSG.execute(
                "--csg", "difference",
                getFile().getAbsolutePath(),
                other.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public CSG union(CSG... others) {
        return union(Arrays.asList(others));
    }

    public CSG union(List<CSG> others) {

        CSG result = this.clone();
        for (CSG csg : others) {
            result = csg.union(csg);
        }

        return result;
    }

    public CSG union(CSG other) {

        CSG result = new CSG();

        int exitValue = VCSG.execute(
                "--csg", "union",
                getFile().getAbsolutePath(),
                other.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public CSG intersect(CSG other) {

        CSG result = new CSG();

        int exitValue = VCSG.execute(
                "--csg", "intersect",
                getFile().getAbsolutePath(),
                other.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    File getFile() {
        return file;
    }

    public List<CSG> split() {

        try {
            File tmpDir = Files.createTempDirectory("_vcsg").toFile();

            File shapeF = new File(tmpDir, "shape.brep");

            Files.copy(getFile().toPath(), shapeF.toPath());

            int exitValue = VCSG.execute(
                    tmpDir,
                    "--edit", "split-shape",
                    shapeF.getAbsolutePath(), "brep"
            ).print(null,System.err).getProcess().exitValue();

            if(exitValue!=0) {
                throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
            }

            return Files.list(tmpDir.toPath()).filter(f->!f.equals(shapeF.toPath())).map(f->new CSG(f.toFile())).
                    collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            new RuntimeException("Cannot create tmp folder", e);
        }

        return Collections.emptyList();
    }

    public CSG round(double radius) {
        CSG result = new CSG();

        int exitValue = VCSG.execute(
                "--edit", "round-edges",
                ""+radius,
                getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public Bounds getBounds() {
        StringPrintStream ps = new StringPrintStream();

        int exitValue = VCSG.execute(
                "--bounds",
                getFile().getAbsolutePath()
        ).print(ps,System.err).getProcess().exitValue();

        System.out.println(ps);

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        String output = ps.toString();

        String[] lines = output.split("\\R");

        for(int i = 0; i < lines.length;i++) {
            String l = lines[i].trim();
            if(l.contains("-> bounds")) {
                String[] boundStrings = l.replace("-> bounds:","").trim().split(",");

                if(boundStrings.length!=6) {
                    throw new RuntimeException("Wrong number of bound values, expected 6, got " + boundStrings.length);
                }

                double[] bounds = new double[boundStrings.length];

                for(int j = 0; j < bounds.length;j++) {
                    try {
                        bounds[j] = Double.parseDouble(boundStrings[j]);
                    } catch(NumberFormatException ex) {
                        throw new RuntimeException("Cannot convert bounds entry " + j, ex);
                    }
                }

                return new Bounds(
                        Vector3d.xyz(bounds[0],bounds[1],bounds[2]),
                        Vector3d.xyz(bounds[3], bounds[4], bounds[5])
                );
            }
        }

        throw new RuntimeException("Cannot compute bounds");
    }

    public static CSG box(Vector3d min, Vector3d max) {
        CSG result = new CSG();

        String coords = min.x()+","+min.y()+","+min.z()+","+max.x()+","+max.y()+","+max.z();

        int exitValue = VCSG.execute(
                "--create", "box", coords,
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public static CSG sphere(Vector3d origin, double radius) {
        CSG result = new CSG();

        String coords = origin.x()+","+origin.y()+","+origin.z()+","+radius;

        int exitValue = VCSG.execute(
                "--create", "sphere", coords,
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public static CSG cylinder(Vector3d origin, double radius, double height) {
        CSG result = new CSG();

        String coords = origin.x()+","+origin.y()+","+origin.z()+","+radius+","+height;

        int exitValue = VCSG.execute(
                "--create", "cyl", coords,
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public static CSG cone(Vector3d origin, double r1, double r2, double height) {
        CSG result = new CSG();

        String coords = origin.x()+","+origin.y()+","+origin.z()+","+r1+","+r2+","+height;

        int exitValue = VCSG.execute(
                "--create", "cyl", coords,
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public CSG transformed(Transform transform) {

        CSG result = new CSG();

        double[] v = transform.to();

        String values =
                v[0]+","+v[1]+","+v[2]+","+v[3]+","+
                v[4]+","+v[5]+","+v[6]+","+v[7]+","+
                v[8]+","+v[9]+","+v[10]+","+v[11];

        int exitValue = VCSG.execute(
                "--transform", "matrix", values,
                this.getFile().getAbsolutePath(),
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
    }

    public CSG toSTEP(File f) {

        if(!f.getAbsolutePath().toLowerCase().endsWith(".stp")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stp'");
        }

        int exitValue = VCSG.execute(
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return this;
    }

    public CSG toBREP(File f) {

        if(!f.getAbsolutePath().toLowerCase().endsWith(".brep")) {
            throw new RuntimeException("Cannot convert file. File must end with '.brep'");
        }

        int exitValue = VCSG.execute(
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return this;
    }

    public CSG toSTL(File f, double tol) {

        if(!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        int exitValue = VCSG.execute(
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath(),
                ""+tol
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return this;
    }

    public CSG toSTL(File f) {

        if(!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        int exitValue = VCSG.execute(
                "--convert",
                getFile().getAbsolutePath(),
                f.getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return this;
    }

    public static CSG fromBREP(File f) {
        if(!f.getAbsolutePath().toLowerCase().endsWith(".brep")) {
            throw new RuntimeException("Cannot convert file. File must end with '.brep'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", ".brep").toFile();
            new CSG(f).toBREP(dest);
            return new CSG(dest);
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    public static CSG fromSTEP(File f) {
        if(!f.getAbsolutePath().toLowerCase().endsWith(".stp")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stp'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", ".brep").toFile();
            new CSG(f).toBREP(dest);
            return new CSG(dest);
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }

    public static CSG fromSTL(File f) {
        if(!f.getAbsolutePath().toLowerCase().endsWith(".stl")) {
            throw new RuntimeException("Cannot convert file. File must end with '.stl'");
        }

        try {
            File dest = Files.createTempFile("_vcsg_", ".brep").toFile();
            new CSG(f).toBREP(dest);
            return new CSG(dest);
        } catch (IOException e) {
            throw new RuntimeException("cannot create csg object because tmp file cannot be created", e);
        }
    }


    public static CSG extrude(Vector3d dir, Vector3d... vertices) {
        return extrude(dir,Arrays.asList(vertices));
    }

    public static CSG extrude(Vector3d dir, List<Vector3d> vertices) {

        CSG result = new CSG();

        String coords = "";

        for (Vector3d v : vertices) {
            coords += ","+v.x()+","+v.y()+","+v.z();
        }

        int exitValue = VCSG.execute(
                "--create", "extrusion:polygon",
                dir.x()+","+dir.y()+","+dir.z()+coords,
                result.getFile().getAbsolutePath()
        ).print(null,System.err).getProcess().exitValue();

        if(exitValue!=0) {
            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
        }

        return result;
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
