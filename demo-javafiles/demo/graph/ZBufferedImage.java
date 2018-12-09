package demo.graph;

import java.awt.image.ColorModel;
import demo.util.DemoColor;

/**
 * ZBufferedImage draws things using a z buffer. It can then be drawn into a java.awt.Graphics.
 * @author deigen
 */
public final class ZBufferedImage {

    // there is a bug in Apple MRJ that I can't figure out really (not all bit masks work)
    // Bit masks that work are the order (from most to least significant): A R G B
    private static ColorModel colorModel_ = new java.awt.image.DirectColorModel( 32,
                                                                            0x00ff0000, // red
                                                                            0x0000ff00, // green
                                                                            0x000000ff, // blue
                                                                            0xff000000 ); // alpha
                                                                            
    private int transX, transY;
    private boolean alphamode_ = false;

    // note: bgcolor is in the ZBufferedImage's color type
    public ZBufferedImage(int width, int height, int bgcolor) {
        image_ = new int[width*height];
        zbuffer_ = new double[width*height];
        colorzbuffer_ = new double[zbuffer_.length];
        this.width_ = width;
        this.height_ = height;
        lineBufSize_ = height_ + 4;
        zbuf1_ = new double[lineBufSize_];
        zbuf2_ = new double[lineBufSize_];
        zbuf3_ = new double[lineBufSize_];
        xbuf1_ = new int[lineBufSize_];
        xbuf2_ = new int[lineBufSize_];
        xbuf3_ = new int[lineBufSize_];
        abuf1_ = new float[lineBufSize_];
        rbuf1_ = new float[lineBufSize_];
        gbuf1_ = new float[lineBufSize_];
        bbuf1_ = new float[lineBufSize_];
        abuf2_ = new float[lineBufSize_];
        rbuf2_ = new float[lineBufSize_];
        gbuf2_ = new float[lineBufSize_];
        bbuf2_ = new float[lineBufSize_];
        abuf3_ = new float[lineBufSize_];
        rbuf3_ = new float[lineBufSize_];
        gbuf3_ = new float[lineBufSize_];
        bbuf3_ = new float[lineBufSize_];
        reset(bgcolor);
    }
    
    /** 
     * Converts a java.awt.Color into the color type used by the ZBufferedImage.
     * @param color the color to convert
     * @return the color used by the ZBufferImage
     */
    public static int convertColor(java.awt.Color color) {
        return  (color.getRed() << 16)
                | (color.getGreen() << 8)
                | (color.getBlue() << 0)
                | (255 << 24);
    }
    
    /** 
     * Converts a (ar,ag,ab,a) array into the color type used by the ZBufferedImage.
     * @param color the color to convert
     * @return the color used by the ZBufferImage
     */
    public static int convertColor(double[] color) {
        if (color[3] == 0)
            return 0;
        int r = (int) Math.round(color[0] / color[3] * 255.0);
        int g = (int) Math.round(color[1] / color[3] * 255.0);
        int b = (int) Math.round(color[2] / color[3] * 255.0);
        int a = (int) Math.round(color[3] * 255.0);
        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;
        a = a > 255 ? 255 : a;
        return  (r << 16)
                | (g << 8)
                | (b << 0)
                | (a << 24);
    }

    /**
     * Converts a DemoColor into the color type used by the ZBufferedImage.
     * @param color the color to convert
     * @return the color used by the ZBufferImage
     */
    public static int convertColor(DemoColor color) {
        if (color.alpha == 0)
            return 0;
        int r = (int) Math.round(color.red   * 255.0);
        int g = (int) Math.round(color.green * 255.0);
        int b = (int) Math.round(color.blue  * 255.0);
        int a = (int) Math.round(color.alpha * 255.0);
        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;
        a = a > 255 ? 255 : a;
        return  (r << 16)
            | (g << 8)
            | (b << 0)
            | (a << 24);
    }
    
    
    /**
     * @return the java.awt.image.ColorModel used by this ZBufferedImage
     */
    public ColorModel colorModel() {
        return colorModel_;
    }
    
    /**
     * @return the image, as an array of int. Each int is a color as specified by the color model.
     * The (n,m) pixel in the image is the n + m*width entry.
     */
     public int[] image() {
        return image_;
     }
     
     /**
      * @return the width of the image
      */
      public int width() {
        return width_;
      }
      
     /**
      * @return the height of the image
      */
      public int height() {
        return height_;
      }
      
    /**
     * This meathod does the same thing as java.awt.Graphics.translate(.).
     * It shifts the origin by the appropriate amount in the x-y plane.
     * @param x the amount to shift the origin in the x direction
     * @param y the amount to shift the origin in the y direction
     */
     public void translate(int x, int y) {
        this.transX = x;
        this.transY = y;
     }
     
    /**
     * Makes the image and z buffer empty, so the ZBufferedImage can be used again.
     */
     public void reset(int bgcolor) {
        for (int i = 0; i < width_ * height_; ++i) {
            image_[i] = bgcolor;
            zbuffer_[i] = Double.NEGATIVE_INFINITY;
            colorzbuffer_[i] = Double.NEGATIVE_INFINITY;
        }
     }

    /**
     * Sets whether the ZBufferedImage are in "alpha mode". If it is, then
     * alpha blending is enabled, and the zbuffer is read-only.
     */
    public void setAlphamode(boolean b) {
        alphamode_ = b;
    }
    
    
    /** 
     * Draws a line.
     * @param x1 the x coord of one point of the line
     * @param y1 the y coord one pt of the line
     * @param z1 the z coord of one pt of the line
     * @param x2 the x coord of the other pt of the line
     * @param y2 the y coord of the other pt of the line
     * @param z2 the z coord of the other pt of the line
     * @param color the color of the line
     */
    public synchronized void drawLine(int x1, int y1, double z1, int x2, int y2, double z2, int color) {
        // note there is a bit of a hack to ensure color is opaque
        // necessary b/c update w/ memory image source will use alpha
        // to overwrite. If alpha blending is added, need to take this
        // check out; will be OK then b/c we'll blend w/ background
        if (alphamode_) {
            float alpha = ((float) ((color >> 24) & 0xFF)) / 255.f;
            coloralphainv = 1.f - alpha;
            int cr = (int) (((color >> 16) & 0xFF) * alpha);
            int cg = (int) (((color >>  8) & 0xFF) * alpha);
            int cb = (int) (( color        & 0xFF) * alpha);
            color_premult = 0xFF000000 | (cr << 16) | (cg << 8) | cb;
            drawLine3DAlphamode( x1 + transX, y1 + transY, z1,   x2 + transX, y2 + transY, z2);
        }
        else {
            this.color = 0xFF000000 | (color & 0x00FFFFFF);
            drawLine3D( x1 + transX, y1 + transY, z1,   x2 + transX, y2 + transY, z2);
        }
    }
    

    /**
     * Draws a line, with shading.
     */
    public synchronized void drawLineShade(int x1, int y1, double z1, int color1,
                                           int x2, int y2, double z2, int color2) {
        drawLine(x1, y1, z1, x2, y2, z2, color1); if (true) return;
        // note there is a bit of a hack to ensure color is opaque
        // necessary b/c update w/ memory image source will use alpha
        // to overwrite. If alpha blending is added, need to take this
        // check out; will be OK then b/c we'll blend w/ background
        if (alphamode_) {
            float r1,g1,b1,a1,r2,g2,b2,a2;
            a1 = (float) ((color1 >> 24) & 0xFF);
            r1 = (float) ((color1 >> 16) & 0xFF);
            g1 = (float) ((color1 >>  8) & 0xFF);
            b1 = (float) ((color1      ) & 0xFF);
            a2 = (float) ((color2 >> 24) & 0xFF);
            r2 = (float) ((color2 >> 16) & 0xFF);
            g2 = (float) ((color2 >>  8) & 0xFF);
            b2 = (float) ((color2      ) & 0xFF);
            a1 /= 255.f;
            a2 /= 255.f;
            drawLine3DAlphamodeShade( x1 + transX, y1 + transY, z1,  r1, g1, b1, a1,
                                      x2 + transX, y2 + transY, z2,  r2, g2, b2, a2 );
        }
        else {
            float r1,g1,b1,r2,g2,b2;
            r1 = (float) ((color1 >> 16) & 0xFF);
            g1 = (float) ((color1 >>  8) & 0xFF);
            b1 = (float) ((color1      ) & 0xFF);
            r2 = (float) ((color2 >> 16) & 0xFF);
            g2 = (float) ((color2 >>  8) & 0xFF);
            b2 = (float) ((color2      ) & 0xFF);
            drawLine3DShade( x1 + transX, y1 + transY, z1, r1, g1, b1,
                             x2 + transX, y2 + transY, z2, r2, g2, b2 );
        }
    }


    /** 
     * Draws a filled triangle.
     * @param color the color of the triangle
     */
    public synchronized void fillTriangle(int x1, int y1, double z1,
                                          int x2, int y2, double z2,
                                          int x3, int y3, double z3, int color) {
        // note there is a bit of a hack to ensure color is opaque
        // necessary b/c update w/ memory image source will use alpha
        // to overwrite. If alpha blending is added, need to take this
        // check out; will be OK then b/c we'll blend w/ background
        if (alphamode_) {
            int ca = (color >> 24) & 0xFF;
            float alpha = ((float) ca) / 255.f;
            coloralphainv = ((float) (255 - ca)) / 255.f;
            int cr = (int) (((float) ((color >> 16) & 0xFF)) * alpha);
            int cg = (int) (((float) ((color >>  8) & 0xFF)) * alpha);
            int cb = (int) (((float) ( color        & 0xFF)) * alpha);
            if (cr < 0 || cg < 0 || cb < 0)
                throw new RuntimeException("LESS THAN ZERO:"+cr+","+cg+","+cb+";"+ca);
            color_premult = 0xFF000000 | (cr << 16) | (cg << 8) | cb;
            fillTriangle3DAlphamode( x1 + transX, y1 + transY, z1,
                                     x2 + transX, y2 + transY, z2,
                                     x3 + transX, y3 + transY, z3 );
        }
        else {
            this.color = 0xFF000000 | (color & 0x00FFFFFF);
            fillTriangle3D( x1 + transX, y1 + transY, z1,
                            x2 + transX, y2 + transY, z2,
                            x3 + transX, y3 + transY, z3 );
        }
    }

    
    /**
     * Draws a filled triangle, with shading.
     */
    public synchronized void fillTriangleShade(int x1, int y1, double z1, int color1,
                                               int x2, int y2, double z2, int color2,
                                               int x3, int y3, double z3, int color3) {
        // note there is a bit of a hack to ensure color is opaque
        // necessary b/c update w/ memory image source will use alpha
        // to overwrite. If alpha blending is added, need to take this
        // check out; will be OK then b/c we'll blend w/ background
        if (alphamode_) {
            float r1,g1,b1,a1,r2,g2,b2,a2,r3,g3,b3,a3;
            a1 = (float) ((color1 >> 24) & 0xFF);
            r1 = (float) ((color1 >> 16) & 0xFF);
            g1 = (float) ((color1 >>  8) & 0xFF);
            b1 = (float) ((color1      ) & 0xFF);
            a2 = (float) ((color2 >> 24) & 0xFF);
            r2 = (float) ((color2 >> 16) & 0xFF);
            g2 = (float) ((color2 >>  8) & 0xFF);
            b2 = (float) ((color2      ) & 0xFF);
            a3 = (float) ((color3 >> 24) & 0xFF);
            r3 = (float) ((color3 >> 16) & 0xFF);
            g3 = (float) ((color3 >>  8) & 0xFF);
            b3 = (float) ((color3      ) & 0xFF);
            a1 /= 255.f;
            a2 /= 255.f;
            a3 /= 255.f;
            fillTriangle3DAlphamodeShade( x1 + transX, y1 + transY, z1,  r1, g1, b1, a1,
                                          x2 + transX, y2 + transY, z2,  r2, g2, b2, a2,
                                          x3 + transX, y3 + transY, z3,  r3, g3, b3, a3 );
        }
        else {
            float r1,g1,b1,r2,g2,b2,r3,g3,b3;
            r1 = (float) ((color1 >> 16) & 0xFF);
            g1 = (float) ((color1 >>  8) & 0xFF);
            b1 = (float) ((color1      ) & 0xFF);
            r2 = (float) ((color2 >> 16) & 0xFF);
            g2 = (float) ((color2 >>  8) & 0xFF);
            b2 = (float) ((color2      ) & 0xFF);
            r3 = (float) ((color3 >> 16) & 0xFF);
            g3 = (float) ((color3 >>  8) & 0xFF);
            b3 = (float) ((color3      ) & 0xFF);
            fillTriangle3DShade( x1 + transX, y1 + transY, z1,  r1, g1, b1,
                                 x2 + transX, y2 + transY, z2,  r2, g2, b2,
                                 x3 + transX, y3 + transY, z3,  r3, g3, b3);
        }
    }


