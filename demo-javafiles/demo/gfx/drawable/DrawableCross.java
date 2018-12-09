//
//  DrawableCross.java
//  Demo
//
//  Created by David Eigen on Tue Jul 23 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

public class DrawableCross implements Drawable3D {
    
    private Point point_;
    private int size_;
    private DemoColor color_;

    private boolean drew_ = false;
    private boolean isNaN_ = false;

    /**
     * Constructs a new DrawableCross.
     * @param pt the point the cross should be drawn at (point is dimension 3)
     * @param size the length of each line in the cross (in pixels)
     * @param color the color of the cross
     */
    public DrawableCross(Point pt, int size, DemoColor color) {
        point_ = pt;
        isNaN_ = pt.isNaN();
        size_ = size;
        color_ = color;
        if (pt instanceof PointSortable)
            ((PointSortable) pt).addObject(this);
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
        int x =  (int) Math.round(point_.coords[0]),
            y = -(int) Math.round(point_.coords[1]);
        g.drawLine(x - size_/2, y,
                   x + size_/2, y);
        g.drawLine(x, y - size_/2,
                   x, y + size_/2);
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
        int color = color_.zBufferColor();
        int x =  (int) Math.round(point_.coords[0]),
            y = -(int) Math.round(point_.coords[1]);
        double z =  point_.coords[2];
        img.drawLine(x - size_/2, y, z,
                     x + size_/2, y, z,
                     color);
        img.drawLine(x, y - size_/2, z,
                     x, y + size_/2, z,
                     color);
        img.drawLine(x, y, z - size_/2,
                     x, y, z + size_/2,
                     color);
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
