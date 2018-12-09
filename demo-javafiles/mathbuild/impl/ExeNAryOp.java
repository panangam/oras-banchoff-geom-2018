//
//  ExeNAryOp.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class ExeNAryOp implements Executor {

    private Operator op_;
    private Executor[] args_;

    /**
     * Creates an n-ary executor with n children.
     * @param args the child executors to apply to the operator
     * @param op the operator to apply to the values from the children
     */
    public ExeNAryOp(Executor[] args, Operator op) {
        op_ = op;
        args_ = args;
    }

    public Value execute(Object runID) {
        Value[] vals = new Value[args_.length];
        for (int i = 0; i < args_.length; ++i)
            vals[i] = args_[i].execute(runID);
        return op_.operate(vals);
    }

    public Type type() {
        return op_.type();
    }
    
}
