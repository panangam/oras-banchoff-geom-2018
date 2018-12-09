//
//  ValueFunctionOne.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.*;
import mathbuild.*;
import mathbuild.impl.*;

public class ValueFunctionOne implements ValueFunction {

    private ValueFunction func_;

    /**
     * Constructs a new one function for the given function.
     * The one function behaves exactly like the given function, only
     * it always returns a one value when it is applied or its derivative
     * is taken.
     * For example, if apply(.) is called on this ValueFunctionOne, then
     * the affect will be as if apply(.) was called on the function func
     * (passed as a parameter to this constructor). However, instead of
     * returning the Executor func's apply(.) would return, this class will
     * return an Executor for a one value for the type corresponding to the
     * type of the executor.
     * The one value of a scalar is 1. The one value of a vector is the
     * identity matrix, represented as a vector of vectors (column-major).
     * In general, a type t(t1,...,tn) (that is, base-type t with component
     * types (t1,...,tn)) has a "one" type of
     * one(t) = t( t(one(t1),zero(t2),...,zero(tn)), ..., t(zero(t1),...,zero(tn-1),one(tn)) )
     *
     * @param func the function to wrap as a zero function
     */
    public ValueFunctionOne(ValueFunction func, SNMakeOne callback) {
        throw new RuntimeException("shouldn't be here");
//        while (func instanceof ValueFunctionOne)
//            func = ((ValueFunctionOne) func).func_;
//        func_ = func;
    }

    /**
     * @return the number of arguments
     */
    public int numArgs() {
        return func_.numArgs();
    }


    public ParameterID[] parameters() {
        return func_.parameters();
    }

    public ParameterID parameter(int i) {
        return func_.parameter(i);
    }

    /**
     * @return the body of the function
     */
    public SyntaxNode body() {
        throw new RuntimeException("shouldn't be here");
        //        return new SNMakeOne();
    }

    /**
     * @return the creation environment of the function
     */
    public Environment environment() {
        return func_.environment();
    }

    /**
     * @return whether this ValueFunction is closing over an environment
     */
    public boolean isClosure() {
        return func_.isClosure();
    }


    /**
     * Does paramter conversion for vector -> many args and many args -> vector
     * @param a list of arguments that will be applied to the function
     * @return a list of arguments that can be applied to the function
     */
    public Executor[] convertParams(Executor[] paramExes) {
        return func_.convertParams(paramExes);
    }


    public static Value makeOne(Type t) {
        if (t.isType(Type.FUNCTION)) {
            throw new RuntimeException("Can't make One for Function Type: shouldn't be here");
        }
        return makeOneImpl(t, t, new IntPtr());
    }
    

    private static class IntPtr {
        int num = 0;
    }

    private static Value makeOneImpl(Type type, Type currType, IntPtr currScalar) {
            if (currType.isType(Type.SCALAR)) {
                return makeOneImpl(type, currScalar.num++, new IntPtr());
            }
            if (currType.isType(Type.VECTOR)) {
                TypeVector t = (TypeVector) currType;
                Value[] vals = new Value[t.numComponents()];
                for (int i = 0; i < vals.length; ++i)
                    vals[i] = makeOneImpl(type, t.componentType(i), currScalar);
                return new ValueVector(vals);
            }
            if (currType.isType(Type.MATRIX)) {
                TypeMatrix t = (TypeMatrix) currType;
                Value[][] vals = new Value[t.numRows()][t.numCols()];
                for (int i = 0; i < vals.length; ++i)
                    for (int j = 0; j < vals[i].length; ++j)
                        vals[i][j] = makeOneImpl(type, t.componentType(i,j), currScalar);
                return new ValueMatrix(vals);
            }
            throw new BuildException("Internal error: can't find the one value of type " + currType);
        }


    // makes a Value containing all zeros, except for the position scalarNum, which is 1
    public static Value makeOneImpl(Type type, int scalarNum, IntPtr currScalarNum) {
        if (type.isType(Type.SCALAR)) {
            if (scalarNum == currScalarNum.num++)
                return new ValueScalar(1);
            return new ValueScalar(0);
        }
        if (type.isType(Type.VECTOR)) {
            Type[] comptypes = ((TypeVector) type).componentTypes();
            Value[] compvals = new Value[comptypes.length];
            for (int i = 0; i < comptypes.length; ++i)
                compvals[i] = makeOneImpl(comptypes[i], scalarNum, currScalarNum);
            return new ValueVector(compvals);
        }
        if (type.isType(Type.MATRIX)) {
            Type[][] comptypes = ((TypeMatrix) type).componentTypes();
            Value[][] compvals = new Value[((TypeMatrix) type).numRows()][((TypeMatrix) type).numCols()];
            for (int i = 0; i < comptypes.length; ++i)
                for (int j = 0; j < comptypes[i].length; ++j)
                    compvals[i][j] = makeOneImpl(comptypes[i][j], scalarNum, currScalarNum);
            return new ValueMatrix(compvals);
        }
        throw new BuildException("Internal error: can't find the one value of type " + type);
    }
    

    public Executor apply(Executor[] paramExes, ExeStack prevParamExes, BuildArguments buildArgs) {
        return new ExeVal(makeOne(func_.apply(paramExes, prevParamExes, buildArgs).type()));
    }


    public Executor autoApply(ExeStack prevParamExes, BuildArguments buildArgs) {
        return new ExeVal(makeOne(func_.autoApply(prevParamExes, buildArgs).type()));
    }

    
    public ValueFunction derivative(ParameterID wrtparam, ExeStack funcParams) {
        throw new RuntimeException("shouldn't be here");
        //        return new ValueFunctionOne(func_.derivative(wrtparam, funcParams));
    }

