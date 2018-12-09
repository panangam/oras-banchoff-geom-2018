//
//  Transpose.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

/**
 * Transpose: matrix transpose.
 */
public class Transpose implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Transpose instance_ = new Transpose();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Transpose inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNTranspose(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.MATRIX)) {
            Type[][] types = ((TypeMatrix) t).componentTypes();
            Type[][] newtypes = new Type[types[0].length][types.length];
            for (int i = 0 ; i < types.length; ++i)
                for (int j = 0 ; j < types[0].length ; ++j)
                    newtypes[j][i] = types[i][j];
            return new OpTranspose(new TypeMatrix(newtypes));
        }
        throw new BuildException("transpose can only be applied to a matrix.");
    }

}

class SNTranspose extends SNUnOp {

    public SNTranspose(SyntaxNode sn) {
        super(sn, Transpose.inst(), Transpose.inst(), "transpose");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNTranspose(doperand);
    }

}

class OpTranspose implements OpUn {
    private TypeMatrix type_;
    public OpTranspose(TypeMatrix t) {type_ = t;}
    public Type type() {
        return type_;
    }
    public Value operate(Value val) {
        Value[][] vals = ((ValueMatrix) val).vals();
        Value[][] newvals = new Value[vals[0].length][vals.length];
        for (int i = 0; i < vals.length; ++i)
            for (int j = 0; j < vals[0].length; ++j)
                newvals[j][i] = vals[i][j];
        return new ValueMatrix(newvals);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}