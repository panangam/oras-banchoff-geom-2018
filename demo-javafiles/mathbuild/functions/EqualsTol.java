//
//  EqualsTol.java
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

public class EqualsTol implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final EqualsTol instance_ = new EqualsTol();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static EqualsTol inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNEqualsTol(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpEqualsTolScal();
        if (lt.isType(Type.VECTOR) && rt.isType(Type.VECTOR))
            return new OpAndVec((TypeVector) lt, (TypeVector) rt, this);
        if (lt.isType(Type.MATRIX) && rt.isType(Type.MATRIX))
            return new OpAndMat((TypeMatrix) lt, (TypeMatrix) rt, this);
        // they're different types, so they're not equal
        return new OpVal(new ValueScalar(0));
    }


}


class SNEqualsTol extends SNBinOp {

    public SNEqualsTol(SyntaxNode l, SyntaxNode r) {
        super(l, r, EqualsTol.inst(), EqualsTol.inst(), "equals-tol");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNNumber(0);
    }


}


class OpEqualsTolScal implements OpBin {

    public static double EQUALS_TOLERANCE = 1E-10;
    
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        if (Math.abs(((ValueScalar) a).number() - ((ValueScalar) b).number()) < EQUALS_TOLERANCE)
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
