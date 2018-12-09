package demo.plot;

import mathbuild.value.*;
import mathbuild.type.TypeVector;

import demo.io.*;
import demo.gfx.*;
import demo.util.*;
import demo.depend.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.gfx.drawable.Polygon3D;

/**
 * Plot for polyhedra
 * @author rlroth, deigen
 */
public class PlotPolyhedron extends Plot {

    private java.util.Vector vertices,faces,colorings;
    private Set edges;
    private String title;
    private int dimension;
    
    private int currVertex_;
    private Edge currEdge_;
    private int currFace_;
    private DemoColor[] colors; // colors for each face eval'ed from colorings
    private PlotOutputArrays output_ = new PlotOutputArrays();
    
    public PlotPolyhedron(String title, int dimension) {
        this(new java.util.Vector(), new java.util.Vector(), new java.util.Vector(), title, dimension);
    }

    public PlotPolyhedron(java.util.Vector vertices,
                          java.util.Vector faces,
                          java.util.Vector colorings,
                          String title,
                          int dimension) {
        super();
        this.vertices=vertices;
        this.faces=faces;
        this.colorings=colorings;
        this.title = title;
        this.dimension = dimension;
        calculateEdges();
        DependencyManager.addDependencies(this, vertices.elements());
        DependencyManager.addDependencies(this, colorings.elements());
        makeObjectTableEntry();
    }

    private boolean __madeTableEntry__ = false;
    public void makeObjectTableEntry() {
        if (__madeTableEntry__) return; __madeTableEntry__ = true;
        plotEntry_.addMember("CurVertex", this, "index currentVertex()");
        plotEntry_.addMember("CurEdge", this, "index[2] currentEdge()");
        plotEntry_.addMember("CurFace", this, "index currentFace()");
        plotEntry_.addMember("NumFaces", this, "int numFaces()");
        plotEntry_.addMember("NumVertices", this, "int numVertices()");
        plotEntry_.addMember("NumEdges", this, "int numEdges()");
        plotEntry_.addMember("NumFaceVertices", this, "int numFaceVertices(index)");
        plotEntry_.addMember("FaceVertex", this,
                             "ValueVector<"+dimension+"> faceVertex(index, index)");
        plotEntry_.addMember("Vertex", this, "ValueVector<"+dimension+"> vertex(index)");
        plotEntry_.addMember("CritEdge", this, "boolean edgeIsCritical(index[2], double)");
        plotEntry_.addMember("CritIndex", this, "int criticalVertexIndex(index)");
        plotEntry_.addMember("Vertices", this,
                             "ValueList<ValueVector<"+dimension+">> vertexList()");
        plotEntry_.addMember("Link", this, "ValueList<ValueScalar> vertexLink(index)");
    }

    /**
     * Sets the title of the polygon.
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the vertices and faces of the polyhedron.
     * @param vertices a vector containing the vertices
     * @param faces a vector containign the faces
     */
    public  void setPolyhedron(java.util.Vector vertices,java.util.Vector faces) {
        if (this.vertices != null)
            for (java.util.Enumeration verts = this.vertices.elements();
                 verts.hasMoreElements();)
                ((Expression) verts.nextElement()).dispose();
        this.vertices = vertices;
        DependencyManager.addDependencies(this, vertices.elements());
        this.faces = faces;
    }

    /**
     * Sets the colorings to the given colorings.
     * @param colorings the coloring to use (one coloring for each face)
     */
    public  void setColorings(java.util.Vector colorings) {
        if (this.vertices != null)
            for (java.util.Enumeration cols = this.colorings.elements();
                 cols.hasMoreElements();) {
                Coloring c = (Coloring) cols.nextElement();
                DependencyManager.removeDependency(this, c);
                c.dispose();
            }
        this .colorings = colorings;
        DependencyManager.addDependencies(this, colorings.elements());
    }

