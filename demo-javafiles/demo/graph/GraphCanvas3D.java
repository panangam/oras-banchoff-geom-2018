package demo.graph;

import java.awt.*;
import java.awt.image.*;
import mathbuild.value.*;

import demo.io.*;
import demo.util.*;
import demo.gfx.*;
import demo.depend.*;
import demo.exec.*;
import demo.ui.Graphics2DSettings;
import demo.Demo;
import demo.expr.ste.STEValue;

/**
 * This is (so far) the only subclass of GraphCanvas.
 * It lets you rotate with the mouse.
 *
 * @author deigen
 */
public class GraphCanvas3D extends GraphCanvas implements FileObject {

    // more constants for the mouse tool
    // 100 - 109 for rotation (only 100 - 103 used)
    // 110 and over are for other things (only 110 used)
    /**
     * Constants for the mouse tool.
     */
    public static final
        int ROTATE = 100, ROTATE_X = 101, ROTATE_Y = 102, ROTATE_Z = 103,
        TRANSLATE_CAMERA = 104,
        MOUSEPOINT = 110;

    // variables we should set with the MOUSEPOINT mouse tool
    private STEValue[] mousePointValues = new STEValue[]{null,null,null};

    // constants we should set when the user drags them with the MOUSEPOINT mouse tool
    private java.util.Dictionary hotspots = new java.util.Hashtable(); // stores Hotspot, Hotspot
    private Hotspot currHotspot = null; // the hotspot currently being dragged around
    private java.util.Dictionary latestHotspotPoints = new java.util.Hashtable(); // stores latest ray/mousepoint for each HS
    private Set hotspotsUpdating = new Set(); // set of hotspots updating on seperate thread(s)

    private boolean mousePoints = true;
    
    // proportionality constant: the number of pixels in one radian (for rotation)
    protected static final  double PIXELSINRADS = 100;

    private  boolean suspendWhileDragging = false;
    private  boolean suspended = false; // whether we are currently suspended

    private  GraphGroup myGroup = null;

    protected  int drawingMode = 0;

    // constants for drawingMode:
    /**
     * Constant for drawing mode. Does not use the Z buffer.
     */
    public static final int AWT_GRAPHICS = 0;
    /**
     * Constant for drawing mode. Uses the Z buffer.
     */
    public static final int Z_BUFFER = 1;

    

    public  GraphCanvas3D( Graph3D graph ) {
        super( graph );

    }

    /**
     * Sets the rotation group of this GraphCanvas.
     * All graphs in the GraphGroup are rotated together. 
     * To add a graph to the group, use the add methods in GraphGroup, not this method.
     * @param group the GrahpGroup
     */
    public  void setGroup( GraphGroup group ) {
        myGroup = group;
    }

    /**
     * Sets the GraphGroup of this GraphCanvas to none.
     * To remove a GraphCanvas and Graph from a group, use the remove methods in GraphGroup,
     * not this method.
     */
    public  void removeGroup() {
        myGroup = null;
    }

    /**
     * @return the GraphGroup of this GraphCanvas, or null if it isn't in a group
     */
    public  GraphGroup getGroup() {
        return myGroup;
    }

    /**
     * @return whether this GraphCanvas is in a GraphGroup
     */
    public  boolean hasGroup() {
        return myGroup != null;
    }

    /**
     * Sets whether to suspend graphs while the user is dragging.
     * @param suspendWhileDragging whether to suspend graphs when dragging
     */
    public  void suspend( boolean suspendWhileDragging ) {
        this .suspendWhileDragging = suspendWhileDragging;
    }

    /**
     * @return whether graphs are suspended when the user drags the mouse
     */
    public  boolean getSuspendState() {
        return suspendWhileDragging;
    }
    
    /**
     * If the GraphCanvas is set to suspend graphs while dragging, this method suspends the graphs.
     */
    public  void setSuspendedState() {
        suspended = suspendWhileDragging;
        graph .drawSuspended( suspendWhileDragging );
        if (suspended)
            bufferImage = null;
    }