    public ValueFunction derivative(ParameterID wrtparam, SyntaxNode dwrtparam, ExeStack funcParams) {
        throw new RuntimeException("shouldn't be here");
//        return new ValueFunctionOne(func_.derivative(wrtparam, dwrtparam, funcParams));
    }


    public ValueFunction gradient(ExeStack funcParams) {
        throw new RuntimeException("shouldn't be here");
//        return new ValueFunctionOne(func_.gradient(funcParams));
    }


    public ValueFunction simplify() {
        throw new RuntimeException("shouldn't be here");
//        return new ValueFunctionOne(func_.simplify());
    }


    public Type type() {
        throw new RuntimeException("shouldn't be here");
//        return new TypeFunction(this);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueFunctionOne))
            return false;
        return func_.equals(((ValueFunctionOne) val).func_);
    }

    public String toString() {
        return func_.toString();
    }


}


class SNOne extends SyntaxNode {

    private MakeOneCallbackID callbackID_;
    private SyntaxNode sn_;
    
    public SNOne(MakeOneCallbackID callbackID, SyntaxNode sn) {
        callbackID_ = callbackID;
        sn_ = sn;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{sn_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        if (buildArgs.contains(callbackID_.envOneKey())) {
            return new ExeVal((Value) buildArgs.lookup(callbackID_.envOneKey()));
        }
        Executor exe = sn_.build(env, funcParams, buildArgs);
        Type type = exe.type();
        if (type.isType(Type.FUNCTION)) {
            ValueFunction f = ((TypeFunction) type).val();
            return new ExeVal(new ValueFunctionImpl(f.parameters(),
                                                    new SNOne(callbackID_, new SNAppVal(f)),
                                                    f.environment()));
        }
// this was commented out on Mon Dec 2 2002. Could be completely wrong for it to be commented out.
        if (type.isType(Type.SCALAR)) {
            return new ExeVal(new ValueScalar(1));
        }
        throw new MakeOneCallback(callbackID_, type);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                          Environment env, ExeStack funcParams) {
        return new SNMakeZero(sn_);
    }

    public SyntaxNode simplify() {
        return new SNOne(callbackID_, sn_.simplify());
    }

    public String toString() {
        return "(SN-one " + sn_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        sn_.findDependencies(env, deps, ranges);
    }
    
}


class SNMakeOne extends SyntaxNode {

    private SyntaxNode sn_;
    private MakeOneCallbackID callbackID_;

    public SNMakeOne(MakeOneCallbackID callbackID, SyntaxNode body) {
        sn_ = body;
        callbackID_ = callbackID;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{sn_};
    }

    public Executor build(Environment env,
                          ExeStack funcParams,
                          BuildArguments buildArgs) {
        try {
            Executor exe = sn_.build(env, funcParams, buildArgs);
            if (exe.type().isType(Type.FUNCTION)) {
                ValueFunction val = ((TypeFunction) exe.type()).val();
                return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{val},
                                                        new SNMakeOne(callbackID_, val.body()),
                                                        val.environment()));
            }
            return exe;
        }
        catch (MakeOneCallback ex) {
            if (ex.id() != callbackID_) throw ex;
            // got a callback continuation: now we know the return type of the function
            Type type = ex.type();
            Value one = ValueFunctionOne.makeOne(type);
            if (one instanceof ValueScalar)
                return sn_.build(env, funcParams,
                                 buildArgs.extend(callbackID_.envOneKey(), one));
            if (one instanceof ValueVector) {
                Executor[] exes = new Executor[((ValueVector) one).numComponents()];
                for (int i = 0; i < exes.length; ++i)
                    exes[i] = sn_.build(env, funcParams,
                                        buildArgs.extend(callbackID_.envOneKey(),
                                                         ((ValueVector) one).component(i)));
                return SNVector.buildVector(exes, env);
            }
            if (one instanceof ValueMatrix) {
                ValueMatrix onemat = (ValueMatrix) one;
                Executor[][] exes = new Executor[onemat.numRows()][onemat.numCols()];
                for (int i = 0; i < exes.length; ++i)
                    for (int j = 0; j < exes[i].length; ++j)
                        exes[i][j] = sn_.build(env, funcParams,
                                               buildArgs.extend(callbackID_.envOneKey(),
                                                                onemat.component(i,j)));
                return SNMatrix.buildMatrix(exes, env);
            }
            throw new BuildException("Unknown type for making a one value (at SNMakeOne.build)");
        }
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        //        return new SNDerivative(this, wrtvar, dwrtvar);
        return new SNMakeOne(callbackID_, sn_.derivative(wrtvar, dwrtvar, env, funcParams));
    }

    public SyntaxNode simplify() {
        SyntaxNode sn = sn_.simplify();
//        if (sn.isValue())
//            return new SNVal(MB.exec(sn));
        return new SNMakeOne(callbackID_, sn);
    }


    public String toString() {
        return "(SN-makeone " + sn_ + ")";
    }


    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        sn_.findDependencies(env, deps, ranges);
    }



}

class MakeOneCallbackID {
    public Object envOneKey() {
        return this;
    }
}

class MakeOneCallback extends RuntimeException {
    private Type type_;
    private MakeOneCallbackID id_;
    public MakeOneCallback(MakeOneCallbackID id, Type type) {type_ = type; id_ = id;}
    public Type type() {return type_;}
    public MakeOneCallbackID id() {return id_;}
}

