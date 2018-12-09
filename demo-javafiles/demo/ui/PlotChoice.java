//
//  PlotChoice.java
//  Demo
//
//  Created by David Eigen on Sat Mar 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.*;
import java.awt.event.*;

import demo.plot.*;
import demo.graph.*;
import demo.util.Set;
import demo.Demo;
import demo.util.U;

/**
 * For choosing a plot. User can choose which graph, and then a plot from the graph.
 */
public class PlotChoice extends Panel implements ItemSelectable, GraphFrameListener, PlotListener {

    /**
     * Constants for constraint set modes.
     */
    public static final int ALL = 0, INCLUDE = 1, EXCLUDE = 2;
    
    private Choice graphChoice_ = new Choice();
    private Choice plotChoice_ = new Choice();
    private java.util.Vector graphs_ = new java.util.Vector();
    private java.util.Vector plots_ = new java.util.Vector();
    private Plot selectedPlot_ = new PlotNull();

    private int graphConstraint_ = ALL, dimensionConstraint_ = ALL, plotConstraint_ = ALL;
    private Set graphSet_ = new Set(), dimensionSet_ = new Set(), plotSet_ = new Set();

    private Set listeners_ = new Set();
    
    private Demo demo;

    private static final Graph NULL_GRAPH = new Graph3D(); // dummy graph for selecting nothing

    /**
     * Creates a PlotChoice, contraining the user to choose from
     * plots in the given graph. Plot constraints may be specified,
     * but if they are, setup() must be called afterwards.
     */
    public PlotChoice(Demo demo, Graph graph) {
        init(demo, false);
        setGraphConstraint(INCLUDE);
        addGraph(graph);
        setGraphChoice();
        graphChoice_.select(1);
        setPlotChoice();
    }

    /**
     * Creates a PlotChoice, where the user can choose any (user-accessable)
     * plot in the program that meets the constraints specified using the
     * constraint-specifying methods. After all constraints are specified,
     * the method setup() must be called.
     */
    public PlotChoice(Demo demo) {
        init(demo, true);
        setGraphChoice();
        setPlotChoice();
    }

    private void init(Demo demo, boolean addGraphChoice) {
        this.demo = demo;
        demo.addGraphFrameListener(this);
        setLayout(new BorderLayout());
        Panel north = new Panel();
        north.setLayout(new BorderLayout());
        Panel south = new Panel();
        south.setLayout(new BorderLayout());
        north.add(new Label("Plot "), "West");
        north.add(plotChoice_, "Center");
        if (addGraphChoice) {
            south.add(new Label("  from graph "), "West");
            south.add(graphChoice_, "Center");
        }
        this.add(north, "North");
        this.add(south, "South");
        graphChoice_.addItemListener(new GraphChoiceLsnr());
        plotChoice_.addItemListener(new PlotChoiceLsnr());
    }

    public void removeNotify() {
        demo.removeGraphFrameListener(this);
        super.removeNotify();
    }

    /**
     * Sets the constraint mode for which graphs a user can choose.
     * @param mode can be ALL, INCLUDE, or EXCLUDE.
     *        If set to ALL, all graphs meeting other constraints are shown.
     *        If set to INCLUDE, only those graphs added are shown.
     *        If set to EXCLUDE, only those graphs not added are shown.
     */
    public void setGraphConstraint(int mode) {
        graphConstraint_ = mode;
    }

    /**
     * Adds a graph to the set of graphs used to determine which
     * graphs the user can choose from. This set can be used as
     * the set of graphs the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void addGraph(Graph graph) {
        graphSet_.add(graph);
    }

    /**
     * Removes a graph from the set of graphs used to determine which
     * graphs the user can choose from. This set can be used as
     * the set of graphs the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void removeGraph(Graph graph) {
        graphSet_.remove(graph);
    }

    /**
     * Sets the constraint mode for which dimensions a user can choose.
     * @param mode can be ALL, INCLUDE, or EXCLUDE.
     *        If set to ALL, all graphs meeting other constraints are shown.
     *        If set to INCLUDE, only those graphs with the dimensions added are shown.
     *        If set to EXCLUDE, only those graphs with the dimensions not added are shown.
     */
    public void setDimensionConstraint(int mode) {
        dimensionConstraint_ = mode;
    }

    /**
     * Adds a dimension to the set of dimensions used to determine which
     * graphs the user can choose from. This set can be used as
     * the set of dimensions for graphs the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void addDimension(int dim) {
        dimensionSet_.add(new Integer(dim));
    }

    /**
     * Removes a dimension from the set of dimensions used to determine which
     * graphs the user can choose from. This set can be used as
     * the set of dimensions for graphs the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void removeDimension(int dim) {
        dimensionSet_.remove(new Integer(dim));
    }

    /**
     * Sets the constraint mode for which plots a user can choose.
     * @param mode can be ALL, INCLUDE, or EXCLUDE.
     *        If set to ALL, all plots meeting other constraints are shown.
     *        If set to INCLUDE, only those plots added are shown.
     *        If set to EXCLUDE, only those plots not added are shown.
     */
    public void setPlotConstraint(int mode) {
        plotConstraint_ = mode;
    }

    /**
     * Adds a plot to the set of plots used to determine which
     * plots the user can choose from. This set can be used as
     * the set of plots the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void addPlot(Plot plot) {
        plotSet_.add(plot);
    }

    /**
     * Removes a plot from the set of plots used to determine which
     * plots the user can choose from. This set can be used as
     * the set of plots the user can see, the set the user cannot see,
     * or can be completely ignored (modes INCLUDE, EXCLUDE, and ALL, respectively).
     */
    public void removePlot(Plot plot) {
        plotSet_.remove(plot);
    }

