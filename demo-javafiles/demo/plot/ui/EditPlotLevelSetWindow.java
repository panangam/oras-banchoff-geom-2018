//
//  EditPlotLevelSetWindow.java
//  Demo
//
//  Created by David Eigen.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.plot.ui;

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.depend.*;
import demo.coloring.ui.*;
import demo.Demo;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.expr.Expression;
import demo.expr.IncompatibleTypeException;
import demo.plot.Plot;
import demo.plot.PlotLevelSet;

public class EditPlotLevelSetWindow extends EditPlotWindow {

    private int dimension_;

    private Button okBtn_ = new Button(" OK "), removeBtn_ = new Button("Remove Plot");

    private static final String[] COLOR_MODE_STRS     = new String[]{"Use Plot Color", "Use Coloring"};
    private static final int[]    COLOR_MODE_SETTINGS = new int[]{PlotLevelSet.PLOT, PlotLevelSet.COLORING};

    private PlotChoice plotChoice_;
    private Checkbox drawThickCheckbox_ = new Checkbox("Draw Thick");
    private Choice colorModeChoice_ = new Choice();
    private Button setColoringBtn_ = new Button("Set Coloring...");
    private TextField normalFld_ = new TextField(30);
    private TextField pointFld_ = new TextField(30);
    private TextField transfFld_ = new TextField(20);
    private TextField filterFld_ = new TextField(20);

    public EditPlotLevelSetWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotLevelSet(),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotLevelSetWindow( PlotLevelSet plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotLevelSetWindow( PlotLevelSet plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );
        dimension_ = dimension;
        if (dimension_ != 3)
            throw new RuntimeException("dimension should be 3");

        plotChoice_ = new PlotChoice(demo);
        plotChoice_.setDimensionConstraint(PlotChoice.INCLUDE);
        plotChoice_.addDimension(3);
        plotChoice_.setPlotConstraint(PlotChoice.EXCLUDE);
        plotChoice_.addPlot(plot);
        plotChoice_.setup();
        for (int i = 0; i < COLOR_MODE_STRS.length; ++i)
            colorModeChoice_.add(COLOR_MODE_STRS[i]);
        normalFld_.setText(plot.planeNormal().definitionString());
        pointFld_.setText(plot.planePoint().definitionString());
        plotChoice_.select(plot.plot());
        drawThickCheckbox_.setState(plot.drawThick());
        setColoringBtn_.setEnabled(plot.coloringMode() == PlotLevelSet.COLORING);
        transfFld_.setText(plot.pointTransformation().definitionString());
        filterFld_.setText(plot.filter().definitionString());
        
        normalFld_.addActionListener(new NormalFldLsnr());
        pointFld_.addActionListener(new PointFldLsnr());
        plotChoice_.addItemListener(new PlotChoiceLsnr());
        drawThickCheckbox_.addItemListener(new DrawThickLsnr());
        colorModeChoice_.addItemListener(new ColorModeLsnr());
        setColoringBtn_.addActionListener(new SetColoringLsnr(this));
        transfFld_.addActionListener(new TransfFldLsnr());
        filterFld_.addActionListener(new FilterFldLsnr());
        
        okBtn_.addActionListener(new OKBtnLsnr());
        removeBtn_.addActionListener(new RemoveBtnLsnr());
        this.addWindowListener(new WindowLsnrCancel());

        Panel okPnl = new Panel();
        okPnl.setLayout(new BorderLayout());
        okPnl.add(removeBtn_, "West");
        okPnl.add(okBtn_, "East");

        Panel optionsPnl = new Panel();
        optionsPnl.setLayout(new FlowVerticalLayout(FlowVerticalLayout.LEFT));
        optionsPnl.add(new PlotVisibleCheckbox("Plot is Visible", plot));
        optionsPnl.add(drawThickCheckbox_);
        optionsPnl.add(new EditPlotCommonOptionsBtn(plot, env, this));

        Panel plotPnl = new Panel();
        plotPnl.setLayout(new FlowLayout(FlowLayout.LEFT));
        plotPnl.add(new Label("Intersect plane with  " ));
        plotPnl.add(plotChoice_);

        Panel planePnl = new Panel();
        planePnl.setLayout(new BorderLayout());
        Panel pointPnl = new Panel();
        Panel normalPnl = new Panel();
        pointPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        normalPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        normalPnl.add(new Label("Normal: "));
        normalPnl.add(normalFld_);
        pointPnl.add(new Label("Point: "));
        pointPnl.add(pointFld_);
        planePnl.add(pointPnl, "North");
        planePnl.add(normalPnl, "South");
        Panel planePnl2 = new Panel();
        planePnl2.setLayout(new FlowLayout(FlowLayout.CENTER));
        planePnl2.add(planePnl);
        planePnl = planePnl2;
        
        Panel colorsPnl = new Panel();
        colorsPnl.setLayout(new FlowVerticalLayout(FlowVerticalLayout.RIGHT));
        colorsPnl.add(colorModeChoice_);
        colorsPnl.add(setColoringBtn_);

        Panel postprocessPnl = new Panel();
        Panel setPtPnl = new Panel();
        Panel filterPnl = new Panel();
        postprocessPnl.setLayout(new FlowVerticalLayout(FlowVerticalLayout.RIGHT));
        setPtPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        filterPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        setPtPnl.add(new Label("Set Point to "));
        setPtPnl.add(transfFld_);
        filterPnl.add(new Label("Show if "));
        filterPnl.add(filterFld_);
        postprocessPnl.add(setPtPnl);
        postprocessPnl.add(filterPnl);
                
        this.setLayout(new BorderLayout());
        this.add(new SeperatedPanel(okPnl, "North"), "South");
        this.add(new SeperatedPanel(optionsPnl, "West"), "East");
        Panel centerPnl = new Panel();
        centerPnl.setLayout(new BorderLayout());
        centerPnl.add(new SeperatedPanel(plotPnl, "South"), "North");
        centerPnl.add(planePnl, "Center");
        Panel centersouthPnl = new Panel();
        centersouthPnl.setLayout(new BorderLayout());
        centersouthPnl.add(new SeperatedPanel(colorsPnl, "East"), "West");
        centersouthPnl.add(postprocessPnl, "East");
        centerPnl.add(new SeperatedPanel(centersouthPnl, "North"), "South");
        this.add(centerPnl, "Center");

        this.pack();
        this.setVisible(true);
    }

