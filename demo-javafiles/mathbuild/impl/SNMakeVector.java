//
//  SNMakeVector.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

/**
 * SNMakeVector returns a vector of its child nodes, if it has mor than one
 * child node. That is, if this node has more than one child component,
 * it builds a vector out of its child components. If this node has only one
 * child component, it just returns the exe from its child and does not make
 * another vector from it.
 */
public class SNMakeVector extends SyntaxNode {

    private SyntaxNode[] components_;

    /**
     * Creates a MakeVector node. 
     * @param components the syntax nodes for the components of the vector
     */
    public SNMakeVector(SyntaxNode[] components) {
        components_ = components;
    }

    /**
     * @return the component nodes for this SNMakeVector
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
        if (exes.length == 1)
            return exes[0];
        return SNVector.buildVector(exes, env);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode[] derivs = new SyntaxNode[components_.length];
        for (int i = 0; i < derivs.length; ++i)
            derivs[i] = components_[i].derivative(wrtvar, dwrtvar, env, funcParams);
        return new SNMakeVector(derivs);
    }

    public SyntaxNode simplify() {
        SyntaxNode[] comps = new SyntaxNode[components_.length];
        boolean isValue = true;
        for (int i = 0; i < comps.length; ++i) {
            comps[i] = components_[i].simplify();
            isValue = isValue && comps[i].isValue();
        }
        if (!isValue)
            return new SNMakeVector(comps);
        // is a value
        return new SNVal(MB.exec(new SNMakeVector(comps)));
    }

    public String toString() {
        String str = "(SN-makevector ";
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
