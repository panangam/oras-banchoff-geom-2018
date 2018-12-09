//
//  OpBinRightOp.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class OpBinRightOp implements OpBin {

    private OpUn op_;

    /**
     * Creates a binary operator that always returns the given unary operator 
     * applied to the right operand.
     * @param op the operator to apply to the right operand
     */
    public OpBinRightOp(OpUn op) {
        op_ = op;
    }

    public Type type() { return op_.type(); }

    public Value operate(Value a, Value b) {
        return op_.operate(b);
    }

    public Value operate(Value[] vals) {
        return op_.operate(vals[1]);
    }    

}