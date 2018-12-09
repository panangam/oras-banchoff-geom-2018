//
//  DemoRuntimeException.java
//  Demo
//
//  Created by David Eigen on Wed Aug 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo;

/**
 * Superclass of all RuntimeExceptions thrown by demo classes.
 */
public class DemoRuntimeException extends RuntimeException {
    public DemoRuntimeException(String msg) {
        super(msg);
    }
}
