//
//  SNAppVal.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;


/**
 * Stores a value. When built, if the stored value is a function and we're in auto-applying mode,
 * the function is applied. Otherwise, an ExeVal containing the value is returned.
 */
public class SNAppVal extends SyntaxNode {

    private Value val_;

    public SNAppVal(Value val) {
        val_ = val;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        if (buildArgs.applying() && val_.type().isType(Type.FUNCTION))
            return ((ValueFunction) val_).autoApply(funcParams, buildArgs);
        return new ExeVal(val_);
    }

    /**
     * If the value stored by this SNAppVal is a function, takes the derivative of the
     * function with respect to the number of the parameter wrtvar. Thus, the derivative
     * of the function is taken with respect to the function's corresponding paramter.
     * If the value stored by this SNAppVal is not a function, derivative(.) returns
     * the Zero value
     */
    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        Type t = val_.type();
        if (t.isType(Type.FUNCTION))
            return new SNAppVal(((ValueFunction) val_).derivative(wrtvar, dwrtvar, funcParams));
        return new SNAppVal(MB.zero(t));
    }

    public SyntaxNode simplify() {
        if (val_.type().isType(Type.FUNCTION)) {
            ValueFunction simplifiedVal = ((ValueFunction) val_).simplify();
            return new SNAppVal(simplifiedVal);
        }
        return this;
    }

    public boolean isValue() {
        return !val_.type().isType(Type.FUNCTION);
    }

    public boolean isValue(int val) {
        if (val_.type().isType(Type.SCALAR))
            return ((ValueScalar) val_).number() == val;
        return false;
    }

    public boolean isValue(Value value) {
        return val_.equals(value);
    }

    public String toString() {
        return "(SN-appval " + val_ + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        if (val_ instanceof demo.depend.Dependable)
            deps.put(val_);
        if (val_ instanceof Range)
            ranges.put(val_);
    }

}
