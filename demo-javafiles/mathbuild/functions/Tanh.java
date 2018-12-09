//
//  Tanh.java
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

public class Tanh implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Tanh instance_ = new Tanh();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Tanh inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNTanh(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpTanh();
        throw new BuildException("tanh can only be applied to reals.");
    }

}

class SNTanh extends SNUnOp {

    public SNTanh(SyntaxNode sn) {
        super(sn, Tanh.inst(), Tanh.inst(), "tanh");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(doperand, new SNExponent(new SNCosh(operand_), new SNNumber(2)));
    }

}

class OpTanh implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double x = ((ValueScalar) val).number();
        return new ValueScalar( (Math.pow(Math.E, x) - Math.pow(Math.E, -x)) /
                                    (Math.pow(Math.E, x) + Math.pow(Math.E, -x)) );

    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}