
package demo.depend;

import java.util .Dictionary;
import java.util .Hashtable;

import demo.exec.*;
import demo.util.Set;

/**
 * The DependencyManager handles the dependency graph structure.
 * All actions performed on the dependency graph (such as connecting two objects to
 * add a dependency, or to update all objects dependent on a object) should be
 * done with the DependencyManager's static methods.
 *
 * @author deigen
 */
public class DependencyManager extends Object {

    /**
     * Constants for types of updates. There is a type of update for
     * each dependencyUpdateX method in the Dependable interface.
     */
    public static final int DEFINITION_UPDATE = 0, VALUE_UPDATE = 1;

    /**
     * Dependencies that are not allowed. Stored as a map from the
     * dependent object's dependency node to a Set of dependency nodes
     * that the object cannot be dependent on.
     */
    private static java.util.Dictionary disallowedDependencies = new java.util.Hashtable();

    /**
     * Sets of currently pending updates. That is, sets of nodes that have an update
     * waiting on the execution queue. pendingValUpdates contains nodes pending
     * a value update, pendingDefUpdates contains nodes pending a definition update.
     */
    private static Set pendingValUpdates_ = new Set(),
                       pendingDefUpdates_ = new Set();

    /**
     * DependencyManager has all static methods and should never be instantiated.
     * Making an instance of DependencyManager throws a RuntimeException.
     */
    public DependencyManager() {
        throw new RuntimeException("DependencyManager instantiated.");
    }

    /**
     * Whether to allow dependentObject to depend on dependency.
     * By default, any object may depend on anything else. In order
     * to disallow a dependency, this method must be used. Note that
     * if a dependency is disallowed, adding the dependency to the graph
     * simply has no effect (it does not generate an error).
     * @param dependentObject the object that should (not) become dependent on dependency
     * @param dependency the class that should (not) be a dependency of dependentObject
     * @param allow whether to allow the dependency between dependentObject and dependency
     */
    public static void allowDependency(Dependable dependentObject, Dependable dependency,
                                       boolean allow) {
        if (dependentObject == null || dependency == null) return;
        DependencyNode dependentNode = dependentObject.dependencyNode();
        DependencyNode dependencyNode = dependency.dependencyNode();
        if (disallowedDependencies.get(dependentNode) == null)
            disallowedDependencies.put(dependentNode, new Set());
        if (allow)
            ((Set) disallowedDependencies.get(dependentNode)).remove(dependencyNode);
        else
            ((Set) disallowedDependencies.get(dependentNode)).add(dependencyNode);
    }

    /**
     * Makes a dependency, so dependentObject depends on dependency.
     * Also checks for circular dependencies that could have been created. If one
     * is found, a CircularException is thrown and the dependency is not set.
     *
     * @param dependentObject the class that should depend on the parameter dependency
     * @param dependency the class that the dependentObject should depend on
     */
    public static void setDependency( Dependable dependentObject, Dependable dependency )
    throws CircularException {
        if (dependentObject == null || dependency == null) return;
        setDependency( dependentObject.dependencyNode(), dependency.dependencyNode(), true );
    }
    
    /**
     * Makes a dependency, so dependentObject depends on dependency.
     * Also checks for circular dependencies that could have been created. If one
     * is found, a CircularException is thrown and the dependency is not set.
     *
     * @param dependentObject the node that should depend on the parameter dependency
     * @param dependency the node that the dependentObject should depend on
     */
    private static void setDependency( DependencyNode dependentNode, DependencyNode dependency )
    throws CircularException {
        if (dependentNode == null || dependency == null) return;
        setDependency( dependentNode, dependency, true );
    }

    /**
     * Makes a dependency, so dependentObject depends on dependency.
     * If checkForCircularDependencies is true, the method also checks for
     * circular dependencies that could have been created. If one is found,
     * a CircularException is thrown and the dependency is not added.
     *
     * @param dependentObject a Dependable that should depend on the parameter dependency
     * @param dependency the Object that the dependentObject should depend on
     * @param checkForCircularDependencies whether circular dependencies should be checked for
     */
    private static void setDependency( DependencyNode dependentNode, DependencyNode dependency,
                                       boolean checkForCircularDependencies )
    throws CircularException {
        if (dependentNode == null || dependency == null) return;
        Object disallowedSet = disallowedDependencies.get(dependentNode);
        if (disallowedSet != null && ((Set) disallowedSet).contains(dependency)) return;
        dependentNode.addDependency(dependency);
        dependency.addDependentNode(dependentNode);
        if ( checkForCircularDependencies ) {
            if ( existsCircularDependency( dependency ) ) {
                // there is a circular dependency exception, so the dependency shouldn't have been added
                removeDependency( dependentNode, dependency );
                throw new CircularException( "" );
            }
        }
    }

