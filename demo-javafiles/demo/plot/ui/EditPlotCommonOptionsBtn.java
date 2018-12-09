package demo.plot.ui;
//
//  EditPlotExtraOptionsBtn.java
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
import demo.ui.FlowVerticalLayout;


/**
 * A button that brings up a dialog for specifying more advanced options for plots,
 * such as the layer level and transparency threshold.
 */
public class EditPlotCommonOptionsBtn extends Panel implements ActionListener {

    private Button btn_;
    private Frame parent_;
    private Environment environment_;
    private Plot plot_;
    
    public EditPlotCommonOptionsBtn(Plot plot, Environment env, Frame parentFrame) {
        plot_ = plot;
        parent_ = parentFrame;
        environment_ = env;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        btn_ = new Button("More Options...");
        btn_.addActionListener(this);
        this.add(btn_);
    }

    public void actionPerformed(ActionEvent e) {
        new EditPlotCommonOptionsDialog();
    }


    
    private class EditPlotCommonOptionsDialog extends Dialog implements ActionListener {

        private Button okBtn_ = new Button(" OK ");
        
        public EditPlotCommonOptionsDialog() {
            super(parent_);
            this.setLayout(new BorderLayout());
            okBtn_.addActionListener(this);
            Panel centerPanel = new Panel();
            centerPanel.setLayout(new FlowVerticalLayout());
            centerPanel.add(new ExtraZInput(plot_));
            centerPanel.add(new TransparencyThresholdInput(plot_, environment_));
            Panel okPanel = new Panel();
            okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            okPanel.add(okBtn_);
            this.add( centerPanel,"Center");
            this.add( okPanel,"South");
            this.setResizable(false);
            this.pack();
            this.setVisible(true);
        }

        public void actionPerformed(ActionEvent ev) {
            if (ev.getSource() == okBtn_)
                dispose();
        }
        
    }

}
