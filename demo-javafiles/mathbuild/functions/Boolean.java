//
//  Boolean.java
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
 * Boolean is a "constructor" function for boolean-valued real numbers. Given a real 
 * number x, the Boolean function returns 1 if x > 0 and 0 if x <= 0.
 */
public class Boolean implements SyntaxNodeConstructorUn, OperatorUnFactory {

private static final Boolean instance_ = new Boolean();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Boolean inst() {
        return instance_;
    }
    
    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return new SNBoolean(sn);
    }

    public OpUn makeOperator(Type t) {
        if (t.isType(Type.SCALAR))
            return new OpBoolean();
        throw new BuildException("bool can only be applied to a real.");
    }

}

class SNBoolean extends SNUnOp {

    public SNBoolean(SyntaxNode sn) {
        super(sn, Boolean.inst(), Boolean.inst(), "bool");
    }

    protected SyntaxNode derivative(SyntaxNode doperand) {
        return new SNNumber(0);
    }

}

class OpBoolean implements OpUn {
    public Type type() {
        return MB.TYPE_SCALAR;
    }
    public Value operate(Value val) {
        if (((ValueScalar) val).number() > 0)
            return new ValueScalar(1);
        return new ValueScalar(0);
    }
    public Value operate(Value[] vals) {
        return operate(vals[0]);
    }
}