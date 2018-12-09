//
//  SNAppSN.java
//  mathbuild
//
//  Created by David Eigen on Wed Aug 07 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNAppSN extends SyntaxNode {

    private SyntaxNode sn_;

    public SNAppSN(SyntaxNode sn) {
        sn_ = sn;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{sn_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor exe = sn_.build(env, funcParams, buildArgs);
        if (buildArgs.applying() && exe.type().isType(Type.FUNCTION))
            return ((TypeFunction) exe.type()).val().autoApply(funcParams, buildArgs);
        return exe;
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNAppSN(sn_.derivative(wrtvar, dwrtvar, env, funcParams));
    }

    public SyntaxNode simplify() {
        SyntaxNode simplifiedSN = sn_.simplify();
        if (simplifiedSN.isValue()) {
            return new SNVal(MB.exec(simplifiedSN));
        }
        return new SNAppSN(simplifiedSN);
    }

    public String toString() {
        return "(SN-appsn " + sn_ + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        sn_.findDependencies(env, deps, ranges);
    }

    
}
