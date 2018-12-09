//
//  SNLet.java
//  mathbuild
//
//  Created by David Eigen on Sun Jul 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

public class SNLet extends SyntaxNode {

    private String[] names_;
    private SyntaxNode[] defs_;
    private SyntaxNode body_;
    
    public SNLet(String[] names, SyntaxNode[] defs, SyntaxNode body) {
        names_ = names;
        defs_ = defs;
        body_ = body;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] nodes = new SyntaxNode[defs_.length + 1];
        int p = 0;
        for (int i = 0; i < defs_.length; ++i)
            nodes[p++] = defs_[i];
        nodes[p++] = body_;
        return nodes;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] defs = new Executor[defs_.length];
        for (int i = 0; i < defs.length; ++i) {
            defs[i] = defs_[i].build(env, new ExeStack(), buildArgs.extendApplying(false));
            if (!(defs[i] instanceof ExeCache))
                defs[i] = new ExeCache(defs[i]);
            env = env.extend(names_[i], defs[i]);
        }
        return body_.build(env, funcParams, buildArgs);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take derivative of let statement.");
    }

    public SyntaxNode simplify() {
        SyntaxNode body = body_.simplify();
        SyntaxNode[] defs = new SyntaxNode[defs_.length];
        for (int i = 0; i < defs.length; ++i)
            defs[i] = defs_[i].simplify();
        // note: we don't check body for if it's a value, since
        // if it's a function then we need let for vars
        return new SNLet(names_, defs, body);
    }
    
    public String toString() {
        String str = "(SN-let ";
        for (int i = 0; i < names_.length; ++i) {
            str += "[" + names_[i] + " = " + defs_[i] + "]";
        }
        str += " " + body_;
        return str;
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < defs_.length; ++i) {
            defs_[i].findDependencies(env, deps, ranges);
            env = env.extend(names_[i], new ExeDummy());
        }
        body_.findDependencies(env, deps, ranges);
    }
    

}
