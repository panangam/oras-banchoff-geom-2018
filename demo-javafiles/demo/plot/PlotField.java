package demo.plot;
//
//  PlotField.java
//  Demo
//
//  Created by David Eigen on Thu Aug 01 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import demo.io.*;
import demo.gfx.*;
import demo.depend.*;
import demo.util.*;
import demo.exec.*;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.STEInterval;
import demo.expr.ste.STEVariable;
import demo.depend.HasDependentsException;

/**
 * A PlotField contains a bunch of plots (called "subplots"), and calculates them at
 * every point in a domain. The domain is specified by a list of intervals. These
 * intervals are replaced with variables in the local environment definitions of this plot.
 * All subplots should be "in" this environment -- that is, all expressions owned by
 * the subplots should be interpreted with the local environment definitions for this
 * PlotField appended to the current environment.
 *
 * Setting the layer level or transparency threshold has no affect on a PlotField.
 *
 * @author deigen
 */
public class PlotField extends Plot implements PlotListener {

    private java.util.Vector plots_ = new java.util.Vector();
    private STEInterval[] intervals_ = null;
    private STEVariable[] variables_ = null;

    // in the output points buffer, bufPoints is a LinkedList containing PointSortable[]:
    // each entry is all the points in one plot calculation
    private PlotFieldOutput output_ = new PlotFieldOutput();

    // disposing_ is true if we're being disposed: this is when we allow subplots to dispose
    private boolean disposing_ = false; 

    /**
     * Creates a new, empty PlotField
     */
    public PlotField() {
        intervals_ = new STEInterval[0];
        variables_ = new STEVariable[0];
    }

    public void dispose() {
        disposing_ = true;
        super.dispose();
        for (java.util.Enumeration plotsEnum = plots_.elements();
             plotsEnum.hasMoreElements();)
            ((Plot) plotsEnum.nextElement()).dispose();
        for (int i = 0; i < variables_.length; ++i)
            variables_[i].dispose();
    }

    /**
     * Adds the given plot to this field of plots.
     */
    public void addPlot(Plot plot) {
        if (plots_.contains(plot))
            return;
        plot.addPlotListener(this);
        plots_.addElement(plot);
        DependencyManager.setDependency(this, plot);
    }

    /**
     * Removes the given plot from this field of plots.
     */
    public void removePlot(Plot plot) {
        DependencyManager.removeDependency(this, plot);
        plots_.removeElement(plot);
        plot.removePlotListener(this);
    }

    /**
     * @return the plots that this field is making a field out of
     */
    public java.util.Enumeration plots() {
        return plots_.elements();
    }

    /**
     * @return the intervals for this field of plots, sorted by dependency
     */
    public STEInterval[] intervals() {
        return intervals_;
    }

    /**
     * Sets the intervals for this field. If any of the subplots of this PlotField
     * are dependent on the (wrapper STEVariables of) intervals that are currently
     * intervals for this plot but do not appear in the given intervals, then
     * a HasDependentsException will be thrown and the intervals will be left
     * unchanged. That is, no subplot can be dependent on an interval that is
     * effectively removed from this plot during setIntervals(.).
     * The array of intervals need not have all unique entries.
     * @param intervals the new intervals
     * @throws HasDependentsException if any subplots are dependent on intervals
     *         not in the given intervals
     */
    public void setIntervals(STEInterval[] intervals) throws HasDependentsException {
        // find the intervals that were removed or added
        // dictionaries map from intervals to their corresponding variable
        java.util.Dictionary removedIntervals = new java.util.Hashtable();
        Set addedIntervals = new Set();
        java.util.Dictionary sameIntervals = new java.util.Hashtable();
        for (int i = 0; i < intervals_.length; ++i)
            removedIntervals.put(intervals_[i], variables_[i]);
        for (int i = 0; i < intervals.length; ++i) {
            int intervalNum = -1;
            for (int j = 0; j < intervals_.length; ++j) {
                if (intervals_[j] == intervals[i]) {
                    intervalNum = j;
                    break;
                }
            }
            if (intervalNum == -1)
                addedIntervals.put(intervals[i]);
            else {
                sameIntervals.put(intervals_[intervalNum], variables_[intervalNum]);
                removedIntervals.remove(intervals_[intervalNum]);
            }
        }
        // make sure no removed interval had dependent objects
        for (java.util.Enumeration removedVars = removedIntervals.elements();
             removedVars.hasMoreElements();) {
            STEVariable variable = (STEVariable) removedVars.nextElement();
            if (DependencyManager.hasDependentObjects(variable))
                throw new HasDependentsException("Can't set intervals because there are subplots dependent on " + variable.name());
            definitions_.remove(variable.name());
        }
        for (java.util.Enumeration removedIntervalsEnum = removedIntervals.keys();
             removedIntervalsEnum.hasMoreElements();) {
            STEInterval interval = (STEInterval) removedIntervalsEnum.nextElement();
            DependencyManager.removeDependency(this, interval);
        }
        // set up intervals and variables
        // sort intervals by dependency
        Set intervalsSet = new Set();
        intervalsSet.addObjects(intervals);
        intervals = null; // we don't need this array anymore and if we use it we'd like to know
        java.util.Vector sortedIntervals = DependencyManager.sortByDependency(intervalsSet);
        intervals_ = new STEInterval[sortedIntervals.size()];
        sortedIntervals.copyInto(intervals_);
        variables_ = new STEVariable[intervals_.length];
        for (int i = 0; i < intervals_.length; ++i) {
            STEInterval interval = intervals_[i];
            DependencyManager.setDependency(this, interval);
            if (addedIntervals.contains(interval)) {
                // the interval was just added
                STEVariable variable = new STEVariable(interval.name(),
                                                       new Expression(interval.minExpr()),
                                                       new Expression(interval.maxExpr()),
                                                       new Expression(interval.resExpr()));
                definitions_.put(variable.name(), variable);
                variables_[i] = variable;
            }
            else {
                // the interval was not added -- we used to have it
                variables_[i] = (STEVariable) sameIntervals.get(interval);
            }
        }
    }