    /**
     * Draws a filled polygon (automatically closes the polygon)
     * @param xcoords an array containing the x-coordinates of the vertices
     * @param ycoords an array containing the y-coordinates of the vertices
     * @param zcoords an array containing the z-coordinates of the vertices
     * @param color the color
     */
    public synchronized void fillPolygon(int[] xcoords, int[] ycoords, double[] zcoords, int color) {
        // note there is a bit of a hack to ensure color is opaque
        // necessary b/c update w/ memory image source will use alpha
        // to overwrite. If alpha blending is added, need to take this
        // check out; will be OK then b/c we'll blend w/ background
        if (alphamode_) {
            float alpha = ((float) ((color >> 24) & 0xFF)) / 255.f;
            coloralphainv = 1.f - alpha;
            int cr = (int) (((color >> 16) & 0xFF) * alpha);
            int cg = (int) (((color >>  8) & 0xFF) * alpha);
            int cb = (int) (( color        & 0xFF) * alpha);
            color_premult = 0xFF000000 | (cr << 16) | (cg << 8) | cb;
            for (int i = 0; i < xcoords.length; ++i) {
                xcoords[i] += transX;
                ycoords[i] += transY;
            }
            fillPolygon3DAlphamode(xcoords, ycoords, zcoords);
            for (int i = 0; i < xcoords.length; ++i) {
                xcoords[i] -= transX;
                ycoords[i] -= transY;
            }
        }
        else {
            this.color = 0xFF000000 | (color & 0x00FFFFFF);
            for (int i = 0; i < xcoords.length; ++i) {
                xcoords[i] += transX;
                ycoords[i] += transY;
            }
            fillPolygon3D(xcoords, ycoords, zcoords);
            for (int i = 0; i < xcoords.length; ++i) {
                xcoords[i] -= transX;
                ycoords[i] -= transY;
            }
        }
    }
    
    
    /**
     * Draws an image with its upper-left corner at the given pt.
     * Transparent pixels in the image are not drawn.
     */
     public synchronized void drawImage(int x, int y, double z, int[] image, int imageWidth) {
        int p = 0, bufPos;
        int imageHeight = image.length / imageWidth;
        int rowmin = (y + transY < 0) ? -y - transY : 0,
            rowmax = (y + transY + imageHeight >= height_) ? height_ - y - transY : imageHeight;
        int colmin = (x + transX < 0) ? -x - transX : 0,
            colmax = (x + transX + imageWidth >= width_) ? width_ - x - transX : imageWidth;
        if (alphamode_) {
            for (int j = rowmin; j < rowmax; ++j) {
                bufPos = width_*(y+j+transY) + x + transX + colmin;
                p = imageWidth * j + colmin;
                for (int i = colmin; i < colmax; ++i) {
                    if ( (z >= zbuffer_[bufPos])
                         && (((image[p] >> 24) & 0xff) != 0) ) {
                        int im = image[p];
                        float ima = (int) (((float) (im >> 24)) / 255.f);
                        int imr = (int) (((im >> 16) & 0xFF) * ima);
                        int img = (int) (((im >>  8) & 0xFF) * ima);
                        int imb = (int) (( im        & 0xFF) * ima);
                        float imainv = 1.f - ima;
                        int curr = (int) (((image_[bufPos] >> 16) & 0xFF) * imainv);
                        int curg = (int) (((image_[bufPos] >>  8) & 0xFF) * imainv);
                        int curb = (int) (( image_[bufPos]        & 0xFF) * imainv);
                        image_[bufPos] = 0xFF000000 | ((imr + curr) << 16)
                                                    | ((img + curg) <<  8)
                                                    | (imb + curb);
                    }
                    ++p;
                    ++bufPos;
                }
            }
        }
        else {
            for (int j = rowmin; j < rowmax; ++j) {
                bufPos = width_*(y+j+transY) + x + transX + colmin;
                p = imageWidth * j + colmin;
                for (int i = colmin; i < colmax; ++i) {
                    if ( (z >= zbuffer_[bufPos])
                         && (((image[p] >> 24) & 0xff) != 0) ) {
                        zbuffer_[bufPos] = z;
                        image_[bufPos] = image[p];
                    }
                    ++p;
                    ++bufPos;
                }
            }
        }
     }




 /***********************************************************/
 /*   Much of the following code is based on the code in    */
 /*   Mike Fried's (mbf) Primitives class         	    */
 /*   revised by deigen. revisions made:	    	    */
 /*	  - changed z buffer to floating point		    */
 /*	  - made z buffer and image in 2 seperate arrays    */
 /*	  - took out shading				    */
 /*	  - put in (better) clipping			    */
 /*       - uses pre-allocated buffers                      */
 /*           (note: this requires synchronized methods)    */
 /***********************************************************/

   private static final int MAXINT = Integer.MAX_VALUE;
	
   private int[] image_;
   private double[] zbuffer_;
   private int width_ = 0;
   private int height_ = 0;

   private int color;

   // buffers for fill3DLineBuff
   private int lineBufSize_;
   private double[]
       zbuf1_,
       zbuf2_,
       zbuf3_;
   private int[]
       xbuf1_,
       xbuf2_,
       xbuf3_;
   private float[]
       abuf1_,
       rbuf1_,
       gbuf1_,
       bbuf1_,
       abuf2_,
       rbuf2_,
       gbuf2_,
       bbuf2_,
       abuf3_,
       rbuf3_,
       gbuf3_,
       bbuf3_;
       
   private double[] colorzbuffer_;
   private float coloralphainv;
   private int color_premult;








   // ************************ NO SHADING OR ALPHA-BLENDING ******************** //

   
   
