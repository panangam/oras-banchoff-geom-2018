//
//  SNParam.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

/**
 * Syntax node that, when built, simply returns a parameter.
 */
public class SNParam extends SyntaxNode {

    private ParameterID param_;

    /**
     * @param param the parameter whose executor should be the result
     *        of building this SNParam
     */
    public SNParam(ParameterID param) {
        param_ = param;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        return funcParams.peek()[param_.num()];
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return ((ParameterDeriv) env.lookup(param_.name())).node();
    }

    public SyntaxNode simplify() {
        return this;
    }

    public String toString() {
        return "(SN-param " + param_.name() + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
    }
    
}
