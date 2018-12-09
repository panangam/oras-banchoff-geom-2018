//
//  OperatorFactory.java
//  mathbuild
//
//  Created by David Eigen on Fri May 24 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.Type;


/**
 * A OperatorBinFactory can create a binary operator given left and right types.
 * It is used to easily implement new operators and funcitons -- see the function
 * definition class files (Add.java, etc.) for examples.
 */
public interface OperatorBinFactory {

    /**
     * @param leftType the type of the left argument
     * @param rightType the type of the right argument
     * @return a binary operator that operates on the given types
     */
    public OpBin makeOperator(Type leftType, Type rightType) ;
    
}
