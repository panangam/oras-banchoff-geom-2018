//
//  RemoveButton.java
//  Demo
//
//  Created by David Eigen on Sat Aug 17 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.*;
import java.awt.event.*;


/**
 * A RemoveButton is a small "button" (actually a graphic) that the user can click on. It can
 * dispatch events to ActionListeners just like a java.awt.Button. RemoveButton doesn't actually
 * do anything in terms of removing stuff -- it is just a picture of an "x" that can say when
 * it has been clicked to ActionListeners.
 */
public class RemoveButton extends LittleButton {

    // note: width, height, length should be even numbers
    private static final int WIDTH = 14, HEIGHT = 14;
    private static final int EX_LENGTH = 6;

    public RemoveButton() {
        super();
        width_ = WIDTH;
        height_ = HEIGHT;
        setSize(width_, height_);
    }
    
    protected void paintGraphic(Graphics g) {
        int cx = WIDTH/2+1, cy = HEIGHT/2+1;
        int l = EX_LENGTH/2;
        g.drawLine( cx-l, cy-l, cx+l, cy+l);
        g.drawLine( cx-l, cy+l, cx+l, cy-l);        
    }

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH+2, HEIGHT+2);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
