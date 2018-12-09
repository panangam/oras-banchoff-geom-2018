//
//  SNIf.java
//  Demo
//
//  Created by David Eigen on Wed Aug 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

/**
 * If statement. If takes three arguments: a condition, and two result values.
 * If the first argument is greater than zero, if returns the first result.
 * If the first argument is less than or equal to zero, if returns the second result.
 */
public class SNIf extends SyntaxNode {

    private SyntaxNode condSN_, trueSN_, falseSN_;

    public SNIf(SyntaxNode condSN, SyntaxNode trueSN, SyntaxNode falseSN) {
        condSN_ = condSN;
        trueSN_ = trueSN;
        falseSN_ = falseSN;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{condSN_, trueSN_, falseSN_};
    }
    
    public Executor build(Environment env,
                          ExeStack funcParams,
                          BuildArguments buildArgs) {
        Executor condExe = condSN_.build(env, funcParams, buildArgs);
        Executor trueExe = trueSN_.build(env, funcParams, buildArgs);
        Executor falseExe = falseSN_.build(env, funcParams, buildArgs);
        Type condType = condExe.type(), trueType = trueExe.type(), falseType = falseExe.type();
        if (condType.isType(Type.FUNCTION) ||
            trueType.isType(Type.FUNCTION) ||
            falseType.isType(Type.FUNCTION)) {
            // function combining with if
            ValueFunction condFunc = condType.isType(Type.FUNCTION) ? ((TypeFunction) condType).val()
                                                                    : null;
            ValueFunction trueFunc = trueType.isType(Type.FUNCTION) ? ((TypeFunction) trueType).val()
                                                                    : null;
            ValueFunction falseFunc = falseType.isType(Type.FUNCTION) ? ((TypeFunction) falseType).val()
                                                                      : null;
            return new ExeVal(new ValueFunctionImpl(
                            new ValueFunction[]{condFunc, trueFunc, falseFunc},
                            new SNIf( condFunc == null ? (SyntaxNode) new SNExe(condExe)
                                : condFunc.isClosure() ? (SyntaxNode) new SNAppVal(condFunc)
                                                       : (SyntaxNode) condFunc.body(),
                                      trueFunc == null ? (SyntaxNode) new SNExe(trueExe)
                                : trueFunc.isClosure() ? (SyntaxNode) new SNAppVal(trueFunc)
                                                       : (SyntaxNode) trueFunc.body(),
                                     falseFunc == null ? (SyntaxNode) new SNExe(falseExe)
                               : falseFunc.isClosure() ? (SyntaxNode) new SNAppVal(falseFunc)
                                                       : (SyntaxNode) falseFunc.body() )));
        }
        else {
            // not a function
            if (!condType.isType(Type.SCALAR))
                throw new BuildException("Condition of if statement must be a real.");
            if (!trueType.isType(falseType))
                throw new BuildException("Result expressions of if statement must have the same type.");
            return new ExeIf(condExe, trueExe, falseExe);
        }
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNIf(condSN_,
                        trueSN_.derivative(wrtvar, dwrtvar, env, funcParams),
                        falseSN_.derivative(wrtvar, dwrtvar, env, funcParams));
    }

    public SyntaxNode simplify() {
        SyntaxNode
        condSN = condSN_.simplify(),
        trueSN = trueSN_.simplify(),
        falseSN = falseSN_.simplify();
        if (condSN.isValue()) {
            Value condVal = MB.exec(condSN);
            if (!(condVal instanceof ValueScalar))
                throw new BuildException("Condition of if statement must be a real.");
            SyntaxNode sn;
            if (((ValueScalar) condVal).number() > 0)
                sn = trueSN;
            else
                sn = falseSN;
            if (sn.isValue())
                return new SNVal(MB.exec(sn));
            return sn;
        }
        return new SNIf(condSN, trueSN, falseSN);
    }

    public String toString() {
        return "(SN-if " + condSN_ + " " + trueSN_ + " " + falseSN_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        condSN_.findDependencies(env, deps, ranges);
        trueSN_.findDependencies(env, deps, ranges);
        falseSN_.findDependencies(env, deps, ranges);
    }


}


class ExeIf implements Executor {
    private Executor cond_, true_, false_;
    public ExeIf(Executor condExe, Executor trueExe, Executor falseExe) {
        cond_ = condExe; true_ = trueExe; false_ = falseExe;
    }
    public Type type() {
        return true_.type();
    }
    public Value execute(Object runID) {
        if (((ValueScalar) cond_.execute(runID)).number() > 0)
            return true_.execute(runID);
        return false_.execute(runID);
    }
}