    /**
     * Unsuspends graphs.
     */
    public  void unsetSuspendedState() {
        suspended = false;
        graph .drawSuspended( false );
    }

    /**
     * Sets whether the mouse can manipulate a hotspot if it hits a hotspot.
     */
    public void setMousePointState(boolean b) {
        mousePoints = b;
    }

    /**
     * @return whether the mouse can manipulate a hotspot if it hits a hotspot.
     */
    public boolean mousePointState() {
        return mousePoints;
    }
    /**
     * Sets the STEValues for the mouse point tool.
     * The user can use the mouse point tool to click on the screen and change the values of these STEs.
     * @param value0 the value for horizontal
     * @param value1 the value for vertical
     * @param value2 the value for out-of-screen
     */
    public  void setMousePointValues( STEValue value0, STEValue value1, STEValue value2 ) {
        mousePointValues[0] = value0;
        mousePointValues[1] = value1;
        mousePointValues[2] = value2;
    }
    
    /**
     * @return an array containing the mouse point STEs in the order: horizontal, vertical, out-of-screen
     */
    public STEValue[] getMousePointValues() {
        return mousePointValues;
    }
    
    /** 
     * Adds a hotspot to the canvas. The hotspot can be clicked and dragged.
     * @param hotspot the hotspot to add.
     */
    public void addHotspot( Hotspot hotspot ) {
        hotspots.put(hotspot, hotspot);
    }
    
    /**
     * Removes a hotspot from the canvas.
     * @param hotspot the hotspot to remove
     */
    public void removeHotspot( Hotspot hotspot ) {
        hotspots.remove(hotspot);
    }
    
    /**
     * @return an Enumeration of all hotspots in the graph
     */
    public java.util.Enumeration hotspots() {
        return hotspots.elements();
    }

    private int findMouseTool(java.awt.Event ev) {
        if ((ev.modifiers & java.awt.Event.CTRL_MASK & java.awt.Event.ALT_MASK) > 0) {
            if (notAllowedTools_.contains(new Integer(ROTATE)))
                return mouseTool;
            return ROTATE;
        }
        if ((ev.modifiers & java.awt.Event.CTRL_MASK) > 0) {
            if (notAllowedTools_.contains(new Integer(ROTATE)))
                return mouseTool;
            return ROTATE;
        }
        if ((ev.modifiers & java.awt.Event.SHIFT_MASK) > 0) {
            if (notAllowedTools_.contains(new Integer(SCALE)))
                return mouseTool;
            return SCALE;
        }
        if ((ev.modifiers & java.awt.Event.ALT_MASK) > 0) {
            if (notAllowedTools_.contains(new Integer(TRANSLATE)))
                return mouseTool;
            return TRANSLATE;
        }
        return mouseTool;
    }


    public 
    boolean mouseDown( java.awt .Event ev, int x, int y ) {
        int activeMouseTool = findMouseTool(ev);
        if ( myGroup == null ) {
            setSuspendedState();
        }
        else {
            myGroup .suspendCanvases();
        }
        if (activeMouseTool == 110) {
            selectHotspot(x,y);
            if (currHotspot == null)
                setMousePointValues(x,y);
        }
        else if (mousePoints) {
            selectHotspot(x,y);
        }
        return super .mouseDown( activeMouseTool, x, y );
    }

