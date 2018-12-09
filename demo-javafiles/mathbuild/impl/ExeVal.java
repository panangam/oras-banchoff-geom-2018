//
//  ExeVal.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

/**
 * A value executor stores a value, and simply returns the value when executed.
 */
public class ExeVal implements Executor {

    private Value val_;

    /**
     * Creates a value executor with the given value.
     */
    public ExeVal(Value val) {
        val_ = val;
    }

    /**
     * @return the value that this ExeVal returns
     */
    public Value val() {
        return val_;
    }

    public Value execute(Object runID) {
        return val_;
    }

    public Type type() {
        return val_.type();
    }
    
}
