package demo.plot;
//
//  PlotListener.java
//  Demo
//
//  Created by David Eigen on Wed Jul 31 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

/**
 * A class that owns a plot, such as a Graph or another Plot that uses child Plots to make
 * drawables, must implement PlotListener. Any other class may also implement PlotListener.
 */
public interface PlotListener {

    /**
     * Called just before a plot is disposed. If all listeners return true, the
     * plot will be disposed. If any listeners return false, the plot is not
     * allowed to be disposed, and is not disposed.
     * @param plot the plot that will be disposed if listeners allow it
     */
    public boolean plotCanDispose(Plot plot);

    /**
     * Called when a plot owned by this owner is disposed.
     */
    public void plotDisposed(Plot plot);

}
