//
//  PlotPoint.java
//  Demo
//
//  Created by David Eigen on Mon Jul 22 2002.
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

public class PlotPoint extends Plot implements RayIntersectable, Dependable, FileObject {

    /**
     * Constants for style.
     */
    public static final int
    DOT = 0, CROSS = 1, SPHERE = 2, NONE = 3;
    
    private int style_ = DOT;
    private double diameter_; // size of the dot (diameter)

    private Expression pointExpr_;
    private Coloring coloring_ = null;
    private boolean useLabel_ = false;
    private String label_ = "";

    private PointSortable point_ = null; // the point we're at (also in points_ array)
    private PlotOutputArrays output_ = new PlotOutputArrays();
    private int dimension_;

    /**
     * Creates a plot for a point.
     * @param Expression the expression for the point.
     * @param coloring the coloring for this PlotPoint
     * @param the dimension of the point (either 2 or 3)
     */
    public PlotPoint(Expression expr, Coloring coloring, int dimension) {
        pointExpr_ = expr;
        coloring_ = coloring;
        dimension_ = dimension;
        diameter_ = 10;
        style_ = DOT;
        init();
    }

    private void init() {
        DependencyManager.setDependency(this, coloring_);
        DependencyManager.setDependency(this, pointExpr_);
    }

    public void dispose() {
        super.dispose();
        coloring_.dispose();
        pointExpr_.dispose();
    }

    /**
     * Sets the style of the dot produced by this PlotPoint.
     */
    public void setStyle(int style) {
        style_ = style;
    }

    /**
     * Sets the size (diameter) of the dot produced by this PlotPoint.
     */
    public void setSize(double size) {
        diameter_ = size;
    }

    /**
     * Sets the expression for the point.
     */
    public void setExpression(Expression expr) {
        pointExpr_.dispose();
        pointExpr_ = expr;
        DependencyManager.setDependency(this, pointExpr_);
    }

    /**
     * Sets the coloring for the point.
     */
    public void setColoring(Coloring coloring) {
        DependencyManager.removeDependency(this, coloring_);
        coloring_.dispose();
        coloring_ = coloring;
        DependencyManager.setDependency(this, coloring_);
    }

    /**
     * Sets the label of this point (text drawn beside the point).
     */
    public void setLabel(String label) {
        label_ = label;
    }

    /**
     * Sets whether to show the label (whether to output the drawable object for the label).
     */
    public void setLabelVisible(boolean b) {
        useLabel_ = b;
    }

    /**
     * @return the expression for the point
     */
    public Expression expression() {
        return pointExpr_;
    }

    /**
     * @return the coloring for the point
     */
    public Coloring coloring() {
        return coloring_;
    }

    /**
     * @return the size (diameter) of the point in pixels
     */
    public double size() {
        return diameter_;
    }

    /**
     * @return the style of the point
     */
    public int style() {
        return style_;
    }

    /**
     * @return the label for the point (text drawn near the pt)
     */
    public String label() {
        return label_;
    }

    /**
     * @return whether the label is showing
     */
    public boolean labelIsVisible() {
        return useLabel_;
    }

    

