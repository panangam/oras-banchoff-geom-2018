//
//  Cross.java
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

public class Cross implements SyntaxNodeConstructorBin, OperatorBinFactory {

private static final Cross instance_ = new Cross();
    /**
    * Returns the (singleton) instance of this class.
     */
    public static Cross inst() {
        return instance_;
    }

    /**
     * Makes a syntax node for an multiplication operation.
     * Does dot product in the case of vectors, matrix multiplcation for matrices.
     * @param left the left child node
     * @param right the right child node
     * @return a SyntaxNode for multiplcation
     */
    public SyntaxNode makeSyntaxNode(SyntaxNode left, SyntaxNode right) {
        return new SNCross(left, right);
    }


    public OpBin makeOperator(Type lt, Type rt) {
        final Type vectype3 = new TypeVector(new Type[]{MB.TYPE_SCALAR, MB.TYPE_SCALAR, MB.TYPE_SCALAR});
        final Type vectype2 = new TypeVector(new Type[]{MB.TYPE_SCALAR, MB.TYPE_SCALAR});
        if (lt.isType(vectype3) && rt.isType(vectype3))
            return new OpCross3();
        if (lt.isType(vectype2) && rt.isType(vectype2))
            return new OpCross2();
        throw new BuildException("Cross product can only be performed on 3D or 2D vectors.");
    }

    
}


class SNCross extends SNBinOp {

    public SNCross(SyntaxNode l, SyntaxNode r) {
        super(l, r, new Cross(), new Cross(), "cross");
    }

    protected SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr) {
        return new SNAdd(new SNCross(left_, dr), new SNCross(dl, right_));
    }

    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        final Value ZERO3 = new ValueVector(new double[]{0,0,0});
        if (l.isValue(ZERO3) || r.isValue(ZERO3))
            return new SNVal(ZERO3);
        final Value ZERO2 = new ValueVector(new double[]{0,0});
        if (l.isValue(ZERO2) || r.isValue(ZERO2))
            return new SNVal(ZERO2);
        return new SNCross(l,r);
    }

}


class OpCross3 implements OpBin {
    public Type type() {
        return new TypeVector(new Type[]{MB.TYPE_SCALAR, MB.TYPE_SCALAR, MB.TYPE_SCALAR});
    }
    public Value operate(Value a, Value b) {
        ValueVector av = (ValueVector) a;
        ValueVector bv = (ValueVector) b;
        return new ValueVector(new Value[]{
            new ValueScalar(
              ((ValueScalar) av.component(1)).number()*((ValueScalar)  bv.component(2)).number()
            - ((ValueScalar) bv.component(1)).number()*((ValueScalar)  av.component(2)).number()),
            new ValueScalar(
              ((ValueScalar) av.component(2)).number()*((ValueScalar)  bv.component(0)).number()
            - ((ValueScalar) bv.component(2)).number()*((ValueScalar)  av.component(0)).number()),
            new ValueScalar(
              ((ValueScalar) av.component(0)).number()*((ValueScalar)  bv.component(1)).number()
            - ((ValueScalar) bv.component(0)).number()*((ValueScalar)  av.component(1)).number())});
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}

class OpCross2 implements OpBin {
    public Type type() {
        return new TypeVector(new Type[]{MB.TYPE_SCALAR, MB.TYPE_SCALAR, MB.TYPE_SCALAR});
    }
    public Value operate(Value a, Value b) {
        ValueVector av = (ValueVector) a;
        ValueVector bv = (ValueVector) b;
        return new ValueVector(new Value[]{
            new ValueScalar(0),
            new ValueScalar(0),
            new ValueScalar(
                ((ValueScalar) av.component(0)).number()*((ValueScalar)  bv.component(1)).number()
              - ((ValueScalar) bv.component(0)).number()*((ValueScalar)  av.component(1)).number())});
    }
    public Value operate(Value[] vals) {
        return operate(vals[0], vals[1]);
    }
}

