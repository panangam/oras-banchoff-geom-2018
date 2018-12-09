//
//  ParameterDummy.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

/**
 * Entry in an environment for a parameter.
 * Used for dummy entries: this entry doesn't actually store anything useful.
 */
public class ParameterDummy {

    private ParameterID param_;

    /**
     * Creates a parameter environment entry.
     * @param param the parameter
     */
    public ParameterDummy(ParameterID param) {
        param_ = param;
    }

    /**
     * @return the parameter for this entry
     */
    public ParameterID param() {
        return param_;
    }

}
