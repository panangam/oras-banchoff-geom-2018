//
//  Sign.java
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

public class Sign implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Sign instance_ = new Sign();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Sign inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNSign(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpSign();
        throw new BuildException("sign can only be applied to reals.");
    }

}

class SNSign extends SNUnOp {

    public SNSign(SyntaxNode sn) {
        super(sn, Sign.inst(), Sign.inst(), "sign");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNNumber(0);
    }

}

class OpSign implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        double v = ((ValueScalar) val).number();
        if (v < 0)
            return new ValueScalar(-1);
        if (v > 0)
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}