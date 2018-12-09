//
//  DependencyUpdateErrorWindow.java
//  Demo
//
//  Created by David Eigen on Tue Aug 13 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.depend;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import demo.util.Set;

class DependencyUpdateErrorWindow extends demo.ui.DemoFrame implements ActionListener {

    // dictionary already open windows mapping from DependencyNode to window
    private static java.util.Dictionary openWindows_ = new java.util.Hashtable();

    private DependencyNode updatingNode_;
    private int updateLevel_;
    private int updateType_;
    private boolean phantom_;

    public DependencyUpdateErrorWindow(Exception ex,
                                       DependencyNode updatingNode,
                                       int updateLevel, int updateType,
                                       boolean phantom) {
        updatingNode_ = updatingNode;
        updateLevel_ = updateLevel;
        updateType_ = updateType;
        phantom_ = phantom;
        this.setLayout(new BorderLayout());
        this .add(new Label("   "), "North");
        this .add(new Label("   "), "East");
        this .add(new Label("   "), "West");
        Panel msgPanel = new Panel();
        msgPanel.setLayout(new GridLayout(0,1));
        msgPanel.add(new Label("The following error occured while updating:"));
        msgPanel.add(new Label(" "));
        msgPanel.add(new Label("  >  " + ex.getMessage() + "  < "));
        msgPanel.add(new Label(" "));
        msgPanel.add(new Label("Please fix this error and update again by clicking the"));
        msgPanel.add(new Label("\"Update\" button below."));
        this.add(msgPanel, "Center");
        Button updateBtn = new Button(" Update ");
        updateBtn.addActionListener(this);
        Panel btnPanel = new Panel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(updateBtn);
        this.add(btnPanel, "South");
        this.pack();
        this.setResizable(false);
        setLocation(100,50);
        removeWindow(updatingNode, updateLevel, updateType);
        addWindow(updatingNode, this);
        this.setVisible(true);
    }

    /**
     * Called when a DependencyUpdate succeeds. This method checks to see if there is an
     * error window open for the node with the same updateLevel and updateType as the update
     * that just succeeded. If there is, it closes the window.
     */
    public static void updateSucceeded(DependencyNode updatingNode,
                                       int updateLevel, int updateType) {
        removeWindow(updatingNode, updateLevel, updateType);
    }

    public void actionPerformed(ActionEvent ev) {
        this.dispose();
        DependencyManager.updateDependentObjectsImpl(updatingNode_, updateLevel_, updateType_, true, phantom_);
    }

    private static void removeWindow(DependencyNode updatingNode,
                              int updateLevel, int updateType) {
        Object winsObj = openWindows_.get(updatingNode);
        if (winsObj == null) return;
        Set windows = (Set) winsObj;
        for (java.util.Enumeration windowsEnum = windows.elements();
             windowsEnum.hasMoreElements();) {
            DependencyUpdateErrorWindow win = (DependencyUpdateErrorWindow) windowsEnum.nextElement();
            if (win.updateLevel_ == updateLevel &&
                win.updateType_ == updateType) {
                win.dispose();
                windows.remove(win);
                if (windows.size() == 0)
                    openWindows_.remove(updatingNode);
                break;
            }
        }
    }

    private static void addWindow(DependencyNode node, DependencyUpdateErrorWindow win) {
        Object winSet = openWindows_.get(node);
        if (winSet == null) {
            winSet = new Set();
            openWindows_.put(node, winSet);
        }
        ((Set) winSet).put(win);
    }


}
