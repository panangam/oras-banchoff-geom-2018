package demo.graph;

import demo.*;
import demo.io.*;
import demo.gfx.*;
import demo.util.M;
import demo.plot.Plot;
import demo.gfx.drawable.Drawable3D;

/**
 * Graph3D is currently the only implementation of Graph.
 * It is a basic 3D graph. Normally, transformations are used for rotation, and scaling is used for scaling.
 * No other transformations are usually applied.
 *
 * @author deigen
 */
public class Graph3D extends Graph implements FileObject {

    public  Graph3D() {
        super();

            this .transformations = new Matrix4D();
            this .transformations_inv = new Matrix4D();
            transformations .set( Matrix .IDENTITY( 4 ) );
            transformations_inv .set( Matrix .IDENTITY( 4 ) );
    }

    // reset flag saying that we're not sorted when we add/remove plots
    public void addPlot( Plot plot ) {
        sorted = zmaxsorted = false;
        super.addPlot(plot);
    }
    

    // the current rotation and scaling
    private  Matrix4D transformations;
    private  Matrix4D transformations_inv;
        
    private  double scale = 1;

    /**
     * @return the matrix of the current transformations of the graph
     */
    public  Matrix4D getTransformations() {
        return transformations;
    }

    /**
     * @return the matrix of inverse of the current transformations of the graph
     */
    public  Matrix4D getInverseTransformations() {
        return transformations_inv;
    }

    /**
     * @return the current scale of the graph
     */
    public  double getScale() {
        return scale;
    }

    public synchronized  void setTransformation( Matrix4D m ) {
        transform( transformations_inv.multiplyOnLeftBy( m ) );
    }

    public synchronized  void setScale( double scale ) {
        scale( scale / this .scale );
    }

    public synchronized  void resetTransformations() {
        scale( 1 / scale );
        transform( transformations_inv );
    }

    /**
     * Rotates around the X axis (horizontal).
     * @param theta the amount to rotate by in radians
     */
    public synchronized  void rotateX( double theta ) {
        rotate( new RotationMatrix4D( theta, 0, 0 ) );
    }

    /**
     * Rotates around the Y axis (vertical).
     * @param theta the amount to rotate by in radians
     */
    public synchronized  void rotateY( double theta ) {
        rotate( new RotationMatrix4D( 0, theta, 0 ) );
    }
    
    /**
     * Rotates around the Z axis (coming out from the screen).
     * @param theta the amount to rotate by in radians
     */
    public synchronized  void rotateZ( double theta ) {
        rotate( new RotationMatrix4D( 0, 0, theta ) );
    }

    public synchronized  void transform( Matrix4D m ) {
        transformations = (Matrix4D) transformations .multiplyOnLeftBy( m );
        transformations_inv = transformations.inverse();
    }

    public Matrix4D getTransformation() {
        return transformations;
    }

    /**
     * Rotates by a rotation matrix. Same as transform(m).
     * @param m the rotation matrix
     */
    public synchronized  void rotate( RotationMatrix4D m ) {
        transform( m );
    }

    public synchronized  void scale( double factor ) {
        scale *= factor;
    }


    protected synchronized void applyTransformations() {
        this.sorted = zmaxsorted = false;
        Matrix4D m = transformations.multiplyOnRightBy(new Matrix4D(new double[]{
                                                                    scale,0,0,0,
                                                                    0,scale,0,0,
                                                                    0,0,scale,0,
                                                                    0,0,0,1}));
        Transformation t = new Transformation(m);
        for (int i = 0; i < points.length; ++i)
            points[i].transform(t);
        for (int i = 0; i < lightingVectors.length; ++i)
            lightingVectors[i].transform(t);
    }
    
