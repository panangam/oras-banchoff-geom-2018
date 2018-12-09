//
//  ExeRange.java
//  mathbuild
//
//  Created by David Eigen on Sun Aug 11 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

/**
 * An ExeRange is an Executor that returns its current value.
 * It also has a start, end, and resolution, which can be calculated
 * by calling start(.), end(.), and res(.).
 */
public abstract class ExeRange implements Executor, Range {

    protected double value_;

    /**
     * Calcualtes the start value for the range.
     * @param runID the ID of the execution run that start(.) is being called in. Used for caching.
     * @return the start of this range (a ValueScalar)
     */
    public abstract Value start(Object runID);

    /**
     * Calcualtes the end value for the range.
     * @param runID the ID of the execution run that end(.) is being called in. Used for caching.
     * @return the end of this range (a ValueScalar)
     */
    public abstract Value end(Object runID);

    /**
     * Calcualtes the resolution value for the range.
     * @param runID the ID of the execution run that resolution(.) is being called in. Used for caching.
     * @return the resolution of this range (a ValueScalar)
     */
    public abstract Value res(Object runID);

    /**
     * Sets the current value of this ExeRange to the given value.
     */
    public void set(double curr) {
        value_ = curr;
    }

    /**
     * Returns a ValueScalar containing the current value of this range.
     */
    public Value execute(Object runID) {
        return new ValueScalar(value_);
    }

    /**
     * @return the type of any ExeRange is TypeScalar
     */
    public Type type() {
        return MB.TYPE_SCALAR;
    }

}
