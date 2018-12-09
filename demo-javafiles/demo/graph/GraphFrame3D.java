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
 * GraphFrame for 3D graphs.
 *
 * @author deigen
 */
public class GraphFrame3D extends GraphFrameUserPlottable implements Dependable, FileObject {

    public 
    GraphFrame3D( Demo demo, GraphCanvas3D canvas, Graph3D graph,
                    String title ) {
        super( title, demo, demo.environment(), 3 );

            // assign fields
            this .demo = demo;
            this .graph = graph;
            this .canvas = canvas;
            this .title = title;
            init();
        }

    private void init() {
        // assemble user interface
        // menuitems and menus
        surfacesGroup = new MenuItemGroup();
        surfacesGroup .addItem( surfacesOpen );
        surfacesGroup .addItem( surfacesFilledFramed );
        surfacesGroup .addItem( surfacesFilledUnframed );
        // select surfaces menuitem
        switch (graph .getSurfaceMode()) {
            case Graph3D .OPEN :
                surfacesGroup .selectItem( surfacesOpen );
                break;

            case Graph3D .FILLED_NOFRAME :
                surfacesGroup .selectItem( surfacesFilledUnframed );
                break;

            case Graph3D .FILLED_FRAME :
                surfacesGroup .selectItem( surfacesFilledFramed );
                break;

            default:
                break;
        }
        mouseToolGroup = new MenuItemGroup();
        mouseToolGroup .addItem( mouseRotate );
        mouseToolGroup .addItem( mouseTranslate );
        mouseToolGroup .addItem( mouseScale );
        mouseToolGroup .addItem( mousePoint );
        // set mouse tool menuitem
        switch (canvas .getMouseTool()) {
            case GraphCanvas3D .ROTATE :
                mouseToolGroup .selectItem( mouseRotate );
                break;

            case GraphCanvas3D .SCALE :
                mouseToolGroup .selectItem( mouseScale );
                break;

            case GraphCanvas3D .TRANSLATE :
                mouseToolGroup .selectItem( mouseTranslate );
                break;

            case GraphCanvas3D .MOUSEPOINT :
                mouseToolGroup .selectItem( mousePoint );
                break;

            case GraphCanvas3D .NONE:  // no tool set yet, so make it rotate (the default for 3D graphs)
                mouseToolGroup .selectItem( mouseRotate );
                canvas.setMouseTool( GraphCanvas3D.ROTATE );
                break;

            default:
                break;
        }
        mousePointChkbx .setState(canvas.mousePointState());
        drawingStyleGroup = new MenuItemGroup();
        drawingStyleGroup .addItem( drawUnsorted );
        drawingStyleGroup .addItem( drawSorted );
        drawingStyleGroup .addItem( drawZBuffered );
        switch (graph .getDrawingMode()) {
            case Graph3D .NONE :
                drawingStyleGroup .selectItem( drawUnsorted );
                break;

            case Graph3D .SORT :
                drawingStyleGroup .selectItem( drawSorted );
                break;

            case Graph3D .SPLIT :
                System.out.println("SPLIT DRAWING MODE NOT YET IMPLEMENTED IN GraphFrame3D");
                break;

            default:
                break;
        }
        switch (canvas .getDrawingMode()) {
            case GraphCanvas3D .Z_BUFFER :
                drawingStyleGroup .selectItem(drawZBuffered);
                break;
            default:
                break;
        }
        AWTMenu toolMenu = new AWTMenu( "Tools" );
        AWTMenu styleMenu = new AWTMenu( "Styles" );
        AWTMenu viewMenu = new AWTMenu( "View" );
        windowMenu = new AWTMenu( "Window" );
        toolMenu .add( mouseRotate );
        toolMenu .add( mouseTranslate );
        toolMenu .add( mouseScale );
        toolMenu .add( mousePoint);
        toolMenu .add( "-" );
        toolMenu .add( mousePointChkbx );
        plotMenu .insert( new AddPlotMenuCreator(demo, this.environment, 3, this).makeMenu(), 0 );
        plotMenu .insert( new MenuItem( "-" ), 1 );
        plotMenu .insert( editPlotVisibleMenuItem, 2 );
        plotMenu .insert( new MenuItem( "-" ), 3 );
        styleMenu .add( surfacesOpen );
        styleMenu .add( surfacesFilledFramed );
        styleMenu .add( surfacesFilledUnframed );
        styleMenu .add( new MenuItem( "-" ) );
        styleMenu .add( drawUnsorted );
        styleMenu .add( drawSorted );
        styleMenu .add( drawZBuffered );
        styleMenu .add( new MenuItem( "-" ) );
        styleMenu .add( drawLighting );
        styleMenu .add( drawAlphaBlend );
        drawLighting .setState( this.graph.useLighting() );
        drawAlphaBlend .setState( this.graph.useAlphaBlending() );
        styleMenu .add( new MenuItem( "-" ) );
        styleMenu .add( drawSuspended );
        drawSuspended .setState( true );
        viewMenu .add( zoomIn );
        viewMenu .add( zoomOut );
        viewMenu .add( new MenuItem( "-" ) );
        viewMenu .add( viewDefault );
        viewMenu .add( viewX );
        viewMenu .add( viewX2 );
        viewMenu .add( viewY );
        viewMenu .add( viewY2 );
        viewMenu .add( viewZ );
        viewMenu .add( viewZ2 );
        windowMenu .add( winClose );
        windowMenu .add( new MenuItem( "-" ) );
        windowMenu .add( changeTitle );
        windowMenu .add( setMousePointValues );
        windowMenu .add( setHotspots );
        windowMenu .add( makeGroup );
        // menu bar
        AWTMenuBar menuBar = new AWTMenuBar();
        menuBar .add( toolMenu );
        menuBar .add( plotMenu );
        menuBar .add( styleMenu );
        menuBar .add( viewMenu );
        menuBar .add( windowMenu );
        this .setMenuBar( menuBar );
        // graph canvas
        this .setLayout( new java.awt .BorderLayout() );
        this .add( canvas, "Center" );
        // size window
        this .setSize( 300, 300 );
        this .setLocation(50,50);        
    }
    
