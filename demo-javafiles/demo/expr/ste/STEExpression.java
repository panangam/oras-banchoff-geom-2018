package demo.expr.ste;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.depend.*;
import demo.exec.*;
import demo.expr.Expression;

public class STEExpression extends STEValue implements mathbuild.Executor {

    /**
     * the expression of the function (eg. x^2 if expr = x^2)
     */
    protected  Expression expression;

    /**
     * @param name the name of the function (eg. "f")
     * @param expression the root node of the expression for the function (eg. x^2 if f(x) = x^2)
     */
    public 
    STEExpression( String name, Expression expression ) {
        super();

            this.name = name;
            this.type = EXPRESSION;
            this .expression = expression;
            expression.setTypeChangeAllowed(true);
            DependencyManager.setDependency(this, expression);
        }

    /**
     * Evaluates the expression.
     * @return the result of evaluating the expression
     */
    public  Value value() {
        return expression.evaluate();
    }
    
    /**
     * Sets the current value of this symbol -- DEPRICATED
     * @param value the value this symbol should have as its current value
     */
    public  void setValue( Value value ) {
        throw new RuntimeException("Cannot do setValue on a STEExpression");
    }

    /**
     * @return the expression this STEExpression evaluates
     */
    public Expression expression() {
        return this.expression;
    }

    /**
     * Sets the expression this STEExpression evaluates.
     */
    public void setExpression(Expression expression) throws demo.depend.CircularException {
        Exec.begin_nocancel();
        if (this.expression != null)
            DependencyManager.removeDependency(this, this.expression);
        try {
            DependencyManager.setDependency(this, expression);
        }
        catch (CircularException ex) {
            DependencyManager.setDependency(this, this.expression);
            throw ex;
        }
        this.expression.dispose();
        this.expression = expression;
        expression.setTypeChangeAllowed(true);
        Exec.end_nocancel();
    }
    

    /**
     * @return the definition string of the expression
     */
    public String expressionDef() {
        return expression.definitionString();
    }


    /**
     * Executes this expression.
     * @param runID the ID for the execution run (used for caching)
     * @return the Value resulting from executing this Expression
     */
    protected Value exec(Object runID) {
        return expression.execute(runID);
    }
    
    /**
     * @return the type of the value returned on execution
     */
    public Type type() {
        return expression.type();
    }

    public void dispose() {
        expression.dispose();
        super.dispose();
    }
    
}
