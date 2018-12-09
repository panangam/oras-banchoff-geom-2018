//
//  TypeRecord.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

import mathbuild.MathbuildRuntimeException;

public class TypeRecord extends Type {

    private int numFields_;
    private java.util.Dictionary fields_ = new java.util.Hashtable(5);
    
    /**
     * Creats a Record type, with fields having the given names and types.
     * @param names the names of the fields in this Record type
     * @param types the types of the fields in this Record type
     */
    public TypeRecord(String[] names, Type[] types) {
        super(RECORD);
        if (names.length != types.length)
            throw new MathbuildRuntimeException("Number of names and types must match.");
        for (int i = 0; i < names.length; ++i)
            fields_.put(names[i], types[i]);
        numFields_ = types.length;
    }

    /**
     * Looks up a field in this record type, and returns the type of the field.
     * @param fieldName the name of the field to look up.
     * @return the type of the given field.
     */
    public Type fieldType(String fieldName) {
        return (Type) fields_.get(fieldName);
    }

    /**
     * @return whether this type contains the given field.
     */
    public boolean containsField(String fieldName) {
        return fieldType(fieldName) != null;
    }

    
    public boolean isType(int type) {
        return type == RECORD;
    }

    public boolean isType(Type type) {
        if (!(type instanceof TypeRecord))
            return false;
        TypeRecord t = (TypeRecord) type;
        if (t.numFields_ != numFields_)
            return false;
        java.util.Enumeration names = fields_.keys();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (t.fields_.get(name) == null ||
                !((Type) fields_.get(name)).isType((Type) t.fields_.get(name)))
                return false;
        }
        return true;
    }

    public boolean compatibleType(Type type) {
        return isType(type);
    }

    public boolean containsType(int type) {
        if (type == RECORD)
            return true;
        java.util.Enumeration types = fields_.elements();
        while (types.hasMoreElements())
            if (((Type) types.nextElement()).containsType(type))
                return true;
        return false;
    }

    public boolean containsType(Type type) {
        if (isType(type))
            return true;
        java.util.Enumeration types = fields_.elements();
        while (types.hasMoreElements())
            if (((Type) types.nextElement()).containsType(type))
                return true;
        return false;
    }

    public String toString() {
        String str = "{";
        java.util.Enumeration names = fields_.keys();
        for (int i = 0; i < numFields_; ++i) {
            String n = (String) names.nextElement();
            str += n + " : " + fields_.get(n) + (i < numFields_ - 1 ? " | " : "}");
        }
        return str;
    }
    
}