    public void calculatePlot() {
        if (intervals_.length == 0)
            return;
        calculate(0);
        output_.setOutput();
    }

    public PlotOutput output() {
        return output_;
    }

    private void calculate(int intervalIndex) {
        if (intervalIndex == intervals_.length) {
            LinkedList points = output_.bufferPoints();
            LinkedList lvecs = output_.bufferLightingVectors();
            LinkedList drawables = output_.bufferDrawables();
            for (int i = 0; i < plots_.size(); ++i) {
                Plot plot = (Plot) plots_.elementAt(i);
                if (plot.isVisible()) {
                    plot.calculate();
                    PlotOutput plotOutput = plot.output();
                    PointSortable[] plotPoints = new PointSortable[plotOutput.numOutputPoints()];
                    plotOutput.copyOutputPoints(plotPoints, 0);
                    LightingVector[] plotLVecs =
                        new LightingVector[plotOutput.numOutputLightingVectors()];
                    plotOutput.copyOutputLightingVectors(plotLVecs, 0);
                    points.add(plotPoints);
                    lvecs.add(plotLVecs);
                    drawables.append(plotOutput.outputDrawableObjects());
                }
            }
        }
        else {
            STEInterval u = intervals_[intervalIndex];
            STEVariable uVar = variables_[intervalIndex];
            double resolution = u.resolution().number();
            int resInt = (int) Math .round( resolution );
            double min = u.min().number();
            double max = u.max().number();
            double incrementAmount = (max - min) / (double) resInt;
            for ( int step = 0; step <= resInt; step++ ) {
                double val = incrementAmount * step + min;
                u.setValue(val);
                uVar.setValue(val);
                calculate(intervalIndex + 1);
            }
        }
    }

    
    public String title() {
        String title = "Field of ";
        if (plots_.size() == 0)
            return title + "nothing";
        if (plots_.size() <= 2) {
            for (int i = 0; i < plots_.size(); ++i)
                title += U.clampString(((Plot) plots_.elementAt(i)).title(), 32) +
                    (i < plots_.size() - 1 ? ", " : "");
            return title;
        }
        return "Field of " + plots_.size() + " plots";
    }

    public void plotDisposed(Plot plot) {
        plots_.removeElement(plot);
        calculated_ = false;
    }

    public boolean plotCanDispose(Plot plot) {
        for (int i = 0; i < plots_.size(); ++i)
            if (plots_.elementAt(i) == plot)
                return true;
        return false;
    }

