//
//  PlotOutputLists.java
//  Demo
//
//  Created by David Eigen on Thu Jul 31 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import demo.exec.*;
import demo.util.LinkedList;
import demo.gfx.PointSortable;
import demo.gfx.LightingVector;

public class PlotOutputLists implements PlotOutput {

    private LinkedList bufPoints_ = new LinkedList(), outPoints_ = new LinkedList();
    private LinkedList bufLVectors_ = new LinkedList(), outLVectors_ = new LinkedList();
    private LinkedList bufDrawables_ = new LinkedList(), outDrawables_ = new LinkedList();

    public  LinkedList makeBufferPoints() {
        return bufPoints_;
    }

    public  LinkedList makeBufferLightingVectors() {
        return bufLVectors_;
    }

    public  LinkedList makeBufferDrawables() {
        return bufDrawables_;
    }

    public  LinkedList bufferPoints() { return bufPoints_; }
    public  LinkedList bufferLightingVectors() { return bufLVectors_; }
    public  LinkedList bufferDrawables() { return bufDrawables_; }

    public  int numOutputPoints() {
        return outPoints_.size();
    }

    public  int copyOutputPoints( PointSortable[] array, int startIndex ) {
        outPoints_.resetEnumeration();
        int p = startIndex;
        while (outPoints_.hasMoreElements())
            array[p++] = (PointSortable) outPoints_.nextElement();
        return p;
    }

    public  int numOutputLightingVectors() {
        return outLVectors_.size();
    }

    public  int copyOutputLightingVectors( LightingVector[] array, int startIndex ) {
        outLVectors_.resetEnumeration();
        int p = startIndex;
        while (outLVectors_.hasMoreElements())
            array[p++] = (LightingVector) outLVectors_.nextElement();
        return p;
    }

    public  LinkedList outputDrawableObjects() {
        return outDrawables_;
    }

    public void setOutput() {
        Exec.begin_nocancel();
        outPoints_ = bufPoints_;
        bufPoints_ = new LinkedList();
        outLVectors_ = bufLVectors_;
        bufLVectors_ = new LinkedList();
        outDrawables_ = bufDrawables_;
        bufDrawables_ = new LinkedList();
        Exec.end_nocancel();
    }


}

