package demo.plot;

import mathbuild.value.*;

import demo.io.*;
import demo.gfx.*;
import demo.util.*;
import demo.depend.*;
import demo.exec.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.STEInterval;
import demo.expr.ste.STEInterval;
import demo.gfx.drawable.PolygonPoint;
import demo.gfx.drawable.PolygonLine;

/**
 * Plot for general parameterized things. The drawable objects produced are 
 * many points, or line segments connecting intervals. The parameterization
 * can have any number of intervals.
 *
 * @author deigen
 */
public class PlotWireframe extends PlotExpression implements Dependable, FileObject {

    private  STEInterval[] intervals = null;

    private  boolean[] connectIntervals = null;

    private PlotWireframeOutput output = new PlotWireframeOutput();
    
    /** 
     * Creates a new PlotWireframe that is empty, whose expression and coloring 
     * will be proveded later.
     */
    public PlotWireframe(int dimension) {
        super(dimension);

            this .expression = null;
            this .coloring = null;
        }

    /**
     * Creates a new PlotWireframe with the given coloring and no expression.
     * The expression should be supplied later.
     */
    public PlotWireframe(Coloring coloring, int dimension) {
        super(dimension);
        this.expression = null;
        this.setColoring(coloring);
    }

    /** 
     * Creates a new PlotWireframe whose parameterized expression is expression.
     * @param expression the expression representing the wireframe
     * @param coloring the coloring
     */
    public 
    PlotWireframe( IntervalExpression expression, Coloring coloring, int dimension ) {
        super(dimension);

            this.setExpression(expression);
            this.setColoring(coloring);
        }

    /** 
     * Sets the expression for the curve to the given expression.
     * @param expression the expression for the parameterized wireframe
     */
    public  void setExpression( IntervalExpression expression ) {
        super.setExpression(expression);
        findIntervals();
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
        if ( state != this .drawThick ) {
            //update 
            this .drawThick = state;
        }
    }

    /**
     * @return whether polygons are being produced for thick lines (true) or normal lines (false)
     */
    public  boolean getDrawThick() {
        return drawThick;
    }

    private  boolean drawThick = false;

    public  void calculatePlot() {
        if ( expression == null || coloring == null ) {
            output.makeBufferPoints(0);
            output.makeBufferColors(0);
            output.makeBufferLightingVectors(0);
            output.makeBufferDrawables();
            findIntervals();
            output.setOutput();
            return;
        }
        if ( intervals == null || connectIntervals == null ) {
            findIntervals();
        }
        calculatePoints();
        calculateColors();
        calculatePolygons();
        output.setOutput();
    }

    /**
     * Calculates the PointSortables for the wireframe.
     */
    private  void calculatePoints() {
        // check the point case
        if ( intervals .length < 1 ) {
            Value value = expression .calculate();
            if ( value instanceof ValueVector ) {
                Object[] points = output.makeBufferPoints(1);
                points[0] = new PointSortable( (ValueVector) value, 0, extraZ_ );
            }
            else if ( value instanceof ValueScalar ) {
                Object[] points = output.makeBufferPoints(1);
                points[0] = new PointSortable( new double []{ ((ValueScalar) value).number() },
                                               0, extraZ_ );
            }
            return ;
        }
        // get the array size and increment of the first level
        double min = intervals[0] .min().number();
        double max = intervals[0] .max().number();
        double res = intervals[0] .resolution().number();
        int resInt = (int) Math .round( res );
        double incrementAmount = (max - min) / (double) resInt;
        Object[] points = output.makeBufferPoints(resInt + 1);
        this .intervals[0].setValue(min);
        // the calculate points method assumes that the current interval is init.ed at the min value.
        calculatePoints( points, this .intervals, incrementAmount, 0 );
    }

