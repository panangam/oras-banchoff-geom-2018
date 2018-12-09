//
//  Divide.java
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

public class Divide implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Divide instance_ = new Divide();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Divide inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNDivide(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpDivideScal();
        if (lt.isType(Type.VECTOR) && rt.isType(Type.SCALAR)) {
            TypeVector vt = (TypeVector) lt;
            for (int i = 0; i < vt.numComponents(); ++i)
                if (!vt.componentType(i).isType(Type.SCALAR))
                    return new OpCorrespVecX((TypeVector) lt, rt, this);
            return new OpDivideVecScal(vt.numComponents());
        }
        if (lt.isType(Type.MATRIX) && rt.isType(Type.SCALAR))
            return new OpCorrespMatX((TypeMatrix) lt, rt, this);
        throw new BuildException("Incompatible types for division.");
    }

    
}


class SNDivide extends SNBinOp {

    public SNDivide(SyntaxNode l, SyntaxNode r) {
        super(l, r, Divide.inst(), Divide.inst(), "divide");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNDivide(new SNSubtract(new SNMultiply(dl, right_),
                                           new SNMultiply(left_, dr)),
                            new SNExponent(right_, new SNNumber(2)));
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (r.isValue(1) || l.isZero())
            return l;
        return new SNDivide(l,r);
    }

}


class OpDivideScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(((ValueScalar) a).number() / ((ValueScalar) b).number());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}


class OpDivideVecScal implements OpBin {
    int dim_;
    public OpDivideVecScal(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a, Value b) {
        Value[] vec = new Value[dim_];
        double bv = ((ValueScalar) b).num();
        Value[] av = ((ValueVector) a).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(((ValueScalar) av[i]).num() / bv);
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}


