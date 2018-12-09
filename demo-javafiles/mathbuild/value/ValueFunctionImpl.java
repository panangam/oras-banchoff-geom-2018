//
//  ValueFunctionImpl.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.*;
import mathbuild.*;
import mathbuild.impl.*;

public class ValueFunctionImpl implements ValueFunction {

    private String[] argnames_;
    private ParameterID[] argIDs_;
    private SyntaxNode body_;
    private Environment env_;
    private boolean isClosure_ = true;
    private java.util.Hashtable buildCache_ = null;
    private java.util.Hashtable derivCache_ = null;

    /**
     * Constructs a new ValueFunction. Argument names are set to the default names, and
     * creation environment is empty. Should be used only for built-in functions
     * @param numArgs the number of arguments
     * @param body the syntax node for the body of the function
     */
    public ValueFunctionImpl(int numArgs, SyntaxNode body) {
        argnames_ = new String[numArgs];
        argIDs_ = new ParameterID[numArgs];
        for (int i = 0; i < numArgs; ++i) {
            argIDs_[i] = new ParameterID(i);
            argnames_[i] = argIDs_[i].name();
        }
        body_ = body;
        env_ = new Environment();
    }

    /**
     * Constructs a new ValueFunction.
     * @param argparams the ParameterIDs for the arguments of this function
     *                  the function value for these ParameterIDs is assigned (mutated) to this
     * @param body the syntax node for the body of the function
     * @param env the creation environment of the function
     */
    public ValueFunctionImpl(ParameterID[] argparams, SyntaxNode body, Environment env) {
        body_ = body;
        env_ = env;
        argIDs_ = argparams;
        argnames_ = new String[argparams.length];
        for (int i = 0; i < argIDs_.length; ++i)
            argnames_[i] = argIDs_[i].name();
    }


    /**
     * Constructs a new ValueFunction.
     * @param subfuncs the subfunctions of this function (any, but not all, of the entries
     *                 in the array can be null)
     * @param body the syntax node for the body of the function
     * @param env the creation environment of the function
     * @throws BuildException if the subfunctions have a different number of arguments
     */
    public ValueFunctionImpl(ValueFunction[] subfuncs, SyntaxNode body, Environment env) throws BuildException {
        body_ = body;
        env_ = env;
        java.util.Vector subfuncs2 = new java.util.Vector(subfuncs.length);
        for (int i = 0; i < subfuncs.length; ++i)
            if (subfuncs[i] != null)
                subfuncs2.addElement(subfuncs[i]);
        subfuncs = new ValueFunction[subfuncs2.size()];
        for (int i = 0; i < subfuncs2.size(); ++i)
            subfuncs[i] = (ValueFunction) subfuncs2.elementAt(i);
        int numargs = subfuncs[0].numArgs();
        for (int i = 1; i < subfuncs.length; ++i)
            if (numargs != subfuncs[i].numArgs())
                throw new BuildException("Functions being combined must have the same number of arguments.");
        argIDs_ = new ParameterID[numargs];
        for (int i = 0; i < numargs; ++i)
            argIDs_[i] = new ParameterID(i, subfuncs);
        argnames_ = new String[numargs];
        for (int i = 0; i < numargs; ++i)
            argnames_[i] = argIDs_[i].name();
    }

    /**
     * Constructs a new ValueFunction. The constructed function is not a closure. Should be used
     * to combine functions, as in "f + g" becoming a combined function for f+g. The functions f
     * and g can be closures, but the function f+g doesn't have to be.
     * @param subfuncs the subfunctions of this function (any, but not all, of the entries
     *                 in the array can be null)
     * @param body the syntax node for the body of the function
     * @throws BuildException if the subfunctions have a different number of arguments
     */
    public ValueFunctionImpl(ValueFunction[] subfuncs, SyntaxNode body) throws BuildException {
        this(subfuncs, body, new Environment());
        isClosure_ = false;
    }

    /**
     * Constructs a new ValueFunction.
     * @param subfunc the subfunction of this function
     * @param body the syntax node for the body of the function
     * @param env the creation environment of the function
     * @throws BuildException if the subfunctions have a different number of arguments
     */
    public ValueFunctionImpl(ValueFunction subfunc, SyntaxNode body, Environment env) throws BuildException {
        this(new ValueFunction[]{subfunc}, body, env);
    }

