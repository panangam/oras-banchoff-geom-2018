//
//  Sine.java
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

public class Sine implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Sine instance_ = new Sine();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Sine inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNSine(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpSin();
        throw new BuildException("sin can only be applied to reals.");
    }

}

class SNSine extends SNUnOp {

    public SNSine(SyntaxNode sn) {
        super(sn, new Sine(), new Sine(), "sin");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNCosine(operand_), doperand);
    }

}

class OpSin implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.sin(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}