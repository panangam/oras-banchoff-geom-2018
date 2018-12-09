//
//  Multiply.java
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

public class Multiply implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Multiply instance_ = new Multiply();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Multiply inst() {
        return instance_;
    }
    
    /**
     * Makes a syntax node for an multiplication operation.
     * Does dot product in the case of vectors, matrix multiplcation for matrices.
     * @param left the left child node
     * @param right the right child node
     * @return a SyntaxNode for multiplcation
     */
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNMultiply(left, right);
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
        if (lt.isType(Type.MATRIX) && rt.isType(Type.VECTOR))
            return new OpMatVecMult((TypeMatrix) lt, (TypeVector) rt, Add.inst(), this);
        if (lt.isType(Type.MATRIX) && rt.isType(Type.MATRIX))
            return new OpMatMult((TypeMatrix) lt, (TypeMatrix) rt, Add.inst(), this);
        throw new BuildException("Incompatible types for multiplication/dot product.");
    }

    
}


class SNMultiply extends SNBinOp {

    public SNMultiply(SyntaxNode l, SyntaxNode r) {
        super(l, r, Multiply.inst(), Multiply.inst(), "mult");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNAdd(new SNMultiply(left_, dr), new SNMultiply(dl, right_));
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
        return new SNMultiply(l,r);
    }

}


class OpMultiplyScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(((ValueScalar) a).number() * ((ValueScalar) b).number());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}



class OpDotVec implements OpBin {
    private OpBin addOp_;
    private OpBin[] multiplyOps_;
    private Value zero_;
    private int numComponents_;

    /**
     * Creates an operator for a vector dot product.
     * @param leftType the type of the left vector
     * @param rightType the type of the right vector
     * @param addFactory operator factory for making the addition used to add the result
     *        of multiplying corresponding components together in the dot product
     * @param multiplyFactory operator factory for making the multiplication used to multiply components
     *        for the dot product
     */
    public OpDotVec(TypeVector leftType, TypeVector rightType,
                    OperatorBinFactory addFactory, OperatorBinFactory multiplyFactory) {
        if (leftType.numComponents() != rightType.numComponents())
            throw new BuildException("Number of components in left and right vectors don't match for dot product.");
        numComponents_ = leftType.numComponents();
        multiplyOps_ = new OpBin[numComponents_];
        for (int i = 0; i < numComponents_; ++i)
            multiplyOps_[i] = multiplyFactory.makeOperator(leftType.componentType(i),
                                                           rightType.componentType(i));
        Type returnType = multiplyOps_[0].type();
        for (int i = 1; i < multiplyOps_.length; ++i) {
            Type t = multiplyOps_[i].type();
            if (!returnType.compatibleType(t))
                throw new BuildException("Component types not compatible for addition in dot product.");
        }
        zero_ = MB.zero(returnType);
        addOp_ = addFactory.makeOperator(returnType, returnType);
    }

    public Type type() {
        return zero_.type();
    }
    public Value operate(Value a, Value b) {
        Value result = zero_;
        ValueVector avec = (ValueVector) a;
        ValueVector bvec = (ValueVector) b;
        for (int i = 0; i < numComponents_; ++i)
            result = addOp_.operate(result, multiplyOps_[i].operate(avec.component(i), bvec.component(i)));
        return result;
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}



class OpMatMult implements OpBin {
    private OpBin add_, multiply_;
    private Value zero_;
    private Type returnType_;
    private int resultRows_, resultCols_;
    private int dotLength_;
    /**
     * Creates an operator for matrix multiplication.
     * @param leftType the type of the left matrix
     * @param rightType the type of the right matrix
     * @param addFactory operator factory for making the addition used to add the result
     *        of multiplying matrix components together
     * @param multiplyFactory operator factory for making the multiplication used to
     *        multiply matrix components together
     */
    public OpMatMult(TypeMatrix leftType, TypeMatrix rightType,
                     OperatorBinFactory addFactory, OperatorBinFactory multiplyFactory) {
        if (leftType.numCols() != rightType.numRows())
            throw new BuildException("Dimensions of left and right matrices incompatible for matrix multiplication.");
        dotLength_ = leftType.numCols();
        Type leftCompType = leftType.componentType(0,0);
        for (int i = 0; i < leftType.numRows(); ++i)
            for (int j = 0; j < leftType.numCols(); ++j)
                if (!leftCompType.compatibleType(leftType.componentType(i,j)))
                    throw new BuildException("Components of left matrix of matrix multiplication must all be the same type.");
        Type rightCompType = rightType.componentType(0,0);
        for (int i = 0; i < rightType.numRows(); ++i)
            for (int j = 0; j < rightType.numCols(); ++j)
                if (!rightCompType.compatibleType(rightType.componentType(i,j)))
                    throw new BuildException("Components of right matrix of matrix multiplication must all be the same type.");
        multiply_ = multiplyFactory.makeOperator(leftCompType, rightCompType);
        zero_ = MB.zero(multiply_.type());
        add_ = addFactory.makeOperator(zero_.type(), multiply_.type());
        resultRows_ = leftType.numRows();
        resultCols_ = rightType.numCols();
        Type[][] returnTypeTypes = new Type[resultRows_][resultCols_];
        for (int i = 0; i < returnTypeTypes.length; ++i)
            for (int j = 0; j < returnTypeTypes[i].length; ++j)
                returnTypeTypes[i][j] = zero_.type();
        returnType_ = new TypeMatrix(returnTypeTypes);
    }

