//
//  Map.java
//  Demo
//
//  Created by David Eigen on Wed Aug 14 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

/**
 * Map is the standard functional programming language "map" function.
 * Map takes a vector and a function. It returns a new vector with the
 * function applied to each component of the vector. Map is not
 * differentiable.
 */
public class Map implements SyntaxNodeConstructorBin {

private static final Map instance_ = new Map();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Map inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNMap(left, right);
    }

}


class SNMap extends SyntaxNode {

    private SyntaxNode left_, right_;

    public SNMap(SyntaxNode l, SyntaxNode r) {
        left_ = l; right_ = r;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{left_, right_};
    }

    
    public Executor build(Environment env,
                          ExeStack funcParams,
                          BuildArguments buildArgs) {
        Executor l = left_.build(env, funcParams, buildArgs);
        Executor r = right_.build(env, funcParams, buildArgs);
        Type lt = l.type(); Type rt = r.type();
        if (!rt.isType(Type.FUNCTION))
            throw new BuildException("Right argument of map must be a function.");
        ValueFunction f = ((TypeFunction) rt).val();
        if (lt.isType(Type.VECTOR)) {
            TypeVector vectType = (TypeVector) lt;
            Executor vectExe = l instanceof ExeCache ? l : new ExeCache(l);
            Executor[] exes = new Executor[vectType.numComponents()];
            for (int i = 0; i < exes.length; ++i)
                exes[i] = f.apply(new Executor[]{new ExeVectorComponent(i, vectExe)}, funcParams, buildArgs);
            return SNVector.buildVector(exes, env);
        }
        else if (lt.isType(Type.MATRIX)) {
            throw new BuildException("Can't apply map to a matrix (yet).");
        }
        else if (lt.isType(Type.FUNCTION)) {
            // function combination with map:
            // apply the map to the value we get from the function once it's applied
            ValueFunction lf = ((TypeFunction) lt).val();
            return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{lf},
                                                    new SNMap(new SNAppVal(lf),
                                                              new SNVal(f))));
        }
        else {
            // just apply the function to the argument, since it is not a vector
            return f.apply(new Executor[]{l}, funcParams, buildArgs);
        }
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of map.");
    }

    public SyntaxNode simplify() {
        SyntaxNode l = left_.simplify();
        SyntaxNode r = right_.simplify();
        return new SNMap(l,r);
    }

    public String toString() {
        return "(SN-map " + left_ + " " + right_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        left_.findDependencies(env, deps, ranges);
        right_.findDependencies(env, deps, ranges);
    }


}

