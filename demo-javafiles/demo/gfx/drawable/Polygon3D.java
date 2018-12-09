package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

/**
 * A Polygon3D is a 3D Polygon that can be drawn.
 * General Polygon3Ds cannot be drawn filled using the Z buffer. If 
 * a draw filled method with a Z buffer is called on a general Polygon3D, 
 * the polygon will be drawn open.
 * 
 * @author deigen
 */
public class Polygon3D extends Polygon implements RayIntersectable, Drawable3D {

    /**
     * color is the color of the polygon
     * if drawing filled and framed, frameColor is the color of the frame.
     */
    private DemoColor color, frameColor;

    /**
     * lighting vector (normal)
     */
    private LightingVector lightvec;
    
    /**
     * Whether this polygon contains NaN or Infinite vertices
     */
    private boolean isNaN = false;

    
    /**
     * @param points the points of this polygon
     * @param color the color of the polygon
     * @param frameColor the color of the frame of the polygon, when drawing filled and framed
     * @param lightvec the lighting vector (e.g. normal vector)
     */
    public 
    Polygon3D( Point[] points, DemoColor color,
                DemoColor frameColor, LightingVector lightvec ) {
        super();

            this .points = points;
            this .color = color;
            this .frameColor = frameColor;
            this .frameColor.alpha = color.alpha;
            this .lightvec = lightvec;
            for (int i = 0; i < points.length; ++i) {
                if (points[i] instanceof PointSortable)
                    ((PointSortable) points[i]).addObject(this);
                isNaN = isNaN || points[i].isNaN();
            }
   }

    public void setColor(DemoColor color) {
        this.color = color;
    }

    public DemoColor color() {
        return color;
    }

    public PointSortable zmaxPoint() {
        int m = 0;
        for (int i = 1; i < points.length; ++i)
            if (points[i].coords[2] > points[m].coords[2])
                m = i;
        return (PointSortable) points[m];
    }

    public boolean isTransparent() {
        return color.alpha != 1;
    }
    
