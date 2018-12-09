//
//  Length.java
//  Demo
//
//  Created by David Eigen on Mon Apr 21 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Length implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Length instance_ = new Length();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Length inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNLength(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.VECTOR)) {
            return new OpVectorLength(Dot.inst().makeOperator(t,t));
        }
        else if (t.isType(Type.LIST))
            return new OpListLength();
        throw new BuildException("length can only be applied to vectors or lists.");
    }

}

class SNLength extends SNUnOp {

    public SNLength(SyntaxNode sn) {
        super(sn, Length.inst(), Length.inst(), "length");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNDivide(new SNDot(doperand, operand_), new SNLength(operand_));
    }

}

class OpVectorLength implements OpUn {
    private OpBin dot_;
    public OpVectorLength(OpBin dot) {
        dot_ = dot;
        if (!dot.type().isType(Type.SCALAR))
            throw new BuildException("internal error: dot op for vector length must return scalar");
    }
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(Math.sqrt(((ValueScalar) dot_.operate(val,val)).number()));
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}

class OpListLength implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        return new ValueScalar(((ValueList) val).length());
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}

