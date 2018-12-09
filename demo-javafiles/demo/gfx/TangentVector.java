//
//  TangentVector.java
//  Demo
//
//  Created by David Eigen on Wed Aug 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.DemoColor;
import demo.util.M;

/**
 * A tangent vector to a curve. Used for lighting.
 */
public class TangentVector extends LightingVector {

    /**
     * Lighting scale factor. Colors should be multiplied by this.
     */
    private double   lightscale;

    /**
     * Creates a tangent vector.
     * @param v the coordinates. Can be whatever dimension -- automatically converted.
     */
    public TangentVector(double[] v) {
        super(v);
        unsetLighting();
    }

    public LightingVector copy() {
        return new TangentVector(untransformedCoords);
    }

    public DemoColor applyLighting(DemoColor c) {
        return c.scale(lightscale);
    }

    public void transform(Transformation t) {
        t.mat.transform(untransformedCoords, coords);
    }

    public void setLighting(double[] dir) {
        double cosine = M.dot(M.normalize(coords), dir);
        double sine = Math.sqrt(1 - cosine*cosine);
        this.lightscale = 0.15 + 0.85 * sine;
    }

    public void unsetLighting() {
        this.lightscale = 1;
    }

}
