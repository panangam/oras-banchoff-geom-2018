//
//  ValueMatrix.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.value;

import mathbuild.type.Type;
import mathbuild.type.TypeMatrix;

public class ValueMatrix implements Value {

    private Value[][] vals_;

    /**
     * Creates a ValueVector with the given values as components.
     */
    public ValueMatrix(Value[][] vals) {
        vals_ = vals;
    }

    /**
     * Creates a ValueVector with the given values as components.
     * The double values are converted to ValueScalars.
     */
    public ValueMatrix(double[][] vals) {
        vals_ = new ValueScalar[vals.length][vals.length > 0 ? vals[0].length : 0];
        for (int i = 0; i < vals.length; ++i)
            for (int j = 0; j < vals[0].length; ++j)
                vals_[i][j] = new ValueScalar(vals[i][j]);
    }

    /**
     * @return the component values
     */
    public Value[][] vals() {
        return vals_;
    }

    /**
     * Converts this ValueMatrix into an array of double. If this ValueMatrix
     * contains non-scalar types as values, this method will break with a class cast exception
     * @return a double[][] for this matrix, with rows indexed by the first index
     */
    public double[][] doubleVals() {
        double[][] doubleVals = new double[vals_.length][vals_[0].length];
        for (int i = 0; i < doubleVals.length; ++i)
            for (int j = 0; j < doubleVals[i].length; ++j)
                doubleVals[i][j] = ((ValueScalar) vals_[i][j]).number();
        return doubleVals;
    }
    
    /**
     * @param i the row index
     * @param j the column index
     * @return the given component's value
     */
    public Value component(int i, int j) {
        return vals_[i][j];
    }

    /**
     * @return the number of rows in this matrix
     */
    public int numRows() {
        return vals_.length;
    }

    /**
     * @return the number of columns in this matrix
     */
    public int numCols() {
        if (vals_.length == 0)
            return 0;
        return vals_[0].length;
    }

    public Type type() {
        Type[][] types = new Type[vals_.length][];
        for (int i = 0; i < vals_.length; ++i) {
            types[i] = new Type[vals_[i].length];
            for (int j = 0; j < vals_[i].length; ++j) {
                types[i][j] = vals_[i][j].type();
            }
        }
        return new TypeMatrix(types);
    }

    public boolean equals(Value val) {
        if (!(val instanceof ValueMatrix))
            return false;
        if (val instanceof ValueMatrix) {
            Value[][] comps = ((ValueMatrix) val).vals();
            if (comps.length != vals_.length)
                return false;
            for (int i = 0; i < vals_.length; ++i) {
                if (comps[i].length != vals_[i].length)
                    return false;
                for (int j = 0; j < vals_.length; ++j) {
                    if (!vals_[i][j].equals(comps[i][j]))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    public String toString() {
        String str = "[";
        for (int i = 0; i < vals_.length; ++i) {
            for (int j = 0; j < vals_[i].length; ++j) {
                str += vals_[i][j];
                if (j < vals_[i].length - 1)
                    str += " , ";
            }
            if (i < vals_.length - 1)
                str += " ; ";
        }
        str += "]";
        return str;
    }
}
