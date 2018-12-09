//
//  Tangent.java
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

public class Tangent implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Tangent instance_ = new Tangent();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Tangent inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNTangent(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpTangent();
        throw new BuildException("tan can only be applied to reals.");
    }

}

class SNTangent extends SNUnOp {

    public SNTangent(SyntaxNode sn) {
        super(sn, Tangent.inst(), Tangent.inst(), "tan");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(doperand,
                            new SNExponent(new SNCosine(operand_), new SNNumber(2)));
    }

}

class OpTangent implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.tan(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}