    /**
     * Intersects a ray with this polygon (exactly, not within tolerance).
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        if (points.length < 3)
            return false;
        double[][] ps_world = new double[points.length][]; // points in world-space
        double[][] ps = new double[points.length][]; // points in ray-space
        for (int i = 0; i < ps.length; ++i) {
            ps_world[i] = M.point(points[i].untransformedCoords);
            ps[i] = ray.transf.transform(ps_world[i]);
        }
        // scanline in the plane to see if the origin is contained in the polygon
        // get segment intersections
        boolean justIntersected = false;
        boolean inPolygon = false;
        // "count" number of edges to the left of zy plane (ie, left of 0) on xz plane
        for (int i = 0; i < ps.length; ++i) {
            int j = (i + 1) % ps.length;
            double[] dir = M.sub(ps[i], ps[j]);
            double t = -ps[j][1] / dir[1];
            if (0 <= t && t <= 1 && M.add(ps[j], M.mult(t, dir))[0] < 0) {
                // intersects; if we just intersected, intersection may be a vertex
                if (justIntersected && (t == 0 || t == 1)) {
                    // vertex intersection
                    // need to check orientation
                    double[] checkpt = M.add(ps[i], new double[]{1,0,0,0});
                    if ( (M.orientation(ps[i-1], ps[i], checkpt)
                          == M.orientation(checkpt, ps[i], ps[j])) )
                        // went through the polygon in this vertex
                        // want net effect of no toggle for this iteration of the loop
                        inPolygon = !inPolygon;
                }
                inPolygon = !inPolygon;
                justIntersected = true;
            }
            else {
                justIntersected = false;
            }
        } // end for
        if (inPolygon) {
            // did intersect: now, need to find intersection w/ the plane
            double[] p0 = ps_world[0], p1 = ps_world[1], p2 = ps_world[2];
            double[] edge1 = M.sub(p1, p0);
            double[] edge2 = M.sub(p2, p0);
            double[] normal = M.cross(edge1, edge2);
            double t = (M.dot(normal, p0) - M.dot(normal, ray.p))
                / M.dot(normal, ray.v);
            double[] q = M.add(M.mult(t, ray.v), ray.p);
            intersection.set(ray, this, t, q);
            return true;
        }
        return false;
    }


    private class PlaneIntersectComparator implements SortComparator {
        private int d;
        public PlaneIntersectComparator(int d) {this.d = d;}
        public boolean isLessThanOrEqualTo(Object a, Object b) {
            if (b == null) return true;
            if (a == null) return false;
            return ((double[]) a)[d] <= ((double[]) b)[d];
        }
    }

    public void planeIntersect(Matrix4D mat,
                               LinkedList outpoints, LinkedList outpolys, LinkedList outlvecs) {
        if (points.length < 3)
            return;
        double[][] vs = new double[points.length][]; // vertices in world-space
        double[][] ps = new double[points.length][]; // vertices in plane-space
        for (int i = 0; i < points.length; ++i) {
            vs[i] = M.point(points[i]);
            ps[i] = mat.transform(vs[i]);
        }
        // find intersections on all edges, then connect them
        double[][] ivs = new double[points.length][]; // intersections, in world-space
        double[] ts = new double[points.length]; // where intersection lies on its edge, 0..1
        boolean intersected = false;
        for (int i = 0; i < points.length; ++i) {
            int j = (i+1) % points.length;
            if ((ps[i][2] <= 0 && ps[j][2] >= 0) ||
                (ps[i][2] >= 0 && ps[j][2] <= 0)) {
                double t;
                if (M.close(ps[i][2] - ps[j][2], 0))
                    // intersected whole edge, or close enough for bad t-val
                    t = ps[i][2] == 0 ? 0 : ps[j][2] == 0 ? 1 : 0.5;
                else
                    // intersected the edge
                    t = -ps[i][2] / (ps[j][2] - ps[i][2]);
                ts[i] = t;
                ivs[i] = M.add(vs[i], M.mult(t, M.sub(vs[j],
                                                      vs[i])));
                intersected = true;
            }
            else {
                ivs[i] = null;
                ts[i] = Double.NaN;
            }
        }
        if (!intersected)
            return;
        // If we find a vertex intersection, remove one of the edges from
        // the intersections, so there is only one intersection for each vertex
        double last_t = ts[points.length-1];
        double[] last_iv = ivs[points.length-1];
        for (int i = 0; i < points.length; ++i) {
            if (ivs[i] == null) {
                last_iv = null;
                continue;
            }
            if ( last_iv != null
                 && M.close(ivs[i], last_iv)
                 && M.close(last_t, 1)
                 && M.close(ts[i], 0) )
                ivs[i] = null; // repeated vertex: get rid of it
            last_t = ts[i];
            last_iv = ivs[i];
        }
        // find index of first edge w/ an intersection (i0)
        int i0; // first index w/ intersection
        for (i0 = 0; i0 < points.length; ++i0)
            if (ivs[i0] != null) break;
        if (i0 == points.length) {
            // no intersections at all
            return;
        }
        // find leftmost intersection of an edge, since it is
        // definitely a bdy between inside and outside of polygon
        // if all x coords are equal, use the one with least y-coord
        double[] v0 = ivs[i0];
        boolean xcoordsAllEqual = true;
        for (int i = 0; i < points.length; ++i) {
            if (ivs[i] != null && ivs[i][0] != v0[0]) {
                xcoordsAllEqual = false;
                break;
            }
        }
        int d = xcoordsAllEqual ? 1 : 0;
        // hack: want to keep the indices w/ the intersection points
        for (int i = 0; i < points.length; ++i)
            if (ivs[i] != null)
                ivs[i][3] = i;
        // sort by d-coord
        Sorter.sort(ivs, new PlaneIntersectComparator(d));
        // next part of hack: get the indices back
        int[] is = new int[points.length];
        for (int i = 0; i < points.length; ++i) {
            if (ivs[i] == null) break;
            is[i] = (int) ivs[i][3];
            ivs[i][3] = 1;
        }
        Matrix4D polygonPlane = M.mat_zaxis_transf(M.normalize(M.cross(M.sub(vs[1], vs[0]),
                                                                       M.sub(vs[2], vs[0]))),
                                                   vs[0]).inverse();
        boolean inPolygon = false;
        for (int i = 0; i < points.length; ++i) {
            if (ivs[i] == null) break; // non-intersections are at the end
            int j = (i + 1) % points.length;
            if (ivs[j] == null) j = 0;
            int ii = is[i]; // unsorted index of this intersection pt
            double t = ts[ii];
            if (M.close(t, 0) || M.close(t, 1)) {
                // intersection w/ first edge is at a vertex
                double[] vj = polygonPlane.transform(ivs[j]);
                double[] vi, vip, vin;
                if (M.close(t, 0)) {
                    vi  = polygonPlane.transform(vs[ii]);
                    vip = polygonPlane.transform(vs[(ii+1)%points.length]);
                    vin = polygonPlane.transform(vs[(ii-1+points.length)%points.length]);
                }
                else { // t is 1
                    vi = polygonPlane.transform(vs[(ii+1)%points.length]);
                    vip = polygonPlane.transform(vs[(ii+2)%points.length]);
                    vin = polygonPlane.transform(vs[ii]);
                }
                if ((M.orientation(vin, vi, vj) == M.orientation(vj, vi, vip)))
                    inPolygon = !inPolygon;
            }
            else {
                // intesrection w/ fist edge not at a vertex
                // so, we can alternate being inside the polygon
                inPolygon = !inPolygon;
            }
            if (inPolygon) {
                // TODO: make this flat line
                TangentVector tanvec = new TangentVector(M.normalize(M.sub(ivs[j], ivs[i])));
                outlvecs.add(tanvec);
                PointSortable outv1 = new PointSortable(ivs[i]),
                              outv2 = new PointSortable(ivs[j]);
                outpoints.add(outv1); outpoints.add(outv2);
                outpolys.add(new PolygonLine(outv1, outv2,
                                             this.color, this.color,
                                             tanvec, tanvec));
            }
        }
    }

    /**
     * Resets the drew state of this Drawable object to false, so
     * the next time a draw method is called on this polygon, the 
     * polygon will be drawn.
     */
    public  void resetDrewState() {
        drew = false;
    }

