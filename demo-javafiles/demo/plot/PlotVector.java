//
//  PlotVector.java
//  Demo
//
//  Created by David Eigen on Thu Jul 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import mathbuild.value.*;

import demo.io.*;
import demo.gfx.*;
import demo.gfx.drawable.*;
import demo.util.*;
import demo.depend.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;

public class PlotVector extends Plot implements FileObject {

    
    /**
     * Constants for endpoint style.
     */
    public static final int
    NONE = 0, ARROW_FORWARDS = 1, ARROW_BACKWARDS = 2, DOT = 3, SPHERE = 4,
    CONE_FORWARDS = 5, CONE_BACKWARDS = 6;
    
    private int baseStyle_ = NONE;
    private int endStyle_ = NONE;
    private double baseSize_ = 10, endSize_ = 10;
    private double length_ = 1; // multiply dir vector by this to scale
    private boolean drawThick_ = false;
    private int lineSubdivs_ = 1;
    
    private Expression baseExpr_, dirExpr_;
    private Coloring coloring_ = null;

    private PlotOutputLists output_ = new PlotOutputLists();
    private int dimension_;

    /**
     * Creates a plot for a vector.
     * @param baseExpr the expression for the base point
     * @param dirExpr the expression for the vector
     * @param coloring the coloring for this PlotPoint
     * @param the dimension of the point (either 2 or 3)
     */
    public PlotVector(Expression baseExpr, Expression dirExpr, Coloring coloring, int dimension) {
        baseExpr_ = baseExpr;
        dirExpr_ = dirExpr;
        coloring_ = coloring;
        dimension_ = dimension;
        endStyle_ = ARROW_FORWARDS;
        init();
    }

    private void init() {
        DependencyManager.setDependency(this, coloring_);
        DependencyManager.setDependency(this, baseExpr_);
        DependencyManager.setDependency(this, dirExpr_);
    }

    public void dispose() {
        super.dispose();
        DependencyManager.remove(this);
        baseExpr_.dispose();
        dirExpr_.dispose();
        coloring_.dispose();
    }
    
    /**
     * Sets the style of the base endpoint.
     */
    public void setBaseStyle(int style) {
        baseStyle_ = style;
    }

    /**
     * Sets the style of the ending endpoint.
     */
    public void setEndStyle(int style) {
        endStyle_ = style;
    }

    /**
     * Sets the size of the base point object (dot, arrow, etc).
     * Units are compatible w/ units for PlotPoint size
     */
    public void setBaseSize(double size) {
        baseSize_ = size;
    }

    /**
     * Sets the size of the end point object (dot, arrow, etc).
     * Units are compatible w/ units for PlotPoint size
     */
    public void setEndSize(double size) {
        endSize_ = size;
    }

    /**
     * Sets the size (length) of the line produced by this PlotVector.
     * The length of the line is size * (length of vector from direction expression)
     */
    public void setLength(double size) {
        length_ = size;
    }

    /**
     * Sets whether the line part of the vector is drawn thick.
     */
    public void setDrawThick(boolean b) {
        drawThick_ = b;
    }

    /**
     * Sets the number of subdivisions made in the line segment of the vector.
     */
    public void setLineSubdivisions(int subdivs) {
        lineSubdivs_ = subdivs;
    }

    /**
     * Sets the expression for the base point.
     */
    public void setBaseExpression(Expression expr) {
        baseExpr_.dispose();
        baseExpr_ = expr;
        DependencyManager.setDependency(this, baseExpr_);
    }

    /**
     * Sets the expression for the direction.
     */
    public void setDirExpression(Expression expr) {
        dirExpr_.dispose();
        dirExpr_ = expr;
        DependencyManager.setDependency(this, dirExpr_);
    }

    /**
     * Sets the coloring for the vector.
     */
    public void setColoring(Coloring coloring) {
        DependencyManager.removeDependency(this, coloring_);
        coloring_.dispose();
        coloring_ = coloring;
        DependencyManager.setDependency(this, coloring_);
    }


    /**
     * @return the expression for the base point
     */
    public Expression baseExpression() {
        return baseExpr_;
    }

