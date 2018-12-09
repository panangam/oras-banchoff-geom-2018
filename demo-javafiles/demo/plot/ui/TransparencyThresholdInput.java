package demo.plot.ui;
//
//  TransparencyThresholdInput.java
//  Demo
//
//  Created by David Eigen on Mon Aug 05 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import mathbuild.Environment;

import demo.plot.Plot;
import demo.depend.DependencyManager;
import demo.expr.Expression;

public class TransparencyThresholdInput extends Panel implements ActionListener {

    private Plot plot_;
    private TextField field_;
    private Environment environment_;

    public TransparencyThresholdInput(Plot plot, Environment env) {
        plot_ = plot;
        environment_ = env;
        field_ = new TextField(8);
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(new Label("Transparency Threshold:"));
        this.add(field_);
        field_.addActionListener(this);
        field_.setText(plot_.transparencyThreshold().definitionString());
    }

    public void actionPerformed(ActionEvent e) {
        Expression expr = demo.Demo.recognizeExpression(field_.getText(), environment_);
        if (expr == null)
            return;
        plot_.setTransparencyThreshold(expr);
        DependencyManager.updateDependentObjectsDefMT(plot_);
    }

}
