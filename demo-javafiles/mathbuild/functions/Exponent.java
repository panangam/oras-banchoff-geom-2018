//
//  Exponent.java
//  mathbuild
//
//  Created by David Eigen on Fri Feb 22 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Exponent implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Exponent instance_ = new Exponent();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Exponent inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNExponent(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpExponentScal();
        throw new BuildException("Exponentiation can only be used on reals.");
    }


}


class SNExponent extends SNBinOp {

    public SNExponent(SyntaxNode l, SyntaxNode r) {
        super(l, r, Exponent.inst(), Exponent.inst(), "exponent");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        // return a^(b-1)*a'*b + a^b*ln|a|*b', 
        // where a = left_ (the base) and b = right_ (the exponent)
        return new SNAdd(new SNMultiply(
                            new SNMultiply(
                                new SNExponent(
                                    left_,
                                    new SNSubtract(right_, new SNNumber(1))),
                                dl),
                            right_),
                         new SNMultiply(
                            new SNMultiply(
                                new SNExponent(left_, right_),
                                new SNLn(new SNAbs(left_))),
                            dr));
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (l.isValue(0))
            return new SNNumber(0);
        if (l.isValue(1))
            return new SNNumber(1);
        if (r.isValue(0))
            return new SNNumber(1);
        if (r.isValue(1))
            return l;
        return new SNExponent(l,r);
    }

}


class OpExponentScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(Math.pow(((ValueScalar) a).number(), ((ValueScalar) b).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