    // override mouseDrag
    public 
    boolean mouseDrag( java.awt .Event ev, int x, int y ) {
        int activeMouseTool = findMouseTool(ev);
        if (mousePoints) {
            if (currHotspot == null)
                setMousePointValues(x,y);
            else
                setCurrHotspot(x,y);
            if (currHotspot != null) return true;
        }
        switch (activeMouseTool)
        {
        case ROTATE :
            // rotate using horizontal rotation (about on screen y-axis) and vertical rotation (about on-screen x-axis)
            mouseDragRotate(x,y);
            break;

        case TRANSLATE :
            Matrix4D transMat = new Matrix4D(new double[]{
                1,0,0,(x-xPrev),
                0,1,0,(yPrev-y),
                0,0,1,0,
                0,0,0,1});
            if ( myGroup == null ) {
                graph .drawSuspended( suspendWhileDragging );
                graph .transform(transMat);
                redraw();
            }
            else {
                myGroup .transform( transMat, this.graph );
                myGroup .redraw();
            }
            break;

        case SCALE :
            double s = Math .pow( 2, ((double) yPrev - y) / PIXELSINSCALE );
            Matrix4D scaleMat = new Matrix4D(new double[]{
                s,0,0,0,
                0,s,0,0,
                0,0,s,0,
                0,0,0,1});
            if ( myGroup == null ) {
                graph .drawSuspended( suspendWhileDragging );
                graph .transform(scaleMat);
                redraw();
            }
                else {
                    myGroup .transform( scaleMat, this.graph );
                    myGroup .redraw();
                }
                break;
            
        case ROTATE_X :
            if ( myGroup == null ) {
                graph .drawSuspended( suspendWhileDragging );
                graph .transform( new RotationMatrix4D( (y - yPrev) / PIXELSINRADS, 0, 0 ) );
                redraw();
            }
            else {
                myGroup .transform( new RotationMatrix4D( (y - yPrev) / PIXELSINRADS, 0, 0 ), this.graph );
                myGroup .redraw();
            }
            break;
        
        case ROTATE_Y :
            if ( myGroup == null ) {
                graph .drawSuspended( suspendWhileDragging );
                graph .transform( new RotationMatrix4D( 0, (x - xPrev) / PIXELSINRADS, 0 ) );
                redraw();
            }
            else {
                myGroup .transform( new RotationMatrix4D( 0, (x - xPrev) / PIXELSINRADS, 0 ), this.graph );
                myGroup .redraw();
            }
            break;
        
        case ROTATE_Z :
            //System .out .println( "Rotate around Z axis not yet implemented." );
            break;
            
        case MOUSEPOINT :
            if (currHotspot == null)
                setMousePointValues(x,y);
            else
                setCurrHotspot(x,y);
            break;
            
        default :
            // the mouse tool isn't one of the special ones for this class, so it is one of the ones for a super class
            return super .mouseDrag( activeMouseTool, x, y );
        }
        xPrev = x;
        yPrev = y;
        return true;
    }

    public 
    boolean mouseUp( java.awt .Event ev, int x, int y ) {
        int activeMouseTool = findMouseTool(ev);
        if ( myGroup == null ) {
            unsetSuspendedState();
            redraw();
        }
        else {
            myGroup .unsuspendCanvases();
            myGroup .redraw();
        }
        unselectHotspot();
        return super .mouseUp( activeMouseTool, x, y );
    }

    private void mouseDragRotate(int x, int y) {
        if ( myGroup == null ) {
            graph .drawSuspended( suspendWhileDragging );
            graph .transform( new RotationMatrix4D( (y - yPrev) / PIXELSINRADS,
                                                    (x - xPrev) / PIXELSINRADS,
                                                    0 ) );
            redraw();
        }
        else {
            myGroup .transform( new RotationMatrix4D( (y - yPrev) / PIXELSINRADS,
                                                      (x - xPrev) / PIXELSINRADS, 0 ), this.graph );
            myGroup .redraw();
        }
    }

    // set the drawing mode (so far, awt or zbuffer)
    /**
     * Sets the drawing mode. mode can be AWT_GRAPHICS (for no z buffering) or Z_BUFFER.
     */
    public void setDrawingMode(int mode) {
        this .drawingMode = mode;
        bufferImage = null;
        zbufferImage = null;
        zbufferImageSource = null;
    }
    
    /**
     * @return the drawing mode of this GraphCanvas
     */
    public int getDrawingMode() {
        return drawingMode;
    }
    
