//
//  Subtract.java
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

public class Subtract implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Subtract instance_ = new Subtract();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Subtract inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNSubtract(left, right);
    }

    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpSubtractScal();
        if (lt.isType(Type.VECTOR) && rt.isType(Type.VECTOR)) {
            TypeVector lvt = (TypeVector) lt, rvt = (TypeVector) rt;
            if (lvt.numComponents() != rvt.numComponents())
                throw new BuildException("Number of components in left and right vectors don't match.");
            for (int i = 0; i < lvt.numComponents(); ++i)
                if (!(lvt.componentType(i).isType(Type.SCALAR) &&
                      rvt.componentType(i).isType(Type.SCALAR)))
                    return new OpCorrespVec((TypeVector) lt, (TypeVector) rt, this);
            return new OpSubtractVec(lvt.numComponents());
        }
        if (lt.isType(Type.MATRIX) && rt.isType(Type.MATRIX))
            return new OpCorrespMat((TypeMatrix) lt, (TypeMatrix) rt, this);
        throw new BuildException("Incompatible types for subtraction.");
    }


}


class SNSubtract extends SNBinOp {

    public SNSubtract(SyntaxNode l, SyntaxNode r) {
        super(l, r, Subtract.inst(), Subtract.inst(), "subtract");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNSubtract(dl, dr);
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (l.isZero())
            return new SNNegate(r);
        if (r.isZero())
            return l;
        return new SNSubtract(l,r);
    }

}


class OpSubtractScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(((ValueScalar) a).number() - ((ValueScalar) b).number());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}

class OpSubtractVec implements OpBin {
    int dim_;
    public OpSubtractVec(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a, Value b) {
        Value[] vec = new Value[dim_];
        Value[] bv = ((ValueVector) b).values();
        Value[] av = ((ValueVector) a).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(((ValueScalar) av[i]).num() - ((ValueScalar) bv[i]).num());
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}


