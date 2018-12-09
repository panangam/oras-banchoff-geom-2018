package demo.gfx;

import demo.gfx.drawable.Drawable3D;

/**
 * A PointSortable is a 3-D point that contains pointers to any Drawable objects that use this point.
 * It is produced by a Plot. It can be used with the sorting (Painter's) algorithm. In the
 * algorithm, only the points need to be sorted by z-coordinate. Then all drawable objects can
 * be drawn in the order of their minimum z-coordinate simply by drawing the objects pointed
 * to by the points in the order that the sorted points are in.
 * 
 * @author deigen
 */
public class PointSortable extends Point{

    public  java.util .Vector objects;

    
    /**
     * Makes a new PointSortable.
     * @param coords the coordinates of the point (dimension 2 or 3. If dimension < 3, then z-coord = 0)
     * @param extraZ value added to z of point after it is transformed (used for plot layering)
     * @param initialCapacity the initial capacity of the stucture storing drawable objects
     */
    public  PointSortable( double[] coords, int initialCapacity, double extraZ ) {
        super( coords, extraZ );
            if ( dimension < 3 ) {
                this .dimension = 3;
                this .coords = new double[]{coords[0], coords[1], 0};
                this .untransformedCoords = new double[]{coords[0], coords[1], 0};
            }
            else if ( dimension > 3 ) {
                this .dimension = 3;
                this .coords = new double[]{coords[0], coords[1], coords[2]};
                this .untransformedCoords = new double[]{coords[0], coords[1], coords[2]};
            }
            objects = new java.util .Vector( initialCapacity );
        }

    /**
     * Makes a new PointSortable.
     * @param vect the coordinates of the point (dimension 2 or 3. If dimension < 3, then z-coord = 0)
     *        This must be a ValueVector of dimension 2 or 3 with all scalar components
     * @param extraZ value added to z of point after it is transformed (used for plot layering)
     * @param initialCapacity the initial capacity of the stucture storing drawable objects
     */
    public  PointSortable( mathbuild.value.ValueVector vect, int initialCapacity, double extraZ ) {
        this(vect.doubleVals(), initialCapacity, extraZ);
    }

    /**
     * Makes a new PointSortable.
     * Initial capacity is set to 1, extraZ set to 0.
     * @param coords the coordinates of the point (dimension 2 or 3. If dimension < 3, then z-coord = 0)
     */
    public  PointSortable( double[] coords ) {
        this(coords, 1, 0);
    }

    /**
     * Adds a drawable object to the drawable objects that use this point.
     * @param obj the drawable object
     */
    public  void addObject( Drawable3D obj ) {
        objects .addElement( obj );
    }

    /**
     * the drawable objects that use this point
     * Modification of this vector will directly set which objects use this point
     */
    public  java.util.Vector objects() {
        return objects;
    }

    /**
     * @return true if any of the coordinates of this point are not a number or infinite
     */
    public boolean isNaN() {
        return  Double.isInfinite(coords[0]) ||
                Double.isInfinite(coords[1]) ||
                Double.isInfinite(coords[2]) ||
                Double.isNaN(coords[0]) ||
                Double.isNaN(coords[1]) ||
                Double.isNaN(coords[2]);
    }

    /**
     * Interpolates between two points. The point returned is NOT already transformed -- its
     * transformed coords are the same as its untransformed coords.
     * @param p one of the endpoints of the line to interpolate on
     * @param q the other endpoint
     * @param c the coefficient for interpolation
     * @param initialCapacity the initial capacty of the structure storing drawable objects
     * @return the interpolated point, c*p + (1-c)*q
     */
    public static PointSortable interpolate(PointSortable p, PointSortable q, double c, int initialCapacity) {
        double[] coords = new double[3];
        double d = 1 - c;
        coords[0] = c*p.untransformedCoords[0] + d*q.untransformedCoords[0];
        coords[1] = c*p.untransformedCoords[1] + d*q.untransformedCoords[1];
        coords[2] = c*p.untransformedCoords[2] + d*q.untransformedCoords[2];
        double extraZ = c*p.extraZ + d*q.extraZ;
        return new PointSortable(coords, initialCapacity, extraZ);
    }



}


