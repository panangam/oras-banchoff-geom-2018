//
//  OpCorrespMatX.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespMatX implements OpBin {
    private OpBin[][] ops_;
    public OpCorrespMatX(OpBin[][] ops) {ops_ = ops;}

    /**
     * Creates an operation for making a new matrix by applying an operation to each component
     * of the left operand and the full right operand. That is, creates a new matrix m such that
     * m[i,j] = l[i,j] <op> r, where l is the left (matrix) operand, r is the right operand,
     * and <op> is the operation we get from the operator factory for l[i,j]<op>r.
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespMatX(TypeMatrix leftType, Type rightType, OperatorBinFactory factory) {
        ops_ = new OpBin[leftType.numRows()][leftType.numCols()];
        for (int i = 0; i < ops_.length; ++i)
            for (int j = 0; j < ops_[i].length; ++j)
                ops_[i][j] = factory.makeOperator(leftType.componentType(i,j), rightType);
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
        ValueMatrix amat = (ValueMatrix) a;
        for (int i = 0; i < result.length; ++i)
            for (int j = 0; j < result[i].length; ++j)
            result[i][j] = ops_[i][j].operate(amat.component(i,j), b);
        return new ValueMatrix(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