    public void dependencyUpdateDef(Set updatingObjects) {
        super.dependencyUpdateDef(updatingObjects);
        Expression[] varMinExprs = new Expression[variables_.length];
        Expression[] varMaxExprs = new Expression[variables_.length];
        Expression[] varResExprs = new Expression[variables_.length];
        for (int i = 0; i < intervals_.length; ++i) {
            STEInterval interval = intervals_[i];
            varMinExprs[i] = new Expression(interval.minExpr());
            varMaxExprs[i] = new Expression(interval.maxExpr());
            varResExprs[i] = new Expression(interval.resExpr());
        }
        Exec.begin_nocancel();
        for (int i = 0; i < intervals_.length; ++i)
            variables_[i].setExpressions(varMinExprs[i],
                                         varMaxExprs[i],
                                         varResExprs[i]);
        Exec.end_nocancel();
    }





    // ****************************** FILE I/O ****************************** //
    private String[] plots__, intervalNames__;
    
    public PlotField(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        intervalNames__ = parser.parseWordList(props.get("intervals"));
        plots__ = parser.parseObjectList(props.get("plots"));
    }

    public void loadFileBind(FileParser parser) {
        for (int i = 0; i < plots__.length; ++i) {
            Plot p = (Plot) parser.getObject(plots__[i]);
            plots_.addElement(p);
            p.addPlotListener(this);
        }
        for (int i = 0; i < plots_.size(); ++i)
            DependencyManager.setDependency(this, (Plot) plots_.elementAt(i));
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        STEInterval[] intervals = new STEInterval[intervalNames__.length];
        for (int i = 0; i < intervalNames__.length; ++i)
            intervals[i] = (STEInterval) parser.currEnvLookup(intervalNames__[i]);
        intervals_ = new STEInterval[0];
        variables_ = new STEVariable[0];
        try {
            setIntervals(intervals);
        }
        catch (HasDependentsException ex) {
            throw new RuntimeException("Variables have dependents on init.");
        }
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        parser.loadExprs(plots_.elements());
        parser.popEnvironment();
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        String[] intervalNames = new String[intervals_.length];
        for (int i = 0; i < intervals_.length; ++i)
            intervalNames[i] = intervals_[i].name();
        props.add("intervals", generator.generateWordList(intervalNames));
        props.add("plots", generator.generateObjectList(plots_.elements()));
        return generator.generateProperties(props);
    }
    
    
}


class PlotFieldOutput implements PlotOutput {

    private LinkedList
        bufPoints_ = new LinkedList(),
        outPoints_ = new LinkedList(),
        bufLVecs_ = new LinkedList(),
        outLVecs_ = new LinkedList(),
        bufDrawables_ = new LinkedList(),
        outDrawables_ = new LinkedList();

    public LinkedList bufferPoints() { return bufPoints_; }

    public LinkedList bufferLightingVectors() { return bufLVecs_; }

    public LinkedList bufferDrawables() { return bufDrawables_; }

    public int numOutputPoints() {
        int numPts = 0;
        outPoints_.resetEnumeration();
        while (outPoints_.hasMoreElements())
            numPts += ((PointSortable[]) outPoints_.nextElement()).length;
        return numPts;
    }

    public int copyOutputPoints(PointSortable[] array, int nextFreeIndex) {
        for (outPoints_.resetEnumeration(); outPoints_.hasMoreElements();) {
            PointSortable[] pointsArray  = (PointSortable[]) outPoints_.nextElement();
            System.arraycopy(pointsArray, 0, array, nextFreeIndex, pointsArray.length);
            nextFreeIndex += pointsArray.length;
        }
        return nextFreeIndex;
    }

    public int numOutputLightingVectors() {
        int numVecs = 0;
        outLVecs_.resetEnumeration();
        while (outLVecs_.hasMoreElements())
            numVecs += ((LightingVector[]) outLVecs_.nextElement()).length;
        return numVecs;
    }

    public int copyOutputLightingVectors(LightingVector[] array, int nextFreeIndex) {
        for (outLVecs_.resetEnumeration(); outLVecs_.hasMoreElements();) {
            LightingVector[] vecsArray  = (LightingVector[]) outLVecs_.nextElement();
            System.arraycopy(vecsArray, 0, array, nextFreeIndex, vecsArray.length);
            nextFreeIndex += vecsArray.length;
        }
        return nextFreeIndex;
    }

    public LinkedList outputDrawableObjects() {
        return outDrawables_;
    }

    public void setOutput() {
        Exec.begin_nocancel();
        outPoints_ = bufPoints_;
        bufPoints_ = new LinkedList();
        outLVecs_ = bufLVecs_;
        bufLVecs_ = new LinkedList();
        outDrawables_ = bufDrawables_;
        bufDrawables_ = new LinkedList();
        Exec.end_nocancel();
    }
}



