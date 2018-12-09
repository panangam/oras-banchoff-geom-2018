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
 * A OperatorUnFactory can create a unary operator given the arugment type.
 * It is used to easily implement new operators and funcitons -- see the function
 * definition class files (Add.java, etc.) for examples.
 */
public interface OperatorUnFactory {

    /**
     * @param type the type of the argument
     * @return a unary operator that operates on the given types
     */
    public OpUn makeOperator(Type type) ;

}
