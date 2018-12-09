//
//  OpCorrespVec.java
//  mathbuild
//
//  Created by David Eigen on Tue Feb 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class OpCorrespVec implements OpBin {
    private OpBin[] ops_;
    public OpCorrespVec(OpBin[] ops) {ops_ = ops;}

    /**
     * Creates a corresponding vector operation with types created from the given operator factory.
     * @param leftType the left argument's type
     * @param rightType the right argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespVec(TypeVector leftType, TypeVector rightType, OperatorBinFactory factory) {
        if (leftType.numComponents() != rightType.numComponents())
            throw new BuildException("Number of components in left and right vectors don't match.");
        ops_ = new OpBin[leftType.numComponents()];
        for (int i = 0; i < ops_.length; ++i)
            ops_[i] = factory.makeOperator(leftType.componentType(i), rightType.componentType(i));
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
        ValueVector bvec = (ValueVector) b;
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].operate(avec.component(i), bvec.component(i));
        return new ValueVector(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
