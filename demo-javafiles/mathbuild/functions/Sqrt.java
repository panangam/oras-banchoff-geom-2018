//
//  Sqrt.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Sqrt implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Sqrt instance_ = new Sqrt();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Sqrt inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNSqrt(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpSqrt();
        throw new BuildException("sqrt can only be applied to reals.");
    }

}

class SNSqrt extends SNUnOp {

    public SNSqrt(SyntaxNode sn) {
        super(sn, Sqrt.inst(), Sqrt.inst(), "sqrt");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNMultiply(new SNNumber(0.5),
                                             new SNExponent(operand_, new SNNumber(-0.5))),
                              doperand);
    }

}

class OpSqrt implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.sqrt(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}