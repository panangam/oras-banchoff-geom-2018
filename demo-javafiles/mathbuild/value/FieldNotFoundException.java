//
//  FieldNotFoundException.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

public class FieldNotFoundException extends mathbuild.MathbuildRuntimeException {

    public FieldNotFoundException(String name) {
        super("Member " + name + " is not defined.");
    }
    
}
