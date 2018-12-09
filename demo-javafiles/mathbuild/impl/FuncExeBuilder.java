//
//  FuncExeBuilder.java
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
 * The FuncExeBuilder class is used to make an Executor object for a new function that applies
 * the operation for of a given Syntax Node class to child Executor objects. That is, if
 * f and g are functions, FuncExeBuilder is used to make an Executor object for functions
 * made out of f and g, such as f+g.
 */
public class FuncExeBuilder {

    /**
     * Makes an executor that combines the two child functions. At least one of left and right
     * must have a Type.FUNCTION return type.
     * @param left the left child
     * @param right the right child
     * @param constructor an SNBinFactory that makes the syntax node for the operation being
     * applied to left and right.
     */
    public static Executor buildBinFuncExe(Executor left, Executor right,
                                           SyntaxNodeConstructorBin constructor) {
        Type lt = left.type();
        Type rt = right.type();
        if (lt.isType(Type.FUNCTION) && rt.isType(Type.FUNCTION)) {
            if (((TypeFunction) lt).numArgs() != ((TypeFunction) rt).numArgs())
                throw new BuildException("Functions being combined must have the same number of arguments.");
        }
        ValueFunction lf = lt.isType(Type.FUNCTION) ? ((TypeFunction) lt).val() : null;
        ValueFunction rf = rt.isType(Type.FUNCTION) ? ((TypeFunction) rt).val() : null;
        SyntaxNode lsn = lf == null ? (SyntaxNode) new SNExe(left)
                   : lf.isClosure() ? (SyntaxNode) new SNAppVal(lf)
                                    : (SyntaxNode) lf.body();
        SyntaxNode rsn = rf == null ? (SyntaxNode) new SNExe(right)
                   : rf.isClosure() ? (SyntaxNode) new SNAppVal(rf)
                                    : (SyntaxNode) rf.body();
        return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{lf,rf},
                                                constructor.makeSyntaxNode(lsn, rsn)));
    }


    /**
     * Makes an executor for the function that performs the given operation on the exe.
     * @param exe the child Executor
     * @param constructor an SNUnFactory that makes the syntax node for the operation being
     * applied to exe.
     */
    public static Executor buildUnFuncExe(Executor exe, SyntaxNodeConstructorUn constructor) {
        ValueFunction func = ((TypeFunction) exe.type()).val();
        return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{func},
                                                constructor.makeSyntaxNode(func.body()),
                                                func.environment()));
    }

    
    
}