    public Type type() {
        return returnType_;
    }
    public Value operate(Value a, Value b) {
        ValueMatrix amat = (ValueMatrix) a;
        ValueMatrix bmat = (ValueMatrix) b;
        Value[][] result = new Value[resultRows_][resultCols_];
        for (int i = 0; i < resultRows_; ++i) {
            for (int j = 0; j < resultCols_; ++j) {
                Value entry = zero_;
                for (int x = 0; x < dotLength_; ++x)
                    entry = add_.operate(entry, multiply_.operate(amat.component(i,x),
                                                                  bmat.component(x,j)));
                result[i][j] = entry;
            }
        }
        return new ValueMatrix(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}



class OpMatVecMult implements OpBin {
    private OpBin add_, multiply_;
    private Value zero_;
    private Type returnType_;
    private int vectorComponents_;
    private int resultComponents_;
    /**
     * Creates an operator for matrix - vector multiplication.
     * @param leftType the type of the left matrix
     * @param rightType the type of the right vector
     * @param addFactory operator factory for making the operator used to add the result
     *        of multiplying components together
     * @param multiplyFactory operator factory for making the operator used to
     *        multiply components together
     */
    public OpMatVecMult(TypeMatrix leftType, TypeVector rightType,
                        OperatorBinFactory addFactory, OperatorBinFactory multiplyFactory) {
        if (leftType.numCols() != rightType.numComponents())
            throw new BuildException("Dimensions of matrix and vector incompatible for matrix*vector multiplication.");
        vectorComponents_ = rightType.numComponents();
        Type leftCompType = leftType.componentType(0,0);
        for (int i = 0; i < leftType.numRows(); ++i)
            for (int j = 0; j < leftType.numCols(); ++j)
                if (!leftCompType.compatibleType(leftType.componentType(i,j)))
                    throw new BuildException("Components of matrix of matrix*vector multiplication must all be the same type.");
        Type rightCompType = rightType.componentType(0);
        for (int i = 1; i < rightType.numComponents(); ++i)
            if (!rightCompType.compatibleType(rightType.componentType(i)))
                throw new BuildException("Components of vector of matrix*vector multiplication must all be the same type.");
        multiply_ = multiplyFactory.makeOperator(leftCompType, rightCompType);
        zero_ = MB.zero(multiply_.type());
        add_ = addFactory.makeOperator(zero_.type(), multiply_.type());
        resultComponents_ = leftType.numRows();
        Type[] returnTypeTypes = new Type[resultComponents_];
        for (int i = 0; i < returnTypeTypes.length; ++i)
            returnTypeTypes[i] = zero_.type();
        returnType_ = new TypeVector(returnTypeTypes);
    }

    public Type type() {
        return returnType_;
    }
    public Value operate(Value a, Value b) {
        ValueMatrix amat = (ValueMatrix) a;
        ValueVector bvec = (ValueVector) b;
        Value[] result = new Value[resultComponents_];
        for (int i = 0; i < resultComponents_; ++i) {
            Value entry = zero_;
            for (int x = 0; x < vectorComponents_; ++x)
                entry = add_.operate(entry, multiply_.operate(amat.component(i,x),
                                                              bvec.component(x)));
            result[i] = entry;
        }
        return new ValueVector(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}


