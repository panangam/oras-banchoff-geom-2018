//
//  DemoFrame.java
//  Demo
//
//  Created by David Eigen on Tue Jul 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.ui;
import demo.util.Set;

public class DemoFrame extends AWTFrame implements DialogListener {

    private Set openDialogs = new Set();
    
    public DemoFrame(String title) {
        super(title);
    }

    public DemoFrame() {
    }

    /**
     * Adds a java.awt.Window to the list of open windows associated with this DemoFrame.
     * When this DemoFrame is closed, all windows added with this method are also closed.
     * @param win the (open) window to be associated with this DemoFrame
     */
    public  void addOpenDialog( java.awt.Window win ) {
        openDialogs .put( win );
    }

    /**
    * Removes a java.awt.Window from the list of open windows associated with this DemoFrame.
     * @win the window to remove from the list
     */
    public  void removeOpenDialog( java.awt.Window win ) {
        openDialogs .remove( win );
    }

    public  void dispose() {
        java.util .Enumeration windows = openDialogs .elements();
        while ( windows .hasMoreElements() ) {
            ((java.awt .Window) windows .nextElement()) .dispose();
        }
        super .dispose();
    }


    public void dialogCanceled( java.awt.Window win ) {
        removeOpenDialog(win);
    }

    public void dialogOKed( java.awt.Window win ) {
        removeOpenDialog(win);
    }

}
