//
//  SNNumber.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNNumber extends SyntaxNode {

    private double num_;

    public SNNumber(double number) {
        num_ = number;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        return new ExeVal(new ValueScalar(num_));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNNumber(0);
    }

    public SyntaxNode simplify() {
        return this;
    }

    public boolean isValue() {
        return true;
    }
    
    public boolean isZero() {
        return num_ == 0;
    }

    public boolean isValue(int val) {
        return num_ == val;
    }

    public boolean isValue(Value val) {
        if (val.type().isType(Type.SCALAR))
            return ((ValueScalar) val).number() == num_;
        return false;
    }
    
    public String toString() {
        return "(SN-num " + num_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
    }

}
