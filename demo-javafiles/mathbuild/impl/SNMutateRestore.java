//
//  SNMutateRestore.java
//  Demo
//
//  Created by David Eigen on Tue Apr 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.Type;
import mathbuild.value.Value;

/**
 * SNMutateRestore mutates some variables when the node is executed,
 * and restores them when the execution of this node is finished.
 */
public class SNMutateRestore extends SyntaxNode {

    private String[] ids_;
    private SyntaxNode[] sns_;
    private SyntaxNode body_;
    
    /**
     * @param ids the identifiers to mutate and restore
     * @param sns the expressions to mutate them to
     * @param body the body to evaluate with the variables mutated
     */
    public SNMutateRestore(String[] ids, SyntaxNode[] sns, SyntaxNode body) {
        ids_ = ids;
        sns_ = sns;
        body_ = body;
        if (ids_.length != sns_.length)
            throw new ParseException("length of ids and exprs must match");
    }

    public SyntaxNode[] children() {
        SyntaxNode[] n = new SyntaxNode[sns_.length+1];
        for (int i = 0; i < sns_.length; ++i)
            n[i] = sns_[i];
        n[n.length-1] = body_;
        return n;
    }
    
    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        int n = ids_.length;
        Executor[] mutExes = new Executor[n];
        Mutable[] vars = new Mutable[n];
        for (int i = 0; i < n; ++i) {
            // get the variable
            Object v = env.lookup(ids_[i]);
            if (v instanceof ParameterExe)
                v = ((ParameterExe) v).exe();
            while (v instanceof ExeCache)
                v = ((ExeCache) v).exe();
            if (!(v instanceof Mutable))
                throw new BuildException("Can only set a mutable variable.");
            vars[i] = (Mutable) v;
            // get the expression to set it to
            Executor e = sns_[i].build(env, funcParams, buildArgs);
            Type vt = ((Executor) v).type();
            Type et = e.type();
            if (et.isType(Type.FUNCTION) || vt.isType(Type.FUNCTION))
                throw new BuildException("Can't mutate function types.");
            if (!et.isType(vt))
                throw new BuildException("Incompatible types for mutation: " + et + " and " + vt);
            mutExes[i] = e;
        }
        Executor bodyExe = body_.build(env, funcParams, buildArgs);
        return new ExeMutateRestore(vars, mutExes, bodyExe);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of a mutate/restore/set node.");
    }

    public SyntaxNode simplify() {
        SyntaxNode[] sns = new SyntaxNode[sns_.length];
        for (int i = 0; i < sns.length; ++i)
            sns[i] = sns_[i].simplify();
        return new SNMutateRestore(ids_, sns, body_.simplify());
    }


    public String toString() {
        String str = "(SN-mut-restr ";
        for (int i = 0; i < ids_.length; ++i)
            str += "[" + ids_[i] + "  " + sns_[i] + "]";
        return str + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        // don't want dependencies from the body for things we're mutating
        demo.util.Set toRemoveFromDeps = new demo.util.Set();
        demo.util.Set toRemoveFromRanges = new demo.util.Set();
        for (int i = 0; i < ids_.length; ++i) {
            Object entry = env.lookup(ids_[i]);
            Executor exe;
            if (entry instanceof Executor)
                exe = (Executor) entry;
            else if (entry instanceof ParameterExe)
                exe = ((ParameterExe) entry).exe();
            else continue;
            while (exe instanceof ExeCache)
                exe = ((ExeCache) exe).exe();
            if ((exe instanceof demo.depend.Dependable)
                && !deps.contains(exe))
                toRemoveFromDeps.add(exe);
            if ((exe instanceof Range)
                && !ranges.contains(exe))
                toRemoveFromRanges.add(exe);
        }
        body_.findDependencies(env, deps, ranges);
        deps.removeObjs(toRemoveFromDeps.elements());
        ranges.removeObjs(toRemoveFromRanges.elements());
        // do want dependencies from the exprs of what we're setting the mutating things to
        for (int i = 0; i < sns_.length; ++i)
            sns_[i].findDependencies(env, deps, ranges);
    }

    
}


class ExeMutateRestore implements Executor {

    private Mutable[] vars_;
    private Executor[] newExes_;
    private Executor body_;
    
    public ExeMutateRestore(Mutable[] vars, Executor[] newExes, Executor body) {
        vars_ = vars;
        newExes_ = newExes;
        body_ = body;
    }

    public Type type() {
        return body_.type();
    }

    public Value execute(Object runID) {
        Executor[] prevExes = new Executor[vars_.length];
        for (int i = 0; i < vars_.length; ++i) {
            prevExes[i] = vars_[i].currentMutation();
            vars_[i].mutate(newExes_[i]);
        }
        Value val = body_.execute(runID);
        for (int i = 0; i < vars_.length; ++i)
            vars_[i].mutate(prevExes[i]);
        return val;
    }
    
}