    public synchronized  void draw( java.awt .Graphics g ) {
        applyTransformations();
        resetDrewState();
        if ( drawSuspended ) {
            drawSuspended( g );
        }
        else {
            if (useLighting)
                applyLighting();
            if ( drawingMode == NONE ) {
                drawOpen( g );
            }
            else {
                if ( ! sorted ) {
                    sortPoints();
                }
                switch (surfaceMode)
                {
                case Graph3D .OPEN :
                    drawOpen( g );
                    break;
                
                case Graph3D .FILLED_FRAME :
                    drawFilledFramed( g );
                    break;
                
                case Graph3D .FILLED_NOFRAME :
                    drawFilled( g );
                    break;
                
                default :
                    drawOpen( g );
                    break;
                }
            }
        }
    }
    
    public synchronized  void draw( ZBufferedImage zimg ) {
        applyTransformations();
        resetDrewState();
        if ( drawSuspended ) {
            drawSuspended( zimg );
        }
        else {
            if (useLighting)
                applyLighting();
            switch (surfaceMode)
            {
            case Graph3D .OPEN :
                drawOpen( zimg );
                break;
            
            case Graph3D .FILLED_FRAME :
                if (useAlphaBlending)
                    drawFilledFramedTransp( zimg );
                else
                    drawFilledFramed( zimg );
                break;
            
            case Graph3D .FILLED_NOFRAME :
                if (useAlphaBlending)
                    drawFilledTransp( zimg );
                else
                    drawFilled( zimg );
                break;
            
            default :
                drawOpen( zimg );
                break;
            }
        }
    }

    /**
     * Sets the surface mode of the graph. That is, the way polygons are drawn.
     * @param mode the mode (see surface mode constants for values).
     */
    public  void setSurfaceMode( int mode ) {
        // check if mode is out of range
        if ( mode != OPEN && mode != FILLED_FRAME && mode != FILLED_NOFRAME ) {
            mode = OPEN;
        }
        surfaceMode = mode;
    }

    /**
     * Sets the drawing mode of the graph. That is, how objects should
     * be drawn to a java.awt.Graphics.
     * @param mode the drawing mode (see drawing mode constants for values)
     */
    public  void setDrawingMode( int mode ) {
        // check if mode is out of range
        if ( mode != NONE && mode != SORT && mode != SPLIT ) {
            mode = NONE;
        }
        drawingMode = mode;
    }

    /**
     * @return the surface mode
     */
    public  int getSurfaceMode() {
        return surfaceMode;
    }

    /**
     * @return the drawing mode
     */
    public  int getDrawingMode() {
        return drawingMode;
    }
    
    /**
     * sets the direction the light goes in
     * @param lightDir a double[3] of the direction of the light.
     *        light will be come in in lightDir and -lightDir
     */
    public void setLightDirection(double[] lightDir) {
        this.lightDir = lightDir;
    }
    
    /**
     * sets whether or not to use lighting
     */
    public void useLighting(boolean b) {
        if (useLighting && !b)
            removeLighting();
        this.useLighting = b;
    }
    
    /**
     * returns whether or not lighting is being used
     */
    public boolean useLighting() {
        return this.useLighting;
    }

    /**
     * sets whether or not to draw translucent objects with alpha blending
     */
    public void useAlphaBlending(boolean b) {
        useAlphaBlending = b;
    }

    /**
     * returns whether or not translucent objects are drawn with alpha blending
     */
    public boolean useAlphaBlending() {
        return useAlphaBlending;
    }

    // if the polygons are sorted since the last transformation that requires sorting before drawing
    protected  boolean sorted = false;
    protected  boolean zmaxsorted = false;

    // surface mode is open, filled, etc.
    protected  int surfaceMode = OPEN;

    /**
     * Constants for surface mode.
     */
    public static final  int OPEN = 1, FILLED_FRAME = 2, FILLED_NOFRAME = 3;

    // selects which drawing algorithm to use
    protected  int drawingMode = NONE;

    /**
     * Constants for drawing mode.
     */
    public static final  int NONE = 0, SORT = 1, SPLIT = 2;
    
    // direction the light goes in
    private double[] lightDir = M.vector(0,0,1);
    
    // whether to use lighting
    private boolean useLighting = false;

    // whether to draw translucent objects w/ alpha blending
    private boolean useAlphaBlending = false;
    
    
    /**
     * applies lighting to all drawable objects
     */
    private synchronized  void applyLighting() {
        for (int i = 0; i < lightingVectors.length; ++i)
            lightingVectors[i].setLighting(lightDir);
    }
    
