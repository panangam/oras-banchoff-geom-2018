package demo.graph;

import java.awt .MenuItem;
import java.awt .CheckboxMenuItem;

import demo.ui.*;

import demo.Demo;
import demo.io.*;
import demo.depend.*;
import demo.gfx.Matrix4D;
import demo.gfx.RotationMatrix4D;
import demo.plot.ui.EditPlotWindow;
import demo.plot.ui.AddPlotMenuCreator;
import demo.plot.Plot;

/**
 * GraphFrame for 2D graphs. There is no rotation.
 *
 * @author deigen
 */
public class GraphFrame2D extends GraphFrameUserPlottable implements Dependable, FileObject {

    public 
    GraphFrame2D( Demo demo, GraphCanvas3D canvas, Graph3D graph,
                    String title ) {
        super( title, demo, demo.environment(), 2 );

            this .demo = demo;
            this .canvas = canvas;
            this .graph = graph;
            this .title = title;
            init();
    }

    private void init() {
            // canvas should not be able to rotate
            canvas.allowMouseTool(GraphCanvas3D.ROTATE, false);
            canvas.allowMouseTool(GraphCanvas3D.ROTATE_X, false);
            canvas.allowMouseTool(GraphCanvas3D.ROTATE_Y, false);
            canvas.allowMouseTool(GraphCanvas3D.ROTATE_Z, false);
            // create menus
            mouseToolGroup = new MenuItemGroup();
            mouseToolGroup .addItem( mouseTranslate );
            mouseToolGroup .addItem( mouseScale );
            mouseToolGroup .addItem( mousePoint );
            switch ( canvas.getMouseTool() ) {
            case GraphCanvas.TRANSLATE:
                mouseToolGroup .selectItem( mouseTranslate );
                break;
            case GraphCanvas.SCALE:
                mouseToolGroup .selectItem( mouseScale );
                break;
            case GraphCanvas3D.MOUSEPOINT:
                mouseToolGroup .selectItem( mousePoint );
                break;
            case GraphCanvas.NONE:  // no mouse tool set yet, so make it translate (default for 2D graphs)
                mouseToolGroup .selectItem( mouseTranslate );
                canvas.setMouseTool( GraphCanvas.TRANSLATE );
                break;
            default:
                break;
            }
            useSortingMenuItem.setState(graph.getDrawingMode() == Graph3D.SORT ||
                                        canvas.getDrawingMode() == GraphCanvas3D.Z_BUFFER);
            AWTMenu toolMenu = new AWTMenu( "Tools" );
            AWTMenu viewMenu = new AWTMenu( "View" );
            AWTMenu windowMenu = new AWTMenu( "Window" );
            toolMenu .add( mouseTranslate );
            toolMenu .add( mouseScale );
            toolMenu .add( mousePoint );
            plotMenu .insert( new AddPlotMenuCreator(demo, this.environment, 2, this).makeMenu(), 0);
            plotMenu .insert( new MenuItem( "-" ), 1 );
            plotMenu .insert( useSortingMenuItem, 2 );
            plotMenu .insert( editPlotVisibleMenuItem, 3 );
            plotMenu .insert( new MenuItem( "-" ), 4 );
            viewMenu .add( zoomIn );
            viewMenu .add( zoomOut );
            windowMenu .add( winClose );
            windowMenu .add( new MenuItem( "-" ) );
            windowMenu .add( changeTitle );
            windowMenu .add( setMousePointValues );
            windowMenu .add( setHotspots );
            AWTMenuBar bar = new AWTMenuBar();
            bar .add( toolMenu );
            bar .add( plotMenu );
            bar .add( viewMenu );
            bar .add( windowMenu );
            this .setMenuBar( bar );
            // put the canvas in the frame
            this .setLayout( new java.awt .BorderLayout() );
            this .add( canvas, "Center" );
            this .setSize( 300, 300 );
            this .setLocation(50,50);
        }

    /**
     * Sets the transformations and scale of the Graph to the default for this GraphFrame.
     * Does not reset the translation.
     */
    public  void setDefaultView() {
        graph .setTransformation( DEFAULT_VIEW );
        graph .setScale( DEFAULT_SCALE );
    }

    private  Demo demo;

    private  Graph3D graph;

    public  GraphCanvas canvas() {
        return canvas;
    }

    private  GraphCanvas3D canvas;

    private  String title;

    private static final  int DEFAULT_ORIGINX = 0, DEFAULT_ORIGINY = 0;

    private static final  double DEFAULT_SCALE = 100.01271474132782372267;

    protected static final 
    RotationMatrix4D DEFAULT_VIEW = new RotationMatrix4D( 0, 0, 0 );

    protected static final  double ZOOM_FACTOR = 1.5;

    private 
    MenuItem zoomIn = new MenuItem( "Zoom In" ),
        zoomOut = new MenuItem( "Zoom Out" );
    
    private  MenuItemGroup mouseToolGroup;

    private 
    CheckboxMenuItem mouseTranslate = new CheckboxMenuItem( "Translate" ),
                        mouseScale = new CheckboxMenuItem( "Zoom" ),
                        mousePoint = new CheckboxMenuItem( "Point" );

    private CheckboxMenuItem useSortingMenuItem = new CheckboxMenuItem("Prioritize Plots");

    private 
    MenuItem changeTitle = new MenuItem( "Window Title..." ),
                setMousePointValues = new MenuItem( "Set Point Variables..." ),
                setHotspots = new MenuItem( "Edit Hotspots..." ),
                winClose = new MenuItem( "Close Window" );

