//
//  SNMatrix.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class SNMatrix extends SyntaxNode {

    private SyntaxNode[][] components_;

    /**
     * creates a syntax node for a matrix constructor
     * @param components the syntax nodes for the components of the vector
     */
    public SNMatrix(SyntaxNode[][] components) {
        components_ = components;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] c = new SyntaxNode[components_.length*components_[0].length];
        int p = 0;
        for (int i = 0; i < components_.length; ++i)
            for (int j = 0; j < components_[i].length; ++i)
                c[p++] = components_[i][j];
        return c;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[][] exes = new Executor[components_.length][components_[0].length];
        for (int i = 0; i < exes.length; ++i)
            for (int j = 0; j < exes[i].length; ++j)
                exes[i][j] = components_[i][j].build(env, funcParams, buildArgs);
        return buildMatrix(exes, env);
    }

    public static Executor buildMatrix(Executor[][] exes, Environment env) {
        boolean isFunc = false; // to check if this should become a function
        ValueFunction[] subfuncs = new ValueFunction[exes.length*exes[0].length];
        for (int i = 0; i < exes.length; ++i) {
            for (int j = 0; j < exes[i].length; ++j) {
                Type exeType = exes[i][j].type();
                if (exeType.isType(Type.FUNCTION)) {
                    subfuncs[j+exes[i].length*i] = ((TypeFunction) exeType).val();
                    isFunc = true;
                }
            }
        }
        if (!isFunc)
            return new ExeMatrix(exes);
        // The components contain a function, so the resulting vector should be a function
        SyntaxNode[][] componentNodes = new SyntaxNode[exes.length][exes[0].length];
        int p = -1;
        for (int i = 0; i < componentNodes.length; ++i) {
            for (int j = 0; j < componentNodes[i].length; ++j) {
                ++p;
                componentNodes[i][j] = subfuncs[p] == null ? (SyntaxNode) new SNExe(exes[i][j])
                                 : subfuncs[p].isClosure() ? (SyntaxNode) new SNAppVal(subfuncs[p])
                                                           : (SyntaxNode) subfuncs[p].body();
            }
        }
        return new ExeVal(new ValueFunctionImpl(subfuncs, new SNMatrix(componentNodes)));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode[][] derivs = new SyntaxNode[components_.length][components_[0].length];
        for (int i = 0; i < derivs.length; ++i)
            for (int j = 0; j < derivs[i].length; ++j)
                derivs[i][j] = components_[i][j].derivative(wrtvar, dwrtvar, env, funcParams);
        return new SNMatrix(derivs);
    }

    public SyntaxNode simplify() {
        SyntaxNode[][] comps = new SyntaxNode[components_.length][components_[0].length];
        boolean isValue = true;
        for (int i = 0; i < comps.length; ++i) {
            for (int j = 0; j < comps[i].length; ++j) {
                comps[i][j] = components_[i][j].simplify();
                isValue = isValue && comps[i][j].isValue();
            }
        }
        if (isValue)
            return new SNVal(MB.exec(new SNMatrix(comps)));
        return new SNMatrix(comps);
    }

    public String toString() {
        String str = "(SN-matrix ";
        for (int i = 0; i < components_.length; ++i) {
            str += "[";
            for (int j = 0; j < components_[i].length; ++j) {
                str += components_[i][j];
                if (j < components_[i].length - 1)
                    str += " ";
            }
            str += "]";
            if (i < components_.length - 1)
                str += " ";
        }
        str += ")";
        return str;
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < components_.length; ++i)
            for (int j = 0; j < components_[i].length; ++j)
                components_[i][j].findDependencies(env, deps, ranges);
    }

}

class ExeMatrix implements Executor {

    Executor[][] exes_;

    public ExeMatrix(Executor[][] exes) {
        exes_ = exes;
    }

    public Value execute(Object runID) {
        Value[][] vals = new Value[exes_.length][exes_[0].length];
        for (int i = 0; i < vals.length; ++i)
            for (int j = 0; j < vals[i].length; ++j)
                vals[i][j] = exes_[i][j].execute(runID);
        return new ValueMatrix(vals);
    }

    public Type type() {
        Type[][] types = new Type[exes_.length][exes_[0].length];
        for (int i = 0; i < types.length; ++i)
            for (int j = 0; j < types[i].length; ++j)
            types[i][j] = exes_[i][j].type();
        return new TypeMatrix(types);
    }

}
