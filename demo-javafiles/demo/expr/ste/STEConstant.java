package demo.expr.ste;

import mathbuild.Executor;
import mathbuild.value.*;
import mathbuild.type.*;

/**
 * SymbolTableEntry for scalar constants (such as pi).
 *
 * @author deigen
 */
public class STEConstant extends STEValue implements Executor {

    public  STEConstant( String name, Value value ) {
        super();

            this .type = CONSTANT;
            this .name = name;
            this .value = value;
        }

    public  STEConstant( String name, double value ) {
        this( name, new ValueScalar(value) );
    }

    /**
     * The value of this constant.
     */
    public  Value value;

    /**
     * @return the value of this constant
     */
    public  Value value() {
        return value;
    }
    
    /**
     * Sets the value of this constant.
     * @param value the value this constant should have
     */
    public  void setValue( Value value ) {
        this.value = value;
    }

    /**
     * @return the type of the Value obtained from executing this Constant
     */
    public Type type() {
        return value.type();
    }

    protected Value exec(Object runID) {
        return value;
    }


}


