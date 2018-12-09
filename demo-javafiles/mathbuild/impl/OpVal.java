//
//  OpVal.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

/**
 * Operator that always returns a given value.
 */
public class OpVal implements OpBin, OpUn {

    private Value val_;
    
    public OpVal(Value val) {
        val_ = val;
    }
    
    public Value operate(Value a) {
        return val_;
    }

    public Value operate(Value a, Value b) {
        return val_;
    }

    public Type type() {
        return val_.type();
    }

    public Value operate(Value[] vals) {
        return val_;
    }
}
