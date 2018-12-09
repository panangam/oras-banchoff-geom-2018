package demo.depend;

import demo.util.Set;

/**
 * All classes that either depend on other objects or could have dependent objects must
 * implement Dependable, and contain a DependencyNode. The DependencyManager class
 * handles the dependency graph structure, and uses the Dependable interface to link the
 * graph structure with actual objects in the program. To make an object implement
 * Dependable, the object should contain an instance of DependencyNode, and return it
 * in the dependencyNode() method. To put this in a class, you can copy the code fragmet
 * at the end of Dependable.java into the class.
 *
 * @author deigen
 */
public interface Dependable {

    /**
     * This method is called by the Dependency Manager when the
     * Dependable object should be updated.
     * This method is called when the actual definition or dependencies of 
     * an object change. Expressions need to be rebuilt.
     *
     * @param updatingObjects a set of all the objects that have been
     *			 or will be updated
     */
    public void dependencyUpdateDef(Set updatingObjects);

    /**
     * This method is called by the Dependency Manager when the
     * Dependable object should be updated.
     * This method is called when only a value changes, and the type
     * of the value does not change. An example of this kind of update
     * is when the value of a variable changes, but the definition of
     * it does not. Expressions do not have to be rebuilt.
     *
     * @param updatingObjects a set of all the objects that have been
     *			 or will be updated
     */
    public void dependencyUpdateVal(Set updatingObjects);

    /**
     * @return the depdenency graph node for this class
     */
    public DependencyNode dependencyNode();

}

/* COPY THIS CODE INTO CLASSES IMPLEMENTING Dependable

// *** IMPLEMENTATION FOR DEPENDABLE *** //
private DependencyNode __myDependencyNode__ = new DependencyNode(this);
public DependencyNode dependencyNode() { return __myDependencyNode__; }




*/