    public  boolean drew = false;

    
    public  void drawProjectedOpen( java.awt .Graphics g ) {
        if ( drew ) {
            return ;
        }
        drew = true;
        g .setColor( lightvec.applyLighting(color).awtColor() );
        g .drawPolygon( coordPoints( 0 ), coordPoints( 1 ), numPoints() );
    }

    public 
    void drawProjectedFilledFramed( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int[] xpoints = coordPoints( 0 ), ypoints = coordPoints( 1 );
        g .setColor( lightvec.applyLighting(color).awtColor() );
        g .fillPolygon( xpoints, ypoints, numPoints() );
        g .setColor( lightvec.applyLighting(frameColor).awtColor() );
        g .drawPolygon( xpoints, ypoints, numPoints() );
    }

    public  void drawProjectedFilled( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( lightvec.applyLighting(color).awtColor() );
        g .fillPolygon( coordPoints( 0 ), coordPoints( 1 ), numPoints() );
    }

    public  void drawProjectedSuspended( java.awt .Graphics g ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        g .setColor( lightvec.applyLighting(color).awtColor() );
        g .drawPolygon( coordPoints( 0 ), coordPoints( 1 ), numPoints() );
    }
    
    
    public void drawProjectedOpen( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drew = true;
        int i;
        int zbufferColor = lightvec.applyLighting(color).zbufferColor();
        for (i = 0; i < numPoints() - 1; ++i) {
            img.drawLine( (int) points[i].coords[0],
                          -(int) points[i].coords[1],
                                points[i].coords[2],
                          
                          (int) points[i+1].coords[0],
                          -(int) points[i+1].coords[1],
                                points[i+1].coords[2], zbufferColor );
        }
        if (numPoints() > 2)
            img.drawLine( (int) points[i].coords[0],
                          -(int) points[i].coords[1],
                                points[i].coords[2],
                          
                          (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                points[0].coords[2], zbufferColor );
    }

    public void drawProjectedFilledFramed( ZBufferedImage img ) {
        if ( drew || isNaN ) {
            return ;
        }
        drawProjectedFilled(img);
        int i;
        int zbufferColor = lightvec.applyLighting(color).zbufferColor();
        int zbufferFrameColor = lightvec.applyLighting(frameColor).zbufferColor();
        for (i = 0; i < numPoints() - 1; ++i) {
            img.drawLine( (int) points[i].coords[0],
                          -(int) points[i].coords[1],
                                points[i].coords[2] + 1,

                          (int) points[i+1].coords[0],
                          -(int) points[i+1].coords[1],
                                points[i+1].coords[2] + 1, zbufferFrameColor );
        }
        if (numPoints() > 2)
            img.drawLine( (int) points[i].coords[0],
                          -(int) points[i].coords[1],
                                points[i].coords[2] + 1,

                          (int) points[0].coords[0],
                          -(int) points[0].coords[1],
                                points[0].coords[2] + 1, zbufferFrameColor );
    }

    public void drawProjectedFilled( ZBufferedImage img ) {
        if (drew || isNaN) return;
        drew = true;
        int zbufferColor = lightvec.applyLighting(color).zbufferColor();
        img.fillPolygon(coordPoints(0), coordPoints(1), coordPointsDouble(2), zbufferColor);
   }

    public void drawProjectedSuspended( ZBufferedImage img ) {
       drawProjectedOpen(img);
    }


}


