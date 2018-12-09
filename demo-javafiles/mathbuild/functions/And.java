//
//  And.java
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

public class And implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final And instance_ = new And();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static And inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNAnd(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpAndScal();
        throw new BuildException("And can only be used on reals.");
    }


}


class SNAnd extends SNBinOp {

    public SNAnd(SyntaxNode l, SyntaxNode r) {
        super(l, r, And.inst(), And.inst(), "and");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNNumber(0);
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        if (l.isValue()) {
            Value lval = MB.exec(l);
            if ( ! (lval instanceof ValueScalar) )
                throw new BuildException("and can only be used on reals.");
            if (((ValueScalar) lval).number() > 0)
                return new SNBoolean(r);
            return new SNNumber(0);
        }
        if (r.isValue()) {
            Value rval = MB.exec(r);
            if ( ! (rval instanceof ValueScalar) )
                throw new BuildException("and can only be used on reals.");
            if (((ValueScalar) rval).number() > 0)
                return new SNBoolean(l);
            return new SNNumber(0);
        }
        return new SNAnd(l,r);
    }


}


class OpAndScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        if (((ValueScalar) a).number() > 0 && ((ValueScalar) b).number() > 0)
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
