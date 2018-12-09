package demo.ui;
//
//  AWTMenuBar.java
//  Demo
//
//  Created by David Eigen on Fri Jul 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;

public class AWTMenuBar extends Object {

    AWTFrame frame_ = null;

    java.util.Vector menus_ = new java.util.Vector();
    java.awt.MenuBar bar_ = new MenuBar();

    public void add(AWTMenu m) {
        menus_.addElement(m);
        if (frame_ != null)
            frame_.menuAdded((AWTMenu) m);
        if (AWTFrame.USE_REAL_MENUS) {
            bar_.add(m.getMenu());
        }
    }

    public void remove(AWTMenu m) {
        menus_.remove(m);
        if (frame_ != null)
            frame_.menuRemoved((AWTMenu) m);
        if (AWTFrame.USE_REAL_MENUS) {
            bar_.remove(m.getMenu());
        }
    }

    public void remove(int i) {
        AWTMenu m = (AWTMenu) menus_.elementAt(i);
        menus_.removeElementAt(i);
        if (frame_ != null)
            frame_.menuRemoved(m);
        if (AWTFrame.USE_REAL_MENUS) {
            bar_.remove(i);
        }
    }

    public void setHelpMenu(Menu m) {
        throw new RuntimeException("Cannot do setHelpMenu on AWTMenuBar wrapper");
    }

    public AWTMenu getMenu(int i) {
        return (AWTMenu) menus_.elementAt(i);
    }

    public int getMenuCount() {
        return menus_.size();
    }

    public Font getFont() {
        return bar_.getFont();
    }

    /**
     * @return the internal java.awt.MenuBar being used if AWTFrame.USE_REAL_MENUS is true
     */
    public java.awt.MenuBar menubar() {
        return bar_;
    }
    
    /**
     * Sets the frame that this menubar is in.
     * Used internally by the AWT frame and menu wrapper classes.
     */
    public void setFrame(AWTFrame frame) {
        frame_ = frame;
    }
    
}
