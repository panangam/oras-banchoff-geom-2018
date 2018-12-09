//
//  PlotLevelSet.java
//  Demo
//
//  Created by David Eigen on Mon Mar 24 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import mathbuild.value.*;
import mathbuild.type.*;
import mathbuild.Executor;
import mathbuild.Operator;
import mathbuild.MB;

import demo.io.*;
import demo.gfx.*;
import demo.gfx.drawable.*;
import demo.util.*;
import demo.depend.*;
import demo.Demo;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.IncompatibleTypeException;
import demo.expr.ste.STEConstant;

public class PlotLevelSet extends Plot {

    /**
     * Coloring modes: COLORING means use the coloring,
     * PLOT means keep color the same as the intersected polygon.
     */
    public static final int COLORING = 0, PLOT = 1;

    private Plot plot_;
    private Expression planeNormal_;
    private Expression planePoint_;
    private boolean drawThick_;
    private int coloringMode_;
    private Coloring coloring_;
    private Expression pointTransformation_;
    private Expression filterExpr_;

    private Polygon currPolygon_ = new PolygonPoint(new PointSortable(M.point(0,0,0)),
                                                    new DemoColor(0,0,0,0));

    private PlotOutputLists output_ = new PlotOutputLists();

    private static final String POINT_ENTRY_NAME = "Point";
    private STEConstant pointTableEntry_ = new STEConstant(POINT_ENTRY_NAME,
                                                           new ValueVector(new double[]{0,0,0}));
    
    public PlotLevelSet() {
        plot_ = new PlotNull();
        makeObjectTableEntry();
        planeNormal_ = Demo.recognizeExpression("0,0,1", mathbuild.Environment.EMPTY);
        planePoint_ = Demo.recognizeExpression("0,0,0", mathbuild.Environment.EMPTY);
        pointTransformation_ = Demo.recognizeExpression(POINT_ENTRY_NAME,
                                                        this.expressionDefinitions());
        filterExpr_ = Demo.recognizeExpression("true", mathbuild.Init.baseEnvironment());
        coloringMode_ = PLOT;
        coloring_ = null;
        drawThick_ = false;
    }

    public int currPolygonVertices() {
        return currPolygon_.points.length;
    }

    public Point currPolygonVertex(int i) {
        if (i < 0) i = 0;
        if (i >= currPolygon_.points.length) i = currPolygon_.points.length - 1;
        return currPolygon_.points[i];
    }

    public class ExePlot implements mathbuild.Executor {
        public Type type() {
            return plot_.objectTableEntry().type();
        }
        public Value execute(Object runID) {
            return plot_.objectTableEntry().execute(runID);
        }
    }

    private boolean __madeTableEntry__ = false;
    public void makeObjectTableEntry() {
        if (__madeTableEntry__) return; __madeTableEntry__ = true;
        plot_.makeObjectTableEntry();
        plotEntry_.addMember("NumPolyVertices", this, "int currPolygonVertices()");
        plotEntry_.addMember("PolyVertex", this, "Point currPolygonVertex(index)");
        plotEntry_.addMember("Plot", new ExePlot());
        this.expressionDefinitions().put(POINT_ENTRY_NAME, pointTableEntry_);
    }

    /**
     * Sets the plot to take the level set of.
     */
    public void setPlot(Plot plot) {
        DependencyManager.removeDependency(this, plot_);
        DependencyManager.setDependency(this, plot);
        plot_ = plot;
    }

    /**
     * Sets the upward-facing normal vector of the plane to
     * intersect with.
     */
    public void setPlaneNormal(Expression normalExpr) {
        planeNormal_.dispose();
        planeNormal_ = normalExpr;
        DependencyManager.setDependency(this, planeNormal_);
    }

    /**
     * Sets an expression for some point on the slicing plane.
     */
    public void setPlanePoint(Expression pointExpr) {
        planePoint_.dispose();
        planePoint_ = pointExpr;
        DependencyManager.setDependency(this, planePoint_);
    }

    /**
     * Sets whether to draw thick lines.
     */
    public void setDrawThick(boolean b) {
        drawThick_ = b;
    }

    /**
     * Sets the coloring to use (if in use-coloring mode)
     */
    public void setColoring(Coloring c) {
        if (coloring_ != null) coloring_.dispose();
        DependencyManager.setDependency(this, c);
        coloring_ = c;
    }

