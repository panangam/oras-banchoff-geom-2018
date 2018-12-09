//
//  SNIdentifier.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNIdentifier extends SyntaxNode {

    private String name_;

    public SNIdentifier(String name) {
        name_ = name;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Object v = env.lookup(name_);
        if (v instanceof ParameterExe)
            return ((ParameterExe) v).exe();
        return (Executor) v;
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        Object x = env.lookup(name_);
        if (x instanceof ParameterDeriv)
            return ((ParameterDeriv) x).node();
        if (x instanceof ParameterExe)
            return new SNVal(MB.zero(((ParameterExe) x).exe().type()));
        Type t = ((Executor) x).type();
        // if the identifier is not a parameter, it's constant wrt the parameter, so its derv is zero
        return new SNVal(MB.zero(t));
    }

    public SyntaxNode simplify() {
        return this;
    }

    public String toString() {
        return "(SN-id " + name_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        Object entry = env.lookup(name_);
        Executor exe;
        if (entry instanceof Executor)
            exe = (Executor) entry;
        else if (entry instanceof ParameterExe)
            exe = ((ParameterExe) entry).exe();
        else
            return;
        while (exe instanceof ExeCache)
            exe = ((ExeCache) exe).exe();
        if (exe instanceof demo.depend.Dependable)
            deps.add(exe);
        if (exe instanceof Range)
            ranges.add(exe);
    }

}
