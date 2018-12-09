//
//  DemoColor.java
//  Demo
//
//  Created by David Eigen on Sat Jun 01 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.util;

import demo.graph.ZBufferedImage;

public class DemoColor {

    public double red, green, blue, alpha;

    /**
     * Creates a color with rgba given in 0..1
     */
    public DemoColor(double r, double g, double b, double a) {
        red = r; green = g; blue = b; alpha = a;
    }

    /**
     * Creates a color with rgb given by the java.awt.Color
     */
    public DemoColor(java.awt.Color color) {
        this(color, 1);
    }
    
    /**
     * @param color the color
     * @param a the alpha component, in 0..1
     * Creates a color with rgb given by the java.awt.Color and alpha in 0..1
     */
    public DemoColor(java.awt.Color color, double a) {
        red = ((double) color.getRed()) / 255.0;
        green = ((double) color.getGreen()) / 255.0;
        blue = ((double) color.getBlue()) / 255.0;
        alpha = a;
    }

    public DemoColor(DemoColor c) {
        red = c.red; green = c.green; blue = c.blue; alpha = c.alpha;
    }

    /**
     * construcs a DemoColor from an array of alpha*red alpha*green alpha*blue alpha, in 0..1
     * NOTE: in this program, double[] colors have alpha premultiplied!!!
     * DemoColor does NOT have alpha premultiplied.
     * @pamam c color in rgba, rgba each in 0..1
     */
    public DemoColor(double[] c) {
        red = c[0]/c[3]; green = c[1]/c[3]; blue = c[2]/c[3]; alpha = c[3];
    }

    /**
     * @return a java.awt.Color with the rgb of this color
     */
    public java.awt.Color awtColor() {
        return new java.awt.Color((float) red, (float) green, (float) blue);
    }

    /**
     * @return a ZBufferImage format color with the rgba of this color
     */
    public int zBufferColor() {
        return ZBufferedImage.convertColor(this);
    }

    /**
     * @return a ZBufferImage format color with the rgba of this color
     */
    public int zbufferColor() {
        return ZBufferedImage.convertColor(this);
    }

    /**
     * @return this color in the color format used by Colorings (ar,ag,ab,a)
     */
    public double[] coloringColor() {
        return new double[]{
            alpha * red,
            alpha * green,
            alpha * blue,
            alpha
        };
    }

    /**
     * @return the color scaled by the given scalar. Does not scale alpha component.
     */
    public DemoColor scale(double s) {
        return new DemoColor(red*s, green*s, blue*s, alpha);
    }

    /**
     * Adds this color to the given color, including alpha components.
     * Returns the new color; this color itself is left untouched.
     */
    public DemoColor add(DemoColor c) {
        return new DemoColor(red+c.red, green+c.green, blue+c.blue, alpha+c.alpha);
    }

    /**
     * Clamps this color so red, green, blue and alpha are all in [0,1].
     */
    public DemoColor clamp() {
        return new DemoColor(red   < 0 ? 0 : red   > 1 ? 1 : red,
                             green < 0 ? 0 : green > 1 ? 1 : green,
                             blue  < 0 ? 0 : blue  > 1 ? 1 : blue,
                             alpha < 0 ? 0 : alpha > 1 ? 1 : alpha);
    }

    /**
     * Linearly between two colors.
     * If t is 0, the result is c1. If t is 1, the result is c2.
     */
    public static DemoColor interpolate(DemoColor c1, DemoColor c2, double t) {
        return new DemoColor(c1.red   + t*(c2.red   - c1.red  ),
                             c1.green + t*(c2.green - c1.green),
                             c1.blue  + t*(c2.blue  - c1.blue ),
                             c1.alpha + t*(c2.alpha - c1.alpha));
    }

}
