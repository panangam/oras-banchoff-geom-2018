package demo.coloring;

import demo.*;
import demo.util.*;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * A Coloring calculates a color and returns it. The color calculated can be
 * based on anything. Usually, a Coloring is used to describe the colors of
 * a the polygons from a PlotExpression. In this case, a color is calculated
 * based on the values of any applicable entities in the program. For example,
 * a Coloring can produce different colors depending on the value of an interval.
 * In this case, one would change the value of the interval, and then ask the 
 * Coloring what color is used for this value, using the calculate(.) method.
 *
 * @author deigen
 */
public  abstract class Coloring extends Object implements Dependable, EnvCloneable {

    // constants for hue spectrum to color conversion
    private static final  float SATURATION = 1, BRIGHTNESS = 1;

    // dependency node
    private DependencyNode myDependencyNode_ = new DependencyNode(this);


    /**
     * Calculates and sets any values that do not change while the Coloring is
     * being used, but can change between times that the Coloring is used. For
     * example, the ColoringGradient needs to keep track of the values at which
     * the colors change. This information does not change while the 
     * ColoringGradient is being used to find the colors of the polygons of
     * a PlotExpression. However, this information can be different each time
     * the colors of all the polygons of the PlotExpression needs to be
     * calculated.
     */
    public  abstract  void setCache() ;
    
    /**
     * Calculates the color based on the values of any applicable entities, and
     * returns it.
     * 
     * @return the resulting color
     */
    public  abstract double[] calculate( ) ;

    /**
     * returns an enumeration of all colorings that the coloring is dependent on.
     *
     * @return an enumeration of all the children in the coloring tree of this coloring
     */
    public  abstract  java.util .Enumeration childColorings() ;

    /**
     * Returns the color in an hue spectrum at a given position in the spectrum.
     * The position is caculated by taking spectrumValue mod 1. The color red
     * is at the value 0, then again at the value 1, then again at 2, etc.
     * Between these points of red is the entire spectrum. So the fractional
     * part of the argument spectrumValue will determine the color.
     *
     * @param spectrumValue the position in the spectrum
     * @return the color in the hue spectrum
     */
    protected  double[] colorOf( double spectrumValue ) {
        /* if the value is too small (close to zero) then we could get an 
          * underflow, and the color will become black, even when it should
          * be red. So make adjust spectrumValue if it close to too small
          * (the color difference will not be noticable). */
        if ( (spectrumValue < 1E-10 && spectrumValue >= 0) ||
                (spectrumValue > - 1E-10 && spectrumValue <= 0) ) {
            spectrumValue = 0;
        }
        java.awt .Color color = java.awt .Color .getHSBColor( (float) (spectrumValue % 1),
                                                                SATURATION, BRIGHTNESS );
        return new double []{ ((double) color .getRed()) / 255.0,
                                  ((double) color .getGreen()) / 255.0,
                                  ((double) color .getBlue()) / 255.0,
                                  1
                               };
    }
    
    /**
     * Disposes the coloring. Removes this coloring and all of its child colorings
     * from the program.
     * This involves disposing any Expressions that the Coloring uses.
     */
    public void dispose() {
        DependencyManager.remove(this);
        this.disposeInternal();
        java.util.Enumeration children = childColorings();
        while (children.hasMoreElements()) {
            Coloring child = (Coloring) children.nextElement();
            if (!DependencyManager.hasDependentObjects(child))
                child.dispose();
        }
    }

    /**
     * This method is overridden by subclasses. It is called when they are being disposed,
     * in order to get rid of any expressions they have, or do any other disposing-related
     * cleanup.
     */
    protected abstract void disposeInternal();

    public abstract Object clone(mathbuild.Environment env);

    public void dependencyUpdateDef(Set updatingObjects) {
        // to be overridden by subclasses when necessary
    }

    public void dependencyUpdateVal(Set updatingObjects) {
        // to be overridden by subclasses when necessary
    }

    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }


}