    public  void calculatePlot() {
        calculatePoints();
        calculateColors();
        calculatePolygons();
        output_.setOutput();
    }

    /**
     * Finds all edges in the plot, and stores them in the member Set edges,
     * as an Edge.
     */
    private void calculateEdges() {
        this.edges = new Set();
        for (int i = 0; i < faces.size(); ++i) {
            int[] verts = (int[]) faces.elementAt(i);
            for (int j = 0; j < verts.length; ++j) {
                int jj = (j + 1) % verts.length;
                if (verts[j] != verts[jj]) {
                    Edge e = new Edge(verts[j],verts[jj]);
                    if (edges.contains(e))
                        e = (Edge) edges.get(e);
                    else {
                        e.adjacentFaces = new Set();
                        edges.add(e);
                    }
                    e.adjacentFaces.add(new Integer(i));
                }
            }
        }
        for (java.util.Enumeration es = this.edges.elements();
             es.hasMoreElements();) {
            Edge e = (Edge) es.nextElement();
            e.adjacentFacesArray = new int[e.adjacentFaces.size()];
            int i = 0;
            for (java.util.Enumeration fs = e.adjacentFaces.elements();
                 fs.hasMoreElements();)
                e.adjacentFacesArray[i++] = ((Integer) fs.nextElement()).intValue();
        }
    }

    /**
     * Calculates the PointSortables for the polyhedron.
     */
    private  void calculatePoints() {
        PointSortable[] points = output_.makeBufferPoints(this.vertices.size());
        for ( int i = 0; i<vertices.size(); i++ ) {
            points[i]= new PointSortable( (ValueVector) ((Expression) vertices.elementAt(i)).evaluate(),
                                          3,extraZ_ );
        }
    }

    /**
     * Calculates the colors for each face of the polyhedron.
     */
    private  void calculateColors() {
        colors = new DemoColor[colorings.size()];
        for (int i = 0; i < colorings.size(); ++i) {
            currFace_ = i;
            Coloring c = (Coloring) colorings.elementAt(i);
            c.setCache();
            colors[i] = new DemoColor(c.calculate());
        }
    }

    /**
     * Calculates the polygons for the curve based on points and
     * colors that were already calculated.
     */
    private  void calculatePolygons() {
        LinkedList polygonList = output_.makeBufferDrawables();
        PointSortable[] points = output_.bufferPoints();
        LightingVector[] lvecs = output_.makeBufferLightingVectors(faces.size());
        for (int i = 0; i < faces.size(); i++) {
            if (colors[i].alpha <= transparencyThreshold_) {
                lvecs[i] = new NullLightingVector();
                continue;
            }
            int[] indexedPoints = (int[]) faces.elementAt(i);
            PointSortable[] face = new PointSortable[indexedPoints.length];
            for (int j=0;j<indexedPoints.length;j++)
                face[j] = points[indexedPoints[j]];
            LightingVector lv;
            if (face.length < 2)
                lv = new NullLightingVector();
            else if (face.length == 2)
                lv = new TangentVector(M.normalize(M.sub(M.point(face[1]), M.point(face[0]))));
            else { // face.length >= 3
				// find 3 non-colinear points
				double[] p1=null, p2=null, p3=null;
				boolean foundNonColinear = false;
				for (int u = 0; u < face.length && !foundNonColinear; ++u) {
					for (int v = u+1; v < face.length && !foundNonColinear; ++v) {
						for (int w = v+1; w < face.length && !foundNonColinear; ++w) {
							if (!M.colinear(p1 = M.point(face[u]), p2 = M.point(face[v]), p3 = M.point(face[w]))) {
								foundNonColinear = true;
							}
						}
					}
				}
				if (foundNonColinear)
					lv = new NormalVector(M.normalize(M.cross(M.sub(p1, p2), M.sub(p3, p2))));
				else
					lv = new TangentVector(M.normalize(M.sub(M.point(face[1]), M.point(face[0]))));
			}
            Polygon3D polygon = new Polygon3D(face, colors[i], new DemoColor(0,0,0,1), lv);
            polygonList.add(polygon);
            lvecs[i] = lv;
        }
    }


