package demo.graph;

import java.awt .*;
import java.awt.image.*;

import demo.*;
import demo.util.*;
import demo.plot.Plot;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * A GraphCanvas holds a Graph, and draws it.
 * A GraphCanvas is also able to respond to mouse movements or key presses, and perform actions on a Graph
 * accordingly.
 *
 * @author deigen
 */
public  abstract class GraphCanvas extends Canvas implements Dependable {

    protected  int mouseTool = NONE;

    // constants for mouseTool:
    // values 99 and under can be used in this class, 100 - 199 in classes that extend this class, etc.
    /**
    * mouse tool const. Translates
     */
    public static final  int TRANSLATE = 0;

    /**
        * mouse tool const. Scales
     */
    public static final  int SCALE = 1;

    /**
        * mouse tool const. Does nothing. This is the default.
     * When being constructed, things such as a GraphFrame may check to see if the
     * tool of this canvas is NONE, and then can set the tool to its default value.
     */
    public static final  int NONE = -1;

    // mouse UI methods. These will probably be overidden by extending classes
    protected  int xDown = 0, yDown = 0, xPrev = 0, yPrev = 0;

    public static final  int PIXELSINSCALE = 100;

    protected  int bufferImageWidth, bufferImageHeight;

    protected  Image bufferImage = null;

    protected  Graphics bufferGraphics = null;

    protected  ZBufferedImage zbufferImage = null;

    protected MemoryImageSource zbufferImageSource = null;

    public  java.awt .Color bgColor = Color .black;

    protected  int originX = 0, originY = 0;
    
    protected  Graph graph;

    private boolean needToRedraw_ = true;

    protected Set notAllowedTools_ = new Set(); // mouse tools that the user cannot use

    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    
    public  GraphCanvas() {
        this( null );
    }

    public  GraphCanvas( Graph graph ) {
        super();

            this .graph = graph;
            DependencyManager.setDependency(this, graph);
        }


    /**
     * @return the graph of this GraphCanvas
     */
    public  Graph graph() {
        return graph;
    }

    /**
     * Sets the mouse tool of the GraphCanvas. See mouse tool constants for possible tools.
     * @param tool the mouse tool
     */
    public  void setMouseTool( int tool ) {
        this .mouseTool = tool;
    }

    /**
     * Sets whether the given mouse tool is allowed to be used by the user. If it is allowed, then
     * it is possible to get to the tool from modifier keys. If it is not allowed, then the modifier
     * key combination that would produce the tool will give the default tool instead.
     * @param tool the mouse tool
     * @param allowed whether the user is allowed to use the given tool
     */
    public  void allowMouseTool( int tool, boolean allowed ) {
        if (allowed)
            notAllowedTools_.remove(new Integer(tool));
        else
            notAllowedTools_.put(new Integer(tool));
    }

    /**
     * @return the mouse tool
     */
    public  int getMouseTool() {
        return mouseTool;
    }

    
    public 
    boolean mouseDown( int activeMouseTool, int x, int y ) {
        xDown = x;
        yDown = y;
        xPrev = x;
        yPrev = y;
        return true;
    }

    public 
    boolean mouseDrag( int activeMouseTool, int x, int y ) {
        switch (activeMouseTool)
        {
        case TRANSLATE :
            this .originX += x - xPrev;
            this .originY += y - yPrev;
            redraw();
            break;
        
        case SCALE :
            graph .scale( Math .pow( 2, ((double) yPrev - y) / PIXELSINSCALE ) );
            redraw();
            break;
        
        default :
            //System .out .println( "INTERNAL ERROR: Unknown tool." );
            return false;
        }
        xPrev = x;
        yPrev = y;
        return true;
    }

    public 
    boolean mouseUp( int activeMouseTool, int x, int y ) {
        return true;
    }

    public 
    void dependencyUpdateVal( Set updatingObjects ) {
        // update the buffer image
        // only need to update if the graph changed
        if ( !updatingObjects.contains(graph) )
            return;
        if (graph.isVisible())
            redraw();
        else
            needToRedraw_ = true;
    }

    public  void dependencyUpdateDef( Set updatingObjs ) {
        dependencyUpdateVal(updatingObjs);
    }

    public synchronized void paint( Graphics g ) {
        if ( bufferImage == null || bufferImageWidth != this .size() .width || bufferImageHeight != this .size() .height || needToRedraw_) {
            needToRedraw_ = false;
            redraw();
            return ;
        }
        g .drawImage( bufferImage, 0, 0, this );
    }

    public  void update( Graphics g ) {
        paint( g );
    }

    public abstract void redraw();


    // methods that get passed onto the graph
    /**
     * Adds a plot to the Graph of this GraphCanvas.
     */
    public  void addPlot( Plot plot ) {
        graph .addPlot( plot );
    }

    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }


}


