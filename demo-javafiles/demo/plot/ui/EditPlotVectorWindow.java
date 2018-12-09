package demo.plot.ui;
//
//  EditPlotVectorWindow.java
//  Demo
//
//  Created by David Eigen on Tue Jul 23 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;

import demo.ui.*;
import demo.depend.*;
import demo.Demo;
import demo.coloring.ui.*;
import demo.util.Set;
import demo.util.DemoColor;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.expr.Expression;
import demo.plot.Plot;
import demo.plot.PlotVector;

public class EditPlotVectorWindow extends EditPlotWindow {

    private int dimension_;

    private Button okBtn_ = new Button(" OK "), removeBtn_ = new Button("Remove Plot");
    private Choice baseStyleChoice_ = new Choice();
    private Choice endStyleChoice_ = new Choice();
    private Slider baseSizeSlider_ = new Slider(1,30);
    private Slider endSizeSlider_ = new Slider(1,30);
    private static final String STYLE_DOT = "Dot", STYLE_SPHERE = "Sphere",
                                STYLE_ARROW_FORWARDS = "Arrow (forwards)",
                                STYLE_ARROW_BACKWARDS = "Arrow (backwards)",
                                STYLE_CONE_FORWARDS = "Cone (forwards)",
                                STYLE_CONE_BACKWARDS = "Cone (backwards)",
                                STYLE_NONE = "None";
    private TextField baseExprFld_ = new TextField(30);
    private TextField dirExprFld_ = new TextField(30);
    private Slider lengthSlider_ = new Slider(-2.0, 2.0, 5);
    private Checkbox drawThickCheckbox_ = new Checkbox("Draw Thick");
//    private TextField labelFld_ = new TextField(10);
//    private Checkbox labelCbx_ = new Checkbox("Show Label: ");

    private PlotVector plot_;