    /**
     * Removes a dependency between a dependent object and one of its dependencies.
     *
     * @param dependentObject the dependent object
     * @param dependency the object that dependentObject is dependent on
     */
    public static void removeDependency( Dependable dependentNode, Dependable dependency ) {
        if (dependentNode == null || dependency == null) return;
        removeDependency(dependentNode.dependencyNode(), dependency.dependencyNode());
    }
        

    /**
     * Removes dependencies between a dependent object and several dependencies.
     *
     * @param dependentObject the dependent object
     * @param dependencies the objects that dependentObject is dependent on
     */
    public static void removeDependencies(Dependable dependentNode,
                                          java.util.Enumeration dependencies) {
        while (dependencies.hasMoreElements())
            removeDependency(dependentNode, (Dependable) dependencies.nextElement());
    }
    
    /**
     * Removes a dependency between a dependent object and one of its dependencies.
     *
     * @param dependentObject the node of the dependent object
     * @param dependency the node that dependentObject is dependent on
     */
    private static void removeDependency( DependencyNode dependentNode, DependencyNode dependency ) {
        if (dependentNode == null || dependency == null) return;
        // remove the dependency from the dependent object
        dependentNode.removeDependency(dependency);
        // remove the dependent object from the dependency
        dependency.removeDependentNode(dependentNode);
    }



    
    /**
     * Removes the dependencies between a dependent object and all the objects that it depends on.
     * 
     * @param dependentObject the object from which to remove all dependencies to objects that
     *			the object depends on.
     */
    public static void removeAllDependencies( Dependable dependentObject ) {
        if (dependentObject == null) return;
        removeAllDependencies(dependentObject.dependencyNode());
    }

    /**
     * Removes the dependencies between a dependent object and all the objects that it depends on.
     *
     * @param dependentObject the node from which to remove all dependencies to objects that
     *			the node depends on.
     */
    private static void removeAllDependencies( DependencyNode dependentObjectNode ) {
        if (dependentObjectNode == null) return;
        java.util.Enumeration dependencyNodes = dependentObjectNode.dependencies();
        while (dependencyNodes.hasMoreElements())
            removeDependency( dependentObjectNode, (DependencyNode) dependencyNodes.nextElement() );
    }

    /**
     * Removes all dependencies (in both the dependent and dependency directions) between this
     * object and anything. In other words, the object is completely removed from the
     * dependency graph structure.
     *
     * @param object the object from which to remove all edges in the dependency graph
     */
    public static void remove( Dependable object ) {
        if (object == null) return;
        remove(object.dependencyNode());
    }
    
    /**
     * Removes all dependencies (in both the dependent and dependency directions) between this
     * object and anything. In other words, the object is completely removed from the 
     * dependency graph structure.
     * 
     * @param objectNode the object from which to remove all edges in the dependency graph
     */
    private static void remove( DependencyNode objectNode ) {
        if (objectNode == null) return;
        java.util.Enumeration dependencyNodes = objectNode.dependencies();
        while ( dependencyNodes.hasMoreElements() )
            removeDependency( objectNode, (DependencyNode) dependencyNodes.nextElement() );
        java.util.Enumeration dependentNodes = objectNode.dependentNodes();
        while ( dependentNodes.hasMoreElements() )
            removeDependency( (DependencyNode) dependentNodes.nextElement(), objectNode );
    }

    /**
     * Makes the dependent object depend only on the objects stored in the given dictionary.
     *
     * @param dependentObject the Dependable whose dependencies should be set.
     * @param dependencies a Dictionary containing all objects dependentObject should depend on
     */
    public static void setDependencies( Dependable dependentObject,
                                        java.util .Dictionary dependencies )
    throws CircularException {
        setDependencies( dependentObject, dependencies .elements() );
    }
    
    /**
     * Makes the dependent object depend only on the objects stored in the given enumeration.
     * If a circular exception is encountered, the dependencies are reset to the old dependencies,
     * leaving the dependency graph unchanged.
     *
     * @param dependentObject the Dependable whose dependencies should be set.
     * @param dependencies an Enumeration containing all Dependable objects dependentObject should depend on
     */
    public static void setDependencies( Dependable dependentObject,
                                        java.util.Enumeration dependencies )
    throws CircularException {
        if (dependentObject == null) return;
        DependencyNode dependentObjectNode = dependentObject.dependencyNode();
        java.util.Enumeration oldDependenciesEnum = dependentObjectNode .dependencies();
        java.util.Vector oldDependencies = new java.util.Vector();
        while (oldDependenciesEnum.hasMoreElements())
            oldDependencies.addElement(oldDependenciesEnum.nextElement());
        removeAllDependencies( dependentObject );
        while ( dependencies .hasMoreElements() )
            setDependency( dependentObjectNode,
                           ((Dependable) dependencies.nextElement()).dependencyNode(),
                           false );
        if (existsCircularDependency(dependentObjectNode)) {
            removeAllDependencies(dependentObject);
            oldDependenciesEnum = oldDependencies.elements();
            while (oldDependenciesEnum.hasMoreElements())
                setDependency(dependentObjectNode,
                              (DependencyNode) oldDependenciesEnum.nextElement(), false);
            throw new CircularException("");
        }
    }

