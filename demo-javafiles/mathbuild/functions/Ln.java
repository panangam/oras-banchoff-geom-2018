//
//  Ln.java
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

public class Ln implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Ln instance_ = new Ln();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Ln inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNLn(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpLn();
        throw new BuildException("ln (natural log) can only be applied to reals.");
    }

}

class SNLn extends SNUnOp {

    public SNLn(SyntaxNode sn) {
        super(sn, Ln.inst(), Ln.inst(), "ln");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(doperand, operand_);
    }

}

class OpLn implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.log(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}