    /**
     * @return the expression for the direction
     */
    public Expression dirExpression() {
        return dirExpr_;
    }

    /**
     * @return the coloring for the vector
     */
    public Coloring coloring() {
        return coloring_;
    }

    /**
     * @return the size (length) multiplier of the vector
     */
    public double length() {
        return length_;
    }

    /**
     * @return whether the line part of the vector is drawn thick
     */
    public boolean drawThick() {
        return drawThick_;
    }

    /**
     * @return the number of subdivisions in the line segment part of the vector
     */
    public int lineSubdivisions() {
        return lineSubdivs_;
    }

    /**
     * @return the style of the base endpoint
     */
    public int baseStyle() {
        return baseStyle_;
    }

    /**
     * @return the style of the ending endpoint
     */
    public int endStyle() {
        return endStyle_;
    }

    /**
     * @return the size of the base point object (dot, arrow, etc).
     * Units are compatible w/ units for PlotPoint
     */
    public double baseSize() {
        return baseSize_;
    }

    /**
     * @return the size of the end point object (dot, arrow, etc).
     * Units are compatible w/ units for PlotPoint
     */
    public double endSize() {
        return endSize_;
    }

    
    

    private void calculateLine(PointSortable basePt, PointSortable endPt, DemoColor color) {
        LinkedList points = output_.bufferPoints();
        LinkedList drawables = output_.bufferDrawables();
        LinkedList lvecs = output_.bufferLightingVectors();
        PointSortable prevPt = basePt;
        double cIncr = 1.0 / (double) lineSubdivs_;
        double c = 1;
        TangentVector tanvec = new TangentVector(M.normalize(M.sub(M.point(endPt),
                                                                   M.point(basePt))));
        lvecs.add(tanvec);
        for (int i = 0; i < lineSubdivs_; ++i) {
            c -= cIncr;
            PointSortable nextPt = PointSortable.interpolate(basePt, endPt, c, 2);
            PolygonLine line = new PolygonLine(prevPt, nextPt,
                                               color, color,
                                               tanvec, tanvec);
            line.setDrawThick(drawThick_);
            drawables.add(line);
            points.add(nextPt);
            prevPt = nextPt;
        }
    }

