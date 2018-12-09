//
//  Init.java
//  Demo
//
//  Created by David Eigen on Tue Aug 13 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.value.*;
import mathbuild.functions.*;
import mathbuild.impl.*;

import java.io.StringReader;
import java.io.BufferedReader;


public class Init {

    private Environment baseEnv_ = null;

    private static Init instance_ = new Init();
    
    private Init() {
        baseEnv_ = new Environment();
        putBuiltins(baseEnv_);
    }

    /**
     * @return The instance of mathbuild.Init -- Init is a singleton class.
     *         Normally, this method does not have to be used; static methods
     *         exist for most things Init is used for.
     */
    public static Init instance() {
        return instance_;
    }

    /**
     * @return the base environment normally used for mathbuild.
     */
    public static Environment baseEnvironment() {
        return instance_.baseEnv_;
    }

    /**
     * Puts all built-in functions into the given environment. These are all the functions
     * in the mathbuild.functions package, plus the following:
     * grad => gradient
     * pi => Math.PI
     * length => func(x){sqrt(dot(x,x))}
     * unit => func(x){x / length(x)}
     * scale => func(x){x / (x_max - x_min)}
     *
     * In addition, the following funtions are not put into the environment, since they
     * are parsed directly as operators:
     * negate, exponent, multiply, divide, add, subtract, if,
     * less than, greater than, and, or, equals (w/ tolerance)
     */
    private void putBuiltins(Environment env) {
        env.put("cos", makeBuiltinFuncExeUn(Cosine.inst()));
        env.put("sin", makeBuiltinFuncExeUn(new Sine()));
        env.put("tan", makeBuiltinFuncExeUn(Tangent.inst()));
        env.put("pi", new ExeVal(new ValueScalar(Math.PI)));
        env.put("true", new ExeVal(new ValueScalar(1)));
        env.put("false", new ExeVal(new ValueScalar(0)));
        env.put("random", new ExeRandom());
        env.put("ln", makeBuiltinFuncExeUn(Ln.inst()));
        env.put("abs", makeBuiltinFuncExeUn(Abs.inst()));
        env.put("sign", makeBuiltinFuncExeUn(Sign.inst()));
        env.put("acos", makeBuiltinFuncExeUn(Acos.inst()));
        env.put("asin", makeBuiltinFuncExeUn(Asin.inst()));
        env.put("atan", makeBuiltinFuncExeUn(Atan.inst()));
        env.put("cosh", makeBuiltinFuncExeUn(Cosh.inst()));
        env.put("sinh", makeBuiltinFuncExeUn(Sinh.inst()));
        env.put("tanh", makeBuiltinFuncExeUn(Tanh.inst()));
        env.put("sqrt", makeBuiltinFuncExeUn(Sqrt.inst()));
        env.put("cross", makeBuiltinFuncExeBin(Cross.inst()));
        env.put("dot", makeBuiltinFuncExeBin(Dot.inst()));
        env.put("map", makeBuiltinFuncExeBin(Map.inst()));
        env.put("transpose", makeBuiltinFuncExeUn(Transpose.inst()));
        env.put("not", makeBuiltinFuncExeUn(BooleanNot.inst()));
        env.put("fpart", makeBuiltinFuncExeUn(FractionPart.inst()));
        env.put("ipart", makeBuiltinFuncExeUn(IntegerPart.inst()));
        env.put("mod", makeBuiltinFuncExeBin(Modulo.inst()));
        ParameterID gradParam = new ParameterID(0);
        env.put("grad", new ExeVal(new ValueFunctionImpl(
                                        new ParameterID[]{gradParam},
                                        new SNDerivative(new SNParam(gradParam),
                                                         SNDerivative.GRADIENT),
                                        Environment.EMPTY)));
        env.put("length", makeBuiltinFuncExeUn(Length.inst()));
        /*
         ParameterID lengthParam = new ParameterID(0);
        env.put("length", new ExeVal(new ValueFunctionImpl(
                                        new ParameterID[]{lengthParam},
                                        Sqrt.inst().makeSyntaxNode(
                                                Dot.inst().makeSyntaxNode(new SNParam(lengthParam),
                                                                         new SNParam(lengthParam))),
                                        Environment.EMPTY)));
         */
        ParameterID unitParam = new ParameterID(0);
        env.put("unit", new ExeVal(new ValueFunctionImpl(
                                        new ParameterID[]{unitParam},
                                        Divide.inst().makeSyntaxNode(
                                                new SNParam(unitParam),
                                                Sqrt.inst().makeSyntaxNode(
                                                        Dot.inst().makeSyntaxNode(
                                                                new SNParam(unitParam),
                                                                new SNParam(unitParam)))),
                                        Environment.EMPTY)));
        // experimental: . as identity function (so you can say g(x,.) or .^2, etc as funcs)
        //env.put(".", makeBuiltinFuncExeUn(Identity.inst()));
    }

    private Executor makeBuiltinFuncExeUn(SyntaxNodeConstructorUn constructor) {
        ParameterID param = new ParameterID(0);
        return new ExeVal(new ValueFunctionImpl(new ParameterID[]{param},
                                                constructor.makeSyntaxNode(new SNParam(param)),
                                                Environment.EMPTY));
    }

    private Executor makeBuiltinFuncExeBin(SyntaxNodeConstructorBin constructor) {
        ParameterID param1 = new ParameterID(0);
        ParameterID param2 = new ParameterID(1);
        return new ExeVal(new ValueFunctionImpl(new ParameterID[]{param1, param2},
                                                constructor.makeSyntaxNode(new SNParam(param1),
                                                                           new SNParam(param2)),
                                                Environment.EMPTY));
    }
        
}
