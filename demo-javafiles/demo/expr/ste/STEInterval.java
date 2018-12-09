package demo.expr.ste;

import mathbuild.*;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.depend.*;
import demo.exec.*;
import demo.expr.Expression;
import demo.util.Set;

/**
 * SymbolTableEntry for intervals.
 *
 * @author deigen
 */
public class STEInterval extends STEValue implements Range, Executor {

    /**
     * the minumum value of the interval
     */
    private  Expression minExpr;

    /**
     * the maximum value of the interval
     */
    private  Expression maxExpr;

    /**
     * the resuolution fo the interval (# of subdivisions)
     */
    private  Expression resolutionExpr;

    /**
     * the current value of the interval
     */
    private  ValueScalar current = new ValueScalar(0);



    /**
     * @param name the name of the interval (eg. "x")
     * @param minExpr the expression for the minimum value
     * @param maxExpr the expression for the maximum value
     * @param resExpr the expression for the resolution (# of subdivisions)
     */
    public
        STEInterval( String name, Expression minExpr, Expression maxExpr, Expression resExpr ) {
            super();

            this .type = INTERVAL;
            this .name = name;
            setExpressions(minExpr, maxExpr, resExpr);
        }


    /**
     * Sets the min, max, and reslution expressions for this entry.
     */
    public void setExpressions(Expression minExpr, Expression maxExpr, Expression resExpr)
    throws demo.depend.CircularException {
        Exec.begin_nocancel();
        Set deps = new Set();
        deps.add(minExpr); deps.add(maxExpr); deps.add(resExpr);
        DependencyManager.setDependencies(this, deps.elements());
        if (this.minExpr != null)
            this.minExpr.dispose();
        if (this.maxExpr != null)
            this.maxExpr.dispose();
        if (this.resolutionExpr != null)
            this.resolutionExpr.dispose();
        this .minExpr = minExpr;
        this .maxExpr = maxExpr;
        this .resolutionExpr = resExpr;
        Exec.end_nocancel();
    }

    /**
     * Executes this expression.
     * @param runID the ID for the execution run (used for caching)
     * @return the Value resulting from executing this Expression
     */
    protected Value exec(Object runID) {
        return current;
    }

    /**
     * @return TypeScalar, which is the type of the Value obtained from executing this Interval
     */
    public Type type() {
        return MB.TYPE_SCALAR;
    }

    /**
     * Calculates the resolution of this interval.
     * @return the resolution
     */
    public Value res(Object runID) {
        double value = ((ValueScalar) resolutionExpr.execute(runID)).number();
        if ( value < 1 || value == Double .NaN
             || value == Double .POSITIVE_INFINITY
             || value == Double .NEGATIVE_INFINITY ) {
            return new ValueScalar(1);
        }
        return new ValueScalar(value);
    }

    /**
     * Calculates the minimum value of this interval.
     * @return the minumum value
     */
    public Value start(Object runID) {
        return minExpr.execute(runID);
    }

    /**
     * Calculates the maximum value of this interval.
     * @return the maximum value
     */
    public Value end(Object runID) {
        return maxExpr.execute(runID);
    }

    /**
     * Calculates the resolution of this interval.
     * @return the resolution
     */
    public  ValueScalar resolution() {
        return (ValueScalar) res(new Object());
    }

    /**
     * Calculates the minimum value of this interval.
     * @return the minumum value
     */
    public  ValueScalar min() {
        return (ValueScalar) start(new Object());
    }

    /**
     * Calculates the maximum value of this interval.
     * @return the maximum value
     */
    public  ValueScalar max() {
        return (ValueScalar) end(new Object());
    }

    /**
     * @return the resolution expression
     */
    public  Expression resExpr() {
        return resolutionExpr;
    }

    /**
     * @return the minumum value expression
     */
    public  Expression minExpr() {
        return minExpr;
    }

    /**
     * @return the maximum value expression
     */
    public  Expression maxExpr() {
        return maxExpr;
    }

    /**
     * @return the definition string of the resolution expression
     */
    public  String resStr() {
        return resolutionExpr.definitionString();
    }

    /**
     * @return the definition string of the minumum value expression
     */
    public  String minStr() {
        return minExpr.definitionString();
    }

    /**
     * @return the definition string of the maximum value expression
     */
    public  String maxStr() {
        return maxExpr.definitionString();
    }

    /**
     * @return the current value of this interval
     */
    public  Value value() {
        return current;
    }

    /**
     * Sets the current value of this interval.
     * @param value the value this interval should have
     */
    public  void setValue( Value value ) {
        if (value instanceof ValueScalar)
            current = (ValueScalar) value;
    }

    /**
     * Sets the current value of this interval.
     * @param value the value this interval should have
     */
    public void setValue( double value ) {
        current = new ValueScalar(value);
    }

    public void dispose() {
        minExpr.dispose();
        maxExpr.dispose();
        resolutionExpr.dispose();
        super.dispose();
    }


}


