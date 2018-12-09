//
//  OpCorrespXVec.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespXVec implements OpBin {
    private OpBin[] ops_;
    public OpCorrespXVec(OpBin[] ops) {ops_ = ops;}

    /**
     * Creates an operation for making a new vector by applying an operation to the full left operand 
     * and each component of the right operand. That is, creates a new vector v such that
     * v[i] = l <op> r[i], where l is the left operand, r is the right (vector) operand, and <op>
     * is the operation we get from the operator factory for l<op>r[i].
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespXVec(Type leftType, TypeVector rightType, OperatorBinFactory factory) {
        ops_ = new OpBin[rightType.numComponents()];
        for (int i = 0; i < ops_.length; ++i)
            ops_[i] = factory.makeOperator(leftType, rightType.componentType(i));
    }

    public Type type() {
        Type[] result = new Type[ops_.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].type();
        return new TypeVector(result);
    }
    public Value operate(Value a, Value b) {
        Value[] result = new Value[ops_.length];
        ValueVector bvec = (ValueVector) b;
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].operate(a, bvec.component(i));
        return new ValueVector(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
