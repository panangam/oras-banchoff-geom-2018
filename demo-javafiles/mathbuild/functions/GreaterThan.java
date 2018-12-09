//
//  GreaterThan.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class GreaterThan implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final GreaterThan instance_ = new GreaterThan();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static GreaterThan inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNGreaterThan(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpGreaterThanScal();
        throw new BuildException("Greater than (>) can only be used on reals.");
    }


}


class SNGreaterThan extends SNBinOp {

    public SNGreaterThan(SyntaxNode l, SyntaxNode r) {
        super(l, r, new GreaterThan(), new GreaterThan(), "greater-than");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNNumber(0);
    }


}


class OpGreaterThanScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        if (((ValueScalar) a).number() > ((ValueScalar) b).number())
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