    /**
     * Adds the given dependencies to the dependent object.
     * If there is a CircularException, none of the dependencies are added.
     * @param dependentObject the dependent object
     * @param dependencies an array of Dependable, containing the dependencies
     */
    public static void addDependencies(Dependable dependentObject, Dependable[] dependencies)
    throws CircularException {
        java.util.Vector vec = new java.util.Vector(dependencies.length);
        for (int i = 0; i < dependencies.length; ++i)
            vec.addElement(dependencies[i]);
        addDependencies(dependentObject, vec.elements());
    }
    
    /**
     * Adds the given dependencies to the dependent object.
     * If there is a CircularException, none of the dependencies are added.
     * @param dependentObject the dependent object
     * @param dependencies an Enumeration of Dependable, containing the dependencies
     */
    public static void addDependencies(Dependable dependentObject, java.util.Enumeration dependencies)
    throws CircularException {
        if (dependentObject == null) return;
        DependencyNode dependentObjectNode = dependentObject.dependencyNode();
        java.util.Enumeration oldDependenciesEnum = dependentObjectNode .dependencies();
        java.util.Vector oldDependencies = new java.util.Vector();
        while (oldDependenciesEnum.hasMoreElements())
            oldDependencies.addElement(oldDependenciesEnum.nextElement());
        while ( dependencies .hasMoreElements() )
            setDependency( dependentObjectNode,
                           ((Dependable) dependencies.nextElement()).dependencyNode(),
                           false );
        if (existsCircularDependency(dependentObjectNode)) {
            removeAllDependencies(dependentObjectNode);
            oldDependenciesEnum = oldDependencies.elements();
            while (oldDependenciesEnum.hasMoreElements())
                setDependency(dependentObjectNode,
                              (DependencyNode) oldDependenciesEnum.nextElement(), false);
            throw new CircularException("");
        }
    }
    
    /**
     * Checks whether any objects depend on the given object.
     *
     * @param dependency an Object
     * @return true iff there are any objects that depend on dependency
     */
    public static boolean hasDependentObjects( Dependable dependency ) {
        if (dependency == null) return false;
        return dependency.dependencyNode().hasDependentNodes();
    }

    /**
     * Finds all objects that the given object is dependent on. This is
     * not just direct dependencies: if a path exists in the dependency
     * graph between the given object and some object obj such that
     * the given object is dependent on obj, then obj will be one of the
     * objects returned by this method.
     * @param object the object whose dependencies should be found
     * @return a Set of all objects that the given object depends on
     */
    public static Set getAllDependencies( Dependable object ) {
        Set set = new Set();
        getAllDependenciesImpl(object.dependencyNode(), set);
        return set;
    }

    /**
     * Finds all objects that the given objects are dependent on. This is
     * not just direct dependencies: if a path exists in the dependency
     * graph between a given object and some object obj such that
     * a given object is dependent on obj, then obj will be one of the
     * objects returned by this method.
     * @param objects a set of Dependables whose dependencies should be found
     * @return a Set of all objects that the given object depends on
     */
    public static Set getAllDependencies( Set objects ) {
        Set set = new Set();
        for (java.util.Enumeration objs = objects.elements(); objs.hasMoreElements();)
        getAllDependenciesImpl(((Dependable) objs.nextElement()).dependencyNode(),
                               set);
        return set;
    }

    private static void getAllDependenciesImpl(DependencyNode node, Set set) {
        if (set.contains(node.object()))
            return;
        set.put(node.object());
        for (java.util.Enumeration deps = node.dependencies(); deps.hasMoreElements();)
            getAllDependenciesImpl((DependencyNode) deps.nextElement(), set);
    }

