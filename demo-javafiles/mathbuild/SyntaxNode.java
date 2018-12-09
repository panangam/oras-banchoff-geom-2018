//
//  SyntaxNode.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.value.Value;
import mathbuild.impl.*;

/**
 * A SyntaxNode tree is made from an expression string by the Parser. A SyntaxNode
 * represents a piece of syntax: for example, "a + b * c" is represented as the
 * tree (SN-add (SN-id a) (SN-mult (SN-id b) (SN-id c)))
 */
public abstract class SyntaxNode {


    /**
     * The build(.) function produces an Executor object for the syntax tree. It is basically
     * an interpreter for the tree, only it produces an Executor instead of a value. A 
     * SyntaxNode should not be built if it has not yet been type checked.
     * @param env the current environment
     * @param applying whether to automatically apply functions
     * @param funcParams stack of current parameters
     * @return an Executor object for this syntax tree
     */
    public abstract Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs);

    /**
     * Takes the derivative of this node with respect to wrtvar.
     * All identifiers that do not immediately map to a parameter in the current environemnt
     * are given a derivative of Zero. The derivative of the parameter with respect to itself
     * is given as dwrtvar.
     * @param wrtvar the variable to take the derivative with respect to.
     * @param env the current derivative environment
     * @param funcParams stack of parameters that the derivative is going to be applied to.
     *        Top of stack is params the deriv will be applied to. If the derivative is a
     *        function that returns a function, second from top of stack are the params for
     *        that function, and so on.
     * @return the derivative of this node
     */
    public abstract SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                          Environment env, ExeStack funcParams);

    /**
     * Does some simple simplification on this syntax tree, such as
     * getting rid of adding zero or wrapping Zero in a function.
     * @return the simplifiied tree
     */
    public abstract SyntaxNode simplify();

    
    /**
     * String representation of SyntaxNodes are of the form "(SN-<type> <data>)" where
     * <type> is the type of node (eg, "add") and <data> is a string of children and/or
     * other data contained by the node
     * @return the String representation of this SyntaxNode
     */
    public abstract String toString();


    /**
     * Finds any objects implementing Dependable that this expression tree uses.
     * The tree "uses" a Dependable object if some identifier is mapped to the object,
     * or some value node directly returns the object.
     * Also finds Executors implementing Range that this expression tree is dependent
     * on as ranges. This is when the range is not embedded in a _min, _max, or _res
     * node. If a range appears anywhere else, it is put into the ranges set.
     * @param env the current environment for dependency finding
     * @param deps the set to put the dependencies into
     * @param ragnes the set to put the ranges into
     */
    public abstract void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges);

    /**
     * Returns the SyntaxNode children of this node. Can be used to
     * recur through the syntax tree.
     * @return the children of this node
     */
    public abstract SyntaxNode[] children();

    /**
     * @return whether this SyntaxNode is a identically a constant value.  Returns false 
     *         by default. Subclasses can (and in many cases should) override this method.
     * Also: if isValue() is true, then this node can be successfully built with an empty environment
     */
    public boolean isValue() {
        return false;
    }
    
    
    /**
     * @return whether this SyntaxNode is identically the given value. 
     *         Returns false by default. Subclasses can (and in many cases should)
     *         override this method.
     */
    public boolean isValue(int val) {
        return false;
    }

    /**
     * @return whether this SyntaxNode is identically the given value.
     *         Returns false by default. Subclasses can (and in many cases should)
     *         override this method.
     */
    public boolean isValue(Value val) {
        return false;
    }

    /**
     * @return whether this SyntaxNode is identically zero (any type).
     *         Returns false by default. Subclasses can (and in many cases should)
     *         override this method.
     */
    public boolean isZero() {
        return false;
    }

    /**
     * Counts the number of nodes in this syntax tree.
     * Does not include nodes in the body of functions stored as values.
     */
    public int dbgNodeCount() {
        int count = 1;
        SyntaxNode[] chil = children();
        for (int i = 0; i < chil.length; ++i)
            count += chil[i].dbgNodeCount();
        return count;
    }

    
    
}
