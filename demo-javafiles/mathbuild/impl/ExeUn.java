//
//  ExeUn.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class ExeUn implements Executor {

    private Executor exe_;
    private OpUn op_;

    /**
     * Creates a unary executor with two children, left and right.
     * @param exe the child executor
     * @param op the operator to apply to the value from the child
     */
    public ExeUn(Executor exe, OpUn op) {
        exe_ = exe;
        op_ = op;
    }

    public Value execute(Object runID) {
        return op_.operate(exe_.execute(runID));
    }

    public Type type() {
        return op_.type();
    }
    
}