    /**
     * Finds all objects that the are dependent on the given object. This is
     * not just direct dependencies: if a path exists in the dependency
     * graph between the given object and some object obj such that
     * obj is dependent on the given object, then obj will be one of the
     * objects returned by this method.
     * @param object the object whose dependent objects should be found
     * @return a Set of all objects that depend on the given object
     */
    public static Set getDependentObjects(Dependable object) {
        java.util.Dictionary objsDict = new java.util.Hashtable();
        getDependentObjects(object.dependencyNode(), objsDict, 0);
        objsDict.remove(object.dependencyNode());
        Set objs = new Set();
        java.util.Enumeration objsEnum = objsDict.keys();
        while (objsEnum.hasMoreElements())
            objs.add(((DependencyNode) objsEnum.nextElement()).object());
        return objs;
    }

    /**
     * Finds all objects that the are directly dependent on the given object.
     * This is only direct dependencies: the dependent object must be
     * connected to the given object by an edge in the dependency graph.
     * @param object the object whose dependent objects should be found
     * @return a Set of all objects that directly depend on the given object
     */
    public static Set getDirectlyDependentObjects(Dependable object) {
        java.util.Enumeration deps = object.dependencyNode().dependentNodes();
        Set depsSet = new Set();
        while (deps.hasMoreElements())
            depsSet.add(((DependencyNode) deps.nextElement()).object());
        return depsSet;
    }

    
    
    // method used internally to get a list of all objects dependent on a given object
    private static void getDependentObjects( DependencyNode currNode,
                                             java.util.Dictionary encountered, int level ) {
        if ( encountered .get( currNode ) != null ) {
            // update the level that we found the object at
            if ( level > ((PriorityPointer) encountered .get( currNode )) .priority ) {
                ((PriorityPointer) encountered .get( currNode )) .priority = level;
            }
            else {
                // we got to this node w/ higher distance already, so there is no
                // point in traversing from this node again
                return;
            }
        }
        else {
            // put the object into the list of the ones we found
            encountered .put( currNode,
                              new PriorityPointer( currNode, level ) );
        }
        // recur on depdendent objects
        java.util .Enumeration dependentNodes = currNode .dependentNodes();
        while ( dependentNodes .hasMoreElements() ) {
            getDependentObjects( (DependencyNode) dependentNodes .nextElement(),
                                 encountered, level + 1 );
        }
    }
    
    // method used internally to sort an array of objects based on their priority
    private static  void sort( PriorityPointer[] array ) {
        sort( array, 0, array .length - 1 );
    }
    
    // quicksort for the priority sorting
    private static void sort( PriorityPointer[] array, int a, int b ) {
        if ( a >= b ) {
            return ;
        }
        // bit shift makes number effectively random
        int pivotIndex = (a + b) >> 1;
        PriorityPointer pivot = array[pivotIndex];
        double pivotPriority = pivot .priority;
        // swap
        array[pivotIndex] = array[b];
        array[b] = pivot;
        int l = a;
        int r = b - 1;
        while ( l <= r ) {
            while ( (l <= r) && (array[l] .priority <= pivotPriority) ) {
                l++;
            }
            while ( (r >= l) && (array[r] .priority >= pivotPriority) ) {
                r--;
            }
            if ( l < r ) {
                // swap
                PriorityPointer temp = array[l];
                array[l] = array[r];
                array[r] = temp;
            }
        }
        // swap
        PriorityPointer temp = array[l];
        array[l] = array[b];
        array[b] = temp;
        sort( array, a, l - 1 );
        sort( array, l + 1, b );
    }
    
    /**
     * Checks to see if there is a circular dependency containing the given node.
     *
     * @param node the node to check
     * @return true iff there is a circular dependency containing node.
     */
    private static boolean existsCircularDependency( DependencyNode node ) {
        return existsCircularDependency( node, new Hashtable() );
    }
    
    // method used internally to check for circular dependencies
    private static boolean existsCircularDependency( DependencyNode node, Dictionary encountered ) {
        if ( encountered .get( node ) != null )
            return true;
        encountered .put( node, node );
        java.util .Enumeration nextNodes = node .dependentNodes();
        while ( nextNodes .hasMoreElements() ) {
            if ( existsCircularDependency( (DependencyNode) nextNodes .nextElement(),
                                           encountered ) ) {
                return true;
            }
        }
        // the recursion went back up to this node; no cycles can exist with it
        encountered .remove( node );
        return false;
    }

    /**
     * @return all nodes reachable from the given set of nodes
     */
    private static Set findReachableNodes(Set nodes) {
        Set reached = new Set();
        java.util.Enumeration nodesEnum = nodes.elements();
        while (nodesEnum.hasMoreElements())
            findReachableNodesImpl((DependencyNode) nodesEnum.nextElement(), reached);
        return reached;
    }

