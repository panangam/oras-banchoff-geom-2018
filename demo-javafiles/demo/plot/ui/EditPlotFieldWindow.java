package demo.plot.ui;

import mathbuild.value.*;

//
//  EditPlotFieldWindow.java
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
import demo.coloring.ui.EditColoringGroupPanel;
import demo.util.Set;
import demo.util.U;
import demo.coloring.Coloring;
import demo.plot.Plot;
import demo.plot.PlotField;
import demo.depend.HasDependentsException;
import demo.expr.ste.STEInterval;

public class EditPlotFieldWindow extends EditPlotWindow implements EditPlotWindowListener {

    private int dimension_;

    private PlotField plot_;

    private Button okBtn_ = new Button(" OK "), removeBtn_ = new Button("Remove Plot");

    private TextField intervalsFld_ = new TextField();
    private Choice addPlotChoice_;
    private Button editPlotBtn_ = new Button("Edit Plot");
    private Button removePlotBtn_ = new Button("Remove Plot");
    private List plotList_ = new List();
    private java.util.Vector plots_ = new java.util.Vector(); // the plots in the list

    private EditPlotWindowCreator plotWindowCreator_;
    

    public EditPlotFieldWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotField(),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotFieldWindow( PlotField plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotFieldWindow( PlotField plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );
        
        dimension_ = dimension;
        plot_ = plot;
        Set thisSet = new Set();
        thisSet.add(this);
        plotWindowCreator_ = new EditPlotWindowCreator(demo, env.append(plot_.expressionDefinitions()), dimension);
        
        okBtn_.addActionListener(new OKBtnLsnr());
        removeBtn_.addActionListener(new RemoveBtnLsnr());
        this.addWindowListener(new WindowLsnrCancel());

        intervalsFld_.addActionListener(new IntervalsFldLsnr());
        addPlotChoice_ = new AddPlotMenuCreator(demo, environment_.append(plot_.expressionDefinitions()), dimension, this).makeChoice();
        editPlotBtn_.addActionListener(new EditPlotBtnLsnr(this));
        removePlotBtn_.addActionListener(new RemovePlotBtnLsnr());
        plotList_.addActionListener(new PlotsListLsnr(this));
        // intervals
        String intervalsStr = "";
        STEInterval[] intervals =  plot_.intervals();
        for (int i = 0; i < intervals.length; ++i)
            intervalsStr += intervals[i].name() + (i < intervals.length - 1 ? ", " : "");
        intervalsFld_.setText(intervalsStr);
        // plots
        java.util.Enumeration plots = plot_.plots();
        while (plots.hasMoreElements()) {
            Plot p = (Plot) plots.nextElement();
            plots_.addElement(p);
            plotList_.add(U.clampString(p.title(), 50));
        }
        

        this.setLayout(new BorderLayout());

        // north contains field for intervals
        Panel northPanel = new Panel();
        northPanel.setLayout(new GridLayout(0,1));
        northPanel.add(new Label("Enter the intervals for this field as a comma-seperated list:"));
        northPanel.add(intervalsFld_);
        northPanel.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        this.add( northPanel,"North");

        // center in two parts: plots on left, options on right
        Panel centerPanel = new Panel();
        this.add( centerPanel,"Center");
        centerPanel.setLayout(new BorderLayout());
        
        // plots part
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add( new Label("Plots:"),"North");
        mainPanel.add( plotList_,"Center");
        Panel mainSouthPanel = new Panel();
        mainSouthPanel.setLayout(new GridLayout(0,1));
        mainSouthPanel.add(addPlotChoice_);
        mainSouthPanel.add(editPlotBtn_);
        mainSouthPanel.add(removePlotBtn_);
        mainPanel.add( mainSouthPanel,"South");
        centerPanel.add( mainPanel,"Center");
        
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
        
        PlotVisibleCheckbox plotVisibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", plot);
        optionsPanel.add(plotVisibleCheckbox);


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


    public void plotCreated( EditPlotWindow win, Plot plot ){
        plot_.addPlot(plot);
        plotList_.addItem(U.clampString(plot.title(), 50));
        plots_.addElement(plot);
        updatePlot();
    }
    public void plotChanged( EditPlotWindow win, Plot plot ){
        int plotIndex = -1;
        for (int i = 0; i < plots_.size(); ++i) {
            if (plots_.elementAt(i) == plot) {
                plotIndex = i;
                break;
            }
        }
        if (plotIndex >= 0 && plotIndex < plots_.size()) {
            plotList_.replaceItem(U.clampString(plot.title(), 50), plotIndex);
            updatePlot();
        }
    }
    public void plotWindowOpened( EditPlotWindow win, Plot plot ) {
        addOpenDialog(win);
    }
    public void plotWindowCanceled( EditPlotWindow win, Plot plot ){}
    public void plotWindowOKed( EditPlotWindow win, Plot plot ){}
    public void plotWindowRemoved( EditPlotWindow win, Plot plot ){
        int plotIndex = -1;
        for (int i = 0; i < plots_.size(); ++i) {
            if (plots_.elementAt(i) == plot) {
                plotIndex = i;
                break;
            }
        }
        if (plotIndex >= 0 && plotIndex < plots_.size()) {
            plotList_.remove(plotIndex);
            plots_.removeElementAt(plotIndex);
            updatePlot();
        }
    }


    private class IntervalsFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // split string by commas
            String str = intervalsFld_.getText();
            java.util.Vector intervalStrs = new java.util.Vector();
            int startIndex = 0;
            int commaIndex = str.indexOf(',');
            while (commaIndex != -1) {
                intervalStrs.addElement(str.substring(startIndex, commaIndex).trim());
                startIndex = commaIndex + 1;
                commaIndex = str.indexOf(',', startIndex);
            }
            intervalStrs.addElement(str.substring(startIndex).trim());
            // get intervals
            STEInterval[] intervals = new STEInterval[intervalStrs.size()];
            for (int i = 0; i < intervals.length; ++i) {
                Object entry;
                try {
                    entry = environment_.lookup((String) intervalStrs.elementAt(i));
                }
                catch (mathbuild.VariableNotFoundException ex) {
                    Demo.showError(ex.getMessage());
                    return;
                }
                if (!(entry instanceof STEInterval)) {
                    demo.showError(intervalStrs.elementAt(i) + " is not an interval.");
                    return;
                }
                intervals[i] = (STEInterval) entry;
            }
            // make sure no subplots are dependent on the old intervals first
            try {
                plot_.setIntervals(intervals);
                updatePlot();
            }
            catch (HasDependentsException ex) {
                Demo.showError(ex.getMessage());
            }
        }
    }

    private class EditPlotBtnLsnr implements ActionListener {
        public EditPlotFieldWindow parent_;
        public EditPlotBtnLsnr(EditPlotFieldWindow parent) {parent_ = parent;}
        public void actionPerformed(ActionEvent e) {
            int index = plotList_.getSelectedIndex();
            if (index == -1) return;
            Set thisSet = new Set(); thisSet.add(parent_);
            plotWindowCreator_.openWindow((Plot) plots_.elementAt(index), thisSet);
        }
    }

    private class RemovePlotBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = plotList_.getSelectedIndex();
            if (index == -1) return;
            Plot plot = (Plot) plots_.elementAt(index);
            plotWindowCreator_.disposeWindowOK(plot);
            plot.dispose();
            plotList_.remove(index);
            plots_.removeElementAt(index);
            updatePlot();
        }
    }

    private class PlotsListLsnr implements ActionListener {
        public EditPlotFieldWindow parent_;
        public PlotsListLsnr(EditPlotFieldWindow parent) {parent_ = parent;}
        public void actionPerformed(ActionEvent e) {
            int index = plotList_.getSelectedIndex();
            if (index == -1) return;
            Set thisSet = new Set(); thisSet.add(parent_);
            plotWindowCreator_.openWindow((Plot) plots_.elementAt(index), thisSet);
        }
    }
    
}
