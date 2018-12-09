//
//  Cosine.java
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

public class Cosine implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Cosine instance_ = new Cosine();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Cosine inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNCosine(sn);
    }
    
    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpCos();
        throw new BuildException("cos can only be applied to reals.");
    }

}

class SNCosine extends SNUnOp {

    public SNCosine(SyntaxNode sn) {
        super(sn, Cosine.inst(), Cosine.inst(), "cos");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNMultiply(new SNNegate(new SNSine(operand_)),
                              doperand);
    }

}

class OpCos implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.cos(((ValueScalar) val).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}