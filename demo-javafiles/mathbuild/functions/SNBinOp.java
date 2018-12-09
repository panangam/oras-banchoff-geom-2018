//
//  SNBinOp.java
//  mathbuild
//
//  Created by David Eigen on Thu Aug 08 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.value.*;
import mathbuild.type.*;

public abstract class SNBinOp extends SyntaxNode {

    /**
     * the left and right operands
     */
    protected SyntaxNode left_, right_;
    
    private OperatorBinFactory opFactory_;
    private SyntaxNodeConstructorBin snFactory_;
    private String snName_;

    /**
     * Creates a SNBinOp with the specified left and right nodes.
     * @param l the left node
     * @param r the right node
     * @param opFactory the operator factory for making the operator
     * @param snFactory the SyntaxNode factory for making this type of syntax node
     * @param name the name of this operator (used for debugging string)
     */
    protected SNBinOp(SyntaxNode l, SyntaxNode r,
                   OperatorBinFactory opFactory, SyntaxNodeConstructorBin snFactory,
                   String name) {
        left_ = l; right_ = r;
        opFactory_ = opFactory;
        snFactory_ = snFactory;
        snName_ = name;
    }

    /**
     * Makes the SyntaxNode for the derivative of this operator.
     * @param dl the derivative of the left operand
     * @param dr the derivative of the right operand
     */
    protected abstract SyntaxNode derivative(SyntaxNode dl, SyntaxNode dr);

    /**
     * Performs some simple simplification on this SyntaxNode.
     * This method can be overridden by subclasses. By default, it simply constructs a new
     * SyntaxNode of this type with children l and r.
     * Note that if both l and r are values, the SNBinOp's implementation of simplify(void)
     * automatically returns a SNVal with the appropriate value.
     * @param l the simplified version of left_
     * @param r the simplified version of right_
     * @return the simplified version of this node
     */
    protected SyntaxNode simplify(SyntaxNode l, SyntaxNode r) {
        return snFactory_.makeSyntaxNode(l, r);
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor l = left_.build(env, funcParams, buildArgs);
        Executor r = right_.build(env, funcParams, buildArgs);
        Type lt = l.type(); Type rt = r.type();
        if (lt.isType(Type.FUNCTION) || rt.isType(Type.FUNCTION))
            return FuncExeBuilder.buildBinFuncExe(l, r, snFactory_);
        return new ExeBin(l, r, opFactory_.makeOperator(lt, rt));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode dl = left_.derivative(wrtvar, dwrtvar, env, funcParams);
        SyntaxNode dr = right_.derivative(wrtvar, dwrtvar, env, funcParams);
        return derivative(dl, dr);
    }

    public SyntaxNode simplify() {
        SyntaxNode l = left_.simplify();
        SyntaxNode r = right_.simplify();
        if (l.isValue() && r.isValue())
            return new SNVal(MB.exec(snFactory_.makeSyntaxNode(l,r)));
        return simplify(l, r);
    }

    public String toString() {
        return "(SN-" + snName_ + " " + left_ + " " + right_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        left_.findDependencies(env, deps, ranges);
        right_.findDependencies(env, deps, ranges);
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{left_, right_};
    }
    
}
