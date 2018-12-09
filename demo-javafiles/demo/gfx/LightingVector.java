//
//  LightingVector.java
//  Demo
//
//  Created by David Eigen on Wed Aug 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.M;
import demo.util.DemoColor;

public abstract class LightingVector {
    
    /**
     * Coordinates of this vector, in transformed coords.
     * A 4-element array [x,y,z,0].
     */
    public double[] coords;

    /**
     * Coordinates of this vector, untransformed.
     * A 4-element array [x,y,z,0].
     */
    public double[] untransformedCoords;
    

    protected LightingVector(double[] v) {
        this.coords = M.vector(v);
        this.untransformedCoords = M.vector(v);
    }

    /**
     * @return an copy of this lighting vector (w/ different coords arrays, etc)
     */
    public abstract LightingVector copy();

    /**
     * Applies lighting to the given color. The given color is left unchanged.
     * @return the color after lighting is applied
     */
    public abstract DemoColor applyLighting(DemoColor c);

    /**
     * Transforms this lighting vector by the given transformation.
     */
    public abstract void transform(Transformation t);

    /**
     * Sets up this vector so it is ready to be applied to colors.
     * @param dir the direction of the light. Must be a unit vector.
     */
    public abstract void setLighting(double[] dir);

    /**
     * Sets this vector so that lighting is effectively not applied.
     */
    public abstract void unsetLighting();
    

}