    private static void findReachableNodesImpl(DependencyNode node,
                                               Set encountered) {
        if (encountered.contains(node))
            return;
        encountered.put(node);
        java.util.Enumeration children = node.dependentNodes();
        while (children.hasMoreElements())
            findReachableNodesImpl((DependencyNode) children.nextElement(), encountered);
        children = node.dependencies();
        while (children.hasMoreElements())
            findReachableNodesImpl((DependencyNode) children.nextElement(), encountered);
    }

    /**
     * Given a set of Dependables, sortByDependency sorts the nodes
     * according to the dependency "chains" between them, and returns
     * the resulting sorted list in a vector. The returned vector stores the objects
     * such that for any object, dependencies of the object appear before the object
     * in the vector.
     *
     * @param objects a Dictionary containing the objects to sort
     * @return a java.util.Vector containing the objects sorted by dependency
     *         nodes not implementing Dependable are at the beginning of the vector.
     */
    public static java.util.Vector sortByDependency( Set objs ) {
        java.util.Vector sortedObjs = new java.util.Vector(objs.size());
        Set nodes = new Set();
        for (java.util.Enumeration objsE = objs.elements(); objsE.hasMoreElements();) {
            Object obj = objsE.nextElement();
            if (obj instanceof Dependable)
                nodes.put(((Dependable) obj).dependencyNode());
            else
                sortedObjs.addElement(obj);
        }
        java.util.Vector sortedNodes = sortNodesByDependency(nodes);
        for (int i = 0; i < sortedNodes.size(); ++i)
            sortedObjs.addElement(((DependencyNode) sortedNodes.elementAt(i)).object());
        return sortedObjs;
    }

    /**
     * Given a dictionary of DependencyNodes, sortNodesByDependency sorts the nodes
     * according to the dependency "chains" between them, and returns
     * the resulting sorted list in a vector. The returned vector contains
     * nodes having no dependencies in the front, and all nodes in the vector have
     * dependencies only in positions before their position in the vector.
     * 
     * @param objects a Dictionary containing the nodes to sort
     * @return a java.util.Vector containing the nodes sorted by by dependency
     */
    private static java.util.Vector sortNodesByDependency( Set nodes ) {
        final Object INDEGREE = new Object();
        java.util.Vector sortedNodes = new java.util.Vector(nodes.size());
        // do a BFS-like traversal on the graph to get nodes in sorted order
        java.util .Vector queue = new java.util .Vector();
        // we need to know all nodes reachable from the subgraph we need to sort, in order
        // to do a global BFS starting from any node with indegree == 0
        Set reachableNodes = findReachableNodes(nodes);
        // first, init the queue to contain nodes with no incoming edge
        java.util.Enumeration allNodesEnum = reachableNodes.elements();
        while ( allNodesEnum.hasMoreElements() ) {
            DependencyNode node = (DependencyNode) allNodesEnum.nextElement();
            node.setDecoration( INDEGREE, new Integer(node.numDependencies()) );
            if ( node.numDependencies() == 0 )
                queue.addElement(node);
        }
        // perform the traversal: add nodes only when indegree == 0
        while ( queue.size() > 0 ) {
            DependencyNode currNode = (DependencyNode) queue.firstElement();
            if ( nodes.contains(currNode) )
                sortedNodes.addElement(currNode);
            queue.removeElementAt(0);
            java.util.Enumeration nextNodes = currNode.dependentNodes();
            while ( nextNodes.hasMoreElements() ) {
                DependencyNode nextNode = (DependencyNode) nextNodes.nextElement();
                int indegree = ((Integer) nextNode.getDecoration(INDEGREE)).intValue() - 1;
                nextNode.setDecoration( INDEGREE, new Integer(indegree) );
                if ( indegree == 0 )
                    queue.addElement(nextNode);
            }
        }
        // clean up the graph
        java.util.Enumeration nodesEnum = reachableNodes.elements();
        while ( nodesEnum.hasMoreElements() ) {
            ((DependencyNode) nodesEnum.nextElement()) .removeDecoration(INDEGREE);
        }
        // return the sorted list of objects
        return sortedNodes;
    }

    /**
     * Updates all objects that depend on a given object with a value update.
     * That is, dependencyUpdateVal(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsDefMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param object the object whose dependent objects should be updated
     */    
    public static void updateDependentObjectsValMT(Dependable object) {
        if (object == null) return;
        updateDependentObjects(object.dependencyNode(), VALUE_UPDATE, true);
    }

    /**
     * Updates all objects that depend on a given object with a definition update.
     * That is, dependencyUpdateDef(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsDefMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param object the object whose dependent objects should be updated
     */
    public static void updateDependentObjectsDefMT(Dependable object) {
        if (object == null) return;
        updateDependentObjects(object.dependencyNode(), DEFINITION_UPDATE, true);
    }