    /**
     * Sets the transformations and scale of the Graph to the default view for this GraphFrame.
     * Translation is not set.
     */
    public  void setDefaultView() {
        graph .setTransformation( DEFAULT_VIEW );
        graph .setScale( DEFAULT_SCALE );
    }

    private  Graph3D graph;

    public  GraphCanvas canvas() {
        return canvas;
    }

    private  GraphCanvas3D canvas;

    protected static final 
    RotationMatrix4D VIEW_X = new RotationMatrix4D( - Math .PI / 2, 0,
                                                    - Math .PI / 2 ),
                    VIEW_X2 = new RotationMatrix4D( - Math .PI / 2, 0,
                                                    Math .PI / 2 ),
                    VIEW_Y = new RotationMatrix4D( Math .PI / 2, Math .PI,
                                                   0 ),
                    VIEW_Y2 = new RotationMatrix4D( - Math .PI / 2, 0, 0 ),
                    VIEW_Z = new RotationMatrix4D( 0, 0, 0 ),
                    VIEW_Z2 = new RotationMatrix4D( 0, Math .PI, 0 ),
                    DEFAULT_VIEW = new RotationMatrix4D( - 1, 0, - 1.8 );

    protected static final 
    double DEFAULT_SCALE = 100.01271474132782372267, ZOOM_FACTOR = 1.5;

    private 
    MenuItem zoomIn = new MenuItem( "Zoom In" ),
               zoomOut = new MenuItem( "Zoom Out" ),
               viewDefault = new MenuItem( "Default" ),
               viewX = new MenuItem( "Up X Axis" ),
               viewX2 = new MenuItem( "Down X Axis" ),
               viewY = new MenuItem( "Up Y Axis" ),
               viewY2 = new MenuItem( "Down Y Axis" ),
               viewZ = new MenuItem( "Up Z Axis" ),
               viewZ2 = new MenuItem( "Down Z Axis" );

    private  MenuItemGroup surfacesGroup;

    private 
    CheckboxMenuItem surfacesOpen = new CheckboxMenuItem( "Open Surfaces" ),
                        surfacesFilledFramed = new CheckboxMenuItem( "Filled Surfaces" ),
                        surfacesFilledUnframed = new CheckboxMenuItem( "Filled Surfaces, No Lines" );

