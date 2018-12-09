//
//  ValueList.java
//  Demo
//
//  Created by David Eigen on Sun Apr 20 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.*;

public class ValueList implements Value {

    private Value[] list_;
    private Type compType_;

    public ValueList(Value[] list, Type compType) {
        list_ = list;
        compType_ = compType;
    }

    public ValueList(java.util.Vector list, Type compType) {
        list_ = new Value[list.size()];
        list.copyInto(list_);
        compType_ = compType;
    }

    public ValueList(java.util.Enumeration list, Type compType) {
        java.util.Vector vec = new java.util.Vector();
        while (list.hasMoreElements())
            vec.addElement(list.nextElement());
        list_ = new Value[vec.size()];
        vec.copyInto(list_);
        compType_ = compType;
    }

    /**
     * Returns the value at the given index.
     * NOTE: in this method, the list is indexed starting from 0.
     */
    public Value valueAt(int i) {
        return list_[i];
    }

    public Value[] values() {
        return list_;
    }

    public int length() {
        return list_.length;
    }

    public Type type() {
        return new TypeList(compType_);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueList))
            return false;
        Value[] l = ((ValueList) val).list_;
        if (l.length != list_.length)
            return false;
        for (int i = 0; i < list_.length; ++i)
            if (!list_[i].equals(l[i]))
                return false;
        return true;
    }

    public String toString() {
        String str = "list{";
        for (int i = 0; i < list_.length; ++i)
            str += list_[i] + (i < list_.length - 1 ? ", " : "");
        return str + "}";
    }


}