package demo.plot.ui;

import demo.plot.Plot;

/** 
 * 
 * @author deigen
 */
public interface EditPlotWindowListener {

    /**
     * Called when a plot is created.
     * @param win the EditPlotWindow that created the plot.
     * @param plot the plot that has been changed
     */
    public void plotCreated( EditPlotWindow win, Plot plot );

    /**
     * Called when the given plot is changed.
     * This method is called only when an existing plot is changed. When
     * a plot is first created, plotChanged(.) is NOT called.
     * @param win the EditPlotWindow that made the change
     * @param plot the plot that has been changed
     */
    public void plotChanged( EditPlotWindow win, Plot plot );

    /**
     * Called when a EditPlotWindow has been opened.
     * @param win the EditPlotWindow that is being closed
     * @param plot the Plot that the EditPlotWindow was editing
     */
    public void plotWindowOpened( EditPlotWindow win, Plot plot );

    /** 
     * Called when a EditPlotWindow has been closed (dismissed), due to the user 
     * clicking the cancel button.
     * @param win the EditPlotWindow that is being closed
     * @param plot the Plot that the EditPlotWindow was editing
     */
    public void plotWindowCanceled( EditPlotWindow win, Plot plot );
    
    /** 
     * Called when a EditPlotWindow has been closed (dismissed), due to the user 
     * clicking the OK button.
     * @param win the EditPlotWindow that is being closed
     * @param plot the Plot that the EditPlotWindow was editing
     */
    public void plotWindowOKed( EditPlotWindow win, Plot plot );

    /** 
     * Called when a EditPlotWindow has been closed (dismissed), due to the user 
     * clicking the remove plot button.
     * The plot should have already been removed from the program when this
     * method is called.
     * @param win the EditPlotWindow that is being closed
     * @param plot the Plot that the EditPlotWindow was editing
     */
    public void plotWindowRemoved( EditPlotWindow win, Plot plot );

    
}
