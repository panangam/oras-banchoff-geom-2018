//
//  IncompatibleTypeException.java
//  Demo
//
//  Created by David Eigen on Wed Aug 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.expr;

/**
 * IncompatibleTypeException is thrown if the return type of an
 * Expression does not match an expected type.
 * This exception may be thrown my classes outside of the 
 * demo.expr package.
 */
public class IncompatibleTypeException extends demo.DemoRuntimeException {

    public IncompatibleTypeException(String msg) {
        super(msg);
    }

}
