//
//  SNUnOp.java
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

public abstract class SNUnOp extends SyntaxNode {

    /**
     * The child node (operand).
     */
    protected SyntaxNode operand_;
    
    private OperatorUnFactory opFactory_;
    private SyntaxNodeConstructorUn snFactory_;
    private String snName_;

    /**
     * Creates a SNUnOp with the specified left and right nodes.
     * @param operand the child node (operand)
     * @param opFactory the operator factory for making the operator
     * @param snFactory the SyntaxNode factory for making this type of syntax node
     * @param name the name of this operator (used for debugging string)
     */
    protected SNUnOp(SyntaxNode operand,
                   OperatorUnFactory opFactory, SyntaxNodeConstructorUn snFactory,
                   String name) {
        operand_ = operand;
        opFactory_ = opFactory;
        snFactory_ = snFactory;
        snName_ = name;
    }

    /**
     * Makes the SyntaxNode for the derivative of this operator.
     * @param doperand the derivative of the operand
     * @return the derivative of this node
     */
    protected abstract SyntaxNode derivative(SyntaxNode doperand);

    /**
     * Performs some simple simplification on this SyntaxNode.
     * This method can be overridden by subclasses. By default, it simply constructs a new
     * SyntaxNode of this type with children l and r.
     * Note that if the simplified operand is a value, SNUnOp's implementation of 
     * simplify(void) automatically returns a SNVal with the appropriate value.
     * @param operand the simplified version of operand_
     * @return the simplified version of this node
     */
    protected SyntaxNode simplify(SyntaxNode operand) {
        return snFactory_.makeSyntaxNode(operand);
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor exe = operand_.build(env, funcParams, buildArgs);
        Type t = exe.type(); 
        if (t.isType(Type.FUNCTION))
            return FuncExeBuilder.buildUnFuncExe(exe, snFactory_);
        return new ExeUn(exe, opFactory_.makeOperator(t));
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        SyntaxNode doperand = operand_.derivative(wrtvar, dwrtvar, env, funcParams);
        return derivative(doperand);
    }

    public SyntaxNode simplify() {
        SyntaxNode operand = operand_.simplify();
        if (operand.isValue())
            return new SNVal(MB.exec(snFactory_.makeSyntaxNode(operand)));
        return simplify(operand);
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{operand_};
    }
    
    public String toString() {
        return "(SN-" + snName_ + " " + operand_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        operand_.findDependencies(env, deps, ranges);
    }

}
