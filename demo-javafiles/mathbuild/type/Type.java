//
//  Type.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

public abstract class Type {

    public static final int SCALAR = 1;
    public static final int VECTOR = 2;
    public static final int MATRIX = 3;
    public static final int FUNCTION = 4;
    public static final int RECORD = 5;
    public static final int LIST = 6;

    private final int type_;


    protected Type(int type) {
        type_ = type;
    }

    /**
     * This method returns one of: ZERO, SCALAR, VECTOR, MATRIX, FUNCTION
     * @return the type of this Type
     */
    public int type() {
        return type_;
    }

    /**
     * @return whether this type is the given type
     */
    public abstract boolean isType(int type);

    /**
     * Note: Type matching with this method is more strict, since things like component
     * types need to be matched as well.
     * In the case of a function type, this method returns true only if the values of each
     * function type have the same function Value.
     * @return whether this type is the given type
     */
    public abstract boolean isType(Type type);

    /**
     * Same as compatibleType(int), but also checks component types, etc. In
     * the case of a function type, this method returns true only if each function
     * type has the same number of arguments.
     * @return whether this type is compatible with the given type for (most) operations
     */
    public abstract boolean compatibleType(Type type);

    /**
     * Checks to see if a given type appears anywhere in this type.
     * A type "appears" in a type if it is the type itself, or if
     * it appears in any components of the vector or matrix types.
     * @return true if the type appears anywhere in this type
     */
    public abstract boolean containsType(int type);

    /**
     * Checks to see if a given type appears anywhere in this type.
     * A type "appears" in a type if it is the type itself, or if
     * it appears in any components of the vector or matrix types.
     * With this method, two types are the same iff all their component
     * types are the same. In the case of function types, the two types
     * must have the same function Value in order to be the same.
     * @return true if the type appears anywhere in this type
     */
    public abstract boolean containsType(Type type);


    public abstract String toString();
    
}
