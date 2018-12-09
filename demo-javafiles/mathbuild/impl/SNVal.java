//
//  SNVal.java
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
 * Right now, only SNApp uses SNVal to store function values.
 */
public class SNVal extends SyntaxNode {

    private Value val_;

    public SNVal(Value val) {
        val_ = val;
    }

    /**
     * @return the value stored in this SNVal
     */
    public Value val() {
        return val_;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    public Executor build(Environment env, ExeStack funcParamss, BuildArguments buildArgs) {
        return new ExeVal(val_);
    }

    /**
     * If the value stored by this SNVal is a function, takes the derivative of the
     * function with respect to the number of the parameter wrtvar. Thus, the derivative
     * of the function is taken with respect to the function's corresponding paramter.
     * If the value stored by this SNVal is not a function, derivative(.) returns
     * the Zero value
     * @param wrtvar the parameter of the derivative
     * @param env the current derivative environment
     */
    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        Type t = val_.type();
        if (t.isType(Type.FUNCTION))
            return new SNVal(((ValueFunction) val_).derivative(wrtvar, dwrtvar, funcParams));
        return new SNVal(MB.zero(t));
    }

    public SyntaxNode simplify() {
        if (val_.type().isType(Type.FUNCTION)) {
            ValueFunction simplifiedVal = ((ValueFunction) val_).simplify();
            return new SNVal(simplifiedVal);
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

    public boolean isZero() {
        return val_.equals(MB.zero(val_.type()));
    }

    public String toString() {
        return "(SN-val " + val_ + ")";
    }



    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        if (val_ instanceof demo.depend.Dependable)
            deps.add(val_);
        if (val_ instanceof Range)
            deps.add(val_);
    }
    
}
