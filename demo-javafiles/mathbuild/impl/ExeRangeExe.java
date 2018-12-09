//
//  ExeRangeExe.java
//  mathbuild
//
//  Created by David Eigen on Sun Aug 11 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

/**
 * Implements ExeRange simply by storing Executor objects the start, end, and resolution
 */
public class ExeRangeExe extends ExeRange {

    private Executor start_, end_, res_;

    public ExeRangeExe(Executor start, Executor end, Executor res) {
        start_ = start; end_ = end; res_ = res;
    }

    public Value start(Object runID) {
        return start_.execute(runID);
    }

    public Value end(Object runID) {
        return end_.execute(runID);
    }

    public Value res(Object runID) {
        return res_.execute(runID);
    }
    

}
