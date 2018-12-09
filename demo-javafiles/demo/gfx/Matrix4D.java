package demo.gfx;
//
//  Matrix4D.java
//  Demo
//
//  Created by David Eigen on Sun Jun 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import demo.io.*;

/**
 * A 4D transformation matrix. Faster than a general Matrix, and has more functionality.
 * Can multiply 3D points -- it multiplies as if the 4th coord of the 3d point is 1
 *
 * @author deigen
 */
public class Matrix4D extends Matrix{

    public Matrix4D() {
        super(4);
        entries[0][0] = entries[1][1] = entries[2][2] = entries[3][3] = 1;
    }

    public Matrix4D(double[] entries) {
        super(4);
        int p = 0;
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                this.entries[i][j] = entries[p++];
    }

    public Matrix4D(double[][] entries) {
        super(4);
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                this.entries[i][j] = entries[i][j];
    }

    public Matrix4D(double a, double b, double c, double d,
                    double e, double f, double g, double h,
                    double i, double j, double k, double l,
                    double m, double n, double o, double p) {
        super(4);
        entries[0][0] = a;
        entries[0][1] = b;
        entries[0][2] = c;
        entries[0][3] = d;
        entries[1][0] = e;
        entries[1][1] = f;
        entries[1][2] = g;
        entries[1][3] = h;
        entries[2][0] = i;
        entries[2][1] = j;
        entries[2][2] = k;
        entries[2][3] = l;
        entries[3][0] = m;
        entries[3][1] = n;
        entries[3][2] = o;
        entries[3][3] = p;
    }

    /**
     * Multiplies a matrix times this. The vales of both matrices are not affected.
     * @param m the matrix to multiply with
     * @return the matrix = m * this, where * stands for matrix multiplication
     */
    public  Matrix4D multiplyOnLeftBy( Matrix4D m ) {
        return m .multiplyOnRightBy( this );
    }

    /**
     * Multiplies this times a matrix. The values of both matrices are not affected.
     * @param m the matrix to multiply with
     * @return the matrix = this * m, where * stands for matrix multiplication
     */
    public  Matrix4D multiplyOnRightBy( Matrix4D m ) {
        Matrix4D result = new Matrix4D( );
        for ( int i = 0; i < 4; i++ ) {
            for ( int j = 0; j < 4; j++ ) {
                // add up the dot product
                double dotproduct = 0;
                for ( int term = 0; term < 4; term++ ) {
                    dotproduct += this .entries[i][term] * m .entries[term][j];
                }
                result .entries[i][j] = dotproduct;
            }
        }
        return result;
    }

    public void transform( Point p ) {
        if ( p .dimension() != 3 )
            throw new RuntimeException("Can mutiply a 4D matrix by a 3D point only");
        // multiply this matrix by p on the right:
        double[] pcoords = p .untransformedCoords;
        double w = (entries[3][0] * pcoords[0] +
                    entries[3][1] * pcoords[1] +
                    entries[3][2] * pcoords[2] +
                    entries[3][3]);
        p .coords[0] = (entries[0][0] * pcoords[0] +
                        entries[0][1] * pcoords[1] +
                        entries[0][2] * pcoords[2] +
                        entries[0][3]) / w;
        p .coords[1] = (entries[1][0] * pcoords[0] +
                        entries[1][1] * pcoords[1] +
                        entries[1][2] * pcoords[2] +
                        entries[1][3]) / w;
        p .coords[2] = (entries[2][0] * pcoords[0] +
                        entries[2][1] * pcoords[1] +
                        entries[2][2] * pcoords[2] +
                        entries[2][3]) / w;
    }

    /*
     * Transforms the point or vector x. The length of x should be 4.
     */
    public double[] transform(double[] x) {
        if (x.length != 4)
            throw new RuntimeException("Length of arg must be 4");
        double[] y = new double[4];
        for (int i = 0; i < 4; ++i) {
            double sum = 0;
            for (int j = 0; j < 4; ++j)
                sum += entries[i][j] * x[j];
            y[i] = sum;
        }
        return y;
    }

    /**
     * Transforms the 4-elt vector x, putting the result into the vector dst.
     */
    public void transform(double[] x, double[] dst) {
        if (x.length != 4)
            throw new RuntimeException("Length of arg must be 4");
        for (int i = 0; i < 4; ++i) {
            double sum = 0;
            for (int j = 0; j < 4; ++j)
                sum += entries[i][j] * x[j];
            dst[i] = sum;
        }
    }


