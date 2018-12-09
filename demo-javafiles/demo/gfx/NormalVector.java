//
//  NormalVector.java
//  Demo
//
//  Created by David Eigen on Wed Aug 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.DemoColor;
import demo.util.M;

/**
 * A normal vector to a surface. Used for lighting.
 */
public class NormalVector extends LightingVector {

    /**
     * Diffuse lighting scale factor. Colors should be multiplied by this.
     */
    private double   lightscale;

    private double specular;

    /**
     * Creates a normal vector.
     * @param v the coordinates. Can be whatever dimension -- automatically converted.
     */
    public NormalVector(double[] v) {
        super(v);
        unsetLighting();
    }

    public LightingVector copy() {
        return new NormalVector(untransformedCoords);
    }

    public DemoColor applyLighting(DemoColor c) {
        DemoColor d = c.scale(lightscale);
        /*
        d.red += specular;
        d.green += specular;
        d.blue += specular;
         */
        return d.clamp();
 
    }
    
    public void transform(Transformation t) {
        t.mat_inv_tr.transform(untransformedCoords, coords);
        coords[3] = 0;
    }

    public void setLighting(double[] dir) {
        final double[] N = M.normalize(coords);
        this.lightscale = 0.15 + 0.85 * Math.abs(M.dot(N, dir));
/*
        double spec = M.dot(M.sub(M.mult(2 * M.dot(dir, N), N),
                                  dir),
                            M.vector(0,0,1));
        if (spec <= 0)
            this.specular = 0;
        else
            this.specular = M.min(1,Math.pow(spec, 20));
 */
    }

    public void unsetLighting() {
        this.lightscale = 1;
        this.specular = 0;
    }


}
