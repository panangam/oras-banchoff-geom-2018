package demo.graph;

import demo.*;
import demo.io.*;
import demo.util.*;
import demo.exec.*;
import demo.gfx.PointSortable;
import demo.gfx.Matrix4D;
import demo.gfx.PointSortable;
import demo.gfx.LightingVector;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;
import demo.plot.Plot;
import demo.plot.PlotListener;
import demo.plot.PlotOutput;

/**
 * A Graph holds a bunch of Plots, and draws them to a java.awt.Graphics or ZBufferedImage.
 * It can perform transformations on the points produeced by the Plots.
 *
 * @author deigen
 */
public  abstract class Graph extends Object implements Dependable, PlotListener, FileObject {
    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    public  DependencyNode dependencyNode() { return myDependencyNode_; }

    protected Set plots = new Set();

    protected  PointSortable[] points = new PointSortable[0];
    protected  PointSortable[] pointsTmp = new PointSortable[0]; // temp buffer for collecting
    protected  PointSortable[] zmaxpoints = new PointSortable[0];

    protected  LightingVector[] lightingVectors = new LightingVector[0];
    protected  LightingVector[] lightingVectorsTmp = new LightingVector[0]; // for collecting
    
    protected  LinkedList drawableObjects = new LinkedList();

    private boolean isVisible_ = false;

    public Graph() {}

    /**
     * Collects all points and drawable objects produced by the plots.
     */
    public synchronized void collect() {
        // find how much space we need
        java.util .Enumeration plotsEnum = this .plots .elements();
        int pointsArrayLength = 0;
        int lvecsArrayLength = 0;
        while ( plotsEnum .hasMoreElements() ) {
            Plot currPlot = (Plot) plotsEnum .nextElement();
            if (currPlot.isVisible()) {
                currPlot .ensureCalculated();
                pointsArrayLength += currPlot.output().numOutputPoints();
                lvecsArrayLength += currPlot.output().numOutputLightingVectors();
            }
        }
        // make new array
        if (this.pointsTmp.length != pointsArrayLength)
            pointsTmp = new PointSortable[pointsArrayLength];
        if (this.lightingVectorsTmp.length != lvecsArrayLength)
            lightingVectorsTmp = new LightingVector[lvecsArrayLength];
        // copy all points into the array and get all drawable objects
        plotsEnum = this .plots .elements();
        LinkedList drawableObjectsTmp = new LinkedList();
        int nextFreePointsIndex = 0;
        int nextFreeLVecsIndex = 0;
        while ( plotsEnum .hasMoreElements() ) {
            Plot currPlot = (Plot) plotsEnum .nextElement();
            if (currPlot.isVisible()) {
                PlotOutput plotOutput = currPlot.output();
                nextFreePointsIndex = plotOutput.copyOutputPoints(pointsTmp,
                                                                  nextFreePointsIndex);
                nextFreeLVecsIndex = plotOutput.copyOutputLightingVectors(lightingVectorsTmp,
                                                                          nextFreeLVecsIndex);
                drawableObjectsTmp.append( plotOutput.outputDrawableObjects() );
            }
        }
        Exec.begin_nocancel();
        PointSortable[] tmp = this.points;
        this.points = this.pointsTmp;
        this.pointsTmp = tmp;
        LightingVector[] tmpv = this.lightingVectors;
        this.lightingVectors = this.lightingVectorsTmp;
        this.lightingVectorsTmp = tmpv;
        this.drawableObjects = drawableObjectsTmp;
        if (drawableObjects.size() != zmaxpoints.length)
            zmaxpoints = new PointSortable[drawableObjects.size()];
        Exec.end_nocancel();
    }

    /**
     * Draws the graph directly to a java.awt.Graphics (possibly using the Painter's sorting algorithm).
     * @param g the Graphics to draw to
     */
    public  abstract  void draw( java.awt .Graphics g ) ;
    
    /**
     * Draws the graph using a Z buffer.
     * @param zimg the ZBufferImage to draw to
     */
    public  abstract  void draw( ZBufferedImage zimg ) ;

    /**
     * Sets whether this graph should draw objects as suspended.
     * @param suspend whether objects should be drawn suspended
     */
    public  void drawSuspended( boolean suspend ) {
        drawSuspended = suspend;
    }

    protected  boolean drawSuspended = false;

