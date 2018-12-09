//
//  SNVectorComponent.java
//  mathbuild
//
//  Created by David Eigen on Tue Jul 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class SNVectorComponent extends SyntaxNode {

    private SyntaxNode vector_;
    private int compNum_;

    /**
     * creates a syntax node for vector component
     * @param compNum the component number
     * @param vector the SN for the vector
     */
    public SNVectorComponent(int compNum, SyntaxNode vector) {
        compNum_ = compNum;
        vector_ = vector;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{vector_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        return buildComponent(compNum_, vector_.build(env, funcParams, buildArgs), env);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNVectorComponent(compNum_, vector_.derivative(wrtvar, dwrtvar, env, funcParams));
    }

    public SyntaxNode simplify() {
        SyntaxNode vector = vector_.simplify();
        if (vector.isValue()) {
            Value val = MB.exec(vector);
            Type t = val.type();
            if (!t.isType(Type.VECTOR))
                throw new BuildException("Can only take vector component of a vector.");
            if (compNum_ >= ((TypeVector) t).numComponents())
                throw new BuildException("Component number larger than number of components in vector.");
            return new SNVal(((ValueVector) val).component(compNum_));
        }
        return new SNVectorComponent(compNum_, vector);
    }    
    

    /**
     * Builds an executor object for getting a component of a vector.
     * @param compnum the component number
     * @param exe the vector executor (usually an ExeCache wrapping another Executor)
     * @param env the current environment
     * @return an executor object that takes the component of the given vector (or function)
     */
    public static Executor buildComponent(int compnum, Executor exe, Environment env) {
        Type exeType = exe.type();
        if (exeType.isType(Type.VECTOR)) {
            // this is not a function type
            if (compnum >= ((TypeVector) exeType).numComponents())
                throw new BuildException("Component number larger than number of components in vector.");
            return new ExeVectorComponent(compnum, exe);
        }
        if (exeType.isType(Type.FUNCTION)) {
            // The vector is a function, so the component should be a function
            ValueFunction func = ((TypeFunction) exeType).val();
            return new ExeVal(new ValueFunctionImpl(func,
                                                    new SNVectorComponent(compnum, func.body()),
                                                    func.environment()));
        }
        throw new BuildException("Vector component can only be applied to a vector or vector-valued function.");
    }    



    public String toString() {
        return "(SN-comp " + compNum_ + " " + vector_ + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        vector_.findDependencies(env, deps, ranges);
    }

}
