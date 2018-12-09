//
//  LessThan.java
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

public class LessThan implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final LessThan instance_ = new LessThan();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static LessThan inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNLessThan(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpLessThanScal();
        throw new BuildException("Less than (<) can only be used on reals.");
    }


}


class SNLessThan extends SNBinOp {

    public SNLessThan(SyntaxNode l, SyntaxNode r) {
        super(l, r, new LessThan(), new LessThan(), "less-than");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNNumber(0);
    }


}


class OpLessThanScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        if (((ValueScalar) a).number() < ((ValueScalar) b).number())
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
