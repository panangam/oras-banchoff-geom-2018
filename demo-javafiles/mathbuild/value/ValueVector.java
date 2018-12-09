//
//  ValueVector.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.Type;
import mathbuild.type.TypeVector;

public class ValueVector implements Value {

    private Value[] vals_;

    /**
     * Creates a ValueVector with the given values as components.
     */
    public ValueVector(Value[] vals) {
        vals_ = vals;
    }

    /**
     * Creates a ValueVector with the given values as components (mapped to ValueScalars).
     */
    public ValueVector(double[] vals) {
        vals_ = new Value[vals.length];
        for (int i = 0; i < vals.length; ++i)
            vals_[i] = new ValueScalar(vals[i]);
    }
    
    /**
     * @return the component values
     */
    public Value[] vals() {
        return vals_;
    }
    
    /**
     * @return the component values
     */
    public Value[] values() {
        return vals_;
    }

    /**
     * Converts this ValueVector into an array of double. If this ValueVector
     * contains non-scalar types as values, this method will break with a class cast exception
     * @return a double[] for this vector
     */
    public double[] doubleVals() {
        double[] doubleVals = new double[vals_.length];
        for (int i = 0; i < doubleVals.length; ++i)
            doubleVals[i] = ((ValueScalar) vals_[i]).number();
        return doubleVals;
    }
    
    /**
     * @param i the component index
     * @return the given component's value
     */
    public Value component(int i) {
        return vals_[i];
    }

    /**
     * @return the number of components in this vector
     */
    public int numComponents() {
        return vals_.length;
    }

    /**
     * @return the number of components in this vector
     */
    public int dimension() {
        return vals_.length;
    }

    public Type type() {
        Type[] types = new Type[vals_.length];
        for (int i = 0; i < types.length; ++i)
            types[i] = vals_[i].type();
        return new TypeVector(types);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueVector))
            return false;
        if (val instanceof ValueVector) {
            Value[] comps = ((ValueVector) val).values();
            if (comps.length != vals_.length)
                return false;
            for (int i = 0; i < vals_.length; ++i) {
                if (!vals_[i].equals(comps[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    public String toString() {
        String str = "(";
        for (int i = 0; i < vals_.length; ++i) {
            str += vals_[i];
            if (i < vals_.length - 1)
                str += " , ";
        }
        str += ")";
        return str;
    }

}