    public  String title() {
        return title;
    }

    public java.util.Vector vertices() {
        return vertices;
    }

    public java.util.Vector faces() {
        return faces;
    }

    public java.util.Vector colorings() {
        return colorings;
    }

    public void setVertex(int i, Expression expr) {
        ((Expression) vertices.elementAt(i)).dispose();
        vertices.setElementAt(expr, i);
        DependencyManager.setDependency(this, expr);
    }

    public void setColoring(int i, Coloring coloring) {
        DependencyManager.removeDependency(this, ((Coloring) colorings.elementAt(i)));
        ((Coloring) colorings.elementAt(i)).dispose();
        colorings.setElementAt(coloring, i);
        DependencyManager.setDependency(this, coloring);
    }

    public void setFace(int i, int[] face) {
        faces.setElementAt(face, i);
        calculateEdges();
    }

    public void addVertex(Expression expr) {
        vertices.addElement(expr);
        DependencyManager.setDependency(this, expr);
    }

    public void addFace(int[] face, Coloring coloring) {
        faces.addElement(face);
        colorings.addElement(coloring);
        DependencyManager.setDependency(this, coloring);
        calculateEdges();
    }

    public void removeVertex(int i) {
        ((Expression) vertices.elementAt(i)).dispose();
        vertices.removeElementAt(i);
    }

    public void removeFace(int i) {
        faces.removeElementAt(i);
        DependencyManager.removeDependency(this, ((Coloring) colorings.elementAt(i)));
        ((Coloring) colorings.elementAt(i)).dispose();
        colorings.removeElementAt(i);
        calculateEdges();
    }

    public PlotOutput output() {
        return output_;
    }
    
    public void dispose() {
        super.dispose();
        DependencyManager.remove(this);
        java.util.Enumeration exprs = vertices.elements();
        while (exprs.hasMoreElements())
            ((Expression) exprs.nextElement()).dispose();
        java.util.Enumeration cols = colorings.elements();
        while (cols.hasMoreElements()) {
            Coloring c = (Coloring) cols.nextElement();
            DependencyManager.removeDependency(this, c);
            c.dispose();
        }
    }



    // ****************************** FILE I/O ****************************** //
    String[] colorings__, vertList__;
    
