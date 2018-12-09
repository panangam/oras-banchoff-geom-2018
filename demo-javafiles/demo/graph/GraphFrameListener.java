//
//  GraphFrameListener.java
//  Demo
//
//  Created by David Eigen on Sat Apr 05 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.graph;

/**
 * Listener for creating and disposing of GraphFrames.
 * Register GraphFrameListeners with the Demo class (using demo.addGraphFrameListener(.)).
 */
public interface GraphFrameListener {

    /**
     * Called just after the given graph frame is created.
     */
    public void graphFrameCreated(GraphFrame frame);

    /**
     * Called just after the given graph frame is disposed.
     */
    public void graphFrameDisposed(GraphFrame frame);
    
}
