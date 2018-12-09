//
//  PlotOutputArray.java
//  Demo
//
//  Created by David Eigen on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import demo.gfx.PointSortable;
import demo.gfx.LightingVector;
import demo.util.LinkedList;
import demo.exec.*;

/**
 * Plot output that keeps track of everything using 1D vertex (point) arrays, and
 * linked lists for drawables.
 */
public class PlotOutputArrays implements PlotOutput {

    private PointSortable[]
        bufferPoints_ = new PointSortable[0],
        outputPoints_ = new PointSortable[0];

    private LightingVector[]
        bufferLightingVectors_ = new LightingVector[0],
        outputLightingVectors_ = new LightingVector[0];

    private LinkedList
        bufferDrawables_ = new LinkedList(),
        outputDrawables_ = new LinkedList();

    /**
     * Creates a buffer points array of the given size. Uses the old buffer if it's the
     * right size, creates a new one if necessary.
     * @param numpoints the length of the buffer points array
     */
    public PointSortable[] makeBufferPoints(int numpoints) {
        if (bufferPoints_.length != numpoints)
            bufferPoints_ = new PointSortable[numpoints];
        return bufferPoints_;
    }

    /**
     * @return the buffer point array (does not create a new one)
     */
    public PointSortable[] bufferPoints() {
        return bufferPoints_;
    }

    /**
     * Creates a buffer lighting vectors array of the given size. Uses the old buffer
     * if it's the right size, creates a new one if necessary.
     * @param numpoints the length of the buffer vectors array
     */
    public LightingVector[] makeBufferLightingVectors(int numvecs) {
        if (bufferLightingVectors_.length != numvecs)
            bufferLightingVectors_ = new LightingVector[numvecs];
        return bufferLightingVectors_;
    }

    /**
     * @return the buffer lighting vectors array (does not create a new one)
     */
    public LightingVector[] bufferLightingVectors() {
        return bufferLightingVectors_;
    }

    /**
     * Creates a buffer points array of the given size. Uses the old buffer if it's the
     * right size, creates a new one if necessary.
     * @param numpoints the length of the buffer points array
     */
    public LinkedList makeBufferDrawables() {
        return bufferDrawables_;
    }

    /**
     * @return the buffer point array (does not create a new one)
     */
    public LinkedList bufferDrawables() {
        return bufferDrawables_;
    }

    public int numOutputPoints() {
        return outputPoints_.length;
    }

    public int copyOutputPoints(PointSortable[] array, int startIndex) {
        System.arraycopy(outputPoints_, 0, array, startIndex, outputPoints_.length);
        return startIndex + outputPoints_.length;
    }

    public int numOutputLightingVectors() {
        return outputLightingVectors_.length;
    }

    public int copyOutputLightingVectors(LightingVector[] array, int startIndex) {
        System.arraycopy(outputLightingVectors_, 0,
                         array, startIndex,
                         outputLightingVectors_.length);
        return startIndex + outputLightingVectors_.length;
    }

    public LinkedList outputDrawableObjects() {
        return outputDrawables_;
    }

    /**
     * Sets the current buffer to the current output. Thread cancelability is handled.
     */
    public void setOutput() {
        Exec.begin_nocancel();
        PointSortable[] tmp = outputPoints_;
        outputPoints_ = bufferPoints_;
        bufferPoints_ = tmp;
        LightingVector[] tmpv = outputLightingVectors_;
        outputLightingVectors_ = bufferLightingVectors_;
        bufferLightingVectors_ = tmpv;
        outputDrawables_ = bufferDrawables_;
        bufferDrawables_ = new LinkedList();
        Exec.end_nocancel();
    }
    

}
