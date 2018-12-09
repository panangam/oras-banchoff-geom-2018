//
//  ValueFunctionZero.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.*;
import mathbuild.*;
import mathbuild.impl.*;

public class ValueFunctionZero implements ValueFunction {

    private ValueFunction func_;
    
    /**
     * Constructs a new zero function for the given function.
     * The zero function behaves exactly like the given function, only
     * it always returns a zero value when it is applied or its derivative
     * is taken.
     * For example, if apply(.) is called on this ValueFunctionZero, then
     * the affect will be as if apply(.) was called on the function func
     * (passed as a parameter to this constructor). However, instead of
     * returning the Executor func's apply(.) would return, this class will
     * return an Executor for a zero value of same type as the executor.
     *
     * @param func the function to wrap as a zero function
     */
    public ValueFunctionZero(ValueFunction func) {
        while (func instanceof ValueFunctionZero)
            func = ((ValueFunctionZero) func).func_;
        func_ = func;
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
        return new SNMakeZero(func_.body());
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


    
    public Executor apply(Executor[] paramExes, ExeStack prevParamExes, BuildArguments buildArgs) {
        return new ExeVal(MB.zero(func_.apply(paramExes, prevParamExes, buildArgs).type()));
    }

    public Executor autoApply(ExeStack prevParamExes, BuildArguments buildArgs) {
        return new ExeVal(MB.zero(func_.autoApply(prevParamExes, buildArgs).type()));
    }
    
    
    public ValueFunction derivative(ParameterID wrtparam, ExeStack funcParams) {
        return new ValueFunctionZero(func_.derivative(wrtparam, funcParams));
    }

    public ValueFunction derivative(ParameterID wrtparam, SyntaxNode dwrtparam, ExeStack funcParams) {
        return new ValueFunctionZero(func_.derivative(wrtparam, dwrtparam, funcParams));
    }


    public ValueFunction gradient(ExeStack funcParams) {
        return new ValueFunctionZero(func_.gradient(funcParams));
    }
    

    public ValueFunction simplify() {
        return new ValueFunctionZero(func_.simplify());
    }

        
    public Type type() {
        return new TypeFunction(this);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueFunctionZero))
            return false;
        return func_.equals(((ValueFunctionZero) val).func_);
    }

    public String toString() {
        return func_.toString();
    }

    
}
