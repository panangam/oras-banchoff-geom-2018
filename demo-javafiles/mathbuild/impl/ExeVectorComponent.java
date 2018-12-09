//
//  ExeVectorComponent.java
//  mathbuild
//
//  Created by David Eigen on Sun Jul 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class ExeVectorComponent implements Executor {

    private int component_;
    private Executor exe_;
    
    public ExeVectorComponent(int component, Executor exe) {
        component_ = component;
        exe_ = exe;
    }

    public Value execute(Object runID) {
        return ((ValueVector) exe_.execute(runID)).component(component_);
    }

    public Type type() {
        return ((TypeVector) exe_.type()).componentType(component_);
    }

}
