//
//  Cosh.java
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

public class Cosh implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Cosh instance_ = new Cosh();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Cosh inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNCosh(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpCosh();
        throw new BuildException("cosh can only be applied to reals.");
    }

}

class SNCosh extends SNUnOp {

    public SNCosh(SyntaxNode sn) {
        super(sn, Cosh.inst(), Cosh.inst(), "cosh");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNSinh(operand_), doperand);
    }

}

class OpCosh implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double x = ((ValueScalar) val).number();
        return new ValueScalar( (Math.pow(Math.E, x) + Math.pow(Math.E, -x)) / 2 );
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}