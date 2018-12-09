package demo.graph;

import demo.plot.Plot;

public class PlotMenuItem extends java.awt.MenuItem {

    private  Plot plot;

    public  void setPlot( Plot plot ) {
        this .plot = plot;
    }

    public  Plot getPlot() {
        return plot;
    }

    // labels (ie. getLabel(.), setLabel(.), etc. are handled by MenuItem

}


