package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

/**
 * A line in 3D that can be drawn.
 *
 * @author deigen
 */
public class PolygonLine extends Polygon implements RayIntersectable, Drawable3D {
  
    /**
     * color1 and color2 are the colors at each of the endpts
     */
    private DemoColor color1, color2;

    /**
     * lighting vectors at each of the endpts
     */
    private LightingVector lightvec1, lightvec2;
    
    private  boolean drawThick = false;

    private  boolean isNaN;


    /**
     * @param start an endpoint of the line
     * @param end an endpoint of the line
     * @param color1 the color at the start pt
     * @param color2 the color at the end pt
     * @param vec1 the lighting vector (e.g. tangent vector) at the start pt
     * @param vec2 the lighting vector (e.g. tangent vector) at the end pt
     */
    public 
    PolygonLine( Point start, Point end,
                 DemoColor color1, DemoColor color2,
                 LightingVector vec1, LightingVector vec2) {
        super();

            this .points = new Point []{ start, end };
            this.color1 = color1;
            this.color2 = color2;
            this.lightvec1 = vec1;
            this.lightvec2 = vec2;
            if (start instanceof PointSortable)
                ((PointSortable) start).addObject(this);
            if (end instanceof PointSortable)
                ((PointSortable) end).addObject(this);
            isNaN = start.isNaN() || end.isNaN();
        }

    public void setColor(DemoColor color) {
        this.color1 = this.color2 = color;
    }

    public DemoColor color() {
        return color1;
    }

    public PointSortable zmaxPoint() {
        if (points[0].coords[2] > points[1].coords[2])
            return (PointSortable) points[0];
        return (PointSortable) points[1];
    }

    public boolean isTransparent() {
        return color1.alpha != 1 || color2.alpha != 1;
    }

    /**
     * Sets whether this line should draw thick or normally.
     * @param state whether to draw thick
     */
    public  void setDrawThick( boolean state ) {
        this .drawThick = state;
    }

