package demo.graph;

import java.awt.MenuItem;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.io.*;
import demo.Demo;
import demo.util.U;
import demo.plot.ui.EditPlotWindow;
import demo.plot.ui.EditPlotWindowListener;
import demo.plot.Plot;
import demo.plot.ui.EditPlotWindowCreator;

/**
 * GraphFrameUserPlottable is a GraphFrame that allows the user to make plots.
 * It has a Plots menu, which allows the user to make plots.
 *
 * @author deigen
 */
public  abstract class GraphFrameUserPlottable extends GraphFrame implements EditPlotWindowListener {

    public  GraphFrameUserPlottable( String title, Demo demo, Environment env, int dimension ) {
        super( title );
        environment = env;
        editPlotWindowCreator_ = new EditPlotWindowCreator(demo, env, dimension);
        dimension_ = dimension;
    }

    protected Environment environment;

    protected 
    java.util .Dictionary plotMenuItems = new java.util .Hashtable( 7 );

    protected  AWTMenu plotMenu = new AWTMenu("Plot");
    protected MenuItem editPlotVisibleMenuItem = new MenuItem("Show/Hide in Menu...");

    private EditPlotWindowCreator editPlotWindowCreator_;

    private int dimension_;

    /**
     * @return the dimension of this plot (currently, corresponds to GraphFrame2D or GraphFrame3D)
     */
    public int dimension() {
        return dimension_;
    }

    /**
     * @return an enumeration of all plots that could go into the Plot menu for editing
     */
    public abstract java.util.Enumeration getEditablePlots();


    /**
     * Adds a menu item for the plot in the Plots menu.
     * @param plot the plot to add a menuitem for
     * @param label the text that should appear in the menu for the menuitem
     */
    public 
    void addPlotMenuItem( Plot plot, String label ) {
        if ( plotMenuItems.get(plot) != null ) {
            // already added to the menu. So just update the label
            ((PlotMenuItem) plotMenuItems.get(plot)).setLabel( label.charAt(0) != ' ' ? " " + label : label );
        }
        else {
            PlotMenuItem menuitem = new PlotMenuItem();
            menuitem .setPlot( plot );
            // put a space in front just in case it begins with a -
            menuitem .setLabel( label.charAt(0) != ' ' ? " " + label : label );
            this .plotMenuItems .put( plot, menuitem );
            this .plotMenu .add( menuitem );
        }
    }

    /**
     * Removes the menuitem in the Plots menu for a plot.
     * @param plot the plot to remove the menuitem of
     */
    public  void removePlotMenuItem( Plot plot ) {
        if ( plotMenuItems.get(plot) != null ) {
            plotMenu.remove( (PlotMenuItem) plotMenuItems.get(plot) );
            plotMenuItems.remove(plot);
        }
    }

    /**
     * Disables the menuitem for a plot.
     * @param plot the plot whose menuitem should be disabled
     */
    public  void disablePlotMenuItem( Plot plot ) {
        if ( plotMenuItems.get(plot) != null )
            ((PlotMenuItem) plotMenuItems.get(plot)).disable();
    }

    /**
     * Enables the menuitem for a plot.
     * @param plot the plot whose menuitem should be enabled
     */
    public  void enablePlotMenuItem( Plot plot ) {
        if ( plotMenuItems.get(plot) != null )
            ((PlotMenuItem) plotMenuItems.get(plot)).enable();
    }

    /** 
     * @return the plots that are shown in the menu
     */
    public  java.util .Enumeration getShownPlots() {
        java.util .Vector shownPlots = new java.util .Vector();
        java.util .Enumeration plotMenuItemsEnum = this .plotMenuItems .elements();
        while ( plotMenuItemsEnum .hasMoreElements() ) {
            shownPlots .addElement( ((PlotMenuItem) plotMenuItemsEnum .nextElement()) .getPlot() );
        }
        return shownPlots .elements();
    }

    /**
     * @return the labels of the plots that are shown in the menu (that is, how they appear in the menu)
     */
    public  java.util .Enumeration getShownPlotsLabels() {
        java.util .Vector labels = new java.util .Vector();
        java.util .Enumeration plotMenuItemsEnum = this .plotMenuItems .elements();
        while ( plotMenuItemsEnum .hasMoreElements() ) {
            String label = ((PlotMenuItem) plotMenuItemsEnum .nextElement()) .getLabel();
            labels .addElement( label );
        }
        return labels .elements();
    }


    /**
     * Should be called by subclasses when an EditPlotWindow is opened.
     * This method handles things such as enabling/disabling plot menu items for the plot,
     * and making the edit plot window an open dialog of this frame.
     * @param w the plot window just opened.
     * @return the given EditPlotWindow w
     */
    protected EditPlotWindow openedEditPlotWindow(EditPlotWindow w) {
        w.addEditPlotWindowListener(this);
        addOpenDialog(w);
        if (w.plot() != null)
            disablePlotMenuItem(w.plot());
        return w;
    }

