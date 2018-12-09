package demo.expr;

import mathbuild.*;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.exec.*;
import demo.depend.*;
import demo.expr.ste.STEInterval;
import demo.util.Set;

/**
 * An IntervalExpression is an expression that depends on intervals.
 * There are routines to keep track of the intervals in ways that are useful, especially for plots.
 * 
 * @author deigen
 */
public class IntervalExpression extends Expression {

    private  STEInterval[] sortedIntervals_;

    /**
     * Creates a new IntervalExpression whose expression is the given expression
     * @param expression the expression
     */
    public  IntervalExpression( Expression expr ) {
        super(expr);
    }
    
    /**
     * Creates a new IntervalExpression whose expression is the given parse tree
     * @param definition the definition string of the expression
     * @param expression the expression
     * @param env the environment that the expression is interpreted in
     */
    public  IntervalExpression( String definition, SyntaxNode expression, Environment env ) {
        super(definition, expression, env);
    }

    /**
     * Sets the expression for this IntervalExpression to the given parse tree.
     * @param expression the expression
     */
    protected  void set( SyntaxNode expression ) {
        super.set(expression);
        getIntervals();
    }

    /**
     * @param expr the expression to check
     * @return whether the given expression uses (is dependent on) the same intervals as this expression
     */
    public 
    boolean containsSameIntervalsAs( IntervalExpression expr ) {
        if ( expr .numIntervals() != this .numIntervals() ) {
            return false;
        }
        for ( java.util.Enumeration otherIntervals = expr.intervals().elements();
              otherIntervals.hasMoreElements();) {
            if ( ! this .intervals_ .contains( otherIntervals.nextElement() ) ) {
                return false;
            }
        }
        return true;
    }



    /**
     * @return the intervals this expression is dependent on, sorted by dependency
     */
    public  STEInterval[] sortedIntervals() {
        return sortedIntervals_;
    }


    public Object clone(Environment env) {
        return new IntervalExpression((Expression) super.clone(env));
    }

    private  void getIntervals() {
        Exec.begin_nocancel();
        sortedIntervals_ = new STEInterval[intervals_.size()];
        DependencyManager.sortByDependency(intervals_).copyInto(sortedIntervals_);
        Exec.end_nocancel();
    }

    public void dependencyUpdateDef( Set updatingObjects ) {
        super.dependencyUpdateDef(updatingObjects);
        getIntervals();
    }


}


