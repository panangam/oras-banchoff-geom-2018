//
//  ExeDummy.java
//  Demo
//
//  Created by David Eigen on Mon Aug 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;
import mathbuild.value.Value;
import mathbuild.type.Type;

/**
 * Dummy executor, which can be used as a dummy environment entry.
 */
public class ExeDummy implements Executor {

    public Value execute(Object runID) {
        throw new RuntimeException("Dummy executor should not be executed.");
    }

    public Type type() {
        throw new RuntimeException("Dummy executor should not have type() called.");
    }
    
}
