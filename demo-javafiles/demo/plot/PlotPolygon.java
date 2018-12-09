package demo.plot;

import mathbuild.value.*;
import mathbuild.type.*;
import mathbuild.Executor;
import mathbuild.MB;
import mathbuild.Operator;

import demo.io.*;
import demo.gfx.*;
import demo.util.*;
import demo.depend.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.STEConstant;
import demo.gfx.drawable.PolygonPoint;
import demo.gfx.drawable.PolygonLine;

/**
 * Plot for polygons
 * @author deigen
 */
public class PlotPolygon extends Plot implements Dependable, FileObject {

    private java.util.Vector vertices_;
    private Coloring coloring_;
    private String title_;
    private int subdivisionsPerLine_ = 1;
    private boolean drawThick_ = false;
    private int dimension_;

    private DemoColor[] colors_; // colors for each face eval'ed from colorings
    private PlotOutputArrays output_ = new PlotOutputArrays();

    private STEConstant pointTableEntry_;
    private int currVertexNum_;
    private int[] currEdge_ = new int[]{0,0};
    
    public PlotPolygon(String title, Coloring coloring, int dimension) {
        this(new java.util.Vector(),
             coloring,
             title, dimension);
    }

    protected PlotPolygon(java.util.Vector vertices,
                          Coloring coloring,
                          String title,
                          int dimension) {
        vertices_ = vertices;
        coloring_ = coloring;
        title_ = title;
        dimension_ = dimension;
        makeObjectTableEntry();
        init();
    }

    public int currVertexNum() {
        return currVertexNum_;
    }

    public int[] currEdge() {
        return currEdge_;
    }

    public Value vertex(int i) {
        if (i < 0) i = 0;
        if (i >= vertices_.size()) i = vertices_.size() - 1;
        return ((Expression) vertices_.elementAt(i)).evaluate();
    }

    public int numVertices() {
        return vertices_.size();
    }

    public ValueVector currPoint() {
        return (ValueVector) pointTableEntry_.value();
    }

    private boolean __madeTableEntry__ = false;
    public void makeObjectTableEntry() {
        if (__madeTableEntry__) return; __madeTableEntry__ = true;
        pointTableEntry_ = new STEConstant("Point",
                                           new ValueVector(dimension_ == 2 ? new double[]{0,0}
                                                                           : new double[]{0,0,0}));
        plotEntry_.addMember("CurVertex", this, "index currVertexNum()");
        plotEntry_.addMember("CurEdge", this, "index[2] currEdge()");
        plotEntry_.addMember("NumVertices", this, "int numVertices()");
        plotEntry_.addMember("Vertex", this, "ValueVector<" + dimension_ + "> vertex(index)");
        plotEntry_.addMember("Point", this, "ValueVector<" + dimension_ + "> currPoint()");
        definitions_.put("Point", pointTableEntry_);
    }

    protected void init() {
        DependencyManager.addDependencies(this, vertices_.elements());
        DependencyManager.setDependency(this, coloring_);        
    }

    /**
     * Sets the title of the polygon.
     * @param title the title
     */
    public void setTitle(String title) {
        title_ = title;
    }

    /**
     * Sets the vertices of the polygon.
     * @param vertices a vector containing the vertices
     */
    public  void setVertices(java.util.Vector vertices) {
        for (java.util.Enumeration verts = vertices_.elements();
             verts.hasMoreElements();)
            ((Expression) verts.nextElement()).dispose();
        DependencyManager.addDependencies(this, vertices.elements());
        vertices_ = vertices;
    }

    /**
     * Sets the coloring for the plot.
     * @param the coloring for the plot
     */
    public  void setColoring(Coloring coloring) {
        if (coloring_ != null) {
            DependencyManager.removeDependency(this, coloring);
            coloring_.dispose();
        }
        DependencyManager.setDependency(this, coloring);
        coloring_ = coloring;
    }

    /**
     * Sets the expression for a given vertex.
     * @param i the vertex number
     * @param expr the expression for the vertex
     */
    public void setVertex(int i, Expression expr) {
        ((Expression) vertices_.elementAt(i)).dispose();
        vertices_.setElementAt(expr, i);
        DependencyManager.setDependency(this, expr);
    }