    private  MenuItemGroup mouseToolGroup;

    private 
    CheckboxMenuItem mouseRotate = new CheckboxMenuItem( "Rotate" ),
                        mouseTranslate = new CheckboxMenuItem( "Translate" ),
                        mouseScale = new CheckboxMenuItem( "Zoom" ),
                        mousePoint = new CheckboxMenuItem( "Point" ),
                        mousePointChkbx = new CheckboxMenuItem( "Selects" );

    private  MenuItemGroup drawingStyleGroup;

    private 
    java.awt .CheckboxMenuItem drawUnsorted = new CheckboxMenuItem( "No Sorting or Buffering" ),
                                    drawSorted = new CheckboxMenuItem( "Sort" ),
                                    drawZBuffered = new CheckboxMenuItem( "Z Buffer" ),
                                    drawSuspended = new CheckboxMenuItem( "Suspend While Dragging" ),
                                    drawLighting = new CheckboxMenuItem( "Use Lighting"),
                                    drawAlphaBlend = new CheckboxMenuItem( "Use Transparency" );

    private  AWTMenu windowMenu;

    private 
    MenuItem changeTitle = new MenuItem( "Window Title..." ),
               setMousePointValues = new MenuItem( "Set Point Variables..." ),
               setHotspots = new MenuItem( "Edit Hotspots..." ),
               winClose = new MenuItem( "Close Window" ),
               makeGroup = new MenuItem( "Link Rotation..." ),
               makeFree = new MenuItem( "Unlink Rotation" );

    private static final  Object CHANGE_TITLE_ARG = new Object();

    private  String title;

    private  Demo demo;