    public EditPlotVectorWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotVector(demo.recognizeExpression(dimension == 2 ? "0,0" : "0,0,0"),
                             demo.recognizeExpression(dimension == 2 ? "1,0" : "1,0,0"),
                             new ColoringConstant(new DemoColor(1,1,1,1) .coloringColor()),
                             dimension),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotVectorWindow( PlotVector plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotVectorWindow( PlotVector plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );
        dimension_ = dimension;
        plot_ = plot;
        
        baseStyleChoice_.addItem(STYLE_NONE);
        baseStyleChoice_.addItem(STYLE_ARROW_FORWARDS);
        baseStyleChoice_.addItem(STYLE_ARROW_BACKWARDS);
        baseStyleChoice_.addItem(STYLE_CONE_FORWARDS);
        baseStyleChoice_.addItem(STYLE_CONE_BACKWARDS);
        baseStyleChoice_.addItem(STYLE_SPHERE);
        baseStyleChoice_.addItem(STYLE_DOT);
        endStyleChoice_.addItem(STYLE_NONE);
        endStyleChoice_.addItem(STYLE_ARROW_FORWARDS);
        endStyleChoice_.addItem(STYLE_ARROW_BACKWARDS);
        endStyleChoice_.addItem(STYLE_CONE_FORWARDS);
        endStyleChoice_.addItem(STYLE_CONE_BACKWARDS);
        endStyleChoice_.addItem(STYLE_SPHERE);
        endStyleChoice_.addItem(STYLE_DOT);
        switch (plot.baseStyle()) {
            case PlotVector.NONE:
                baseStyleChoice_.select(STYLE_NONE);
                break;
            case PlotVector.ARROW_FORWARDS:
                baseStyleChoice_.select(STYLE_ARROW_FORWARDS);
                break;
            case PlotVector.ARROW_BACKWARDS:
                baseStyleChoice_.select(STYLE_ARROW_BACKWARDS);
                break;
            case PlotVector.CONE_FORWARDS:
                baseStyleChoice_.select(STYLE_CONE_FORWARDS);
                break;
            case PlotVector.CONE_BACKWARDS:
                baseStyleChoice_.select(STYLE_CONE_BACKWARDS);
                break;
            case PlotVector.DOT:
                baseStyleChoice_.select(STYLE_DOT);
                break;
            case PlotVector.SPHERE:
                baseStyleChoice_.select(STYLE_SPHERE);
                break;
            default:
        }
        switch (plot.endStyle()) {
            case PlotVector.NONE:
                endStyleChoice_.select(STYLE_NONE);
                break;
            case PlotVector.ARROW_FORWARDS:
                endStyleChoice_.select(STYLE_ARROW_FORWARDS);
                break;
            case PlotVector.ARROW_BACKWARDS:
                endStyleChoice_.select(STYLE_ARROW_BACKWARDS);
                break;
            case PlotVector.CONE_FORWARDS:
                endStyleChoice_.select(STYLE_CONE_FORWARDS);
                break;
            case PlotVector.CONE_BACKWARDS:
                endStyleChoice_.select(STYLE_CONE_BACKWARDS);
                break;
            case PlotVector.DOT:
                endStyleChoice_.select(STYLE_DOT);
                break;
            case PlotVector.SPHERE:
                endStyleChoice_.select(STYLE_SPHERE);
                break;
            default:
        }
        baseStyleChoice_.addItemListener(new StyleChoiceLsnr());
        endStyleChoice_.addItemListener(new StyleChoiceLsnr());
        baseExprFld_.setText(plot.baseExpression().definitionString());
        baseExprFld_.addActionListener(new ExprFldLsnr());
        dirExprFld_.setText(plot.dirExpression().definitionString());
        dirExprFld_.addActionListener(new ExprFldLsnr());
        lengthSlider_.setValue(plot_.length());
        lengthSlider_.addActionListener(new SizeLsnr());
        drawThickCheckbox_.setState(plot_.drawThick());
        drawThickCheckbox_.addItemListener(new DrawThickLsnr());
        baseSizeSlider_.setValue(plot_.baseSize());
        baseSizeSlider_.addActionListener(new SizeLsnr());
        endSizeSlider_.setValue(plot_.endSize());
        endSizeSlider_.addActionListener(new SizeLsnr());
//        labelCbx_.setState(((PlotPoint) plot).labelIsVisible());
//        labelFld_.setText(((PlotPoint) plot).label());
//        labelFld_.setEnabled(labelCbx_.getState());
//        labelCbx_.addItemListener(new LabelLsnr());
//        labelFld_.addActionListener(new LabelLsnr());
        okBtn_.addActionListener(new OKBtnLsnr());
        removeBtn_.addActionListener(new RemoveBtnLsnr());
        this.addWindowListener(new WindowLsnrCancel());

        this.setLayout(new BorderLayout());

        // top of window: entering the point
        Panel northPanel = new Panel();
        northPanel.setLayout(new GridLayout(0,1));
        Panel baseExprPanel = new Panel();
        baseExprPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        baseExprPanel.add(new Label("Base point: "));
        baseExprPanel.add( baseExprFld_);
        Panel dirExprPanel = new Panel();
        dirExprPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        dirExprPanel.add(new Label("Vector: "));
        dirExprPanel.add(dirExprFld_);
        northPanel.add(dirExprPanel);
        northPanel.add(baseExprPanel);
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
        Panel baseStylePanel = new Panel();
        baseStylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        baseStylePanel.add(new Label("Base Point Style: "));
        baseStylePanel.add(baseStyleChoice_);
        Panel baseSizePanel = new Panel();
        baseSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        baseSizePanel.add(new Label("Base Point Size: "));
        baseSizePanel.add(baseSizeSlider_);
        Panel endStylePanel = new Panel();
        endStylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        endStylePanel.add(new Label("End Point Style:  "));
        endStylePanel.add(endStyleChoice_);
        Panel endSizePanel = new Panel();
        endSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        endSizePanel.add(new Label("End Point Size:  "));
        endSizePanel.add(endSizeSlider_);
        Panel lengthPanel = new Panel();
        lengthPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lengthPanel.add(new Label("Length: "));
        lengthPanel.add(lengthSlider_);
        Panel drawThickPanel = new Panel();
        drawThickPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        drawThickPanel.add(drawThickCheckbox_);
        PlotVisibleCheckbox plotVisibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", plot);
//        Panel labelPanel = new Panel();
//        labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//        labelPanel.add(labelCbx_);
//        labelPanel.add(labelFld_);
        optionsPanel.setLayout(new FlowVerticalLayout());
        optionsPanel.add(plotVisibleCheckbox);
        optionsPanel.add(drawThickPanel);
        optionsPanel.add(lengthPanel);
        optionsPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        optionsPanel.add(endStylePanel);
        optionsPanel.add(endSizePanel);
        optionsPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        optionsPanel.add(baseStylePanel);
        optionsPanel.add(baseSizePanel);
        //        optionsPanel.add(labelPanel);
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

    // sets the base point and vector for the plot (reads in the expressions from the fields)
    // returns whether the expressions were successfully recognized and set
    private boolean setPlotExpressions() {
        Expression baseExpr = demo.recognizeExpression(baseExprFld_.getText(),
                                                      environment_);
        Expression vectExpr = demo.recognizeExpression(dirExprFld_.getText(),
                                                      environment_);
        if (vectExpr == null || baseExpr == null) {
            if (baseExpr != null)
                baseExpr.dispose();
            if (vectExpr != null)
                vectExpr.dispose();
            return false;
        }
        if (baseExpr.numIntervals() > 0 || vectExpr.numIntervals() > 0) {
            demo.showError("The vector and base point cannot have intervals.");
            baseExpr.dispose();
            vectExpr.dispose();
            return false;
        }
        if (!baseExpr.returnsVector(dimension_) || !vectExpr.returnsVector(dimension_)) {
            demo.showError("The vector and base point point must each have dimension " + dimension_ + ".");
            baseExpr.dispose();
            vectExpr.dispose();
            return false;
        }
        plot_.setBaseExpression(baseExpr);
        plot_.setDirExpression(vectExpr);
        updatePlot();
        return true;
    }
    
    private class ExprFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setPlotExpressions();
        }
    }

    private class ColoringEditorLsnr implements EditColoringListener, ComponentListener {
        public void coloringChanged(Coloring coloring) {
            plot_.setColoring(coloring);
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
            if (e.getSource() == baseStyleChoice_) {
                if (item == STYLE_NONE)
                    plot_.setBaseStyle(PlotVector.NONE);
                else if (item == STYLE_DOT)
                    plot_.setBaseStyle(PlotVector.DOT);
                else if (item == STYLE_SPHERE)
                    plot_.setBaseStyle(PlotVector.SPHERE);
                else if (item == STYLE_ARROW_FORWARDS)
                    plot_.setBaseStyle(PlotVector.ARROW_FORWARDS);
                else if (item == STYLE_ARROW_BACKWARDS)
                    plot_.setBaseStyle(PlotVector.ARROW_BACKWARDS);
                else if (item == STYLE_CONE_FORWARDS)
                    plot_.setBaseStyle(PlotVector.CONE_FORWARDS);
                else if (item == STYLE_CONE_BACKWARDS)
                    plot_.setBaseStyle(PlotVector.CONE_BACKWARDS);
            }
            else if (e.getSource() == endStyleChoice_) {
                if (item == STYLE_NONE)
                    plot_.setEndStyle(PlotVector.NONE);
                else if (item == STYLE_DOT)
                    plot_.setEndStyle(PlotVector.DOT);
                else if (item == STYLE_SPHERE)
                    plot_.setEndStyle(PlotVector.SPHERE);
                else if (item == STYLE_ARROW_FORWARDS)
                    plot_.setEndStyle(PlotVector.ARROW_FORWARDS);
                else if (item == STYLE_ARROW_BACKWARDS)
                    plot_.setEndStyle(PlotVector.ARROW_BACKWARDS);
                else if (item == STYLE_CONE_FORWARDS)
                    plot_.setEndStyle(PlotVector.CONE_FORWARDS);
                else if (item == STYLE_CONE_BACKWARDS)
                    plot_.setEndStyle(PlotVector.CONE_BACKWARDS);
            }
            updatePlot();
        }
    }


    private class SizeLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == lengthSlider_)
                plot_.setLength(lengthSlider_.value());
            else if (e.getSource() == baseSizeSlider_)
                plot_.setBaseSize(baseSizeSlider_.value());
            else if (e.getSource() == endSizeSlider_)
                plot_.setEndSize(endSizeSlider_.value());
            updatePlot();
        }
    }


    private class DrawThickLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            plot_.setDrawThick(drawThickCheckbox_.getState());
            updatePlot();
        }
    }
    

    protected class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent e) {
            if (setPlotExpressions())
                super.actionPerformed(e);
        }
    }
    
}
