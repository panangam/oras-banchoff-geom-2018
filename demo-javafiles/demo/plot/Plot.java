package demo.plot;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.*;
import demo.io.*;
import demo.gfx.*;
import demo.util.Set;
import demo.expr.Expression;
import demo.expr.ExprObject;
import demo.expr.ste.SymbolTableEntry;
import demo.expr.ste.STERecord;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * A plot is something that produces drawable objects (and the points used by those objects).
 * Examples of plots are surfaces (which produce triangles) and curves (which produce line segments).
 *
 * @author deigen
 */
public  abstract class Plot extends Object
implements RayIntersectable, Dependable, FileObject, ExprObject {

    protected Set plotListeners_ = new Set();
    
    // locally defined objects (like plot position)
    protected Environment definitions_ = new Environment();

    protected static final String PLOT_ENTRY_NAME = "Plot";
    protected STERecord plotEntry_ = new STERecord(PLOT_ENTRY_NAME);

    private DependencyNode myDependencyNode_ = new DependencyNode(this);

    protected boolean visible_ = true;

    protected boolean calculated_ = false;
    
    private boolean calculateOnUpdate_ = true;

    

    /**
     * Plots should not add drawables whose color's alpha value is less 
     * than or equal to transparencyThreshold_
     */
    protected double transparencyThreshold_ = 0;
    private Expression transparencyThresholdExpr_ = Demo.recognizeExpression("0", new Environment());

    /**
     * Priority should be passed to all PointSortables created by subclasses.
     * This value is added to the z coordinate of the PointSortable after
     * transformations are applied to the point. This allows different plots to
     * always draw on top of other plots (that is, to "layer" the plos) without
     * any objects actually appearing in a different place to the user.
     */
    protected double extraZ_ = 0;
    
    public Plot() {
        definitions_.put(PLOT_ENTRY_NAME, plotEntry_);
    }

    public void makeObjectTableEntry() {
        // to be overridden by subclasses
    }
        
    /**
     * Calculates the points and drawable objects. Note that since
     * the points are recalculated, they also need to be retransformed
     * before they are drawn to the screen. The ponts and drawable objects can be
     * accessed using the numPoints, copyPoints, and drawableObjects methods.
     */
    public synchronized void calculate() {
        transparencyThreshold_ = ((ValueScalar) transparencyThresholdExpr_.evaluate()).number();
        if (transparencyThreshold_ < 0)
            transparencyThreshold_ = 0;
        else if (transparencyThreshold_ > 1)
            transparencyThreshold_ = 1;
		calculatePlot();
    }

    /**
     * This is the internal implementation of calculate. Plots must implement this method.
     */
    public abstract void calculatePlot();
    

    /**
     * Calculates the plot if it has not been calculated already.
     */
	public synchronized void ensureCalculated() {
		if (!calculated_) {
			calculate();
			calculated_ = true;
		}
	}
	
    /**
     * @return the output for this plot (contains the points and drawable objects)
     */
    public abstract PlotOutput output();
    
    /**
     * @return the titile for this plot (what should be shown to the user)
     */
    public abstract String title();

    /**
     * @return whether this plot is visible in the graph
     */
    public boolean isVisible() {
        return visible_;
    }

    /**
     * Sets whether this plot is visible in its graph.
     */
    public void setVisible(boolean b) {
        visible_ = b;
        ensureCalculated();
    }

    /**
     * @return the layer level of this plot
     */
    public double extraZ() {
        return extraZ_;
    }

    /**
     * Sets the layer level of this plot
     */
    public void setExtraZ(double level) {
        extraZ_ = level;
        calculated_ = false;
    }

    /**
     * @return the transparency threshold of this plot
     */
    public Expression transparencyThreshold() {
        return transparencyThresholdExpr_;
    }

    /**
     * Sets the transparency threshold of this plot
     */
    public void setTransparencyThreshold(Expression expr) {
        transparencyThresholdExpr_.dispose();
        transparencyThresholdExpr_ = expr;
        DependencyManager.setDependency(this, transparencyThresholdExpr_);
        calculated_ = false;
    }

    /**
     * Adds a plot listener.
     */
    public void addPlotListener( PlotListener lis ) {
        plotListeners_.add(lis);
    }

    /**
     * Removes a plot listener.
     */
    public void removePlotListener( PlotListener lis ) {
        plotListeners_.remove(lis);
    }

    /**
     * @return an Environment containing the locally defined expression definitions,
     *         such as the position of the plot.
     */
    public Environment expressionDefinitions() {
        return definitions_;
    }

    /**
     * @return the SymbolTableEntry that represents this plot in Mathbuild expressions.
     */
    public SymbolTableEntry tableEntry() {
        return plotEntry_;
    }

    /**
     * @return the SymbolTableEntry that represents this plot in Mathbuild expressions.
     */
    public SymbolTableEntry objectTableEntry() {
        return plotEntry_;
    }

    /**
     * Intersects this plot with a ray.
     * Can be overridden by subclasses -- this method simply checks all drawables
     * that implement RayIntersectable for intersection.
     */
    public boolean intersect(Ray ray, RayIntersection intersection) {
        java.util.Enumeration drawables = output().outputDrawableObjects().elements();
        double t = Double.POSITIVE_INFINITY;
        RayIntersection i = new RayIntersection();
        boolean intersected = false;
        Object obj;
        while (drawables.hasMoreElements()) {
            obj = drawables.nextElement();
            if (obj instanceof RayIntersectable && ((RayIntersectable) obj).intersect(ray, i)) {
                intersected = true;
                if (i.t < t) {
                    intersection.set(i);
                    t = i.t;
                }
            }
        }
        return intersected;
    }
    
    /**
     * If a subclass overrides dispose(), it must call super.dispose().
     * @param demo this is so we can dispose colorings well. This should go away in the
     * future with new file format.
     */
    public void dispose() {
        DependencyManager.remove(this);
        for (java.util.Enumeration l = plotListeners_.elements();
             l.hasMoreElements();)
            ((PlotListener) l.nextElement()).plotDisposed(this);
    }

    /**
     * Whether this plot can be disposed.
     */
    public boolean disposable() {
        java.util.Enumeration dependents =
                DependencyManager.getDirectlyDependentObjects(this).elements();
        while (dependents.hasMoreElements()) {
            Object dep = dependents.nextElement();
            if (!(dep instanceof PlotListener))
                return false;
            if (!((PlotListener) dep).plotCanDispose(this))
                return false;
        }
        for (java.util.Enumeration l = plotListeners_.elements();
             l.hasMoreElements();)
            if (!((PlotListener) l.nextElement()).plotCanDispose(this))
                return false;
        return true;
    }

    /**
     * subclasses must call super.dependencyUpdateX(.) if they override dependencyUpdateX(.)
     * This call to super.dependencyUpdateX must be the first thing the overriding method does
     */
    public void dependencyUpdateVal(Set updatingObjects) {
        // whether this plot is calculated is handled by the plot's owner
        calculated_ = false;
    }

    /**
     * subclasses must call super.dependencyUpdateX(.) if they override dependencyUpdateX(.)
     * This call to super.dependencyUpdateX must be the first thing the overriding method does
     */
    public void dependencyUpdateDef(Set updatingObjects) {
        // subclasses must call super.dependencyUpdateX(.) if they override dependencyUpdateX(.)
        // whether this plot is calculated is handled by the plot's owner
        calculated_ = false;
    }

    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }



    // ****************************** FILE I/O ****************************** //
    String transparencyThresholdExpr__;
    
    public Plot(Token tok, FileParser parser) {
        this();
        FileProperties props = parser.parseProperties(tok);
        visible_ = parser.parseBoolean(props.get("visible"));
        // below properties added in a newer version (2.0.6)
        if (props.contains("transpt"))
            transparencyThresholdExpr__ = parser.parseExpression(props.get("transpt"));
        if (props.contains("addedz"))
            extraZ_ = parser.parseNumber(props.get("addedz"));
    }

    public void loadFileBind(FileParser parser) {
    }

    public void loadFileExprs(FileParser parser) {
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        transparencyThresholdExpr_ = parser.recognizeExpression(transparencyThresholdExpr__);
        parser.popEnvironment();
        DependencyManager.setDependency(this, transparencyThresholdExpr_);
    }

    public void loadFileFinish(FileParser parser) {
        ensureCalculated();
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("visible", generator.generateBoolean(visible_));
        // properties below here added in a newer version (2.0.6)
        props.add("transpt", generator.generateExpression(transparencyThresholdExpr_));
        props.add("addedz", generator.generateNumber(extraZ_));
        return generator.generateProperties(props);
    }    

}


