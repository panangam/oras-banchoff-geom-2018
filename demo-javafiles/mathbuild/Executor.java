//
//  Executor.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.value.Value;
import mathbuild.type.Type;

public interface Executor {

    /**
     * Executes this executor object, and returns the value resulting from execution.
     * @param runID an Object unique to this run of execute(.). The run ID is used
     *        to tell one run of execute apart from another, so cached values for
     *        some executor nodes can be stored.
     * @return the resulting value
     */
    public Value execute(Object runID);

    /**
     * @return the type of the value obtained from execution
     */
    public Type type();

}