    private 
    void calculatePoints( Object[] array, STEInterval[] intervals,
                             double currIncrementAmount, int currInterval ) {
        // if we're not at the bottom level of the array, recurse to the next dimension
        int nextInterval = currInterval + 1;
        if ( nextInterval < intervals .length ) {
            for ( int step = 0; step < array .length; step++ ) {
                // the value of this interval changed, so we need to recalculate the
                // value of the next, possibly dependent, interval.
                double nextMin = intervals[nextInterval] .min().number();
                double nextMax = intervals[nextInterval] .max().number();
                double nextRes = intervals[nextInterval] .resolution().number();
                int nextResInt = (int) Math .round( nextRes );
                double nextIncrementAmount = (nextMax - nextMin) / (double) nextResInt;
                intervals[nextInterval].setValue(nextMin);
                output.makeRecursiveBuffer(array, step, nextResInt + 1);
                calculatePoints( (Object[]) array[step], intervals,
                                       nextIncrementAmount, nextInterval );
                intervals[currInterval].setValue(((ValueScalar) intervals[currInterval].value()).number()
                                                 + currIncrementAmount);
            }
        }
        else {
            // we're at the bottom level. So calculate.
            intervals[currInterval].setValue(intervals[currInterval] .min());
            for ( int step = 0; step < array .length; step++ ) {
                array[step] = new PointSortable( (ValueVector) expression .calculate(),
                                                 intervals .length * 2,
                                                 extraZ_ );
                intervals[currInterval].setValue(((ValueScalar) intervals[currInterval].value()).number()
                                                 + currIncrementAmount);
            }
        }
    }

    /**
     * Calculates the colors for each polygon in the wireframe.
     */
    private  void calculateColors() {
        coloring .setCache();
        // check the point case
        if ( intervals .length < 1 ) {
            double[] color = coloring .calculate();
            Object[] colors = output.makeBufferColors(1);
            colors[0] = new DemoColor(color);
            return ;
        }
        // get the array size and increment of the first level
        double min = intervals[0] .min().number();
        double max = intervals[0] .max().number();
        double res = intervals[0] .resolution().number();
        int resInt = (int) Math .round( res ) + 1;
        double incrementAmount = (max - min) / (double) resInt;
        // the array should have at least one color.
        Object[] colors = output.makeBufferColors(resInt > 0 ? resInt : 1);
        this .intervals[0].setValue(min);
        // the calculate colors method assumes that the current interval is init.ed at the min value.
        calculateColors( colors, output.bufferPoints(), this .intervals, incrementAmount, 0 );
    }

    private 
    void calculateColors( Object[] array, Object[] pointsArray, STEInterval[] intervals,
                             double currIncrementAmount, int currInterval ) {
        // if we're not at the bottom level of the array, recurse to the next dimension
        int nextInterval = currInterval + 1;
        if ( nextInterval < intervals .length ) {
            for ( int step = 0; step < array .length; step++ ) {
                double nextMin = intervals[nextInterval] .min().number();
                double nextMax = intervals[nextInterval] .max().number();
                double nextRes = intervals[nextInterval] .resolution().number();
                int nextResInt = (int) Math .round( nextRes ) + 1;
                double nextIncrementAmount = (nextMax - nextMin) / (double) nextResInt;
                intervals[nextInterval].setValue(nextMin);
                // make the array have at least one color.
                output.makeRecursiveBuffer(array, step, nextResInt > 0 ? nextResInt : 1);
                calculateColors( (Object[]) array[step], (Object[]) pointsArray[step], intervals,
                                       nextIncrementAmount, nextInterval );
                intervals[currInterval].setValue(((ValueScalar) intervals[currInterval].value()).number()
                                                 + currIncrementAmount);
            }
        }
        else {
            // we're at the bottom level. So calculate.
            intervals[currInterval].setValue(intervals[currInterval] .min());
            for ( int step = 0; step < array .length; step++ ) {
                currPoint_ = (Point) pointsArray[step];
                array[step] = new DemoColor(coloring.calculate());
                intervals[currInterval].setValue(((ValueScalar) intervals[currInterval].value()).number()
                                                 + currIncrementAmount);
            }
        }
    }

