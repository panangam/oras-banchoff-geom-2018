//
//  ParameterExe.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

/**
 * Entry in an environment for a parameter.
 * Stores an executable that the parameter is bound to in the environment.
 */
public class ParameterExe {

    private ParameterID param_;
    private Executor exe_;

    /**
     * Creates a parameter environment entry.
     * @param param the parameter
     * @param exe the executor being plugged into this parameter
     */
    public ParameterExe(ParameterID param, Executor exe) {
        param_ = param;
        exe_ = exe;
    }

    /**
     * @return the parameter for this entry
     */
    public ParameterID param() {
        return param_;
    }
    
    /**
     * @return the parameter number of this parameter
     */
    public int num() {
        return param_.num();
    }

    /**
     * @return the name of this parameter
     */
    public String name() {
        return param_.name();
    }

    /**
     * @return the executor being plugged into this parameter
     */
    public Executor exe() {
        return exe_;
    }

    
    
}
