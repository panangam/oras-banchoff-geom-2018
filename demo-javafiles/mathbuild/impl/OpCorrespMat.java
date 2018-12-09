//
//  OpCorrespMat.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespMat implements OpBin {
    
    private OpBin[][] ops_;
    public OpCorrespMat(OpBin[][] ops) {ops_ = ops;}

    /**
     * Creates a corresponding matrix operation with types created from the given operator factory.
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factory the operator factory to get component operators
     */
    public OpCorrespMat(TypeMatrix leftType, TypeMatrix rightType, OperatorBinFactory factory) {
        if (leftType.numRows() != rightType.numRows() || leftType.numCols() != rightType.numCols())
            throw new BuildException("Dimensions of left and right matrices don't match.");
        ops_ = new OpBin[leftType.numRows()][leftType.numCols()];
        for (int i = 0; i < ops_.length; ++i)
            for (int j = 0; j < ops_[i].length; ++j)
                ops_[i][j] = factory.makeOperator(leftType.componentType(i,j), rightType.componentType(i,j));
    }
    
    
    public Type type() {
        Type[][] result = new Type[ops_.length][];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new Type[ops_[i].length];
            for (int j = 0; j < result[i].length; ++j)
                result[i][j] = ops_[i][j].type();
        }
        return new TypeMatrix(result);
    }
    public Value operate(Value a, Value b) {
        Value[][] result = new Value[ops_.length][];
        ValueMatrix amat = (ValueMatrix) a;
        ValueMatrix bmat = (ValueMatrix) b;
        for (int i = 0; i < result.length; ++i) {
            result[i] = new Value[ops_[i].length];
            for (int j = 0; j < result[i].length; ++j)
                result[i][j] = ops_[i][j].operate(amat.component(i,j), bmat.component(i,j));
        }
        return new ValueMatrix(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
