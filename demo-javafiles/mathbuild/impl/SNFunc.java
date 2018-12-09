//
//  SNFunc.java
//  mathbuild
//
//  Created by David Eigen on Sat Jul 13 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;

/**
 * Syntax Node for creating a function.
 */
public class SNFunc extends SyntaxNode {

    private ParameterID[] paramIDs_;
    private SyntaxNode body_;

    public SNFunc(String argname, SyntaxNode body) {
        this(new String[]{argname}, body);
    }
    
    public SNFunc(String[] argnames, SyntaxNode body) {
        body_ = body;
        paramIDs_ = new ParameterID[argnames.length];
        for (int i = 0; i < argnames.length; ++i)
            paramIDs_[i] = new ParameterID(i, argnames[i]);
    }

    private SNFunc(ParameterID[] paramIDs, SyntaxNode body) {
        body_ = body;
        paramIDs_ = paramIDs;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{body_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        return new ExeVal(new ValueFunctionImpl(paramIDs_, body_, env));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        // if F(x) = func(y){f(x,y)} then F'(x) = func(y){f_x(x,y)}
        return new SNDerivative(this, wrtvar, dwrtvar);
        /* commented out on Jul 31 2003. This might have avoided an inf loop, but don't know
        if (funcParams.pop().isEmpty()) {
            // we don't know the type of the parameter yet: so put off derivative for later
            return new SNDerivative(this, wrtvar, dwrtvar);
        }
        Executor[] futureParams = funcParams.pop().peek();
        ParameterDeriv[] params = new ParameterDeriv[paramIDs_.length];
        String[] argnames = new String[paramIDs_.length];
        for (int i = 0; i < paramIDs_.length; ++i) {
            if (wrtvar.isSubparam(paramIDs_[i]))
                params[i] = new ParameterDeriv(paramIDs_[i],
                                               ((ParameterDeriv) env.lookup(wrtvar.name())).node());
            else
                params[i] = new ParameterDeriv(paramIDs_[i],
                                               new SNVal(MB.zero(futureParams[i].type())));
            argnames[i] = paramIDs_[i].name();
        }
        return new SNFunc(paramIDs_,
                          body_.derivative(wrtvar, dwrtvar,
                                           env.extend(argnames, params), funcParams.pop()));
         */
    }

    public SyntaxNode simplify() {
        SyntaxNode body = body_.simplify();
        return new SNFunc(paramIDs_, body);
    }
    
    public String toString() {
        String namesStr = "";
        for (int i = 0; i < paramIDs_.length; ++i)
            namesStr += paramIDs_[i].name() + (i < paramIDs_.length-1 ? " " : "");
        return "(SN-func (" + namesStr + ") {" + body_ + "})";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        String[] argnames = new String[paramIDs_.length];
        ParameterDummy[] argvals = new ParameterDummy[paramIDs_.length];
        for (int i = 0; i < paramIDs_.length; ++i) {
            argnames[i] = paramIDs_[i].name();
            argvals[i] = new ParameterDummy(paramIDs_[i]);
        }
        body_.findDependencies(env.extend(argnames, argvals), deps, ranges);
    }
    
}