    //private  MenuItemGroup lines, surfaces, sort, control;
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
            openEditPlotWindow( ((PlotMenuItem) event.target).getPlot() );
            return true;
        }
        if ( event .target == mouseRotate || event .target == mouseTranslate || event .target == mouseScale || event .target == mousePoint || event .target == mousePointChkbx) {
            setMouseMode( event .target );
        }
        else if ( event .target == zoomIn ) {
                this .graph .scale( ZOOM_FACTOR );
                canvas .redraw();
        }
        else if ( event .target == zoomOut ) {
                this .graph .scale( 1 / ZOOM_FACTOR );
                canvas .redraw();
        }
        else if ( event .target == viewX || event .target == viewX2 || event .target == viewY || event .target == viewY2 || event .target == viewZ || event .target == viewZ2 || event .target == viewDefault ) {
                setView( event .target );
        }
        else if ( event .target == surfacesOpen || event .target == surfacesFilledFramed || event .target == surfacesFilledUnframed ) {
                setSurfaceMode( event .target );
        }
        else if ( event .target == drawUnsorted || event .target == drawSorted || event .target == drawZBuffered ) {
                setDrawingMode( event .target );
        }
        else if ( event .target == drawSuspended ) {
                this .canvas .suspend( drawSuspended .getState() );
        }
        else if ( event .target == drawLighting ) {
                this.graph.useLighting( drawLighting.getState() );
                this.canvas.redraw();
        }
        else if ( event .target == drawAlphaBlend ) {
                this.graph.useAlphaBlending( drawAlphaBlend.getState() );
                this.canvas.redraw();
        }
        else if ( event .target == winClose ) {
                setVisible(false);
                dispose();
        }
        else if ( event .target == makeGroup ) {
                new SelectGroupWindow( this,
                                                this .canvas .getGroup(),
                                                this .canvas .graph(),
                                                this .canvas,
                                                demo );
        }
        else if ( event .target == makeFree ) {
                demo .removeFromGroup( this .canvas .getGroup(),
                                            this .canvas .graph(),
                                            this .canvas );
                setGraphGroupState( false );
        }
        else if ( event .target == setMousePointValues ) {
                new MousePointValuesWindow( this, this.canvas, this.demo, true );
        }
        else if ( event .target == setHotspots ) {
            java.awt.Window w = new EditHotspotsWindow( this, this.canvas, this.graph, this.demo,
                                                        this.environment, 3 );
            addOpenDialog(w);
        }
        else if ( event .target == changeTitle ) {
                new AskMessageDialog( "Enter the new title for the window:",
                                                this .getTitle(),
                                                this,
                                                this,
                                                CHANGE_TITLE_ARG );
        }
        else if ( (event .target instanceof AskMessageDialog) && (event .arg == CHANGE_TITLE_ARG) ) {
                this .setTitle( ((AskMessageDialog) event .target) .getResponce() );
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
        if ( key == ' ' ) {
            setView( viewDefault );
        }
        else {
            if ( key == 'x' ) {
                setView( viewX );
            }
            else {
                if ( key == 'X' ) {
                    setView( viewX2 );
                }
                else {
                    if ( key == 'y' ) {
                        setView( viewY );
                    }
                    else {
                        if ( key == 'Y' ) {
                            setView( viewY2 );
                        }
                        else {
                            if ( key == 'z' ) {
                                setView( viewZ );
                            }
                            else {
                                if ( key == 'Z' ) {
                                    setView( viewZ2 );
                                }
                                else {
                                    return super .keyDown( e, key );
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }


    /**
     * Sets whether the Graph and GraphCanvas of this GraphFrame are in a GraphGroup.
     * Used to set the appropriate menu items.
     */
    public  void setGraphGroupState( boolean isInGroup ) {
        // update menu
        MenuItem toRemove = isInGroup ? makeGroup : makeFree;
        MenuItem toAdd = isInGroup ? makeFree : makeGroup;
        int index;
        for ( index = 0; index < windowMenu.getItemCount(); ++index ) {
            if ( windowMenu.getItem(index) == toRemove ) break;
        }
        if ( index < windowMenu.getItemCount() ) {
            windowMenu.insert( toAdd, index );
            windowMenu.remove( toRemove );
        }
    }

    private  void setView( Object target ) {
        if ( target == viewDefault ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( DEFAULT_VIEW ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( DEFAULT_VIEW );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewX ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_X ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_X );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewX2 ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_X2 ) , this.graph);
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_X2 );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewY ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_Y ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_Y );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewY2 ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_Y2 ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_Y2 );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewZ ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_Z ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_Z );
                canvas .redraw();
            }
            return ;
        }
        if ( target == viewZ2 ) {
            if ( canvas .hasGroup() ) {
                canvas .getGroup() .transform( (Matrix4D) graph .getTransformations() .inverse() .multiplyOnLeftBy( VIEW_Z2 ), this.graph );
                canvas .getGroup() .redraw();
            }
            else {
                graph .setTransformation( VIEW_Z2 );
                canvas .redraw();
            }
            return ;
        }
    }

    private  void setSurfaceMode( Object target ) {
        surfacesGroup .selectItem( (CheckboxMenuItem) target );
        if ( target == this .surfacesOpen ) {
            this .graph .setSurfaceMode( Graph3D .OPEN );
        }
        else {
            if ( target == this .surfacesFilledFramed ) {
                this .graph .setSurfaceMode( Graph3D .FILLED_FRAME );
            }
            else {
                if ( target == this .surfacesFilledUnframed ) {
                    this .graph .setSurfaceMode( Graph3D .FILLED_NOFRAME );
                }
            }
        }
        this .canvas .redraw();
    }

    private  void setMouseMode( Object target ) {
        if ( target == this .mousePointChkbx ) {
            this .canvas .setMousePointState(((CheckboxMenuItem) target).getState());
            return;
        }
        mouseToolGroup .selectItem( (CheckboxMenuItem) target );
        if ( target == this .mouseRotate ) {
            this .canvas .setMouseTool( GraphCanvas3D .ROTATE );
        }
        else {
            if ( target == this .mouseTranslate ) {
                this .canvas .setMouseTool( GraphCanvas3D .TRANSLATE );
            }
            else {
                if ( target == this .mouseScale ) {
                    this .canvas .setMouseTool( GraphCanvas3D .SCALE );
                }
                else {
                    if ( target == this .mousePoint ) {
                        this .canvas .setMouseTool( GraphCanvas3D .MOUSEPOINT );
                    }
                }
            }
        }
    }

    private  void setDrawingMode( Object target ) {
        drawingStyleGroup .selectItem( (CheckboxMenuItem) target );
        if ( target == this .drawUnsorted ) {
            this .graph .setDrawingMode( Graph3D .NONE );
            this .canvas .setDrawingMode( GraphCanvas3D.AWT_GRAPHICS);
        }
        else {
            if ( target == this .drawSorted ) {
                this .graph .setDrawingMode( Graph3D .SORT );
                this .canvas .setDrawingMode( GraphCanvas3D.AWT_GRAPHICS);
           }
            else {
                if ( target == this .drawZBuffered ) {
                    // check to make sure they're running Java 1.1 or higher
                    if (System.getProperty("java.version").substring(0,3).equals("1.0"))
                        demo.showError("Z Buffering requires Java 1.1 or later");
                    else
                        this .canvas .setDrawingMode( GraphCanvas3D.Z_BUFFER);
                }
            }
        }
        this .canvas .redraw();
    }

    public  java.util.Enumeration getEditablePlots() {
        demo.util.Set plots = new demo.util.Set();
        for (java.util.Enumeration graphPlots = graph.plots(); graphPlots.hasMoreElements();) {
            Plot plot = (Plot) graphPlots.nextElement();
            plots.add(plot);
        }
        return plots.elements();
    }

    public  void setVisible(boolean showing) {
        this.graph.setVisible(showing);
        if (!showing) {
            super.setVisible(false);
            return;
        }
        // make sure all the default menuitems are set correctly
        // mouse tool
        switch (canvas .getMouseTool())
        {
        case GraphCanvas3D .ROTATE :
            mouseToolGroup .selectItem( mouseRotate );
            break;
        
        case GraphCanvas3D .TRANSLATE :
            mouseToolGroup .selectItem( mouseTranslate );
            break;
        
        case GraphCanvas3D .SCALE :
            mouseToolGroup .selectItem( mouseScale );
            break;
        
        default :
            break;
        }
        // drawing mode
        switch (graph .getDrawingMode())
        {
        case Graph3D .NONE :
            drawingStyleGroup .selectItem( drawUnsorted );
            break;
        
        case Graph3D .SORT :
            drawingStyleGroup .selectItem( drawSorted );
            break;
        
        case Graph3D .SPLIT :
            System.out.println("SPLIT DRAWING MODE NOT YET SUPPORTED IN GraphFrame3D");
            break;
        }
        switch (canvas .getDrawingMode()) {
        case GraphCanvas3D .Z_BUFFER :
            drawingStyleGroup .selectItem(drawZBuffered);
            break;
        default:
            break;
        }
        // surface mode
        switch (graph .getSurfaceMode())
        {
        case Graph3D .OPEN :
            surfacesGroup .selectItem( surfacesOpen );
            break;
        
        case Graph3D .FILLED_FRAME :
            surfacesGroup .selectItem( surfacesFilledFramed );
            break;
        
        case Graph3D .FILLED_NOFRAME :
            surfacesGroup .selectItem( surfacesFilledUnframed );
            break;
        }
        // suspend graphs? 
        drawSuspended .setState( canvas .getSuspendState() );
        // group
        setGraphGroupState( canvas .getGroup() != null );
        //show
        super.setVisible(true);
        /*
        canvas.setDrawMessage("Redrawing...");
        canvas.setDrawMessageColor(java.awt.Color.white);
        canvas.setDrawMessage(true);
        canvas.setDrawGraph(false);
        super .setVisible(true);
        canvas.setDrawMessage(false);
        canvas.setDrawGraph(true);
        canvas.redraw();
         */
    }

    public  void dispose() {
        demo .disposeGraphFrameAndContents( this, canvas, graph );
        super .dispose();
    }



    public void plotCreated( EditPlotWindow win, Plot plot ) {
      if (plot != null)
          if (!graph.containsPlot(plot))
              graph.addPlot(plot);
      super.plotCreated(win, plot); 
    }



    // ****************************** FILE I/O ****************************** //
    String canvas__, graph__;
    
    public GraphFrame3D(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("frame"), parser, 3);
        parser.pushDimension(3);
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
        canvas__ = null; graph__ = null;
        DependencyManager.setDependency(this, canvas);
        super.loadFileBind(parser);
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


