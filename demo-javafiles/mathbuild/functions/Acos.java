//
//  Acos.java
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

public class Acos implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Acos instance_ = new Acos();
    /**
     * Returns the (singleton) instance of this class.
     */
    public static Acos inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNAcos(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpAcos();
        throw new BuildException("acos can only be applied to reals.");
    }

}

class SNAcos extends SNUnOp {

    public SNAcos(SyntaxNode sn) {
        super(sn, Acos.inst(), Acos.inst(), "acos");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(new SNNegate(doperand),
                            new SNSqrt(new SNSubtract(new SNNumber(1),
                                                      new SNExponent(operand_,
                                                                     new SNNumber(2)))));
    }

}

class OpAcos implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.acos(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}