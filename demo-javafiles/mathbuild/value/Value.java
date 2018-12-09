//
//  Value.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.Type;

public interface Value {

    /**
     * @return the type of this value
     */
    public  Type type();

    /**
     * @return whether this value equals the given value
     */
    public  boolean equals(Value val);

}
