//
//  Abs.java
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

public class Abs implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Abs instance_ = new Abs();
    /**
     * Returns the (singleton) instance of this class.
     */
    public static Abs inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNAbs(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpAbs();
        throw new BuildException("abs can only be applied to reals.");
    }

}

class SNAbs extends SNUnOp {

    public SNAbs(SyntaxNode sn) {
        super(sn, Abs.inst(), Abs.inst(), "abs");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNSign(operand_), doperand);
    }

}

class OpAbs implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.abs(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}