    // recognizes the expression in the expression field, and sets the expression of the plot.
    // returns whether the expression was successfully recognized and set
    private boolean setPointExpression() {
        Expression expr = demo.recognizeExpression(pointFld_.getText(), environment_);
        if (expr == null) return false;
        if (expr.numIntervals() > 0) {
            demo.showError("A plane point cannot have intervals.");
            expr.dispose();
            return false;
        }
        if (!expr.returnsVector(dimension_)) {
            demo.showError("The point must be have dimension " + dimension_ + ".");
            expr.dispose();
            return false;
        }
        ((PlotLevelSet) plot).setPlanePoint(expr);
        updatePlot();
        return true;
    }

    // recognizes the expression in the expression field, and sets the expression of the plot.
    // returns whether the expression was successfully recognized and set
    private boolean setNormalExpression() {
        Expression expr = demo.recognizeExpression(normalFld_.getText(), environment_);
        if (expr == null) return false;
        if (expr.numIntervals() > 0) {
            demo.showError("A plane normal cannot have intervals.");
            expr.dispose();
            return false;
        }
        if (!expr.returnsVector(dimension_)) {
            demo.showError("The vector must be have dimension " + dimension_ + ".");
            expr.dispose();
            return false;
        }
        ((PlotLevelSet) plot).setPlaneNormal(expr);
        updatePlot();
        return true;
    }

    private boolean setPlot() {
        Plot oldPlot = ((PlotLevelSet) plot).plot();
        try {
            ((PlotLevelSet) plot).setPlot(plotChoice_.selectedPlot());
        }
        catch (CircularException ex) {
            demo.showError("Circular dependency.");
            ((PlotLevelSet) plot).setPlot(oldPlot);
            return false;
        }
        updatePlot();
        return true;
    }
    
    private class PointFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setPointExpression();
        }
    }

    private class NormalFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setNormalExpression();
        }
    }

    private class PlotChoiceLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setPlot();
        }
    }

    private class DrawThickLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            ((PlotLevelSet) plot).setDrawThick(drawThickCheckbox_.getState());
            updatePlot();
        }
    }

    private class ColorModeLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            int mode = COLOR_MODE_SETTINGS[colorModeChoice_.getSelectedIndex()];
            if (mode == PlotLevelSet.COLORING && ((PlotLevelSet) plot).coloring() == null)
                ((PlotLevelSet) plot).setColoring(new ColoringConstant(java.awt.Color.white));
            ((PlotLevelSet) plot).setColoringMode(mode);
            setColoringBtn_.setEnabled(mode == PlotLevelSet.COLORING);
            updatePlot();
        }
    }

    private class SetColoringLsnr implements ActionListener {
        private DemoFrame parent;
        public SetColoringLsnr(DemoFrame parent) {this.parent = parent;}
        public void actionPerformed(ActionEvent e) {
            Coloring c = ((PlotLevelSet) plot).coloring();
            if (c == null) c = new ColoringConstant(java.awt.Color.white);
            new EditColoringWindow(new String[]{
                                        EditColoringWindow.CONSTANT,
                                        EditColoringWindow.EXPRESSION,
                                        EditColoringWindow.GRADIENT },
                                   demo,
                                   new ColorWindowLsnr(),
                                   c,
                                   parent,
                                   environment_.append(plot.expressionDefinitions()));
        }
    }

    private class ColorWindowLsnr implements DialogListener {
        public void dialogCanceled( Window dialog ) {
        }
        public void dialogOKed( Window dialog ) {
            ((PlotLevelSet) plot).setColoring(((EditColoringWindow) dialog).coloring());
            updatePlot();
        }
    }

    private class TransfFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Expression expr = Demo.recognizeExpression(
                                    transfFld_.getText(),
                                    demo.environment().append(plot.expressionDefinitions()));
            if (expr == null) return;
            try {
                ((PlotLevelSet) plot).setPointTransformation(expr);
            }
            catch (IncompatibleTypeException ex) {
                Demo.showError(ex);
            }
            updatePlot();
        }
    }

    private class FilterFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Expression expr = Demo.recognizeExpression(
                                    filterFld_.getText(),
                                    demo.environment().append(plot.expressionDefinitions()));
            if (expr == null) return;
            try {
                ((PlotLevelSet) plot).setFilter(expr);
            }
            catch (IncompatibleTypeException ex) {
                Demo.showError(ex);
            }
            updatePlot();
        }
    }

    protected class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent e) {
            if (setPointExpression() && setNormalExpression())
                super.actionPerformed(e);
        }
    }
    
}