    /**
     * Redraws the graph to the Canvas.
     */
    public  synchronized void redraw() {
        if ( bufferImage == null || bufferImageWidth != this .size() .width || bufferImageHeight != this .size() .height ) {
            bufferImageWidth = this .size() .width;
            bufferImageHeight = this .size() .height;
            if (drawingMode == AWT_GRAPHICS) {
                bufferImage = createImage( bufferImageWidth, bufferImageHeight );
            }
            if (drawingMode == Z_BUFFER) {
                zbufferImage = new ZBufferedImage(bufferImageWidth, bufferImageHeight, 
                                            ZBufferedImage.convertColor(this.bgColor));
                zbufferImageSource = new MemoryImageSource( bufferImageWidth, bufferImageHeight,
                                                            zbufferImage.colorModel(), zbufferImage.image(),
                                                            0, zbufferImage.width() );
                zbufferImageSource.setAnimated(true);
                bufferImage = createImage(zbufferImageSource);
            }
        }
        if (drawingMode == AWT_GRAPHICS) {
            // get the graphics in order to reset the origin
            bufferGraphics = bufferImage .getGraphics();
            // turn off antialiasing
            if ( Demo.javaVersionIsGreaterThanOrEqualTo(new int[]{1,2}) ) {
                try {
                    Class.forName("java.awt.Graphics2D"); // make sure Graphics2D exists
                    Graphics2DSettings.setAntialiasing( bufferGraphics, false );
                    Graphics2DSettings.setRenderingToSpeed( bufferGraphics );
                    Graphics2DSettings.setLineThickness( bufferGraphics, (float) 0.05 );
                } catch (Throwable ex) {
                    // not java 2, or some other error -- don't worry about it
                }
            }
            bufferGraphics .setColor( this .bgColor );
            bufferGraphics .fillRect( 0, 0, this .size() .width,
                                        this .size() .height );
            bufferGraphics .translate( this .size() .width / 2 + originX,
                                        this .size() .height / 2 + originY );
            graph .draw( bufferGraphics );
        }
        else if (drawingMode == Z_BUFFER) {
            zbufferImage.reset(ZBufferedImage.convertColor(this.bgColor));
            zbufferImage.translate(this.size().width / 2 + originX, this.size().height / 2 + originY);
            graph.draw(zbufferImage);
            zbufferImageSource.newPixels(0, 0, bufferImageWidth, bufferImageHeight);
        }
        else
            System.out.println("DRAWING MODE NOT A KNOWN MODE");
        paint(this.getGraphics());  // don't do repaint() because it can be slow updating on some systems
    }
    
    
    /**
     * Sets the mouse point value STEs to the given location.
     * Updates all objects dependent on the mouse point values.
     * @param x the x coord of the on-screen location
     * @param y the y coord of the on-screen location
     */
    private void setMousePointValues(int x, int y) {
        double[] point = convertToGraphSpaceCoordinate(x,y,0);
        latestHotspotPoints.put(mousePointValues, point);
        if (!hotspotsUpdating.contains(mousePointValues)) {
            hotspotsUpdating.put(mousePointValues);
            Exec.run(new ExecCallback(){
                public void invoke() {
                    double[] p = (double[]) latestHotspotPoints.get(mousePointValues);
                    // set STEValue values
                    for (int i = 0; i < 3; ++i)
                        if ( mousePointValues[i] != null )
                            mousePointValues[i].setValue(new ValueScalar(p[i]));
                    DependencyManager.updateDependentObjectsValST(mousePointValues);
                }
                public void cleanup() {
                    hotspotsUpdating.remove(mousePointValues);
                }
            });
        }
    }
    
    
    /**
     * Sets the current hotspot to a hotspot close enough to x,y, if any are close enough.
     * @param x the x coord of the on-screen location
     * @param y the y coord of the on-screen location
     */
    private void selectHotspot(int x, int y) {
        // see which hotspots intersect the ray from the pixel, and choose closest one
        Ray ray = rayThroughPixel(x,y);
        Hotspot hotspot = null;
        RayIntersection i = new RayIntersection();
        double t = Double.POSITIVE_INFINITY;
        for (java.util.Enumeration hotspotsEnum = this.hotspots.elements();
             hotspotsEnum.hasMoreElements();) {
            Hotspot hs = (Hotspot) hotspotsEnum.nextElement();
            if (hs.intersect(ray, i)) {
                if (i.t < t) {
                    t = i.t;
                    hotspot = hs;
                }
            }
        }
        this.currHotspot = hotspot;
    }
    
