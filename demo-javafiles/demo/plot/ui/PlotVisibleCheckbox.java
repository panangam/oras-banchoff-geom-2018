package demo.plot.ui;

import java.awt.*;

import demo.depend.*;
import demo.io.*;
import demo.util.Set;
import demo.plot.Plot;
import demo.plot.PlotListener;

public class PlotVisibleCheckbox extends Panel implements Dependable, FileObject, PlotListener {
    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    public DependencyNode dependencyNode() {return myDependencyNode_;}

    Checkbox checkbox_;

    java.util.Vector plots_;
    java.util.Vector values_;
    String label_;

    public PlotVisibleCheckbox( String label ) {
        this(label, new java.util.Vector(0), new java.util.Vector(0));
    }
        
    public PlotVisibleCheckbox( String label, Plot plot ) {
        label_ = label;
        plots_ = new java.util.Vector();
        values_ = new java.util.Vector();
        plots_.addElement(plot);
        values_.addElement(new Boolean(true));
        DependencyManager.setDependency(this, plot);
        plot.addPlotListener(this);
        init(label_);
    }

    private void init(String label) {
        checkbox_ = new Checkbox(label);
        boolean state = true;
        for (int i = 0; i < plots_.size(); ++i) {
            state = state && (((Plot) plots_.elementAt(i)).isVisible() == ((Boolean) values_.elementAt(i)).booleanValue());
        }
        checkbox_.setState( state );
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(checkbox_);
    }

    public PlotVisibleCheckbox( String label, java.util.Vector plots, java.util.Vector showWhenTrue ) {
        label_ = label;
        plots_ = (java.util.Vector) plots.clone();
        values_ = (java.util.Vector) showWhenTrue.clone();
        DependencyManager.setDependencies(this, plots_.elements());
        for (int i = 0; i < plots.size(); ++i)
            ((Plot) plots.elementAt(i)).addPlotListener(this);
        init(label_);
    }
    
    public void dependencyUpdateDef( Set updatingObjects ) {
        boolean state = true;
        for (int i = 0; i < plots_.size(); ++i)
            state &= ( ((Plot) plots_.elementAt(i)).isVisible() == ((Boolean) values_.elementAt(i)).booleanValue() );
        checkbox_.setState( state );
    }

    public void dependencyUpdateVal( Set updatingObjects ) {}
    
    public boolean action( Event ev, Object o ) {
        if (ev.target == checkbox_) {
            if (checkbox_.getState())
                for (int i = 0; i < plots_.size(); ++i)
                    ((Plot) plots_.elementAt(i)).setVisible( ((Boolean) values_.elementAt(i)).booleanValue() );
            else
                for (int i = 0; i < plots_.size(); ++i)
                    ((Plot) plots_.elementAt(i)).setVisible( !((Boolean) values_.elementAt(i)).booleanValue() );
            DependencyManager.updateDependentObjectsDefMT(plots_.elements());
            return true;
        }
        return false;
    }
    
    public String getLabel() {
        return label_;
    }

    public boolean getState() {
        return checkbox_.getState();
    }

    public void setLabel(String label) {
        label_ = label;
        checkbox_.setLabel(label);
    }
    
    public java.util.Vector getPlots() {
        return plots_;
    }
    
    public java.util.Vector getShowAtStateValues() {
        return values_;
    }

    public void setShowAtStateValue(Plot p, boolean b) {
        for (int i = 0; i < plots_.size(); ++i)
            if (plots_.elementAt(i) == p) {
                values_.setElementAt(new Boolean(b), i);
                return;
            }
    }
    
    public int numPlots() {
        return plots_.size();
    }

    public void addPlot(Plot plot, boolean showWhenChecked) {
        plots_.add(plot);
        values_.add(new Boolean(showWhenChecked));
        DependencyManager.setDependency(this, plot);
    }
    
    public void removePlot(Plot plot) {
        int i;
        for (i = 0; i < plots_.size(); ++i)
            if (plots_.elementAt(i) == plot)
                break;
        if (i < plots_.size()) {
            plot.removePlotListener(this);
            DependencyManager.removeDependency(this, plot);
            plots_.removeElementAt(i);
            values_.removeElementAt(i);
        }
    }

    public boolean disposed_ = false;
    public void dispose() {
        disposed_ = true;
        for (int i = 0; i < plots_.size(); ++i)
            ((Plot) plots_.elementAt(i)).removePlotListener(this);
        DependencyManager.remove(this);
    }

    public void removeNotify() {
        if (!disposed_)
            dispose();
        super.removeNotify();
    }


    public void plotDisposed(Plot plot) {
        removePlot(plot);
    }

    public boolean plotCanDispose(Plot plot) {
        return true;
    }




    // ****************************** FILE I/O ****************************** //
    private String[] plots__;
    
    public PlotVisibleCheckbox(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        plots_ = new java.util.Vector();
        values_ = new java.util.Vector();
        label_ = parser.parseWord(props.get("label"));
        plots__ = parser.parseObjectList(props.get("plots"));
        boolean[] vals = parser.parseBooleanList(props.get("values"));
        for (int i = 0; i < vals.length; ++i)
            values_.addElement(new Boolean(vals[i]));
        init(label_);
    }

    public void loadFileBind(FileParser parser) {
        for (int i = 0; i < plots__.length; ++i) {
            Plot p = (Plot) parser.getObject(plots__[i]);
            plots_.addElement(p);
            p.addPlotListener(this);
        }
        DependencyManager.addDependencies(this, plots_.elements());
        plots__ = null;
    }

    public void loadFileExprs(FileParser parser) {
    }

    public void loadFileFinish(FileParser parser) {
        boolean state = true;
        for (int i = 0; i < plots_.size(); ++i) {
            state = state && (((Plot) plots_.elementAt(i)).isVisible() == ((Boolean) values_.elementAt(i)).booleanValue());
        }
        checkbox_.setState(state);
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("label", generator.generateWord(label_));
        props.add("plots", generator.generateObjectIDList(plots_.elements()));
        boolean[] vals = new boolean[values_.size()];
        for (int i = 0; i < vals.length; ++i)
            vals[i] = ((Boolean) values_.elementAt(i)).booleanValue();
        props.add("values", generator.generateBooleanList(vals));
        return generator.generateProperties(props);
    }

}