    public PlotPolyhedron(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        this.vertices = new java.util.Vector();
        this.faces = new java.util.Vector();
        this.colorings = new java.util.Vector();
        FileProperties props = parser.parseProperties(tok);
        vertList__ = parser.parseExpressionList(props.get("vertices"));
        dimension = parser.currDimension();
        TokenString str = parser.parseList(props.get("faces"));
        for (int i = 0; i < str.size(); ++i) {
            double[] numlist = parser.parseNumberList(str.tokenAt(i));
            int[] intlist = new int[numlist.length];
            for (int j = 0; j < intlist.length; ++j)
                intlist[j] = (int) numlist[j];
            faces.addElement(intlist);
        }
        title = parser.parseWord(props.get("title"));
        colorings__ = parser.parseObjectList(props.get("colors"));
        if (faces.size() != colorings__.length)
            parser.error("polyhedron must have the same number of faces and colorings");
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
        for (int i = 0; i < colorings__.length; ++i)
            colorings.addElement((Coloring) parser.getObject(colorings__[i]));
        DependencyManager.addDependencies(this, colorings.elements());
        colorings__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        Expression[] vertList = parser.recognizeExpressionList(vertList__);
        for (int i = 0; i < vertList.length; ++i)
            vertices.addElement(vertList[i]);
        DependencyManager.addDependencies(this, vertList);
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        parser.loadExprs(colorings.elements());
        parser.popEnvironment();
    }
    
    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
        calculateEdges();
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("colors", generator.generateObjectList(colorings.elements()));
        props.add("vertices", generator.generateExpressionList(vertices.elements()));
        TokenString str = new TokenString();
        for (int i = 0; i < faces.size(); ++i)
            str.add(generator.generateNumberList((int[]) faces.elementAt(i)));
        props.add("faces", generator.generateList(str));
        props.add("title", generator.generateWord(title));
        return generator.generateProperties(props);
    }



    // ******* Things for hooks into Expressions ******** //

    private class Edge {
        public int v1, v2;
        public Set adjacentFaces = null;
        public int[] adjacentFacesArray;
        public Edge(int a, int b) {v1 = a; v2 = b;}
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof Edge) {
                int ev1 = ((Edge) obj).v1, ev2 = ((Edge) obj).v2;
                return (ev1 == v1 && ev2 == v2) || (ev1 == v2 && ev2 == v1);
            }
            return false;
        }
        public int hashCode() {
            return (v1+1) * (v2+1);
        }
    }

    public int currentVertex() {
        return currVertex_;
    }
    public int[] currentEdge() {
        return new int[]{currEdge_.v1, currEdge_.v2};
    }
    public int currentFace() {
        return currFace_;
    }
    public int numFaces() {
        return faces.size();
    }
    public int numVertices() {
        return vertices.size();
    }
    public int numEdges() {
        return edges.size();
    }
    public int numFaceVertices(int i) {
        if (i < 0) i = 0;
        if (i >= faces.size()) i = faces.size()-1;
        return ((int[]) faces.elementAt(i)).length;
    }
    public ValueVector vertex(int i) {
        if (i < 0) i = 0;
        if (i >= vertices.size()) i = vertices.size()-1;
        return (ValueVector) ((Expression) vertices.elementAt(i)).evaluate();
    }
    public ValueList vertexList() {
        Value[] vs = new Value[vertices.size()];
        for (int i = 0; i < vs.length; ++i)
            vs[i] = ((Expression) vertices.elementAt(i)).evaluate();
        return new ValueList(vs, new TypeVector(dimension));
    }
    public ValueVector faceVertex(int face, int vertex) {
        if (face < 0) face = 0;
        if (face >= faces.size()) face = faces.size()-1;
        int[] verts = (int[]) faces.elementAt(face);
        if (vertex < 0) vertex = 0;
        if (vertex >= verts.length) vertex = verts.length-1;
        return (ValueVector) ((Expression) vertices.elementAt(verts[vertex])).evaluate();
    }
    public ValueList vertexLink(int vertex) {
        if (vertex < 0) vertex = 0;
        if (vertex >= vertices.size()) vertex = vertices.size()-1;
        int v = 0;
        while (edges.get(new Edge(vertex, v)) == null && v < vertices.size())
            ++v;
        if (v == vertices.size())
            return new ValueList(new Value[0], new TypeVector(dimension));
        int f = ((Edge) edges.get(new Edge(vertex, v))).adjacentFacesArray[0];
        int startf = f;
        java.util.Vector link = new java.util.Vector(8);
        do {
            link.addElement(new ValueScalar(v+1));
            int[] face = (int[]) faces.elementAt(f);
            int i = 0;
            while ((face[i] == v || face[i] == vertex ||
                    edges.get(new Edge(vertex, face[i])) == null)
                   && i < face.length)
                ++i;
            v = face[i];
            Edge e = (Edge) edges.get(new Edge(vertex, v));
            if (e.adjacentFacesArray[0] == f) {
                if (e.adjacentFaces.size() == 1)
                    break;
                i = 1;
            }
            else i = 0;
            f = e.adjacentFacesArray[i];
        } while (f != startf);
        Value[] vals = new Value[link.size()];
        link.copyInto(vals);
        return new ValueList(vals, mathbuild.MB.TYPE_SCALAR);
    }
    public boolean edgeIsCritical(int[] edgeVertexNums, double tol) {
        if (this.dimension != 3)
            return false;
        Edge e = (Edge) edges.get(new Edge(edgeVertexNums[0], edgeVertexNums[1]));
        if (e == null || e.adjacentFaces.size() != 2)
            return false;
        int face1Index, face2Index;
        java.util.Enumeration adjacentFaces = e.adjacentFaces.elements();
        face1Index = ((Integer) adjacentFaces.nextElement()).intValue();
        face2Index = ((Integer) adjacentFaces.nextElement()).intValue();
        int[] face1, face2;
        face1 = (int[]) faces.elementAt(face1Index);
        face2 = (int[]) faces.elementAt(face2Index);
        int i1 = 0, i2 = 0;
        while ((face1[i1] == e.v1 || face1[i1] == e.v2) && i1 < face1.length) ++i1;
        while ((face2[i2] == e.v1 || face2[i2] == e.v2) && i2 < face2.length) ++i2;
        Expression ev1E = (Expression) vertices.elementAt(e.v1); // ev1, ev2 are vertices of edge
        Expression ev2E = (Expression) vertices.elementAt(e.v2);
        Expression fv1E = (Expression) vertices.elementAt(face1[i1]); // fv1, fv2 verts on faces 1,2
        Expression fv2E = (Expression) vertices.elementAt(face2[i2]);
        ValueVector ev1 = (ValueVector) ev1E.evaluate();
        ValueVector ev2 = (ValueVector) ev2E.evaluate();
        ValueVector fv1 = (ValueVector) fv1E.evaluate();
        ValueVector fv2 = (ValueVector) fv2E.evaluate();
        double z = ((ValueScalar) ev1.component(2)).num();
        if (Math.abs(z - ((ValueScalar) ev2.component(2)).num()) > tol) return false;
        double z1 = ((ValueScalar) fv1.component(2)).num();
        double z2 = ((ValueScalar) fv2.component(2)).num();
        return (z1 > z && z2 > z) || (z1 < z && z2 < z);
    }
    public int criticalVertexIndex(int vertNum) {
        if (vertNum < 0) vertNum = 0;
        if (vertNum >= vertices.size()) vertNum = vertices.size() - 1;
        if (vertices.size() == 0)
            return 0;
        double z = ((ValueScalar) ((ValueVector) ((Expression)
                            vertices.elementAt(vertNum)).evaluate()).component(2)).num();
        Set adjacentFaceEdges = new Set(); // edges on adjacent faces not containing the vertex
        for (int f = 0; f < faces.size(); ++f) {
            int[] face = (int[]) faces.elementAt(f);
            boolean vertexInFace = false;
            for (int v = 0; v < face.length; ++v) {
                if (face[v] == vertNum) {
                    vertexInFace = true;
                    break;
                }
            }
            if (vertexInFace) {
                for (int v = 0; v < face.length; ++v) {
                    int vv = (v + 1) % face.length;
                    if (face[v] == face[vv] ||
                        face[v] == vertNum  ||
                        face[vv] == vertNum)
                        continue;
                    adjacentFaceEdges.add(new Edge(face[v], face[vv]));
                }
            }
        }
        java.util.Enumeration es = adjacentFaceEdges.elements();
        int numEdgesCrossing = 0;
        while (es.hasMoreElements()) {
            Edge e = (Edge) es.nextElement();
            double z1 = ((ValueScalar) ((ValueVector) ((Expression)
                                vertices.elementAt(e.v1)).evaluate()).component(2)).num();
            double z2 = ((ValueScalar) ((ValueVector) ((Expression)
                                vertices.elementAt(e.v2)).evaluate()).component(2)).num();
            if ((z1 <= z && z < z2) || (z2 <= z && z < z1))
                ++numEdgesCrossing;
        }
        return 1 - numEdgesCrossing/2;
    }
    
}


