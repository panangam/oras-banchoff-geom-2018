//
//  ExeMatrixComponent.java
//  mathbuild
//
//  Created by David Eigen on Sun Jul 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class ExeMatrixComponent implements Executor {

    private int row_, col_;
    private Executor exe_;
    
    public ExeMatrixComponent(int row, int col, Executor exe) {
        row_ = row;
        col_ = col;
        exe_ = exe;
    }

    public Value execute(Object runID) {
        return ((ValueMatrix) exe_.execute(runID)).component(row_, col_);
    }

    public Type type() {
        return ((TypeMatrix) exe_.type()).componentType(row_, col_);
    }
    
}
