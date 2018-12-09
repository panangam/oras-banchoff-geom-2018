//
//  PlotOutput.java
//  Demo
//
//  Created by David Eigen on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import demo.gfx.*;
import demo.util.LinkedList;

/**
 * Output for plots. Contains vertex arrays and drawable objects. Normals in future.
 * Does thread synchronization for double-buffering the arrays, to allow thread
 * cancelling during plot calculating.
 */
public interface PlotOutput {

    /**
     * @return the number of output points
     */
    public int numOutputPoints();

    /**
     * Copies all output points into the given array, starting at
     * index startIndex.
     * The points all have references to all Drawable objects produced
     * by the plot.
     * @param array the array to copy the points into
     * @param startIndix the index in the array to start copying at.
     * @return the index of in the array after the last index a point
     * was copied to (that is, the next free index).
     */
    public int copyOutputPoints(PointSortable[] array, int startIndex);

    /**
     * @return a LinkedList containing the drawable objects for this output
     */
    public LinkedList outputDrawableObjects();

    /**
     * @return the number of lighting vectors
     */
    public int numOutputLightingVectors();

    /**
     * Copies all lighting vectors into the given array, startingat startIndex.
     * Returns the next free index.
     * @param array the array to copy the vectors into
     * @param startIndix the index in the array to start copying at.
     * @return the index of in the array after the last index a vector
     * was copied to (that is, the next free index).
     */
    public int copyOutputLightingVectors(LightingVector[] array, int startIndex);
    
}
