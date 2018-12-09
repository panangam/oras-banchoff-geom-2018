//
//  ExeCache.java
//  mathbuild
//
//  Created by David Eigen on Sun Jul 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.type.Type;

/**
 * ExeCache executes its child Executor, and stores the resulting value. If
 * execute(.) is called on the ExeCache in the same execution run, the value
 * obtained in the first execution is returned.
 */
public class ExeCache implements Executor {

    private Object runID_ = null;
    private Value value_;

    private Executor exe_;
    
    public ExeCache(Executor exe) {
        exe_ = exe;
    }

    /**
     * @return the Executor being cached.
     */
    public Executor exe() {
        return exe_;
    }
    
    public Value execute(Object runID) {
        if (runID_ == runID)
            return value_;
        // unset runID_ in case thread is cancelled sometime in the next 3 lines
        runID_ = null; 
        value_ = exe_.execute(runID);
        runID_ = runID;
        return value_;
    }

    public Type type() {
        return exe_.type();
    }
    
}
