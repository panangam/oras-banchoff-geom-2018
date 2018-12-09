package demo.graph;

import demo.io.*;

import demo.gfx.Matrix4D;
import demo.util.M;

/**
 * A GraphGroup stores a group of Graphs, and their corresponding GraphCanvas3Ds.
 * A GraphGroup is used to transform (rotate) a group of graphs together.
 *
 * @author deigen
 */
public class GraphGroup extends Object implements FileObject {

    private java.util .Vector graphs = new java.util .Vector();

    private java.util .Vector canvases = new java.util .Vector();

    public GraphGroup() {}

    private boolean transformAll = false;


    private Matrix4D rotationPart(Matrix4D t) {
        // get rotation. Could peel out from matrix, but for
        // now just get basis of rot transf by applying to basis vecs
        final double[] v1 = M.vector(1,0,0);
        final double[] v2 = M.vector(0,1,0);
        final double[] v3 = M.vector(0,0,1);
        double[] tv1 = M.normalize(t.transform(v1));
        double[] tv2 = M.normalize(t.transform(v2));
        double[] tv3 = M.normalize(t.transform(v3));
        return new Matrix4D(tv1[0], tv2[0], tv3[0],  0,
                            tv1[1], tv2[1], tv3[1],  0,
                            tv1[2], tv2[2], tv3[2],  0,
                                 0,      0,      0,  1);
    }

    /**
     * Transforms all the graphs in the group by the given matrix.
     * @param m the transformation matrix
     * @param transformingGraph the graph that this transformation is coming from (if any)
     */
    public  void transform( Matrix4D m, Graph transformingGraph ) {
        if (transformAll) {
            for ( int i = 0; i < graphs .size(); i++ )
                ((Graph) graphs .elementAt( i )) .transform( m );
        }
        else {
            Matrix4D rot = rotationPart(m);
            for (int i = 0; i < graphs.size(); ++i) {
                Graph g = (Graph) graphs.elementAt(i);
                if (g == transformingGraph) {
                    g.transform(m);
                }
                else {
                    Matrix4D gRot = rotationPart(g.getTransformation());
                    g.setTransformation(g.getTransformation().multiplyOnRightBy(gRot.inverse().multiplyOnRightBy(rot.multiplyOnRightBy(gRot))));
                }
            }
        }
    }

    /**
     * Redraws all canvases in the group.
     */
    public  void redraw() {
        for ( int i = 0; i < canvases .size(); i++ ) {
            if (((Graph) graphs.elementAt(i)).isVisible())
                ((GraphCanvas) canvases .elementAt( i )) .redraw();
        }
    }

    /**
     * Tells all canvases to setSuspendedState(.). All canvases that are set to 
     * suspend while dragging or animating get suspended.
     */
    public  void suspendCanvases() {
        for ( int i = 0; i < canvases .size(); i++ ) {
            ((GraphCanvas3D) canvases .elementAt( i )) .setSuspendedState();
        }
    }

    /**
     * Tells all canvases to unsetSuspendedState(.). All canvases become unsuspended.
     */
    public  void unsuspendCanvases() {
        for ( int i = 0; i < canvases .size(); i++ ) {
            ((GraphCanvas3D) canvases .elementAt( i )) .unsetSuspendedState();
        }
    }

    /**
     * @return whether there are no canvases and no graphs in the group
     */
    public  boolean isEmpty() {
        return this .canvases .size() == 0 && this .graphs .size() == 0;
    }

    /**
     * @return whether the number of canvases and graphs are each less than 1
     */
    public  boolean containsOneItemOrLess() {
        return this .canvases .size() <= 1 && this .graphs .size() <= 1;
    }

    /**
     * Adds a graph to the group.
     * @param graph the graph to add to the group
     */
    public  void addGraph( Graph graph ) {
        if ( ! this .containsGraph( graph ) ) {
            this .graphs .addElement( graph );
        }
    }

    /**
     * Removes a graph from the group.
     * @param graph the graph to remove from the group
     */
    public  void removeGraph( Graph graph ) {
        for ( int i = 0; i < graphs .size(); i++ ) {
            if ( graphs .elementAt( i ) == graph ) {
                graphs .removeElementAt( i );
                return ;
            }
        }
    }

    /**
     * @param graph the graph to check
     * @return whether the given graph is in the group
     */
    public  boolean containsGraph( Graph graph ) {
        for ( int i = 0; i < graphs .size(); i++ ) {
            if ( graphs .elementAt( i ) == graph ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return whether all transformations are linked (as opposed to just rotations)
     */
    public  boolean transformAll() {
        return transformAll;
    }

    /**
     * sets whether all transformations are linked (as opposed to just rotations)
     */
    public void setTransformAll(boolean b) {
        transformAll = b;
    }

    /**
     * @return an enumeration of all Graphs in the group
     */
    public  java.util .Enumeration graphs() {
        return this .graphs .elements();
    }

    /**
     * Adds a canvas to the group.
     * @param canvas the canvas to add to the group
     */
    public  void addCanvas( GraphCanvas3D canvas ) {
        if ( ! this .containsCanvas( canvas ) ) {
            this .canvases .addElement( canvas );
            canvas .setGroup( this );
        }
    }

    /**
     * Removes a canvas from the group.
     * @param canvas the canvas to remove from the group
     */
    public  void removeCanvas( GraphCanvas3D canvas ) {
        for ( int i = 0; i < canvases .size(); i++ ) {
            if ( canvases .elementAt( i ) == canvas ) {
                canvases .removeElementAt( i );
                canvas .removeGroup();
                return ;
            }
        }
    }

    /**
     * @param canvas the canvas to check
     * @return whether the given canvas is in the group
     */
    public  boolean containsCanvas( GraphCanvas canvas ) {
        for ( int i = 0; i < canvases .size(); i++ ) {
            if ( canvases .elementAt( i ) == canvas ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return an enumeration of all GraphCanvas3Ds in the group
     */
    public  java.util .Enumeration canvases() {
        return this .canvases .elements();
    }





    // ****************************** FILE I/O ****************************** //
    String[] graphs__, canvases__;
    
    public GraphGroup(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        graphs__ = parser.parseObjectList(props.get("graphs"));
        canvases__ = parser.parseObjectList(props.get("canvases"));
        if (props.contains("rotonly"))
            transformAll = !parser.parseBoolean(props.get("rotonly"));
    }

    public void loadFileBind(FileParser parser) {
        for (int i = 0; i < graphs__.length; ++i)
            addGraph((Graph) parser.getObject(graphs__[i]));
        for (int i = 0; i < canvases__.length; ++i)
            addCanvas((GraphCanvas3D) parser.getObject(canvases__[i]));
        graphs__ = canvases__ = null;
    }

    public void loadFileExprs(FileParser parser) {
    }

    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("graphs", generator.generateObjectIDList(graphs.elements()));
        props.add("canvases", generator.generateObjectIDList(canvases.elements()));
        if (transformAll)
            props.add("rotonly", generator.generateBoolean(!transformAll));
        return generator.generateProperties(props);
    }
    

}


