package demo.util;
//
//  Sorter.java
//  Demo
//
//  Created by David Eigen on Tue Jul 02 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

public class Sorter {

    private static java.util.Random randomizer = new java.util.Random();

    /**
     * Sorts the objects using quicksort.
     * @param objs the objects to sort
     * @param comp the comparator for the objects
     */
    public static void sort(Object[] objs, SortComparator comp) {
        randomizer.setSeed(System.currentTimeMillis());
        sort( 0, objs.length - 1, objs, comp );
    }

    // quicksort method to perform the sort
    private static void sort( int a, int b, Object[] objs, SortComparator comp ) {
        if ( a >= b )
            return ;
        int pivotIndex = (a + b) >> 1;
     //   int pivotIndex = (randomizer.nextInt() % (b - a)) + a;
        Object pivot = objs[pivotIndex];
        // swap
        objs[pivotIndex] = objs[b];
        objs[b] = pivot;
        int l = a;
        int r = b - 1;
        while ( l <= r ) {
            while ( (l <= r) && comp.isLessThanOrEqualTo(objs[l], pivot) )
                l++;
            while ( (r >= l) && comp.isLessThanOrEqualTo(pivot, objs[r]) )
                r--;
            if ( l < r ) {
                // swap values
                Object temp = objs[l];
                objs[l] = objs[r];
                objs[r] = temp;
            }
        }
        // swap values
        Object temp = objs[l];
        objs[l] = objs[b];
        objs[b] = temp;
        // recur
        sort( a, l - 1, objs, comp );
        sort( l + 1, b, objs, comp );
    }
    
    
}
