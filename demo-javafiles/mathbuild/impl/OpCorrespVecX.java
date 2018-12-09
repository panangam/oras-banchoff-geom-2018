//
//  OpCorrespVecX.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespVecX implements OpBin {
    private OpBin[] ops_;
    public OpCorrespVecX(OpBin[] ops) {ops_ = ops;}

    /**
     * Creates an operation for making a new vector by applying an operation to each component
     * of the left operand and the full right operand. That is, creates a new vector v such that
     * v[i] = l[i] <op> r, where l is the left (vector) operand, r is the right operand, and <op>
     * is the operation we get from the operator factory for l[i]<op>r.
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespVecX(TypeVector leftType, Type rightType, OperatorBinFactory factory) {
        ops_ = new OpBin[leftType.numComponents()];
        for (int i = 0; i < ops_.length; ++i)
            ops_[i] = factory.makeOperator(leftType.componentType(i), rightType);
    }

    public Type type() {
        Type[] result = new Type[ops_.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].type();
        return new TypeVector(result);
    }
    public Value operate(Value a, Value b) {
        Value[] result = new Value[ops_.length];
        ValueVector avec = (ValueVector) a;
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].operate(avec.component(i), b);
        return new ValueVector(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
