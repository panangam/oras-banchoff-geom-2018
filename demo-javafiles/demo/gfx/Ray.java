//
//  Ray.java
//  Demo
//
//  Created by David Eigen on Sat Mar 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

import demo.util.M;

public class Ray {

    /** p is the point the ray starts from */
    public double[] p;
    /** v is the direction of the ray (normalized) */
    public double[] v;
    /** q = p + v */
    public double[] q;

    /** tolerance for this ray when hitting a thin object (pt, line, etc)
     *  corresponds to the "thickness" of the ray. If the ray is w/in tol of a pt,
     *  it should be an intersection. In other words, tol is how far apart (in
     *  world-space) a point can be from the ray in order for it to be considered a hit
     */
    public double tol;
    
    /** transf is a transformation that transforms space st p -> origin, v -> (0,0,1,0) */
    public Matrix4D transf;
    /** inverse of transf */
    public Matrix4D transf_inv;
    /** p in this transformed space */
    public static final double[] tp = new double[]{0,0,0,1};
    /** v in this transformed space */
    public static final double[] tv = new double[]{0,0,1,0};
    
    public Ray() {
        p = v = null;
    }

    public Ray(double[] pt, double[] dir) {
        this(pt, dir, 0.1);
    }
    
    public Ray(double[] pt, double[] dir, double tolerance) {
        this.tol = tolerance;
        this.p = new double[4];
        this.v = new double[4];
        int len = pt.length < dir.length ? pt.length : dir.length;
        for (int i = 0; i < len; ++i) {
            p[i] = pt[i];
            v[i] = dir[i];
        }
        if (len == 3) {
            p[3] = 1;
            v[3] = 0;
        }
        v = M.normalize(v);
        q = M.add(p, v);
        transf_inv = M.mat_zaxis_transf(v, p);
        transf = transf_inv.inverse();
    }

}
