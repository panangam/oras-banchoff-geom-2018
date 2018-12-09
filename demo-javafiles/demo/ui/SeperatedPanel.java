//
//  SeperatedPanel.java
//  Demo
//
//  Created by David Eigen on Fri Apr 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.ui;

import java.awt.Panel;
import java.awt.Component;
import java.awt.BorderLayout;

/**
 * A panel that contains a Component, and draws seperators to the
 * north, south, east or west as specified.
 */
public class SeperatedPanel extends Panel {

    /**
     * Creates a component that contains the given component, and
     * draws seperators in the directions given by the sep parameters.
     * These params can be "North", "South", "East", or "West"
     */
    public SeperatedPanel(Component comp) {
        this(comp, new String[0]);
    }

    /**
     * Creates a component that contains the given component, and
     * draws seperators in the directions given by the sep parameters.
     * These params can be "North", "South", "East", or "West"
     */
    public SeperatedPanel(Component comp, String sep1) {
        this(comp, new String[]{sep1});
    }

    /**
     * Creates a component that contains the given component, and
     * draws seperators in the directions given by the sep parameters.
     * These params can be "North", "South", "East", or "West"
     */
    public SeperatedPanel(Component comp, String sep1, String sep2) {
        this(comp, new String[]{sep1, sep2});
    }

    /**
     * Creates a component that contains the given component, and
     * draws seperators in the directions given by the sep parameters.
     * These params can be "North", "South", "East", or "West"
     */
    public SeperatedPanel(Component comp, String sep1, String sep2, String sep3) {
        this(comp, new String[]{sep1, sep2, sep3});
    }

    /**
     * Creates a component that contains the given component, and
     * draws seperators in the directions given by the sep parameters.
     * These params can be "North", "South", "East", or "West"
     */
    public SeperatedPanel(Component comp, String sep1, String sep2, String sep3, String sep4) {
        this(comp, new String[]{sep1, sep2, sep3, sep4});
    }

    private SeperatedPanel(Component comp, String[] seps) {
        setLayout(new BorderLayout());
        add(comp, "Center");
        for (int i = 0; i < seps.length; ++i)
            add(new PanelSeperator(seps[i].equals("East") || seps[i].equals("West") ?
                                   PanelSeperator.VERTICAL : PanelSeperator.HORIZONTAL),
                seps[i]);
    }

}
