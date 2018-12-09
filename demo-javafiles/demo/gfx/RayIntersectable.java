//
//  RayIntersectable.java
//  Demo
//
//  Created by David Eigen on Sat Mar 08 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.gfx;

public interface RayIntersectable {

    /**
     * Performs a ray intersection.
     * @param ray the ray to intersect with
     * @param intersection the intersection object to fill in if this object intersects the ray
     * @return whether the ray intersects this object
     */
    public boolean intersect(Ray ray, RayIntersection intersection);
    
}
