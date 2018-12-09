//
//  BooleanNot.java
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

/**
 * Not maps anything less than or equal to 0 to 1, and anything greater than 0 to 0.
 */
public class BooleanNot implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final BooleanNot instance_ = new BooleanNot();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static BooleanNot inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNBooleanNot(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpBooleanNot();
        throw new BuildException("not can only be applied to a real.");
    }

}

class SNBooleanNot extends SNUnOp {

    public SNBooleanNot(SyntaxNode sn) {
        super(sn, BooleanNot.inst(), BooleanNot.inst(), "not");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNNumber(0);
    }

}

class OpBooleanNot implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        if (((ValueScalar) val).number() > 0)
            return new ValueScalar(0);
        return new ValueScalar(1);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}