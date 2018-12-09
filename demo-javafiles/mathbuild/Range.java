//
//  Range.java
//  Demo
//
//  Created by David Eigen on Mon Aug 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.value.*;


/**
 * A Range specifies a range on the real number line. Examples of Ranges include
 * intervals, variables, and integral variables.
 */
public interface Range {

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
    
    
}
