//
//  OpAndMat.java
//  Demo
//
//  Created by David Eigen on Thu Aug 15 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

/**
 * Given operations ops that return a scalars: When operating on ValueMatrixes a and b,
 * OpAndMat performs a[i,j] ops[i,j] b[i,j] for all components i. If a[i,j] ops[i,j] b[i,j]
 * is "true" (i.e., greater than 0) for all operations, OpAndMat returns 1. Otherwise,
 * OpAndMat returns 0.
 */
public class OpAndMat implements OpBin {
    private OpBin[][] ops_;
    int numRows_, numCols_;
    public OpAndMat(TypeMatrix rt, TypeMatrix lt, OperatorBinFactory factory) {
        if (lt.numRows() != rt.numRows() || lt.numCols() != rt.numCols())
            throw new BuildException("Left and right matrices must have the same dimension.");
        numRows_ = rt.numRows();
        numCols_ = rt.numCols();
        ops_ = new OpBin[numRows_][numCols_];
        for (int i = 0; i < numRows_; ++i)
            for (int j = 0; j < numCols_; ++j)
                ops_[i][j] = factory.makeOperator(lt.componentType(i,j), rt.componentType(i,j));
    }
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        ValueMatrix amat = (ValueMatrix) a;
        ValueMatrix bmat = (ValueMatrix) b;
        for (int i = 0; i < numRows_; ++i)
            for (int j = 0; j < numCols_; ++j)
                if ( ! (((ValueScalar) ops_[i][j]
                         .operate(amat.component(i,j), bmat.component(i,j))).number() > 0) )
                    return new ValueScalar(0);
        return new ValueScalar(1);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
