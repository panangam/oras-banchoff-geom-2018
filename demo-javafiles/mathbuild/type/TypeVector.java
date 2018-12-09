//
//  TypeVector.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

import mathbuild.MB;

public class TypeVector extends Type {

    private Type[] types_;

    public TypeVector(Type[] components) {
        super(VECTOR);
        types_ = components;
    }

    /**
     * Creates a TypeVector whose component types are all TypeScalar.
     * @param numComponents the number of component scalar types
     */
    public TypeVector(int numComponents) {
        super(VECTOR);
        types_ = new Type[numComponents];
        for (int i = 0; i < numComponents; ++i)
            types_[i] = MB.TYPE_SCALAR;
    }

    /**
     * @return the number of components in this vector type
     */
    public int numComponents() {
        return types_.length;
    }
    
    /**
     * @return the component types
     */
    public Type[] componentTypes() {
        return types_;
    }

    /**
     * @param the component number (indexed from 0)
     * @return the given component type
     */
    public Type componentType(int component) {
        return types_[component];
    }


    public boolean isType(int type) {
        return type == VECTOR;
    }

    public boolean isType(Type type) {
        if (!(type instanceof TypeVector)) return false;
        Type[] comps = ((TypeVector) type).componentTypes();
        if (types_.length != comps.length)
            return false;
        for (int i = 0; i < comps.length; ++i) {
            if (!types_[i].isType(comps[i]))
                return false;
        }
        return true;
    }

    public boolean compatibleType(Type type) {
        if (!(type instanceof TypeVector)) return false;
        Type[] comps = ((TypeVector) type).componentTypes();
        if (types_.length != comps.length)
            return false;
        for (int i = 0; i < comps.length; ++i) {
            if (!types_[i].compatibleType(comps[i]))
                return false;
        }
        return true;
    }

    public boolean containsType(int type) {
        if (type == VECTOR)
            return true;
        for (int i = 0; i < types_.length; ++i)
            if (types_[i].containsType(type))
                return true;
        return false;
    }

    public boolean containsType(Type type) {
        for (int i = 0; i < types_.length; ++i)
            if (types_[i].containsType(type))
                return true;
        return isType(type);
    }

    public String toString() {
        String str = "(";
        for (int i = 0; i < types_.length; ++i)
            str += types_[i] + (i < types_.length - 1 ? ", " : ")");
        return str;
    }
    
}