    private void calculateCone(PointSortable point, double[] dir, double size, DemoColor color) {
        LinkedList points = output_.bufferPoints();
        LinkedList drawables = output_.bufferDrawables();
        LinkedList lvecs = output_.bufferLightingVectors();
        final double HEIGHT = size * 0.020, RADIUS = 1.0 / 2.6;
        final double DISC_SEPERATION = Math.min(0.003, HEIGHT * 0.02);
        // note: the radius of the cone is RADIUS*HEIGHT (RADIUS is proportional to HEIGHT)
        final int ures = 14, rres = 3, vres = 2;
        final double umin = 0, vmin = 0.001, rmin = 0.001;
        final double uincr = 2*Math.PI/(double)ures,
            rincr = (HEIGHT-rmin)/(double)rres,
            vincr = (RADIUS*HEIGHT-vmin)/(double)vres;
        // get e1,e2: basis normal to direction
        dir = M.normalize(M.neg(M.vector(dir)));
        double[] pt = M.point(point.untransformedCoords);
        double[] e1;
        if (Math.abs(dir[1]) < 1e-8 && Math.abs(dir[2]) < 1e-8)
            e1 = new double[]{0,1,0,0};
        else
            e1 = new double[]{1,0,0,0};
        e1 = M.normalize(M.sub(e1, M.mult(M.dot(e1, dir), dir)));
        double[] e2 = M.cross(dir, e1);
        // cone part
        {
            double u = umin;
            double r = rmin;
            PointSortable[] prevPts = new PointSortable[rres+1];
            double[] hypotenuseVec = M.add(dir, M.mult(RADIUS, e1));
            LightingVector prevLVec = new NormalVector(
                                M.normalize(M.add(M.mult(dir, M.dot(hypotenuseVec, e1)),
                                                  M.mult(e1,  M.dot(hypotenuseVec, M.neg(dir))))));
            lvecs.add(prevLVec);
            for (int j = 0; j <= rres; ++j) {
                double[] p = M.add(pt, M.mult(r, hypotenuseVec));
                prevPts[j] = new PointSortable(p, 6,extraZ_);
                r += rincr;
            }
            for (int copyi = 0; copyi < prevPts.length; ++copyi)
                points.add(prevPts[copyi]);
            double[] e1e2;
            for (int i = 0; i < ures; ++i) {
                u += uincr;
                r = rmin;
                PointSortable[] nextPts = new PointSortable[rres+1];
                LightingVector nextLVec = null;
                e1e2 = M.add(M.mult(Math.cos(u), e1), M.mult(Math.sin(u), e2));
                hypotenuseVec = M.add(dir, M.mult(RADIUS, e1e2));
                nextLVec = new NormalVector(
                                M.normalize(M.add(M.mult(dir,  M.dot(hypotenuseVec, e1e2)),
                                                  M.mult(e1e2, M.dot(hypotenuseVec, M.neg(dir))))));
                lvecs.add(nextLVec);
                for (int j = 0; j <= rres; ++j) {
                    double[] p = M.add(pt, M.mult(r, hypotenuseVec));
                    nextPts[j] = new PointSortable(p, 6,extraZ_);
                    r += rincr;
                }
                for (int copyi = 0; copyi < nextPts.length; ++copyi)
                    points.add(nextPts[copyi]);
                for (int p = 0; p < rres; ++p) {
                    PolygonTriangle tri1 = new PolygonTriangle(
                                                        prevPts[p], nextPts[p], nextPts[p+1],
                                                        color, color, color,
                                                        prevLVec, nextLVec, nextLVec,
                                                        false, false, false,
                                                        color);
                    PolygonTriangle tri2 = new PolygonTriangle(
                                                        prevPts[p], nextPts[p+1], prevPts[p+1],
                                                        color, color, color,
                                                        prevLVec, nextLVec, prevLVec,
                                                        false, false, false,
                                                        color);
                    drawables.add(tri1);
                    drawables.add(tri2);
                }
                prevPts = nextPts;
                prevLVec = nextLVec;
            }
        }
        // disc part
        {
            final double[] center = M.add(pt, M.mult(HEIGHT + DISC_SEPERATION, dir));
            double u = umin;
            double v = vmin;
            PointSortable[] prevPts = new PointSortable[vres+1];
            PointSortable centerPt = new PointSortable(center, ures*2,extraZ_);
            LightingVector lvec = new NormalVector(M.normalize(dir));
            lvecs.add(lvec);
            for (int j = 0; j <= vres; ++j) {
                double[] p = M.add(M.mult(v, e1),
                                           center);
                prevPts[j] = new PointSortable(p, 6,extraZ_);
                v += vincr;
            }
            for (int copyi = 0; copyi < prevPts.length; ++copyi)
                points.add(prevPts[copyi]);
            for (int i = 0; i < ures; ++i) {
                u += uincr;
                PointSortable[] nextPts = new PointSortable[vres+1];
                v = vmin;
                for (int j = 0; j <= vres; ++j) {
                    double[] p = M.add(M.mult(v*Math.cos(u), e1),
                                 M.add(M.mult(v*Math.sin(u), e2),
                                       center));
                    nextPts[j] = new PointSortable(p, 6,extraZ_);
                    v += vincr;
                }
                for (int copyi = 0; copyi < nextPts.length; ++copyi)
                    points.add(nextPts[copyi]);
                for (int p = 0; p < vres; ++p) {
                    PolygonTriangle tri1 = new PolygonTriangle(
                                                        prevPts[p], nextPts[p], nextPts[p+1],
                                                        color, color, color,
                                                        lvec, lvec, lvec,
                                                        false, false, false,
                                                        color);
                    PolygonTriangle tri2 = new PolygonTriangle(
                                                        prevPts[p], nextPts[p+1], prevPts[p+1],
                                                        color, color, color,
                                                        lvec, lvec, lvec,
                                                        false, false, false,
                                                        color);
                    drawables.add(tri1);
                    drawables.add(tri2);
                }
                prevPts = nextPts;
            }
        }
    }