    /**
     * Constructs a new ValueFunction. The constructed function is not a closure. Should be used
     * to combine functions, as in "-f" becoming a combined function for -f. The function f
     * can be a closures, but the function -f doesn't have to be.
     * @param subfunc the subfunction of this function
     * @param body the syntax node for the body of the function
     * @throws BuildException if the subfunctions have a different number of arguments
     */
    public ValueFunctionImpl(ValueFunction subfunc, SyntaxNode body) throws BuildException {
        this(new ValueFunction[]{subfunc}, body, new Environment());
        isClosure_ = false;
    }

    /**
     * Constructs a new ValueFunction.
     * @param argparams the ParameterIDs for the arguments of this function
     *                  the function value for these ParameterIDs is assigned (mutated) to this
     * @param body the syntax node for the body of the function
     * @param env the creation environment of the function
     * @param isClosure whether this ValueFunctionImpl is a closure.
     */
    protected ValueFunctionImpl(ParameterID[] argparams, SyntaxNode body, Environment env, boolean isClosure) {
        this(argparams, body, env);
        isClosure_ = isClosure;
    }
        
    /**
     * @return the number of arguments
     */
    public int numArgs() {
        return argnames_.length;
    }
    
    
    public ParameterID[] parameters() {
        return argIDs_;
    }
    
    public ParameterID parameter(int i) {
        return argIDs_[i];
    }
    
    /**
     * @return the body of the function
     */
    public SyntaxNode body() {
        return body_;
    }

    /**
     * @return the creation environment of the function
     */
    public Environment environment() {
        return env_;
    }

    /**
     * @return whether this ValueFunction is closing over an environment
     */
    public boolean isClosure() {
        return isClosure_;
    }

    
    /**
     * Does paramter conversion for vector -> many args and many args -> vector
     * @param a list of arguments that will be applied to the function
     * @return a list of arguments that can be applied to the function
     */
    public Executor[] convertParams(Executor[] paramExes) {
        if (numArgs() == paramExes.length)
            // no parameter conversion necessary: number of arguments matches
            return paramExes;
        // try parameter type conversion
        Executor[] newParams = new Executor[numArgs()];
        if (numArgs() == 1 && paramExes.length > 1) {
            // many arguments --> vector (or function)
            newParams[0] = SNVector.buildVector(paramExes, Environment.EMPTY);
        }
        else if (numArgs() > 1 && paramExes.length == 1 &&
                 paramExes[0].type().isType(Type.VECTOR)) {
            // vector --> many arguments
            // first, check that number of arguments to the function is the same as the
            // number of components in the vector
            TypeVector paramType = (TypeVector) paramExes[0].type();
            if (paramType.numComponents() != numArgs())
                throw new BuildException("Function applied with wrong number of arguments.");
            Executor exeCache = paramExes[0] instanceof ExeCache ? (ExeCache) paramExes[0]
                                                                 : new ExeCache(paramExes[0]);
            for (int i = 0; i < newParams.length; ++i)
                newParams[i] = new ExeVectorComponent(i, exeCache);
        }
        else if (numArgs() > 1 && paramExes.length == 1 &&
                 paramExes[0].type().isType(Type.FUNCTION)) {
            // vector --> many arguments
            Executor paramExe = paramExes[0] instanceof ExeCache ? (ExeCache) paramExes[0]
                                                                 : new ExeCache(paramExes[0]);
            for (int i = 0; i < newParams.length; ++i)
                newParams[i] = SNVectorComponent.buildComponent(i, paramExe,
                                                                Environment.EMPTY);
        }
        else {
            throw new BuildException("Function applied with wrong number of arguments.");
        }
        return newParams;
    }
    

    private class BuildCacheKey {
        private Executor[] exes;
        private ExeStack stack;
        private BuildArguments args;
        public BuildCacheKey(Executor[] e, ExeStack s, BuildArguments a) {
            exes = e; stack = s; args = a;
        }
        public boolean equals(Object o) {
            if (!(o instanceof BuildCacheKey))
                return false;
            BuildCacheKey k = (BuildCacheKey) o;
            if (k.exes.length != exes.length) return false;
            for (int i = 0; i < exes.length; ++i)
                if (!k.exes[i].equals(exes[i]))
                    return false;
            return k.stack.equals(stack);// && k.args == args;
        }
        public int hashCode() {
            return (exes.length > 0 ? exes[0].hashCode() : 0)
                   + stack.hashCode();// + args.hashCode();
        }
    }
    
