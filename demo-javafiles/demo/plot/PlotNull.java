//
//  PlotNull.java
//  Demo
//
//  Created by David Eigen on Mon Mar 24 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.plot;

import demo.io.*;

/**
 * The Null plot does not produce any points or drawable objects.
 */
public class PlotNull extends Plot {

    private PlotOutput output_ = new PlotOutputArrays();
    
    public PlotNull() {
    }

    public void calculatePlot() {
    }

    public PlotOutput output() {
        return output_;
    }

    public String title() {
        return "Nothing";
    }


    // ****************************** FILE I/O ****************************** //

    public PlotNull(Token tok, FileParser parser) {
    }

    public void loadFileBind(FileParser parser) {
    }

    public void loadFileExprs(FileParser parser) {
    }

    public void loadFileFinish(FileParser parser) {
        ensureCalculated();
    }

    public Token saveFile(FileGenerator generator) {
        return generator.generateWord("x");
    }

}