    private void calculateSphere(PointSortable point, double size, DemoColor color) {
        LinkedList points = output_.bufferPoints();
        LinkedList drawables = output_.bufferDrawables();
        LinkedList lvecs = output_.bufferLightingVectors();
        final double radius = size * 0.007;
        final int uSubdivs = 16, vSubdivs = 8;
        final double uIncr = (2*Math.PI - 0.002) / (double) uSubdivs;
        final double vIncr = Math.PI / (double) vSubdivs;
        final double[] pt = M.point(point);
        double u = -Math.PI + 0.001, v = 0;
        PointSortable[] prevPts = new PointSortable[vSubdivs+1];
        LightingVector[] prevLVecs = new LightingVector[prevPts.length];
        for (int j = 0; j <= vSubdivs; ++j) {
            double[] r = M.vector(Math.cos(u)*Math.cos(v),
                                  Math.cos(u)*Math.sin(v),
                                  Math.sin(u));
            prevPts[j] = new PointSortable(M.add(pt, M.mult(radius, r)),
                                           6,extraZ_);
            prevLVecs[j] = new NormalVector(r);
            v += vIncr;
        }
        for (int copyi = 0; copyi < prevPts.length; ++copyi) {
            points.add(prevPts[copyi]);
            lvecs.add(prevLVecs[copyi]);
        }
        for (int i = 0; i < uSubdivs; ++i) {
            u += uIncr;
            PointSortable[] nextPts = new PointSortable[vSubdivs+1];
            LightingVector[] nextLVecs = new LightingVector[nextPts.length];
            v = 0;
            for (int j = 0; j <= vSubdivs; ++j) {
                double[] r = M.vector(Math.cos(u)*Math.cos(v),
                                      Math.cos(u)*Math.sin(v),
                                      Math.sin(u));
                nextPts[j] = new PointSortable(M.add(pt, M.mult(radius, r)),
                                               6,extraZ_);
                nextLVecs[j] = new NormalVector(r);
                v += vIncr;
            }
            for (int copyi = 0; copyi < nextPts.length; ++copyi) {
                points.add(nextPts[copyi]);
                lvecs.add(nextLVecs[copyi]);
            }
            for (int p = 0; p < vSubdivs; ++p) {
                PolygonTriangle tri1 = new PolygonTriangle(
                                                    prevPts[p], nextPts[p], nextPts[p+1],
                                                    color, color, color,
                                                    prevLVecs[p], nextLVecs[p], nextLVecs[p+1],
                                                    false, false, false,
                                                    color);
                PolygonTriangle tri2 = new PolygonTriangle(
                                                    prevPts[p], nextPts[p+1], prevPts[p+1],
                                                    color, color, color,
                                                    prevLVecs[p], nextLVecs[p+1], prevLVecs[p+1],
                                                    false, false, false,
                                                    color);
                drawables.add(tri1);
                drawables.add(tri2);
            }
            prevPts = nextPts;
            prevLVecs = nextLVecs;
        }
    }
    

    private void calculateDot(PointSortable pt, double size, DemoColor color) {
        LinkedList drawables = output_.bufferDrawables();
        drawables.add(new DrawableDot(pt, (int) size, color));
    }
    

    private void calculateArrow(PointSortable pt, double[] dir, double size, DemoColor color) {
        LinkedList points = output_.bufferPoints();
        LinkedList drawables = output_.bufferDrawables();
        dir = M.normalize(dir);
        PointSortable dirPt = new PointSortable(M.add(pt.untransformedCoords, dir),
                                                1,extraZ_);
        drawables.add(new DrawableArrowhead(pt, dirPt, size*2.0, size*2.0*0.65, color));
        points.add(dirPt);
    }
    
    