    /**
     * Sets up the menus for choosing. This method must be called
     * after all constraints, etc are specified.
     */
    public void setup() {
        setGraphChoice();
        setPlotChoice();
        select(selectedPlot_);
    }
    
    /**
     * Adds an ItemListener to this PlotChoice.
     */
    public void addItemListener(ItemListener l) {
        listeners_.add(l);
    }
    
    /**
     * Removes an ItemListener to from PlotChoice.
     */
    public void removeItemListener(ItemListener l) {
        listeners_.remove(l);
    }

    
    /**
     * Sets the current selection to the given plot.
     */
    public void select(Plot plot) {
        boolean found = false;
        int i;
        for (i = 0; i < graphs_.size(); ++i) {
            Graph g = (Graph) graphs_.elementAt(i);
            java.util.Enumeration plots = g.plots();
            while (plots.hasMoreElements()) {
                if (plots.nextElement() == plot) {
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        if (found) {
            graphChoice_.select(i);
            setPlotChoice();
            for (int j = 0; j < plots_.size(); ++j) {
                if (plots_.elementAt(j) == plot) {
                    plotChoice_.select(j);
                    break;
                }
            }
        }
        else {
            graphChoice_.select(0);
            setPlotChoice();
            plotChoice_.select(0);
        }
        selectedPlot_ = plot;
    }

    /**
     * @return the selected plot.
     */
    public Plot selectedPlot() {
        return selectedPlot_;
    }

    /**
     * @return the selected objects (ie, an array of one element containing the selected plot)
     */
    public Object[] getSelectedObjects() {
        return new Object[]{selectedPlot()};
    }

    public void graphFrameCreated(GraphFrame frame) {
        setup();
    }

    public void graphFrameDisposed(GraphFrame frame) {
        setup();
    }

    public void plotDisposed(Plot plot) {
        plotSet_.remove(plot);
        if (plot == selectedPlot_)
            selectedPlot_ = new PlotNull();
        setup();
    }

    public boolean plotCanDispose(Plot plot) {
        return true;
    }

    // puts graphs into the graph choice, given the current constraints
    private void setGraphChoice() {
        java.util .Enumeration frames = demo.allGraphFrames();
        graphs_.removeAllElements();
        graphChoice_.removeAll();
        graphs_.addElement(NULL_GRAPH);
        graphChoice_.add("None");
        while (frames.hasMoreElements()) {
            GraphFrame currFrame = (GraphFrame) frames.nextElement();
            if (currFrame instanceof GraphFrameUserPlottable) {
                Graph graph = null;
                if (currFrame instanceof GraphFrame2D)
                    graph = ((GraphFrame2D) currFrame).canvas().graph();
                if (currFrame instanceof GraphFrame3D)
                    graph = ((GraphFrame3D) currFrame).canvas().graph();
                Integer dim = new Integer(((GraphFrameUserPlottable) currFrame).dimension());
                if (    ( (graphConstraint_ == ALL) ||
                          (graphConstraint_ == INCLUDE && graphSet_.contains(graph)) ||
                          (graphConstraint_ == EXCLUDE && !graphSet_.contains(graph)) )
                     && ( (dimensionConstraint_ == ALL) ||
                          (dimensionConstraint_ == INCLUDE && dimensionSet_.contains(dim)) ||
                          (dimensionConstraint_ == EXCLUDE && !dimensionSet_.contains(dim)) )
                    ) {
                    graphChoice_.add(currFrame.getTitle());
                    graphs_.addElement(graph);
                }
            }
        }
        graphChoice_.select(0);
    }
    
    // puts plots into the plot choice, given the current graph selected by
    // the graph choice and the current constraints
    private void setPlotChoice() {
        Graph g = (Graph) graphs_.elementAt(graphChoice_.getSelectedIndex());
        plots_.removeAllElements();
        plots_.addElement(new PlotNull());
        plotChoice_.removeAll();
        plotChoice_.add("None");
        int selectedPlotIndex = 0;
        int i = 1;
        if (g != NULL_GRAPH) {
            java.util.Enumeration plots = g.plots();
            while (plots.hasMoreElements()) {
                Plot p = (Plot) plots.nextElement();
                if ( (plotConstraint_ == ALL) ||
                     (plotConstraint_ == INCLUDE && plotSet_.contains(p)) ||
                     (plotConstraint_ == EXCLUDE && !plotSet_.contains(p)) ) {
                    plotChoice_.add(U.clampString(p.title(), 40));
                    plots_.addElement(p);
                    if (p == selectedPlot_)
                        selectedPlotIndex = i;
                    ++i;
                }
            }
        }
        plotChoice_.select(selectedPlotIndex);
    }

    private void broadcastChange() {
        ItemEvent e = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, 0);
        java.util.Enumeration ls = listeners_.elements();
        while (ls.hasMoreElements())
            ((ItemListener) ls.nextElement()).itemStateChanged(e);
    }
    
    private class PlotChoiceLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            selectedPlot_ = (Plot) plots_.elementAt(plotChoice_.getSelectedIndex());
            broadcastChange();
        }
    }

    private class GraphChoiceLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setPlotChoice();
            selectedPlot_ = (Plot) plots_.elementAt(plotChoice_.getSelectedIndex());
            broadcastChange();
        }
    }
    
}
