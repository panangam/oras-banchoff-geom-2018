//
//  FractionPart.java
//  Demo
//
//  Created by David Eigen on Thu May 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;


import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class FractionPart implements SyntaxNodeConstructorUn, OperatorUnFactory {

    private static final FractionPart instance_ = new FractionPart();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static FractionPart inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNFractionPart(sn);
    }

    public OpUn makeOperator(Type t) {
        if (!t.isType(Type.SCALAR))
            throw new BuildException("fpart can only be applied to reals");
        return new OpFractionPart();
    }

}

class SNFractionPart extends SNUnOp {

    public SNFractionPart(SyntaxNode sn) {
        super(sn, FractionPart.inst(), FractionPart.inst(), "fpart");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return doperand;
    }

}

class OpFractionPart implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double v = ((ValueScalar) val).num();
        if (v >= 0)
            return new ValueScalar(v - Math.floor(v));
        else
            return new ValueScalar(v + Math.floor(-v));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}

