//
//  SNVector.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class SNVector extends SyntaxNode {

    private SyntaxNode[] components_;

    /**
     * creates a syntax node for a vector constructor
     * @param components the syntax nodes for the components of the vector
     */
    public SNVector(SyntaxNode[] components) {
        components_ = components;
    }

    /**
     * @return the component nodes for this SNVector
     */
    public SyntaxNode[] components() {
        return components_;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] c = new SyntaxNode[components_.length];
        for (int i = 0; i < c.length; ++i)
            c[i] = components_[i];
        return c;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] exes = new Executor[components_.length];
        for (int i = 0; i < exes.length; ++i)
            exes[i] = components_[i].build(env, funcParams, buildArgs);
        return buildVector(exes, env);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode[] derivs = new SyntaxNode[components_.length];
        for (int i = 0; i < derivs.length; ++i)
            derivs[i] = components_[i].derivative(wrtvar, dwrtvar, env, funcParams);
        return new SNVector(derivs);
    }

    public SyntaxNode simplify() {
        SyntaxNode[] comps = new SyntaxNode[components_.length];
        boolean isValue = true;
        for (int i = 0; i < comps.length; ++i) {
            comps[i] = components_[i].simplify();
            isValue = isValue && comps[i].isValue();
        }
        if (isValue)
            return new SNVal(MB.exec(new SNVector(comps)));
        return new SNVector(comps);
    }


    /**
     * Builds an executor object for making a vector out of the given component executors
     * @param exes the component executors
     * @param env the current environment
     * @return an executor object that makes a vector (or function) from the given components
     */
    public static Executor buildVector(Executor[] exes, Environment env) {
        boolean isFunc = false; // to check if this should become a function
        ValueFunction[] subfuncs = new ValueFunction[exes.length];
        for (int i = 0; i < exes.length; ++i) {
            Type exeType = exes[i].type();
            if (exeType.isType(Type.FUNCTION)) {
                subfuncs[i] = ((TypeFunction) exeType).val();
                isFunc = true;
            }
        }
        if (!isFunc) // if function type is null, this is not a function type
            return new ExeVector(exes);
        // The components contain a function, so the resulting vector should be a function
        SyntaxNode[] componentNodes = new SyntaxNode[exes.length];
        for (int i = 0; i < componentNodes.length; ++i)
            componentNodes[i] = subfuncs[i] == null ? (SyntaxNode) new SNExe(exes[i])
                          : subfuncs[i].isClosure() ? (SyntaxNode) new SNAppVal(subfuncs[i])
                                                    : (SyntaxNode) subfuncs[i].body();
        return new ExeVal(new ValueFunctionImpl(subfuncs, new SNVector(componentNodes)));
    }

    public String toString() {
        String str = "(SN-vector ";
        for (int i = 0; i < components_.length; ++i) {
            str += components_[i];
            if (i < components_.length - 1)
                str += " ";
        }
        str += ")";
        return str;
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < components_.length; ++i)
            components_[i].findDependencies(env, deps, ranges);
    }

}

class ExeVector implements Executor {

    Executor[] exes_;

    public ExeVector(Executor[] exes) {
        exes_ = exes;
    }

    public Value execute(Object runID) {
        Value[] vals = new Value[exes_.length];
        for (int i = 0; i < vals.length; ++i)
            vals[i] = exes_[i].execute(runID);
        return new ValueVector(vals);
    }

    public Type type() {
        Type[] types = new Type[exes_.length];
        for (int i = 0; i < types.length; ++i)
            types[i] = exes_[i].type();
        return new TypeVector(types);
    }
}
