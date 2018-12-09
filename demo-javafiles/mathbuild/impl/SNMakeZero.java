//
//  SNMakeZero.java
//  Demo
//
//  Created by David Eigen on Mon Aug 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

/**
 * Makes a zero out of a child node. That is, wraps a child SyntaxNode. When
 * built, this node builds the child node, and returns an Executor that returns
 * a zero value of the same type as the Executor obtained from the child node.
 */
public class SNMakeZero extends SyntaxNode {

    private SyntaxNode sn_;

    /**
     * @param sn the SyntaxNode that should be wrapped, and made into a Zero value
     */
    public SNMakeZero(SyntaxNode sn) {
        sn_ = sn;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{sn_};
    }

    public Executor build(Environment env,
                          ExeStack funcParams,
                          BuildArguments buildArgs) {
        return new ExeVal(MB.zero(
                        sn_.build(env, funcParams, buildArgs).type()));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNMakeZero(sn_.derivative(wrtvar, dwrtvar, env, funcParams));
    }

    public SyntaxNode simplify() {
        SyntaxNode sn = sn_.simplify();
        if (sn.isValue())
            return new SNVal(MB.zero(MB.build(sn).type()));
        return new SNMakeZero(sn);
    }


    public String toString() {
        return "(SN-makezero " + sn_ + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        sn_.findDependencies(env, deps, ranges);
    }
    

}
