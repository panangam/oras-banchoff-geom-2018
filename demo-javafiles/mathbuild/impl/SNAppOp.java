//
//  SNAppOp.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.Type;
import mathbuild.type.TypeFunction;
import mathbuild.value.Value;
import mathbuild.value.ValueFunction;
import mathbuild.value.ValueFunctionImpl;

public class SNAppOp extends SyntaxNode {

    private Operator op_;
    private SyntaxNode[] sns_;
    private Type[] types_;

    /**
     * When a SNAppOp is built, an executor is created that
     * simply applies the operator op to the executors built
     * from the given argument SNs.
     * @param op the operator
     * @param argSNs the args to apply to the operator
     */
    public SNAppOp(Operator op, SyntaxNode[] argSNs) {
        op_ = op;
        sns_ = argSNs;
        types_ = null;
    }

    /**
     * When a SNAppOp is built, an executor is created that
     * simply applies the operator op to the executors built
     * from the given argument SNs.
     * @param op the operator
     * @param argSNs the args to apply to the operator
     * @param argTypes the expected types of the arguments:
              the types are checked when building
     */
    public SNAppOp(Operator op, SyntaxNode[] argSNs, Type[] argTypes) {
        op_ = op;
        sns_ = argSNs;
        types_ = argTypes;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] n = new SyntaxNode[sns_.length];
        for (int i = 0; i < n.length; ++i)
            n[i] = sns_[i];
        return n;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] exes = new Executor[sns_.length];
        for (int i = 0; i < exes.length; ++i)
            exes[i] = sns_[i].build(env, funcParams, buildArgs);
        if (types_ != null) {
            boolean isFunc = false; // to check if this should become a function
            ValueFunction[] subfuncs = new ValueFunction[exes.length];
            for (int i = 0; i < exes.length; ++i) {
                Type exeType = exes[i].type();
                if ( !types_[i].compatibleType(exeType) ) {
                    if ( exeType.isType(Type.FUNCTION) ) {
                        subfuncs[i] = ((TypeFunction) exeType).val();
                        isFunc = true;
                    }
                    else {
                        // incompatible types
                        throw new BuildException("Incompatible types.");
                    }
                }
            }
            if (!isFunc) // if function type is null, this is not a function type
                return new ExeNAryOp(exes, op_);
            // The components contain a function, so the resulting vector should be a function
            SyntaxNode[] argNodes = new SyntaxNode[exes.length];
            for (int i = 0; i < argNodes.length; ++i)
                argNodes[i] = subfuncs[i] == null ? (SyntaxNode) new SNExe(exes[i])
                        : subfuncs[i].isClosure() ? (SyntaxNode) new SNAppVal(subfuncs[i])
                                                  : (SyntaxNode) subfuncs[i].body();
            return new ExeVal(new ValueFunctionImpl(subfuncs, new SNAppOp(op_, argNodes, types_)));
        }
        return new ExeNAryOp(exes, op_);
    }




    /**
     * It's an error to take the derivative of an SNAppOp, at least for now.
     */
    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of a non-expression function.");
    }

    public SyntaxNode simplify() {
        SyntaxNode[] sns = new SyntaxNode[sns_.length];
        for (int i = 0; i < sns_.length; ++i)
            sns[i] = sns_[i].simplify();
        return new SNAppOp(op_, sns);
    }

    public boolean isValue() {
        return false;
    }

    public boolean isValue(int val) {
        return false;
    }

    public boolean isValue(Value value) {
        return false;
    }

    public String toString() {
        String snsStr = "";
        for (int i = 0; i < sns_.length; ++i)
            snsStr += sns_.toString() + (i < sns_.length - 1 ? " " : "");
        return "(SN-appop " + op_ + " " + snsStr + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < sns_.length; ++i)
            sns_[i].findDependencies(env, deps, ranges);
        if (op_ instanceof demo.depend.Dependable)
            deps.put(op_);
    }
    
}
