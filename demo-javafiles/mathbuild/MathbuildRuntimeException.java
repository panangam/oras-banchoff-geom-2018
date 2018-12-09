//
//  MathbuildRuntimeException.java
//  Demo
//
//  Created by David Eigen on Wed Aug 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

/**
 * Superclass of all RuntimeExceptions thrown by mathbuild classes.
 */
public class MathbuildRuntimeException extends RuntimeException {
    public MathbuildRuntimeException(String msg) {
        super(msg);
    }
}
