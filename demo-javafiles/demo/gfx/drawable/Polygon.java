package demo.gfx.drawable;

import demo.gfx.Point;
import demo.gfx.Matrix4D;
import demo.util.LinkedList;

/**
 * A Polygon stores an array of points. These points are the vertices of the polygon.
 * 
 * @author deigen
 */
public abstract class Polygon {

    /**
     * the points of the polyogn
     */
    public  Point[] points;

    /**
     * returns an array containing the coordNumber'th coordinates of all points.
     * takes the floor of the coordinates to make them ints.
     * If coordNumber == 1 (the y-coordinate), returns the negative of the stored y-coordinates
     * For example, coordPoints(0) will return an array containing the x-coordinates of the points
     * @param coordNumber the number of the coordinate to get values for
     * @return an array of the coordNumber-th coordinates for all points
     */
    protected  int[] coordPoints( int coordNumber ) {
        int[] coordPoints = new int [ points .length ];
        // negate if it's the y-coordinate
        if ( coordNumber != 1 ) {
            for ( int i = 0; i < coordPoints .length; i++ ) {
                coordPoints[i] = (int) ((Point) points[i]) .coords[coordNumber];
            }
        }
        else {
            for ( int i = 0; i < coordPoints .length; i++ ) {
                coordPoints[i] = - (int) ((Point) points[i]) .coords[coordNumber];
            }
        }
        return coordPoints;
    }

    /**
     * returns an array containing the coordNumber'th coordinates of all points.
     * If coordNumber == 1 (the y-coordinate), returns the negative of the stored y-coordinates
     * For example, coordPoints(0) will return an array containing the x-coordinates of the points
     * @param coordNumber the number of the coordinate to get values for
     * @return an array of the coordNumber-th coordinates for all points
     */
    protected  double[] coordPointsDouble( int coordNumber ) {
        double[] coordPoints = new double [ points .length ];
        // negate if it's the y-coordinate
        if ( coordNumber != 1 ) {
            for ( int i = 0; i < coordPoints .length; i++ ) {
                coordPoints[i] = ((Point) points[i]) .coords[coordNumber];
            }
        }
        else {
            for ( int i = 0; i < coordPoints .length; i++ ) {
                coordPoints[i] = - ((Point) points[i]) .coords[coordNumber];
            }
        }
        return coordPoints;
    }


    /**
     * @return the number of points in the polygon
     */
    protected  int numPoints() {
        return points .length;
    }


    /**
     * Intersects this polygon with a plane.
     * The plane is specified as an affine transformation
     * that transforms space st the plane normal goes to (0,0,1)
     * and a point on the plane goes to the origin.
     * All drawables, points, and lighting vectors created are added to the given lists.
     * @param points the list to add points to
     * @param polys  the list to add polygons to
     * @param lvecs  the list to add lighting vectors to
     */
    public abstract void planeIntersect(Matrix4D mat,
                                        LinkedList poitns, LinkedList polys, LinkedList lvecs);


}


