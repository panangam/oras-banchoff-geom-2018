//
//  ValueFunction.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.SyntaxNode;
import mathbuild.Environment;
import mathbuild.Executor;
import mathbuild.impl.ParameterID;
import mathbuild.impl.ExeStack;
import mathbuild.impl.BuildArguments;

public interface ValueFunction extends Value {
    
    /**
     * @return the number of arguments
     */
    public int numArgs();
    
    /**
     * @return the parameter IDs for the parameters of this function
     */
    public ParameterID[] parameters();

    /**
     * @param i the parameter number
     * @return the ParameterID for that parameter
     */
    public ParameterID parameter(int i);

    /**
     * @return body syntax node of this function
     */
    public SyntaxNode body();
    
    /**
     * @return the creation environment of the function
     */
    public Environment environment();

    /**
     * @return whether this ValueFunction is closing over an environment
     */
    public boolean isClosure();

    /**
     * Does paramter conversion for vector -> many args and many args -> vector
     * @param a list of arguments that will be applied to the function
     * @return a list of arguments that can be applied to the function
     */
    public Executor[] convertParams(Executor[] paramExes);
        
    /**
     * @param paramExes the parameters to apply the function to
     * @param prevParamExes If applying this function returns a function F, prevParamExes are the
     *        parameters that F will be applied to.
     * @return the Executor object obtained from applying the function
     */
    public Executor apply(Executor[] paramExes, ExeStack prevParamExes, BuildArguments buildArgs);

    /**
     * applies this function for auto-application, using the given (current) stack of parameters
     * @param paramExes the current stack of parameters
     * @return the Executor object obtained from applying the function
     */
    public Executor autoApply(ExeStack paramExes, BuildArguments buildArgs);

    /**
     * Finds the derivative of this ValueFunction with respect to a parameter from this function.
     * All identifiers that are not immediately bound to a parameter are given a
     * derivative of Zero.
     * @param wrtparam the number of the parameter to take the derivative with respect to
     * @param dwrtparam the derivative of wrtparam wrt itself
     * @param futureParams the parameters that will be applied to this function
     * @return the derivative of this function
     */
    public ValueFunction derivative(ParameterID wrtparam, ExeStack funcParams);

    /**
     * Finds the derivative of this ValueFunction with respect to a parameter *NOT* from this function.
     * All identifiers that are not immediately bound to a parameter are given a
     * derivative of Zero.
     * @param wrtparam the number of the parameter to take the derivative with respect to
     * @param dwrtparam the derivative of wrtparam wrt itself
     * @param futureParams the parameters that will be applied to this function
     * @return the derivative of this function
     */
    public ValueFunction derivative(ParameterID wrtparam, SyntaxNode dwrtparam, ExeStack funcParams);        

    /**
     * Finds the gradient of this ValueFunction.
     * All identifiers that are not immediately bound to a parameter are given a
     * derivative of Zero.
     * @return the gradient of this function
     */
    public ValueFunction gradient(ExeStack funcParams);

    /**
     * Simplifies the body of this function.
     * @return the simplified funciton
     */
    public ValueFunction simplify();

        
    
}
