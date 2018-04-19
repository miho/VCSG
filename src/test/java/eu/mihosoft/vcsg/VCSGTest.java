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

import eu.mihosoft.vvecmath.Vector3d;
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
        
        //VCSG.execute().print();

        Vector3d min = Vector3d.xyz(-5,-5,-5);
        Vector3d max = Vector3d.xyz(5,5,5);

        CSG box = CSG.box(min, max);
        CSG sphere = CSG.sphere(Vector3d.xyz(0,0,0), 5.25);

        CSG cut = box.difference(sphere).round(0.25);

        cut.toSTL(new File("/Users/miho/Downloads/cut.stl"));
        cut.toSTEP(new File("/Users/miho/Downloads/cut.stp"));

        box.toSTL(new File("/Users/miho/Downloads/box.stl"));

//        Bounds originalBounds = new Bounds(min,max);
//        Bounds computedBounds = cut.getBounds();
//
//        Assert.assertEquals(originalBounds, computedBounds);
    }
}


// VOLUME & SURFACE
//
// GProp_GProps System;
// BRepGProp::LinearProperties(shape, System);
//        BRepGProp::SurfaceProperties(shape, System);
//        BRepGProp::VolumeProperties(shape, System);
//        System.Mass();