//
//  OpBinLeft.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class OpBinLeft implements OpBin {

    private Type returnType_;

    /**
     * Creates a binary operator that always returns the left operand.
     * @param returnType the type that the left operand will be
     */
    public OpBinLeft(Type returnType) {
        returnType_ = returnType;
    }

    public Type type() { return returnType_; }

    public Value operate(Value a, Value b) {
        return a;
    }

    public Value operate(Value[] vals) {
        return vals[0];
    }
    
}
