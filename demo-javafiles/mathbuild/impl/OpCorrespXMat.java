//
//  OpCorrespXMat.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespXMat implements OpBin {
    private OpBin[][] ops_;
    public OpCorrespXMat(OpBin[][] ops) {ops_ = ops;}

    /**
     * Creates an operation for making a new matrix by applying an operation to the full right 
     * operand and each component of the left operand. That is, creates a new matrix m such that
     * m[i,j] = l <op> r[i,j], where l is the left operand, r is the right (matrix) operand,
     * and <op> is the operation we get from the operator factory for l<op>r[i,j].
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespXMat(Type leftType, TypeMatrix rightType, OperatorBinFactory factory) {
        ops_ = new OpBin[rightType.numRows()][rightType.numCols()];
        for (int i = 0; i < ops_.length; ++i)
            for (int j = 0; j < ops_[i].length; ++j)
                ops_[i][j] = factory.makeOperator(leftType, rightType.componentType(i,j));
    }

    public Type type() {
        Type[][] result = new Type[ops_.length][ops_[0].length];
        for (int i = 0; i < result.length; ++i)
            for (int j = 0; j < result[i].length; ++j)
                result[i][j] = ops_[i][j].type();
        return new TypeMatrix(result);
    }
    public Value operate(Value a, Value b) {
        Value[][] result = new Value[ops_.length][ops_[0].length];
        ValueMatrix bmat = (ValueMatrix) b;
        for (int i = 0; i < result.length; ++i)
            for (int j = 0; j < result[i].length; ++j)
                result[i][j] = ops_[i][j].operate(a, bmat.component(i,j));
        return new ValueMatrix(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
