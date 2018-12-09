//
//  OpAndVec.java
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
 * Given operations ops that return a scalars: When operating on ValueVectors a and b,
 * OpAndVec performs a[i] ops[i] b[i] for all components i. If a[i] ops[i] b[i] is "true" (i.e., 
 * greater than 0) for all operations, OpAndVec returns 1. Otherwise, OpAndVec returns 0.
 */
public class OpAndVec implements OpBin {
    private OpBin[] ops_;
    int numComponents_;
    public OpAndVec(TypeVector rt, TypeVector lt, OperatorBinFactory factory) {
        if (lt.numComponents() != rt.numComponents())
            throw new BuildException("Left and right vectors must have same number of components.");
        numComponents_ = rt.numComponents();
        ops_ = new OpBin[numComponents_];
        for (int i = 0; i < numComponents_; ++i)
            ops_[i] = factory.makeOperator(lt.componentType(i), rt.componentType(i));
    }
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        ValueVector avec = (ValueVector) a;
        ValueVector bvec = (ValueVector) b;
        for (int i = 0; i < numComponents_; ++i)
            if ( ! (((ValueScalar) ops_[i].operate(avec.component(i), bvec.component(i))).number() > 0) )
                return new ValueScalar(0);
        return new ValueScalar(1);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
