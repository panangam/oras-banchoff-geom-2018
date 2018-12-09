package demo.ui;
//
//  EditVisibleWindow.java
//  Demo
//
//  Created by David Eigen on Tue Jul 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;

public class EditVisibleWindow extends DemoFrame {

    private List list_ = new List();
    private ListObject[] objects_;

    private Checkbox itemCheckbox_ = new Checkbox("Item is Visible");

    private Button okBtn_ = new Button("OK");

    private EditVisibleWindowCallback callback_;

    int editingItem_;

    /**
     * @param objects the components that this EditVisibleWindow lists.
     * @param callback the callback for this window
     */
    public EditVisibleWindow(java.util.Enumeration objects, EditVisibleWindowCallback callback) {
        callback_ = callback;
        java.util.Vector objsVector = new java.util.Vector();
        while (objects.hasMoreElements())
            objsVector.addElement(objects.nextElement());
        objects_ = new ListObject[objsVector.size()];
        for (int i = 0; i < objects_.length; ++i) {
            objects_[i] = new ListObject();
            objects_[i].obj = objsVector.elementAt(i);
            objects_[i].title = callback.getTitle(objects_[i].obj);
        }
        demo.util.Sorter.sort(objects_, new ListObjectComparator());
        for (int i = 0; i < objects_.length; ++i) {
            list_.add(makeLabel(objects_[i].title, callback.isObjectVisible(objects_[i].obj)));
        }
        itemCheckbox_.addItemListener(new ItemCheckboxListener());
        list_.addItemListener(new ListListener());
        okBtn_.addActionListener(new OKListener(this));
        itemCheckbox_.disable();
        this.setLayout(new BorderLayout());
        this.add( list_,"Center");
        Panel checkboxPanel = new Panel();
        checkboxPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        checkboxPanel.add(itemCheckbox_);
        this.add( checkboxPanel,"North");
        Panel okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPanel.add(okBtn_);
        this.add( okPanel,"South");
        this.addWindowListener(new OKListener(this));
	setSize(250,300);
    }

    private void setEditItemMode(int item) {
        if (item == -1) {
            editingItem_ = item;
            itemCheckbox_.setEnabled(false);
            itemCheckbox_.setState(false);
        }
        else {
            editingItem_ = item;
            itemCheckbox_.setState(callback_.isObjectVisible(objects_[item].obj));
            itemCheckbox_.setEnabled(true);
        }
    }

    private void setObjectVisible(boolean vis) {
        callback_.setObjectVisible(objects_[editingItem_].obj, vis);
        String label = makeLabel(objects_[editingItem_].title, vis);
        list_.replaceItem(label, editingItem_);
        list_.select(editingItem_);
        itemCheckbox_.setState(vis);
    }

    private String makeLabel(String title, boolean visible) {
        if (visible)
            return title + "  [VISIBLE]";
        else
            return title + "  [HIDDEN]";
    }
    
    
    private class ListObject {
        Object obj;
        String title;
    }
    
    private class ListObjectComparator implements demo.util.SortComparator {
        public boolean isLessThanOrEqualTo(Object a, Object b) {
            return ((ListObject) a).title.compareTo(((ListObject) b).title) <= 0;
        }
    }

    private class ListListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEditItemMode(list_.getSelectedIndex());
            }
        }
    }

    private class ItemCheckboxListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setObjectVisible(e.getStateChange() == ItemEvent.SELECTED);
        }
    }

    private class OKListener extends WindowListenerNoActions implements ActionListener {
        EditVisibleWindow parent;
        public OKListener(EditVisibleWindow parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            callback_.editVisibleWindowClosed(parent);
            dispose();
        }
        public void windowClosing(WindowEvent e) {
            setVisible(false);
            callback_.editVisibleWindowClosed(parent);
            dispose();
        }
    }

}
