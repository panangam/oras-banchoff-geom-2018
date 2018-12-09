//
//  AddPlotMenu.java
//  Demo
//
//  Created by David Eigen on Fri Aug 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.plot.ui;

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;

import demo.Demo;
import demo.util.Set;

public class AddPlotMenuCreator {

    // ******* ADD TO FOLLOWING MENUITEMS FOR NEW PLOT ******** //
    private AddPlotMenuItem[] menuItems_ = new AddPlotMenuItem[] {
        new AddPlotMenuItem("Point", EditPlotWindowCreator.PLOT_TYPE_POINT),
        new AddPlotMenuItem("Vector", EditPlotWindowCreator.PLOT_TYPE_VECTOR),
        new AddPlotMenuItem("Curve", EditPlotWindowCreator.PLOT_TYPE_CURVE),
        new AddPlotMenuItem("Surface", EditPlotWindowCreator.PLOT_TYPE_SURFACE),
        new AddPlotMenuItem("Wireframe", EditPlotWindowCreator.PLOT_TYPE_WIREFRAME),
        new AddPlotMenuItem("Polygon", EditPlotWindowCreator.PLOT_TYPE_POLYGON),
        new AddPlotMenuItem("Polyhedron", EditPlotWindowCreator.PLOT_TYPE_POLYHEDRON),
        new AddPlotMenuItem("Field", EditPlotWindowCreator.PLOT_TYPE_FIELD),
        new AddPlotMenuItem("Level Set", EditPlotWindowCreator.PLOT_TYPE_LEVELSET)
    };


    private Demo demo_;
    private EditPlotWindowCreator creator_;
    private Set editPlotWindowListeners_ = new Set();
    private int dimension_;

    /**
     * Creates a new AddPlotMenuCreator. An AddPlotMenuCreator is used to make menus or popup menus
     * for adding plots.
     * @param demo the demo class
     * @param env the expresison environment expressions in plots should be interpreted in
     * @param dimension the dimension of the plots to be created
     * @param listener a listener for the created EditPlotWindows
     */
    public AddPlotMenuCreator(Demo demo, Environment env, int dimension, EditPlotWindowListener listener) {
        demo_ = demo;
        dimension_ = dimension;
        creator_ = new EditPlotWindowCreator(demo, env, dimension);
        editPlotWindowListeners_.add(listener);
    }

    /**
     * Creates a new menu for adding plots.
     * @return a java.awt.Menu that can be used as a submenu for adding plots
     */
    public java.awt.Menu makeMenu() {
        java.awt.Menu menu = new java.awt.Menu("Add Plot");
        initMenu(menu);
        return menu;
    }

    /**
     * Creates a new popup menu for adding plots.
     * @return a java.awt.PopupMenu for adding plots
     */
    public java.awt.PopupMenu makePopupMenu() {
        java.awt.PopupMenu menu = new java.awt.PopupMenu();
        initMenu(menu);
        return menu;
    }

    /**
     * Creates a new Choice for adding plots. This isn't quite a pull-down menu, but 
     * it's the closest that Java 1.1 has.
     */
    public java.awt.Choice makeChoice() {
        java.awt.Choice choice = new java.awt.Choice();
        initChoice(choice);
        return choice;
    }
    
    
    private void initMenu(java.awt.Menu menu) {
        AddPlotMenuItemLsnr lsnr = new AddPlotMenuItemLsnr();
        for (int i = 0; i < menuItems_.length; ++i) {
            if (shouldContainMenuItem(menuItems_[i])) {
                menu.add(menuItems_[i]);
                menuItems_[i].addActionListener(lsnr);
            }
        }
    }

    private void initChoice(java.awt.Choice choice) {
        choice.addItem("Add Plot");
        for (int i = 0; i < menuItems_.length; ++i) {
            if (shouldContainMenuItem(menuItems_[i])) {
                choice.addItem(menuItems_[i].getLabel());
            }
        }
        choice.addItemListener(new AddPlotMenuItemLsnr());
        choice.select(0);
    }
    

    /**
     * Can do more things later, and maybe use constructor parameters to see which kind of
     * plots can be added. For now, just will say to add everything except for
     * surface in a 2D plot.
     */
    private boolean shouldContainMenuItem(AddPlotMenuItem menuitem) {
        // ****************** EDIT THIS WHEN ADDING NEW PLOT ******************* //
        return ! (dimension_ == 2 &&
                  (menuitem.plotType() == EditPlotWindowCreator.PLOT_TYPE_SURFACE ||
                   menuitem.plotType() == EditPlotWindowCreator.PLOT_TYPE_LEVELSET));
    }

    private class AddPlotMenuItemLsnr implements ActionListener, ItemListener {
        public void actionPerformed(ActionEvent ev) {
            AddPlotMenuItem mi = (AddPlotMenuItem) ev.getSource();
            creator_.openWindow(mi.plotType(), editPlotWindowListeners_);
        }
        public void itemStateChanged(ItemEvent ev) {
            String item = (String) ev.getItem();
            for (int i = 0; i < menuItems_.length; ++i)
                if (menuItems_[i].getLabel().equals(item))
                    creator_.openWindow(menuItems_[i].plotType(), editPlotWindowListeners_);
            ((java.awt.Choice) ev.getSource()).select(0);
        }
    }

    private class AddPlotMenuItem extends MenuItem {
        private int plotType_;
        public AddPlotMenuItem(String label, int type) {
            super(label);
            plotType_ = type;
        }
        public int plotType() { return plotType_; }
    }

}