    // ** Methods from Plot ** //
    public  void calculatePlot() {
        LinkedList points = output_.makeBufferPoints();
        LinkedList drawables = output_.makeBufferDrawables();
        LinkedList lvecs = output_.makeBufferLightingVectors();
        coloring_.setCache();
        DemoColor color = new DemoColor(coloring_.calculate());
        if (color.alpha <= transparencyThreshold_)
            return;
        PointSortable basePt = new PointSortable((ValueVector) baseExpr_.evaluate(),
                                                 10,extraZ_);
        double[] dir = ((ValueVector) dirExpr_.evaluate()).doubleVals();
        if (dir.length == 2)
            dir = new double[]{dir[0], dir[1], 0};
        PointSortable endPt = new PointSortable(M.add(basePt.coords,
                                                              M.mult(length_, dir)),
                                                10,extraZ_);
        points.add(basePt);
        points.add(endPt);
        calculateLine(basePt, endPt, color);
        switch (baseStyle_) {
            case ARROW_FORWARDS:
                calculateArrow(basePt, dir, baseSize_, color);
                break;
            case ARROW_BACKWARDS:
                calculateArrow(basePt, M.neg(dir), baseSize_, color);
                break;
            case CONE_FORWARDS:
                calculateCone(basePt, dir, baseSize_, color);
                break;
            case CONE_BACKWARDS:
                calculateCone(basePt, M.neg(dir), baseSize_, color);
                break;
            case DOT:
                calculateDot(basePt, baseSize_, color);
                break;
            case SPHERE:
                calculateSphere(basePt, baseSize_, color);
                break;
            case NONE:
            default:
                break;
        }
        switch (endStyle_) {
            case ARROW_FORWARDS:
                calculateArrow(endPt, dir, endSize_, color);
                break;
            case ARROW_BACKWARDS:
                calculateArrow(endPt, M.neg(dir), endSize_, color);
                break;
            case CONE_FORWARDS:
                calculateCone(endPt, dir, endSize_, color);
                break;
            case CONE_BACKWARDS:
                calculateCone(endPt, M.neg(dir), endSize_, color);
                break;
            case DOT:
                calculateDot(endPt, endSize_, color);
                break;
            case SPHERE:
                calculateSphere(endPt, endSize_, color);
                break;
            case NONE:
            default:
                break;
        }
        output_.setOutput();
    }

    public PlotOutput output() {
        return output_;
    }
    
    public  String title() {
        return "Vector: " + dirExpr_.definitionString() + "  starting at " + baseExpr_.definitionString();
    }



    // ****************************** FILE I/O ****************************** //
    private String coloring__, baseExpr__, dirExpr__;
    
    public PlotVector(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        baseExpr__ = parser.parseExpression(props.get("base"));
        dirExpr__ = parser.parseExpression(props.get("dir"));
        coloring__ = parser.parseObject(props.get("color"));
        baseStyle_ = (int) parser.parseNumber(props.get("basestyle"));
        endStyle_ = (int) parser.parseNumber(props.get("endstyle"));
        baseSize_ = parser.parseNumber(props.get("basesize"));
        endSize_ = parser.parseNumber(props.get("endsize"));
        length_ = parser.parseNumber(props.get("len"));
        drawThick_ = parser.parseBoolean(props.get("thick"));
        dimension_ = (int) parser.parseNumber(props.get("dim"));
        lineSubdivs_ = (int) parser.parseNumber(props.get("subdivs"));
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
        coloring_ = (Coloring) parser.getObject(coloring__);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        baseExpr_ = parser.recognizeExpression(baseExpr__);
        dirExpr_ = parser.recognizeExpression(dirExpr__);
        init();
        parser.loadExprs(coloring_);
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("base", generator.generateExpression(baseExpr_));
        props.add("dir", generator.generateExpression(dirExpr_));
        props.add("color", generator.generateObject(coloring_));
        props.add("basestyle", generator.generateNumber(baseStyle_));
        props.add("endstyle", generator.generateNumber(endStyle_));
        props.add("basesize", generator.generateNumber(baseSize_));
        props.add("endsize", generator.generateNumber(endSize_));
        props.add("len", generator.generateNumber(length_));
        props.add("thick", generator.generateBoolean(drawThick_));
        props.add("subdivs", generator.generateNumber(lineSubdivs_));
        props.add("dim", generator.generateNumber(dimension_));
        return generator.generateProperties(props);
    }    
    
}





