package demo.plot;

import mathbuild.value.*;

import demo.io.*;
import demo.gfx.*;
import demo.util.*;
import demo.depend.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.STEInterval;
import demo.gfx.drawable.PolygonLine;

/**
 * Plot for parameterized curves. The drawable objects produced are 
 * many line segments connecting points on the curve. There is a lne
 * segment for each subdivision in the interval the parameterized expression
 * is dependent on.
 *
 * @author deigen
 */
public class PlotCurve extends PlotExpression implements Dependable, FileObject {


    private  DemoColor[] colors_ = null;
    
    private  boolean drawThick_ = false;

    private PlotOutputArrays output_ = new PlotOutputArrays();


    /** 
     * Creates a new PlotCurve that is empty, whose expression and coloring 
     * will be proveded later.
     */
    public  PlotCurve(int dimension) {
        super(dimension);

            this .expression = null;
            this .coloring = null;
        }

    /**
     * Creates a new PlotCurve with the given coloring and no expression.
     * The expression should be supplied later.
     */
    public PlotCurve(Coloring coloring, int dimension) {
        super(dimension);
        this.expression = null;
        this.setColoring(coloring);
    }

    /** 
     * Creates a new PlotCurve whose parameterized expression is expression.
     * @param expression the expression representing the curve
     * @param coloring the coloring
     */
    public 
    PlotCurve( IntervalExpression expression, Coloring coloring, int dimension ) {
        super(dimension);

            this.setExpression(expression);
            this.setColoring(coloring);
        }

    /** 
     * Sets the expression for the curve to the given expression.
     * If expression uses more than one interval, just one of the
     * intervals will be used as the parameter.
     * @param expression the expression for the parameterized curve
     */
    public  void setExpression( IntervalExpression expression ) {
        super.setExpression(expression);
    }

    /**
     * Sets the coloring to the given coloring.
     * @param coloring the coloring to use
     */
    public  void setColoring( Coloring coloring ) {
        super.setColoring(coloring);
    }

    /**
     * @param state whether to produce polygons for thick lines.
     */
    public   void setDrawThick( boolean state ) {
        if ( state != this .drawThick_ ) {
            //update 
            this .drawThick_ = state;
        }
    }

    /**
     * @return whether polygons are being produced for thick lines (true) or normal lines (false)
     */
    public  boolean getDrawThick() {
        return drawThick_;
    }


    public  void calculatePlot() {
        if ( expression == null || coloring == null ) {
            output_.makeBufferPoints(0);
            output_.makeBufferLightingVectors(0);
            output_.makeBufferDrawables();
            output_.setOutput();
            return;
        }
        calculatePoints();
        calculateColors();
        calculateVectors();
        calculatePolygons();
        output_.setOutput();
    }

    /**
     * Calculates the PointSortables for the curve.
     */
    private  void calculatePoints() {
        STEInterval u = expression .sortedIntervals()[0];
        double resolution = u .resolution().number();
        int resInt = (int) Math .round( resolution );
        double min = u .min().number();
        double max = u .max().number();
        double incrementAmount = (max - min) / (double) resInt;
        PointSortable[] points = output_.makeBufferPoints(resInt+1);
        for ( int step = 0; step <= resInt; step++ ) {
            u.setValue(incrementAmount * step + min);
            points[step] = new PointSortable( ((ValueVector) expression .calculate()).doubleVals(),
                                              2,extraZ_ );
        }
    }

    /**
     * Calculates the colors for each polygon in the curve.
     */
    private  void calculateColors() {
        PointSortable[] points = output_.bufferPoints();
        coloring .setCache();
        STEInterval u = this .expression .sortedIntervals()[0];
        double resolution = u .resolution().number();
        int resInt = (int) Math .round( resolution );
        double min = u .min().number() + (u .max().number() - u .min().number()) / u .resolution().number() / 2;
        double max = u .max().number() - (u .max().number() - u .min().number()) / u .resolution().number() / 2;
        double incrementAmount = (max - min) / (double) resInt;
        if (colors_ == null || colors_.length != resInt)
            colors_ = new DemoColor [ resInt ];
        for ( int step = 0; step < resInt; step++ ) {
            u.setValue(incrementAmount * step + min);
            currPoint_ = points[step];
            colors_[step] = new DemoColor(coloring.calculate());
        }
    }

    /**
     * Calculates the tangent vectors for the curve based on the (already calculated) points.
     */
    private void calculateVectors() {
        PointSortable[] points = output_.bufferPoints();
        LightingVector[] lvecs = output_.makeBufferLightingVectors(points.length);
        int i;
        lvecs[0] = new TangentVector( M.normalize(M.sub(M.point(points[1]),
                                                        M.point(points[0]))) );
        for (i = 1; i < points.length - 1; ++i) {
            double[] p1 = M.point(points[i-1]);
            double[] p2 = M.point(points[i]);
            double[] p3 = M.point(points[i+1]);
            // can use addition for average because we're normalzing
            lvecs[i] = new TangentVector( M.normalize(M.add(M.sub(p2, p1),
                                                            M.sub(p3, p2))) );
        }
        lvecs[i] = new TangentVector( M.normalize(M.sub(M.point(points[i]),
                                                        M.point(points[i-1]))) );
    }

    /**
     * Calculates the polygons for the curve based on points and colors that were already calculated.
     */
    private  void calculatePolygons() {
        LinkedList linesList = output_.makeBufferDrawables();
        PointSortable[] points = output_.bufferPoints();
        LightingVector[] tanvecs = output_.bufferLightingVectors();
        for ( int i = 0; i < colors_ .length; i++ ) {
            if (colors_[i].alpha > transparencyThreshold_) {
                // TODO: 2 coloring modes (w/ interpolation and without)
                PolygonLine line = new PolygonLine( points[i], points[i + 1],
                                                    colors_[i], colors_[i],
                                                    tanvecs[i], tanvecs[i + 1]);
                line .setDrawThick( drawThick_ );
                linesList .add( line );
            }
        }
    }

    public PlotOutput output() {
        return output_;
    }
    
    public String title() {
        if (expression ==  null)
            return "Curve: unspecified";
        return "Curve: " + expression.definitionString();
    }

    



    // ****************************** FILE I/O ****************************** //

    public PlotCurve(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        drawThick_ = parser.parseBoolean(parser.parseProperties(tok).get("thick"));
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("thick", generator.generateBoolean(drawThick_));
        return generator.generateProperties(props);
    }

    
}


