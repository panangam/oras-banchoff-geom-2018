package demo.io;
//
//  FileObjectIOClasses.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

public interface FileObjectIOClasses {

    /**
     * Add new classes for loading here.
     * The classes in this list must implement FileObjectIO.
     *
     * This array contains the names of all classes implementing
     * FileObjectIO; these are all the loaders that can be invoked
     * by the parser for parsing an object.
     */
    // LIST NEW CLASSES HERE
    static final FileObjectIO[] OBJECT_IO_CLASSES = new FileObjectIO[]{
        new FileFileObjectIO("demo.coloring.ColoringBase", "coloring.base"),
        new FileFileObjectIO("demo.coloring.ColoringChecker", "coloring.checker"),
        new FileFileObjectIO("demo.coloring.ColoringConstant", "coloring.constant"),
        new FileFileObjectIO("demo.coloring.ColoringExpression", "coloring.expression"),
        new FileFileObjectIO("demo.coloring.ColoringGradient", "coloring.gradient"),
        new FileFileObjectIO("demo.coloring.ColoringGroup", "coloring.group"),
        new FileFileObjectIO("demo.graph.Graph3D", "graph.3d"),
        new FileFileObjectIO("demo.graph.GraphCanvas3D", "canvas.3d"),
        new FileFileObjectIO("demo.graph.GraphFrame3D", "frame.3d"),
        new FileFileObjectIO("demo.graph.GraphFrame2D", "frame.2d"),
        new FileFileObjectIO("demo.graph.Hotspot", "hotspot"),
        new FileFileObjectIO("demo.graph.GraphGroup", "group"),
        new FileFileObjectIO("demo.plot.PlotNull", "plot.null"),
        new FileFileObjectIO("demo.plot.PlotAxes", "plot.axes"),
        new FileFileObjectIO("demo.plot.PlotPoint", "plot.point"),
        new FileFileObjectIO("demo.plot.PlotVector", "plot.vector"),
        new FileFileObjectIO("demo.plot.PlotCurve", "plot.curve"),
        new FileFileObjectIO("demo.plot.PlotSurface3D", "plot.surface"),
        new FileFileObjectIO("demo.plot.PlotWireframe", "plot.wireframe"),
        new FileFileObjectIO("demo.plot.PlotPolyhedron", "plot.polyhedron"),
        new FileFileObjectIO("demo.plot.PlotPolygon", "plot.polygon"),
        new FileFileObjectIO("demo.plot.PlotField", "plot.field"),
        new FileFileObjectIO("demo.plot.PlotLevelSet", "plot.levelset"),
        new FileFileObjectIO("demo.expr.ui.ReadoutPanel", "readout"),
        new FileFileObjectIO("demo.ControlsFrame", "controls"),
        new FileFileObjectIO("demo.plot.ui.PlotVisibleCheckbox", "plotcheckbox"),
        new FileFileObjectIO("demo.expr.ui.PlotIdentifierPanel", "plotid")
    };
    
}

