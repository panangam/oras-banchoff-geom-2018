package demo.plot.ui;

import java.awt .*;
import java.awt.event.*;
import mathbuild.Environment;

import demo.*;
import demo.ui.*;
import demo.depend.*;
import demo.exec.*;
import demo.util.Set;
import demo.plot.Plot;

public  abstract class EditPlotWindow extends DemoFrame {
    
    private java.util.Dictionary listeners_ = new java.util.Hashtable(3);
    
    protected Plot plot;

    protected Demo demo;
    
    private boolean editing;

    protected Environment environment_;
    
    private int exitStatus = 0;
    
    protected static final int EXIT_STATUS_CANCEL = 0;
    protected static final int EXIT_STATUS_OK = 1;
    protected static final int EXIT_STATUS_REMOVE = 2;
    
    
    protected EditPlotWindow( Plot plot, Demo demo, Environment env, boolean editing, Set listeners ) {
        this(plot, demo, env, "", editing, listeners);
    }
    
    protected EditPlotWindow( Plot plot, Demo demo, Environment env, String title, boolean editing, Set listeners ) {
        super(title);
        this.plot = plot;
        this.demo = demo;
        this.environment_ = env;
        this.editing = editing;
        for (java.util.Enumeration l = listeners.elements();
             l.hasMoreElements();)
            addEditPlotWindowListener((EditPlotWindowListener) l.nextElement());
        setResizable(false);
        // send open event to listeners
        for (java.util.Enumeration l = listeners_.elements();
             l.hasMoreElements();)
            ((EditPlotWindowListener) l.nextElement()).plotWindowOpened(this, plot);
    }
    
    /** 
     * Adds an EditPlotWindowListener on which plotChanged(.) is called when the plot changes
     */
    public void addEditPlotWindowListener( EditPlotWindowListener l ) {
        listeners_.put(l,l);
    }
    
    /**
     * Removes an EditPlotWindowListener
     */
    public void removeEditPlotWindowListener( EditPlotWindowListener l ) {
        listeners_.remove(l);
    }

    /**
	 * @return the plot being edited by this window (null if no plot is created yet)
     */
    public Plot plot() {
        return plot;
    }
    
    /**
     * Disposes this window and sends a window closed event with exit status as OK
     * to all EditPlotListeners.
     */
    public void disposeWindowOK() {
        setExitStatus(EXIT_STATUS_OK);
        setVisible(false);
        dispose();
    }
    
    /**
     * Called by subclasses when the plot is updated.
     * Calculates the plot, updates dependent objects, and calls plotChanged(.) on all listeners.
     * Does nothing if the plot is null.
     */
    protected void updatePlot() {
        if (plot == null)
            return;
        Exec.run(new ExecCallback() {
            public void invoke() {
                plot.calculate();
                java.util.Enumeration listeners = listeners_.elements();
                while (listeners.hasMoreElements())
                    ((EditPlotWindowListener) listeners.nextElement())
                            .plotChanged(EditPlotWindow.this, plot);
                DependencyManager.updateDependentObjectsDefST(plot);
            }
        });
    }

    /**
     * Disposes the plot for this window.
     * @return whether the plot disposal was successful
     */
    protected boolean removePlot() {
        if (plot.disposable()) {
            plot.dispose();
            return true;
        }
        else {
            Demo.showError("You cannot remove this plot because there are things dependent on it.\nIf you want to remove this plot, remove the dependencies first, and try again.");
            return false;
        }
    }

    /**
     * Sends plotCreated(.) methods to all listeners.
     */
    protected void plotCreated() {
      if (plot == null)
	return;
      java.util.Enumeration listeners = listeners_.elements();
      while (listeners.hasMoreElements())
	((EditPlotWindowListener) listeners.nextElement()).plotCreated(this, plot);
    }
    
    /**
     * @return whether the plot is being edited, or created for the first time
     */
    protected boolean editing() {
        return editing;
    }
    
    /**
     * Sets the exit status of this window.
     * When the plot is disposed, the method corresponding to the exit status will 
     * be called on all listeners.
     * Possible exit statuses are defined in this class.
     */
    protected void setExitStatus(int status) {
        this.exitStatus = status;
    }
    
    public void dispose() {
        java.util.Enumeration listeners = listeners_.elements();
        while (listeners.hasMoreElements())
            switch (exitStatus) {
            case EXIT_STATUS_CANCEL:
                ((EditPlotWindowListener) listeners.nextElement()).plotWindowCanceled(this, plot);
                break;
            case EXIT_STATUS_OK:
                ((EditPlotWindowListener) listeners.nextElement()).plotWindowOKed(this, plot);
                break;
            case EXIT_STATUS_REMOVE:
                ((EditPlotWindowListener) listeners.nextElement()).plotWindowRemoved(this, plot);
                break;
            }
        super.dispose();
    }    


    /**
     * Used by subclasses to act appropriately when the OK button is pressed.
     * This class can (and usually should) be overridden to do other things.
     * Currently sets the exit status to OK and closes the window.
     * Note: this is not used for PlotExpressions (they were implemented before this)
     */
    protected class OKBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setExitStatus(EXIT_STATUS_OK);
            setVisible(false);
            dispose();
        }
    }

    /**
     * Used by subclasses to act appropriately when the Remove Plot button is pressed.
     * Note: this is not used for PlotExpressions (they were implemented before this)
     */
    protected class RemoveBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (removePlot()) {
                setExitStatus(EXIT_STATUS_REMOVE);
                setVisible(false);
                dispose();
            }
        }
    }

    /**
     * Used by subclasses to close the window when the close button is pressed.
     * When the user closes the window, this listener closes the window with exit
     * status EXIT_STATUS_CANCEL.
     * Note: this is not used for PlotExpressions (they were implemented before this)
     */
    protected class WindowLsnrCancel extends WindowListenerNoActions {
        public void windowClosing(WindowEvent e) {
            setExitStatus(EXIT_STATUS_CANCEL);
            setVisible(false);
            dispose();
        }
    }

    
    
    
}

