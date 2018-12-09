package demo.ui;
//
//  WindowListenerNoActions.java
//  Demo
//
//  Created by David Eigen on Fri Jul 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/**
 * WindowListenerNoActions implements WindowListener and performs no actions for any of the events.
 * WindowListenerNoActions is used as a superclass for other window listeners.
 */
public class WindowListenerNoActions implements WindowListener {
    public void windowActivated(WindowEvent e) {
    }
    public void windowClosed(WindowEvent e) {
    }
    public void windowClosing(WindowEvent e) {
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
    }
}
