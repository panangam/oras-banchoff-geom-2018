//
//  DrawableDot.java
//  Demo
//
//  Created by David Eigen on Tue Jul 23 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.graph.ZBufferedImage;

public class DrawableDot implements Drawable3D {

    private Point point_;
    private int size_;
    private DemoColor color_;

    private boolean drew_ = false;
    private boolean isNaN_ = false;

    /**
     * Constructs a new DrawableDot.
     * @param pt the point the dot should be drawn at.
     * @param size the diameter of the dot (in pixels)
     * @param color the color of the dot
     */
    public DrawableDot(Point pt, int size, DemoColor color) {
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
        g.fillOval((int) Math.round(point_.coords[0] - size_/2),
                   (int) Math.round(-point_.coords[1] - size_/2),
                   size_, size_);
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
        double angle = 0;
        int numArcs = (int) Math.ceil(Math.log((double) size_) * 8);
        double angleIncr = 2 * Math.PI / (double) numArcs;
//        int[] xcoords = new int[numArcs+1], ycoords = new int[numArcs+1], zcoords = new int[numArcs+1];
        int ptx =  (int) Math.round(point_.coords[0]),
            pty = -(int) Math.round(point_.coords[1]),
            ptz =  (int) Math.round(point_.coords[2]);
        double radius = ((double) size_) / 2.0;
        int prevX = (int) Math.round(ptx + radius),
            prevY = pty;
        for (int i = 0; i < numArcs; ++i) {
            angle += angleIncr;
            int nextX = (int) Math.round( ptx + radius*Math.cos(angle) );
            int nextY = (int) Math.round( pty + radius*Math.sin(angle) );
            img.fillTriangle(ptx, pty, ptz,
                             prevX, prevY, ptz,
                             nextX, nextY, ptz,
                             color);
            prevX = nextX; prevY = nextY;
        }
        img.fillTriangle(ptx, pty, ptz,
                         prevX, prevY, ptz,
                         (int) Math.round(ptx + radius), pty, ptz,
                         color);
        /*
        for (int i = 0; i < numArcs; ++i) {
            angle += angleIncr;
            xcoords[i] = (int) Math.round( point_.coords[0] + radius*Math.cos(angle));
            ycoords[i] = (int) Math.round(-point_.coords[1] + radius*Math.sin(angle));
            zcoords[i] = zcoord;
        }
        xcoords[numArcs] = (int) Math.round( point_.coords[0] + radius);
        ycoords[numArcs] = (int) Math.round(-point_.coords[1]);
        zcoords[numArcs] = zcoord;
        img.fillPolygon(xcoords, ycoords, zcoords, color);
         */
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
