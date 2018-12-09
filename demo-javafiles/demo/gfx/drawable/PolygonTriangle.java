package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

/**
 * A triangle in 3D that can be drawn.
 * Triangles can be filled when drawing with a Z buffer.
 *
 * @author deigen
 */
 public class PolygonTriangle extends Polygon implements Drawable3D, RayIntersectable {

    /**
     * color is the color of the polygon
     * if drawing filled and framed, frameColor is the color of the frame.
     */
    private DemoColor color1, color2, color3, frameColor;
        
    /**
     * The lighting vectors for the 3 vertices.
     */
    private LightingVector lightvec1, lightvec2, lightvec3;
    
    /**
     * whether to draw the edges
     */
    private boolean drawEdge01, drawEdge12, drawEdge20;

    /**
     * Whether any of the vertices of this triangle are NaN or Infinite
     */
    private boolean isNaN;


    /**
     * @param pt1 a vertex of the triangle
     * @param pt2 a vertex of the triangle
     * @param pt3 a vertex of the triangle
     * @param color1 the color at vertex 1 of the triangle
     * @param color2 the color at vertex 2 of the triangle
     * @param color3 the color at vertex 3 of the triangle
     * @param vec1 the lighting vector at vertex 1 of the triangle (e.g. normal vector)
     * @param vec2 the lighting vector at vertex 2 of the triangle (e.g. normal vector)
     * @param vec3 the lighting vector at vertex 3 of the triangle (e.g. normal vector)
     * @param drawEdge12 whether to draw the edge from pt1 to pt2
     * @param drawEdge23 whether to draw the edge from pt2 to pt3
     * @param drawEdge31 whether to draw the edge from pt3 to pt1
     * @param frameColor the color of the frame of the triangle, when drawing filled and framed
     */
    public PolygonTriangle(Point pt1, Point pt2, Point pt3,
                           DemoColor color1, DemoColor color2, DemoColor color3,
                           LightingVector vec1, LightingVector vec2, LightingVector vec3,
                           boolean drawEdge12, boolean drawEdge23, boolean drawEdge31,
                           DemoColor frameColor) {
        this .points = new Point []{ pt1, pt2, pt3 };
        this .color1 = color1;
        this .color2 = color2;
        this .color3 = color3;
        this .lightvec1 = vec1;
        this .lightvec2 = vec2;
        this .lightvec3 = vec3;
        this .drawEdge01 = drawEdge12;
        this .drawEdge12 = drawEdge23;
        this .drawEdge20 = drawEdge31;
        this .frameColor = frameColor;
        this .frameColor.alpha = color1.alpha; // TODO : make frame color shaded to interp alpha
        if (pt1 instanceof PointSortable)
            ((PointSortable) pt1).addObject(this);
        if (pt2 instanceof PointSortable)
            ((PointSortable) pt2).addObject(this);
        if (pt3 instanceof PointSortable)
            ((PointSortable) pt3).addObject(this);
        isNaN = pt1.isNaN() || pt2.isNaN() || pt3.isNaN();
    }

    public void setColor(DemoColor color) {
        this.color1 = this.color2 = this.color3 = color;
    }

    public DemoColor color() {
        return color1;
    }

    public PointSortable zmaxPoint() {
        int m = 0;
        for (int i = 1; i < 3; ++i)
            if (points[i].coords[2] > points[m].coords[2])
                m = i;
        return (PointSortable) points[m];
    }

    public boolean isTransparent() {
        return color1.alpha != 1 || color2.alpha != 1 || color3.alpha != 1;
    }

    /**
     * Intersects this triangle with a ray.
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        // vertices in world-space
        double[] p0 = M.point(points[0].untransformedCoords),
                 p1 = M.point(points[1].untransformedCoords),
                 p2 = M.point(points[2].untransformedCoords);
        // vertices in ray-space
        double[] v0 = ray.transf.transform(p0),
                 v1 = ray.transf.transform(p1),
                 v2 = ray.transf.transform(p2);
        // in this transformed space, ray is the z axis
        // does the triangle projected to xy-plane contain the origin?
        for (int i = 0; i < 2; ++i)
            if ( (v0[i] < 0 && v1[i] < 0 && v2[i] < 0) ||
                 (v0[i] > 0 && v1[i] > 0 && v2[i] > 0) )
                return false;
        int o1 = M.orientation(v0, v1, ray.tp);
        int o2 = M.orientation(v1, v2, ray.tp);
        int o3 = M.orientation(v2, v0, ray.tp);
        int o = o1 == M.COLINEAR ? o2 == M.COLINEAR ? o3 : o2 : o1;
        if ((o1 != o && o1 != M.COLINEAR) ||
            (o2 != o && o2 != M.COLINEAR) ||
            (o3 != o && o3 != M.COLINEAR))
            return false;
        // pt does intersect: find intersection pt
        double[] edge1 = M.sub(p1, p0);
        double[] edge2 = M.sub(p2, p0);
        double[] normal = M.cross(edge1, edge2);
        double t = (M.dot(normal, p0) - M.dot(normal, ray.p))
                 / M.dot(normal, ray.v);
        double[] q = M.add(M.mult(t, ray.v), ray.p);
        intersection.set(ray, this, t, q);
        return true;
    }

    
    public void planeIntersect(Matrix4D mat,
                               LinkedList outpoints, LinkedList outpolys, LinkedList outlvecs) {
        double[] v0 = M.point(points[0]);
        double[] v1 = M.point(points[1]);
        double[] v2 = M.point(points[2]);
        double[] p0 = mat.transform(v0);
        double[] p1 = mat.transform(v1);
        double[] p2 = mat.transform(v2);
        double z0 = p0[2], z1 = p1[2], z2 = p2[2];
        // we intersect if z coords are on - and + sides of xy-plane
        if (z0 == 0 && z1 == 0 && z2 == 0) {
            // this whole triangle is the intersection
            PointSortable outp1 = new PointSortable(v0),
                          outp2 = new PointSortable(v1),
                          outp3 = new PointSortable(v2);
            LightingVector outlvec1 = (LightingVector) lightvec1.copy(),
                           outlvec2 = (LightingVector) lightvec2.copy(),
                           outlvec3 = (LightingVector) lightvec3.copy();
            outpoints.add(outp1); outpoints.add(outp2); outpoints.add(outp3);
            outlvecs.add(outlvec1); outlvecs.add(outlvec2); outlvecs.add(outlvec3);
            outpolys.add(new PolygonTriangle(outp1, outp2, outp3,
                                             this.color1, this.color2, this.color3,
                                             outlvec1, outlvec2, outlvec3,
                                             false, false, false,
                                             this.frameColor));
            return;
        }
        if (z0 == 0 && z1 == 0) {
            // light vector: project line segment to vertex's tangent plane
            double[] v = M.sub(v1, v0);
            double[] lv1 = M.sub(v, M.mult(M.dot(lightvec1.untransformedCoords, v),
                                           lightvec1.untransformedCoords));
            double[] lv2 = M.sub(v, M.mult(M.dot(lightvec2.untransformedCoords, v),
                                           lightvec2.untransformedCoords));
            PointSortable outp1 = new PointSortable(v0),
                          outp2 = new PointSortable(v1);
            TangentVector outlvec1 = new TangentVector(M.normalize(lv1)),
                          outlvec2 = new TangentVector(M.normalize(lv2));
            outpoints.add(outp1); outpoints.add(outp2);
            outlvecs.add(outlvec1); outlvecs.add(outlvec2);
            outpolys.add(new PolygonLine(outp1, outp2,
                                         this.color1, this.color2,
                                         outlvec1, outlvec2));
            return;
        }
        if (z1 == 0 && z2 == 0) {
            // light vector: project line segment to vertex's tangent plane
            double[] v = M.sub(v2, v1);
            double[] lv2 = M.sub(v, M.mult(M.dot(lightvec2.untransformedCoords, v),
                                           lightvec2.untransformedCoords));
            double[] lv3 = M.sub(v, M.mult(M.dot(lightvec3.untransformedCoords, v),
                                           lightvec3.untransformedCoords));
            PointSortable outp2 = new PointSortable(v1),
                          outp3 = new PointSortable(v2);
            TangentVector outlvec2 = new TangentVector(M.normalize(lv2)),
                          outlvec3 = new TangentVector(M.normalize(lv3));
            outpoints.add(outp2); outpoints.add(outp3);
            outlvecs.add(outlvec2); outlvecs.add(outlvec3);
            outpolys.add(new PolygonLine(outp2, outp3,
                                         this.color2, this.color3,
                                         outlvec2, outlvec3));
            return;
        }
        if (z2 == 0 && z1 == 0) {
            // light vector: project line segment to vertex's tangent plane
            double[] v = M.sub(v0, v2);
            double[] lv1 = M.sub(v, M.mult(M.dot(lightvec1.untransformedCoords, v),
                                           lightvec1.untransformedCoords));
            double[] lv3 = M.sub(v, M.mult(M.dot(lightvec3.untransformedCoords, v),
                                           lightvec3.untransformedCoords));
            PointSortable outp3 = new PointSortable(v2),
                          outp1 = new PointSortable(v0);
            TangentVector outlvec3 = new TangentVector(M.normalize(lv3)),
                          outlvec1 = new TangentVector(M.normalize(lv1));
            outpoints.add(outp3); outpoints.add(outp1);
            outlvecs.add(outlvec3); outlvecs.add(outlvec1);
            outpolys.add(new PolygonLine(outp3, outp1,
                                         this.color3, this.color1,
                                         outlvec3, outlvec1));
            return;
        }
        double[] i1 = null;
        double[] i2 = null;
        double[] n1 = null;
        double[] n2 = null;
        DemoColor c1 = null;
        DemoColor c2 = null;
        if ((z0 < 0 && z1 >= 0) || (z0 > 0 && z1 <= 0)) {
            // intersect 01 edge
            double t = -z0 / (z1 - z0);
            i1 = M.add(v0, M.mult(t, M.sub(v1, v0)));
            n1 = M.add(M.mult(1-t, lightvec1.untransformedCoords),
                       M.mult(  t, lightvec2.untransformedCoords));
            c1 = DemoColor.interpolate(color1, color2, t);
        }
        if ((z1 < 0 && z2 >= 0) || (z1 > 0 && z2 <= 0)) {
            // intersect 12 edge
            double t = -z1 / (z2 - z1);
            double[] i = M.add(v1, M.mult(t, M.sub(v2, v1)));
            double[] n = M.add(M.mult(1-t, lightvec2.untransformedCoords),
                               M.mult(  t, lightvec3.untransformedCoords));
            DemoColor c = DemoColor.interpolate(color2, color3, t);
            if (i1 == null) {
                i1 = i;
                n1 = n;
                c1 = c;
            }
            else {
                i2 = i;
                n2 = n;
                c2 = c;
            }
        }
        if ((z2 < 0 && z0 >= 0) || (z2 > 0 && z0 <= 0)) {
            // intersect 02 edge
            double t = -z2 / (z0 - z2);
            double[] i = M.add(v2, M.mult(t, M.sub(v0, v2)));
            double[] n = M.add(M.mult(1-t, lightvec3.untransformedCoords),
                               M.mult(  t, lightvec1.untransformedCoords));
            DemoColor c = DemoColor.interpolate(color3, color1, t);
            if (i1 == null) {
                i1 = i;
                n1 = n;
                c1 = c;
            }
            else {
                i2 = i;
                n2 = n;
                c2 = c;
            }
        }
        if (i1 != null && i2 != null) {
            // light vector: project line segment to vertex's tangent plane
            double[] v = M.sub(i2, i1);
            double[] lv1 = M.sub(v, M.mult(M.dot(n1, v), n1));
            double[] lv2 = M.sub(v, M.mult(M.dot(n2, v), n2));
            PointSortable outp1 = new PointSortable(i1),
                          outp2 = new PointSortable(i2);
            TangentVector outlvec1 = new TangentVector(M.normalize(lv1)),
                          outlvec2 = new TangentVector(M.normalize(lv2));
            outpoints.add(outp1); outpoints.add(outp2);
            outlvecs.add(outlvec1); outlvecs.add(outlvec2);
            outpolys.add(new PolygonLine(outp1, outp2,
                                         c1, c2,
                                         outlvec1, outlvec2));
        }
    }


    /**
     * Resets the drew state of this Drawable object to false, so
     * the next time a draw method is called on this triangle, the 
     * triangle will be drawn.
     */
    public  void resetDrewState() {
        drew = false;
    }

    public  boolean drew = false;
    
    
    
    // ** DRAWING METHODS **

    public  void drawProjectedOpen( java.awt .Graphics g ) {
        if ( drew || isNaN )
            return;
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                     (int) points[1].coords[0], -(int) points[1].coords[1] );
        g .drawLine( (int) points[1].coords[0], -(int) points[1].coords[1],
                     (int) points[2].coords[0], -(int) points[2].coords[1] );
        g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                     (int) points[2].coords[0], -(int) points[2].coords[1] );            
    }

    public 
    void drawProjectedFilledFramed( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int[] xpoints = coordPoints( 0 ), ypoints = coordPoints( 1 );
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        g .fillPolygon( xpoints, ypoints, numPoints() );
        g.setColor(lightvec1.applyLighting(frameColor).awtColor());
        if (drawEdge01)
            g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                         (int) points[1].coords[0], -(int) points[1].coords[1] );
        if (drawEdge12)
            g .drawLine( (int) points[1].coords[0], -(int) points[1].coords[1],
                         (int) points[2].coords[0], -(int) points[2].coords[1] );
        if (drawEdge20)
            g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                         (int) points[2].coords[0], -(int) points[2].coords[1] );            
    }

    public  void drawProjectedFilled( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        g .fillPolygon( coordPoints( 0 ), coordPoints( 1 ), numPoints() );
    }

    public  void drawProjectedSuspended( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g.setColor(lightvec1.applyLighting(color1).awtColor()); // TODO: use avg color?
        g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                     (int) points[1].coords[0], -(int) points[1].coords[1] );
        g .drawLine( (int) points[1].coords[0], -(int) points[1].coords[1],
                     (int) points[2].coords[0], -(int) points[2].coords[1] );
        g .drawLine( (int) points[0].coords[0], -(int) points[0].coords[1],
                     (int) points[2].coords[0], -(int) points[2].coords[1] );
    }


    public void drawProjectedOpen( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor0 = ZBufferedImage.convertColor(lightvec1.applyLighting(color1));
        int zbufColor1 = ZBufferedImage.convertColor(lightvec2.applyLighting(color2));
        int zbufColor2 = ZBufferedImage.convertColor(lightvec3.applyLighting(color3));
        img.drawLineShade( (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                 points[0].coords[2],
                           zbufColor0,
                           (int) points[1].coords[0],
                          -(int) points[1].coords[1],
                                 points[1].coords[2],
                           zbufColor1 );
        img.drawLineShade( (int) points[1].coords[0],
                          -(int) points[1].coords[1],
                                 points[1].coords[2],
                           zbufColor1,
                           (int) points[2].coords[0],
                          -(int) points[2].coords[1],
                                 points[2].coords[2],
                           zbufColor2 );
        img.drawLineShade( (int) points[2].coords[0],
                          -(int) points[2].coords[1],
                                 points[2].coords[2],
                           zbufColor2,
                           (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                 points[0].coords[2],
                           zbufColor0 );
    }

    public void drawProjectedFilledFramed( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor0 = ZBufferedImage.convertColor(lightvec1.applyLighting(color1));
        int zbufColor1 = ZBufferedImage.convertColor(lightvec2.applyLighting(color2));
        int zbufColor2 = ZBufferedImage.convertColor(lightvec3.applyLighting(color3));
        int zbufFrameColor0 = ZBufferedImage.convertColor(lightvec1.applyLighting(frameColor));
        int zbufFrameColor1 = ZBufferedImage.convertColor(lightvec2.applyLighting(frameColor));
        int zbufFrameColor2 = ZBufferedImage.convertColor(lightvec3.applyLighting(frameColor));
        img.fillTriangleShade( (int) points[0].coords[0],
                              -(int) points[0].coords[1],
                                     points[0].coords[2],
                               zbufColor0,
                               (int) points[1].coords[0],
                              -(int) points[1].coords[1],
                                     points[1].coords[2],
                               zbufColor1,
                               (int) points[2].coords[0],
                              -(int) points[2].coords[1],
                                     points[2].coords[2],
                               zbufColor2 );
        if (drawEdge01)
            img.drawLineShade( (int) points[0].coords[0],
                               -(int) points[0].coords[1],
                               points[0].coords[2] + 1,
                               zbufFrameColor0,
                               (int) points[1].coords[0],
                               -(int) points[1].coords[1],
                               points[1].coords[2] + 1,
                               zbufFrameColor1 );
        if (drawEdge12)
            img.drawLineShade( (int) points[1].coords[0],
                               -(int) points[1].coords[1],
                               points[1].coords[2] + 1,
                               zbufFrameColor1,
                               (int) points[2].coords[0],
                               -(int) points[2].coords[1],
                               points[2].coords[2] + 1,
                               zbufFrameColor2 );
        if (drawEdge20)
            img.drawLineShade( (int) points[2].coords[0],
                               -(int) points[2].coords[1],
                               points[2].coords[2] + 1,
                               zbufFrameColor2,
                               (int) points[0].coords[0],
                               -(int) points[0].coords[1],
                               points[0].coords[2] + 1,
                               zbufFrameColor0 );
    }

    public void drawProjectedFilled( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int zbufColor0 = ZBufferedImage.convertColor(lightvec1.applyLighting(color1));
        int zbufColor1 = ZBufferedImage.convertColor(lightvec2.applyLighting(color2));
        int zbufColor2 = ZBufferedImage.convertColor(lightvec3.applyLighting(color3));
        img.fillTriangleShade( (int) points[0].coords[0],
                              -(int) points[0].coords[1],
                                     points[0].coords[2],
                               zbufColor0,
                               (int) points[1].coords[0],
                              -(int) points[1].coords[1],
                                     points[1].coords[2],
                               zbufColor1,
                               (int) points[2].coords[0],
                              -(int) points[2].coords[1],
                                     points[2].coords[2],
                               zbufColor2 );
    }

    public void drawProjectedSuspended( ZBufferedImage img ) {
        drawProjectedOpen(img);
    }

}
