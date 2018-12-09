//
//  SNExe.java
//  mathbuild
//
//  Created by David Eigen on Sat Jul 13 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

/**
 * SNExe is a placeholder node that simply returns its child executor in its build(.) method.
 */
public class SNExe extends SyntaxNode {

    private Executor exe_;

    public SNExe(Executor exe) {
        exe_ = exe;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    /**
     * @return the Executor stored in this SNExe node.
     */
    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        return exe_;
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        Type t = exe_.type();
        if (t.isType(Type.FUNCTION))
            return new SNExe(new ExeVal(((TypeFunction) t).val().derivative(wrtvar, dwrtvar,
                                                                            funcParams)));
        return new SNVal(MB.zero(t));
    }

    public SyntaxNode simplify() {
        Type t = exe_.type();
        if (t.isType(Type.FUNCTION)) {
            ValueFunction simplifiedVal = ((TypeFunction) t).val().simplify();
            return new SNVal(simplifiedVal);
        }
        return this;
    }
    
    public String toString() {
        return "[SN-Exe]";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        Executor exe = exe_;
        while (exe instanceof ExeCache)
            exe = ((ExeCache) exe).exe();
        if (exe instanceof demo.depend.Dependable)
            deps.add(exe);
        if (exe instanceof Range)
            ranges.add(exe);
    }

}
