//
//  OpBin.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

public interface OpBin extends Operator {

    /**
     * Operates on the given values and returns the resulting value.
     * @param a the left value to apply the operator to
     * @param b the right value to apply the operator to
     * @return the value resulting from operating on the given values
     */
    public Value operate(Value a, Value b);
    
}