    /**
     * Calculates the polygons for the wireframe based on points and colors that were already calculated.
     */
    private  void calculatePolygons() {
        Object[] points = output.bufferPoints();
        Object[] colors = output.bufferColors();
        LinkedList linesList = output.makeBufferDrawables();
        // check the point case
        if ( intervals .length < 1 ) {
            if (((DemoColor) colors[0]).alpha > transparencyThreshold_) {
                PolygonPoint point = new PolygonPoint( (PointSortable) points[0],
                                                       (DemoColor) colors[0] );
                point .setDrawThick( drawThick );
                linesList .add( point );
            }
            return ;
        }
        // call recursive calculatePolygons on first level
        int[] intervalIndices = new int [ intervals .length ];
        intervalIndices[0] = 0;
        calculatePolygons( points, colors, intervalIndices, 0 );
    }

    private 
    void calculatePolygons( Object[] currPointsArray,
                            Object[] currColorsArray,
                            int[] intervalIndices, int currInterval ) {
        LinkedList linesList = output.bufferDrawables();
        LinkedList lvecs = output.bufferLightingVectors();
        if ( currInterval < this .intervals .length - 1 ) {
            // recurse to the next level
            // check if there's only one spot in the domain
            if ( currPointsArray .length == 1 ) {
                intervalIndices[currInterval] = 0;
                calculatePolygons( (Object[]) currPointsArray[0],
                                         (Object[]) currColorsArray[0],
                                         intervalIndices, currInterval + 1 );
                return ;
            }
            for ( int i = 0; i < currPointsArray .length - 1; i++ ) {
                intervalIndices[currInterval] = i;
                calculatePolygons( (Object[]) currPointsArray[i],
                                         (Object[]) currColorsArray[i],
                                         intervalIndices, currInterval + 1 );
            }
            // do the last point (it make the one last line without which we'd get "frayed edges")
            intervalIndices[currInterval] = currPointsArray .length - 1;
            // use the color for interval between the last and second to last point for the last point as well.
            calculatePolygons( (Object[]) currPointsArray[currPointsArray .length - 1],
                                     (Object[]) currColorsArray[currColorsArray .length - 1],
                                     intervalIndices, currInterval + 1 );
        }
        else {
            // calculate polygons for this string of points
            for ( int point = 0; point < currPointsArray .length; point++ ) {
                intervalIndices[currInterval] = point;
                PointSortable currPoint = (PointSortable) currPointsArray[point];
                double[] mcurrpt = M.point(currPoint);
                DemoColor currColor = (DemoColor) currColorsArray[point < currColorsArray .length ? point : currColorsArray .length - 1];
                if (currColor.alpha <= transparencyThreshold_)
                    continue;
                // make lines with adjacent points
                boolean madeLine = false;
                // go one point adjacent for each interval
                for ( int i = 0; i < intervalIndices .length; i++ ) {
                    if ( connectIntervals[i] ) {
                        intervalIndices[i]++;
                        PointSortable adjacentPoint = getPoint( intervalIndices );
                        if ( adjacentPoint != null ) {
                            madeLine = true;
                            TangentVector tanvec = new TangentVector(
                                                M.normalize(M.sub(M.point(adjacentPoint),
                                                                  mcurrpt)));
                            lvecs.add(tanvec);
                            PolygonLine line = new PolygonLine( currPoint, adjacentPoint,
                                                                currColor, currColor,
                                                                tanvec, tanvec );
                            line .setDrawThick( drawThick );
                            linesList .add( line );
                        }
                        intervalIndices[i]--;
                    }
                }
                if ( ! madeLine ) {
                    // make a point
                    PolygonPoint polygon = new PolygonPoint( currPoint, currColor );
                    polygon .setDrawThick( drawThick );
                    linesList .add( polygon );
                }
            }
        }
    }