    public Matrix4D transpose() {
        double[][] tr = new double[4][4];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                tr[i][j] = entries[j][i];
        return new Matrix4D(tr);
    }

    private static final double EPSILON = 1e-10;
    public Matrix4D inverse() {
        Matrix4D inv = new Matrix4D(); // init to identity
        Matrix4D a = new Matrix4D();
        a.set(this); // need to modify the matrix w/ operations, so make a copy
        for (int topRow = 0; topRow < 4; ++topRow) {
            // step 1: find leftmost col not entirely zeros
            int col=0;
            int row=0;
            for (col = 0; col < 4; ++col) {
                for (row = topRow; row < 4; ++row)
                    if ( Math.abs(a.entries[row][col]) > EPSILON )
                        break;
                if (row < 4)
                    break;
            }
            if (col == 4)
                // non-invertable matrix
                throw new RuntimeException("inverse() called on non-invertable matrix");
            // interchange top row with row, if necessary
            if (row != topRow) {
                double tempa;
                double tempinv;
                for (int j = 0; j < 4; ++j) {
                    tempa = a.entries[topRow][j];
                    a.entries[topRow][j] = a.entries[row][j];
                    a.entries[row][j] = tempa;
                    tempinv = inv.entries[topRow][j];
                    inv.entries[topRow][j] = inv.entries[row][j];
                    inv.entries[row][j] = tempinv;
                }
            }
            // multiply first row by 1/x, where x = a(topRow,col)
            double x = 1/a.entries[topRow][col];
            for (int j = 0; j < 4; ++j) {
                a.entries[topRow][j] = a.entries[topRow][j] * x;
                inv.entries[topRow][j] = inv.entries[topRow][j] * x;
            }
            // add multiples of top row to all rows below so
            // all entries in cols below the 1 are 0
            for (int i = topRow + 1; i < 4; ++i) {
                double mult = -a.entries[i][col];
                for (int j = 0; j < 4; ++j) {
                    a.entries[i][j] = a.entries[i][j] + mult * a.entries[topRow][j];
                    inv.entries[i][j] = inv.entries[i][j] + mult * inv.entries[topRow][j];
                }
            }
        }
        // put zeros above leading ones by adding mults of rows going upwards
        for (int bottomRow = 3; bottomRow >= 0; --bottomRow) {
            // find 1 position
            int col;
            for (col = 0; col < 4; ++col)
                if (Math.abs(a.entries[bottomRow][col] - 1) <= EPSILON)
                    break;
            if (col == 4) continue;
            for (int i = bottomRow - 1; i >= 0; --i) {
                double mult = -a.entries[i][col];
                for (int j = 0; j < 4; ++j) {
                    a.entries[i][j] = a.entries[i][j] + mult * a.entries[bottomRow][j];
                    inv.entries[i][j] = inv.entries[i][j] + mult * inv.entries[bottomRow][j];
                }
            }
        }
        return inv;
    }

    public String toString() {
        String str = "";
        for (int i = 0; i < entries.length; ++i) {
            for (int j = 0; j < entries[i].length; ++j)
                str += entries[i][j] + (j < entries[i].length - 1 ? ", " : "");
            str += (i < entries.length - 1 ? " ; " : "");
        }
        return str;
    }


    // ****************************** FILE I/O ****************************** //

    public Matrix4D(Token tok, FileParser parser) {
        super(4);
        double[] list = parser.parseNumberList(tok);
        if (list.length == 16) {
            for (int i = 0; i < 4; ++i)
                for (int j = 0; j < 4; ++j)
                    entries[i][j] = list[i*4+j];
        }
        else if (list.length == 9) {
            entries[0][3] = entries[1][3] = entries[2][3] = entries[3][0] = entries[3][1] = entries[3][2] = 0;
            entries[3][3] = 1;
            for (int i = 0; i < 3; ++i)
                for (int j = 0; j < 3; ++j)
                    entries[i][j] = list[i*3+j];
        }
        else {
            parser.error("transformation matrix must have 9 or 16 entries");
        }
    }

    public Token saveFile(FileGenerator generator) {
        double[] list = new double[16];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                list[i*4+j] = entries[i][j];
        return generator.generateNumberList(list);
    }    
}
