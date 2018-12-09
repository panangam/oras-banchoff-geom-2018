//
//  DrawableArrowhead.java
//  Demo
//
//  Created by David Eigen on Thu Jul 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

public class DrawableArrowhead implements Drawable3D {

    private Point point_, dirPoint_;
    private double width_, length_;
    private DemoColor color_;

    private boolean drew_ = false;
    private boolean isNaN_ = false;
    
    /**
     * Constructs an arrowhead, which is just an isocoles triangle
     * with its apex at point, and base in the direction of dirPt - point.
     * @param point the apex of the arrohead
     * @param dirPt the arrowhead points in the direction dirPt - point
     * @param length the length of the arrowhead triangle (in pixels)
     * @param width the width of the base of ht arrowhead triangle (in pixels)
     * @param color the color
     */
    public DrawableArrowhead(Point point, Point dirPt,
                             double length, double width,
                             DemoColor color) {
        point_ = point;
        dirPoint_ = dirPt;
        isNaN_ = point.isNaN() || dirPt.isNaN();
        length_ = length;
        width_ = width;
        color_ = color;
        if (point instanceof PointSortable)
            ((PointSortable) point).addObject(this);
    }

    public void setColor(DemoColor color) {
        color_ = color;
    }

    public DemoColor color() {
        return color_;
    }
    
    public PointSortable zmaxPoint() {
        return (PointSortable) point_;
    }

    public boolean isTransparent() {
        return color_.alpha != 1;
    }
    
    public void drawProjectedOpen( java.awt .Graphics g ) {
        if (drew_ || isNaN_) return;
        drew_ = true;
        g.setColor(color_.awtColor());
        double[] dir = M.normalize(M.sub(point_.coords, dirPoint_.coords));
        if (M.aboutEqual(dir, new double[]{0,0,0})) {
            int ptx = (int) Math.round(point_.coords[0]);
            int pty = -(int) Math.round(point_.coords[1]);
            int w = (int) width_;
            g.fillOval(ptx-w/2, pty-w/2, w, w);
        }
        else {
            double[] baseCenter = M.add(M.mult(length_, dir), point_.coords);
            double[] baseDir = new double[]{-dir[1], dir[0], 0};
            double[] basePt1 = M.add(baseCenter, M.mult(width_/2, baseDir));
            double[] basePt2 = M.add(baseCenter, M.mult(-width_/2, baseDir));
            g.fillPolygon(new int[]{(int) Math.round(point_.coords[0]),
                                    (int) Math.round(basePt1[0]),
                                    (int) Math.round(basePt2[0])},
                          new int[]{-(int) Math.round(point_.coords[1]),
                                    -(int) Math.round(basePt1[1]),
                                    -(int) Math.round(basePt2[1])},
                          3);
        }
    }
    public void drawProjectedFilledFramed( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }
    public void drawProjectedFilled( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }
    public void drawProjectedSuspended( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }


    public void drawProjectedOpen( ZBufferedImage img ) {
        if (drew_ || isNaN_) return;
        drew_ = true;
        double[] dir = M.normalize(M.sub(point_.coords, dirPoint_.coords));
        double[] baseCenter = M.add(M.mult(length_, dir), point_.coords);
        double[] baseDir = new double[]{-dir[1], dir[0], 0};
        double[] basePt1 = M.add(baseCenter, M.mult(width_/2, baseDir));
        double[] basePt2 = M.add(baseCenter, M.mult(-width_/2, baseDir));
        img.fillTriangle((int) Math.round(point_.coords[0]),
                         -(int) Math.round(point_.coords[1]),
                               point_.coords[2],

                         (int) Math.round(basePt1[0]),
                         -(int) Math.round(basePt1[1]),
                               basePt1[2],

                         (int) Math.round(basePt2[0]),
                         -(int) Math.round(basePt2[1]),
                               basePt2[2],

                         color_.zBufferColor());
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


    public void resetDrewState() {
        drew_ = false;
    }

    

}
