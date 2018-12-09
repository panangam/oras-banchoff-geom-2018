//
//  ValueRecord.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.Executor;
import mathbuild.type.Type;
import mathbuild.type.TypeRecord;

public class ValueRecord implements Value {

    private Type type_;
    private java.util.Dictionary fields_ = new java.util.Hashtable(5);
    
    public ValueRecord(String[] names, Executor[] exes) {
        Type[] types = new Type[exes.length];
        for (int i = 0; i < types.length; ++i)
            types[i] = exes[i].type();
        type_ = new TypeRecord(names, types);
        for (int i = 0; i < names.length; ++i)
            fields_.put(names[i], exes[i]);
    }

    
    /**
     * @return the type of this value
     */
    public Type type() {
        return type_;
    }

    /**
     * This equality testing returns true iff the field names of the given value
     * match this record, and the executors for the fields in the value are the 
     * same excutors as this record has.
     * @return whether this value equals the given value
     */
    public boolean equals(Value val) {
        if (!val.type().isType(type_))
            return false;
        ValueRecord v = (ValueRecord) val;
        for (java.util.Enumeration flds = fields_.keys(); flds.hasMoreElements();) {
            String name = (String) flds.nextElement();
            if (v.fields_.get(name) != fields_.get(name))
                return false;
        }
        return true;
    }

    /**
     * Calculates the value of a field in the record.
     * @param name the name of the field.
     * @param runID the ID of the execution run (used for caching, etc).
     * @return the value of a field in the record
     */
    public Value executeField(String name, Object runID) {
        Object e = fields_.get(name);
        if (e == null)
            throw new FieldNotFoundException(name);
        return ((Executor) e).execute(runID);
    }

    
    public String toString() {
        String str = "object:{";
        for (java.util.Enumeration names = fields_.keys();
             names.hasMoreElements();)
            str += names.nextElement() + (names.hasMoreElements() ? ", " : "");
        return str + "}";
    }

}
