/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcsg;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import eu.mihosoft.vvecmath.Vectors3d;
import org.junit.Assert;
import org.junit.Test;

import eu.mihosoft.vcsg.util.*;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VCSGTest {

    @Test
    public void executeExampleScriptTest() throws IOException {
        
//        //VCSG.execute().print();
//        //if(true) return;
//
        double w = 10;
        double h = 10;

        Vector3d min = Vector3d.xyz(-w/2.0,-h/2.0,-0.3);
        Vector3d max = Vector3d.xyz(w/2.0,h/2.0,0.3);

        double r = 0.4;

        CSG box = CSG.box(min, max);
        CSG sphere = CSG.sphere(Vector3d.xyz(0,0,0), r);

        CSG cut = box;
        int count = 0;
        for(double i = 0; i < w; i++) {
            for(double j = 0; j < h; j++) {
                System.out.println("csg operation " + ++count);
                CSG s = sphere.transformed(Transform.unity().
                        translate(i-w/2+r*1.25,j-h/2+r*1.25,0));
                cut = cut.difference(s);
            }
        }
        System.out.println("before");
        cut = cut.round(0.05);
        System.out.println("after");

        cut.toSTL(new File("/Users/miho/Downloads/cut.stl"),0.1);
        cut.toSTEP(new File("/Users/miho/Downloads/cut.stp"));

        CSG transformed = CSG.fromSTEP(new File("/Users/miho/Downloads/cut.stp")).
                transformed(Transform.unity().scale(0.5, 1.0,1.0)).
                transformed(Transform.unity().translateX(12)).
                transformed(Transform.unity().rotX(45));

        transformed.toSTL(new File("/Users/miho/Downloads/trans.stl"),0.1);

        CSG.extrude(Vector3d.z(1),Vectors3d.xy(0,0,10,0,10,4,8,4,8,6,10,6,10,10,0,10)).toSTL(
                new File("/Users/miho/Downloads/extrude.stl")
        );

    }
}


// VOLUME & SURFACE
//
// GProp_GProps System;
// BRepGProp::LinearProperties(shape, System);
//        BRepGProp::SurfaceProperties(shape, System);
//        BRepGProp::VolumeProperties(shape, System);
//        System.Mass();