    /**
     * Intersects this line segment with a ray (within the ray's tolerance)
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        // vertices in world-space
        double[] p0 = M.point(points[0].untransformedCoords),
                 p1 = M.point(points[1].untransformedCoords);
        // vertices in ray-space
        double[] v0 = ray.transf.transform(p0),
                 v1 = ray.transf.transform(p1);
        // in this transformed space (ray-space), ray is the z axis
        // does the line segment projected to xy-plane contain the origin?
        double[] dir = M.sub(v1, v0);
        double[] dirPlane = new double[]{dir[0], dir[1]};
        double[] v0Plane = new double[]{v0[0], v0[1]};
        double s = - M.dot(v0Plane, dirPlane) / M.dot(dirPlane, dirPlane);
        if (0 <= s && s <= 1 && M.length(M.add(v0Plane, M.mult(s, dirPlane))) < ray.tol) {
            // intersected w/in tolerance
            double[] q = M.add(v0, M.mult(s, dir)); // pt of intersection (ray-space)
            double t = q[2]; // in ray-space, t is just direction along z axis
            intersection.set(ray, this, t, ray.transf_inv.transform(q));
            return true;
        }
        return false;
    }


    public void planeIntersect(Matrix4D mat,
                               LinkedList outpoints, LinkedList outpolys, LinkedList outlvecs) {
        double[] v0 = M.point(points[0]);
        double[] v1 = M.point(points[1]);
        double[] p0 = mat.transform(v0);
        double[] p1 = mat.transform(v1);
        double z0 = p0[2], z1 = p1[2];
        if ((z0 <= 0 && z1 >= 0) || (z0 >= 0 && z1 <= 0)) {
            // intersected: the intersection is abs(z0) away from p0
            double t = -z0 / (z1 - z0);
            double[] p = M.add(v0, M.mult(t, M.sub(v1, v0)));
            PointSortable outp = new PointSortable(p);
            outpoints.add(outp);
            outpolys.add(new PolygonPoint(outp,
                                          DemoColor.interpolate(this.color1, this.color2, t)));
        }
    }



    // ** DRAWING METHODS **

    public  void drawProjectedOpen( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        int x = (int) points[0] .coords[0],
                y = - (int)  points[0] .coords[1];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        g .drawLine( x, y, x2, y2 );
        if ( drawThick ) {
            g .drawLine( x + 1, y, x2 + 1, y2 );
            g .drawLine( x - 1, y, x2 - 1, y2 );
            g .drawLine( x, y + 1, x2, y2 + 1 );
            g .drawLine( x, y - 1, x2, y2 - 1 );
        }
    }

    public 
    void drawProjectedFilledFramed( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        g .drawLine( x, y, x2, y2 );
        if ( drawThick ) {
            g .drawLine( x + 1, y, x2 + 1, y2 );
            g .drawLine( x - 1, y, x2 - 1, y2 );
            g .drawLine( x, y + 1, x2, y2 + 1 );
            g .drawLine( x, y - 1, x2, y2 - 1 );
        }
    }

    public  void drawProjectedFilled( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        g .drawLine( x, y, x2, y2 );
        if ( drawThick ) {
            g .drawLine( x + 1, y, x2 + 1, y2 );
            g .drawLine( x - 1, y, x2 - 1, y2 );
            g .drawLine( x, y + 1, x2, y2 + 1 );
            g .drawLine( x, y - 1, x2, y2 - 1 );
        }
    }

    public  void drawProjectedSuspended( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        g .drawLine( (int) points[0] .coords[0],
                      - (int) points[0] .coords[1],
                      (int) points[1] .coords[0],
                      - (int) points[1] .coords[1] );
    }
    
    public void drawProjectedOpen( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor1 = lightvec1.applyLighting(color1).zbufferColor();
        int zbufColor2 = lightvec2.applyLighting(color2).zbufferColor();
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        double z =  points[0].coords[2];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        double z2 =  points[1].coords[2];
        img .drawLineShade( x,  y,  z+1,  zbufColor1,
                            x2, y2, z2+1, zbufColor2 );
        if ( drawThick ) {
            // change z a little to draw the line "thicker" in the z dimension also
            img .drawLineShade( x + 1,  y,  z+1,  zbufColor1,
                                x2 + 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x - 1,  y,  z+1,  zbufColor1,
                                x2 - 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x,  y + 1,  z+1,  zbufColor1,
                                x2, y2 + 1, z2+1, zbufColor2 );
            img .drawLineShade( x,  y - 1,  z+1,  zbufColor1,
                                x2, y2 - 1, z2+1, zbufColor2 );
        }
    }

    public void drawProjectedFilledFramed(ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor1 = lightvec1.applyLighting(color1).zbufferColor();
        int zbufColor2 = lightvec2.applyLighting(color2).zbufferColor();
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        double z =  points[0].coords[2];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        double z2 =  points[1].coords[2];
        img .drawLineShade( x,  y,  z+1,  zbufColor1,
                            x2, y2, z2+1, zbufColor2 );
        if ( drawThick ) {
            // change z a little to draw the line "thicker" in the z dimension also
            img .drawLineShade( x + 1,  y,  z+1,  zbufColor1,
                                x2 + 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x - 1,  y,  z+1,  zbufColor1,
                                x2 - 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x,  y + 1,  z+1,  zbufColor1,
                                x2, y2 + 1, z2+1, zbufColor2 );
            img .drawLineShade( x,  y - 1,  z+1,  zbufColor1,
                                x2, y2 - 1, z2+1, zbufColor2 );
        }
    }
    
    public void drawProjectedFilled( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor1 = lightvec1.applyLighting(color1).zbufferColor();
        int zbufColor2 = lightvec2.applyLighting(color2).zbufferColor();
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        double z =  points[0].coords[2];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        double z2 =  points[1].coords[2];
        img .drawLineShade( x,  y,  z+1,  zbufColor1,
                            x2, y2, z2+1, zbufColor2 );
        if ( drawThick ) {
            // change z a little to draw the line "thicker" in the z dimension also
            img .drawLineShade( x + 1,  y,  z+1,  zbufColor1,
                                x2 + 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x - 1,  y,  z+1,  zbufColor1,
                                x2 - 1, y2, z2+1, zbufColor2 );
            img .drawLineShade( x,  y + 1,  z+1,  zbufColor1,
                                x2, y2 + 1, z2+1, zbufColor2 );
            img .drawLineShade( x,  y - 1,  z+1,  zbufColor1,
                                x2, y2 - 1, z2+1, zbufColor2 );
        }
    }
    
    public void drawProjectedSuspended( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor1 = lightvec1.applyLighting(color1).zbufferColor();
        int x = (int) points[0] .coords[0],
                y = - (int) points[0] .coords[1];
        double z =  points[0].coords[2];
        int x2 = (int) points[1] .coords[0],
                y2 = - (int) points[1] .coords[1];
        double z2 =  points[1].coords[2];
        img .drawLine( x, y, z+1,  x2, y2, z2+1,  zbufColor1 );
    }


    public  void resetDrewState() {
        drew = false;
    }

    public  boolean drew = false;


}


