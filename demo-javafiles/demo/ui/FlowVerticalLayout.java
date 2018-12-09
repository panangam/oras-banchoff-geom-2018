package demo.ui;
//
//  FlowVerticalLayout.java
//  Demo
//
//  Created by David Eigen on Thu Jul 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;

public class FlowVerticalLayout implements LayoutManager {

    /**
     * horizontal alignment
     */
    public static final int LEFT = 0, RIGHT = 1;

    private int horizontalAlignment_ = LEFT;

    public FlowVerticalLayout() {
    }

    public FlowVerticalLayout(int horizontalAlignment) {
        horizontalAlignment_ = horizontalAlignment;
    }
    
    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        int width = 0;
        int height = 0;
        Component[] comps = parent.getComponents();
        for (int i = 0; i < comps.length; ++i) {
            Dimension d = comps[i].getPreferredSize();
            if (d.width > width)
                width = d.width;
            height += d.height;
        }
        return new Dimension(width, height);
    }

    public void layoutContainer(Container parent) {
        int x = 0; int y = 0;
        Component[] comps = parent.getComponents();
        int parentWidth = parent.getSize().width;
        for (int i = 0; i < comps.length; ++i) {
            Component c = comps[i];
            c.setSize(c.getPreferredSize());
            if (c instanceof PanelSeperator)
                c.setSize(parentWidth, c.getSize().height);
            if (horizontalAlignment_ == LEFT)
                c.setLocation(x,y);
            else if (horizontalAlignment_ == RIGHT) {
                c.setLocation(parentWidth - c.getSize().width, y);
            }
            else
                c.setLocation(x,y);
            y += c.getSize().height;
        }            
    }
    
}
