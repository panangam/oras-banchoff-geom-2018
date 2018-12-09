//
//  ExeBin.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class ExeBin implements Executor {

    private Executor left_, right_;
    private OpBin op_;

    /**
     * Creates a binary executor with two children, left and right.
     * @param left the left child executor
     * @param right the right child executor
     * @param op the operator to apply to the values from the children
     */
    public ExeBin(Executor left, Executor right, OpBin op) {
        left_ = left; right_ = right;
        op_ = op;
    }

    public Value execute(Object runID) {
        return op_.operate(left_.execute(runID), right_.execute(runID));
    }

    public Type type() {
        return op_.type();
    }
    
}