    private  PointSortable getPoint( int[] intervalIndices ) {
        // go down through the points array to get to the point
        try  {
            Object[] currArray = output.bufferPoints();
            for ( int i = 0; i < intervals .length - 1; i++ ) {
                currArray = (Object[]) currArray[intervalIndices[i]];
            }
            return (PointSortable) currArray[intervalIndices[intervalIndices .length - 1]];
        } catch( ArrayIndexOutOfBoundsException ex ) {
            return null;
        }
    }

    public 
    void setConnect( STEInterval interval, boolean connect ) {
        if ( this .intervals == null || this .connectIntervals == null ) {
            findIntervals();
        }
        // find the interval and set its connect
        // if the interval isn't there, that's OK. Just don't set anything.
        for ( int i = 0; i < intervals .length; i++ ) {
            if ( intervals[i] == interval ) {
                connectIntervals[i] = connect;
                break;
            }
        }
    }

    public  STEInterval[] intervals() {
        return intervals;
    }

    public  boolean[] connectIntervalValues() {
        return connectIntervals;
    }

    protected  void findIntervals() {
        if (this.expression == null) {
            Exec.begin_nocancel();
            this.intervals = new STEInterval[0];
            this.connectIntervals = new boolean[0];
            Exec.end_nocancel();
            return;
        }
        STEInterval[] oldIntervals = this .intervals;
        boolean[] oldConnects = this .connectIntervals;
        Exec.begin_nocancel();
        this .intervals = this .expression .sortedIntervals();
        this .connectIntervals = new boolean [ intervals .length ];
        if ( oldIntervals == null ) {
            // we didn't have any intervals before; so set all connects to true
            for ( int i = 0; i < this .connectIntervals .length; i++ ) {
                this .connectIntervals[i] = true;
            }
            return ;
        }
        // for each of the new intervals, see if it existed in the old intervals, and set the connect
        for ( int interval = 0; interval < this .intervals .length; interval++ ) {
            STEInterval currInterval = this .intervals[interval];
            this .connectIntervals[interval] = true;
            // see if it was in the old intervals
            for ( int i = 0; i < oldIntervals .length; i++ ) {
                if ( oldIntervals[i] == currInterval ) {
                    this .connectIntervals[interval] = oldConnects[i];
                    break;
                }
            }
        }
        Exec.end_nocancel();
    }

    public PlotOutput output() {
        return output;
    }

    public String title() {
        if (expression == null)
            return "Wireframe: unspecified";
        return "Wireframe: " + expression.definitionString();
    }

    public 
    void dependencyUpdateDef( Set updatingObjects ) {
        super.dependencyUpdateDef(updatingObjects);
        findIntervals();
    }



    // ****************************** FILE I/O ****************************** //
    private String[] intervalNames__;
    private STEInterval[] intervalVals__;
    private boolean[] connectVals__;
    
    public PlotWireframe(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        drawThick = parser.parseBoolean(props.get("thick"));
        intervalNames__ = parser.parseWordList(props.get("intervals"));
        connectVals__ = parser.parseBooleanList(props.get("connect"));
        if (intervalNames__.length != connectVals__.length)
            parser.error("number of intervals must be the same as the number of connect flags in wireframe");
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        intervalVals__ = new STEInterval[intervalNames__.length];
        for (int i = 0; i < intervalNames__.length; ++i)
            intervalVals__[i] = (STEInterval) parser.currEnvLookup(intervalNames__[i]);
    }

    public void loadFileFinish(FileParser parser) {
        for (int i = 0; i < intervalVals__.length; ++i) {
            setConnect(intervalVals__[i], connectVals__[i]);
            intervalVals__[i] = null;
        }
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("thick", generator.generateBoolean(drawThick));
        props.add("connect", generator.generateBooleanList(connectIntervals));
        TokenString str = new TokenString();
        for (int i = 0; i < intervals.length; ++i)
            str.add(generator.generateWord(intervals[i].name()));
        String[] intervalNames = new String[intervals.length];
        for (int i = 0; i < intervals.length; ++i)
            intervalNames[i] = intervals[i].name();
        props.add("intervals", generator.generateWordList(intervalNames));
        return generator.generateProperties(props);
    }

    
private class PlotWireframeOutput implements PlotOutput {

