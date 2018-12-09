//
//  ParameterDeriv.java
//  mathbuild
//
//  Created by David Eigen on Wed Aug 07 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;

public class ParameterDeriv {

    private ParameterID param_;
    private SyntaxNode node_;

    /**
     * Creates a parameter environment entry used for taking derivatives.
     * @param param the parameter
     * @param val the Value for the derivative of this parameter
     */
    public ParameterDeriv(ParameterID param, Value val) {
        param_ = param;
        node_ = new SNVal(val);
    }

    /**
     * Creates a parameter environment entry used for taking derivatives.
     * @param param the parameter
     * @param node the SNVal for the derivative of this parameter
     */
    public ParameterDeriv(ParameterID param, SyntaxNode node) {
        param_ = param;
        node_ = node;
    }

    /**
     * @return the parameter for this entry
     */
    public ParameterID param() {
        return param_;
    }


    /**
     * @return the SNVal for the derivative of this parameter
     */
    public SyntaxNode node() {
        return node_;
    }

    

}
