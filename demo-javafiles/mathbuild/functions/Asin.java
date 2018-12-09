//
//  Asin.java
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

public class Asin implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Asin instance_ = new Asin();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Asin inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNAsin(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpAsin();
        throw new BuildException("asin can only be applied to reals.");
    }

}

class SNAsin extends SNUnOp {

    public SNAsin(SyntaxNode sn) {
        super(sn, Asin.inst(), Asin.inst(), "asin");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(doperand,
                            new SNSqrt(new SNSubtract(new SNNumber(1),
                                                      new SNExponent(operand_,
                                                                     new SNNumber(2)))));
    }

}

class OpAsin implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.asin(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}