//
//  SNApp.java
//  mathbuild
//
//  Created by David Eigen on Sat Jul 13 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;
import mathbuild.functions.Dot;

/**
 * Syntax Node for function application.
 */
public class SNApp extends SyntaxNode {

    private SyntaxNode func_;
    private SyntaxNode[] args_;

    public SNApp(SyntaxNode func, SyntaxNode arg) {
        this(func, new SyntaxNode[]{arg});
    }
    
    public SNApp(SyntaxNode func, SyntaxNode[] args) {
        func_ = func;
        args_ = args;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] n = new SyntaxNode[args_.length+1];
        for (int i = 0; i < args_.length; ++i)
            n[i] = args_[i];
        n[n.length-1] = func_;
        return n;
    }

    /**
     * Builds the function SN with no automatic function application, and the argument nodes
     * with the current applying mode. All nodes are built in the current environment. Then
     * applies the function executor obtained from building the function node to the executors
     * obtained from building the argument nodes, and turns on automatic function application
     * for this process.
     */
    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] argExes = new Executor[args_.length];
        for (int i = 0; i < args_.length; ++i) {
            argExes[i] = args_[i].build(env, new ExeStack(), buildArgs);
            if (!(argExes[i] instanceof ExeCache))
                argExes[i] = new ExeCache(argExes[i]);
        }
        Executor funcExe = func_.build(env, funcParams.push(argExes), buildArgs.extendApplying(false));
        Type funcType = funcExe.type();
        if (!funcType.isType(Type.FUNCTION))
            //            return funcExe;
            throw new BuildException("Non-function being applied in function application.");
        ValueFunction f = ((TypeFunction) funcType).val();
        return f.apply(argExes, funcParams, buildArgs);
    }


    
    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        // gradf is (grad(f), f_wrtvar)(args_) where grad(f) is a vector (here, f is func_)
        // for the chain rule, this is dotted with (args__wrtvar, 1) where args__wrtvar is a vector
        // this correctly produces the result we need
        SyntaxNode gradf = new SNApp(new SNVector(new SyntaxNode[]{
                                                    new SNDerivative(func_,
                                                                     SNDerivative.GRADIENT),
                                                    new SNDerivative(func_,
                                                                     wrtvar, dwrtvar)}),
                                     args_);
        SyntaxNode[] dargscomps = new SyntaxNode[args_.length];
        for (int i = 0; i < args_.length; ++i)
            dargscomps[i] = args_[i].derivative(wrtvar, dwrtvar, env, new ExeStack());
        // dot product for chain rule
        // note vector --> many scalar parameter conversion is handled by SNMakeVector
        // this is b/c we want a vector of the args, and SNMakeVector creates a new SNVector
        // only if dargscomps is not a single vector. Furthermore, since we took the
        // derivative of this funciton, we know that if it has one argument it should be a
        // scalar (or else the derivative is zero)
        return Dot.inst().makeSyntaxNode(gradf,
                                        new SNVector(new SyntaxNode[]{
                                            new SNMakeVector(dargscomps),
                                            new SNNumber(1)}));
    }
    

    public SyntaxNode simplify() {
        SyntaxNode func = func_.simplify();
        SyntaxNode[] args = new SyntaxNode[args_.length];
        for (int i = 0; i < args.length; ++i)
            args[i] = args_[i].simplify();
        if (func.isValue())
            return new SNVal(MB.exec(func_));
        return new SNApp(func, args);
    }
    
    public String toString() {
        String argStr = "";
        for (int i = 0; i < args_.length; ++i)
            argStr += args_[i].toString();
        return "(SN-app " + func_ + " " + argStr + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        func_.findDependencies(env, deps, ranges);
        for (int i = 0; i < args_.length; ++i)
            args_[i].findDependencies(env, deps, ranges);
    }



}


