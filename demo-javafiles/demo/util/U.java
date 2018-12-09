//
//  U.java
//  Demo
//
//  Created by David Eigen on Fri Apr 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.util;

/**
 * Miscilaneous utility functions.
 */
public final class U {

    /**
     * Clamps a string so its length is <= the given length.
     * Puts a ... at the end of the string if necessary, so that
     * the total length of the string is <= len. The given length
     * must be at least 3 characters long.
     * @param str the string to clamp
     * @param len the length to truncate the string to
     * @return the string, truncated to the given length
     */
    public static String clampString(String str, int len) {
        if (str.length() <= len) return str;
        if (len < 3) throw new RuntimeException("Length of str truncating must be at least 3.");
        return str.substring(0, len - 3) + "...";
    }

    /**
     * @return the classes of a given array of objects
     */
    public static Class[] getClasses(Object[] objs) {
        Class[] cs = new Class[objs.length];
        for (int i = 0; i < objs.length; ++i)
            cs[i] = objs[i].getClass();
        return cs;
    }

    /**
	 * Copies the source array into the destination array, and
     * returns the destination array. This method can be used for
     * converting array types. For example, to convert from a
     * Object[] to a Plot[], put the following code into the place
     * where you need a plot array:
     * Plot[] plotarray = (Plot[]) arraycopy(myOldObjArray, new Plot[myOldObjArray.length], myOldObjArray.length)
     *
     * @param src the source array
     * @param dst the destination array
     * @param length the length of the array
     * @return the destination array
     */
    public static Object arraycopy(Object src, Object dst, int length) {
        System.arraycopy(src, 0, dst, 0, length);
        return dst;
    }
	
}
