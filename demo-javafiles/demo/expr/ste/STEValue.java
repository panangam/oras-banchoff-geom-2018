package demo.expr.ste;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

/**
 * Abstract superclass for SymbolTableEntries that have scalar values.
 *
 * @author deigen
 */
public  abstract class STEValue extends SymbolTableEntry {

    /**
    * @return the current value of this symbol (possibly cached)
     */
    public  abstract  Value value() ;


    /**
    * Sets the current value of this symbol -- DEPRICATED
     * @param value the value this symbol should have as its current value
     */
    public  abstract  void setValue( Value value ) ;


}


