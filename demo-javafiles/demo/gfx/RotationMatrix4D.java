package demo.gfx;

/**
 * A 4 by 4 rotation matrix. 
 */
public class RotationMatrix4D extends Matrix4D {

    /**
     * Creates a new matrix that rotates points about the x-axis xRot radians, 
     * about the y-axis yRot radians, and about the z-axis zRot radians
     */
    public 
    RotationMatrix4D( double xRot, double yRot, double zRot ) {
        super();

            // set the rotation matrix to be rotation about x-axis (vertical)
            // times rotation about y-axis (horizontal)
            // times rotation about z-axis (spin)
            double sinx = - Math .sin( xRot ), cosx = Math .cos( xRot );
            double siny = - Math .sin( yRot ), cosy = Math .cos( yRot );
            double sinz = - Math .sin( zRot ), cosz = Math .cos( zRot );
            entries[0][0] = cosy * cosz;
            entries[0][1] = cosy * sinz;
            entries[0][2] = - siny;
            entries[1][0] = sinx * siny * cosz - cosx * sinz;
            entries[1][1] = sinx * siny * sinz + cosx * cosz;
            entries[1][2] = sinx * cosy;
            entries[2][0] = cosx * siny * cosz + sinx * sinz;
            entries[2][1] = cosx * siny * sinz - sinx * cosz;
            entries[2][2] = cosx * cosy;
        }


}