    public Executor apply(Executor[] paramExes, ExeStack prevParamExes, BuildArguments buildArgs) {
        Executor exe;
        BuildCacheKey key = new BuildCacheKey(paramExes, prevParamExes, buildArgs);
        if (buildCache_ == null)
            buildCache_ = new java.util.Hashtable();
        if ((exe = (Executor) buildCache_.get(key)) != null) {
//            System.out.println(" *** Found cached func result! *** ");
            return exe;
        }
        Executor[] convertedParamExes = convertParams(paramExes);
        ParameterExe[] params = new ParameterExe[numArgs()];
        for (int i = 0; i < params.length; ++i) {
            params[i] = new ParameterExe(argIDs_[i], convertedParamExes[i]);
        }
        exe = body().build(environment().extend(argnames_, params),
                           prevParamExes.push(convertedParamExes),
                           buildArgs.extendApplying(true));
        if (!(exe instanceof ExeCache))
            exe = new ExeCache(exe);
        if (buildCache_.size() > 20)
            buildCache_.clear();
        buildCache_.put(key, exe);
        return exe;
    }

    public Executor autoApply(ExeStack paramExeStack, BuildArguments buildArgs) {
        return apply(paramExeStack.peek(), paramExeStack.pop(), buildArgs);
        /*
        Executor[] paramExes = convertParams(paramExeStack.peek());
        ParameterExe[] params = new ParameterExe[numArgs()];
        for (int i = 0; i < params.length; ++i) {
            params[i] = new ParameterExe(argIDs_[i], paramExes[i]);
        }
        return body().build(environment().extend(argnames_, params),
                            paramExeStack.pop().push(paramExes),
                            buildArgs.extendApplying(true));
         */
    }

    /*
    public ValueFunction substitute(SyntaxNode[] paramSNs, Type[] paramTypes, BuildArguments buildArgs) {
        paramSNs = convertParams(paramSNs, paramTypes);
        return new ValueFunctionImpl(new ParameterID[0],
                                     body().substitute(argIDs_, paramSNs, environment()),
                                     environment(),
                                     isClosure_);
    }
     */

    private class DerivCacheKey {
        private ParameterID wrtparam;
        private SyntaxNode dwrtparam;
        private ExeStack stack;
        public DerivCacheKey(ParameterID wrtp, SyntaxNode dwrtp, ExeStack s) {
            wrtparam = wrtp; dwrtparam = dwrtp; stack = s;
        }
        public boolean equals(Object o) {
            if (!(o instanceof DerivCacheKey))
                return false;
            DerivCacheKey k = (DerivCacheKey) o;
            if ((dwrtparam != null && k.dwrtparam == null) ||
                (dwrtparam == null && k.dwrtparam != null))
                return false;
            return k.wrtparam.equals(wrtparam) &&
                   (dwrtparam != null ? k.dwrtparam.equals(dwrtparam) : true) &&
                   k.stack.equals(stack);
        }
        public int hashCode() {
            return wrtparam.hashCode() + stack.hashCode()
                   + (dwrtparam == null ? 0 : dwrtparam.hashCode());
        }
    }

    
    
