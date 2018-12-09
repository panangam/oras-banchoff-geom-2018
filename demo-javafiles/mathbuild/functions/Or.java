//
//  Or.java
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

public class Or implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Or instance_ = new Or();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Or inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNOr(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpOrScal();
        throw new BuildException("Or can only be used on reals.");
    }


}


class SNOr extends SNBinOp {

    public SNOr(SyntaxNode l, SyntaxNode r) {
        super(l, r, Or.inst(), Or.inst(), "or");
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
                return new SNNumber(1);
            return new SNBoolean(r);
        }
        if (r.isValue()) {
            Value rval = MB.exec(r);
            if ( ! (rval instanceof ValueScalar) )
                throw new BuildException("and can only be used on reals.");
            if (((ValueScalar) rval).number() > 0)
                return new SNNumber(1);
            return new SNBoolean(r);
        }
        return new SNOr(l,r);
    }    

}


class OpOrScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        if (((ValueScalar) a).number() > 0 || ((ValueScalar) b).number() > 0)
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