    /**
     * Adds a vertex.
     * @param expr the expression for the vertex
     */
    public void addVertex(Expression expr) {
        vertices_.addElement(expr);
        DependencyManager.setDependency(this, expr);
    }

    /**
     * Removes a vertex.
     * @param i the index of the vertex to remove
     */
    public void removeVertex(int i) {
        ((Expression) vertices_.elementAt(i)).dispose();
        vertices_.removeElementAt(i);
    }

    /**
     * Sets whether lines are drawn thick
     * @param b whether lines should be drawn thick
     */
    public  void setDrawThick(boolean b) {
        drawThick_ = b;
    }

    /**
     * Sets the number of line segments per edge. Multiple line
     * segments are used per edge to color an edge by position on the polygon.
     * @param subdivs the number of subdivisions per edge
     */
    public void setEdgeSubdivisions(int subdivs) {
        subdivisionsPerLine_ = subdivs;
    }


    public  void calculatePlot() {
        calculatePoints();
        calculateColors();
        calculateVectors();
        calculatePolygons();
        output_.setOutput();
    }

    /**
     * Calculates the PointSortables for the polygon.
     */
    private  void calculatePoints() {
        if (vertices_.size() == 0) {
            output_.makeBufferPoints(0);
            return;
        }
        PointSortable[] points = output_.makeBufferPoints(
                                    (vertices_.size() - 1) * subdivisionsPerLine_ + 1);
        double lineIncr = 1.0 / (double) subdivisionsPerLine_;
        int p = 0; // index into points array
        PointSortable currPt = new PointSortable(
                    (ValueVector) ((Expression) vertices_.elementAt(0)).evaluate(),
                                                 2,extraZ_);
        for (int i = 1; i < vertices_.size(); ++i) {
            PointSortable nextPt = new PointSortable(
                    (ValueVector) ((Expression) vertices_.elementAt(i)).evaluate(),
                                                     2,extraZ_);
            double c = 0;
            for (int j = 0; j < subdivisionsPerLine_; ++j) {
                // interpolate between curr pt and next pt
                points[p] = PointSortable.interpolate(nextPt, currPt, c, 2);
                c += lineIncr;
                ++p;
            }
            currPt = nextPt;
        }
        points[p] = currPt;
        // assert that p ends up at the end:
        if (p != points.length - 1)
            throw new RuntimeException("didn't get to end of pts array: "
                                       + p + "!=" + (points.length-1));
    }

    /**
     * Calculates the color for each part of each line segment.
     */
    private  void calculateColors() {
        PointSortable[] points = output_.bufferPoints();
        if (points.length == 0) {
            colors_ = new DemoColor[0];
            return;
        }
        if (points.length == 1) {
            colors_ = new DemoColor[1];
            coloring_.setCache();
            currVertexNum_ = 1;
            currEdge_ = new int[]{0,0};
            if (dimension_ == 2)
                pointTableEntry_.setValue(new ValueVector(new double[]{points[0].untransformedCoords[0], points[0].untransformedCoords[1]}));
            else
                pointTableEntry_.setValue(new ValueVector(points[0].untransformedCoords));
            colors_[0] = new DemoColor(coloring_.calculate());
            return;
        }
        // general case
        colors_ = new DemoColor[points.length - 1];
        coloring_.setCache();
        if (dimension_ == 2) {
            for (int i = 0; i < colors_.length; ++i) {
                double[] coords = points[i].untransformedCoords;
                currVertexNum_ = i/subdivisionsPerLine_;
                currEdge_ = new int[]{currVertexNum_, currVertexNum_ + 1};
                pointTableEntry_.setValue(new ValueVector(new double[]{coords[0],coords[1]}));
                colors_[i] = new DemoColor(coloring_.calculate());
            }            
        }
        else {
            for (int i = 0; i < colors_.length; ++i) {
                currVertexNum_ = i/subdivisionsPerLine_;
                currEdge_ = new int[]{currVertexNum_, currVertexNum_ + 1};
                pointTableEntry_.setValue(new ValueVector(points[i].untransformedCoords));
                colors_[i] = new DemoColor(coloring_.calculate());
            }
        }
    }

