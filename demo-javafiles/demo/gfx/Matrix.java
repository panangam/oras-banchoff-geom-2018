package demo.gfx;

/** 
 * An n by n square matrix. Is able to perform some operations (like multiplication).
 * 
 * @author deigen
 */
public class Matrix extends Object {

    protected  int dimension;

    /**
     * @return the dimension of the matrix (ie. n)
     */
    public  int dimension() {
        return dimension;
    }

    /**
     * The entries of the matrix, indexed by [row][column]
     */
    public  double[][] entries;

    /**
     * Creates a new matrix with the given dimension.
     * @param dimension the number of rows and columns (eg. 3 would be 3 by 3).
     */
    public  Matrix( int dimension ) {
        super();

            this .dimension = dimension;
            this .entries = new double [ dimension ] [ dimension ];
        }

    /**
     * Multiplies a matrix times this. The vales of both matrices are not affected.
     * @param m the matrix to multiply with
     * @return the matrix = m * this, where * stands for matrix multiplication
     */
    public  Matrix multiplyOnLeftBy( Matrix m ) {
        return m .multiplyOnRightBy( this );
    }

    /**
     * Multiplies this times a matrix. The values of both matrices are not affected.
     * @param m the matrix to multiply with
     * @return the matrix = this * m, where * stands for matrix multiplication
     */
    public  Matrix multiplyOnRightBy( Matrix m ) {
        if ( m .dimension() != this .dimension ) {
            System .out .println( " ERROR: Can't multiply a " + dimension + "x" + dimension + " matrix and a " + m .dimension() + "x" + m .dimension() + " matrix." );
            return null;
        }
        Matrix result = new Matrix( dimension );
        for ( int i = 0; i < dimension; i++ ) {
            for ( int j = 0; j < dimension; j++ ) {
                // add up the dot product
                double dotproduct = 0;
                for ( int term = 0; term < dimension; term++ ) {
                    dotproduct += this .entries[i][term] * m .entries[term][j];
                }
                result .entries[i][j] = dotproduct;
            }
        }
        return result;
    }

    /**
     * Performs this * p, where * is matrix multiplication, and p is treated as a column vector
     * Puts the transformed coordinates of p into p.coords; performs p.coords = this * p.untransfomredCoords
     * @param p the point to transform
     */
    public  void transform( Point p ) {
        if ( p .dimension() != this .dimension )
            throw new RuntimeException("dimensions not equal for matrix*point multiplication");
        // multiply this matrix by p on the right:
        for ( int i = 0; i < dimension; i++ ) {
            double dotproduct = 0;
            for ( int term = 0; term < dimension; term++ )
                dotproduct += this .entries[i][term] * p .untransformedCoords[term];
            p .coords[i] = dotproduct;
        }
    }

    /**
     * Sets the values (entries) of this matrix to those of another matrix.
     * @param m the matrix whose values should be used
     */
    public  void set( Matrix m ) {
        if ( m .dimension() != this .dimension() ) {
            throw new RuntimeException( "ERROR: Can't set a " + dimension + "x" + dimension + " matrix to a " + m .dimension() + "x" + m .dimension() + " matrix." );
        }
        for ( int i = 0; i < dimension; i++ ) {
            for ( int j = 0; j < dimension; j++ ) {
                entries[i][j] = m .entries[i][j];
            }
        }
    }

    public  Object clone() {
        Matrix clone = new Matrix( dimension );
        clone .set( this );
        return clone;
    }

    /**
     * @param dimension the dimension of the identity matrix
     * @return the identity matrix of given dimension
     */
    public static final  Matrix IDENTITY( int dimension ) {
        Matrix identity = new Matrix( dimension );
        for ( int i = 0; i < dimension; i++ ) {
            for ( int j = 0; j < dimension; j++ ) {
                identity .entries[i][j] = i == j ? 1 : 0;
            }
        }
        return identity;
    }

    // for debugging
    /**
     * Prints the matrix to System.out. For debugging. Commented out currently.
     */
    public  void print() {
        for ( int i = 0; i < entries .length; i++ ) {
            //System .out .print( "[" );
            for ( int j = 0; j < entries[0] .length; j++ ) {
                //System .out .print( entries[i][j] );
                if ( j < entries[0] .length - 1 ) {
                    //System .out .print( " , " );
                }
            }
            //System .out .println( "]" );
        }
    }


}