    /**
     * Opens the EditPlotWindow for the given plot, and returns it.
     * Automatically calls openedEditPlotWindow on the window.
     * @return the window that was opened
     */
    protected EditPlotWindow openEditPlotWindow(Plot plot) {
        demo.util.Set listeners = new demo.util.Set();
        listeners.add(this);
        return editPlotWindowCreator_.openWindow(plot, listeners);
    }
    

    public void plotCreated( EditPlotWindow win, Plot plot ) {
        if (plot == null) return;
	if ( plotMenuItems.get(plot) == null ) {
            addPlotMenuItem(plot, makePlotLabel(plot));
	    disablePlotMenuItem(plot);
        }
    }

    public void plotChanged( EditPlotWindow win, Plot plot ) {
        if (plot == null) return;
        if (plotMenuItems.get(plot) == null) return;
        ((PlotMenuItem) plotMenuItems.get(plot)).setLabel( makePlotLabel(plot) );
    }

    public void plotWindowOpened( EditPlotWindow win, Plot plot ) {
        openedEditPlotWindow(win);
    }
    
    public void plotWindowCanceled( EditPlotWindow win, Plot plot ) {
        if (plot == null) return;
        enablePlotMenuItem(plot);
    }
    
    public void plotWindowOKed( EditPlotWindow win, Plot plot ) {
        if (plot == null) return;
        if ( plotMenuItems.get(plot) == null ) {
            addPlotMenuItem(plot, makePlotLabel(plot));
        }
        else {
            enablePlotMenuItem(plot);
        }
    }

    public void plotWindowRemoved( EditPlotWindow win, Plot plot ) {
        if (plot == null) return;
        removePlotMenuItem(plot);
    }
    
    private String makePlotLabel( Plot plot ) {
        String label = plot.title();
        if ( label.charAt(0) != ' ' )
            label = " " + label;
        return label;
    }

    public boolean action(java.awt.Event e, Object o) {
        if (e.target == editPlotVisibleMenuItem) {
            editPlotVisibleMenuItem.setEnabled(false);
            EditVisibleWindow win = new EditVisibleWindow(getEditablePlots(), new VisibleWindowCallback());
            addOpenDialog(win);
            win.setVisible(true);
            return true;
        }
        return super.action(e, o);
    }

    protected class VisibleWindowCallback implements EditVisibleWindowCallback {
        public boolean isObjectVisible(Object obj) {
            return plotMenuItems.get(obj) != null;
        }
        public void setObjectVisible(Object obj, boolean vis) {
            Plot plot = (Plot) obj;
            if (vis)
                addPlotMenuItem(plot, U.clampString(plot.title(), 70));
            else
                removePlotMenuItem(plot);
        }
        public String getTitle(Object obj) {
            return ((Plot) obj).title();
        }
        public void editVisibleWindowClosed(EditVisibleWindow win) {
            editPlotVisibleMenuItem.setEnabled(true);
            removeOpenDialog(win);
        }
    }
    



    // ****************************** FILE I/O ****************************** //
    String[] plots__, labels__;
    boolean isVisible__ = true;
    
    public GraphFrameUserPlottable(Token tok, FileParser parser, int dimension) {
        FileProperties props = parser.parseProperties(tok);
        dimension_ = dimension;
        setTitle(parser.parseWord(props.get("title")));
        plots__ = parser.parseObjectList(props.get("plots"));
        labels__ = parser.parseWordList(props.get("labels"));
        if (plots__.length != labels__.length)
            parser.error("must have the same number of plots as labels for menuitems");
        if (props.contains("visible"))
            isVisible__ = parser.parseBoolean(props.get("visible"));
    }

    public void loadFileBind(FileParser parser) {
        for (int i = 0; i < plots__.length; ++i)
            addPlotMenuItem((Plot) parser.getObject(plots__[i]), labels__[i]);
        plots__ = null; labels__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        this.environment = parser.currEnvironment();
        editPlotWindowCreator_ = new EditPlotWindowCreator(parser.demo(),
                                                           this.environment,
                                                           dimension_);
    }

    public void loadFileFinish(FileParser parser) {
        if (isVisible__) {
            this.setLocation(parser.currGraphFrameLocation());
            this.setVisible(true);
            parser.incrementGraphFrameLocation();
        }
        else {
            this.setLocation(100,100);
        }
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("title", generator.generateWord(getTitle()));
        java.util.Enumeration menuitems = plotMenuItems.elements();
        Plot[] plots = new Plot[plotMenuItems.size()];
        String[] labels = new String[plotMenuItems.size()];
        for (int i = 0; menuitems.hasMoreElements(); ++i) {
            PlotMenuItem item = (PlotMenuItem) menuitems.nextElement();
            plots[i] = item.getPlot();
            labels[i] = item.getLabel();
        }
        props.add("plots", generator.generateObjectIDList(plots));
        props.add("labels", generator.generateWordList(labels));
        props.add("visible", generator.generateBoolean(this.isVisible()));
        return generator.generateProperties(props);
    }

    
}