    private static final  Object CHANGE_TITLE_ARG = new Object();

    public  boolean handleEvent( java.awt .Event event ) {
        if ( event .id == java.awt .Event .WINDOW_DESTROY ) {
            setVisible(false);
            this .dispose();
            return true;
        }
        else {
            return super .handleEvent( event );
        }
    }

    public 
    boolean action( java.awt .Event event, Object object ) {
        if ( event .target instanceof PlotMenuItem ) {
            openEditPlotWindow( ((PlotMenuItem) event .target) .getPlot() );
            return true;
        }
        if ( event .target == zoomIn ) {
            this .graph .scale( ZOOM_FACTOR );
            canvas .redraw();
        }
        else if ( event .target == zoomOut ) {
                this .graph .scale( 1 / ZOOM_FACTOR );
                canvas .redraw();
        }
        else if ( event .target == this .mouseTranslate ) {
                this .canvas .setMouseTool( GraphCanvas3D .TRANSLATE );
                mouseToolGroup .selectItem( mouseTranslate );
        }
        else if ( event .target == this .mouseScale ) {
                this .canvas .setMouseTool( GraphCanvas3D .SCALE );
                mouseToolGroup .selectItem( mouseScale );
        }
        else if ( event .target == this.mousePoint ) {
                this .canvas .setMouseTool( GraphCanvas3D .MOUSEPOINT );
                mouseToolGroup .selectItem( mousePoint );
        }
        else if ( event .target == winClose ) {
                setVisible(false);
                dispose();
        }
        else if ( event .target == changeTitle ) {
                new AskMessageDialog( "Enter the new title for the window:",
                                                this .getTitle(),
                                                this, this,
                                                CHANGE_TITLE_ARG );
        }
        else if ( event .target == setMousePointValues ) {
                new MousePointValuesWindow( this, this.canvas, this.demo, false );
        }
        else if ( event .target == setHotspots ) {
                java.awt.Window w = new EditHotspotsWindow( this, this.canvas, this.graph, this.demo, this.environment, 2 );
                addOpenDialog(w);
        }
        else if ( (event .target instanceof AskMessageDialog) && (event .arg == CHANGE_TITLE_ARG) ) {
                this .setTitle( ((AskMessageDialog) event .target) .getResponce() );
        }
        else if ( (event .target == useSortingMenuItem) ) {
            if (useSortingMenuItem.getState())
                graph.setDrawingMode(Graph3D.SORT);
            else
                graph.setDrawingMode(Graph3D.NONE);
            canvas.redraw();
        }
        else {
            return super .action( event, object );
        }
        return true;
    }

    public  boolean keyDown( java.awt .Event e, int key ) {
        if ( e .target instanceof java.awt .TextField ) {
            return super .keyDown( e, key );
        }
        // allow text fields to get their input!
        return true;
    }

    public  java.util.Enumeration getEditablePlots() {
        demo.util.Set plots = new demo.util.Set();
        for (java.util.Enumeration graphPlots = graph.plots(); graphPlots.hasMoreElements();) {
            Plot plot = (Plot) graphPlots.nextElement();
            plots.add(plot);
        }
        return plots.elements();
    }

    // override dispose
    public  void dispose() {
        demo .disposeGraphFrameAndContents( this, canvas, graph );
        super .dispose();
    }

    public void setVisible(boolean vis) {
        this.graph.setVisible(vis);
        super.setVisible(vis);
//        if (vis)
//            canvas.ensureRedrawn();
        /*
        if (vis) {
            canvas.setDrawMessage("Redrawing...");
            canvas.setDrawMessageColor(java.awt.Color.white);
            canvas.setDrawMessage(true);
            canvas.setDrawGraph(false);
            super.setVisible(true);
            canvas.setDrawMessage(false);
            canvas.setDrawGraph(true);
            canvas.redraw();
        }
        else {
            super.setVisible(false);
        }
         */
    }


    public void plotCreated( EditPlotWindow win, Plot plot ) {
        if (plot != null)
            if (!graph.containsPlot(plot))
                graph.addPlot(plot);
        super.plotCreated(win, plot); 
    }



    // ****************************** FILE I/O ****************************** //
    String canvas__, graph__;

    public GraphFrame2D(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("frame"), parser, 2);
        parser.pushDimension(2);
        demo = parser.demo();
        FileProperties props = parser.parseProperties(tok);
        canvas__ = parser.parseObject(props.get("canvas"));
        graph__ = parser.parseObject(props.get("graph"));
        parser.popDimension();
    }

    public void loadFileBind(FileParser parser) {
        canvas = (GraphCanvas3D) parser.getObject(canvas__);
        graph = (Graph3D) parser.getObject(graph__);
        title = getTitle();
        canvas__ = graph__ = null;
        super.loadFileBind(parser);
        DependencyManager.setDependency(this, canvas);
    }

    public void loadFileExprs(FileParser parser) {
        parser.loadExprs(canvas);
        super.loadFileExprs(parser);
    }

    public void loadFileFinish(FileParser parser) {
        init();
        super.loadFileFinish(parser);
    }
    
    public Token saveFile(FileGenerator generator) {
        Token superTok = super.saveFile(generator);
        FileProperties props = new FileProperties();
        props.add("canvas", generator.generateObject(canvas));
        props.add("graph", generator.generateObjectID(graph));
        props.add("frame", superTok);
        return generator.generateProperties(props);
    }
    
    
}


