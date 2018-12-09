//
//  HasDependentsException.java
//  Demo
//
//  Created by David Eigen on Fri Aug 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.depend;

/**
 * This exception may be thrown if a function can only succeed if
 * some object does not have any objects dependent on it. For example,
 * in the implementation of many object removals (such as 
 * variable removals), the operation can only take place if no
 * objects depend on the variable. While currently nothing in the
 * demo.depend package throws HasDependentsException, it is used
 * outside the package, especially in the user interface.
 */
public class HasDependentsException extends Exception {

    public HasDependentsException(String message) {
        super(message);
    }

}
