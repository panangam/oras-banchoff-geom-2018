//
//  SNListAccess.java
//  Demo
//
//  Created by David Eigen on Mon Apr 21 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNListAccess extends SyntaxNode {

    private SyntaxNode list_;
    private SyntaxNode component_;
    private boolean wraparound_;
    
    /**
     * Creates a syntax node for accessing the nth item in a list.
     * @param list the SN for the list
     * @param comp the SN for the component index
     * @param wraparound whether this access should wrap-around or truncate out of bounds indices
     */
    public SNListAccess(SyntaxNode list, SyntaxNode comp, boolean wraparound) {
        list_ = list;
        component_ = comp;
        wraparound_ = wraparound;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{list_, component_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor listExe = list_.build(env, funcParams, buildArgs);
        Executor compExe = component_.build(env, funcParams, buildArgs);
        if (!listExe.type().isType(Type.LIST))
            throw new BuildException("Can only do a list access on lists.");
        if (!compExe.type().isType(Type.SCALAR))
            throw new BuildException("Index of a list access must be a scalar.");
        if (wraparound_)
            return new ExeListAccessWraparound(listExe, compExe);
        else
            return new ExeListAccessTruncate(listExe, compExe);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of a list access.");
    }

    public SyntaxNode simplify() {
        return new SNListAccess(list_.simplify(), component_.simplify(), wraparound_);
    }

    public String toString() {
        return "(SN-list-access " + list_ + " " + component_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        list_.findDependencies(env, deps, ranges);
        component_.findDependencies(env, deps, ranges);
    }


}


class ExeListAccessWraparound implements Executor {
    private Executor list_, component_;
    private Type comptype_;
    public ExeListAccessWraparound(Executor list, Executor comp) {
        list_ = list;
        component_ = comp;
        comptype_ = ((TypeList) list.type()).componentType();
    }
    public Type type() {
        return comptype_;
    }
    public Value execute(Object runID) {
        ValueList l = (ValueList) list_.execute(runID);
        int i = ((int) ((ValueScalar) component_.execute(runID)).number()) - 1;
        while (i < 0) i += l.length();
        if (i >= l.length()) i %= l.length();
        return l.valueAt(i);
    }
}



class ExeListAccessTruncate implements Executor {
    private Executor list_, component_;
    private Type comptype_;
    public ExeListAccessTruncate(Executor list, Executor comp) {
        list_ = list;
        component_ = comp;
        comptype_ = ((TypeList) list.type()).componentType();
    }
    public Type type() {
        return comptype_;
    }
    public Value execute(Object runID) {
        ValueList l = (ValueList) list_.execute(runID);
        int i = ((int) ((ValueScalar) component_.execute(runID)).number()) - 1;
        if (i < 0) i = 0;
        if (i >= l.length()) i = l.length() - 1;
        return l.valueAt(i);
    }
}


