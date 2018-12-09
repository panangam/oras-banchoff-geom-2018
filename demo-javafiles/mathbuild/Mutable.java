//
//  MutableRestorable.java
//  Demo
//
//  Created by David Eigen on Tue Apr 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild;

/**
 * A Mutable class is a class that can set up itself to execute a given
 * Executor value upon executing.
 */
public interface Mutable {

    /**
     * Mutates this so that it executes the given exe.
     * If this Mutable is an Executor and exe is this object, the call
     * should make this executor simply execute itself.
     */
    public void mutate(Executor exe);

    /**
     * @return what this Mutable is currently executing.
     *         If this Mutable is an Executor executing itself, this
     *         object is returned.
     */
    public Executor currentMutation();
    
}
