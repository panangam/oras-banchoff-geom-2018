//
//  SNIntegral.java
//  mathbuild
//
//  Created by David Eigen on Sun Aug 11 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.value.*;
import mathbuild.type.*;

public class SNIntegral extends SyntaxNode {

    private SyntaxNode body_;
    private SyntaxNode lower_;
    private SyntaxNode upper_;
    private SyntaxNode resolution_;

    private ExeRange variable_;

    // whether we already have a variable, or whether we need to 
    // make one from the lower, upper, and res exes
    private boolean needToMakeVariable_;

    /**
     * Creates a SyntaxNode for an integral
     * @param varname the name of the variable that the integral is over
     * @param body the expression for the integrand (this is not a function of the variable varname)
     * @param lowerLimit the lower limit
     * @param upperLimit the upper limit
     * @param resolution the resolution for the integral. Automatically rounded by the executor.
     */
    public SNIntegral(String varname, SyntaxNode body, SyntaxNode lowerLimit, SyntaxNode upperLimit, SyntaxNode resolution) {
        this(new SNFunc(varname, body), lowerLimit, upperLimit, resolution);
    }

    /**
     * Creates a SyntaxNode for an integral
     * @param bodyFunc a SN that builds to a function of one variable. This is the integrand. 
     * @param lowerLimit the lower limit
     * @param upperLimit the upper limit
     * @param resolution the resolution for the integral. Automatically rounded by the executor.
     */
    public SNIntegral(SyntaxNode bodyFunc, SyntaxNode lowerLimit, SyntaxNode upperLimit, SyntaxNode resolution) {
        body_ = bodyFunc;
        lower_ = lowerLimit;
        upper_ = upperLimit;
        resolution_ = resolution;
        needToMakeVariable_ = true;
    }

    private SNIntegral(SyntaxNode bodyFunc, ExeRange variable) {
        body_ = bodyFunc;
        variable_ = variable;
        needToMakeVariable_ = false;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{body_, lower_, upper_, resolution_};
    }

    public Executor build(Environment env,
                          ExeStack funcParams, BuildArguments buildArgs) {
        SyntaxNode bodySN;
        if (needToMakeVariable_) {
            Executor lowerExe = new ExeCache(lower_.build(env, funcParams, buildArgs));
            Executor upperExe = new ExeCache(upper_.build(env, funcParams, buildArgs));
            Executor resExe = new ExeCache(resolution_.build(env, funcParams, buildArgs));
            if (!(lowerExe.type().isType(Type.SCALAR) && upperExe.type().isType(Type.SCALAR)))
                throw new BuildException("Limits of integral must be reals.");
            if (!resExe.type().isType(Type.SCALAR))
                throw new BuildException("Resolution of integral must be a real.");
            variable_ = new ExeRangeExe(lowerExe, upperExe, resExe);
            bodySN =  new SNApp(body_, new SyntaxNode[]{new SNExe(variable_)});
        }
        else {
            bodySN = body_;
        }
        Executor bodyExe = bodySN.build(env, funcParams, buildArgs);
        Type bodyType = bodyExe.type();
        if (bodyType.isType(Type.FUNCTION)) {
            ValueFunction bodyFunc = ((TypeFunction) bodyType).val();
            return new ExeVal(new ValueFunctionImpl(new ValueFunction[]{bodyFunc},
                                                    new SNIntegral(bodyFunc.body(),
                                                                   variable_),
                                                    bodyFunc.environment()));
        }
        return new ExeIntegral(variable_, bodyExe,
                               Add.inst().makeOperator(bodyType, bodyType),
                               Multiply.inst().makeOperator(MB.TYPE_SCALAR, bodyType));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                          Environment env, ExeStack funcParams) {
        // if F(x) = integral of  f(x,t) dt from g(x) to h(x), then
        // F'(x) = f(x,h(x))h'(x) - f(x,g(x))g'(x) + integral of d/dx f(x,t) dt from g(x) to h(x)
        // that is:
        // for F(x) = \int_{g(x)}^{h(x)} f(x,t) dt  :
        // F'(x) = f(x,h(x))h'(x) - f(x,g(x))g'(x) + \int_{g(x)}^{h(x)} f_x(x,t) dt
        SyntaxNode dbody  =  body_.derivative(wrtvar, dwrtvar, env, funcParams);
        if (needToMakeVariable_) {
            SyntaxNode dlower = lower_.derivative(wrtvar, dwrtvar, env, null);
            SyntaxNode dupper = upper_.derivative(wrtvar, dwrtvar, env, null);
            SyntaxNode term1 = new SNMultiply(new SNApp(body_, upper_), dupper);
            SyntaxNode term2 = new SNMultiply(new SNApp(body_, lower_), dlower);
            SyntaxNode term3 = new SNIntegral(dbody, lower_, upper_, resolution_);
            return new SNAdd(new SNSubtract(term1, term2), term3);
        }
        return new SNIntegral(dbody, variable_);
    }