    /**
     * Finds the derivative of this ValueFunction with respect to a parameter from this function.
     * All identifiers that are not immediately bound to a parameter are given a
     * derivative of Zero.
     * @param wrtparam the number of the parameter to take the derivative with respect to
     * @param paramStack the current parameter stack. The top is the type the deriv will
     *        be applied to.
     * @return the derivative of this function
     */
    public ValueFunction derivative(ParameterID wrtparam, ExeStack paramStack) {
        Executor[] paramExes = convertParams(paramStack.peek());
        DerivCacheKey key = new DerivCacheKey(wrtparam, null, paramStack);
        if (derivCache_ == null)
            derivCache_ = new java.util.Hashtable();
        ValueFunction df;
        if ((df = (ValueFunction) derivCache_.get(key)) != null) {
//            System.out.println(" **** Found Cahced Derivative! **** ");
            return df;
        }
        Value[] paramZeroVals = new Value[paramExes.length];
        ParameterDeriv[] params = new ParameterDeriv[paramExes.length];
        for (int i = 0; i < params.length; ++i) {
            paramZeroVals[i] = MB.zero(paramExes[i].type());
            params[i] = new ParameterDeriv(argIDs_[i],
                                           new SNVal(paramZeroVals[i]));
        }
        Type wrtparamType = paramExes[wrtparam.num()].type();
        if (wrtparamType.isType(Type.FUNCTION)) {
            MakeOneCallbackID callbackID = new MakeOneCallbackID();
            SyntaxNode dwrtparam = new SNOne(callbackID, new SNVal(paramZeroVals[wrtparam.num()]));
            df = new ValueFunctionImpl(argIDs_,
                                        new SNMakeOne(callbackID,
                                            body_.derivative(
                                                wrtparam, dwrtparam,
                                                env_.extend(argnames_, params)
                                                    .extend(wrtparam.name(),
                                                            new ParameterDeriv(wrtparam, dwrtparam)),
                                                paramStack.pop().push(paramExes))),
                                        env_,
                                        isClosure_);
        }
        else {
            // wrt param type is not a function
            Value one = ValueFunctionOne.makeOne(wrtparamType);
            if (one instanceof ValueScalar) {
                SyntaxNode dwrtparam = new SNVal(one);
                df = new ValueFunctionImpl(argIDs_,
                                           body_.derivative(
                                                wrtparam, dwrtparam,
                                                env_.extend(argnames_, params)
                                                    .extend(wrtparam.name(),
                                                            new ParameterDeriv(wrtparam, dwrtparam)),
                                                paramStack.pop().push(paramExes)),
                                           env_,
                                           isClosure_);
            }
            else if (one instanceof ValueVector) {
                // "one" is actually the identity matrix (or other one type) as a vector of columns
                ValueVector onevec = (ValueVector) one;
                SyntaxNode[] derivs = new SyntaxNode[onevec.numComponents()];
                for (int i = 0; i < derivs.length; ++i) {
                    SyntaxNode dwrtparam = new SNVal(onevec.component(i));
                    derivs[i] = body_.derivative(
                                        wrtparam, dwrtparam,
                                        env_.extend(argnames_, params)
                                        .extend(wrtparam.name(),
                                                new ParameterDeriv(wrtparam, dwrtparam)),
                                        paramStack.pop().push(paramExes));
                }
                df = new ValueFunctionImpl(argIDs_,
                                           new SNVector(derivs),
                                           env_,
                                           isClosure_);
            }
            else if (one instanceof ValueMatrix) {
                // "one" is actually a matrix of matrices that are the "one" values
                ValueMatrix onemat = (ValueMatrix) one;
                SyntaxNode[][] derivs = new SyntaxNode[onemat.numRows()][onemat.numCols()];
                for (int i = 0; i < derivs.length; ++i) {
                    for (int j = 0; j < derivs[i].length; ++j) {
                        SyntaxNode dwrtparam = new SNVal(onemat.component(i,j));
                        derivs[i][j] = body_.derivative(wrtparam, dwrtparam,
                                                        env_.extend(argnames_, params)
                                                        .extend(wrtparam.name(),
                                                                new ParameterDeriv(wrtparam, dwrtparam)),
                                                        paramStack.pop().push(paramExes));
                    }
                }
                df = new ValueFunctionImpl(argIDs_,
                                           new SNMatrix(derivs),
                                           env_,
                                           isClosure_);
            }
            else throw new BuildException("Unknown type for the variable to take the derivative with respect to.");
        }
        if (derivCache_.size() > 20)
            derivCache_.clear();
        derivCache_.put(key, df);
        return df;
    }

