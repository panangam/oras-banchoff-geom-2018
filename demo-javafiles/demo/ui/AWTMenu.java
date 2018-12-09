package demo.ui;
//
//  AWTMenu.java
//  Demo
//
//  Created by David Eigen on Fri Jul 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.MenuComponent;

public class AWTMenu extends Object {

    Menu menu_;

    public AWTMenu() {
        menu_ = AWTFrame.USE_REAL_MENUS ? new Menu() : new PopupMenu();
    }

    public AWTMenu(String s) {
        menu_ = AWTFrame.USE_REAL_MENUS ? new Menu(s) : new PopupMenu(s);
    }

    public void setLabel(String s) {
        menu_.setLabel(s);
    }
    
    public String getLabel() {
        return menu_.getLabel();
    }

    public void setEnabled(boolean b) {
        menu_.setEnabled(b);
    }

    public MenuItem add(MenuItem item) {
        return menu_.add(item);
    }

    public void add(String item) {
        menu_.add(item);
    }

    public void addSeparator() {
        menu_.addSeparator();
    }

    public MenuItem getItem(int i) {
        return menu_.getItem(i);
    }

    public int getItemCount() {
        return menu_.getItemCount();
    }

    public void insert(MenuItem item, int index) {
        menu_.insert(item, index);
    }

    public void insert(String label, int index) {
        menu_.insert(label, index);
    }

    public void insertSeparator(int index) {
        menu_.insertSeparator(index);
    }

    public void remove(int index) {
        menu_.remove(index);
    }

    public void remove(MenuComponent item) {
        menu_.remove(item);
    }

    public void removeAll() {
        menu_.removeAll();
    }


    /**
     * Used internally by the AWTFrame, AWTMenuBar, AWTMenuCanvas classes.
     */
    public PopupMenu getPopupMenu() {
        return (PopupMenu) menu_;
    }

    /**
     * Used internally by the AWTFrame, AWTMenuBar, AWTMenuCanvas classes.
     */
    public Menu getMenu() {
        return menu_;
    }
    
    
}
