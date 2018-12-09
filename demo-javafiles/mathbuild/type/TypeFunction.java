//
//  TypeFunction.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

import mathbuild.value.ValueFunction;
import mathbuild.impl.ParameterID;

public class TypeFunction extends Type {

    private ValueFunction val_;

    public TypeFunction(ValueFunction val) {
        super(FUNCTION);
        val_ = val;
    }

    /**
     * @return the function
     */
    public ValueFunction val() {
        return val_;
    }

    /**
     * @return the number of arguments for this function type
     */
    public int numArgs() {
        return val_.numArgs();
    }

    public boolean isType(int type) {
        return type == FUNCTION;
    }

    public boolean isType(Type type) {
        if (!(type instanceof TypeFunction)) return false;
        return val_.equals(((TypeFunction) type).val());
    }

    public boolean compatibleType(Type type) {
        if (!(type instanceof TypeFunction)) return false;
        return numArgs() == ((TypeFunction) type).numArgs();
    }

    public boolean containsType(int type) {
        return isType(type);
    }

    public boolean containsType(Type type) {
        return isType(type);
    }

    public String toString() {
        String str = "func(";
        ParameterID[] params = val_.parameters();
        for (int i = 0; i < params.length; ++i)
            str += params[i].name() + (i < params.length - 1 ? "," : ")");
        return str;
    }
    
}
