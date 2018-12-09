//
//  RayIntersection.java
//  Demo
//
//  Created by David Eigen on Sat Mar 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.M;

/**
 * Holds information about the intersection with a ray.
 */
public class RayIntersection {

    /** whether this intersection is valid */
    public boolean valid = false;

    /** the ray that was intersected with */
    public Ray ray = null;

    /** the object that the ray hit */
    public Object obj = null;

    /** the t at which the ray hit (the hit pt is approximately p = ray.p + t * ray.v) */
    public double t = Double.POSITIVE_INFINITY;

    /** the point at which the ray hit (the hit pt is approximately p = ray.p + t * ray.v) */
    public double[] p = new double[4];

    
    public RayIntersection() {
    }

    /**
     * Sets the values in this RayIntersection to the given values.
     */
    public void set(Ray ray, Object obj, double t, double[] p) {
        this.valid = true;
        this.ray = ray;
        this.obj = obj;
        this.t = t;
        this.p = M.point(p);
    }

    /**
     * Copies the data from the given intersection into this intersection.
     */
    public void set(RayIntersection i) {
        this.valid = i.valid;
        this.ray = i.ray;
        this.obj = i.obj;
        this.t = i.t;
        this.p = new double[]{i.p[0], i.p[1], i.p[2], i.p[3]};
    }

}
