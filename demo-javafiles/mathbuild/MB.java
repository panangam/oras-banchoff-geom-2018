//
//  MB.java
//  Demo
//
//  Created by David Eigen on Mon Feb 03 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.value.*;
import mathbuild.type.*;
import mathbuild.impl.*;

/**
 * Basic utility functions for mathbuild.
 * MB is for "mathbuild"
 */
public final class MB {

    // objects needed for small optimizations
    public static final Object ONETIME_EXEC_ID   = new Object();
    public static final ValueScalar ZERO_SCALAR  = new ValueScalar(0);
    public static final ValueVector ZERO_VECTOR2 = new ValueVector(
                                                      new Value[]{ZERO_SCALAR, ZERO_SCALAR});
    public static final ValueVector ZERO_VECTOR3 = new ValueVector(
                                                      new Value[]{ZERO_SCALAR,ZERO_SCALAR,ZERO_SCALAR});
    public static final TypeScalar TYPE_SCALAR = new TypeScalar();

    /**
     * Builds a syntax node.
     */
    public static Executor build(SyntaxNode sn) {
        return sn.build(Environment.EMPTY,
                        ExeStack.EMPTY,
                        BuildArguments.EMPTY);
    }

    /**
     * Builds a syntax node, with a given base environment.
     */
    public static Executor build(SyntaxNode sn, Environment env) {
        return sn.build(env,
                        ExeStack.EMPTY,
                        BuildArguments.EMPTY);
    }

    
    /**
     * Executes an executor object.
     */
    public static Value exec(Executor exe) {
        return exe.execute(new Object());
    }

    
    /**
     * Builds and executes a syntax node
     */
    public static Value exec(SyntaxNode sn) {
        return sn.build(Environment.EMPTY,
                        ExeStack.EMPTY,
                        BuildArguments.EMPTY)
            .execute(ONETIME_EXEC_ID);
    }


    /**
     * Builds and executes a syntax node, in the given base environment.
     */
    public static Value exec(SyntaxNode sn, Environment env) {
        return sn.build(env,
                        ExeStack.EMPTY,
                        BuildArguments.EMPTY)
            .execute(ONETIME_EXEC_ID);
    }


    /**
     * Makes a "hook" executor. The value of thie executor is a function
     * that takes parameters of the given types and applies the given operator to them.
     * This method should be used where possible, and makeHook(Operator, int)
     * should be avoided, in case a type system is added.
     */
    public static Executor makeHook(Type[] paramTypes, Operator op) {
        int numArgs = paramTypes.length;
        if (numArgs == 0)
            return makeHook(op);
        ParameterID[] params = new ParameterID[numArgs];
        SyntaxNode[] argSNs = new SyntaxNode[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            params[i] = new ParameterID(i);
            argSNs[i] = new SNParam(params[i]);
        }
        SyntaxNode body = new SNAppOp(op, argSNs, paramTypes);
        return new ExeVal(new ValueFunctionImpl(params, body, Environment.EMPTY));
    }

    /**
     * Makes a "hook" executor. The value of the executor is a function
     * that takes the given number of parameters and applies the operator to them.
     */
    public static Executor makeHook(int numArgs, Operator op) {
        if (numArgs == 0)
            return makeHook(op);
        ParameterID[] params = new ParameterID[numArgs];
        SyntaxNode[] argSNs = new SyntaxNode[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            params[i] = new ParameterID(i);
            argSNs[i] = new SNParam(params[i]);
        }
        SyntaxNode body = new SNAppOp(op, argSNs);
        return new ExeVal(new ValueFunctionImpl(params, body, Environment.EMPTY));
    }

    /**
     * Makes a "hook" executor. The value of the executor is the value returned
     * by the operator -- no arguments are applied to it.
     */
    public static Executor makeHook(Operator op) {
        return new ExeNAryOp(new Executor[0], op);
    }


    /**
     * Creates a Value for zero consisting of all zeros. The type of the returned value
     * is equivalent to the given type.
     * @param type the type of the zero to make
     * @return a value representing zero whose type is the given type
     */
    public static Value zero(Type type) {
        if (type.isType(Type.SCALAR))
            return ZERO_SCALAR;
            //            return new ValueScalar(0); // above may (prob is) more optimized
        if (type.isType(Type.VECTOR)) {
            TypeVector t = (TypeVector) type;
            int n = t.numComponents();
            if (n == 2 && t.componentType(0).isType(Type.SCALAR)
                       && t.componentType(1).isType(Type.SCALAR))
                return ZERO_VECTOR2;
            if (n == 3 && t.componentType(0).isType(Type.SCALAR)
                       && t.componentType(1).isType(Type.SCALAR)
                       && t.componentType(2).isType(Type.SCALAR))
                return ZERO_VECTOR3;
            Value[] zeros = new Value[n];
            for (int i = 0; i < n; ++i)
                zeros[i] = zero(t.componentType(i));
            return new ValueVector(zeros);
        }
        if (type.isType(Type.MATRIX)) {
            TypeMatrix t = (TypeMatrix) type;
            Value[][] zeros = new Value[t.numRows()][t.numCols()];
            for (int i = 0; i < zeros.length; ++i)
                for (int j = 0; j < zeros[i].length; ++j)
                    zeros[i][j] = zero(t.componentType(i,j));
            return new ValueMatrix(zeros);
        }
        if (type.isType(Type.FUNCTION))
            return new ValueFunctionZero(((TypeFunction) type).val());
        throw new RuntimeException("Unknown type: " + type);
    }


    /**
     * @return the base environment normally used for mathbuild.
     */
    public static Environment baseEnvironment() {
        return Init.baseEnvironment();
    }

    
}
