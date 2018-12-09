package demo.gfx.drawable;

import demo.util.*;
import demo.gfx.*;
import demo.ui.Graphics2DSettings;
import demo.graph.ZBufferedImage;
import demo.Demo;

/**
 * A TextDrawable is text that can be drawn.
 * Text is always drawn parallel to the xy plane. The z coordinate is which plane the text is in.
 *
 * @author deigen
 */
public class TextDrawable implements Drawable3D{

    /**
     * @param text the text this TextDrawable should draw
     * @param point the upper-left corner of the text
     * @param color the color of the text
     */
    public
    TextDrawable( String text, Point point, DemoColor color ) {
        super();

            this .text = text;
            this .point = point;
            this .isNaN = point.isNaN();
            if (point instanceof PointSortable)
                ((PointSortable) point).addObject(this);
            initColor(color);
        }

    private void initColor(DemoColor color) {
        this .color = color;
        this .awtColor = color.awtColor();
        // create image for z-buffered drawing
        if ( ! System.getProperty("java.version").substring(0,3).equals("1.0") ) {
            java.awt.Image img = Demo.createImage(300,100);
            java.awt.Graphics g = img.getGraphics();
            g.setColor(java.awt.Color.white);
            g.fillRect(0,0,300,100);
            g.setColor(java.awt.Color.black);
            // turn off text antialiasing, if there is Java 2
            if ( Demo.javaVersionIsGreaterThanOrEqualTo(new int[]{1,2}) ) {
                try {
                    Class.forName("java.awt.Graphics2D"); // make sure Graphics2D exists
                    Graphics2DSettings.setTextAntialiasing( g, false );
                } catch (Throwable ex) {
                    // not java 2, or some other error -- don't worry about it
                }
            }
            g.drawString(text, 0, g.getFontMetrics().getHeight());
            // put the string into the array
            int ztransparent = ZBufferedImage.convertColor(new double[]{0,0,0,0});
            int zcolor = color.zBufferColor();
            zbufferImageWidth = Math.min(
                                         300, g.getFontMetrics().charsWidth(text.toCharArray(), 0, text.length()));
            if (zbufferImageWidth <= 0) zbufferImageWidth = 1;
            zbufferImageHeight = (int) Math.min(100, Math.ceil(g.getFontMetrics().getHeight() * 1.5));
            zbufferImage = new int[zbufferImageWidth * zbufferImageHeight];
            try {
                java.awt.image.PixelGrabber pixGrabber = new java.awt.image.PixelGrabber(
                                                                                         img, 0,0, zbufferImageWidth,zbufferImageHeight, zbufferImage, 0, zbufferImageWidth );
                pixGrabber.grabPixels();
                for (int i = 0; i < zbufferImage.length; ++i)
                    if (pixGrabber.getColorModel().getRed(zbufferImage[i]) == 0)
                        zbufferImage[i] = zcolor;
                    else
                        zbufferImage[i] = ztransparent;
            } catch (InterruptedException ex) {}
            img.flush();
            img = null;
        }
    }        

    private  Point point;

    private  String text;

    private  DemoColor color;

    private  boolean drew = false;

    private  java.awt.Color awtColor;

    private  boolean isNaN;

    /**
     * Distance that the top-left of the String should be drawn from the point (in pixels)
     */
    private static final int DISTANCE_FROM_POINT_X = 6, DISTANCE_FROM_POINT_Y = 0;
    
    /**
     * Image that can be drawn into a ZBufferedImage to display the text.
     * The image is the text.
     */
    private int[] zbufferImage;
    private int zbufferImageWidth, zbufferImageHeight;

    public void setColor(DemoColor color) {
        initColor(color);
    }

    public DemoColor color() {
        return color;
    }

    public PointSortable zmaxPoint() {
        return (PointSortable) point;
    }

    public boolean isTransparent() {
        return color.alpha != 1;
    }
    
    
    public  void drawProjectedOpen( java.awt .Graphics g ) {
        if ( drew || isNaN)
            return;
        drew = true;
        g .setColor( awtColor );
        g .drawString( text, (int) Math .round( point .coords[0] ) + DISTANCE_FROM_POINT_X,
                       - (int) Math .round( point .coords[1] ) + g.getFontMetrics().getHeight() + DISTANCE_FROM_POINT_Y );
    }

    public 
    void drawProjectedFilledFramed( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }

    public  void drawProjectedFilled( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }

    public  void drawProjectedSuspended( java.awt .Graphics g ) {
        drawProjectedOpen(g);
    }
    
    
    public void drawProjectedOpen( ZBufferedImage img ) {
        if ( drew || isNaN )
            return;
        drew = true;
        img.drawImage((int) Math .round(point.coords[0]) + DISTANCE_FROM_POINT_X,
                     -(int) Math .round(point.coords[1]) + DISTANCE_FROM_POINT_Y,
                            Math .round(point.coords[2]),
                      zbufferImage, zbufferImageWidth);
    }

    public void drawProjectedFilledFramed( ZBufferedImage img ) {
        drawProjectedOpen( img );
    }

    public void drawProjectedFilled( ZBufferedImage img ) {
        drawProjectedOpen( img );
    }
    
    public void drawProjectedSuspended( ZBufferedImage img ) {
        drawProjectedOpen( img );
    }

    public  void resetDrewState() {
        drew = false;
    }


}


