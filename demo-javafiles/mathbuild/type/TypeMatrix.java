//
//  TypeMatrix.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.type;

import mathbuild.MB;

public class TypeMatrix extends Type {

    Type[][] types_;

    public TypeMatrix(Type[][] components) {
        super(MATRIX);
        types_ = components;
    }

    /**
     * Constructs a matrix type with all Scalar entries.
     */
    public TypeMatrix(int rows, int cols) {
        super(MATRIX);
        types_ = new TypeMatrix[rows][cols];
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j)
                types_[i][j] = MB.TYPE_SCALAR;
    }

    /**
     * @return the number of rows in the matrix
     */
    public int numRows() {
        return types_.length;
    }

    /**
     * @return the number of columns in the matrix
     */
    public int numCols() {
        if (types_.length == 0) return 0;
        return types_[0].length;
    }
    
    /**
     * @return the component types
     */
    public Type[][] componentTypes() {
        return types_;
    }

    /**
     * @param i the row index
     * @param j the column index
     * @return the given component type
     */
    public Type componentType(int i, int j) {
        return types_[i][j];
    }



    public boolean isType(int type) {
        return type == MATRIX;
    }

    public boolean isType(Type type) {
        if (!(type instanceof TypeMatrix)) return false;
        Type[][] comps = ((TypeMatrix) type).componentTypes();
        if (types_.length != comps.length)
            return false;
        for (int i = 0; i < comps.length; ++i) {
            if (types_[i].length != comps[i].length)
                return false;
            for (int j = 0; j < comps[i].length; ++j) {
                if (!types_[i][j].isType(comps[i][j]))
                    return false;
            }
        }
        return true;
    }

    public boolean compatibleType(Type type) {
        if (!(type instanceof TypeMatrix)) return false;
        Type[][] comps = ((TypeMatrix) type).componentTypes();
        if (types_.length != comps.length)
            return false;
        for (int i = 0; i < comps.length; ++i) {
            if (types_[i].length != comps[i].length)
                return false;
            for (int j = 0; j < comps[i].length; ++j) {
                if (!types_[i][j].compatibleType(comps[i][j]))
                    return false;
            }
        }
        return true;
    }

    public boolean containsType(int type) {
        if (type == MATRIX)
            return true;
        for (int i = 0; i < types_.length; ++i)
            for (int j = 0; j < types_[i].length; ++j)
                if (types_[i][j].containsType(type))
                    return true;
        return false;
    }

    public boolean containsType(Type type) {
        for (int i = 0; i < types_.length; ++i)
            for (int j = 0; j < types_[i].length; ++j)
                if (types_[i][j].containsType(type))
                    return true;
        return isType(type);
    }


    public String toString() {
        String str = "[";
        for (int i = 0; i < types_.length; ++i)
            for (int j = 0; j < types_[i].length; ++j)
                str += types_[i][j] + ( j < types_[i].length - 1 ? ", "
                                         : i < types_.length - 1 ? " ; "
                                                                 : "]" );
        return str;
    }
    
    
}