   // Uses midpoint algorithm, z-buffer
   private void drawLine3D(int x1, int y1, double z1,
                           int x2, int y2, double z2)
   {
      // if the line is entirely off of one side of the screen, return
      // the rest of the clipping and deciding if the line is outside won't deal with some of those cases
      if ( (x1 < 0 && x2 < 0) || (x1 >= width_ && x2 >= width_) ||
           (y1 < 0 && y2 < 0) || (y1 >= height_ && y2 >= height_) )
                return;
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;
      if (x1double < 0) {
        // clip x1double,y1double,z1double
        double s = (-x2double)/(x1double - x2double);
        z1double = (z1double - z2double)*s + z2double;
        y1double = (y1double - y2double)*s + y2double;
        x1double = 0;
      }
      else if (x1double >= width_) {
        // clip x1double,y1double,z1double
        double s = (width_-1-x2double)/(x1double - x2double);
        z1double = (z1double - z2double)*s + z2double;
        y1double = (y1double - y2double)*s + y2double;
        x1double = width_ - 1;
      }
      if (y1double < 0) {
        // clip x1double,y1double,z1double
        if (y1double == y2double) return;
        double s = (-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1double = 0;
      }
      else if (y1double >= height_) {
        // clip x1double,y1double,z1double
        if (y1double == y2double) return;
        double s = (height_-1-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1double = height_ - 1;
      }
      
      if (x2double < 0) {
        // clip x2double,y2double,z2double
        if (x2double == x1double) return;
        double s = (-x1double)/(x2double - x1double);
        z2double = (z2double - z1double)*s + z1double;
        y2double = (y2double - y1double)*s + y1double;
        x2double = 0;
      }
      else if (x2double >= width_) {
        // clip x2double,y2double,z2double
        if (x2double == x1double) return;
        double s = (width_-1-x1double)/(x2double - x1double);
        z2double = (z2double - z1double)*s + z1double;
        y2double = (y2double - y1double)*s + y1double;
        x2double = width_ - 1;
      }
      if (y2double < 0) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        double s = (-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2double = 0;
      }
      else if (y2double >= height_) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        double s = (height_-1-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2double = height_ - 1;
      }

      x1 = (int) Math.round(x1double); x2 = (int) Math.round(x2double);
      y1 = (int) Math.round(y1double); y2 = (int) Math.round(y2double);
      z1 = z1double; z2 = z2double;
                
      // possible that the line is entirely outside, maybe with a pixel inside a corner of the screen
      if ( x1 < 0 || x2 < 0 || x1 >= width_ || x2 >= width_ 
         || y1 < 0 || y2 < 0 || y1 >= height_ || y2 >= height_ )
        return;

      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;
      
      int p = x + (y * width_);
      int dx, dy, dsx, dsy, d2x, d2y, dpx, dpy;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;
      dpx = dsx;
      dpy = dsy * width_;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;
//
// WHAT DO I DO HERE??? d2x, d2y possibly should not have bit shift
//
         d2x = dx << 1;
         d2y = dy << 1;
         //d2x = dx;
         //d2y = dy;

      double dd = (double) (dx > dy ? dx : dy);
      dz /= dd;

      if (dy == dx)
      {
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] =  z;
            image_[p] = color;
        }
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dpx + dpy;
            z += dz;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = color;
            }
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] = z;
            image_[p] = color;
        }
         while (x != x2)
         {
            if (d < 0)
            {
               d += goE;
               x += dsx;
               p += dpx;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = color;
            }
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] =  z;
            image_[p] = color;
        }
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dpy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = color;
            }
         }
      }
   }



   private void fillTriangle3D(int x1, int y1, double z1,
                               int x2, int y2, double z2,
                               int x3, int y3, double z3)
   {
       int tmpi; double tmpd;
       // sort so y is increasing
       if (y1 >= y2 && y1 >= y3) {
           // y1 largest
           tmpi = x3;  x3 = x1;  x1 = tmpi;
           tmpi = y3;  y3 = y1;  y1 = tmpi;
           tmpd = z3;  z3 = z1;  z1 = tmpd;
       }
       else if (y2 >= y1 && y2 >= y3) {
           // y2 largest
           tmpi = x3;  x3 = x2;  x2 = tmpi;
           tmpi = y3;  y3 = y2;  y2 = tmpi;
           tmpd = z3;  z3 = z2;  z2 = tmpd;
       }
       // else y3 largest, so don't swap
       if (y1 > y2) {
           // swap so smallest is in y1
           tmpi = x1;  x1 = x2;  x2 = tmpi;
           tmpi = y1;  y1 = y2;  y2 = tmpi;
           tmpd = z1;  z1 = z2;  z2 = tmpd;
       }
       // now we're sorted in increasing y
       if (y1 >= height_ || y3 < 0) return; // outside the screen
                                            // get clipped y
       int maxy = y3 > height_-1 ? height_-1 : y3;
       int miny = y1 < 0 ? 0 : y1;
       // put boundary of triangle into buffers
       fill3DLineBuff(miny, maxy,
                      x1, y1, z1,
                      x2, y2, z2,
                      xbuf1_, zbuf1_);
       fill3DLineBuff(miny, maxy,
                      x2, y2, z2,
                      x3, y3, z3,
                      xbuf2_, zbuf2_);
       fill3DLineBuff(miny, maxy,
                      x1, y1, z1,
                      x3, y3, z3,
                      xbuf3_, zbuf3_);
       int y = miny;
       int p = 0;
       if (y2 < miny) {
           if (xbuf3_[0] > xbuf2_[0]) {
               // line 13 to right of line 23 (line13 > line23)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3D(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3D(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p]);
           }
       }
       else {
           int ymiddle = y2 > maxy ? maxy : y2;
           if (xbuf3_[ymiddle-miny] > xbuf1_[ymiddle-miny]) {
               // line 13 to right of line 23 (line13 > line23)
               // do lower part of triangle
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3D(xbuf1_[p], y, xbuf3_[p], zbuf1_[p], zbuf3_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3D(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3D(xbuf3_[p], y, xbuf1_[p], zbuf3_[p], zbuf1_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3D(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p]);
           }
       }
   }


   private class LineBuffer {
       public LineBuffer(int ind, int size, int xcoord1, int xcoord2, int ycoord1, int ycoord2, double zcoord1, double zcoord2) {
		   index = ind;
		   x1 = xcoord1; x2 = xcoord2;
		   y1 = ycoord1; y2 = ycoord2;
		   z1 = zcoord1; z2 = zcoord2;
           if (y1 < y2) {
               ymin = y1;
               ymax = y2;
           }
           else {
               ymin = y2;
               ymax = y1;
           }
           xbuff = new int[size];
           zbuff = new double[size];
       }
       int x1, x2, y1, y2;
	   double z1, z2;
       int[] xbuff;
       double[] zbuff;
       int ymin;
       int ymax;
	   int index;
   }


   // fillPolygon3D and support class above by deigen
   private void fillPolygon3D(int[] xcoords, int[] ycoords, double[] zcoords) {
       // find min and max y for polygon
       double minyd = 1e30;
       double maxyd = -1e30;
       for (int i = 0; i < ycoords.length; ++i) {
           if (ycoords[i] < minyd)
               minyd = ycoords[i];
           if (ycoords[i] > maxyd)
               maxyd = ycoords[i];
       }
       int miny = (int) minyd;
       int maxy = (int) maxyd;
       ++maxy; --miny;  // add/subtract 1 so the y coords fit fully inside the stripe
                        // clip y to screen
       if (miny >= height_ || maxy < 0 ) return;
       miny = miny < 0 ? 0 : miny;
       maxy = maxy >= height_ ? height_ - 1 : maxy;
       int size = maxy - miny + 1;
       LineBuffer[] buffers = new LineBuffer[xcoords.length+1];
	   int imax = xcoords.length - 1;
	   for (int i = 0; i < imax; ++i) {
           buffers[i] = new LineBuffer(i, size,
                                       xcoords[i], xcoords[i+1],
                                       ycoords[i], ycoords[i+1],
									   zcoords[i], zcoords[i+1]);
		   fill3DLineBuff(miny, maxy,
						  xcoords[i], ycoords[i], zcoords[i],
						  xcoords[i+1], ycoords[i+1], zcoords[i+1],
						  buffers[i].xbuff,
						  buffers[i].zbuff);
		   // if horizontal, make sure the buffer value is the smaller x (needed to sort in the right order)
		   if (ycoords[i] == ycoords[i+1]) {
			   buffers[i].xbuff[ycoords[i]-miny] = xcoords[i] < xcoords[i+1] ? xcoords[i] : xcoords[i+1];
			   buffers[i].zbuff[ycoords[i]-miny] = xcoords[i] < xcoords[i+1] ? zcoords[i] : zcoords[i+1];
		   }
       }
       if (imax >= 0) {
           buffers[imax] = new LineBuffer(imax, size,
                                          xcoords[imax], xcoords[0],
                                          ycoords[imax], ycoords[0],
										  zcoords[imax], zcoords[0]);
		   fill3DLineBuff(miny, maxy,
						  xcoords[imax], ycoords[imax], zcoords[imax],
						  xcoords[0], ycoords[0], zcoords[0],
						  buffers[imax].xbuff,
						  buffers[imax].zbuff);
		   // if horizontal, make sure the buffer value is the smaller x (needed to sort in the right order)
		   if (ycoords[imax] == ycoords[0]) {
			   buffers[imax].xbuff[ycoords[imax]-miny] = xcoords[imax] < xcoords[0] ? xcoords[imax] : xcoords[0];
			   buffers[imax].zbuff[ycoords[imax]-miny] = xcoords[imax] < xcoords[0] ? zcoords[imax] : zcoords[0];
		   }
       }
	   // dummy buffer so there's no edge case in scanline active edge checking
	   buffers[imax+1] = new LineBuffer(imax<<1,0,0,0,MAXINT,MAXINT,0,0);
	   // sort buffers by y-coord
	   boolean sorted = false;
	   while (!sorted) {
		   sorted = true;
		   for (int u = 1; u < buffers.length; ++u) {
			   LineBuffer b1 = buffers[u-1];
			   LineBuffer b2 = buffers[u];
			   if (b2.ymin < b1.ymin) {
				   // swap
				   sorted = false;
				   buffers[u-1] = b2;
				   buffers[u] = b1;
			   }
		   }
	   }
       // use horizontal scan lines (moving vertically)
       int j = 0; // index of scan line in x and z line buffers
	   // one beyond the max index for active edges
	   int activeEdgeMax = 0;
	   int nextActiveEdgeYmax = MAXINT;
	   LineBuffer[] activeEdges = new LineBuffer[buffers.length];
	   int nactiveEdges = 0;
	   for (int y = miny; y <= maxy; ++y) {
		   // check if we reached the beginning of inactive edges, or the end of any active edges
		   while (buffers[activeEdgeMax].ymin <= y) {
			   LineBuffer b = buffers[activeEdgeMax];
			   if (b.ymax < nextActiveEdgeYmax)
				   nextActiveEdgeYmax = b.ymax;
			   activeEdges[nactiveEdges++] = b;
			   activeEdgeMax++;
		   }
		   while (nextActiveEdgeYmax < y) {
			   nextActiveEdgeYmax = MAXINT;
			   for (int i = 0; i < nactiveEdges; ++i) {
				   if (activeEdges[i].ymax < y) {
					   activeEdges[i] = activeEdges[--nactiveEdges];
					   --i; // want to repeat this entry, since it was just swapped
				   }
				   else {
					   if (activeEdges[i].ymax < nextActiveEdgeYmax)
						   nextActiveEdgeYmax = activeEdges[i].ymax;
				   }
			   }
		   }
		   // make sure buffers are sorted by the x coords for this scanline
		   sorted = false;
		   while (!sorted) {
			   sorted = true;
			   for (int u = 1; u < nactiveEdges; ++u) {
				   LineBuffer b1 = activeEdges[u-1];
				   LineBuffer b2 = activeEdges[u];
				   if (b2.xbuff[j] < b1.xbuff[j]) {
					   // swap
					   sorted = false;
					   activeEdges[u-1] = b2;
					   activeEdges[u] = b1;
				   }
			   }
		   }
		   // draw scanline
		   boolean inPolygon = false;
		   boolean horizontal = false;
		   LineBuffer horizontalStartB = null;
		   for (int i = 0; i < nactiveEdges-1; ++i) {
			   LineBuffer b1 = activeEdges[i];
			   LineBuffer b2 = activeEdges[i+1];
			   if (horizontal) { 
				   if (b2.y1 != b2.y2) {
					   // end of horizontal stretch: reset b1 to what it was before the horizontals,
					   // and do a vertex enter/leave check (horizontal line like extended vertex)
					   horizontal = false;
					   b1 = horizontalStartB;
					   int b1y = b1.y1 == y ? b1.y2 : b1.y1;
					   int b2y = b2.y1 == y ? b2.y2 : b2.y1;
					   if (!((b1y > y && b2y < y) || (b1y < y && b2y > y)))
						   // crossing bdy
						   inPolygon = !inPolygon;
				   }
				   else continue;
			   }
			   inPolygon = !inPolygon;
			   if (b1.xbuff[j] == b2.xbuff[j]) {
				   // vertex or self intersection
				   // find the other edge for the vertex (if any)
				   // make b1,b2 be the vertex edge pair. Move edges if necessary in active edge table
				   LineBuffer e;
				   boolean isVertex = false;
				   for (int u = i+1; u < nactiveEdges; ++u) {
					   if ((e = activeEdges[u]).xbuff[j] != b1.xbuff[j])
						   break; // done going through the active edges with same x coord (list is sorted by x)
					   if ( (e.index == (b1.index+1)%(imax+1) && e.y1 == b1.y2 && e.x1 == b1.x2 && e.y1 == y) ) {
						   isVertex = true;
						   activeEdges[u] = b2;
						   activeEdges[i+1] = b2 = e;
						   break;
					   }
					   else if ( (b1.index == (e.index+1)%(imax+1) && b1.y1 == e.y2 && b1.x1 == e.x2 && e.y2 == y) ) {
						   isVertex = true;
						   activeEdges[u] = b2;
						   activeEdges[i+1] = b2 = b1;
						   activeEdges[i] = b1 = e;
						   break;
					   }
				   }
				   if (isVertex) {
					   if ((b1.y1 > y && b2.y2 < y) || (b1.y1 < y && b2.y2 > y))
						   // crossing bdy: want next iteration to be opposite inPolygon status as this iter
						   inPolygon = !inPolygon;
					   if (b1.y1 == b1.y2 || b2.y1 == b2.y2) {
						   horizontal = true;
						   horizontalStartB = b1.y1 == b1.y2 ? b2 : b1;
					   }
				   }
			   }
			   if (inPolygon)
				   drawHLine3D(b1.xbuff[j], y, b2.xbuff[j], b1.zbuff[j], b2.zbuff[j]);
		   }
		   ++j;
	   } // end for each horizontal scanline
   }
   
 
   
   
   
   // Uses midpoint algorithm, z-buffer
   // Clips against given coordinates
   private void fill3DLineBuff(int miny, int maxy,
                               int x1, int y1, double z1,
                                    int x2, int y2, double z2,
                                    int xbuff[],
                                    double zbuff[])
   {
       if (y1 == y2) {
           xbuff[y1-miny] = x2;
           zbuff[y1-miny] = z2;
           return;
       }
      // clip to region
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;  
      if (y1 < miny) {
        // clip x1double,y1double,z1double
        double s = (miny-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = miny;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
      }
      else if (y1 > maxy) {
        // clip x1double,y1double,z1double
        double s = (maxy-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = maxy;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
      }
      if (y2 < miny) {
        // clip x2double,y2double,z2double
        double s = (miny-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = miny;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
      }
      else if (y2 > maxy) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        double s = (maxy-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = maxy;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
      }
         
      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;

      int p = (y - miny);
      int dx, dy, dsx, dsy, d2x, d2y;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;
      d2x = dx << 1;
      d2y = dy << 1;

      double dd = (double) (dx > dy ? dx : dy);
      dz /= dd;

      if (dy == dx)
      {
            xbuff[p] = x;
            zbuff[p] = z;
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dsy;
            z += dz;
               xbuff[p] = x;
               zbuff[p] = z;
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
            xbuff[p] = x;
            zbuff[p] = z;
         while (y != y2)
         {
            if (d < 0)
            {
               int numincrements = (int) Math.ceil(((double) -d)/(double) goE);
               d += goE * numincrements;
               x += dsx * numincrements;
               z += dz * numincrements;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dsy;
               z += dz;
            }
               xbuff[p] = x;
               zbuff[p] = z;
         }
         if (x != x2)
         {
             // didn't quite get to x2 by the time we got to y2.
             // (x2,y2) is on y2 horizontal line, so go for as many
             // increments as it would take to get to x2.
             int numincrements = x < x2 ? x2 - x : x - x2;
             // d += goE * numincrements; // d doesn't matter to us
             x += dsx * numincrements;
             z += dz * numincrements;
             xbuff[p] = x;
             zbuff[p] = z;
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
           xbuff[p] = x;
           zbuff[p] = z;
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dsy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dsy;
            }
            z += dz;
               xbuff[p] = x;
               zbuff[p] = z;
         }
      }
	  // make sure endpts are correct
	  xbuff[y1-miny] = x1;
   	  xbuff[y2-miny] = x2;
   	  zbuff[y1-miny] = z1;
   	  zbuff[y2-miny] = z2;
   }

    


   // NOTE: this requires x1 <= x2
   private void drawHLine3D(int x1, int y, int x2, double z1, double z2)
   {
       if (y < 0 || y >= height_) throw new RuntimeException("HLine: y outside bounds");
       if (x2 < 0 || (x1 >= width_)) return; // whole segment is off screen
       if (x1 < 0) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = -x2double/(x1double - x2double);
           z1 = (z1double - z2double)*s + z2double;
           x1 = 0;
       }
       if (x2 >= width_) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = (width_-1-x1double)/(x2double - x1double);
           z2 = (z2double - z1double)*s + z1double;
           x2 = width_ - 1;
       }
       int x = x1;
       double z = z1;
       int xdist = x2 - x1;
       double dz = (z2-z1)/(double)xdist;
       int p = x + y * width_;
	   int p2 = p + xdist;
       for(; p <= p2; ++p) {
           if (z > zbuffer_[p]) {
               zbuffer_[p] = z;
               image_[p] = color;
           }
           z += dz;
       }
   }







   


// ***************************** METHODS WITH SHADING ****************************** //

   
   // Uses midpoint algorithm, z-buffer
   // used to have shading, but deigen took that out
   private void drawLine3DShade(int x1, int y1, double z1, float r1, float g1, float b1,
                                int x2, int y2, double z2, float r2, float g2, float b2)
   {
       // if the line is entirely off of one side of the screen, return
       // the rest of the clipping and deciding if the line is outside won't deal with some of those cases
       if ( (x1 < 0 && x2 < 0) || (x1 >= width_ && x2 >= width_) ||
            (y1 < 0 && y2 < 0) || (y1 >= height_ && y2 >= height_) )
           return;
       double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;
       if (x1double < 0) {
           // clip x1double,y1double,z1double
           double s = (-x2double)/(x1double - x2double);
           z1double = (z1double - z2double)*s + z2double;
           y1double = (y1double - y2double)*s + y2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           x1double = 0;
       }
       else if (x1double >= width_) {
           // clip x1double,y1double,z1double
           double s = (width_-1-x2double)/(x1double - x2double);
           z1double = (z1double - z2double)*s + z2double;
           y1double = (y1double - y2double)*s + y2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           x1double = width_ - 1;
       }
       if (y1double < 0) {
           // clip x1double,y1double,z1double
           if (y1double == y2double) return;
           double s = (-y2double)/(y1double - y2double);
           z1double = (z1double - z2double)*s + z2double;
           x1double = (x1double - x2double)*s + x2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           y1double = 0;
       }
       else if (y1double >= height_) {
           // clip x1double,y1double,z1double
           if (y1double == y2double) return;
           double s = (height_-1-y2double)/(y1double - y2double);
           z1double = (z1double - z2double)*s + z2double;
           x1double = (x1double - x2double)*s + x2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           y1double = height_ - 1;
       }

       if (x2double < 0) {
           // clip x2double,y2double,z2double
           if (x2double == x1double) return;
           double s = (-x1double)/(x2double - x1double);
           z2double = (z2double - z1double)*s + z1double;
           y2double = (y2double - y1double)*s + y1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           x2double = 0;
       }
       else if (x2double >= width_) {
           // clip x2double,y2double,z2double
           if (x2double == x1double) return;
           double s = (width_-1-x1double)/(x2double - x1double);
           z2double = (z2double - z1double)*s + z1double;
           y2double = (y2double - y1double)*s + y1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           x2double = width_ - 1;
       }
       if (y2double < 0) {
           // clip x2double,y2double,z2double
           if (y1double == y2double) return;
           double s = (-y1double)/(y2double - y1double);
           z2double = (z2double - z1double)*s + z1double;
           x2double = (x2double - x1double)*s + x1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           y2double = 0;
       }
       else if (y2double >= height_) {
           // clip x2double,y2double,z2double
           if (y1double == y2double) return;
           double s = (height_-1-y1double)/(y2double - y1double);
           z2double = (z2double - z1double)*s + z1double;
           x2double = (x2double - x1double)*s + x1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           y2double = height_ - 1;
       }
       
      x1 = (int) Math.round(x1double); x2 = (int) Math.round(x2double);
      y1 = (int) Math.round(y1double); y2 = (int) Math.round(y2double);
      z1 = z1double; z2 = z2double;
                
      // possible that the line is entirely outside, maybe with a pixel inside a corner of the screen
      if ( x1 < 0 || x2 < 0 || x1 >= width_ || x2 >= width_ 
         || y1 < 0 || y2 < 0 || y1 >= height_ || y2 >= height_ )
        return;

      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;

      r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
      g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
      b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
      r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
      g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
      b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;

      float r = r1, g = g1, b = b1;
      float dr = r2 - r1;
      float dg = g2 - g1;
      float db = b2 - b1;
      
      int p = x + (y * width_);
      int dx, dy, dsx, dsy, d2x, d2y, dpx, dpy;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;
      dpx = dsx;
      dpy = dsy * width_;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;

      d2x = dx << 1;
      d2y = dy << 1;

      double dd = (double) (dx > dy ? dx : dy);
      dz /= dd;
      dr /= dd;
      dg /= dd;
      db /= dd;
      
      if (dy == dx)
      {
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] =  z;
            image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));
        }
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dpx + dpy;
            z += dz;
            r += dr;
            g += dg;
            b += db;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));;
            }
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] = z;
            image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));;
        }
         while (x != x2)
         {
            if (d < 0)
            {
               d += goE;
               x += dsx;
               p += dpx;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));;
            }
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
        if (z >= zbuffer_[p])
        {
            zbuffer_[p] =  z;
            image_[p] = color;
        }
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dpy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
            if (z >= zbuffer_[p])
            {
                zbuffer_[p] =  z;
                image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));;
            }
         }
      }
   }


   private void fillTriangle3DShade(int x1, int y1, double z1, float r1, float g1, float b1,
                                    int x2, int y2, double z2, float r2, float g2, float b2,
                                    int x3, int y3, double z3, float r3, float g3, float b3)
   {
       int tmpi; double tmpd; float tmpf;
       // sort so y is increasing
       if (y1 >= y2 && y1 >= y3) {
           // y1 largest
           tmpi = x3;  x3 = x1;  x1 = tmpi;
           tmpi = y3;  y3 = y1;  y1 = tmpi;
           tmpd = z3;  z3 = z1;  z1 = tmpd;
           tmpf = r3;  r3 = r1;  r1 = tmpf;
           tmpf = g3;  g3 = g1;  g1 = tmpf;
           tmpf = b3;  b3 = b1;  b1 = tmpf;
       }
       else if (y2 >= y1 && y2 >= y3) {
           // y2 largest
           tmpi = x3;  x3 = x2;  x2 = tmpi;
           tmpi = y3;  y3 = y2;  y2 = tmpi;
           tmpd = z3;  z3 = z2;  z2 = tmpd;
           tmpf = r3;  r3 = r2;  r2 = tmpf;
           tmpf = g3;  g3 = g2;  g2 = tmpf;
           tmpf = b3;  b3 = b2;  b2 = tmpf;
       }
       // else y3 largest, so don't swap
       if (y1 > y2) {
           // swap so smallest is in y1
           tmpi = x1;  x1 = x2;  x2 = tmpi;
           tmpi = y1;  y1 = y2;  y2 = tmpi;
           tmpd = z1;  z1 = z2;  z2 = tmpd;
           tmpf = r1;  r1 = r2;  r2 = tmpf;
           tmpf = g1;  g1 = g2;  g2 = tmpf;
           tmpf = b1;  b1 = b2;  b2 = tmpf;
       }
       // now we're sorted in increasing y
       if (y1 >= height_ || y3 < 0) return; // outside the screen
                                            // get clipped y
       int maxy = y3 > height_-1 ? height_-1 : y3;
       int miny = y1 < 0 ? 0 : y1;
       // put boundary of triangle into buffers
       fill3DLineBuffShade(miny, maxy,
                           x1, y1, z1, r1, g1, b1,
                           x2, y2, z2, r2, g2, b2,
                           xbuf1_, zbuf1_, rbuf1_, gbuf1_, bbuf1_);
       fill3DLineBuffShade(miny, maxy,
                           x2, y2, z2, r2, g2, b2,
                           x3, y3, z3, r3, g3, b3,
                           xbuf2_, zbuf2_, rbuf2_, gbuf2_, bbuf2_);
       fill3DLineBuffShade(miny, maxy,
                           x1, y1, z1, r1, g1, b1,
                           x3, y3, z3, r3, g3, b3,
                           xbuf3_, zbuf3_, rbuf3_, gbuf3_, bbuf3_);
       int y = miny;
       int p = 0;
       if (y2 < miny) {
           if (xbuf3_[0] > xbuf2_[0]) {
               // line 13 to right of line 23 (line13 > line23)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DShade(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p],
                                    rbuf2_[p], gbuf2_[p], bbuf2_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DShade(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p],
                                    rbuf2_[p], gbuf2_[p], bbuf2_[p]);
           }
       }
       else {
           int ymiddle = y2 > maxy ? maxy : y2;
           if (xbuf3_[ymiddle-miny] > xbuf1_[ymiddle-miny]) {
               // line 13 to right of line 23 (line13 > line23)
               // do lower part of triangle
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DShade(xbuf1_[p], y, xbuf3_[p], zbuf1_[p], zbuf3_[p],
                                    rbuf1_[p], gbuf1_[p], bbuf1_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DShade(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p],
                                    rbuf2_[p], gbuf2_[p], bbuf2_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DShade(xbuf3_[p], y, xbuf1_[p], zbuf3_[p], zbuf1_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p],
                                    rbuf1_[p], gbuf1_[p], bbuf1_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DShade(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p],
                                    rbuf3_[p], gbuf3_[p], bbuf3_[p],
                                    rbuf2_[p], gbuf2_[p], bbuf2_[p]);
           }
       }
   }


   // Uses midpoint algorithm, z-buffer
   // Clips against given coordinates
   private void fill3DLineBuffShade(int miny, int maxy,
                                    int x1, int y1, double z1, float r1, float g1, float b1,
                                    int x2, int y2, double z2, float r2, float g2, float b2,
                                    int xbuff[],
                                    double zbuff[],
                                    float[] rbuff, float[] gbuff, float[] bbuff)
   {
       if (y1 == y2) {
           xbuff[0] = x2;
           zbuff[0] = z2;
           rbuff[0] = r2;
           gbuff[0] = g2;
           bbuff[0] = b2;
           return;
       }
      // clip to region
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;  
      if (y1 < miny) {
        // clip x1double,y1double,z1double
        double s = (miny-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = miny;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
        r1 = ((float) ((r1 - r2)*s)) + r2;
        g1 = ((float) ((g1 - g2)*s)) + g2;
        b1 = ((float) ((b1 - b2)*s)) + b2;
      }
      else if (y1 > maxy) {
        // clip x1double,y1double,z1double
        double s = (maxy-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = maxy;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
        r1 = ((float) ((r1 - r2)*s)) + r2;
        g1 = ((float) ((g1 - g2)*s)) + g2;
        b1 = ((float) ((b1 - b2)*s)) + b2;
      }
      if (y2 < miny) {
        // clip x2double,y2double,z2double
        double s = (miny-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = miny;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
        r2 = ((float) ((r2 - r1)*s)) + r1;
        g2 = ((float) ((g2 - g1)*s)) + g1;
        b2 = ((float) ((b2 - b1)*s)) + b1;
      }
      else if (y2 > maxy) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        double s = (maxy-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = maxy;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
        r2 = ((float) ((r2 - r1)*s)) + r1;
        g2 = ((float) ((g2 - g1)*s)) + g1;
        b2 = ((float) ((b2 - b1)*s)) + b1;
      }
         
      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;

      r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
      g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
      b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
      r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
      g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
      b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;

      float r = r1, g = g1, b = b1;
      float dr = r2 - r1;
      float dg = g2 - g1;
      float db = b2 - b1;

      int p = (y - miny);
      int dx, dy, dsx, dsy, d2x, d2y;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;
      d2x = dx << 1;
      d2y = dy << 1;

      double dd = (double) (dx > dy ? dx : dy);
      dr /= dd;
      dg /= dd;
      db /= dd;
      dz /= dd;

      if (dy == dx)
      {
            xbuff[p] = x;
            zbuff[p] = z;
            rbuff[p] = r;
            gbuff[p] = g;
            bbuff[p] = b;
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dsy;
            z += dz;
            r += dr;
            g += dg;
            b += db;
               xbuff[p] = x;
               zbuff[p] = z;
               rbuff[p] = r;
               gbuff[p] = g;
               bbuff[p] = b;
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
            xbuff[p] = x;
            zbuff[p] = z;
            rbuff[p] = r;
            gbuff[p] = g;
            bbuff[p] = b;
         while (y != y2)
         {
            if (d < 0)
            {
               int numincrements = (int) Math.ceil(((double) -d)/(double) goE);
               d += goE * numincrements;
               x += dsx * numincrements;
               z += dz * numincrements;
               r += dr * numincrements;
               g += dg * numincrements;
               b += db * numincrements;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dsy;
               z += dz;
               r += dr;
               g += dg;
               b += db;
            }
               xbuff[p] = x;
               zbuff[p] = z;
               rbuff[p] = r;
               gbuff[p] = g;
               bbuff[p] = b;
         }
         if (x != x2)
         {
             // didn't quite get to x2 by the time we got to y2.
             // (x2,y2) is on y2 horizontal line, so go for as many
             // increments as it would take to get to x2.
             int numincrements = x < x2 ? x2 - x : x - x2;
             // d += goE * numincrements; // d doesn't matter to us
             x += dsx * numincrements;
             z += dz * numincrements;
             r += dr * numincrements;
             g += dg * numincrements;
             b += db * numincrements;
             xbuff[p] = x;
             zbuff[p] = z;
             rbuff[p] = r;
             gbuff[p] = g;
             bbuff[p] = b;
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
           xbuff[p] = x;
           zbuff[p] = z;
           rbuff[p] = r;
           gbuff[p] = g;
           bbuff[p] = b;
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dsy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dsy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
               xbuff[p] = x;
               zbuff[p] = z;
               rbuff[p] = r;
               gbuff[p] = g;
               bbuff[p] = b;
         }
      }
   }

    


   // NOTE: this requires x1 <= x2
   private void drawHLine3DShade(int x1, int y, int x2, double z1, double z2,
                                 float r1, float g1, float b1,
                                 float r2, float g2, float b2)
   {
       if (y < 0 || y >= height_) throw new RuntimeException("HLine: y outside bounds");
       if (x2 < 0 || (x1 >= width_)) return; // whole segment is off screen
       if (x1 < 0) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = -x2double/(x1double - x2double);
           z1 = (z1double - z2double)*s + z2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           x1 = 0;
       }
       if (x2 >= width_) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = (width_-1-x1double)/(x2double - x1double);
           z2 = (z2double - z1double)*s + z1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           x2 = width_ - 1;
       }
       int x = x1;
       double z = z1;
       float r = r1, g = g1, b = b1;
       int xdist = x2 - x1;
       double dz = (z2-z1)/(double)xdist;
       float dr = (r2-r1)/(float)xdist,
             dg = (g2-g1)/(float)xdist,
             db = (b2-b1)/(float)xdist;
       int p = x + y * width_;
       for(; x <= x2; ++x, ++p) {
           if (z > zbuffer_[p]) {
               zbuffer_[p] = z;
               image_[p] = (0xFF000000 | (((int)r) << 16) | (((int)g) << 8) | ((int) b));;
           }
           z += dz;
           r += dr;
           g += dg;
           b += db;
       }
   }







   




// ******************* ALPHA-BLEND / READ-ONLY ZBUFFER MODE METHODS **************** //

   private static final double OUTLINE_ZPUSH = 0;//0.7;
   private static final double OUTLINE_ZTOL  = 0;//0.9;
   private static final double TRIANGLE_SETPIX_ZTOL = 2;
   private static final double LINE_SETPIX_ZTOL = -10;
   private static final double POLY_SETPIX_ZTOL = 2;
   private double drawline_ztol = 0;
   private double setpixel_ztol = 2;
   private boolean use_colorzbuff = false;
   
   private void setPixelAlphamode(int p, double z) {
       if (use_colorzbuff && Math.abs(z - colorzbuffer_[p]) < setpixel_ztol)
           return;
       if (use_colorzbuff && z > colorzbuffer_[p])
           colorzbuffer_[p] = z;
       int ipx = image_[p];
       int ir = (int) (((float) ((ipx >> 16) & 0xFF)) * coloralphainv);
       int ig = (int) (((float) ((ipx >>  8) & 0xFF)) * coloralphainv);
       int ib = (int) (((float) ( ipx        & 0xFF)) * coloralphainv);
       if (ir < 0 || ig < 0 || ib < 0)
           throw new RuntimeException("Pix color vals cannot be negative.");
//       image_[p] = color_premult + ((ir << 16) | (ig << 8) | ib);
       int cr = (color_premult >> 16) & 0xFF;
       int cg = (color_premult >>  8) & 0xFF;
       int cb = (color_premult      ) & 0xFF;
       int ar = ir + cr;
       int ag = ig + cg;
       int ab = ib + cb;
       ar = ar > 255 ? 255 : ar;
       ag = ag > 255 ? 255 : ag;
       ab = ab > 255 ? 255 : ab;
       image_[p] = 0xFF000000 | (ar << 16) | (ag << 8) | ab;
   }
   
   // Uses midpoint algorithm, z-buffer
   // used to have shading, but deigen took that out
   private void drawLine3DAlphamode(int x1, int y1, double z1,
                                    int x2, int y2, double z2)
   {
      // if the line is entirely off of one side of the screen, return
      // the rest of the clipping and deciding if the line is outside won't deal with some of those cases
      if ( (x1 < 0 && x2 < 0) || (x1 >= width_ && x2 >= width_) ||
           (y1 < 0 && y2 < 0) || (y1 >= height_ && y2 >= height_) )
                return;
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;  
      if (x1double < 0) {
        // clip x1double,y1double,z1double
        z1double = (z1double - z2double)/(x1double - x2double) * (-x2double) + z2double;
        y1double = (y1double - y2double)/(x1double - x2double) * (-x2double) + y2double;
        x1double = 0;
      }
      else if (x1double >= width_) {
        // clip x1double,y1double,z1double
        z1double = (z1double - z2double)/(x1double - x2double) * (width_-1-x2double) + z2double;
        y1double = (y1double - y2double)/(x1double - x2double) * (width_-1-x2double) + y2double;
        x1double = width_ - 1;
      }
      if (y1double < 0) {
        // clip x1double,y1double,z1double
        if (y1double == y2double) return;
        z1double = (z1double - z2double)/(y1double - y2double) * (-y2double) + z2double;
        x1double = (x1double - x2double)/(y1double - y2double) * (-y2double) + x2double;
        y1double = 0;
      }
      else if (y1double >= height_) {
        // clip x1double,y1double,z1double
        if (y1double == y2double) return;
        z1double = (z1double - z2double)/(y1double - y2double) * (height_-1-y2double) + z2double;
        x1double = (x1double - x2double)/(y1double - y2double) * (height_-1-y2double) + x2double;
        y1double = height_ - 1;
      }
      
      if (x2double < 0) {
        // clip x2double,y2double,z2double
        if (x2double == x1double) return;
        z2double = (z2double - z1double)/(x2double - x1double) * (-x1double) + z1double;
        y2double = (y2double - y1double)/(x2double - x1double) * (-x1double) + y1double;
        x2double = 0;
      }
      else if (x2double >= width_) {
        // clip x2double,y2double,z2double
        if (x2double == x1double) return;
        z2double = (z2double - z1double)/(x2double - x1double) * (width_-1-x1double) + z1double;
        y2double = (y2double - y1double)/(x2double - x1double) * (width_-1-x1double) + y1double;
        x2double = width_ - 1;
      }
      if (y2double < 0) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        z2double = (z2double - z1double)/(y2double - y1double) * (-y1double) + z1double;
        x2double = (x2double - x1double)/(y2double - y1double) * (-y1double) + x1double;
        y2double = 0;
      }
      else if (y2double >= height_) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        z2double = (z2double - z1double)/(y2double - y1double) * (height_-1-y1double) + z1double;
        x2double = (x2double - x1double)/(y2double - y1double) * (height_-1-y1double) + x1double;
        y2double = height_ - 1;
      }

      x1 = (int) Math.round(x1double); x2 = (int) Math.round(x2double);
      y1 = (int) Math.round(y1double); y2 = (int) Math.round(y2double);
      z1 = z1double; z2 = z2double;
                
      // possible that the line is entirely outside, maybe with a pixel inside a corner of the screen
      if ( x1 < 0 || x2 < 0 || x1 >= width_ || x2 >= width_ 
         || y1 < 0 || y2 < 0 || y1 >= height_ || y2 >= height_ )
        return;

      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;
      
      int p = x + (y * width_);
      int dx, dy, dsx, dsy, d2x, d2y, dpx, dpy;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;
      dpx = dsx;
      dpy = dsy * width_;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;