    /**
     * Updates all objects that depend on a given object with a value update.
     * Single threaded: the updates are performed in the current thread.
     * Same as updateDependentObjectsValMT, but everything is done in this thread.
     *
     * @param object the object whose dependent objects should be updated
     */
    public static void updateDependentObjectsValST(Dependable object) {
        if (object == null) return;
        updateDependentObjects(object.dependencyNode(), VALUE_UPDATE, false);
    }

    /**
     * Updates all objects that depend on a given object with a definition update.
     * Single threaded: the updates are performed in the current thread.
     * Same as updateDependentObjectsDefMT, but everything is done in this thread.
     *
     * @param object the object whose dependent objects should be updated
     */
    public static void updateDependentObjectsDefST(Dependable object) {
        if (object == null) return;
        updateDependentObjects(object.dependencyNode(), DEFINITION_UPDATE, false);
    }

    /**
     * Updates all objects that depend on a given node.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     *
     * @param dependencyNode the node whose dependent objects should be updated
     * @param updateType the type of update (either DEFINITION_UPDATE or VALUE_UPDATE)
     * @param multithreaded If true, puts this update on Exec's default execution queue
     *                      and the update is performed in that thread. If false, the
     *                      update is performed in the current thread immediately.
     */
    private synchronized static void updateDependentObjects( DependencyNode dependencyNode, int updateType, boolean multithreaded ) {
        if (dependencyNode == null) return;
        updateDependentObjectsImpl(dependencyNode, 1, updateType, multithreaded, false);
    }

    /**
     * Updates all objects that depend on any of the given objects with a value update.
     * That is, dependencyUpdateVal(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsDefMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param dependencies the objects whose dependent objects should be updated
     */
    public synchronized static void updateDependentObjectsValMT(Dependable[] dependencies) {
        DependencyNode[] nodes = new DependencyNode[dependencies.length];
        for (int i = 0; i < nodes.length; ++i)
            nodes[i] = dependencies[i] == null ? null : dependencies[i].dependencyNode();
        updateDependentObjects(nodes, VALUE_UPDATE, true);
    }

    /**
     * Updates all objects that depend on any of the given objects with a definition update.
     * That is, dependencyUpdateDef(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsDefMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param dependencies the objects whose dependent objects should be updated
     */
    public synchronized static void updateDependentObjectsDefMT(Dependable[] dependencies) {
        DependencyNode[] nodes = new DependencyNode[dependencies.length];
        for (int i = 0; i < nodes.length; ++i)
            nodes[i] = dependencies[i] == null ? null : dependencies[i].dependencyNode();
        updateDependentObjects(nodes, DEFINITION_UPDATE, true);
    }


    /**
     * Updates all objects that depend on any of the given objects with a value update.
     * Single threaded: the updates are performed immediately in the current thread.
     * Besides being performed in the currently running thread, same as 
	 * updateDependentObjectsValMT.
     *
     * @param dependencies the objects whose dependent objects should be updated
     */
    public synchronized static void updateDependentObjectsValST(Dependable[] dependencies) {
        DependencyNode[] nodes = new DependencyNode[dependencies.length];
        for (int i = 0; i < nodes.length; ++i)
            nodes[i] = dependencies[i] == null ? null : dependencies[i].dependencyNode();
        updateDependentObjects(nodes, VALUE_UPDATE, false);
    }

    /**
     * Updates all objects that depend on any of the given objects with a definition update.
     * Single threaded: the updates are performed immediately in the current thread.
     * Besides being performed in the currently running thread, same as 
	 * updateDependentObjectsDefMT.
     *
     * @param dependencies the objects whose dependent objects should be updated
     */
    public synchronized static void updateDependentObjectsDefST(Dependable[] dependencies) {
        DependencyNode[] nodes = new DependencyNode[dependencies.length];
        for (int i = 0; i < nodes.length; ++i)
            nodes[i] = dependencies[i] == null ? null : dependencies[i].dependencyNode();
        updateDependentObjects(nodes, DEFINITION_UPDATE, false);
    }

    /**
     * Updates all objects that depend on any of the given objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     *
     * @param dependencies the objects whose dependent objects should be updated
     * @param the type of update (either DEFINITION_UPDATE or VALUE_UPDATE)
     * @param multithreaded If true, puts this update on Exec's default execution queue
     *                      and the update is performed in that thread. If false, the
     *                      update is performed in the current thread immediately.
     */
    private synchronized static void updateDependentObjects(DependencyNode[] dependencies, int updateType, boolean multithreaded) {
        DependencyNode phantom = new PhantomDependableObject().dependencyNode();
        for (int i = 0; i < dependencies.length; ++i)
            setDependency(dependencies[i], phantom);
        updateDependentObjectsImpl(phantom, 2, updateType, multithreaded, true);
    }

