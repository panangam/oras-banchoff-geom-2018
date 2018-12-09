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

public class OpCorrespVecUn implements OpUn {
    private OpUn[] ops_;
    public OpCorrespVecUn(OpUn[] ops) {ops_ = ops;}

    /**
     * Creates a corresponding vector operation with types created from the given operator factory.
     * @param argType the argument's type
     * @param factor the operator factory to get component operators
     */
    public OpCorrespVecUn(TypeVector argType, OperatorUnFactory factory) {
        ops_ = new OpUn[argType.numComponents()];
        for (int i = 0; i < ops_.length; ++i)
            ops_[i] = factory.makeOperator(argType.componentType(i));
    }

    public Type type() {
        Type[] result = new Type[ops_.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].type();
        return new TypeVector(result);
    }
    public Value operate(Value v) {
        Value[] result = new Value[ops_.length];
        ValueVector vvec = (ValueVector) v;
        for (int i = 0; i < result.length; ++i)
            result[i] = ops_[i].operate(vvec.component(i));
        return new ValueVector(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}
