//
//  M.java
//  Demo
//
//  Created by David Eigen on Thu Jul 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.util;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.gfx.Point;
import demo.gfx.Matrix4D;

/**
 * Math operations. Does vector math with double[] arrays representing vectors (or pts).
 * All operations assume compatable lengths. If lengths of arguments are not right,
 * an error will occur. The user of this class should ensure length compatabilities.
 * The "M" is for "Math".
 */
public final class M {

    public static final double EPSILON = 1e-10;

	/**
	 * Constants for orientation.
     */
    public static final int COLINEAR = 0, CW = -1, CCW = 1;

	
    /** makes a double[4] point from a double[any dim] */
    public static double[] point(double[] p) {
        return new double[]{p[0], p[1], p.length == 2 ? 0 : p[2], 1};
    }

    /** makes a double[4] vector from a double[any dim] */
    public static double[] vector(double[] v) {
        return new double[]{v[0], v[1], v.length == 2 ? 0 : v[2], 0};
    }

    /** makes a double[4] point from a ValueVector */
    public static double[] point(ValueVector pv) {
        double[] p = pv.doubleVals();
        return new double[]{p[0], p[1], p.length == 2 ? 0 : p[2], 1};
    }

    /** makes a double[4] vector from a ValueVector */
    public static double[] vector(ValueVector vv) {
        double[] v = vv.doubleVals();
        return new double[]{v[0], v[1], v.length == 2 ? 0 : v[2], 0};
    }

    /** makes a double[4] point from a Point (using untranformedCoords) */
    public static double[] point(Point pp) {
        double[] p = pp.untransformedCoords;
        return new double[]{p[0], p[1], p.length == 2 ? 0 : p[2], 1};
    }

    /** makes a double[4] point from three doubles */
    public static double[] point(double x, double y, double z) {
        return new double[]{x,y,z,1};
    }

    /** makes a double[4] vector from three doubles */
    public static double[] vector(double x, double y, double z) {
        return new double[]{x,y,z,0};
    }

    /** makes string representation of a double[] */
    public static String str(double[] x) {
        if (x == null) return "<null>";
        String str = "<";
        for (int i = 0; i < x.length; ++i)
            str += x[i] + (i < x.length-1 ? ", " : "");
        return str + ">";
    }

    public static double max(double a, double b) {
        return a > b ? a : b;
    }

    public static double min(double a, double b) {
        return a < b ? a : b;
    }

    public static boolean isNaN(double x) {
        return Double.isNaN(x) || Double.isInfinite(x);
    }

    public static boolean isNaN(double[] x) {
        for (int i = 0; i < x.length; ++i)
            if (isNaN(x[i]))
                return true;
        return false;
    }
    
    public static double dot(double[] a, double[] b) {
        double dot = 0;
        for (int i = 0; i < a.length; ++i)
            dot += a[i]*b[i];
        return dot;
    }

    public static double[] cross(double[] a, double[] b) {
        return new double[]{
            a[1]*b[2] - b[1]*a[2],
            b[0]*a[2] - a[0]*b[2],
            a[0]*b[1] - b[0]*a[1],
            0
        };
    }

    public static double length(double[] v) {
        return Math.sqrt(dot(v,v));
    }

    public static double[] normalize(double[] v) {
        return mult(v, 1.0/length(v));
    }

    public static double[] neg(double[] v) {
        double[] neg = new double[v.length];
        for (int i = 0; i < neg.length; ++i)
            neg[i] = -v[i];
        return neg;
    }

    public static double[] add(double[] a, double[] b) {
        double[] v = new double[a.length];
        for (int i = 0; i < v.length; ++i)
            v[i] = a[i] + b[i];
        return v;
    }

    public static double[] add(double[] a, double[] b, double[] c) {
        double[] v = new double[a.length];
        for (int i = 0; i < v.length; ++i)
            v[i] = a[i] + b[i] + c[i];
        return v;
    }

    public static double[] add(double[] a, double[] b, double[] c, double[] d) {
        double[] v = new double[a.length];
        for (int i = 0; i < v.length; ++i)
            v[i] = a[i] + b[i] + c[i] + d[i];
        return v;
    }

    public static double[] sub(double[] a, double[] b) {
        double[] v = new double[a.length];
        for (int i = 0; i < v.length; ++i)
            v[i] = a[i] - b[i];
        return v;
    }

    public static double[] mult(double a, double[] v) {
        double[] p = new double[v.length];
        for (int i = 0; i < p.length; ++i)
            p[i] = a * v[i];
        return p;
    }

    public static double[] mult(double[] v, double a) {
        return mult(a, v);
    }

    public static double[] mult(Matrix4D m, double[] x) {
        return m.transform(x);
    }

    public static boolean aboutEqual(double a, double b) {
        return Math.abs(a-b) <= EPSILON;
    }

    public static boolean close(double a, double b) {
        return Math.abs(a-b) <= EPSILON;
    }    

    public static boolean equal(double a, double b) {
        return a == b;
    }

    public static boolean equal(double[] a, double[] b) {
        for (int i = 0; i < a.length; ++i)
            if (a[i] != b[i])
                return false;
        return true;
    }

