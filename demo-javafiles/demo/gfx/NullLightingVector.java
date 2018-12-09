//
//  NullLightingVector.java
//  Demo
//
//  Created by David Eigen on Wed Aug 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.DemoColor;

import demo.util.M;

/**
 * A null lighting vector does not apply any lighting.
 * Its lightscale is always equal to 1.
 */
public class NullLightingVector extends LightingVector {

    /**
     * Creates a null lighting vector.
     */
    public NullLightingVector() {
        super(M.vector(0,0,0));
    }

    public LightingVector copy() {
        return new NullLightingVector();
    }

    public DemoColor applyLighting(DemoColor c) {
        return c;
    }
    
    public void transform(Transformation t) { }

    public void setLighting(double[] dir) { }

    public void unsetLighting() { }

}
