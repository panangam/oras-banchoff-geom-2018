//
//  EditPlotCheckboxWindow.java
//  Demo
//
//  Created by David Eigen on Wed Apr 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot.ui;

import java.awt.*;
import java.awt.event.*;

import demo.ui.*;
import demo.depend.DependencyManager;
import demo.Demo;
import demo.plot.Plot;

public class EditPlotCheckboxWindow extends DemoFrame implements ActionListener, ItemListener {

    private PlotVisibleCheckbox mybox_;
    private List plotsList_ = new List();
    private java.util.Vector plots_ = new java.util.Vector();
    private java.util.Vector showWhenCheckedVals_ = new java.util.Vector();
    private Button addPlotBtn_ = new Button("Add Plot...");
    private Button removePlotBtn_ = new Button("Remove Plot");
    private Checkbox showWhenTrueCheckbox_ = new Checkbox("Show When True");
    private Button okBtn_ = new Button(" OK ");
    private TextField labelFld_ = new TextField(30);

    private Demo demo;
    private DemoFrame parentFrame;
    
    public EditPlotCheckboxWindow(DemoFrame parentFrame, PlotVisibleCheckbox checkbox, Demo demo) {
        mybox_ = checkbox;
        this.demo = demo;
        parentFrame.addOpenDialog(this);
        this.parentFrame = parentFrame;

        labelFld_.setText(mybox_.getLabel());
        java.util.Vector plots = mybox_.getPlots();
        java.util.Vector vals = mybox_.getShowAtStateValues();
        for (int i = 0; i < plots.size(); ++i) {
            plots_.addElement(plots.elementAt(i));
            showWhenCheckedVals_.addElement(vals.elementAt(i));
            plotsList_.add(((Plot) plots_.elementAt(i)).title());
        }

        plotsList_.addItemListener(this);
        addPlotBtn_.addActionListener(this);
        removePlotBtn_.addActionListener(this);
        showWhenTrueCheckbox_.addItemListener(this);
        okBtn_.addActionListener(this);
        labelFld_.addActionListener(this);
        
        setLayout(new BorderLayout());
        Panel okPnl = new Panel();
        okPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPnl.add(okBtn_);
        this.add(new SeperatedPanel(okPnl, "North"), "South");
        Panel centerPnl = new Panel();
        Panel northPnl = new Panel();
        northPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
        northPnl.add(new Label("Label: "));
        northPnl.add(labelFld_);
        this.add(northPnl, "North");
        centerPnl.setLayout(new BorderLayout());
        Panel centerNthPnl = new Panel();
        centerNthPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
        centerNthPnl.add(showWhenTrueCheckbox_);
        centerPnl.add(centerNthPnl, "North");
        centerPnl.add(plotsList_, "Center");
        Panel centerSthPnl = new Panel();
        centerSthPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
        centerSthPnl.add(addPlotBtn_);
        centerSthPnl.add(removePlotBtn_);
        centerPnl.add(centerSthPnl, "South");
        this.add(centerPnl, "Center");
        enableButtons(false);
        plotsList_.select(-1);
        this.pack();
        this.setVisible(true);
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == addPlotBtn_) {
            new MyAddPlotWindow();
        }
        else if (source == removePlotBtn_) {
            int i = plotsList_.getSelectedIndex();
            if (i == -1) {
                Demo.showError("There is no plot selected.");
                return;
            }
            Plot p = (Plot) plots_.elementAt(i);
            plotsList_.remove(i);
            plots_.removeElementAt(i);
            showWhenCheckedVals_.removeElementAt(i);
            mybox_.removePlot(p);
            plotsList_.select(-1);
            enableButtons(false);
        }
        else if (source == labelFld_) {
            mybox_.setLabel(labelFld_.getText());
        }
        else if (source == okBtn_) {
            mybox_.setLabel(labelFld_.getText());
            parentFrame.removeOpenDialog(this);
            dispose();
        }
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == plotsList_) {
            enableButtons(true);
            int i = plotsList_.getSelectedIndex();
            showWhenTrueCheckbox_.setState(((Boolean) showWhenCheckedVals_.elementAt(i))
                                           .booleanValue());
        }
        if (source == showWhenTrueCheckbox_) {
            int i = plotsList_.getSelectedIndex();
            if (i == -1) {
                Demo.showError("There is no plot selected.");
                return;
            }
            Plot p = (Plot) plots_.elementAt(i);
            mybox_.setShowAtStateValue(p, showWhenTrueCheckbox_.getState());
            if (p.isVisible() != (showWhenTrueCheckbox_.getState() ? mybox_.getState()
                                                                   : !mybox_.getState())) {
                p.setVisible(!p.isVisible());
                DependencyManager.updateDependentObjectsValMT(p);
            }
        }
    }

    public void enableButtons(boolean b) {
        removePlotBtn_.setEnabled(b);
        showWhenTrueCheckbox_.setEnabled(b);
    }
    

    private void addPlot(Plot p) {
        int i;
        for (i = 0; i < plots_.size(); ++i)
            if (plots_.elementAt(i) == p)
                break;
        if (i == plots_.size()) {
            // don't already have the plot, so add it
            plots_.addElement(p);
            boolean val = p.isVisible() ? mybox_.getState() : !mybox_.getState();
            showWhenCheckedVals_.addElement(new Boolean(val));
            plotsList_.add(((Plot) plots_.elementAt(i)).title());
            mybox_.addPlot(p, val);
        }
        selectIndex(i);
        enableButtons(true);
    }

    private void selectIndex(int i) {
        plotsList_.select(i);
        showWhenTrueCheckbox_.setState(((Boolean) showWhenCheckedVals_.elementAt(i))
                                       .booleanValue());
    }



    private class MyAddPlotWindow extends DemoFrame implements ActionListener {
        private Button okBtn = new Button(" OK ");
        private PlotChoice plotChoice = new PlotChoice(demo);
        public MyAddPlotWindow() {
            setLayout(new BorderLayout());
            Panel okPnl = new Panel();
            okPnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
            okPnl.add(okBtn);
            okBtn.addActionListener(this);
            this.add(okPnl, "South");
            this.add(new Label("Select Plot to Add:"), "North");
            this.add(plotChoice, "Center");
            pack();
            setVisible(true);
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okBtn) {
                addPlot(plotChoice.selectedPlot());
                dispose();
            }
        }
    }


}