    public static boolean aboutEqual(double[] a, double[] b) {
        for (int i = 0; i < a.length; ++i)
            if (Math.abs(a[i] - b[i]) > EPSILON)
                return false;
        return true;
    }

    public static boolean close(double[] a, double[] b) {
        for (int i = 0; i < a.length; ++i)
            if (Math.abs(a[i] - b[i]) > EPSILON)
                return false;
        return true;
    }

    /**
     * Creates an orthonormal matrix that sends the z axis to v, and the origin to p.
     * That is, this makes an affine transformation st the vector (0,0,1,0) maps to v,
     * and the point (0,0,0,1) maps to p. Note: v should be normalized.
     */
    public static Matrix4D mat_zaxis_transf(double[] v, double[] p) {
        double[] x = (Math.abs(v[0]) < 0.1 && Math.abs(v[1]) < 0.1) ? vector(1,0,0) : vector(0,0,1);
        double[] y = normalize(cross(v, x));
        x = cross(y, v);
        return new Matrix4D(x[0], y[0], v[0], p[0],
                            x[1], y[1], v[1], p[1],
                            x[2], y[2], v[2], p[2],
                               0,    0,    0,    1);
    }
	
	/**
	 * Computes whether the given points are colinear.
	 * The points may be any dimension.
	 */
	public static boolean colinear(double[] a, double[] b, double[] c) {
		double[] v = sub(b,a), w = sub(c,a);
		int i = 0;
		while (i < w.length && w[i] == 0) i++;
		if (i == w.length) return true; // w==0
		return aboutEqual(v, mult(v[i]/w[i], w));
	}

    /**
     * Computes the orientation of points a,b,c, in the xy plane.
     * The x and y coords of the pts are used, and z and w coords are ignored (if they exist).
     * Returns: -1 for CW, 0 for collinear, 1 for CCW
     */
    public static int orientation(double[] a, double[] b, double[] c) {
        int orientation; // the orientation (to be calculated and returned)
                         // test if the first segment's slope is undefined
        if ( b[0] == a[0] ) {
            // the slope of the segment connecting a and b is undefined
            if ( b[1] == a[1] )
                return COLINEAR;
            if ( c[0] > b[0] )
                orientation = CW;
            else if ( c[0] < b[0] )
                orientation = CCW;
            else
                orientation = COLINEAR;
            // if the y-coord of b < the y-coord of a then switch the orientation
            if ( b[1] > a[1] )
                orientation = -orientation;
            return orientation;
        }
        // test if the second segment's slope is undefined
        if ( c[0] == b[0] ) {
            if ( c[1] == b[1] )
                return COLINEAR;
            if ( a[0] > b[0] )
                orientation = CW;
            else if ( a[0] < b[0] )
                orientation = CCW;
            else
                orientation = COLINEAR;
            // if the y-coord of c < the y-coord of b then switch the orientation
            if ( c[1] > b[1] )
                orientation = -orientation;
            return orientation;
        }
        // neither slope is undefined, so make the calculation:
        double slope_ab = ((double) (a[1] - b[1])) / (double) (a[0] - b[0]);
        double slope_bc = ((double) (b[1] - c[1])) / (double) (b[0] - c[0]);
        if ( slope_ab < slope_bc )
            orientation = CW;
        else if ( slope_ab > slope_bc )
            orientation = CCW;
        else
            orientation = COLINEAR;
        // reverse the orientation if necessary
        if ( b[0] < a[0] )
            orientation = -orientation;
        if ( c[0] < b[0] )
            orientation = -orientation;
        return orientation;
    }

    
	public static double dot(ValueVector a, ValueVector b) {
		return  dot(a.doubleVals(), b.doubleVals()) ;
	}

	public static ValueVector cross(ValueVector a, ValueVector b) {
		return new ValueVector( cross(a.doubleVals(), b.doubleVals()) );
	}

	public static ValueVector add(ValueVector a, ValueVector b) {
		return new ValueVector( add(a.doubleVals(), b.doubleVals()) );
	}

	public static ValueVector sub(ValueVector a, ValueVector b) {
		return new ValueVector( sub(a.doubleVals(), b.doubleVals()) );
	}

	public static ValueVector mult(double a, ValueVector v) {
		return new ValueVector( mult(a, v.doubleVals()) );
	}

	public static ValueVector mult(ValueVector v, double a) {
		return new ValueVector( mult(v.doubleVals(), a) );
	}

	public static boolean equal(ValueVector a, ValueVector b) {
		return  equal(a.doubleVals(), b.doubleVals()) ;
	}

	public static boolean aboutEqual(ValueVector a, ValueVector b) {
		return  aboutEqual(a.doubleVals(), b.doubleVals()) ;
	}

	public static double length(ValueVector v) {
		return  length(v.doubleVals()) ;
	}

	public static ValueVector normalize(ValueVector v) {
		return new ValueVector( normalize(v.doubleVals()) );
	}

	public static ValueVector neg(ValueVector v) {
		return new ValueVector( neg(v.doubleVals()) );
	}


}