    /**
     * Calculates the lighting vectors for the polygon based on the
     * points, which were already caculated.
     */
    private void calculateVectors() {
        PointSortable[] points = output_.bufferPoints();
        if (points.length < 2) {
            output_.makeBufferLightingVectors(0);
            return;
        }
        LightingVector[] lvecs = output_.makeBufferLightingVectors(points.length - 1);
        for (int i = 0; i < lvecs.length; ++i) {
            lvecs[i] = new TangentVector(M.normalize(M.sub(M.point(points[i+1]),
                                                           M.point(points[i]))));
        }
    }
    
    /**
     * Calculates the polygons for the curve based on points and
     * colors that were already calculated.
     */
    private  void calculatePolygons() {
        PointSortable[] points = output_.bufferPoints();
        LinkedList polygonList = output_.makeBufferDrawables();
        if (points.length == 0)
            return;
        if (points.length == 1) {
            if (colors_[0].alpha > transparencyThreshold_) {
                PolygonPoint p = new PolygonPoint(points[0], colors_[0]);
                polygonList.add(p);
            }
            return;
        }
        // general case
        LightingVector[] lvecs = output_.bufferLightingVectors();
        for (int i = 0; i < points.length - 1; ++i) {
            if (colors_[i].alpha > transparencyThreshold_) {
                // TODO: flat line
                PolygonLine l = new PolygonLine(points[i], points[i+1],
                                                colors_[i], colors_[i],
                                                lvecs[i], lvecs[i]);
                l.setDrawThick(drawThick_);
                polygonList.add(l);
            }
        }
    }

    public PlotOutput output() {
        return output_;
    }
    
    public  String title() {
        return title_;
    }

    public java.util.Vector vertices() {
        return vertices_;
    }

    /**
     * @return whether lines are set to draw thick
     */
    public boolean drawThick() {
        return drawThick_;
    }
    
    /**
     * Gets the number of line segments per edge. Multiple line
     * segments are used per edge to color an edge by position on the polygon.
     * @return the number of subdivisions per edge
     */
    public int edgeSubdivisions() {
        return subdivisionsPerLine_;
    }

    public Coloring coloring() {
        return coloring_;
    }

    
    public void dispose() {
        super.dispose();
        DependencyManager.remove(this);
        java.util.Enumeration exprs = vertices_.elements();
        while (exprs.hasMoreElements())
            ((Expression) exprs.nextElement()).dispose();
        coloring_.dispose();
    }


    
    // ****************************** FILE I/O ****************************** //
    private String coloring__;
    private String[] vertexExprs__;
    
    public PlotPolygon(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        title_ = parser.parseWord(props.get("title"));
        dimension_ = (int) parser.parseNumber(props.get("dim"));
        drawThick_ = parser.parseBoolean(props.get("thick"));
        subdivisionsPerLine_ = (int) parser.parseNumber(props.get("subdivs"));
        coloring__ = parser.parseObject(props.get("color"));
        vertexExprs__ = parser.parseExpressionList(props.get("vertices"));
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
        coloring_ = (Coloring) parser.getObject(coloring__);
        DependencyManager.setDependency(this, coloring_);
        coloring__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        vertices_ = new java.util.Vector();
        for (int i = 0; i < vertexExprs__.length; ++i)
            vertices_.addElement(parser.recognizeExpression(vertexExprs__[i]));
        DependencyManager.addDependencies(this, vertices_.elements());
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        parser.loadExprs(coloring_);
        parser.popEnvironment();
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("title", generator.generateWord(title_));
        props.add("dim", generator.generateNumber(dimension_));
        props.add("thick", generator.generateBoolean(drawThick_));
        props.add("subdivs", generator.generateNumber(subdivisionsPerLine_));
        props.add("color", generator.generateObject(coloring_));
        props.add("vertices", generator.generateExpressionList(vertices_.elements()));
        return generator.generateProperties(props);
    }

    
}

