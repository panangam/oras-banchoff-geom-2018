//
//  Operator.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.type.Type;
import mathbuild.value.Value;

public interface Operator {

    /**
     * Applies this operator to the given values.
     * The result is a value of type type().
     * In many cases, the given values are type-checked -- they may need 
     * to be of the types expected by the operator. This is not necessarily
     * the case, depending on the implementation of Operator.
     */
    public Value operate(Value[] vals);
    
    /**
     * @return the type of the value resulting from applying this operator
     */
    public Type type();

}
