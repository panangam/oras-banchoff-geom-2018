package demo.ui;
//
//  WindowListenerDispose.java
//  Demo
//
//  Created by David Eigen on Fri Jul 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.Window;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/**
 * Disposes a Window when the windowClosing event happens.
 */
public class WindowListenerDispose extends WindowListenerNoActions {
    Window win_;

    /**
     * Constructs a WindowListener that dispose the given Window w when windowClosing is called
     * @param w the window to dispose when its close box is pressed
     */
    public WindowListenerDispose(Window w) {
        win_ = w;
    }
    
    public void windowClosing(WindowEvent e) {
        win_.dispose();
    }
}
