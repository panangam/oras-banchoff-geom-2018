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

public class OpCorrespMatUn implements OpUn {

    private OpUn[][] ops_;
    public OpCorrespMatUn(OpUn[][] ops) {ops_ = ops;}

    /**
     * Creates a corresponding matrix operation with types created from the given operator factory.
     * @param argType the argument's type
     * @param factory the operator factory to get component operators
     */
    public OpCorrespMatUn(TypeMatrix argType, OperatorUnFactory factory) {
        ops_ = new OpUn[argType.numRows()][argType.numCols()];
        for (int i = 0; i < ops_.length; ++i)
            for (int j = 0; j < ops_[i].length; ++j)
                ops_[i][j] = factory.makeOperator(argType.componentType(i,j));
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
    public Value operate(Value v) {
        Value[][] result = new Value[ops_.length][];
        ValueMatrix vmat = (ValueMatrix) v;
        for (int i = 0; i < result.length; ++i) {
            result[i] = new Value[ops_[i].length];
            for (int j = 0; j < result[i].length; ++j)
                result[i][j] = ops_[i][j].operate(vmat.component(i,j));
        }
        return new ValueMatrix(result);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}