    /**
     * Sets location of the current hotspot to x,y.
     * Updates all objects dependent on the hotspot.
     * @param x the x coord of the on-screen location
     * @param y the y coord of the on-screen location
     */
    private void setCurrHotspot(int x, int y) {
        final Hotspot hs = currHotspot;
        if (hs != null) {
            final double z = convertToFilmSpaceCoordinate((ValueVector) currHotspot.location())[2];
            final ValueVector vec = new ValueVector(convertToGraphSpaceCoordinate(x,y,z));
            final Ray ray = rayThroughPixel(x,y);
            latestHotspotPoints.put(hs, new Object[]{vec, ray});
            if (!hotspotsUpdating.contains(hs)) {
                hotspotsUpdating.put(hs);
                Exec.run(new ExecCallback(){
                    public void invoke() {
                        hotspotsUpdating.remove(hs);
                        Object[] hsdata = (Object[]) latestHotspotPoints.get(hs);
                        hs.setLocation( (ValueVector) hsdata[0], (Ray) hsdata[1] );
                        DependencyManager.updateDependentObjectsValST(hs);
                    }
                });                
            }
        }
    }
    
    /**
     * Unselects the current hotspot (sets currHotspot to null).
     */
    private void unselectHotspot() {
        currHotspot = null;
    }

    /**
     * @return intersection tolerance for hotspots. (related to scale of worldspace transform)
     */
    private double intersectionTolerance() {
        final double[] e1 = new double[]{1,0,0,0};
        final double[] e2 = new double[]{0,1,0,0};
        final double[] e3 = new double[]{0,0,1,0};
        double s = ((Graph3D) graph).getScale();
        Matrix4D m = ((Graph3D) graph).getTransformations();
        double t1 = M.length(m.transform(e1));
        double t2 = M.length(m.transform(e2));
        double t3 = M.length(m.transform(e3));
        double tavg = (t1 + t2 + t3)/3;
        return 10.0 / (s * tavg);
    }
    
    /**
     * @param x the x coord of a point in the on-screen pixel space
     * @param y the y coord of a point in the on-screen pixel space
     * @param z "pixels" coming out from the screen in the on-screen pixel space
     * @return the point in the abstract graph space the given pixel corresponds to,
     *         given as a three dimensional array of double containing x,y,z
     */
    private double[] convertToGraphSpaceCoordinate(double x, double y, double z) {
        // convert on-screen coordinate to abstract 3-space coordinate
        // first untranslate and flip y-coordinate
        x = x - this.size().width / 2 - originX;
        y = -( y - this.size().height / 2 - originY );
        // now, unapply transformations
        demo.gfx.Point point = new demo.gfx.Point( new double[]{x,y,z}, 0 );
        ((Graph3D) graph).getInverseTransformations().transform(point);
        // unscale
        point.coords[0] /= ((Graph3D) graph).getScale();
        point.coords[1] /= ((Graph3D) graph).getScale();
        point.coords[2] /= ((Graph3D) graph).getScale();
        // point is the point in the abstract 3-space
        return point.coords;
    }