    /**
     * removes lighting from all drawable objects
     */
    private synchronized  void removeLighting() {
        for (int i = 0; i < lightingVectors.length; ++i)
            lightingVectors[i].unsetLighting();
    }

    /**
     * Resets the drawn state of all drawable objects to false (so they are not drawn).
     */
    protected synchronized  void resetDrewState() {
        drawableObjects .resetEnumeration();
        while ( drawableObjects .hasMoreElements() )
            ((Drawable3D) drawableObjects .nextElement()) .resetDrewState();
    }

    protected synchronized  void drawOpen( java.awt .Graphics g ) {
        for ( int i = 0; i < points .length; ++i )
            for ( int j = 0; j < points[i] .objects .size(); ++j )
                ((Drawable3D) points[i] .objects .elementAt(j)) .drawProjectedOpen( g );
    }

    protected  synchronized 
    void drawFilledFramed( java.awt .Graphics g ) {
        for ( int i = 0; i < points .length; ++i )
            for ( int j = 0; j < points[i] .objects .size(); ++j )
                ((Drawable3D) points[i] .objects .elementAt(j)) .drawProjectedFilledFramed( g );
    }

    protected  synchronized 
    void drawFilled( java.awt .Graphics g ) {
        for ( int i = 0; i < points .length; ++i )
            for ( int j = 0; j < points[i] .objects .size(); ++j )
                ((Drawable3D) points[i] .objects .elementAt(j)) .drawProjectedFilled( g );
    }

    protected synchronized  
    void drawSuspended( java.awt .Graphics g ) {
        for ( int i = 0; i < points .length; ++i )
            for ( int j = 0; j < points[i] .objects .size(); ++j )
                ((Drawable3D) points[i] .objects .elementAt(j)) .drawProjectedSuspended( g );
    }
    
    
    protected  synchronized  void drawOpen( ZBufferedImage zimg ) {
        drawableObjects.resetEnumeration();
        while (drawableObjects.hasMoreElements())
            ((Drawable3D) drawableObjects.nextElement()) .drawProjectedOpen( zimg );
    }

    protected synchronized void drawFilledFramed( ZBufferedImage zimg ) {
        drawableObjects.resetEnumeration();
        while (drawableObjects.hasMoreElements())
            ((Drawable3D) drawableObjects.nextElement()) .drawProjectedFilledFramed( zimg );
    }

    protected synchronized void drawFilled( ZBufferedImage zimg ) {
        drawableObjects.resetEnumeration();
        while (drawableObjects.hasMoreElements())
            ((Drawable3D) drawableObjects.nextElement()) .drawProjectedFilled( zimg );
    }

    protected synchronized  
    void drawFilledFramedTransp( ZBufferedImage zimg ) {
        if (!zmaxsorted)
            sortZMaxPoints();
        zimg.setAlphamode(false);
        Drawable3D d;
        for ( int i = 0; i < zmaxpoints .length; ++i ) {
            for ( int j = 0; j < zmaxpoints[i] .objects .size(); ++j ) {
                d = ((Drawable3D) zmaxpoints[i] .objects .elementAt(j));
                if (!d.isTransparent())
                    d.drawProjectedFilledFramed( zimg );
            }
        }
        zimg.setAlphamode(true);
        for ( int i = 0; i < zmaxpoints .length; ++i ) {
            for ( int j = 0; j < zmaxpoints[i] .objects .size(); ++j ) {
                d = ((Drawable3D) zmaxpoints[i] .objects .elementAt(j));
                if (d.isTransparent() && d.zmaxPoint() == zmaxpoints[i])
                    d.drawProjectedFilledFramed( zimg );
            }
        }
        zimg.setAlphamode(false);
    }

