package demo.gfx;

/**
 * A point in n-space. In this program, the PointSortable subclass is usually instantiated, and not
 * the Point class itself.
 * Contains functionality for transforming, and more.
 *
 * @author deigen
 */
public class Point {

    protected  int dimension;

    /**
     * the transformed coordinates of this point
     */
    public  double[] coords;

    /**
     * the untransformed coordinates of this point
     */
    public double[] untransformedCoords;

    /**
     * extra amount added to z coordinate after transformation is applied
     * used to "layer" plots
     */
    public double extraZ = 0;
    
    
    
    /**
     * @return the dimension of this point
     */
    public  int dimension() {
        return dimension;
    }



    

    /**
     * Creates a point with the given coordinates.
     * Note the actual doube[] pointer is used, not the values in the array.
     * @param coords the coordinates
     * @param extraZ the value to add to the z coordinate of this Point
     *        after transformations are applied
     */
    public  Point( double[] coords, double extraZ ) {
        super();

            this .dimension = coords .length;
            this .untransformedCoords = coords;
            this .coords = new double[coords.length];
            this .extraZ = extraZ;
            System.arraycopy(coords, 0, this.coords, 0, coords.length);
        }

    /**
     * @return the coordinates of this point
     */
    public  double[] coords() {
        return coords;
    }

    /**
     * Sets the transformed values of this point to the transformation
     * applied to the untransformed values. Also, adds the extra Z coordinate
     * to the transformed Z coordinate.
     * Changes the coords array.
     * @param t the transformation
     */
    public  void transform( Transformation t ) {
        t.mat.transform(this);
        coords[2] += extraZ;
    }

    /**
     * Multiplies the transformed coords of this point by the given scale factor.
     * @param factor what to multiply by
     */
    public  void scale( double factor ) {
        for ( int i = 0; i < coords .length; i++ ) {
            coords[i] *= factor;
        }
    }

    /**
     * @return true if any of the coordinates of this point are not a number or infinite
     */
    public boolean isNaN() {
        for (int i = 0; i < coords.length; ++i)
            if (Double.isInfinite(coords[i]) || Double.isNaN(coords[i]))
                return true;
        return false;
    }
    
    public String toString() {
        String str = "";
        for (int i = 0; i < coords.length; ++i) {
            str += coords[i];
            if (i < coords.length - 1)
                str += ", ";
        }
        return str;
    }


}


