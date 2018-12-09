package demo.plot.ui;
//
//  EditPlotPointWindow.java
//  Demo
//
//  Created by David Eigen on Tue Jul 23 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.depend.*;
import demo.coloring.ui.*;
import demo.Demo;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.util.Set;
import demo.plot.Plot;
import demo.plot.PlotPoint;
import demo.expr.Expression;
import demo.util.DemoColor;

public class EditPlotPointWindow extends EditPlotWindow {

    private int dimension_;

    private Button okBtn_ = new Button(" OK "), removeBtn_ = new Button("Remove Plot");
    private Choice styleChoice_ = new Choice();
    private static final String STYLE_DOT = "Dot", STYLE_CROSS = "Cross",
                                STYLE_SPHERE = "Sphere", STYLE_NONE = "None";
    private TextField exprFld_ = new TextField(30);
    private Slider sizeSlider_ = new Slider(1, 25);
    private TextField labelFld_ = new TextField(10);
    private Checkbox labelCbx_ = new Checkbox("Show Label: ");

    public EditPlotPointWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotPoint(demo.recognizeExpression(dimension == 2 ? "0,0" : "0,0,0"),
                            new ColoringConstant(new DemoColor(1,1,1,1) .coloringColor()),
                            dimension),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotPointWindow( PlotPoint plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotPointWindow( PlotPoint plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );
        dimension_ = dimension;

        styleChoice_.addItem(STYLE_DOT);
        styleChoice_.addItem(STYLE_CROSS);
        styleChoice_.addItem(STYLE_SPHERE);
        styleChoice_.addItem(STYLE_NONE);
        switch (plot.style()) {
            case PlotPoint.NONE:
                styleChoice_.select(STYLE_NONE);
                break;
            case PlotPoint.DOT:
                styleChoice_.select(STYLE_DOT);
                break;
            case PlotPoint.CROSS:
                styleChoice_.select(STYLE_CROSS);
                break;
            case PlotPoint.SPHERE:
                styleChoice_.select(STYLE_SPHERE);
                break;
            default:
        }
        styleChoice_.addItemListener(new StyleChoiceLsnr());
        exprFld_.setText(plot.expression().definitionString());
        exprFld_.addActionListener(new ExprFldLsnr());
        sizeSlider_.setValue(((PlotPoint) plot).size());
        sizeSlider_.addActionListener(new SizeLsnr());
        labelCbx_.setState(((PlotPoint) plot).labelIsVisible());
        labelFld_.setText(((PlotPoint) plot).label());
        labelFld_.setEnabled(labelCbx_.getState());
        labelCbx_.addItemListener(new LabelLsnr());
        labelFld_.addActionListener(new LabelLsnr());
        okBtn_.addActionListener(new OKBtnLsnr());
        removeBtn_.addActionListener(new RemoveBtnLsnr());
        this.addWindowListener(new WindowLsnrCancel());

        this.setLayout(new BorderLayout());

        // top of window: entering the point
        Panel northPanel = new Panel();
        northPanel.setLayout(new GridLayout(0,1));
        northPanel.add(new Label("Enter a point: "));
        northPanel.add(exprFld_);
        northPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        this.add( northPanel,"North");

        // center in two parts: color on left, options on right
        Panel centerPanel = new Panel();
        this.add( centerPanel,"Center");
        centerPanel.setLayout(new BorderLayout());
        // color part: seperator on right side
        ColoringEditorLsnr coloringEditorLsnr = new ColoringEditorLsnr();
        EditColoringGroupPanel coloringEditor = new EditColoringGroupPanel(demo, this, coloringEditorLsnr, plot.coloring(), new String[]{EditColoringWindow.CONSTANT, EditColoringWindow.EXPRESSION, EditColoringWindow.GRADIENT}, environment_.append(plot.expressionDefinitions()));
        coloringEditor.addComponentListener(coloringEditorLsnr);
        centerPanel.add( coloringEditor,"Center");
        // options part
        Panel optionsLinePanel = new Panel();
        centerPanel.add( optionsLinePanel,"East");
        optionsLinePanel.setLayout(new BorderLayout());
        optionsLinePanel.add( new PanelSeperator(PanelSeperator.VERTICAL),"West");
        Panel optionsPanel = new Panel();
        Panel optionsPanelWrap = new Panel();
        optionsPanelWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
        optionsPanelWrap.add(optionsPanel);
        optionsLinePanel.add( optionsPanelWrap,"East");
        optionsPanel.setLayout(new FlowVerticalLayout());
        Panel stylePanel = new Panel();
        stylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        stylePanel.add(new Label("Style: "));
        stylePanel.add(styleChoice_);
        Panel sizePanel = new Panel();
        sizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(new Label("Size: "));
        sizePanel.add(sizeSlider_);
        PlotVisibleCheckbox plotVisibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", plot);
        Panel labelPanel = new Panel();
        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(labelCbx_);
        labelPanel.add(labelFld_);
        optionsPanel.add(plotVisibleCheckbox);
        optionsPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        optionsPanel.add(stylePanel);
        optionsPanel.add(sizePanel);
        optionsPanel.add(labelPanel);
        optionsPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        optionsPanel.add(new EditPlotCommonOptionsBtn(plot, environment_, this));


        // bottom of window: OK button and remove plot button
        Panel southPanel = new Panel();
        southPanel.setLayout(new BorderLayout());
        Panel southPanelInternal = new Panel();
        southPanelInternal.setLayout(new GridLayout(1,2));
        southPanel.add( new PanelSeperator(PanelSeperator.HORIZONTAL),"North");
        southPanel.add( southPanelInternal,"Center");
        Panel okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPanel.add(okBtn_);
        Panel removePanel = new Panel();
        removePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        removePanel.add(removeBtn_);
        southPanelInternal.add(removePanel);
        southPanelInternal.add(okPanel);
        this.add( southPanel,"South");

        this.pack();
        this.setVisible(true);
    }

