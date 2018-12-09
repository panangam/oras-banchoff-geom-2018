//
//  OpUn.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

public interface OpUn extends Operator {

    /**
     * Operates on the given value and returns the resulting value.
     * @param val the value to operate on
     * @return the value resulting from operating on the given values
     */
    public Value operate(Value val);
    
}