    /**
     * Finds the derivative of this ValueFunction with respect to a parameter *NOT* from this function.
     * All identifiers that are not immediately bound to a parameter are given a
     * derivative of Zero.
     * @param wrtparam the number of the parameter to take the derivative with respect to
     * @param dwrtparam the derivative of wrtparam wrt itself
     * @param funcParams the stack of parameters; the top parameters are the types that will be
     *        applied to this function
     * @return the derivative of this function
     */
    public ValueFunction derivative(ParameterID wrtparam, SyntaxNode dwrtparam, ExeStack funcParams) {
        Executor[] paramExes = convertParams(funcParams.peek());
        DerivCacheKey key = new DerivCacheKey(wrtparam, dwrtparam, funcParams);
        if (derivCache_ == null)
            derivCache_ = new java.util.Hashtable();
        ValueFunction df;
        if ((df = (ValueFunction) derivCache_.get(key)) != null) {
//            System.out.println(" **** Found Cahced Derivative! **** ");
            return df;
        }
        // get a list of all zeros for parameters
        ParameterDeriv[] params = new ParameterDeriv[argnames_.length];
        for (int i = 0; i < params.length; ++i)
            params[i] = new ParameterDeriv(argIDs_[i],
                                           new SNVal(MB.zero(
                                                                paramExes[i].type())));
        // it's the parameter for another function: so take the derivative wrt it, w/ all
        // parameters of this function (except a subparameter of the wrtparam) mapping to zero
        for (int i = 0; i < numArgs(); ++i)
            if (wrtparam.isSubparam(argIDs_[i]))
                params[i] = new ParameterDeriv(argIDs_[i], dwrtparam);
        df = new ValueFunctionImpl(argIDs_,
                                     body_.derivative(wrtparam, dwrtparam,
                                                      env_.extend(argnames_, params),
                                                      funcParams.pop().push(paramExes)),
                                     env_,
                                     isClosure_);
        if (derivCache_.size() > 20)
            derivCache_.clear();
        derivCache_.put(key, df);
        return df;
    }


    public ValueFunction gradient(ExeStack funcParams) {
        if (argIDs_.length == 1) {
            return derivative(argIDs_[0], funcParams);
        }
        Executor[] paramExes = convertParams(funcParams.peek());
        Executor[] derivs = new Executor[paramExes.length];
        for (int i = 0; i < derivs.length; ++i) {
            derivs[i] = new ExeVal(derivative(argIDs_[i], funcParams));
        }
        return ((TypeFunction) SNVector.buildVector(derivs, env_).type()).val();
        /*
        // get a list of all zeros for parameters
        ParameterDeriv[] params = new ParameterDeriv[argnames_.length];
        for (int i = 0; i < params.length; ++i)
            params[i] = new ParameterDeriv(argIDs_[i],
                                           new SNVal(MB.zero(paramExes[i].type())));
        // take derivative wrt all our variables
        SyntaxNode[] derivs = new SyntaxNode[numArgs()];
        for (int i = 0; i < numArgs(); ++i) {
            ParameterID wrtparam = argIDs_[i];
            Type wrtparamType = paramExes[wrtparam.num()].type();
             = ValueFunctionOne.makeOne(wrtparamType);
            derivs[i] = new SNVal(new ValueFunctionImpl(
                            argIDs_,
                            body_.derivative(wrtparam, dwrtparam,
                                            env_.extend(wrtparam.name(),
                                                        new ParameterDeriv(wrtparam,
                                                                           dwrtparam)),
                                            funcParams),
                            env_,
                            isClosure_));
        }
        return new ValueFunctionImpl(argIDs_,
                                     new SNVector(derivs),
                                     env_,
                                     isClosure_);
*/
    }
    

    public ValueFunction simplify() {
        return new ValueFunctionImpl(argIDs_, body_.simplify(), env_, isClosure_);
    }

        
    public Type type() {
        return new TypeFunction(this);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueFunctionImpl))
            return false;
        ValueFunctionImpl f = (ValueFunctionImpl) val;
        if ( ! ((f.body() == body_) && (f.environment() == env_)) )
            return false;
        if (f.numArgs() != this.numArgs())
            return false;
        for (int i = 0; i < numArgs(); ++i)
            if (!argnames_[i].equals(f.argnames_[i]))
                return false;
        return true;
    }

    public String toString() {
        String str = "func(";
        for (int i = 0; i < argnames_.length; ++i) {
            str += argnames_[i];
            if (i < argnames_.length - 1)
                str += ",";
        }
        str += "){...}";
        return str;
    }

    
}
