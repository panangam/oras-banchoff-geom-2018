//
//  Transformation.java
//  Demo
//
//  Created by David Eigen on Wed Aug 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

public class Transformation {

    /**
     * the transformation matrix
     */
    public Matrix4D mat;

    /**
     * inverse transpose of the transformation matrix
     */
    public Matrix4D mat_inv_tr;

    
    public Transformation(Matrix4D mat) {
        this.mat = mat;
        mat_inv_tr = mat.inverse().transpose();
    }
    
}