    /**
     * Adds a plot to the graph.
     * The points and drawable objects from the plot are automatically added to the graph,
     * and all transformations are applied.
     * @param plot the plot to add
     */
    public synchronized void addPlot( Plot plot ) {
        if ( plots .contains(plot) ) {
            // this plot is already added
            return ;
        }
        plot.addPlotListener(this);
        // put the plot into the set of plots
        plots .put( plot );
        // we are depndent on the plot, so set the dependency
        DependencyManager.setDependency(this, plot);
        // calculate the plot if the plot is visible
        if (plot.isVisible()) {
            plot .ensureCalculated();
            PlotOutput plotOutput = plot.output();
            PointSortable[] oldPoints = this .points;
            int oldPointsLength = oldPoints .length;
            this .points = new PointSortable [ oldPointsLength + plotOutput.numOutputPoints() ];
            // copy the old points to the new array
            System.arraycopy(oldPoints, 0, this.points, 0, oldPointsLength);
            // we don't need the old points anymore
            oldPoints = null;
            // copy new plot's points into the array
            plotOutput.copyOutputPoints( this .points, oldPointsLength );
            // add drawable objects of plot
            this .drawableObjects .append( plotOutput.outputDrawableObjects() );
            if (drawableObjects.size() != zmaxpoints.length)
                zmaxpoints = new PointSortable[drawableObjects.size()];
        }
    }

    /**
     * Responds to a plot in this graph being disposed. This basically just
     * removes the plot from the graph and recollects the drawables from the
     * other plots.
     */
    public synchronized void plotDisposed( final Plot plot ) {
        plots .remove( plot );
        DependencyManager.removeDependency(this, plot);
        Exec.run(new ExecCallback() {
            public void invoke() {
                collect();
                DependencyManager.updateDependentObjectsDefMT(Graph.this);
            }
        });
    }

    /**
     * Processes a request for a plot to be disposed.
     */
    public boolean plotCanDispose(Plot plot) {
        return plots.contains(plot);
    }


    /**
     * @param plot the plot to check
     * @return whether the given plot is in this Graph
     */
    public  boolean containsPlot( Plot plot ) {
        return this.plots.contains(plot);
    }

    /**
     * Applies transformations to all points.
     */
    protected  abstract void applyTransformations();

    /**
     * Transforms everything in the graph with a transformation matrix m.
     * @param m the transformation matrix
     */
    public  abstract  void transform( Matrix4D m ) ;

    /**
     * @return the current transformation
     */
    public abstract Matrix4D getTransformation();

    /**
     * Scales everything in the graph.
     * @param factor the scaling factor (the value 1.0 does nothing)
     */
    public  abstract  void scale( double factor ) ;

    /**
     * Resets the transformations of the graph, so there are no transformations applied.
     */
    public  abstract  void resetTransformations() ;

    /**
     * Sets the transformations of the graph to the matrix m.
     * @param m the transformation matrix
     */
    public  abstract 
    void setTransformation( Matrix4D m ) ;

    /**
     * @return an Enumeration of all the plots in the graph
     */
    public  java.util .Enumeration plots() {
        return plots .elements();
    }

    /**
     * Sets whether this graph is "visible" -- that is, whether it is currently being drawn.
     * Used to tell whether plots should calculate or not. If the graph isn't drawing, then
     * plots are not calculated until the graph is shown again.
     */
    public void setVisible(boolean vis) {
        if (vis && !isVisible_) {
            isVisible_ = true;
            collect();
        }
        else {
            isVisible_ = false;
        }
    }

    /**
     * @return whether this graph is "visible" -- that is, whether it is currently being drawn.
     * Used to tell whether plots should calculate or not. If the graph isn't drawing, then
     * plots are not calculated until the graph is shown again.
     */
    public boolean isVisible() {
        return isVisible_;
    }


    public synchronized void dependencyUpdateVal( Set updatingObjects ) {
        if (!isVisible())
            return;
        // we need to get the points and objects from the plots
        boolean plotUpdated = false;
        java.util .Enumeration plotsEnum = this .plots .elements();
        while ( plotsEnum .hasMoreElements() ) {
            if ( updatingObjects.contains((Plot) plotsEnum.nextElement()) ) {
                plotUpdated = true;
                break;
            }
        }
        if ( plotUpdated )
            collect();
    }

    public void dependencyUpdateDef(Set updatingObjs) {
        dependencyUpdateVal(updatingObjs);
    }
    


    // ****************************** FILE I/O ****************************** //
    private String[] plotStrs__; private Plot[] plots__;
    
    public Graph(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        plotStrs__ = parser.parseObjectList(props.get("plots"));
    }

    public void loadFileBind(FileParser parser) {
        plots__ = new Plot[plotStrs__.length];
        for (int i = 0; i < plotStrs__.length; ++i)
            plots__[i] = (Plot) parser.getObject(plotStrs__[i]);
        DependencyManager.addDependencies(this, plots__);
    }

    public void loadFileExprs(FileParser parser) {
        parser.loadExprs(plots__);
    }

    public void loadFileFinish(FileParser parser) {
        for (int i = 0; i < plots__.length; ++i)
            addPlot(plots__[i]);
        plotStrs__ = null;
        plots__ = null;
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("plots", generator.generateObjectList(plots.elements()));
        return generator.generateProperties(props);
    }

}