    /**
     * Intersects a ray with this PlotPoint (that is, the pt this PlotPoint is
     * currently at), within the ray's tolerance.
     * If this is an intersection, this point's location is given as the
     * intersection w/ the ray, not the pt on the ray.
     * This method overrides the default Plot.intersect. This is so intersecting
     * will intersect with the point, and not with some shape being drawn around
     * the point.
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        // point in world-space
        double[] p_world = M.point(point_.untransformedCoords);
        // point in ray-space
        double[] p = ray.transf.transform(p_world);
        if (M.length(new double[]{p[0], p[1]}) < ray.tol) {
            intersection.set(ray, this, p[2], p_world);
            return true;
        }
        return false;
    }


    private void calculateDot(PointSortable pt, DemoColor color) {
        Drawable3D dot = new DrawableDot(pt,
                                         (int) Math.round(diameter_),
                                         color);
        output_.bufferDrawables().add(dot);
        PointSortable[] points = output_.makeBufferPoints(1);
        points[0] = pt;
        output_.makeBufferLightingVectors(0);
    }

    private void calculateCross(PointSortable pt, DemoColor color) {
        Drawable3D cross = new DrawableCross(pt,
                                             (int) Math.round(diameter_),
                                             color);
        output_.bufferDrawables().add(cross);
        PointSortable[] points = output_.makeBufferPoints(1);
        points[0] = pt;
        output_.makeBufferLightingVectors(0);
    }

    private void calculateSphere(PointSortable pt, DemoColor color) {
        tesselateSphere(pt, color);
    }
    
    private void tesselateSphere(PointSortable point, DemoColor color) {
        final double DIAMETER_SCALE = 0.007;
        int uSubdivs = 16, vSubdivs = 8;
        double uIncr = (2*Math.PI - 0.002) / (double) uSubdivs;
        double vIncr = Math.PI / (double) vSubdivs;
        double[] pt = M.point(point);
        double radius = ((double) diameter_) * DIAMETER_SCALE;
        PointSortable[] points = output_.makeBufferPoints((vSubdivs+1)*(uSubdivs+1)+1);
        LightingVector[] lvecs = output_.makeBufferLightingVectors(points.length - 1);
        LinkedList drawables = output_.bufferDrawables();
        points[points.length-1] = point;
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
        System.arraycopy(prevPts, 0, points, 0, prevPts.length);
        System.arraycopy(prevLVecs, 0, lvecs, 0, prevLVecs.length);
        for (int i = 0; i < uSubdivs; ++i) {
            u += uIncr;
            v = 0;
            PointSortable[] nextPts = new PointSortable[vSubdivs+1];
            LightingVector[] nextLVecs = new LightingVector[nextPts.length];
            for (int j = 0; j <= vSubdivs; ++j) {
                double[] r = M.vector(Math.cos(u)*Math.cos(v),
                                      Math.cos(u)*Math.sin(v),
                                      Math.sin(u));
                nextPts[j] = new PointSortable(M.add(pt, M.mult(radius, r)),
                                               6,extraZ_);
                nextLVecs[j] = new NormalVector(r);
                v += vIncr;
            }
            System.arraycopy(nextPts, 0, points, nextPts.length*(i+1), nextPts.length);
            System.arraycopy(nextLVecs, 0, lvecs, nextLVecs.length*(i+1), nextLVecs.length);
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

    private void calculateNone(PointSortable pt, DemoColor color) {
        PointSortable[] points = output_.makeBufferPoints(1);
        points[0] = pt;
        output_.makeBufferLightingVectors(0);
    }
    

    // ** Methods from Plot ** //
    public  void calculatePlot() {
        LinkedList drawables = output_.makeBufferDrawables();
        coloring_.setCache();
        DemoColor color = new DemoColor(coloring_.calculate());
        if (color.alpha <= transparencyThreshold_) {
            output_.makeBufferPoints(0);
            return;
        }
        PointSortable pt = point_ = new PointSortable((ValueVector) pointExpr_.evaluate(),
                                                      1,extraZ_);
        switch (style_) {
            case DOT:
                calculateDot(pt, color);
                break;
            case CROSS:
                calculateCross(pt, color);
                break;
            case SPHERE:
                calculateSphere(pt, color);
                break;
            case NONE:
                calculateNone(pt, color);
                break;
            default:
                calculateDot(pt, color);
                break;
        }
        if (useLabel_) {
            Drawable3D text = new TextDrawable(label_, pt, color);
            drawables.add(text);
        }
        output_.setOutput();
    }

    public PlotOutput output() {
        return output_;
    }
    
    public  String title() {
        return "Point: " + pointExpr_.definitionString();
    }



    // ****************************** FILE I/O ****************************** //
    private String coloring__, pointExpr__;
    
    public PlotPoint(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        pointExpr__ = parser.parseExpression(props.get("expr"));
        coloring__ = parser.parseObject(props.get("color"));
        style_ = (int) parser.parseNumber(props.get("style"));
        diameter_ = parser.parseNumber(props.get("size"));
        dimension_ = (int) parser.parseNumber(props.get("dim"));
        label_ = parser.parseWord(props.get("label"));
        useLabel_ = parser.parseBoolean(props.get("showlabel"));
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
        coloring_ = (Coloring) parser.getObject(coloring__);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        pointExpr_ = parser.recognizeExpression(pointExpr__);
        init();
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        parser.loadExprs(coloring_);
        parser.popEnvironment();
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("expr", generator.generateExpression(pointExpr_));
        props.add("color", generator.generateObject(coloring_));
        props.add("style", generator.generateNumber(style_));
        props.add("size", generator.generateNumber(diameter_));
        props.add("dim", generator.generateNumber(dimension_));
        props.add("label", generator.generateWord(label_));
        props.add("showlabel", generator.generateBoolean(useLabel_));
        return generator.generateProperties(props);
    }    
    
}
