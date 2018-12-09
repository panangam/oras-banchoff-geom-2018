//
//  Modulo.java
//  Demo
//
//  Created by David Eigen on Thu May 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Modulo implements SyntaxNodeConstructorBin, OperatorBinFactory {

    private static final Modulo instance_ = new Modulo();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Modulo inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNModulo(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        if (lt.isType(Type.SCALAR) && rt.isType(Type.SCALAR))
            return new OpModuloScal();
        throw new BuildException("mod can only be used on reals.");
    }


}


class SNModulo extends SNBinOp {

    public SNModulo(SyntaxNode l, SyntaxNode r) {
        super(l, r, Modulo.inst(), Modulo.inst(), "mod");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
         throw new BuildException("Can't take derivative of mod (yet)");
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        return new SNModulo(l,r);
    }

}


class OpModuloScal implements OpBin {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value a, Value b) {
        return new ValueScalar(Math.IEEEremainder(((ValueScalar) a).num(),
                                                  ((ValueScalar) b).num()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}