    /**
     * Updates all objects that depend on any of the given objects with a value update.
     * That is, dependencyUpdateVal(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsValMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param dependencies the objects whose dependent objects should be updated
     *                     dependencies is a java.util.Enumeration of Dependable
     */
    public synchronized static void updateDependentObjectsValMT(java.util.Enumeration dependencies) {
        DependencyNode phantom = new PhantomDependableObject().dependencyNode();
        while (dependencies.hasMoreElements())
            setDependency(((Dependable) dependencies.nextElement()).dependencyNode(), phantom);
        updateDependentObjectsImpl(phantom, 2, VALUE_UPDATE, true, true);
    }

    /**
     * Updates all objects that depend on any of the given objects with a definition update.
     * That is, dependencyUpdateDef(.) will be called on all dependent objects.
     * This is done in such a way that each object that needs to be updated is
     * updated exactly once, and is not updated until all the objects that
     * it depends on have already been updated (if they needed to be).
     * Uses the default execution thread, Exec, for performing these updates.
     * This means that the call to updateDependentObjectsDefMT returns before the
     * updates are actually performed. "MT" is for multi-threaded.
     *
     * @param dependencies the objects whose dependent objects should be updated
     *                     dependencies is a java.util.Enumeration of Dependable
     */
    public synchronized static void updateDependentObjectsDefMT(java.util.Enumeration dependencies) {
        DependencyNode phantom = new PhantomDependableObject().dependencyNode();
        while (dependencies.hasMoreElements())
            setDependency(((Dependable) dependencies.nextElement()).dependencyNode(), phantom);
        updateDependentObjectsImpl(phantom, 2, DEFINITION_UPDATE, true, true);
    }

    /**
     * Same as updateDependentObjectsValMT, but is run in the current thread.
     *
     * @param dependencies the objects whose dependent objects should be updated
     *                     dependencies is a java.util.Enumeration of Dependable
     */
    public synchronized static void updateDependentObjectsValST(java.util.Enumeration dependencies) {
        DependencyNode phantom = new PhantomDependableObject().dependencyNode();
        while (dependencies.hasMoreElements())
            setDependency(((Dependable) dependencies.nextElement()).dependencyNode(), phantom);
        updateDependentObjectsImpl(phantom, 2, VALUE_UPDATE, false, true);
    }

    /**
     * Same as updateDependentObjectsDefMT, but is run in the current thread.
     *
     * @param dependencies the objects whose dependent objects should be updated
     *                     dependencies is a java.util.Enumeration of Dependable
     */
    public synchronized static void updateDependentObjectsDefST(java.util.Enumeration dependencies) {
        DependencyNode phantom = new PhantomDependableObject().dependencyNode();
        while (dependencies.hasMoreElements())
            setDependency(((Dependable) dependencies.nextElement()).dependencyNode(), phantom);
        updateDependentObjectsImpl(phantom, 2, DEFINITION_UPDATE, false, true);
    }

    /**
     * Updates all dependent objects that are a distance updateLevel or more away from the node.
     * If updateLevel is 0, the dependency is updated, as well as all objects dependent on it.
     * If updateLevel is 1, the dependency is not updated, but all of its dependent objects are.
     * If updateLevel is 2, objects dependent on objects dependent on the dependency are updated, as
     *    well as all the objects dependent on those objects.
     * If the object of dependencyNode is a PhantomDependableObject, the dependencyNode is automatically
     * removed after a succssful dependency update.
     *
     * This method is public for internal user-interface code. This method should not be called outside
     * of the gependency graph framework classes.
     *
     * @param dependencyNode the node whose dependent objects should be updated
     * @param updateLevel see description above
     * @param updateType either VALUE_UPDATE or DEFINITION_UPDATE
     * @param multithreaded If true, the update is performed in the default Exec thread/queue.
                            If false, the update is performed immediately in this thread.
     * @param phantom whether the given dependencyNode is a phantom node and should be removed after the update.
     */
    public synchronized static void updateDependentObjectsImpl(final DependencyNode dependencyNode,
                                                               final int updateLevel,
                                                               final int updateType,
                                                               final boolean multithreaded,
                                                               final boolean phantom) {
        if (multithreaded) {
            // make sure that the update is not already on the execution queue
            // (if it is, we'll do the update when we get to it)
            final Set pending = updateType == VALUE_UPDATE ? pendingValUpdates_
                         : updateType == DEFINITION_UPDATE ? pendingDefUpdates_
                                                           : null;
            if (pending.contains(dependencyNode))
                return;
            pending.add(dependencyNode);
            // put the update on the execution queue
            Exec.run(new ExecCallback(){
                public void invoke() {
					pending.remove(dependencyNode);
                    updateDependentObjectsImplImpl(dependencyNode, updateLevel, updateType, phantom);
                }
                public void cleanup(boolean completed) {
                    if (phantom) remove(dependencyNode);
                }
            });
        }
        else {
            // single threaded
            updateDependentObjectsImplImpl(dependencyNode, updateLevel, updateType, phantom);
            if (phantom)
                remove(dependencyNode);
        }
    }