    protected synchronized  
    void drawFilledTransp( ZBufferedImage zimg ) {
        if (!zmaxsorted)
            sortZMaxPoints();
        zimg.setAlphamode(false);
        Drawable3D d;
        for ( int i = 0; i < zmaxpoints .length; ++i ) {
            for ( int j = 0; j < zmaxpoints[i] .objects .size(); ++j ) {
                d = ((Drawable3D) zmaxpoints[i] .objects .elementAt(j));
                if (!d.isTransparent())
                    d.drawProjectedFilled( zimg );
            }
        }
        zimg.setAlphamode(true);
        for ( int i = 0; i < zmaxpoints .length; ++i ) {
            for ( int j = 0; j < zmaxpoints[i] .objects .size(); ++j ) {
                d = ((Drawable3D) zmaxpoints[i] .objects .elementAt(j));
                if (d.isTransparent() && d.zmaxPoint() == zmaxpoints[i])
                    d.drawProjectedFilled( zimg );
            }
        }
        zimg.setAlphamode(false);
    }

    protected synchronized  
    void drawSuspended( ZBufferedImage zimg ) {
        drawableObjects.resetEnumeration();
        while (drawableObjects.hasMoreElements())
            ((Drawable3D) drawableObjects.nextElement()) .drawProjectedSuspended( zimg );
    }

    /**
     * Sorts points by height (z-value).
     */
    protected synchronized  void sortPoints() {
        sortPoints( points, 0, points .length - 1 );
        this .sorted = true;
    }

    /**
     * Gets the max z point from all drawables, and sorts the array by
     * increasing order of z-value. Result is in the member variable zmaxpoints.
     */
    protected synchronized  void sortZMaxPoints() {
        java.util.Enumeration ds = drawableObjects.elements();
        int i = 0;
        while (ds.hasMoreElements())
            zmaxpoints[i++] = ((Drawable3D) ds.nextElement()).zmaxPoint();
        sortPoints(zmaxpoints, 0, zmaxpoints.length-1);
        this.zmaxsorted = true;
    }

    private static synchronized 
    void sortPoints( PointSortable[] array, int a, int b ) {
        if ( a >= b ) {
            return ;
        }
        // bit shift makes number effectively random
        int pivotIndex = (a + b) >> 1;
        PointSortable p = array[pivotIndex];
        double pz = p .coords[2];
        // swap
        array[pivotIndex] = array[b];
        array[b] = p;
        int l = a;
        int r = b - 1;
        while ( l <= r ) {
            while ( (l <= r) && (array[l] .coords[2] <= pz || Double .isNaN( pz )) ) {
                l++;
            }
            while ( (r >= l) && (array[r] .coords[2] >= pz || Double .isNaN( array[r] .coords[2] )) ) {
                r--;
            }
            if ( l < r ) {
                // swap
                PointSortable temp = array[l];
                array[l] = array[r];
                array[r] = temp;
            }
        }
        // swap
        PointSortable temp = array[l];
        array[l] = array[b];
        array[b] = temp;
        sortPoints( array, a, l - 1 );
        sortPoints( array, l + 1, b );
    }





    // ****************************** FILE I/O ****************************** //

    public Graph3D(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("graph"), parser);
        FileProperties props = parser.parseProperties(tok);
        transformations = new Matrix4D(props.get("transf"), parser);
        transformations_inv = transformations.inverse();
        scale = parser.parseNumber(props.get("scale"));
        drawingMode = (int) parser.parseNumber(props.get("drawmode"));
        surfaceMode = (int) parser.parseNumber(props.get("surfacemode"));
        useLighting = parser.parseBoolean(props.get("light"));
        if (props.contains("alpha"))
            useAlphaBlending = parser.parseBoolean(props.get("alpha"));
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("graph", super.saveFile(generator));
        props.add("transf", transformations.saveFile(generator));
        props.add("scale", generator.generateNumber(scale));
        props.add("drawmode", generator.generateNumber(getDrawingMode()));
        props.add("surfacemode", generator.generateNumber(getSurfaceMode()));
        props.add("light", generator.generateBoolean(useLighting()));
        props.add("alpha", generator.generateBoolean(useAlphaBlending()));
        return generator.generateProperties(props);
    }

    
}