    /**
     * @param p the point in the graph space (world-space)
     * @return the point in the canvas (film) space the given pixel corresponds to,
     *         given as a three dimensional array of double containing x,y,z
     */
    private double[] convertToFilmSpaceCoordinate(ValueVector p) {
        double x,y,z;
        double[] xs = p.doubleVals();
        x = xs[0]; y = xs[1]; z = xs.length == 2 ? 0 : xs[2];
        // scale
        x *= ((Graph3D) graph).getScale();
        y *= ((Graph3D) graph).getScale();
        z *= ((Graph3D) graph).getScale();
        // now, apply transformations
        demo.gfx.Point point = new demo.gfx.Point( new double[]{x,y,z}, 0 );
        ((Graph3D) graph).getTransformations().transform(point);
        x = point.coords[0]; y = point.coords[1]; z = point.coords[2];
        // translate and flip y-coordinate
        x = x + this.size().width / 2 + originX;
        y = -y + this.size().height / 2 + originY;
        // point is the point in the abstract 3-space
        return new double[]{x,y,z};
    }

    /**
     * Makes a ray (going from z=0 in screen space) out from the given x,y pixel.
     * @param x,y the x,y values at the pixel
     * @return a ray through the pixel
     */
    private Ray rayThroughPixel(double x, double y) {
        double[] p = convertToGraphSpaceCoordinate(x, y, 0);
        double[] v = ((Graph3D) graph).getInverseTransformations()
                                      .transform(new double[]{0,0,-10,0});
        v = M.normalize(v);
        return new Ray(p, v, intersectionTolerance());
    }
    
    


    // ****************************** FILE I/O ****************************** //
    private String graph__;
    private String group__;
    private String[] hotspots__;
    private String[] mousePointValues__;
    
    public GraphCanvas3D(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        graph__ = parser.parseObject(props.get("graph"));
        mouseTool = (int) parser.parseNumber(props.get("tool"));
        if (props.contains("points"))
            mousePoints = parser.parseBoolean(props.get("points"));
        drawingMode = (int) parser.parseNumber(props.get("drawmode"));
        if (props.contains("mousevars")) {
            mousePointValues__ = parser.parseWordList(props.get("mousevars"));
            if (mousePointValues__.length != 3) parser.error("must have 3 mouse point values");
        }
        else {
            mousePointValues__ = new String[]{"%","%","%"};
        }
        hotspots__ = parser.parseObjectList(props.get("hotspots"));
        group__ = parser.parseObject(props.get("group"));
    }

    public void loadFileBind(FileParser parser) {
        this.graph = (Graph) parser.getObject(graph__);
        DependencyManager.setDependency(this, graph);
        for (int i = 0; i < hotspots__.length; ++i) {
            Hotspot hs = (Hotspot) parser.getObject(hotspots__[i]);
            this.hotspots.put(hs,hs);
        }
        setGroup((GraphGroup) parser.getObject(group__));
        graph__ = group__ = null; hotspots__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        for (int i = 0; i < mousePointValues__.length; ++i)
            if (mousePointValues__[i].equals("%"))
                mousePointValues[i] = null;
            else
                mousePointValues[i] = (STEValue) parser.currEnvLookup(mousePointValues__[i]);
        parser.loadExprs(graph);
        parser.loadExprs(myGroup);
        parser.loadExprs(hotspots.elements());
    }

    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("graph", generator.generateObject(graph));
        props.add("tool", generator.generateNumber(mouseTool));
        props.add("drawmode", generator.generateNumber(drawingMode));
        if (mousePointValues[0] != null || mousePointValues[1] != null || mousePointValues[2] != null) {
            TokenString str = new TokenString();
            for (int i = 0; i < mousePointValues.length; ++i)
                str.add(generator.generateWord(mousePointValues[i] == null ? "%" : mousePointValues[i].name()));
            props.add("mousevars", generator.generateList(str));
        }
        props.add("hotspots", generator.generateObjectList(hotspots.elements()));
        props.add("group", generator.generateObject(myGroup));
        props.add("points", generator.generateBoolean(mousePoints));
        return generator.generateProperties(props);
    }

    
}


