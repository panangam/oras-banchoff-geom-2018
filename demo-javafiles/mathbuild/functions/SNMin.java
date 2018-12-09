//
//  SNMin.java
//  Demo
//
//  Created by David Eigen on Sat Jun 14 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.value.*;
import mathbuild.type.*;

public class SNMin extends SyntaxNode {

    private SyntaxNode[] nodes_;

    /**
     * Creates a SyntaxNode for finding the min of a list of scalars
     * @param nodes the nodes to take the min of
     */
    public SNMin(SyntaxNode[] nodes) {
        nodes_ = nodes;
    }

    public SyntaxNode[] children() {
        return nodes_;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] exes = new Executor[nodes_.length];
        for (int i = 0; i < nodes_.length; ++i) {
            exes[i] = nodes_[i].build(env, funcParams, buildArgs);
            if (!exes[i].type().isType(Type.SCALAR))
                throw new BuildException("min can only be applied to scalars");
        }
        return new ExeMin(exes);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode[] dnodes = new SyntaxNode[nodes_.length];
        for (int i = 0; i < nodes_.length; ++i)
            dnodes[i] = nodes_[i].derivative(wrtvar, dwrtvar, env, funcParams);
        return new SNMinDeriv(nodes_, dnodes);
    }

    public SyntaxNode simplify() {
        SyntaxNode[] sns = new SyntaxNode[nodes_.length];
        for (int i = 0; i < sns.length; ++i)
            sns[i] = nodes_[i].simplify();
        return new SNMin(sns);
    }

    public String toString() {
        String str = "(SN-min ";
        for (int i = 0; i < nodes_.length; ++i)
            str += nodes_[i].toString() + (i < nodes_.length-1 ? " " : "");
        return str + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < nodes_.length; ++i)
            nodes_[i].findDependencies(env, deps, ranges);
    }
    
    
}


class SNMinDeriv extends SyntaxNode {

    private SyntaxNode[] compnodes_, retnodes_;

    /**
     * Creates a SyntaxNode for the derivative of a min
     * @param nodes the nodes that get compared to see which is the min
     * @param retnodes the nodes whose values to return (the one in same pos as min of compnodes)
     */
    public SNMinDeriv(SyntaxNode[] compnodes, SyntaxNode[] retnodes) {
        compnodes_ = compnodes;
        retnodes_ = retnodes;
    }

    public SyntaxNode[] children() {
        SyntaxNode[] chil = new SyntaxNode[compnodes_.length + retnodes_.length];
        int p = 0;
        for (int i = 0; i < compnodes_.length; ++i)
            chil[p++] = compnodes_[i];
        for (int i = 0; i < retnodes_.length; ++i)
            chil[p++] = retnodes_[i];
        return chil;
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor[] compExes = new Executor[compnodes_.length];
        Executor[] retExes = new Executor[retnodes_.length];
        if (compExes.length != retExes.length)
            throw new BuildException("Internal error: comp and ret exes must have same length");
        for (int i = 0; i < compnodes_.length; ++i) {
            compExes[i] = compnodes_[i].build(env, funcParams, buildArgs);
            if (!compExes[i].type().isType(Type.SCALAR))
                throw new BuildException("min can only be applied to scalars");
        }
        for (int i = 0; i < retnodes_.length; ++i) {
            retExes[i] = retnodes_[i].build(env, funcParams, buildArgs);
            if (!retExes[i].type().isType(Type.SCALAR))
                throw new BuildException("min can only be applied to scalars");
        }
        return new ExeMinDeriv(compExes, retExes);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode[] dnodes = new SyntaxNode[retnodes_.length];
        for (int i = 0; i < retnodes_.length; ++i)
            dnodes[i] = retnodes_[i].derivative(wrtvar, dwrtvar, env, funcParams);
        return new SNMinDeriv(compnodes_, dnodes);
    }

    public SyntaxNode simplify() {
        SyntaxNode[] compSNs = new SyntaxNode[compnodes_.length];
        SyntaxNode[] retSNs = new SyntaxNode[retnodes_.length];
        for (int i = 0; i < compnodes_.length; ++i)
            compSNs[i] = compnodes_[i].simplify();
        for (int i = 0; i < retnodes_.length; ++i)
            retSNs[i] = retnodes_[i].simplify();
        return new SNMinDeriv(compSNs, retSNs);
    }

    public String toString() {
        String str = "(SN-min-deriv compnodes: ";
        for (int i = 0; i < compnodes_.length; ++i)
            str += compnodes_[i].toString() + " ";
        str += "retnodes: ";
        for (int i = 0; i < retnodes_.length; ++i)
            str += retnodes_[i].toString() + (i < retnodes_.length-1 ? " " : "");
        return str + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        for (int i = 0; i < compnodes_.length; ++i)
            compnodes_[i].findDependencies(env, deps, ranges);
        for (int i = 0; i < retnodes_.length; ++i)
            retnodes_[i].findDependencies(env, deps, ranges);
    }

}


class ExeMin implements Executor {
    private Executor[] exes_;
    public ExeMin(Executor[] exes) { exes_ = exes; }
    public Type type() { return MB.TYPE_SCALAR; }
    public Value execute(Object runID) {
        double val = Double.POSITIVE_INFINITY;
        for (int i = 0; i < exes_.length; ++i) {
            double v = ((ValueScalar) exes_[i].execute(runID)).num();
            if (v < val) val = v;
        }
        return new ValueScalar(val);
    }
}


class ExeMinDeriv implements Executor {
    private Executor[] compexes_, retexes_;
    public ExeMinDeriv(Executor[] compexes, Executor[] retexes) {
        compexes_ = compexes;
        retexes_ = retexes;
    }
    public Type type() { return MB.TYPE_SCALAR; }
    public Value execute(Object runID) {
        double val = Double.POSITIVE_INFINITY;
        int ival = 0;
        for (int i = 0; i < compexes_.length; ++i) {
            double v = ((ValueScalar) compexes_[i].execute(runID)).num();
            if (v < val) {
                val = v;
                ival = i;
            }
        }
        return retexes_[ival].execute(runID);
    }
}