//
// WHAT DO I DO HERE??? d2x, d2y possibly should not have bit shift
//
         d2x = dx << 1;
         d2y = dy << 1;
         //d2x = dx;
         //d2y = dy;

      double dd = (double) (dx > dy ? dx : dy);
      dz /= dd;

//      setpixel_ztol = LINE_SETPIX_ZTOL;

      if (dy == dx)
      {
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamode(p,z);
//            zbuffer_[p] = z;
        }
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dpx + dpy;
            z += dz;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamode(p,z);
//                zbuffer_[p] = z;
            }
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamode(p,z);
//            zbuffer_[p] = z;
        }
         while (x != x2)
         {
            if (d < 0)
            {
               d += goE;
               x += dsx;
               p += dpx;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamode(p,z);
//                zbuffer_[p] = z;
            }
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamode(p,z);
            zbuffer_[p] = z;
        }
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dpy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamode(p,z);
//                zbuffer_[p] = z;
            }
         }
      }
   }


   private void fillTriangle3DAlphamode(int x1, int y1, double z1,
                                        int x2, int y2, double z2,
                                        int x3, int y3, double z3)
   {
       int tmpi; double tmpd;
       // sort so y is increasing
       if (y1 >= y2 && y1 >= y3) {
           // y1 largest
           tmpi = x3;  x3 = x1;  x1 = tmpi;
           tmpi = y3;  y3 = y1;  y1 = tmpi;
           tmpd = z3;  z3 = z1;  z1 = tmpd;
       }
       else if (y2 >= y1 && y2 >= y3) {
           // y2 largest
           tmpi = x3;  x3 = x2;  x2 = tmpi;
           tmpi = y3;  y3 = y2;  y2 = tmpi;
           tmpd = z3;  z3 = z2;  z2 = tmpd;
       }
       // else y3 largest, so don't swap
       if (y1 > y2) {
           // swap so smallest is in y1
           tmpi = x1;  x1 = x2;  x2 = tmpi;
           tmpi = y1;  y1 = y2;  y2 = tmpi;
           tmpd = z1;  z1 = z2;  z2 = tmpd;
       }
       // now we're sorted in increasing y
       if (y1 >= height_ || y3 < 0) return; // outside the screen
                                            // get clipped y
       int maxy = y3 > height_-1 ? height_-1 : y3;
       int miny = y1 < 0 ? 0 : y1;
       // put boundary of triangle into buffers
       fill3DLineBuff(miny, maxy,
                      x1, y1, z1,
                      x2, y2, z2,
                      xbuf1_, zbuf1_);
       fill3DLineBuff(miny, maxy,
                      x2, y2, z2,
                      x3, y3, z3,
                      xbuf2_, zbuf2_);
       fill3DLineBuff(miny, maxy,
                      x1, y1, z1,
                      x3, y3, z3,
                      xbuf3_, zbuf3_);
       int y = miny;
       int p = 0;
       use_colorzbuff = true;
       if (y2 < miny) {
           if (xbuf3_[0] > xbuf2_[0]) {
               // line 13 to right of line 23 (line13 > line23)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamode(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamode(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p]);
           }
       }
       else {
           int ymiddle = y2 > maxy ? maxy : y2;
           if (xbuf3_[ymiddle-miny] > xbuf1_[ymiddle-miny]) {
               // line 13 to right of line 23 (line13 > line23)
               // do lower part of triangle
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DAlphamode(xbuf1_[p], y, xbuf3_[p], zbuf1_[p], zbuf3_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamode(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DAlphamode(xbuf3_[p], y, xbuf1_[p], zbuf3_[p], zbuf1_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamode(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p]);
           }
       }
       use_colorzbuff = false;
   }



   // fillPolygon3D and support class above by deigen
   private void fillPolygon3DAlphamode(int[] xcoords, int[] ycoords, double[] zcoords) {
       // find min and max y for polygon
       double minyd = 1e30;
       double maxyd = -1e30;
       for (int i = 0; i < ycoords.length; ++i) {
           if (ycoords[i] < minyd)
               minyd = ycoords[i];
           if (ycoords[i] > maxyd)
               maxyd = ycoords[i];
       }
       int miny = (int) minyd;
       int maxy = (int) maxyd;
       ++maxy; --miny;  // add/subtract 1 so the y coords fit fully inside the stripe
                        // clip y to screen
       if (miny >= height_ || maxy < 0 ) return;
       miny = miny < 0 ? 0 : miny;
       maxy = maxy >= height_ ? height_ - 1 : maxy;
       int size = maxy - miny + 1;
       LineBuffer[] buffers = new LineBuffer[xcoords.length+1];
	   int imax = xcoords.length - 1;
	   for (int i = 0; i < imax; ++i) {
           buffers[i] = new LineBuffer(i, size,
                                       xcoords[i], xcoords[i+1],
                                       ycoords[i], ycoords[i+1],
									   zcoords[i], zcoords[i+1]);
		   fill3DLineBuff(miny, maxy,
						  xcoords[i], ycoords[i], zcoords[i],
						  xcoords[i+1], ycoords[i+1], zcoords[i+1],
						  buffers[i].xbuff,
						  buffers[i].zbuff);
		   // if horizontal, make sure the buffer value is the smaller x (needed to sort in the right order)
		   if (ycoords[i] == ycoords[i+1]) {
			   buffers[i].xbuff[ycoords[i]-miny] = xcoords[i] < xcoords[i+1] ? xcoords[i] : xcoords[i+1];
			   buffers[i].zbuff[ycoords[i]-miny] = xcoords[i] < xcoords[i+1] ? zcoords[i] : zcoords[i+1];
		   }
       }
       if (imax >= 0) {
           buffers[imax] = new LineBuffer(imax, size,
                                          xcoords[imax], xcoords[0],
                                          ycoords[imax], ycoords[0],
										  zcoords[imax], zcoords[0]);
		   fill3DLineBuff(miny, maxy,
						  xcoords[imax], ycoords[imax], zcoords[imax],
						  xcoords[0], ycoords[0], zcoords[0],
						  buffers[imax].xbuff,
						  buffers[imax].zbuff);
		   // if horizontal, make sure the buffer value is the smaller x (needed to sort in the right order)
		   if (ycoords[imax] == ycoords[0]) {
			   buffers[imax].xbuff[ycoords[imax]-miny] = xcoords[imax] < xcoords[0] ? xcoords[imax] : xcoords[0];
			   buffers[imax].zbuff[ycoords[imax]-miny] = xcoords[imax] < xcoords[0] ? zcoords[imax] : zcoords[0];
		   }
       }
	   // dummy buffer so there's no edge case in scanline active edge checking
	   buffers[imax+1] = new LineBuffer(imax<<1,0,0,0,MAXINT,MAXINT,0,0);
	   // sort buffers by y-coord
	   boolean sorted = false;
	   while (!sorted) {
		   sorted = true;
		   for (int u = 1; u < buffers.length; ++u) {
			   LineBuffer b1 = buffers[u-1];
			   LineBuffer b2 = buffers[u];
			   if (b2.ymin < b1.ymin) {
				   // swap
				   sorted = false;
				   buffers[u-1] = b2;
				   buffers[u] = b1;
			   }
		   }
	   }
       // use horizontal scan lines (moving vertically)
       int j = 0; // index of scan line in x and z line buffers
	   int activeEdgeMax = 0; // one beyond the max index for active edges
	   int nextActiveEdgeYmax = MAXINT;
	   LineBuffer[] activeEdges = new LineBuffer[buffers.length];
	   int nactiveEdges = 0;
	   for (int y = miny; y <= maxy; ++y) {
		   // check if we reached the beginning of inactive edges, or the end of any active edges
		   while (buffers[activeEdgeMax].ymin <= y) {
			   LineBuffer b = buffers[activeEdgeMax];
			   if (b.ymax < nextActiveEdgeYmax)
				   nextActiveEdgeYmax = b.ymax;
			   activeEdges[nactiveEdges++] = b;
			   activeEdgeMax++;
		   }
		   while (nextActiveEdgeYmax < y) {
			   nextActiveEdgeYmax = MAXINT;
			   for (int i = 0; i < nactiveEdges; ++i) {
				   if (activeEdges[i].ymax < y) {
					   activeEdges[i] = activeEdges[--nactiveEdges];
					   --i; // want to repeat this entry, since it was just swapped
				   }
				   else {
					   if (activeEdges[i].ymax < nextActiveEdgeYmax)
						   nextActiveEdgeYmax = activeEdges[i].ymax;
				   }
			   }
		   }
		   // make sure buffers are sorted by the x coords for this scanline
		   sorted = false;
		   while (!sorted) {
			   sorted = true;
			   for (int u = 1; u < nactiveEdges; ++u) {
				   LineBuffer b1 = activeEdges[u-1];
				   LineBuffer b2 = activeEdges[u];
				   if (b2.xbuff[j] < b1.xbuff[j]) {
					   // swap
					   sorted = false;
					   activeEdges[u-1] = b2;
					   activeEdges[u] = b1;
				   }
			   }
		   }
		   // draw scanline
		   boolean inPolygon = false;
		   boolean horizontal = false;
		   LineBuffer horizontalStartB = null;
		   for (int i = 0; i < nactiveEdges-1; ++i) {
			   LineBuffer b1 = activeEdges[i];
			   LineBuffer b2 = activeEdges[i+1];
			   if (horizontal) { 
				   if (b2.y1 != b2.y2) {
					   // end of horizontal stretch: reset b1 to what it was before the horizontals,
					   // and do a vertex enter/leave check (horizontal line like extended vertex)
					   horizontal = false;
					   b1 = horizontalStartB;
					   int b1y = b1.y1 == y ? b1.y2 : b1.y1;
					   int b2y = b2.y1 == y ? b2.y2 : b2.y1;
					   if (!((b1y > y && b2y < y) || (b1y < y && b2y > y)))
						   // crossing bdy
						   inPolygon = !inPolygon;
				   }
				   else continue;
			   }
			   inPolygon = !inPolygon;
			   if (b1.xbuff[j] == b2.xbuff[j]) {
				   // vertex or self intersection
				   // find the other edge for the vertex (if any)
				   // make b1,b2 be the vertex edge pair. Move edges if necessary in active edge table
				   LineBuffer e;
				   boolean isVertex = false;
				   for (int u = i+1; u < nactiveEdges; ++u) {
					   if ((e = activeEdges[u]).xbuff[j] != b1.xbuff[j])
						   break; // done going through the active edges with same x coord (list is sorted by x)
					   if ( (e.index == (b1.index+1)%(imax+1) && e.y1 == b1.y2 && e.x1 == b1.x2 && e.y1 == y) ) {
						   isVertex = true;
						   activeEdges[u] = b2;
						   activeEdges[i+1] = b2 = e;
						   break;
					   }
					   else if ( (b1.index == (e.index+1)%(imax+1) && b1.y1 == e.y2 && b1.x1 == e.x2 && e.y2 == y) ) {
						   isVertex = true;
						   activeEdges[u] = b2;
						   activeEdges[i+1] = b2 = b1;
						   activeEdges[i] = b1 = e;
						   break;
					   }
				   }
				   if (isVertex) {
					   if ((b1.y1 > y && b2.y2 < y) || (b1.y1 < y && b2.y2 > y))
						   // crossing bdy: want next iteration to be opposite inPolygon status as this iter
						   inPolygon = !inPolygon;
					   if (b1.y1 == b1.y2 || b2.y1 == b2.y2) {
						   horizontal = true;
						   horizontalStartB = b1.y1 == b1.y2 ? b2 : b1;
					   }
				   }
			   }
			   if (inPolygon)
				   drawHLine3DAlphamode(b1.xbuff[j], y, b2.xbuff[j], b1.zbuff[j], b2.zbuff[j]);
		   }
		   ++j;
	   } // end for each horizontal scanline
   }
   
   
   

   // NOTE: requires x1 <= x2
   private void drawHLine3DAlphamode(int x1, int y, int x2, double z1, double z2)
   {
       if (y < 0 || y >= height_) throw new RuntimeException("HLine: y outside bounds");
       if (x2 < 0 || (x1 >= width_)) return; // whole segment is off screen
       if (x1 < 0) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = -x2double/(x1double - x2double);
           z1 = (z1double - z2double)*s + z2double;
           x1 = 0;
       }
       if (x2 >= width_) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = (width_-1-x1double)/(x2double - x1double);
           z2 = (z2double - z1double)*s + z1double;
           x2 = width_ - 1;
       }
       int x = x1;
       double z = z1;
       int xdist = x2 - x1;
       double dz = (z2-z1)/(double)xdist;
       int p = x + y * width_;
	   int p2 = p + xdist;
       for(; p <= p2; ++p) {
           if (z > zbuffer_[p])
               setPixelAlphamode(p,z);
           z += dz;
       }
   }       






   

   

   // ********************** ALPHA BLENDING AND SHADING ************************ //


   // r,g,b should be in [0,255]. alpha should be in [0,1].
   private void setPixelAlphamodeShade(int p, double z,
                                       float r, float g, float b, float a) {
       if (use_colorzbuff && Math.abs(z - colorzbuffer_[p]) < setpixel_ztol)
           return;
       if (use_colorzbuff && z > colorzbuffer_[p])
           colorzbuffer_[p] = z;
       float alphainv = 1.f - a;
       int ipx = image_[p];
       int ir = (int) (((float) ((ipx >> 16) & 0xFF)) * alphainv);
       int ig = (int) (((float) ((ipx >>  8) & 0xFF)) * alphainv);
       int ib = (int) (((float) ( ipx        & 0xFF)) * alphainv);
       if (ir < 0 || ig < 0 || ib < 0)
           throw new RuntimeException("Pix color vals cannot be negative.");
//       image_[p] = color_premult + ((ir << 16) | (ig << 8) | ib);
       int cr = (int) (r*a);
       int cg = (int) (g*a);
       int cb = (int) (b*a);
       int ar = ir + cr;
       int ag = ig + cg;
       int ab = ib + cb;
       ar = ar > 255 ? 255 : ar;
       ag = ag > 255 ? 255 : ag;
       ab = ab > 255 ? 255 : ab;
       image_[p] = 0xFF000000 | (ar << 16) | (ag << 8) | ab;
   }
   
   // r,g,b should be in [0,255]. alpha should be in [0,1].
   private void drawLine3DAlphamodeShade(int x1, int y1, double z1,
                                         float r1, float g1, float b1, float a1,
                                         int x2, int y2, double z2,
                                         float r2, float g2, float b2, float a2)
   {
      // if the line is entirely off of one side of the screen, return
      // the rest of the clipping and deciding if the line is outside won't deal with some of those cases
      if ( (x1 < 0 && x2 < 0) || (x1 >= width_ && x2 >= width_) ||
           (y1 < 0 && y2 < 0) || (y1 >= height_ && y2 >= height_) )
                return;
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;
      if (x1double < 0) {
          // clip x1double,y1double,z1double
          double s = (-x2double)/(x1double - x2double);
          z1double = (z1double - z2double)*s + z2double;
          y1double = (y1double - y2double)*s + y2double;
          r1 = ((float) ((r1 - r2)*s)) + r2;
          g1 = ((float) ((g1 - g2)*s)) + g2;
          b1 = ((float) ((b1 - b2)*s)) + b2;
          a1 = ((float) ((a1 - a2)*s)) + a2;
          x1double = 0;
      }
      else if (x1double >= width_) {
          // clip x1double,y1double,z1double
          double s = (width_-1-x2double)/(x1double - x2double);
          z1double = (z1double - z2double)*s + z2double;
          y1double = (y1double - y2double)*s + y2double;
          r1 = ((float) ((r1 - r2)*s)) + r2;
          g1 = ((float) ((g1 - g2)*s)) + g2;
          b1 = ((float) ((b1 - b2)*s)) + b2;
          a1 = ((float) ((a1 - a2)*s)) + a2;
          x1double = width_ - 1;
      }
      if (y1double < 0) {
          // clip x1double,y1double,z1double
          if (y1double == y2double) return;
          double s = (-y2double)/(y1double - y2double);
          z1double = (z1double - z2double)*s + z2double;
          x1double = (x1double - x2double)*s + x2double;
          r1 = ((float) ((r1 - r2)*s)) + r2;
          g1 = ((float) ((g1 - g2)*s)) + g2;
          b1 = ((float) ((b1 - b2)*s)) + b2;
          a1 = ((float) ((a1 - a2)*s)) + a2;
          y1double = 0;
      }
      else if (y1double >= height_) {
          // clip x1double,y1double,z1double
          if (y1double == y2double) return;
          double s = (height_-1-y2double)/(y1double - y2double);
          z1double = (z1double - z2double)*s + z2double;
          x1double = (x1double - x2double)*s + x2double;
          r1 = ((float) ((r1 - r2)*s)) + r2;
          g1 = ((float) ((g1 - g2)*s)) + g2;
          b1 = ((float) ((b1 - b2)*s)) + b2;
          a1 = ((float) ((a1 - a2)*s)) + a2;
          y1double = height_ - 1;
      }

      if (x2double < 0) {
          // clip x2double,y2double,z2double
          if (x2double == x1double) return;
          double s = (-x1double)/(x2double - x1double);
          z2double = (z2double - z1double)*s + z1double;
          y2double = (y2double - y1double)*s + y1double;
          r2 = ((float) ((r2 - r1)*s)) + r1;
          g2 = ((float) ((g2 - g1)*s)) + g1;
          b2 = ((float) ((b2 - b1)*s)) + b1;
          a2 = ((float) ((a2 - a1)*s)) + a1;
          x2double = 0;
      }
      else if (x2double >= width_) {
          // clip x2double,y2double,z2double
          if (x2double == x1double) return;
          double s = (width_-1-x1double)/(x2double - x1double);
          z2double = (z2double - z1double)*s + z1double;
          y2double = (y2double - y1double)*s + y1double;
          r2 = ((float) ((r2 - r1)*s)) + r1;
          g2 = ((float) ((g2 - g1)*s)) + g1;
          b2 = ((float) ((b2 - b1)*s)) + b1;
          a2 = ((float) ((a2 - a1)*s)) + a1;
          x2double = width_ - 1;
      }
      if (y2double < 0) {
          // clip x2double,y2double,z2double
          if (y1double == y2double) return;
          double s = (-y1double)/(y2double - y1double);
          z2double = (z2double - z1double)*s + z1double;
          x2double = (x2double - x1double)*s + x1double;
          r2 = ((float) ((r2 - r1)*s)) + r1;
          g2 = ((float) ((g2 - g1)*s)) + g1;
          b2 = ((float) ((b2 - b1)*s)) + b1;
          a2 = ((float) ((a2 - a1)*s)) + a1;
          y2double = 0;
      }
      else if (y2double >= height_) {
          // clip x2double,y2double,z2double
          if (y1double == y2double) return;
          double s = (height_-1-y1double)/(y2double - y1double);
          z2double = (z2double - z1double)*s + z1double;
          x2double = (x2double - x1double)*s + x1double;
          r2 = ((float) ((r2 - r1)*s)) + r1;
          g2 = ((float) ((g2 - g1)*s)) + g1;
          b2 = ((float) ((b2 - b1)*s)) + b1;
          a2 = ((float) ((a2 - a1)*s)) + a1;
          y2double = height_ - 1;
      }

      x1 = (int) Math.round(x1double); x2 = (int) Math.round(x2double);
      y1 = (int) Math.round(y1double); y2 = (int) Math.round(y2double);
      z1 = z1double; z2 = z2double;

      // possible that the line is entirely outside, maybe with a pixel inside a corner of the screen
      if ( x1 < 0 || x2 < 0 || x1 >= width_ || x2 >= width_
           || y1 < 0 || y2 < 0 || y1 >= height_ || y2 >= height_ )
          return;
      
      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;

      r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
      g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
      b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
      a1 = a1 >   1 ?   1 : a1 < 0 ? 0 : a1;
      r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
      g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
      b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;
      a2 = a2 >   1 ?   1 : a2 < 0 ? 0 : a2;

      float r = r1, g = g1, b = b1, a = a1;
      float dr = r2 - r1;
      float dg = g2 - g1;
      float db = b2 - b1;
      float da = a2 - a1;
      
      int p = x + (y * width_);
      int dx, dy, dsx, dsy, d2x, d2y, dpx, dpy;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;
      dpx = dsx;
      dpy = dsy * width_;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;

      d2x = dx << 1;
      d2y = dy << 1;

      double dd = (double) (dx > dy ? dx : dy);
      dz /= dd;
      dr /= dd;
      dg /= dd;
      db /= dd;
      da /= dd;
      
//      setpixel_ztol = LINE_SETPIX_ZTOL;

      if (dy == dx)
      {
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamode(p,z);
        }
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dpx + dpy;
            z += dz;
            r += dr;
            g += dg;
            b += db;
            a += da;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamodeShade(p,z,r,g,b,a);
            }
         }
      }
      else if (dy < dx)
      {
         d = d2y - dx;
         goE = d2y;
         goNE = d2y - d2x;
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamodeShade(p,z,r,g,b,a);
        }
         while (x != x2)
         {
            if (d < 0)
            {
               d += goE;
               x += dsx;
               p += dpx;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
            a += da;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamodeShade(p,z,r,g,b,a);
            }
         }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
        if (z > zbuffer_[p] + drawline_ztol)
        {
            setPixelAlphamodeShade(p,z,r,g,b,a);
            zbuffer_[p] = z;
        }
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dpy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dpx + dpy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
            a += da;
            if (z > zbuffer_[p] + drawline_ztol)
            {
                setPixelAlphamodeShade(p,z,r,g,b,a);
            }
         }
      }
   }


   // r,g,b should be in [0,255]. alpha should be in [0,1].
   private void fillTriangle3DAlphamodeShade(int x1, int y1, double z1,
                                             float r1, float g1, float b1, float a1,
                                             int x2, int y2, double z2,
                                             float r2, float g2, float b2, float a2,
                                             int x3, int y3, double z3,
                                             float r3, float g3, float b3, float a3)
   {
       int tmpi; double tmpd; float tmpf;
       // sort so y is increasing
       if (y1 >= y2 && y1 >= y3) {
           // y1 largest
           tmpi = x3;  x3 = x1;  x1 = tmpi;
           tmpi = y3;  y3 = y1;  y1 = tmpi;
           tmpd = z3;  z3 = z1;  z1 = tmpd;
           tmpf = r3;  r3 = r1;  r1 = tmpf;
           tmpf = g3;  g3 = g1;  g1 = tmpf;
           tmpf = b3;  b3 = b1;  b1 = tmpf;
           tmpf = a3;  a3 = a1;  a1 = tmpf;
       }
       else if (y2 >= y1 && y2 >= y3) {
           // y2 largest
           tmpi = x3;  x3 = x2;  x2 = tmpi;
           tmpi = y3;  y3 = y2;  y2 = tmpi;
           tmpd = z3;  z3 = z2;  z2 = tmpd;
           tmpf = r3;  r3 = r2;  r2 = tmpf;
           tmpf = g3;  g3 = g2;  g2 = tmpf;
           tmpf = b3;  b3 = b2;  b2 = tmpf;
           tmpf = a3;  a3 = a2;  a2 = tmpf;
       }
       // else y3 largest, so don't swap
       if (y1 > y2) {
           // swap so smallest is in y1
           tmpi = x1;  x1 = x2;  x2 = tmpi;
           tmpi = y1;  y1 = y2;  y2 = tmpi;
           tmpd = z1;  z1 = z2;  z2 = tmpd;
           tmpf = r1;  r1 = r2;  r2 = tmpf;
           tmpf = g1;  g1 = g2;  g2 = tmpf;
           tmpf = b1;  b1 = b2;  b2 = tmpf;
           tmpf = a1;  a1 = a2;  a2 = tmpf;
       }
       // now we're sorted in increasing y
       if (y1 >= height_ || y3 < 0) return; // outside the screen
                                            // get clipped y
       int maxy = y3 > height_-1 ? height_-1 : y3;
       int miny = y1 < 0 ? 0 : y1;
       // put boundary of triangle into buffers
       fill3DLineBuffAlphamodeShade(miny, maxy,
                                    x1, y1, z1, r1, g1, b1, a1,
                                    x2, y2, z2, r2, g2, b2, a2,
                                    xbuf1_, zbuf1_, rbuf1_, gbuf1_, bbuf1_, abuf1_);
       fill3DLineBuffAlphamodeShade(miny, maxy,
                                    x2, y2, z2, r2, g2, b2, a2,
                                    x3, y3, z3, r3, g3, b3, a3,
                                    xbuf2_, zbuf2_, rbuf2_, gbuf2_, bbuf2_, abuf2_);
       fill3DLineBuffAlphamodeShade(miny, maxy,
                                    x1, y1, z1, r1, g1, b1, a1,
                                    x3, y3, z3, r3, g3, b3, a3,
                                    xbuf3_, zbuf3_, rbuf3_, gbuf3_, bbuf3_, abuf3_);
       int y = miny;
       int p = 0;
       use_colorzbuff = true;
       if (y2 < miny) {
           if (xbuf3_[0] > xbuf2_[0]) {
               // line 13 to right of line 23 (line13 > line23)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p],
                                             rbuf2_[p], gbuf2_[p], bbuf2_[p], abuf2_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p],
                                             rbuf2_[p], gbuf2_[p], bbuf2_[p], abuf2_[p]);
           }
       }
       else {
           int ymiddle = y2 > maxy ? maxy : y2;
           if (xbuf3_[ymiddle-miny] > xbuf1_[ymiddle-miny]) {
               // line 13 to right of line 23 (line13 > line23)
               // do lower part of triangle
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf1_[p], y, xbuf3_[p], zbuf1_[p], zbuf3_[p],
                                             rbuf1_[p], gbuf1_[p], bbuf1_[p], abuf1_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf2_[p], y, xbuf3_[p], zbuf2_[p], zbuf3_[p],
                                             rbuf2_[p], gbuf2_[p], bbuf2_[p], abuf2_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p]);
           }
           else {
               // line 13 to left of line 12 (line13 < line12)
               for(; y <= ymiddle; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf3_[p], y, xbuf1_[p], zbuf3_[p], zbuf1_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p],
                                             rbuf1_[p], gbuf1_[p], bbuf1_[p], abuf1_[p]);
               // do upper part of triangle
               for (; y <= maxy; ++y, ++p)
                   drawHLine3DAlphamodeShade(xbuf3_[p], y, xbuf2_[p], zbuf3_[p], zbuf2_[p],
                                             rbuf3_[p], gbuf3_[p], bbuf3_[p], abuf3_[p],
                                             rbuf2_[p], gbuf2_[p], bbuf2_[p], abuf2_[p]);
           }
       }
       use_colorzbuff = false;
   }


   private void fill3DLineBuffAlphamodeShade(
                                    int miny, int maxy,
                                    int x1, int y1, double z1,
                                    float r1, float g1, float b1, float a1,
                                    int x2, int y2, double z2,
                                    float r2, float g2, float b2, float a2,
                                    int xbuff[],
                                    double zbuff[],
                                    float[] rbuff, float[] gbuff, float[] bbuff, float[] abuff)
   {
       if (y1 == y2) {
           xbuff[0] = x2;
           zbuff[0] = z2;
           rbuff[0] = r2;
           gbuff[0] = g2;
           bbuff[0] = b2;
           abuff[0] = a2;
           return;
       }
      // clip to region
      double x1double=x1, x2double=x2, y1double=y1, y2double=y2, z1double=z1, z2double=z2;  
      if (y1 < miny) {
        // clip x1double,y1double,z1double
        double s = (miny-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = miny;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
        r1 = ((float) ((r1 - r2)*s)) + r2;
        g1 = ((float) ((g1 - g2)*s)) + g2;
        b1 = ((float) ((b1 - b2)*s)) + b2;
        a1 = ((float) ((a1 - a2)*s)) + a2;
      }
      else if (y1 > maxy) {
        // clip x1double,y1double,z1double
        double s = (maxy-y2double)/(y1double - y2double);
        z1double = (z1double - z2double)*s + z2double;
        x1double = (x1double - x2double)*s + x2double;
        y1 = maxy;
        x1 = (int) Math.round(x1double);
        z1 = z1double;
        r1 = ((float) ((r1 - r2)*s)) + r2;
        g1 = ((float) ((g1 - g2)*s)) + g2;
        b1 = ((float) ((b1 - b2)*s)) + b2;
        a1 = ((float) ((a1 - a2)*s)) + a2;
      }
      if (y2 < miny) {
        // clip x2double,y2double,z2double
        double s = (miny-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = miny;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
        r2 = ((float) ((r2 - r1)*s)) + r1;
        g2 = ((float) ((g2 - g1)*s)) + g1;
        b2 = ((float) ((b2 - b1)*s)) + b1;
        a2 = ((float) ((a2 - a1)*s)) + a1;
      }
      else if (y2 > maxy) {
        // clip x2double,y2double,z2double
        if (y1double == y2double) return;
        double s = (maxy-y1double)/(y2double - y1double);
        z2double = (z2double - z1double)*s + z1double;
        x2double = (x2double - x1double)*s + x1double;
        y2 = maxy;
        x2 = (int) Math.round(x2double);
        z2 = z2double;
        r2 = ((float) ((r2 - r1)*s)) + r1;
        g2 = ((float) ((g2 - g1)*s)) + g1;
        b2 = ((float) ((b2 - b1)*s)) + b1;
        a2 = ((float) ((a2 - a1)*s)) + a1;
      }
         
      int x = x1;
      int y = y1;
      double z = z1;
      double dz = z2 - z1;

      r1 = r1 > 255 ? 255 : r1 < 0 ? 0 : r1;
      g1 = g1 > 255 ? 255 : g1 < 0 ? 0 : g1;
      b1 = b1 > 255 ? 255 : b1 < 0 ? 0 : b1;
      a1 = a1 >   1 ?   1 : a1 < 0 ? 0 : a1;
      r2 = r2 > 255 ? 255 : r2 < 0 ? 0 : r2;
      g2 = g2 > 255 ? 255 : g2 < 0 ? 0 : g2;
      b2 = b2 > 255 ? 255 : b2 < 0 ? 0 : b2;
      a2 = a2 >   1 ?   1 : a2 < 0 ? 0 : a2;

      float r = r1, g = g1, b = b1, a = a1;
      float dr = r2 - r1;
      float dg = g2 - g1;
      float db = b2 - b1;
      float da = a2 - a1;

      int p = (y - miny);
      int dx, dy, dsx, dsy, d2x, d2y;
      int d, goE, goN, goNE;

      dx = x2 - x1;
      dy = y2 - y1;
      dsx = dx > 0 ? 1 : -1;
      dsy = dy > 0 ? 1 : -1;

      dy = dy < 0 ? -dy : dy;
      dx = dx < 0 ? -dx : dx;
      d2x = dx << 1;
      d2y = dy << 1;

      double dd = (double) (dx > dy ? dx : dy);
      dr /= dd;
      dg /= dd;
      db /= dd;
      da /= dd;
      dz /= dd;

      if (dy == dx)
      {
//         {
            xbuff[p] = x;
            zbuff[p] = z;
            rbuff[p] = r;
            gbuff[p] = g;
            bbuff[p] = b;
            abuff[p] = a;
//         }
         while (x != x2)
         {
            x += dsx;
            y += dsy;
            p += dsy;
            z += dz;
            r += dr;
            g += dg;
            b += db;
            a += da;
               xbuff[p] = x;
               zbuff[p] = z;
               rbuff[p] = r;
               gbuff[p] = g;
               bbuff[p] = b;
               abuff[p] = a;
         }
      }
      else if (dy < dx)
      {
          d = d2y - dx;
          goE = d2y;
          goNE = d2y - d2x;
          xbuff[p] = x;
          zbuff[p] = z;
          rbuff[p] = r;
          gbuff[p] = g;
          bbuff[p] = b;
          abuff[p] = a;
          while (y != y2)
          {
              if (d < 0)
              {
                  int numincrements = (int) Math.ceil(((double) -d)/(double) goE);
                  d += goE * numincrements;
                  x += dsx * numincrements;
                  z += dz * numincrements;
                  r += dr * numincrements;
                  g += dg * numincrements;
                  b += db * numincrements;
                  a += da * numincrements;
              }
              else
              {
                  d += goNE;
                  x += dsx;
                  y += dsy;
                  p += dsy;
                  z += dz;
                  r += dr;
                  g += dg;
                  b += db;
                  a += da;
              }
              xbuff[p] = x;
              zbuff[p] = z;
              rbuff[p] = r;
              gbuff[p] = g;
              bbuff[p] = b;
              abuff[p] = a;
          }
          if (x != x2)
          {
              // didn't quite get to x2 by the time we got to y2.
              // (x2,y2) is on y2 horizontal line, so go for as many
              // increments as it would take to get to x2.
              int numincrements = x < x2 ? x2 - x : x - x2;
              // d += goE * numincrements; // d doesn't matter to us
              x += dsx * numincrements;
              z += dz * numincrements;
              r += dr * numincrements;
              g += dg * numincrements;
              b += db * numincrements;
              a += da * numincrements;
              xbuff[p] = x;
              zbuff[p] = z;
              rbuff[p] = r;
              gbuff[p] = g;
              bbuff[p] = b;
              abuff[p] = a;
          }
      }
      else // (dx < dy)
      {
         d = d2x - dy;
         goN = d2x;
         goNE = d2x - d2y;
//         {
           xbuff[p] = x;
           zbuff[p] = z;
           rbuff[p] = r;
           gbuff[p] = g;
           bbuff[p] = b;
           abuff[p] = a;
//         }
         while (y != y2)
         {
            if (d < 0)
            {
               d += goN;
               y += dsy;
               p += dsy;
            }
            else
            {
               d += goNE;
               x += dsx;
               y += dsy;
               p += dsy;
            }
            z += dz;
            r += dr;
            g += dg;
            b += db;
            a += da;
               xbuff[p] = x;
               zbuff[p] = z;
               rbuff[p] = r;
               gbuff[p] = g;
               bbuff[p] = b;
               abuff[p] = a;
         }
      }
   }


   

   // r,g,b should be in [0,255]. alpha should be in [0,1].
   // NOTE: requires x1 <= x2
   private void drawHLine3DAlphamodeShade(int x1, int y, int x2,
                                          double z1, double z2,
                                          float r1, float g1, float b1, float a1,
                                          float r2, float g2, float b2, float a2)
   {

       if (y < 0 || y >= height_) throw new RuntimeException("HLine: y outside bounds");
       if (x2 < 0 || (x1 >= width_)) return; // whole segment is off screen
       if (x1 < 0) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = -x2double/(x1double - x2double);
           z1 = (z1double - z2double)*s + z2double;
           r1 = ((float) ((r1 - r2)*s)) + r2;
           g1 = ((float) ((g1 - g2)*s)) + g2;
           b1 = ((float) ((b1 - b2)*s)) + b2;
           a1 = ((float) ((a1 - a2)*s)) + a2;
           x1 = 0;
       }
       if (x2 >= width_) {
           double x1double = x1, x2double = x2, z1double = z1, z2double = z2;
           double s = (width_-1-x1double)/(x2double - x1double);
           z2 = (z2double - z1double)*s + z1double;
           r2 = ((float) ((r2 - r1)*s)) + r1;
           g2 = ((float) ((g2 - g1)*s)) + g1;
           b2 = ((float) ((b2 - b1)*s)) + b1;
           a2 = ((float) ((a2 - a1)*s)) + a1;
           x2 = width_ - 1;
       }
       int x = x1;
       double z = z1;
       float r = r1, g = g1, b = b1, a = a1;
       int xdist = x2 - x1;
       double dz = (z2-z1)/(double)xdist;
       float  dr = (r2-r1)/(float)xdist,
              dg = (g2-g1)/(float)xdist,
              db = (b2-b1)/(float)xdist,
              da = (a2-a1)/(float)xdist;
       int p = x + y * width_;
       for(; x <= x2; ++x, ++p) {
           if (z > zbuffer_[p])
               setPixelAlphamodeShade(p,z,r,g,b,a);
           z += dz;
           r += dr;
           g += dg;
           b += db;
           a += da;
       }
   }

   

}
