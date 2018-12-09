//
//  Atan.java
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

public class Atan implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Atan instance_ = new Atan();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Atan inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNAtan(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpAtan();
        throw new BuildException("atan can only be applied to reals.");
    }

}

class SNAtan extends SNUnOp {

    public SNAtan(SyntaxNode sn) {
        super(sn, Atan.inst(), Atan.inst(), "atan");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(doperand,
                            new SNAdd(new SNNumber(1),
                                      new SNExponent(operand_, new SNNumber(2))));
    }

}

class OpAtan implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.atan(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}