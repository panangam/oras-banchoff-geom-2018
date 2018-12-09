package demo.plot.ui;
//
//  ExtraZSlider.java
//  Demo
//
//  Created by David Eigen on Mon Aug 05 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import demo.depend.DependencyManager;
import demo.plot.Plot;
import demo.ui.Slider;

public class ExtraZInput extends Panel implements ActionListener {

    private Plot plot_;
    private Slider slider_;
    
    public ExtraZInput(Plot plot) {
        plot_ = plot;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        slider_ = new Slider(-30,30,3);
        this.add(new Label("Out of screen adjustment: "));
        this.add(slider_);
        slider_.addActionListener(this);
        slider_.setValue(plot_.extraZ());
    }

    public void actionPerformed(ActionEvent e) {
        plot_.setExtraZ(slider_.value());
        DependencyManager.updateDependentObjectsValMT(plot_);
    }

}
