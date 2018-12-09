package demo.expr;


import mathbuild.*;
import mathbuild.type.*;
import mathbuild.value.*;

import demo.util.*;
import demo.depend.*;
import demo.expr.ste.*;
import demo.exec.*;
import demo.expr.IncompatibleTypeException;

public class Expression extends Object implements Dependable {

    private String definition_;
    protected Environment environment_;
    protected SyntaxNode tree_;
    protected Executor executor_;
    protected Type type_;
    protected Set intervals_;
    private boolean typeChangeAllowed_ = false;
    
    /**
     * Creates a new Expression whose expression is the given parse tree
     * @param expression the expression
     * @param definition the definition string of the expression
     */
    public  Expression( String definition, SyntaxNode expression, Environment env ) {
        super();
            definition_ = new String(definition);
            environment_ = env;
            set( expression );
    }

    /**
     * Creates a new Expression that is identical to the given expression.
     * The parse tree and definition string of this expression will be the
     * same (that is, the pointers will be exactly the same). This expression
     * will be dependent on the objects that the given expression's parse tree
     * is dependent on.
     * Disposing or setting the dependencies of this expression will not affect
     * the given expression, and vice-versa.
     * Whether this expression is allowed to change type is set to whether the
     * given expression is allowed to change type.
     * @param expression the expression
     */
    public  Expression( Expression expression ) {
        this(expression.definitionString(), expression.tree(), expression.environment());
        this.typeChangeAllowed_ = expression.typeChangeAllowed();
    }

    /**
     * Disposes this expression. Removes this expression from the dependency structure.
     * This means nothing will depend on this expression anymore, and this expression
     * will not depend on anything, making it ready to be garbage collected.
     */
    public void dispose() {
        DependencyManager.remove(this);
    }
    
    /**
     * @return the definition string of the expression
     */
    public String definitionString() {
        return definition_;
    }
    
    /** 
     * This method should be used as little as possible.
     * It is supplied only for backwards compatibility with older code that
     * has not been updated to use expressions.
     * @return the root ParseTreeNode of the expression.
     */
    public SyntaxNode tree() {
        return tree_;
    }

    /**
     * @return the environment that this expression was interpreted in.
     */
    public mathbuild.Environment environment() {
        return environment_;
    }

    /**
     * Sets the expression for this Expression to the given parse tree.
     * @param expression the expression
     */
    protected  void set( SyntaxNode expression ) {
        Executor executor = MB.build(expression, environment_);
        Type newType = executor.type();
        if (!typeChangeAllowed_ && type_ != null && !newType.compatibleType(type_))
            throw new IncompatibleTypeException("Expected type " + type_ + " but got " + newType);
        // if we're here, then build worked OK. So set the dependencies:
        Set dependencies = new Set(), ranges = new Set(), intervals = new Set();
        expression.findDependencies(environment_, dependencies, ranges);
        for (java.util.Enumeration rangesEnum = ranges.elements();
             rangesEnum.hasMoreElements();) {
            Object obj = rangesEnum.nextElement();
            if (obj instanceof STEInterval)
                intervals.put(obj);
        }
        for (java.util.Enumeration depsEnum = dependencies.elements();
             depsEnum.hasMoreElements();) {
            Object obj = depsEnum.nextElement();
            if (obj instanceof STEExpression)
                intervals.addObjects(((STEExpression) obj).expression().intervals().elements());
        }
        Exec.begin_nocancel();
        executor_ = executor;
        type_ = newType;
        tree_ = expression;
        DependencyManager.setDependencies(this, dependencies.elements());
        intervals_ = intervals;
        Exec.end_nocancel();
    }

    
    /** 
     * @return whether this expression returns a scalar value
     */
    public boolean returnsScalar() {
        return type_.compatibleType(MB.TYPE_SCALAR);
    }
    
    /**
     * @return whether this expression returns a vector or scalars with the given number of coords
     */
    public boolean returnsVector(int dimension) {
        Type[] types = new Type[dimension];
        for (int i = 0; i < dimension; ++i)
            types[i] = MB.TYPE_SCALAR;
        return type_.compatibleType(new TypeVector(types));
    }
    
    /**
     * @return whether this expression and the given expression have the same return type
     */
    public boolean returnTypeEqual(Expression expr) {
        return type_.compatibleType(expr.type_);
    }
    
    /**
     * @return the number of intervals this expression is dependent on as ranges.
     */
    public int numIntervals() {
        return intervals_.size();
    }

    /**
     * @reutrn the intervals that this expression is dependent on as ranges.
     */
    public Set intervals() {
        return intervals_;
    }

    /**
     * Calculates the expression and returns the result.
     * @return the result of evaluation
     */
    public  Value calculate() {
        return executor_.execute(new Object());
    }
    
    /**
     * Calculates the expression and returns the result.
     * @return the result of evaluation
     */
    public Value evaluate() {
        return executor_.execute(new Object());
    }

    /**
     * Calculates the expression and returns the result.
     * @param the ID for this run of the mathbuild.Executor objects (used for caching)
     * @return the result of evaluation
     */
    public Value execute(Object runID) {
        return executor_.execute(runID);
    }

    /**
     * @return the type of the value obtained from executing/evaluating this expression
     */
    public Type type() {
        return type_;
    }

    /**
     * Sets whether the type of this expression is allowed to change.
     * The type is not allowed to change by default.
     * @param allow whether to allow type changes
     */
    public void setTypeChangeAllowed(boolean allow) {
        typeChangeAllowed_ = allow;
    }

    /**
     * @return whether the type of this Expression is allowed to change
     */
    public boolean typeChangeAllowed() {
        return typeChangeAllowed_;
    }

    public Object clone(Environment env) {
        return new Expression(definition_, tree_, env);
    }
    
    public void dependencyUpdateDef( Set updatingObjects ) {
        set(tree_);
    }
    public void dependencyUpdateVal( Set updatingObjects ) {
    }

    
    
    private DependencyNode __myDependencyNode__ = new DependencyNode(this);
    public DependencyNode dependencyNode() { return __myDependencyNode__; }
    
}
