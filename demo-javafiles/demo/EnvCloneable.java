//
//  EnvCloneable.java
//  Demo
//
//  Created by David Eigen on Mon Apr 07 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo;

/**
 * Interface to produce a clone of the object.
 * All expressions are re-parsed in the Environment given to clone(.).
 */
public interface EnvCloneable {

    /**
     * Clones this object, reparsing all expressions in the given environment.
     */
    public Object clone(mathbuild.Environment env);
    
}
