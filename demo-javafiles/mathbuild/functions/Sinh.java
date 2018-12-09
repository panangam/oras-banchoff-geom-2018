//
//  Sinh.java
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

public class Sinh implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Sinh instance_ = new Sinh();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Sinh inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNSinh(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpSinh();
        throw new BuildException("sinh can only be applied to reals.");
    }

}

class SNSinh extends SNUnOp {

    public SNSinh(SyntaxNode sn) {
        super(sn, Sinh.inst(), Sinh.inst(), "sinh");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNCosh(operand_), doperand);
    }

}

class OpSinh implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double x = ((ValueScalar) val).number();
        return new ValueScalar( (Math.pow(Math.E, x) - Math.pow(Math.E, -x)) / 2 );
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}