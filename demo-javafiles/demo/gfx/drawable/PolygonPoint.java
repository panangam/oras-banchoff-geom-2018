package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

/**
 * A point (dot) in 3D that can be drawn.
 *
 * @author deigen
 */
public class PolygonPoint extends Polygon implements RayIntersectable, Drawable3D{

    /**
     * color is the color of the polygon
     */
    private  DemoColor color;

    private  boolean drawThick = false;

    private  boolean isNaN;


    /**
     * @param point the point
     * @param color the color of the point
     */
    public  PolygonPoint( Point point, DemoColor color ) {
        super();

            this .points = new Point []{ point };
            this .color = color;
            if (point instanceof PointSortable)
                ((PointSortable) point).addObject(this);
            isNaN = point.isNaN();
        }

    /**
     * Sets whether this line should draw thick or normally.
     * @param state whether to draw thick
     */
    public  void setDrawThick( boolean state ) {
        this .drawThick = state;
    }

    public void setColor(DemoColor color) {
        this.color = color;
    }

    public DemoColor color() {
        return color;
    }

    public PointSortable zmaxPoint() {
        return (PointSortable) points[0];
    }

    public boolean isTransparent() {
        return color.alpha != 1;
    }

    /**
     * Intersects a ray with this point (within the ray's tolerance).
     * If this is an intersection, this point's location is given as the
     * intersection w/ the ray, not the pt on the ray.
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        // point in world-space
        double[] p_world = M.point(points[0].untransformedCoords);
        // point in ray-space
        double[] p = ray.transf.transform(p_world);
        if (M.length(new double[]{p[0], p[1]}) < ray.tol) {
            intersection.set(ray, this, p[2], p_world);
            return true;
        }
        return false;
    }

    public void planeIntersect(Matrix4D mat,
                               LinkedList outpoints, LinkedList outpolys, LinkedList outlvecs) {
        double[] p = mat.transform(M.point(points[0]));
        if (M.close(p[2], 0)) {
            PointSortable outp = new PointSortable(M.point(points[0]));
            outpoints.add(outp);
            outpolys.add(new PolygonPoint(outp, this.color));
        }
    }
    
    
    public  void drawProjectedOpen( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( color.awtColor() );
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        if ( drawThick ) {
            g .fillOval( x - 1, y - 1, 2, 2 );
        }
        else {
            g .drawLine( x, y, x, y );
        }
    }

    public 
    void drawProjectedFilledFramed( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( color.awtColor() );
        g .drawLine( (int) points[0] .coords[0],
                      - (int) points[0] .coords[1],
                      (int) points[0] .coords[0],
                      - (int) points[0] .coords[1] );
    }

    public  void drawProjectedFilled( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( color.awtColor() );
        g .drawLine( (int) points[0] .coords[0],
                      - (int) points[0] .coords[1],
                      (int) points[0] .coords[0],
                      - (int) points[0] .coords[1] );
    }

    public  void drawProjectedSuspended( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( color.awtColor() );
        g .drawLine( (int) points[0] .coords[0],
                      - (int) points[0] .coords[1],
                      (int) points[0] .coords[0],
                      - (int) points[0] .coords[1] );
    }


    public void drawProjectedOpen( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufferColor = color.zbufferColor();
        img.drawLine( (int) points[0].coords[0],
                      -(int) points[0].coords[1],
                            points[0].coords[2],
                      
                      (int) points[0].coords[0],
                      -(int) points[0].coords[1],
                            points[0].coords[2], zbufferColor );
        if (drawThick) {
            // draw around the pt also
            img.drawLine( (int) points[0].coords[0] - 1,
                          -(int) points[0].coords[1],
                                points[0].coords[2],

                          (int) points[0].coords[0] - 1,
                          -(int) points[0].coords[1],
                                points[0].coords[2], zbufferColor );
            
            img.drawLine( (int) points[0].coords[0] + 1,
                          -(int) points[0].coords[1],
                                points[0].coords[2],

                          (int) points[0].coords[0] + 1,
                          -(int) points[0].coords[1],
                                points[0].coords[2], zbufferColor );
            
            img.drawLine( (int) points[0].coords[0],
                          -(int) points[0].coords[1] - 1,
                                points[0].coords[2],

                          (int) points[0].coords[0],
                          -(int) points[0].coords[1] - 1,
                                points[0].coords[2], zbufferColor );
            
            img.drawLine( (int) points[0].coords[0],
                          -(int) points[0].coords[1] + 1,
                                points[0].coords[2],

                          (int) points[0].coords[0],
                          -(int) points[0].coords[1] + 1,
                                points[0].coords[2], zbufferColor );
            
            img.drawLine( (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                points[0].coords[2] + 1,

                          (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                points[0].coords[2] + 1, zbufferColor );
        }
    }

    public void drawProjectedFilledFramed( ZBufferedImage img ) {
        drawProjectedOpen(img);
    }

    public void drawProjectedFilled( ZBufferedImage img ) {
        drawProjectedOpen(img);
    }

    public void drawProjectedSuspended( ZBufferedImage img ) {
        drawProjectedOpen(img);
    }

    public  void resetDrewState() {
        drew = false;
    }

    public  boolean drew = false;



}


