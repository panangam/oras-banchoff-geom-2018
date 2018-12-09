//
//  ValueScalar.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.MB;
import mathbuild.type.Type;
import mathbuild.type.TypeScalar;

public class ValueScalar implements Value {

    private double num_;
	
    /**
     * Creates a ValueScalar with the given number.
     */
    public ValueScalar(double num) {
        num_ = num;
    }

    /**
     * @return the number stored in this value
     */
    public double num() {
        return num_;
    }
    
    /**
     * @return the number stored in this value
     */
    public double number() {
        return num_;
    }

    public Type type() {
        return MB.TYPE_SCALAR;
    }

    public boolean equals(Value val) {
        if (val instanceof ValueScalar)
            return ((ValueScalar) val).number() == num_;
        return false;
    }

    public boolean equals(double val) {
        return num_ == val;
    }

    /**
     * @return the boolean value represented by this scalar. All
     *         real numbers are either in the true or false range
     *         according to the semantics of mathbuild. This method
     *         returns true if this number represents true, and false
     *         if this number represents false.
     */
    public boolean booleanValue() {
        return num_ > 0;
    }

    
    public String toString() {
        return new Double(num_) .toString();
    }
    
}
