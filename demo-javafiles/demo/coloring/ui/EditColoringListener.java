//
//  EditColoringListener.java
//  Demo
//
//  Created by David Eigen on Mon Jun 10 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.coloring.ui;

import demo.coloring.Coloring;

public interface EditColoringListener {

    /**
     * This method is called when there is a change in the edit coloring UI's coloring
     * that needs to be reflected in the rest of the program. This could happen, for
     * example, when the user edits a EditColoringGroupPanel, and the updated coloring
     * needs to go to the rest of the program.
     * @param coloring the new coloring -- this is the newly-build coloring that has the new changes
     */
    public void coloringChanged(Coloring coloring);
    
}
