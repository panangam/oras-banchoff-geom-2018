//
//  SNListConstruct.java
//  Demo
//
//  Created by David Eigen on Mon Apr 21 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//


package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNListConstruct extends SyntaxNode {

    private SyntaxNode[] components_;

    /**
     * Creates a syntax node for constructing a list value from a list of values.
     * @param components the component SNs of the list
     */
    public SNListConstruct(SyntaxNode[] components) {
        components_ = components;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] n = new SyntaxNode[components_.length];
        for (int i = 0; i < n.length; ++i)
            n[i] = components_[i];
        return n;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] exes = new Executor[components_.length];
        for (int i = 0; i < exes.length; ++i)
            exes[i] = components_[i].build(env, funcParams, buildArgs);
        if (exes.length == 0) {
            throw new BuildException("Cannot construct an empty list.");
        }
        // nonzero length: make sure all the types are the same
        Type t = exes[0].type();
        for (int i = 1; i < exes.length; ++i)
            if (!exes[i].type().compatibleType(t))
                throw new BuildException("All entries in a list must have the same type.");
        // OK: make the executor
        return new ExeListConstruct(exes);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of a list.");
    }

    public SyntaxNode simplify() {
        SyntaxNode[] newcomps = new SyntaxNode[components_.length];
        for (int i = 0; i < newcomps.length; ++i)
            newcomps[i] = components_[i].simplify();
        return new SNListConstruct(newcomps);
    }

    public String toString() {
        String str = "(SN-list ";
        for (int i = 0; i < components_.length; ++i)
            str += components_[i] + (i < components_.length - 1 ? " " : "");
        return str + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < components_.length; ++i)
            components_[i].findDependencies(env, deps, ranges);
    }
    
    
}


class ExeListConstruct implements Executor {
    private Executor[] exes_;
    private Type comptype_;
    public ExeListConstruct(Executor[] exes) {
        exes_ = exes;
        comptype_ = exes_[0].type();
    }
    public Type type() {
        return new TypeList(comptype_);
    }
    public Value execute(Object runID) {
        Value[] vals = new Value[exes_.length];
        for (int i = 0; i < vals.length; ++i)
            vals[i] = exes_[i].execute(runID);
        return new ValueList(vals, comptype_);
    }
}