    /**
     * Sets the coloring mode to use (COLORING or PLOT)
     */ 
    public void setColoringMode(int mode) {
        if (mode == PLOT)
            DependencyManager.removeDependency(this, coloring_);
        else if (mode == COLORING && coloring_ != null)
            DependencyManager.setDependency(this, coloring_);
        coloringMode_ = mode;
    }

    /**
     * Sets the "point transformation". This is an expression that should return a
     * 3D or 2D vector (depending on the dimension of this plot). For each point on
     * the calculated level set, this transformation is applied to get the result of
     * this plot. That is, this transformation is applied to all points on the level
     * to produce the final output. The given expression is usually a linear
     * transformation or projection of the untransformed point, called "Point" in
     * the environment this.expressionDefinitions().
     */
    public void setPointTransformation(Expression expr) throws IncompatibleTypeException {
        if (!expr.returnsVector(3))
            throw new IncompatibleTypeException("Transformation must be a vector of dimension 3.");
        expr.setTypeChangeAllowed(false);
        pointTransformation_.dispose();
        pointTransformation_ = expr;
        DependencyManager.setDependency(this, pointTransformation_);
    }

    /**
     * Sets the "filter expression". For each polygon in the level set, this expression
     * is evaluated. If its result is true (> 0), the polygon is included in the final
     * output of this plot. If its result is false, the polygon is not included.
     */
    public void setFilter(Expression expr) throws IncompatibleTypeException {
        if (!expr.returnsScalar())
            throw new IncompatibleTypeException("Filter expression must be a scalar.");
        expr.setTypeChangeAllowed(false);
        filterExpr_.dispose();
        filterExpr_ = expr;
        DependencyManager.setDependency(this, filterExpr_);
    }
    
    /**
     * @return the upward-facing normal vector of the plane to
     * intersect with.
     */
    public Expression planeNormal() {
        return planeNormal_;
    }

    /**
     * @return the expression for some point on the slicing plane.
     */
    public Expression planePoint() {
        return planePoint_;
    }

    /**
     * @return the plot being sliced.
     */
    public Plot plot() {
        return plot_;
    }

    /**
     * @return whether to draw thick lines.
     */
    public boolean drawThick() {
        return drawThick_;
    }

    /**
     * @return the coloring being used (if in use-coloring mode)
     */
    public Coloring coloring() {
        return coloring_;
    }

    /**
     * @return the current coloring mode (COLORING or PLOT)
     */
    public int coloringMode() {
        return coloringMode_;
    }

    /**
     * @return the point transformation, as described in setPointTransformation(.).
     */
    public Expression pointTransformation() {
        return pointTransformation_;
    }

    /**
     * @return the filter expression, as described in setFilter(.).
     */
    public Expression filter() {
        return filterExpr_;
    }

    

    public void calculatePlot() {
        LinkedList points = output_.makeBufferPoints();
        LinkedList drawables = output_.makeBufferDrawables();
        LinkedList lvecs = output_.makeBufferLightingVectors();
        double[] planeNormal = M.vector((mathbuild.value.ValueVector) planeNormal_.evaluate());
        if (M.equal(planeNormal, M.vector(0,0,0)))
            return;
        planeNormal = M.normalize(planeNormal);
        double[] planePt = M.point((mathbuild.value.ValueVector) planePoint_.evaluate());
        plot_.ensureCalculated();
        DemoColor color = null;
        // matrix to rotate st plane normal goes to (0,0,1),
        // and translate st plane point goes to origin
        Matrix4D mat = M.mat_zaxis_transf(planeNormal, planePt).inverse();
        java.util.Enumeration plotDrawables = plot_.output().outputDrawableObjects().elements();
        if (coloringMode_ == COLORING)
            coloring_.setCache();
        while (plotDrawables.hasMoreElements()) {
            Object d = plotDrawables.nextElement();
            if (d instanceof Polygon) {
                ((Polygon) d).planeIntersect(mat, points, drawables, lvecs);
            }
        }
        for (java.util.Enumeration ds = drawables.elements(); ds.hasMoreElements();) {
            Polygon p = (Polygon) ds.nextElement();
            if (!(p instanceof Drawable3D))
                throw new RuntimeException("INTERNAL ERROR: Plane intersection not drawable");
            if (p instanceof PolygonLine)
                ((PolygonLine) p).setDrawThick(drawThick_);
        }
        for (java.util.Enumeration ps = points.elements(); ps.hasMoreElements();) {
            PointSortable p = (PointSortable) ps.nextElement();
            if (p.objects().size() < 1)
                currPolygon_ = new PolygonPoint(p, new DemoColor(0,0,0,1));
            else
                currPolygon_ = (Polygon) p.objects().elementAt(0);
            // apply the "point-transformation"
            pointTableEntry_.setValue(new ValueVector(p.untransformedCoords));
            ValueVector v = (ValueVector) pointTransformation_.evaluate();
            p.untransformedCoords = M.point(v);
            // evaluate the "filter"; don't include if it's false
            if (!((ValueScalar) filterExpr_.evaluate()).booleanValue()) {
                for (int i = 0; i < p.objects().size(); ++i)
                    drawables.remove(p.objects().elementAt(i));
            }
            else {
                // set color. Note that "Point" is already set to some vertex of the polygon
                if (coloringMode_ == COLORING) {
                    DemoColor c = new DemoColor(coloring_.calculate());
                    for (int i = 0; i < p.objects().size(); ++i)
                        ((Drawable3D) p.objects().elementAt(i)).setColor(c);
                }
            }
        }
        output_.setOutput();
    }

