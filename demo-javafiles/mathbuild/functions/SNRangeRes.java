//
//  SNRangeRes.java
//  mathbuild
//
//  Created by David Eigen on Sun Aug 11 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class SNRangeRes extends SyntaxNode {

    private SyntaxNode range_;

    public SNRangeRes(SyntaxNode range) {
        range_ = range;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{range_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor rangeExe = range_.build(env, funcParams, buildArgs);
        while (rangeExe instanceof ExeCache)
            rangeExe = ((ExeCache) rangeExe).exe();
        if ( ! (rangeExe instanceof Range) )
            throw new BuildException("Can only get the resolution of a range (interval or variable).");
        return new ExeRangeRes((Range) rangeExe);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNNumber(0);
    }

    public SyntaxNode simplify() {
        return this;
    }

    public String toString() {
        return "(SN-range-res " + range_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        // ranges not actually dependent inside range end function
        range_.findDependencies(env, deps, new demo.util.Set());
    }

}


class ExeRangeRes implements Executor {
    private Range range_;
    public ExeRangeRes(Range range) { range_ = range; }
    public Type type() { return MB.TYPE_SCALAR; }
    public Value execute(Object runID) {
        return range_.res(runID);
    }
}

