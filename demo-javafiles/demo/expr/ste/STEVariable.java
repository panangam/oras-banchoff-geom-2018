package demo.expr.ste;

import mathbuild.*;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.exec.*;
import demo.depend.DependencyManager;
import demo.util.Set;
import demo.expr.Expression;

/**
 * SymbolTableEntry for variables.
 *
 * @author deigen
 */
public class STEVariable extends STEValue implements Range, Executor {

    /**
     * the minimum value of this variable
     */
    private  Expression minExpr;

    /**
     * the maximum value of this variable
     */
    private  Expression maxExpr;

    /**
     * the resolution (# of steps ) of this variable
     */
    private  Expression resolutionExpr;

    /**
     * the current value
     */
    private  ValueScalar current = new ValueScalar(0);

    private  boolean useCachedValues = false; // whether to use cached values for min,max,res

    // cached values
    private  ValueScalar resolutionCache = new ValueScalar(0);
    private  ValueScalar minCache = new ValueScalar(0);
    private  ValueScalar maxCache = new ValueScalar(0);

    
    /**
     * @param name the name of the variable (eg. "x")
     * @param minStr the definition string of the minumum value (eg. "-10")
     * @param maxStr the definition string of the maximum value (eg. "pi / 2")
     * @param resolutionStr the definition string of the resolution (# of steps)
     * @param minExpr the minimum value
     * @param maxExpr the maximum value
     * @param resExpr the resolution (# of steps when animating)
     */
    public 
    STEVariable( String name, Expression minExpr, Expression maxExpr, Expression resExpr ) {
        super();

            this .type = VARIABLE;
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
     * Sets the current value of this variable.
     * @param value the value this variable should have
     */
    public  void setCurrent( double value ) {
        current = new ValueScalar(value);
    }
    
    /**
     * Sets the current value of this variable.
     * @param value the value this variable should have
     */
    public  void setValue( Value value ) {
        if (value instanceof ValueScalar)
            current = (ValueScalar) value;
    }
    
    /**
     * Sets the current value of this variable.
     * @param value the value this variable should have
     */
    public void setValue( double value ) {
        current = new ValueScalar(value);
    }

    /**
     * @return the current value of this variable
     */
    public  ValueScalar current() {
        return current;
    }

    /**
     * Recalculates the minimum, maximum, and resolution.
     */
    public  void setCache() {
        useCachedValues = false;
        resolutionCache = resolution();
        minCache = min();
        maxCache = max();
        useCachedValues = true;
    }

    /**
     * Makes it so that values are recalculated every time they are requested
     * by the min(), max(), or resolution() methods.
     */
    public  void unCache() {
        useCachedValues = false;
    }

    /**
     * @return TypeScalar, which is the type of the Value obtained from executing this Variable
     */
    public Type type() {
        return MB.TYPE_SCALAR;
    }

    protected Value exec(Object runID) {
        return current;
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
     * @return the current value of this variable
     */
    public  Value value() {
        return current;
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


    
    public void dependencyUpdateDef( Set updatingObjects ) {
        if ( useCachedValues )
            setCache();
    }
    
    public void dependencyUpdateVal( Set updatingObjects ) {
        if ( useCachedValues )
            setCache();
    }

    public void dispose() {
        minExpr.dispose();
        maxExpr.dispose();
        resolutionExpr.dispose();
        super.dispose();
    }
    

}


