//
//  OpIdentity.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

public class OpIdentity implements OpUn {

    private Type type_;
    
    /**
     * constructs an identity operator for the given type
     */
    public OpIdentity(Type t) {
        type_ = t;
    }
    
    public Type type() {
        return type_;
    }

    public Value operate(Value v) {
        return v;
    }

    public Value operate(Value[] vals) {
        return vals[0];
    }
    
}