    // recognizes the expression in the expression field, and sets the expression of the plot.
    // returns whether the expression was successfully recognized and set
    private boolean setPlotExpression() {
        Expression expr = demo.recognizeExpression(exprFld_.getText(), environment_);
        if (expr == null) return false;
        if (expr.numIntervals() > 0) {
            demo.showError("A point cannot have intervals.");
            expr.dispose();
            return false;
        }
        if (!expr.returnsVector(dimension_)) {
            demo.showError("The point must be have dimension " + dimension_ + ".");
            expr.dispose();
            return false;
        }
        ((PlotPoint) plot).setExpression(expr);
        updatePlot();
        return true;
    }

    private class ExprFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setPlotExpression();
        }
    }

    private class ColoringEditorLsnr implements EditColoringListener, ComponentListener {
        public void coloringChanged(Coloring coloring) {
            ((PlotPoint) plot).setColoring(coloring);
            updatePlot();
        }

        public void componentResized( java.awt.event.ComponentEvent ev ) {
            pack();
        }
        public void componentHidden( java.awt.event.ComponentEvent ev ) {
        }
        public void componentShown( java.awt.event.ComponentEvent ev ) {
        }
        public void componentMoved( java.awt.event.ComponentEvent ev ) {
        }
    }

    

    private class StyleChoiceLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            String item = (String) e.getItem();
            if (item == STYLE_DOT) {
                ((PlotPoint) plot).setStyle(PlotPoint.DOT);
            }
            else if (item == STYLE_CROSS) {
                ((PlotPoint) plot).setStyle(PlotPoint.CROSS);
            }
            else if (item == STYLE_SPHERE) {
                ((PlotPoint) plot).setStyle(PlotPoint.SPHERE);
            }
            else if (item == STYLE_NONE) {
                ((PlotPoint) plot).setStyle(PlotPoint.NONE);
            }
            updatePlot();
        }
    }


    private class SizeLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ((PlotPoint) plot).setSize(sizeSlider_.value());
            updatePlot();
        }
    }

    private class LabelLsnr implements ItemListener, ActionListener {
        public void actionPerformed(ActionEvent e) {
            ((PlotPoint) plot).setLabel(labelFld_.getText());
            updatePlot();
        }
        public void itemStateChanged(ItemEvent e) {
            boolean showLabel = labelCbx_.getState();
            labelFld_.setEnabled(showLabel);
            ((PlotPoint) plot).setLabelVisible(showLabel);
            updatePlot();
        }
    }

    protected class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent e) {
            if (setPlotExpression())
                super.actionPerformed(e);
        }
    }
    
}
