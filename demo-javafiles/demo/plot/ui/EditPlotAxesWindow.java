//
//  EditPlotAxesWindow.java
//  Demo
//
//  Created by David Eigen on Fri Apr 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot.ui;

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;

import demo.ui.*;
import demo.depend.*;
import demo.Demo;
import demo.coloring.ui.EditColoringGroupPanel;
import demo.coloring.ui.EditColoringListener;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.plot.Plot;
import demo.plot.PlotAxes;

public class EditPlotAxesWindow extends EditPlotWindow {

    private int dimension_;

    private Button okBtn_ = new Button(" OK ");

    private TextField xlabelFld_ = new TextField(10);
    private TextField ylabelFld_ = new TextField(10);
    private TextField zlabelFld_ = new TextField(10);

    public EditPlotAxesWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotAxes(dimension == 3 ? new String[]{"x", "y", "z"}
                                          : dimension == 2 ? new String[]{"x", "y"}
                                                           : new String[dimension]),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotAxesWindow( PlotAxes plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotAxesWindow( PlotAxes plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );
        dimension_ = dimension;

        String[] labels = plot.labels();
        if (labels.length >= 1)
            xlabelFld_.setText(labels[0]);
        if (labels.length >= 2)
            ylabelFld_.setText(labels[1]);
        if (labels.length >= 3)
            zlabelFld_.setText(labels[2]);
            
        ActionListener lsnr = new LabelFldLsnr();
        xlabelFld_.addActionListener(lsnr);
        ylabelFld_.addActionListener(lsnr);
        zlabelFld_.addActionListener(lsnr);
        okBtn_.addActionListener(new OKBtnLsnr());
        this.addWindowListener(new WindowLsnrCancel());

        Panel okPnl = new Panel();
        okPnl.setLayout(new BorderLayout());
        okPnl.add(okBtn_, "East");

        Panel labelsPnl = new Panel();
        labelsPnl.setLayout(new GridLayout(0,2));
        labelsPnl.add(new Label("X-axis label:"));
        labelsPnl.add(xlabelFld_);
        labelsPnl.add(new Label("Y-axis label:"));
        labelsPnl.add(ylabelFld_);
        if (dimension == 3) {
            labelsPnl.add(new Label("Z-axis label:"));
            labelsPnl.add(zlabelFld_);
        }
        else if (dimension != 2)
            throw new RuntimeException("Can only make axes edit window for dims 2 and 3 ");
        
        this.setLayout(new BorderLayout());
        this.add(new SeperatedPanel(okPnl, "North"), "South");
        this.add(new SeperatedPanel(new PlotVisibleCheckbox("Plot is Visible", plot),
                                    "West"),
                 "East");
        this.add(labelsPnl, "Center");
        
        this.pack();
        this.setVisible(true);
    }

    private void setLabels() {
        String[] strs = new String[dimension_];
        strs[0] = xlabelFld_.getText();
        strs[1] = ylabelFld_.getText();
        if (dimension_ == 3)
            strs[2] = zlabelFld_.getText();
        ((PlotAxes) plot).setLabels(strs);
        updatePlot();
    }
    
    private class LabelFldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setLabels();
        }
    }

    private class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent e) {
            setLabels();
            super.actionPerformed(e);
        }
    }
    
}
