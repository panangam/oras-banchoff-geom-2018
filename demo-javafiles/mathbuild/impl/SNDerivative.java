//
//  SNDerivative.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class SNDerivative extends SyntaxNode {

    /**
     * For derivatives other than a derivative wrt a single parameter, construct SNDerivative
     * using one of the following in the constructor
     */
    public static final DerivativeAction GRADIENT = new DerivActionGradient();

    private SyntaxNode func_;
    
    private DerivativeAction[] derivs_; // types of derivatives to take
                                        // innermost derivative (first to take) is first in vector,
                                        // last one to take is last in vector.
                                        // entries can be 3 types:
                                        // ParameterID : the exact parameter to take the derivative wrt
                                        // Integer : number of parameter of function to take deriv wrt
                                        // String : name of parameter of function to take deriv wrt
                                      
    
    /**
     * @param func the SyntaxNode of the function whose derivative to take
     * @param wrtparam the parameter number that the derivative with respect to
     */
    public SNDerivative(SyntaxNode func, int wrtparam) {
        func_ = func;
        derivs_ = new DerivativeAction[]{new DerivActionFuncParamNum(wrtparam)};
    }

    /**
     * @param func the SyntaxNode of the function whose derivative to take
     * @param wrtname the name of the parameter that the derivative is taken with respect to
     */
    public SNDerivative(SyntaxNode func, String wrtname) {
        func_ = func;
        derivs_ = new DerivativeAction[]{new DerivActionFuncParamName(wrtname)};
    }

    /**
     * @param func the SyntaxNode of the function whose derivative to take
     * @param wrtparam the parameter that the derivative is taken with respect to
     * @param dwrtparam the derivative of wrtparam wrt itself
     */
    public SNDerivative(SyntaxNode func, ParameterID wrtparam, SyntaxNode dwrtparam) {
        func_ = func;
        derivs_ = new DerivativeAction[]{new DerivActionParam(wrtparam, dwrtparam)};
    }

    /**
     * Constructs a SNDerivative that takes a derivative other than a derivative wrt a single var
     * @param func the SyntaxNode of the function whose derivative to take
     * @param derivAction the type of derivative (see DerivativeAction constants above)
     */
    public SNDerivative(SyntaxNode func, DerivativeAction derivAction) {
        func_ = func;
        derivs_ = new DerivativeAction[]{derivAction};
    }
    
    /**
     * @param func the SyntaxNode of the function whose derivative to take
     * @param lastAction the action for the last derivative to take
     * @param actions the first derivatives
     */
    private SNDerivative(SyntaxNode func, DerivativeAction lastAction, DerivativeAction[] actions) {
        func_ = func;
        derivs_ = new DerivativeAction[actions.length + 1];
        for (int i = 0; i < actions.length; ++i)
            derivs_[i] = actions[i];
        derivs_[derivs_.length-1] = lastAction;
    }

    /**
     * @param func the SyntaxNode of the function whose derivative to take
     * @param actions the derivatives
     */
    private SNDerivative(SyntaxNode func, DerivativeAction[] actions) {
        func_ = func;
        derivs_ = new DerivativeAction[actions.length];
        for (int i = 0; i < actions.length; ++i)
            derivs_[i] = actions[i];
    }

    

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
//        if (buildArgs.applying() && !funcParams.isEmpty())
//            funcParams = funcParams.pop();
        Executor funcExe = func_.build(env, funcParams, buildArgs.extendApplying(false));
        Type t = funcExe.type();
        if (!t.isType(Type.FUNCTION)) {
            // return new ExeVal(MB.zero(t));
            throw new BuildException("Derivative taken of a non-function.");
        }
        ValueFunction f = ((TypeFunction) t).val();
        return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{f},
                                                new SNAppDeriv(f, derivs_)));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        return new SNDerivative(func_, new DerivActionParam(wrtvar, dwrtvar), derivs_);
    }

    public SyntaxNode simplify() {
        SyntaxNode func = func_.simplify();
        return new SNDerivative(func, derivs_);
    }

    public String toString() {
        String actionsStr = "";
        for (int i = 0; i < derivs_.length; ++i)
            actionsStr += derivs_[i].actionString() + " ";
        return "(SN-deriv " + actionsStr + func_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        func_.findDependencies(env, deps, ranges);
    }



    public SyntaxNode[] children() {
        return new SyntaxNode[]{func_};
    }
    


    
    private static abstract class DerivativeAction {
        /**
         * @param f the function to take the derivative of
         * @return this derivative applied to f
         */
        public abstract ValueFunction derivative(ValueFunction f, Environment env, ExeStack params);
        public abstract String actionString(); // for SNDerivative.toString()
    }

    private static class DerivActionParam extends DerivativeAction {
        ParameterID param_;
        SyntaxNode dparam_;
        public DerivActionParam(ParameterID param, SyntaxNode dparam) {param_ = param; dparam_ = dparam;}
        public ValueFunction derivative(ValueFunction f, Environment env, ExeStack params) {
            return f.derivative(param_, dparam_, params);
        }
        public String actionString() {
            return "p:" + param_.name();
        }
    }

    private static class DerivActionFuncParamNum extends DerivativeAction {
        int param_;
        public DerivActionFuncParamNum(int param) {param_ = param;}
        public ValueFunction derivative(ValueFunction f, Environment env, ExeStack params) {
            if (param_ >= f.numArgs())
                throw new BuildException("Parameter number for derivative greater than number of parameters for the function.");
            return f.derivative(f.parameter(param_), params);
        }
        public String actionString() {
            return String.valueOf(param_);
        }
    }

    private static class DerivActionFuncParamName extends DerivativeAction {
        String param_;
        public DerivActionFuncParamName(String param) {param_ = param;}
        public ValueFunction derivative(ValueFunction f, Environment env, ExeStack params) {
            ParameterID param = null;
            for (int i = 0; i < f.numArgs(); ++i) {
                if (f.parameter(i).name().equals(param_)) {
                    param = f.parameter(i);
                    break;
                }
            }
            if (param == null)
                throw new BuildException("Variable for derivative is not a parameter of the function.");
            return f.derivative(param, params);
        }
        public String actionString() {
            return param_;
        }
    }

    private static class DerivActionGradient extends DerivativeAction {
        public DerivActionGradient() {}
        public ValueFunction derivative(ValueFunction f, Environment env, ExeStack params) {
            return f.gradient(params);
        }
        public String actionString() {
            return "grad";
        }
    }




    

    private class SNAppDeriv extends SyntaxNode {

        private ValueFunction func_;
        private DerivativeAction[] derivs_;

        public SNAppDeriv(ValueFunction f, DerivativeAction[] derivs) {
            func_ = f;
            derivs_ = derivs;
        }

        private SNAppDeriv(ValueFunction func, DerivativeAction lastAction, DerivativeAction[] actions) {
            func_ = func;
            derivs_ = new DerivativeAction[actions.length + 1];
            for (int i = 0; i < actions.length; ++i)
                derivs_[i] = actions[i];
            derivs_[derivs_.length-1] = lastAction;
        }
        
        public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
            if (funcParams.isEmpty())
                throw new BuildException("INTERNAL ERROR: funcParams should not be empty in SNAppDeriv");
            ValueFunction df = func_;
            for (int i = 0; i < derivs_.length; ++i)
                df = derivs_[i].derivative(df, env, funcParams).simplify();
            if (buildArgs.applying())
                return df.autoApply(funcParams, buildArgs);
            return new ExeVal(df);
        }

        public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                     Environment env, ExeStack funcParams) {
            return new SNAppDeriv(func_, new DerivActionParam(wrtvar, dwrtvar), derivs_);
        }

        public SyntaxNode simplify() {
            return this;
        }

        public String toString() {
            String actionsStr = "";
            for (int i = 0; i < derivs_.length; ++i)
                actionsStr += derivs_[i].actionString() + " ";
            return "(SN-appderiv " + actionsStr + func_ + ")";
        }

        public SyntaxNode[] children() {
            return new SyntaxNode[]{};
        }
        
        public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        }

        
    }



    

}





