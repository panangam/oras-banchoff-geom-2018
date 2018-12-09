//
//  LoadProgressFrame.java
//  Demo
//
//  Created by David Eigen on Mon Jul 01 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.io;

import demo.ui.ProgressBar;

public class LoadProgressFrame extends java.awt.Frame {

    private ProgressBar bar = new ProgressBar();

    public LoadProgressFrame() {
        super("Loading Demo");
        setLayout(new java.awt.BorderLayout());
        add( new java.awt.Label(" Loading Demo..."),"North");
        add( bar,"Center");
        add( new java.awt.Label(" "),"South");
        pack();
        setSize(this.getSize().width + 150, this.getSize().height + bar.getBarHeight() + 5);
        setLocation(300,200);
        setVisible(true);
    }

    public ProgressBar progressBar() {
        return bar;
    }

}
