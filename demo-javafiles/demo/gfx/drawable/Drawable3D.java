package demo.gfx.drawable;

import demo.graph.ZBufferedImage;
import demo.gfx.PointSortable;
import demo.util.DemoColor;

/** 
 * Interface implemented by anything that can be drawn to a java.awt.Graphics and ZBufferedImage.
 * For example, Polygons implement Drawable3D.
 * The object implementing Drawable3D should draw only if it has not already been drawn. That is,
 * if a draw method was just called on it, it should not draw again if a draw method is called.
 * To make the object able to be drawn again, call resetDrewState().
 *
 * @author deigen
 */
public interface Drawable3D{

	public void drawProjectedOpen( java.awt .Graphics g ) ;

	public void drawProjectedFilledFramed( java.awt .Graphics g ) ;

	public void drawProjectedFilled( java.awt .Graphics g ) ;

	public void drawProjectedSuspended( java.awt .Graphics g ) ;

	public void drawProjectedOpen( ZBufferedImage img ) ;

	public void drawProjectedFilledFramed( ZBufferedImage img ) ;

	public void drawProjectedFilled( ZBufferedImage img ) ;

	public void drawProjectedSuspended( ZBufferedImage img ) ;

        /**
         * Sets the color of this object.
         * This may or may not have any affect, depending on the
         * class implementing Drawable3D.
         * @param color the color this object should be drawn in
         */
        public void setColor(DemoColor color);

        /**
         * @return the color of this object. If this object is not given a color,
         *         returns some approximation.
         */
        public DemoColor color();

        /**
         * Resets the drew state of this Drawable object to false, so
         * the next time a draw method is called on this object, the 
         * object will be drawn.
         */
	public void resetDrewState() ;
        
        /**
         * Returns a PointSortable from this object whose z value is
         * the max z value of this object.
         */
        public PointSortable zmaxPoint() ;

        /**
         * Returns whether this object has any transparency (alpha < 1).
         */
        public boolean isTransparent() ;


}