    public PlotOutput output() {
        return output_;
    }
    
    public String title() {
        return "Level Set: " + U.clampString(plot_.title(), 32)
                             + " in plane " + planePoint_.definitionString()
                                    + " ; " + planeNormal_.definitionString();
    }

    public void dispose() {
        super.dispose();
        planeNormal_.dispose();
        planePoint_.dispose();
        if (coloring_ != null) coloring_.dispose();
        pointTransformation_.dispose();
        filterExpr_.dispose();
    }


    // ****************************** FILE I/O ****************************** //

    private String coloring__ = null, plot__,
        planeNormal__, planePoint__, pointTransformation__, filterExpr__;
    
    public PlotLevelSet(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        drawThick_ = parser.parseBoolean(props.get("thick"));
        planeNormal__ = parser.parseExpression(props.get("nml"));
        planePoint__ = parser.parseExpression(props.get("pt"));
        plot__ = parser.parseObject(props.get("plot"));
        pointTransformation__ = parser.parseExpression(props.get("transf"));
        filterExpr__ = parser.parseExpression(props.get("fil"));
        coloringMode_ = (int) parser.parseNumber(props.get("cmode"));
        if (coloringMode_ == COLORING)
            coloring__ = parser.parseObject(props.get("coloring"));
    }

    public void loadFileBind(FileParser parser) {
        if (coloring__ != null) {
            coloring_ = (Coloring) parser.getObject(coloring__);
            DependencyManager.setDependency(this, coloring_);
        }
        else coloring_ = null;
        plot_ = (Plot) parser.getObject(plot__);
        DependencyManager.setDependency(this, plot_);
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        planeNormal_ = parser.recognizeExpression(planeNormal__);
        planePoint_ = parser.recognizeExpression(planePoint__);
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        pointTransformation_ = parser.recognizeExpression(pointTransformation__);
        filterExpr_ = parser.recognizeExpression(filterExpr__);
        parser.loadExprs(coloring_);
        parser.popEnvironment();
        DependencyManager.setDependency(this, planeNormal_);
        DependencyManager.setDependency(this, planePoint_);
        DependencyManager.setDependency(this, pointTransformation_);
        DependencyManager.setDependency(this, filterExpr_);
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("thick", generator.generateBoolean(drawThick_));
        props.add("nml", generator.generateExpression(planeNormal_));
        props.add("pt", generator.generateExpression(planePoint_));
        props.add("cmode", generator.generateNumber(coloringMode_));
        props.add("transf", generator.generateExpression(pointTransformation_));
        props.add("fil", generator.generateExpression(filterExpr_));
        if (coloringMode_ == COLORING)
            props.add("coloring", generator.generateObject(coloring_));
        if (plot_ instanceof PlotNull)
            props.add("plot", generator.generateObject(plot_));
        else
            props.add("plot", generator.generateObjectID(plot_));
        return generator.generateProperties(props);
    }
    
    
}
