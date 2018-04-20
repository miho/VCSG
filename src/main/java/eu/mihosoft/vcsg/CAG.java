//package eu.mihosoft.vcsg;
//
//import eu.mihosoft.vvecmath.Vector3d;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Arrays;
//import java.util.List;
//
//public class CAG {
//    private File file;
//
//    private CAG() {
//        try {
//            file = Files.createTempFile("_vcsg_", ".brep").toFile();
//        } catch (IOException e) {
//            throw new RuntimeException("cannot create cag object because tmp file cannot be created", e);
//        }
//    }
//
//    private CAG(File file) {
//        this.file = file;
//    }
//
//    File getFile() {
//        return file;
//    }
//
////    public static CAG rect(double x1, double y1, double x2, double y2) {
////        CAG result = new CAG();
////
////        String coords = x1+","+y1+","+x2+","+y2;
////
////        int exitValue = VCSG.execute(
////                "--create", "2d:rect", coords,
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
////
////    public static CAG circle(double x1, double y1, double r) {
////        CAG result = new CAG();
////
////        String coords = x1+","+y1+","+r;
////
////        int exitValue = VCSG.execute(
////                "--create", "2d:circle", coords,
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
//
//    public static CAG polygon(Vector3d... vertices) {
//        CAG result = new CAG();
//
//        String coords = "";
//
//        for (Vector3d v : vertices) {
//            coords += v.x()+","+v.y()+(coords.length()==0?",":"");
//        }
//
//        int exitValue = VCSG.execute(
//                "--create", "2d:polygon", coords,
//                result.getFile().getAbsolutePath()
//        ).print().getProcess().exitValue();
//
//        if(exitValue!=0) {
//            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
//        }
//
//        return result;
//    }
//
//    public CAG clone() {
//        return new CAG(getFile());
//    }
//
////    public CAG difference(CAG... others) {
////
////        CAG result = new CAG();
////        CAG union = new CAG().union(others);
////
////        int exitValue = VCSG.execute(
////                "--csg", "difference",
////                getFile().getAbsolutePath(),
////                union.getFile().getAbsolutePath(),
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
////
////    public CAG difference(CAG other) {
////
////        CAG result = new CAG();
////
////        int exitValue = VCSG.execute(
////                "--csg", "difference",
////                getFile().getAbsolutePath(),
////                other.getFile().getAbsolutePath(),
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
////
////    public CAG union(CAG... others) {
////        return union(Arrays.asList(others));
////    }
////
////    public CAG union(List<CAG> others) {
////
////        CAG result = this.clone();
////        for (CAG csg : others) {
////            result = csg.union(csg);
////        }
////
////        return result;
////    }
////
////    public CAG union(CAG other) {
////
////        CAG result = new CAG();
////
////        int exitValue = VCSG.execute(
////                "--csg", "union",
////                getFile().getAbsolutePath(),
////                other.getFile().getAbsolutePath(),
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
////
////    public CAG intersect(CAG other) {
////
////        CAG result = new CAG();
////
////        int exitValue = VCSG.execute(
////                "--csg", "intersect",
////                getFile().getAbsolutePath(),
////                other.getFile().getAbsolutePath(),
////                result.getFile().getAbsolutePath()
////        ).print().getProcess().exitValue();
////
////        if(exitValue!=0) {
////            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
////        }
////
////        return result;
////    }
//
//    public CSG extrude(Vector3d dir) {
//        CSG result = new CSG();
//
//        int exitValue = VCSG.execute(
//                "--create", "extrusion:file",
//                dir.x()+","+dir.y()+","+dir.z(),
//                getFile().getAbsolutePath(),
//                result.getFile().getAbsolutePath()
//        ).print().getProcess().exitValue();
//
//        if(exitValue!=0) {
//            throw new RuntimeException("Error during CSG command, exit value: " + exitValue);
//        }
//
//        return result;
//    }
//}
