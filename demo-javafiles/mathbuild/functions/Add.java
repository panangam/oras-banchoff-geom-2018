//
//  Add.java
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

public class Add implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Add instance_ = new Add();
    /**
     * Returns the (singleton) instance of this class.
     */
    public static Add inst() {
        return instance_;
    }

    /**
     * Makes a syntax node for an addition operation.
     * @param left the left child node
     * @param right the right child node
     * @return a SyntaxNode for addition
     */
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNAdd(left, right);
    }

    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpAddScal();
        if (lt.isType(Type.VECTOR) && rt.isType(Type.VECTOR)) {
            TypeVector lvt = (TypeVector) lt, rvt = (TypeVector) rt;
            if (lvt.numComponents() != rvt.numComponents())
                throw new BuildException("Number of components in left and right vectors don't match.");
            for (int i = 0; i < lvt.numComponents(); ++i)
                if (!(lvt.componentType(i).isType(Type.SCALAR) &&
                      rvt.componentType(i).isType(Type.SCALAR)))
                    return new OpCorrespVec((TypeVector) lt, (TypeVector) rt, this);
            return new OpAddVec(lvt.numComponents());
        }
        if (lt.isType(Type.MATRIX) && rt.isType(Type.MATRIX))
            return new OpCorrespMat((TypeMatrix) lt, (TypeMatrix) rt, this);
        throw new BuildException("Incompatible types for addition.");
    }

    
    
}


class SNAdd extends SNBinOp {

    public SNAdd(SyntaxNode l, SyntaxNode r) {
        super(l, r, Add.inst(), Add.inst(), "add");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNAdd(dl, dr);
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (l.isZero())
            return r;
        if (r.isZero())
            return l;
        return new SNAdd(l,r);
    }
    
}


class OpAddScal implements OpBin {
    public OpAddScal() {}
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(((ValueScalar) a).number() + ((ValueScalar) b).number());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}


class OpAddVec implements OpBin {
    int dim_;
    public OpAddVec(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a, Value b) {
        Value[] vec = new Value[dim_];
        Value[] bv = ((ValueVector) b).values();
        Value[] av = ((ValueVector) a).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(((ValueScalar) av[i]).num() + ((ValueScalar) bv[i]).num());
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}


