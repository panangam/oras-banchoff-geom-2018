package demo.plot.ui;
//
//  EditPlotWindowCreator.java
//  Demo
//
//  Created by David Eigen on Fri Aug 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import mathbuild.Environment;

import demo.Demo;
import demo.plot.*;

/**
 * The EditPlotWindowCreator class is used to open and keep track of EditPlotWindows.
 *
 * @author deigen
 */
public class EditPlotWindowCreator implements EditPlotWindowListener {

    /**
     * Types for plots. Used to tell which type of plot an open window is for.
     */
    // ****** ADD TO FOLLOWING TYPES FOR NEW PLOT ****** //
    public static final int
        PLOT_TYPE_AXES = 0,
        PLOT_TYPE_POINT = 1,
        PLOT_TYPE_VECTOR = 2,
        PLOT_TYPE_CURVE = 3,
        PLOT_TYPE_SURFACE = 4,
        PLOT_TYPE_WIREFRAME = 5,
        PLOT_TYPE_POLYGON = 6,
        PLOT_TYPE_POLYHEDRON = 7,
        PLOT_TYPE_FIELD = 8,
        PLOT_TYPE_LEVELSET = 9
        ;

    private Demo demo_;
    private Environment environment_;
    private int dimension_;

    // maps from Plots to their windows
    private static java.util.Dictionary openWindows_ = new java.util.Hashtable();
    

    /**
     * Creates a new EditPlotWindowCreator with the given parameters. The EditPlotWindowCreator
     * class is able to open EditPlotWindows for plots.
     * @param demo the demo class
     * @param env the environment expressions for plots should be interpreted in
     * @param dimension the dimension of the plots
     */
    public EditPlotWindowCreator(Demo demo, Environment env, int dimension) {
        demo_ = demo;
        environment_ = env;
        dimension_ = dimension;
    }
    

    /**
     * Opens an new EditPlotWindow to create a plot of the given type.
     * @param plotType the type of plot whose window we should open.
     * @param listeners any initial EditPlotListeners for the window
     * @return the EditPlotWindow just opened for creating the given plot type
     */
    public EditPlotWindow openWindow(int plotType, demo.util.Set listeners) {
        listeners.add(this);

        
        // ********** ADD CASE TO THE FOLLOWING FOR A NEW PLOT WINDOW ********** //
        
        switch (plotType) {
            case PLOT_TYPE_AXES:
                return new EditPlotAxesWindow(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_POINT:
                return new EditPlotPointWindow(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_VECTOR:
                return new EditPlotVectorWindow(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_CURVE:
                return new EditPlotWindowCurve(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_SURFACE:
                return new EditPlotWindowSurface(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_WIREFRAME:
                return new EditPlotWindowWireframe(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_POLYGON:
                return new EditPlotPolygonWindow(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_POLYHEDRON:
                return new EditPlotPolyhedronWindow(demo_, environment_, dimension_, listeners);
                
            case PLOT_TYPE_FIELD:
                return new EditPlotFieldWindow(demo_, environment_, dimension_, listeners);

            case PLOT_TYPE_LEVELSET:
                return new EditPlotLevelSetWindow(demo_, environment_, dimension_, listeners);

                
        // ^^^^^^^^^^^ ADD CASE TO THE ABOVE FOR NEW PLOT WINDOW ^^^^^^^^^^^ //

                
            default:
                throw new RuntimeException("Unknown plot type.");
        }
    }


    
    /**
     * If a plot window is already open for the given plot, brings the window to the front.
     * Otherwise, opens a new EditPlotWindow to edit the given plot. In either
     * case, returns the EditPlotWindow that is open (or just opened) for the plot.
     * @param plot the plot to edit
     * @param listener any initial EditPlotListeners for the window
     *        If the window already exists, these listeners are added to the listeners of the window.
     * @return the EditPlotWindow that has been opened for the plot
     */
    public EditPlotWindow openWindow(Plot plot, demo.util.Set listeners) {
        if (openWindows_.get(plot) != null) {
            // there is already a window open for this plot, so bring it to the front
            EditPlotWindow win = (EditPlotWindow) openWindows_.get(plot);
            win.toFront();
            for (java.util.Enumeration l = listeners.elements();
                 l.hasMoreElements();)
                win.addEditPlotWindowListener((EditPlotWindowListener) l.nextElement());
            return win;
        }
        // window not open yet, so we need to create it
        listeners.add(this);

        
        // ********** ADD CASE TO THE FOLLOWING FOR A NEW PLOT WINDOW ********** //
        
        if (plot instanceof PlotAxes)
            return new EditPlotAxesWindow( (PlotAxes) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotPoint)
            return new EditPlotPointWindow( (PlotPoint) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotVector)
            return new EditPlotVectorWindow( (PlotVector) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotCurve)
            return new EditPlotWindowCurve( (PlotCurve) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotSurface3D)
            return new EditPlotWindowSurface( (PlotSurface3D) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotWireframe)
            return new EditPlotWindowWireframe( (PlotWireframe) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotPolyhedron)
            return new EditPlotPolyhedronWindow( (PlotPolyhedron) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotPolygon)
            return new EditPlotPolygonWindow( (PlotPolygon) plot,
                                demo_, environment_, dimension_, listeners);
        
        if (plot instanceof PlotField)
            return new EditPlotFieldWindow( (PlotField) plot,
                                demo_, environment_, dimension_, listeners);

        if (plot instanceof PlotLevelSet)
            return new EditPlotLevelSetWindow( (PlotLevelSet) plot,
                                               demo_, environment_, dimension_, listeners);

        
        // ^^^^^^^^^^^ ADD CASE TO THE ABOVE FOR NEW PLOT WINDOW ^^^^^^^^^^^ //

        
        else
            throw new RuntimeException("Unknown plot type.");
    }



    /**
     * Closes a plot window for a given plot, if one is open.
     * The exit status of the plot window is set to OK, and all
     * EditPlotWindow listeners are notified of the window closing
     * as if the OK button were pressed.
     */
    public static void disposeWindowOK(Plot plot) {
        if (openWindows_.get(plot) == null)
            return;
        ((EditPlotWindow) openWindows_.get(plot)).disposeWindowOK();
    }

    

    // ***** EditPlotWindowListener implementation ***** //

    public void plotCreated( EditPlotWindow win, Plot plot ){ 
        if (plot != null)
            openWindows_.put(plot, win);
    }
    public void plotChanged( EditPlotWindow win, Plot plot ){}
    public void plotWindowOpened( EditPlotWindow win, Plot plot ){
        if (plot != null)
            openWindows_.put(plot, win);
    }
    public void plotWindowCanceled( EditPlotWindow win, Plot plot ){
        if (plot != null)
            openWindows_.remove(plot);
    }
    public void plotWindowOKed( EditPlotWindow win, Plot plot ){
        if (plot != null)
            openWindows_.remove(plot);
    }
    public void plotWindowRemoved( EditPlotWindow win, Plot plot ){
        if (plot != null)
            openWindows_.remove(plot);
    }

    



}
