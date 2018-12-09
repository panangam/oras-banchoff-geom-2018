//
//  OpBinRight.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class OpBinRight implements OpBin {

    private Type returnType_;

    /**
     * Creates a binary operator that always returns the right operand.
     * @param returnType the type that the right operand will be
     */
    public OpBinRight(Type returnType) {
        returnType_ = returnType;
    }

    public Type type() { return returnType_; }

    public Value operate(Value a, Value b) {
        return b;
    }

    public Value operate(Value[] vals) {
        return vals[1];
    }
    
}