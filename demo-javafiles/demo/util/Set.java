package demo.util;
//
//  Set.java
//  Demo
//
//  Created by David Eigen on Thu Jun 27 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

/**
 * Implementation of a basic set.
 * @author deigen
 */
public class Set {

    private java.util.Dictionary set = new java.util.Hashtable();

    /**
     * Creates a new empty set.
     */
    public Set() {
    }

    /**
     * Creates a set containing the objects in the given Enumeration
     */
    public Set(java.util.Enumeration objects) {
        while (objects.hasMoreElements()) {
            Object obj = objects.nextElement();
            set.put(obj,obj);
        }
    }

    /**
     * Puts the given object into this set.
     */
    public void put(Object obj) {
        set.put(obj, obj);
    }

    /**
     * Puts the given object into this set.
     */
    public void add(Object obj) {
        set.put(obj, obj);
    }

    /**
     * Puts the given objects into this set.
     * @param objs an Enumeration containing the objects to add
     */
    public void addObjects(java.util.Enumeration objs) {
        while (objs.hasMoreElements())
            add(objs.nextElement());
    }

    /**
     * Puts the given objects into this set.
     * @param objs an array containing the objects to add
     */
    public void addObjects(Object[] objs) {
        for (int i = 0; i < objs.length; ++i)
            add(objs[i]);
    }

    /**
     * Removes the given object from this set.
     */
    public void remove(Object obj) {
        set.remove(obj);
    }

    /**
     * Removes the given objects from this set.
     */
    public void removeObjs(java.util.Enumeration objs) {
        while (objs.hasMoreElements())
            remove(objs.nextElement());
    }

    /**
     * @return whether this set contains the given object
     */
    public boolean contains(Object obj) {
        return set.get(obj) != null;
    }

    /**
     * @return whether this set contains any of the objects in the given set
     */
    public boolean containsAny(Set set) {
        return containsAny(set.elements());
    }

    /**
     * @return whether this set contains any of the objects in the given enumeration
     */
    public boolean containsAny(java.util.Enumeration enum) {
        while (enum.hasMoreElements())
            if (this.contains(enum.nextElement()))
                return true;
        return false;
    }

    /**
     * @return the object stored in the Set matching the given object, or null if the
     *         object is not in the set.
     */
    public Object get(Object obj) {
        return set.get(obj);
    }

    /**
     * @return the number of elements in this set
     */
    public int size() {
        return set.size();
    }

    /**
     * @return whether the set is empty
     */
    public boolean isEmpty() {
        return set.size() == 0;
    }

    /**
     * @return all the objects in the set
     */
    public java.util.Enumeration elements() {
        return set.elements();
    }

}