    private synchronized static void updateDependentObjectsImplImpl(final DependencyNode dependencyNode,
                                                                    final int updateLevel,
                                                                    final int updateType,
                                                                    final boolean phantom) {
        java.util.Dictionary nodes = new java.util.Hashtable();
        getDependentObjects( dependencyNode, nodes, 0 );
        java.util.Enumeration nodesEnum = nodes.elements();
        PriorityPointer[] array = new PriorityPointer[nodes.size()];
        Set updatingObjectsSet = new Set();
        updatingObjectsSet.put(dependencyNode);
        for ( int i = 0; i < array.length; i++ ) {
            PriorityPointer curr = (PriorityPointer) nodesEnum.nextElement();
            array[i] = curr;
            updatingObjectsSet.put( curr.node.object() );
        }
        sort( array );
        // find when to start updating: we need to update all objects with distance
        // (that is, priority pointer priority) at least updateLevel
        int start;
        for (start = 0; start < array.length; ++start)
            if (array[start].priority >= updateLevel) break;
        try {
            if (updateType == DEFINITION_UPDATE) {
                for ( int i = start; i < array.length; i++ )
                    array[i].node.object().dependencyUpdateDef( updatingObjectsSet );
            }
            else if (updateType == VALUE_UPDATE) {
                for ( int i = start; i < array.length; i++ )
                    array[i].node.object().dependencyUpdateVal( updatingObjectsSet );
            }
            if (dependencyNode.object() instanceof PhantomDependableObject)
                remove(dependencyNode);
            DependencyUpdateErrorWindow.updateSucceeded(dependencyNode, updateLevel, updateType);
        }
        catch (demo.DemoRuntimeException ex) {
            new DependencyUpdateErrorWindow(ex, dependencyNode, updateLevel, updateType, phantom);
        }
        catch (mathbuild.MathbuildRuntimeException ex) {
            new DependencyUpdateErrorWindow(ex, dependencyNode, updateLevel, updateType, phantom);
        }
    }

    
}


class PhantomDependableObject implements Dependable {
    private DependencyNode myDependencyNode_ = new PhantomDependencyNode(this);
    public void dependencyUpdateDef(Set updatingObjs) {}
    public void dependencyUpdateVal(Set updatingObjs) {}
    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }
    private class PhantomDependencyNode extends DependencyNode {
        public PhantomDependencyNode(Dependable obj){ super(obj); }
        public int hashCode() {
            int sum = 0;
            for (java.util.Enumeration deps = dependencies();
                 deps.hasMoreElements();)
                sum += deps.nextElement().hashCode();
            for (java.util.Enumeration deps = dependentNodes();
                 deps.hasMoreElements();)
                sum += deps.nextElement().hashCode();
            return sum;
        }
        public boolean equals(Object obj) {
            if ( !(obj instanceof PhantomDependencyNode) )
                return false;
            PhantomDependencyNode n = (PhantomDependencyNode) obj;
            if (n.dependencies_.size() != dependencies_.size() ||
                n.dependentNodes_.size() != dependentNodes_.size())
                return false;
            for (java.util.Enumeration deps = n.dependencies_.elements();
                 deps.hasMoreElements();)
                if (dependencies_.get(deps.nextElement()) == null)
                    return false;
            for (java.util.Enumeration deps = n.dependentNodes_.elements();
                 deps.hasMoreElements();)
                if (dependentNodes_.get(deps.nextElement()) == null)
                    return false;
            return true;
        }
    }
}


/**
 * Used to sort based on a priority that makes sense in context.
 * The context in which this is used so far is to sort based on
 * the dependencies of objects.
 * The PriorityPointer class simply stores an object and some int
 * priority assigned to the object.
 */
class PriorityPointer extends Object{

    /**
     * the Object being stored ("pointed" to)
     */
    public  DependencyNode node;
    
    /**
     * the priority of the object
     */
    public  int priority;
    

    /**
     * Creates a new PriorityPointer storing the given node 
     * and the given priority.
     *
     * @param node the node to store
     * @param priority an int representing the priority of the object
     */
    public  PriorityPointer( DependencyNode node, int priority ) {
        super();

            this .node = node;
            this .priority = priority;
        }
        

}


