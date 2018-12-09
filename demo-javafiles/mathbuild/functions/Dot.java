//
//  Dot.java
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

/**
 * Multiply is also used to implement dot product. This dot product is mostly
 * used for the internal chain-rule implementation.
 */
public class Dot implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Dot instance_ = new Dot();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Dot inst() {
        return instance_;
    }

    /**
     * Makes a syntax node for a dot product.
     * Does "dot product" in the case of vectors and matrices.
     * Does not do matrix multiplication.
     * @param left the left child node
     * @param right the right child node
     * @return a SyntaxNode for "dot product"
     */
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNDot(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpMultiplyScal();
        if (lt.isType(Type.SCALAR) && rt.isType(Type.VECTOR)) {
            for (int i = 0; i < ((TypeVector) rt).numComponents(); ++i)
                if (!((TypeVector) rt).componentType(i).isType(Type.SCALAR))
                    return new OpCorrespXVec(lt, (TypeVector) rt, this);
            return new OpScalVecMult(((TypeVector) rt).numComponents());
        }
        if (lt.isType(Type.VECTOR) && rt.isType(Type.SCALAR)) {
            for (int i = 0; i < ((TypeVector) lt).numComponents(); ++i)
                if (!((TypeVector) lt).componentType(i).isType(Type.SCALAR))
                    return new OpCorrespVecX((TypeVector) lt, rt, this);
            return new OpVecScalMult(((TypeVector) lt).numComponents());
        }
        if (lt.isType(Type.VECTOR) && rt.isType(Type.VECTOR)) {
            if (((TypeVector) lt).numComponents() != ((TypeVector) rt).numComponents())
                throw new BuildException("Number of components in left and right vectors don't match for dot product.");
            for (int i = 0; i < ((TypeVector) lt).numComponents(); ++i)
                if (!( ((TypeVector) lt).componentType(i).isType(Type.SCALAR) &&
                       ((TypeVector) rt).componentType(i).isType(Type.SCALAR) ))
                    return new OpDotVec((TypeVector) lt, (TypeVector) rt, Add.inst(), this);
            return new OpVecVecDot(((TypeVector) lt).numComponents());
        }
        if (lt.isType(Type.SCALAR) && rt.isType(Type.MATRIX))
            return new OpCorrespXMat(lt, (TypeMatrix) rt, this);
        if (lt.isType(Type.MATRIX) && rt.isType(Type.SCALAR))
            return new OpCorrespMatX((TypeMatrix) lt, rt, this);
        if (lt.isType(Type.MATRIX) && rt.isType(Type.MATRIX))
            return new OpDotMat((TypeMatrix) lt, (TypeMatrix) rt, Add.inst(), this);
        throw new BuildException("Incompatible types for dot.");
    }

    
}


class SNDot extends SNBinOp {

    public SNDot(SyntaxNode l, SyntaxNode r) {
        super(l, r, Dot.inst(), Dot.inst(), "dot");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNAdd(new SNDot(left_, dr), new SNDot(dl, right_));
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (l.isValue(0))
            return new SNMakeZero(r);
        if (r.isValue(0))
            return new SNMakeZero(l);
        if (l.isValue(1))
            return r;
        if (r.isValue(1))
            return l;
        return new SNDot(l,r);
    }

}



class OpScalVecMult implements OpBin {
    int dim_;
    public OpScalVecMult(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a, Value b) {
        Value[] vec = new Value[dim_];
        double av = ((ValueScalar) a).num();
        Value[] bv = ((ValueVector) b).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(av * ((ValueScalar) bv[i]).num());
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}

class OpVecScalMult implements OpBin {
    int dim_;
    public OpVecScalMult(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a, Value b) {
        Value[] vec = new Value[dim_];
        double bv = ((ValueScalar) b).num();
        Value[] av = ((ValueVector) a).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(bv * ((ValueScalar) av[i]).num());
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}

class OpVecVecDot implements OpBin {
    int dim_;
    public OpVecVecDot(int dim) {dim_ = dim;}
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        Value[] av = ((ValueVector) a).values();
        Value[] bv = ((ValueVector) b).values();
        double v = 0;
        for (int i = 0; i < dim_; ++i)
            v += ((ValueScalar) av[i]).num() * ((ValueScalar) bv[i]).num();
        return new ValueScalar(v);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0],vals[1]);
    }
}


class OpDotMat implements OpBin {
    
    private OpBin addOp_;
    private OpBin[][] multiplyOps_;
    private Value zero_;
    private int numRows_, numCols_;

    /**
     * Creates an operator for a matrix "dot product".
     * @param leftType the type of the left matrix
     * @param rightType the type of the right matrix
     * @param addFactory operator factory for making the addition used to add the result
     *        of multiplying corresponding components together in the dot product
     * @param multiplyFactory operator factory for making the multiplication used to multiply components
     *        for the dot product
     */
    public OpDotMat(TypeMatrix leftType, TypeMatrix rightType,
                    OperatorBinFactory addFactory, OperatorBinFactory multiplyFactory) {
        if ( leftType.numRows() != rightType.numRows() ||
             leftType.numCols() != rightType.numCols() )
            throw new BuildException("Dimensions of left and right matrices don't match for dot product.");
        numRows_ = leftType.numRows();
        numCols_ = rightType.numCols();
        multiplyOps_ = new OpBin[numRows_][numCols_];
        for (int i = 0; i < numRows_; ++i)
            for (int j = 0; j < numCols_; ++j)
                multiplyOps_[i][j] = multiplyFactory.makeOperator(leftType.componentType(i,j),
                                                                  rightType.componentType(i,j));
        Type returnType = multiplyOps_[0][0].type();
        for (int i = 0; i < numRows_; ++i) {
            for (int j = 0; j < numCols_; ++j) {
                Type t = multiplyOps_[i][j].type();
                if (!returnType.compatibleType(t))
                    throw new BuildException("Component types not compatible for addition in dot product");
            }
        }
        zero_ = MB.zero(returnType);
        addOp_ = addFactory.makeOperator(returnType, returnType);
    }

    public Type type() {
        return zero_.type();
    }
    
    public Value operate(Value a, Value b) {
        Value result = zero_;
        ValueMatrix amat = (ValueMatrix) a;
        ValueMatrix bmat = (ValueMatrix) b;
        for (int i = 0; i < numRows_; ++i)
            for (int j = 0; j < numCols_; ++j)
                result = addOp_.operate(result,
                                        multiplyOps_[i][j].operate(amat.component(i,j),
                                                                   bmat.component(i,j)));
        return result;
    }

    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }

}