    private  Object[] bufPoints = null, outPoints = null;

    private  Object[] bufColors = new DemoColor[0];

    private  LinkedList bufLVecs = new LinkedList(), outLVecs = new LinkedList();

    private  LinkedList bufLinesList = new LinkedList(), outLinesList = new LinkedList();

    public Object[] makeBufferPoints(int length) {
        if (bufPoints == null || bufPoints.length != length)
            bufPoints = new Object[length];
        return bufPoints;
    }

    public LinkedList makeBufferLightingVectors(int length) {
        return bufLVecs;
    }

    public Object[] makeBufferColors(int length) {
        if (bufColors == null || bufColors.length != length)
            bufColors = new Object[length];
        return bufColors;
    }

    public LinkedList makeBufferDrawables() {
        return bufLinesList;
    }
    
    public Object[] bufferPoints() { return bufPoints; }

    public LinkedList bufferLightingVectors() { return bufLVecs; }

    public Object[] bufferColors() { return bufColors; }

    public LinkedList bufferDrawables() { return bufLinesList; }

    public Object[] makeRecursiveBuffer(Object[] prevArray, int index, int length) {
        if (  prevArray[index] == null ||
            !(prevArray[index] instanceof Object[]) ||
              ((Object[]) prevArray[index]).length != length)
            prevArray[index] = new Object[length];
        return (Object[]) prevArray[index];
    }

    public int numOutputPoints() {
        // handle single point case
        if ( this .outPoints .length == 1 && this .outPoints[0] instanceof PointSortable )
            return 1;
        // recursively add together the length of all point vectors
        return numOutputElements( this .outPoints, 0 );
    }

    private int numOutputElements( Object[] currArray, int currIntervalNumber ) {
        if ( currIntervalNumber == intervals .length - 1 )
            return currArray .length;
        int n = 0;
        for ( int i = 0; i < currArray .length; i++ )
            n += numOutputElements( (Object[]) currArray[i], currIntervalNumber + 1 );
        return n;
    }
    

    public int copyOutputPoints( PointSortable[] array, int startIndex ) {
        // handle single point case
        if ( this .outPoints .length == 1 && this .outPoints[0] instanceof PointSortable ) {
            array[startIndex] = (PointSortable) this .outPoints[0];
            return startIndex + 1;
        }
        return copyOutputElements( array, startIndex, this .outPoints, 0 );
    }

    private int copyOutputElements( Object[] outArray, int startIndex,
                                    Object[] currArray, int currIntervalNumber ) {
        // check if we're at the bottom
        if ( currIntervalNumber == intervals .length - 1 ) {
            int index = startIndex;
            for ( int i = 0; i < currArray .length; i++ ) {
                outArray[index] = currArray[i];
                index++;
            }
            return index;
        }
        // recur to the next level
        int nextIndex = startIndex;
        for ( int i = 0; i < currArray .length; i++ ) {
            nextIndex = copyOutputElements( outArray, nextIndex,
                                            (Object[]) currArray[i],
                                            currIntervalNumber + 1 );
        }
        return nextIndex;
    }


    public int numOutputLightingVectors() {
        return outLVecs.size();
    }

    public int copyOutputLightingVectors( LightingVector[] array, int index ) {
        java.util.Enumeration vecs = outLVecs.elements();
        while (vecs.hasMoreElements())
            array[index++] = (LightingVector) vecs.nextElement();
        return index;
    }
    
    
    public  LinkedList outputDrawableObjects() {
        return outLinesList;
    }

    
    public void setOutput() {
        Exec.begin_nocancel();
        Object[] tmp = outPoints;
        outPoints = bufPoints;
        bufPoints = tmp;
        outLVecs = bufLVecs;
        bufLVecs = new LinkedList();
        outLinesList = bufLinesList;
        bufLinesList = new LinkedList();
        Exec.end_nocancel();
    }
    
    
}


}


