//
//  TypeScalar.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

public class TypeScalar extends Type {

    public TypeScalar() {
        super(SCALAR);
    }


    public boolean isType(int type) {
        return type == SCALAR;
    }

    public boolean isType(Type type) {
        return type instanceof TypeScalar;
    }
    
    public boolean compatibleType(Type type) {
        return type instanceof TypeScalar;
    }

    public boolean containsType(int type) {
        return type == SCALAR;
    }

    public boolean containsType(Type type) {
        return type instanceof TypeScalar;
    }

    public String toString() {
        return "real";
    }

    
}
