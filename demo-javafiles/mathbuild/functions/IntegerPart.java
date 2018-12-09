//
//  IntegerPart.java
//  Demo
//
//  Created by David Eigen on Thu May 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class IntegerPart implements SyntaxNodeConstructorUn, OperatorUnFactory {

    private static final IntegerPart instance_ = new IntegerPart();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static IntegerPart inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNIntegerPart(sn);
    }

    public OpUn makeOperator(Type t) {
        if (!t.isType(Type.SCALAR))
            throw new BuildException("ipart can only be applied to reals");
        return new OpIntegerPart();
    }

}

class SNIntegerPart extends SNUnOp {

    public SNIntegerPart(SyntaxNode sn) {
        super(sn, IntegerPart.inst(), IntegerPart.inst(), "ipart");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNVal(MB.ZERO_SCALAR);
    }

}

class OpIntegerPart implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double v = ((ValueScalar) val).num();
        if (v >= 0)
            return new ValueScalar(Math.floor(v));
        else
            return new ValueScalar(-Math.floor(-v));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}

