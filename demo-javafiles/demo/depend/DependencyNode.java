//
//  DependencyNode.java
//  Demo
//
//  Created by David Eigen on Wed Jun 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.depend;

/**
 * A node in the dependency graph.
 *
 * @author deigen
 */
public class DependencyNode extends Object {

    /**
     * The Object stored in this node.
     */
    private  Dependable object_;

    /**
     * A Dictionary containing a list of all dependencies. That is, all nodes that
     * this node is dependent on.
     */
    protected java.util .Dictionary dependencies_ = new java.util .Hashtable();

    /**
     * A Dictionary containing a list of all dependent objects. That is, all nodes
     * that are dependent on this node.
     */
    protected java.util .Dictionary dependentNodes_ = new java.util .Hashtable();

    /**
     * a table that can be used to store decorations in algorithms
     */
    private java.util .Hashtable decorations_ = new java.util .Hashtable( 3 );

    /**
     * Creates a new DependencyNode with the given Object as its object.
     *
     * @param obj the object to store in this node.
     */
    public  DependencyNode( Dependable obj ) {
        super();

        this .object_ = obj;
    }

    /**
     * @return the object associated with this dependency node
     */
    public Dependable object() {
        return object_;
    }

    /**
     * @return the nodes that this node is dependent on
     */
    public java.util.Enumeration dependencies() {
        return dependencies_.elements();
    }

    /**
     * @return the nodes that are dependent on this node
     */
    public java.util.Enumeration dependentNodes() {
        return dependentNodes_.elements();
    }

    /**
     * @return whether this node is dependent on x
     */
    public boolean hasDependency(DependencyNode x) {
        return dependencies_.get(x) != null;
    }

    /**
     * @return whether x is dependent on this node
     */
    public boolean hasDependentNode(DependencyNode x) {
        return dependentNodes_.get(x) != null;
    }

    /**
     * @return whether this node is dependent on any nodes
     */
    public boolean hasDependencies() {
        return dependencies_.size() > 0;
    }

    /**
     * @return whether any nodes depend on this node
     */
    public boolean hasDependentNodes() {
        return dependentNodes_.size() > 0;
    }

    /**
     * Adds x to the set of nodes that this node is dependent on
     */
    public void addDependency(DependencyNode x) {
        if (dependencies_.get(x) == null)
            dependencies_.put(x,x);
    }

    /**
     * Adds x to the set of nodes dependent on this node
     */
    public void addDependentNode(DependencyNode x) {
        if (dependentNodes_.get(x) == null)
            dependentNodes_.put(x,x);
    }

    /**
     * Removes x from the set of nodes that this node is dependent on
     */
    public void removeDependency(DependencyNode x) {
        dependencies_.remove(x);
    }

    /**
     * Removes x from the set of nodes dependent on this node
     */
    public void removeDependentNode(DependencyNode x) {
        dependentNodes_.remove(x);
    }

    /**
     * @return the number of nodes that this node is dependent on
     */
    public int numDependencies() {
        return dependencies_.size();
    }

    /**
     * @return the number of nodes dependent on this node
     */
    public int numDependentNodes() {
        return dependentNodes_.size();
    }

    /**
     * sets the value of a decoration, or adds the decoration if it does not yet exist
     * @param key the key for the decoration
     * @param value the value for the decoration
     */
    public void setDecoration(Object key, Object value) {
        decorations_.put(key,value);
    }

    /**
     * Gets the value of a decoration.
     * @param key the key of the decoration
     * @return the value of the given decoration
     */
    public Object getDecoration(Object key) {
        return decorations_.get(key);
    }

    /**
     * Removes a decoration.
     * @param key the key of the decoration to remove
     */
    public void removeDecoration(Object key) {
        decorations_.remove(key);
    }

    /**
     * Checks whether a decoration has been put into this node.
     * @param key the decoration to check
     * @return whether the given decoration exists
     */
    public boolean hasDecoration(Object key) {
        return decorations_.get(key) != null;
    }
}