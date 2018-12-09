//
//  Negate.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Negate implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Negate instance_ = new Negate();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Negate inst() {
        return instance_;
    }
    
    /**
    * Makes a syntax node for an negation operation.
     * @param sn the child node
     * @return a SyntaxNode for negation
     */
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNNegate(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpNegateScal();
        if (t.isType(Type.VECTOR)) {
            for (int i = 0; i < ((TypeVector) t).numComponents(); ++i)
                if (!((TypeVector) t).componentType(i).isType(Type.SCALAR))
                    return new OpCorrespVecUn((TypeVector) t, this);
            return new OpNegateVec(((TypeVector) t).numComponents());
        }
        if (t.isType(Type.MATRIX))
            return new OpCorrespMatUn((TypeMatrix) t, this);
        throw new BuildException("Incompatible types for negation.");
    }
    
}


class SNNegate extends SNUnOp {

    public SNNegate(SyntaxNode sn) {
        super(sn, Negate.inst(), Negate.inst(), "negate");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNNegate(doperand);
    }

}


class OpNegateScal implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value v) {
        return new ValueScalar(-((ValueScalar) v).number());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}


class OpNegateVec implements OpUn {
    int dim_;
    public OpNegateVec(int dim) {dim_ = dim;}
    public Type type() {
        return new TypeVector(dim_);
    }
    public Value operate(Value a) {
        Value[] vec = new Value[dim_];
        Value[] av = ((ValueVector) a).values();
        for (int i = 0; i < dim_; ++i)
            vec[i] = new ValueScalar(-((ValueScalar) av[i]).num());
        return new ValueVector(vec);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}