    public SyntaxNode simplify() {
        SyntaxNode body = body_.simplify();
        if (body.isValue())
            return new SNMultiply(body, new SNSubtract(upper_, lower_)).simplify();
        return needToMakeVariable_ ? new SNIntegral(body,
                                                    lower_.simplify(), upper_.simplify(),
                                                    resolution_.simplify())
                                   : new SNIntegral(body, variable_);
    }

    public String toString() {
        return "(SN-integral " + body_ + " " +
        lower_ + " " + upper_ + " " + resolution_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        body_.findDependencies(env, deps, ranges);
        upper_.findDependencies(env, deps, ranges);
        lower_.findDependencies(env, deps, ranges);
        resolution_.findDependencies(env, deps, ranges);
    }
    
    
}

class ExeIntegral implements Executor {

    private ExeRange var_;
    private Executor body_;
    private OpBin addOp_, multOp_;

    /**
     * Creates a new Executor object for an integral using Simpson's rule.
     * The integral evaluates \int_a^b f(x) dx
     * @param variable the variable that the integral is over (ie, "x" in the above)
     * @param body the body of the integral (ie, "f(x)" in the above)
     * @param addOp the operator for adding two values of the body's type (must return body's type)
     * @param multOp the operator for multiplying the body's type by a scalar (must return body's type)
     */
    public ExeIntegral(ExeRange variable, Executor body, OpBin addOp, OpBin multOp) {
        var_ = variable;
        body_ = body;
        addOp_ = addOp;
        multOp_ = multOp;
    }

    public Type type() {
        return body_.type();
    }

    public Value execute(Object runID) {
        final Value FOUR = new ValueScalar(4),
                    TWO = new ValueScalar(2);
        // calculate integral with Simpson's rule:
        // Area = h/3 * (y_0 + 4y_1 + 2y_2 + 4y_3 + ... + 4y_n-1 + y_n)
        double lower = ((ValueScalar) var_.start(new Object())).number();
        double upper = ((ValueScalar) var_.end(new Object())).number();
        double res = Math.round(((ValueScalar) var_.res(new Object())).number());
        if (res % 2 != 0)
            res++;
        if (res < 2) res = 2;
        var_.set(lower);
        Value firstEval = body_.execute(new Object());
        var_.set(upper);
        Value lastEval = body_.execute(new Object());
        double h = (upper - lower) / res;
        double curr = lower + h;
        // add first and last values
        Value result = addOp_.operate(firstEval, lastEval);
        // add odd multiple values (they have 4 as coefficient)
        for ( int i = 1; i <= res - 1; i += 2 ) {
            var_.set(curr);
            result = addOp_.operate(result, multOp_.operate(FOUR, body_.execute(new Object())));
            curr += 2 * h;
        }
        curr = lower + 2 * h;
        // add even multiple values (they have 2 as coefficient)
        for ( int i = 2; i <= res - 2; i += 2 ) {
            var_.set(curr);
            result = addOp_.operate(result, multOp_.operate(TWO, body_.execute(new Object())));
            curr += 2 * h;
        }
        // multiply result by h/3
        return multOp_.operate(new ValueScalar(h/3), result);
    }

}
