package demo.plot;

import mathbuild.value.*;

import demo.io.*;
import demo.gfx.*;
import demo.util.*;
import demo.depend.*;
import demo.exec.*;
import demo.gfx.drawable.PolygonFramedTriangle;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.expr.ste.STEInterval;

/**
 * Plot for surfaces.
 */
public class PlotSurface3D extends PlotExpression implements Dependable, FileObject {

    private PlotSurfaceOutput output_ = new PlotSurfaceOutput();

    private boolean interpolateColors_ = true;
    private boolean useVertexNormals_ = true;

    private static final DemoColor BLACK = new DemoColor(0,0,0,1);

    private final LightingVector SINGULAR_FLAG = new NullLightingVector();

    /** 
     * Creates a new PlotSurface that is empty, whose expression and coloring 
     * will be proveded later.
     */
    public 
    PlotSurface3D(int dimension) {
        super(3);
            if (dimension != 3) throw new RuntimeException("dimension of Surface must be 3");
            this .expression = null;
            this .coloring = null;
        }

    /**
     * Creates a new PlotSurface3D with the given coloring and no expression.
     * The expression should be supplied later.
     */
    public PlotSurface3D(Coloring coloring, int dimension) {
        super(3);
        if (dimension != 3) throw new RuntimeException("dimension of Surface must be 3");
        this.expression = null;
        this.setColoring(coloring);
    }

    
    /** 
     * Creates a new PlotSurface whose parameterized expression is expression.
     * @param expression the expression representing the surface
     * @param coloring the coloring
     */
    public 
    PlotSurface3D( IntervalExpression expression, Coloring coloring, int dimension ) {
        super(3);
            if (dimension != 3) throw new RuntimeException("dimension of Surface must be 3");
            this.setExpression(expression);
            this.setColoring(coloring);
        }


    /**
     * Sets whether to interpolate colors from the coloring.
     * If true, colors will be associated with points, and polygons will be created
     * with a different color at each point. If false, colors are associated with
     * polygons, and each polygon will be created with one color.
     */
    public void setInterpolateColors(boolean b) {
        interpolateColors_ = b;
    }

    /**
     * Whether to interpolate colors from the coloring.
     * If true, colors will be associated with points, and polygons will be created
     * with a different color at each point. If false, colors are associated with
     * polygons, and each polygon will be created with one color.
     */
    public boolean interpolateColors() {
        return interpolateColors_;
    }

    /**
     * Sets whether to use vertex normal vectors, or triangle (polygon) normal vectors.
     * If true, normals will be associated with points, and polygons will be created
     * with a different normal at each point. If false, normals are associated with
     * polygons, and each polygon will be created with one normal (its own).
     */
    public void setUseVertexNormals(boolean b) {
        useVertexNormals_ = b;
    }

    /**
     * Whether to use vertex normal vectors, or triangle (polygon) normal vectors.
     * If true, normals will be associated with points, and polygons will be created
     * with a different normal at each point. If false, normals are associated with
     * polygons, and each polygon will be created with one normal (its own).
     */
    public boolean useVertexNormals() {
        return useVertexNormals_;
    }

    /** 
     * Sets the expression for the surface to the given expression.
     * If expression uses more than two intervals, just two of the
     * intervals will be used as the parameters.
     * @param expression the expression for the parameterized surface
     */
    public  void setExpression( IntervalExpression expression ) {
        super.setExpression(expression);
    }

    /**
     * Sets the coloring to the given coloring.
     * @param coloring the coloring to use
     */
    public  void setColoring( Coloring coloring ) {
        super.setColoring(coloring);
    }

    public  void calculatePlot() {
        if ( expression == null || coloring == null ) {
            output_.makeBufferPoints(0);
            output_.makeBufferColors(0);
            output_.makeBufferLightingVectors(0);
            output_.makeBufferDrawables();
            output_.setOutput();
            return;
        }
        calculatePoints();
        calculateColors();
        calculateVectors();
        calculatePolygons();
        output_.setOutput();
    }

    /**
     * Calculates the PointSortables for the surface.
     */
    private  void calculatePoints() {
        STEInterval u = this .expression .sortedIntervals()[0];
        STEInterval v = this .expression .sortedIntervals()[1];
        double uRes = u .resolution().number(), uMin = u .min().number(),
               uMax = u .max().number();
        int uResInt = (int) Math .round( uRes );
        double uIncr = (uMax - uMin) / (double) uResInt;
        output_.makeBufferPoints(uResInt + 1);
        double uVal = uMin;
        for ( int i = 0; i <= uResInt; i++ ) {
            u.setValue(uVal);
            double vMin = v .min().number(), vMax = v .max().number(),
                        vRes = v .resolution().number();
            int vResInt = (int) Math .round( vRes );
            double vIncr = (vMax - vMin) / (double) vResInt;
            PointSortable[] points = output_.makeBufferPoints(i, vResInt + 1);
            double vVal = vMin;
            for ( int j = 0; j <= vResInt; j++ ) {
                v.setValue(vVal);
                points[j] = new PointSortable( ((ValueVector) expression.calculate()).doubleVals(),
                                               6,extraZ_ );
                vVal += vIncr;
            }
            uVal += uIncr;
        }
    }

    /**
     * Calculates the colors for each polygon in the surface.
     */
    private  void calculateColors() {
        PointSortable[][] points = output_.bufferPoints();
        coloring .setCache();
        STEInterval u = this .expression .sortedIntervals()[0];
        STEInterval v = this .expression .sortedIntervals()[1];
        double uRes = u .resolution().number(), uMin = u .min().number(),
               uMax = u .max().number();
        int uResInt = (int) Math .round( uRes );
        double uIncr = (uMax - uMin) / (double) uResInt;
        int uArraySize = interpolateColors_ ? uResInt + 1 : uResInt;
        output_.makeBufferColors(uArraySize);
        double uVal = uMin;
        for ( int i = 0; i < uArraySize; i++ ) {
            u.setValue(uVal);
            double vMin = v .min().number(), vMax = v .max().number(),
                   vRes = v .resolution().number();
            int vResInt = (int) Math .round( vRes );
            double vIncr = (vMax - vMin) / (double) vResInt;
            int vArraySize = interpolateColors_ ? vResInt + 1 : vResInt;
            DemoColor[] colors = output_.makeBufferColors(i, vArraySize);
            double vVal = vMin;
            for ( int j = 0; j < vArraySize; j++ ) {
                v.setValue(vVal);
                currPoint_ = points[i][j];
                colors[j] = new DemoColor(coloring.calculate());
                vVal += vIncr;
            }
            uVal += uIncr;
        }
    }
    
    /**
     * Calculates the normal vectors for the surface based on points, which
     * were already calculated, and the expression.
     */
    private  void calculateVectors()
    {
        if (useVertexNormals_)
            calculateVertexVectors();
        else
            output_.makeBufferLightingVectors(0);
    }

    private void calculateVertexVectors() {
        double h = 1e-5;
        PointSortable[][] points = output_.bufferPoints();
        STEInterval u = this .expression .sortedIntervals()[0];
        STEInterval v = this .expression .sortedIntervals()[1];
        double uRes = u .resolution().number(), uMin = u .min().number(),
            uMax = u .max().number();
        int uResInt = (int) Math .round( uRes );
        double uIncr = (uMax - uMin) / (double) uResInt;
        output_.makeBufferLightingVectors(uResInt + 1);
        double uVal = uMin;
        double uh, vh;
        boolean uhSwitched, vhSwitched;
        int iSwitchUH = uResInt/2;
        uh = M.min(h, 0.1 * Math.abs(uIncr));
        uh = uMax > uMin ? uh : -uh;
        uhSwitched = false;
        for ( int i = 0; i <= uResInt; i++ ) {
            u.setValue(uVal);
            double vMin = v .min().number(), vMax = v .max().number(),
                vRes = v .resolution().number();
            int vResInt = (int) Math .round( vRes );
            double vIncr = (vMax - vMin) / (double) vResInt;
            LightingVector[] lvecs = output_.makeBufferLightingVectors(i, vResInt + 1);
            double vVal = vMin;
            int jSwitchVH = vResInt/2;
            vh = M.min(h, 0.1 * Math.abs(vIncr));
            vh = vMax > vMin ? vh : -vh;
            vhSwitched = false;
            for ( int j = 0; j <= vResInt; j++ ) {
                double[] p = M.point(points[i][j]), p1 = p, p2 = p;
                u.setValue(uVal+uh);
                v.setValue(vVal);
                p1 = M.point((ValueVector) expression.calculate());
                if (M.close(p1, p) || M.isNaN(p1)) {
                    // singular, or NaN
                    lvecs[j] = SINGULAR_FLAG;
                }
                else {
//                if (M.close(p1, p) || M.isNaN(p1)) {
                    // maybe expr was in polar coords, u was radius?
                    // try moving v a little also
                    // hopefully this will work. If not, too complicated so just give up
//                    for (int ntries = 0;
//                         (M.close(p1, p) || M.isNaN(p1)) && ntries < 4;
//                         ++ntries) {
//                        u.setValue(uVal + Math.random()*uh);
//                        v.setValue(vVal + Math.random()*vh);
//                        p1 = M.point((ValueVector) expression.calculate());
//                    }
//                }
                u.setValue(uVal);
                v.setValue(vVal+vh);
                p2 = M.point((ValueVector) expression.calculate());
                if (M.close(p2, p) || M.close(p2, p1) || M.isNaN(p2)) {
                    lvecs[j] = SINGULAR_FLAG;
                }
                else {
   //             if (M.close(p2, p) || M.close(p2, p1) || M.isNaN(p2)) {
//                    // try moving u a little also
//                    for (int ntries = 0;
//                         (M.close(p2, p) || M.close(p2, p1) || M.isNaN(p2)) && ntries < 4;
 //                        ++ntries) {
//                        u.setValue(uVal + Math.random()*uh);
//                        v.setValue(vVal + Math.random()*vh);
//                        p2 = M.point((ValueVector) expression.calculate());
//                    }
//                }
                    double[] normalvec = M.normalize(M.cross(M.sub(p1, p),
                                                             M.sub(p2, p)));
                    // make sure normal is oriented the right way (all outward or all inward)
                    if ((vhSwitched && !uhSwitched) || (uhSwitched && !vhSwitched))
                        normalvec = M.neg(normalvec);
                    lvecs[j] = new NormalVector(normalvec);
                }
                }
                vVal += vIncr;
                if (j == jSwitchVH) {
                    vh = -vh;
                    vhSwitched = true;
                }
            }
            uVal += uIncr;
            if (i == iSwitchUH) {
                uh = -uh;
                uhSwitched = true;
            }
        }
    }


    private final void getTriangleLightingVectors(LightingVector[] vecs, LinkedList lvecList) {
        LightingVector v1 = vecs[0],
                       v2 = vecs[1],
                       v3 = vecs[2];
        if (v1 == SINGULAR_FLAG) {
            if (v2 == SINGULAR_FLAG)
                v1 = v3;
            else if (v3 == SINGULAR_FLAG)
                v1 = v2;
            else {
                v1 = new NormalVector(M.normalize(M.add(v2.untransformedCoords,
                                                        v3.untransformedCoords)));
                lvecList.add(v1);
            }
        }
        if (v2 == SINGULAR_FLAG) {
            if (v3 == SINGULAR_FLAG)
                v2 = v1;
            else {
                v2 = new NormalVector(M.normalize(M.add(v1.untransformedCoords,
                                                        v3.untransformedCoords)));
                lvecList.add(v2);
            }
        }
        if (v3 == SINGULAR_FLAG && v1 != SINGULAR_FLAG && v2 != SINGULAR_FLAG) {
            v3 = new NormalVector(M.normalize(M.add(v1.untransformedCoords,
                                                    v2.untransformedCoords)));
            lvecList.add(v3);
        }
        vecs[0] = v1;
        vecs[1] = v2;
        vecs[2] = v3;
    }

    private PolygonFramedTriangle makeTriangle(int i1, int j1,
                                               int i2, int j2,
                                               int i3, int j3,
                                               PointSortable[][] points,
                                               DemoColor[][] colors,
                                               LightingVector[][] lvecs,
                                               LinkedList extraLVecs,
                                               DemoColor[] bufcolors,
                                               LightingVector[] bufvecs) {
        if (useVertexNormals_) {
            bufvecs[0] = lvecs[i1][j1];
            bufvecs[1] = lvecs[i2][j2];
            bufvecs[2] = lvecs[i3][j3];
            getTriangleLightingVectors(bufvecs, extraLVecs);
        }
        else {
            bufvecs[0] = bufvecs[1] = bufvecs[2] =
                new NormalVector(M.normalize(M.cross(M.sub(M.point(points[i1][j1]),
                                                           M.point(points[i3][j3])),
                                                     M.sub(M.point(points[i2][j2]),
                                                           M.point(points[i3][j3])))));
            extraLVecs.add(bufvecs[0]);
        }
        if (interpolateColors_) {
            bufcolors[0] = colors[i1][j1];
            bufcolors[1] = colors[i2][j2];
            bufcolors[2] = colors[i3][j3];
        }
        else {
            bufcolors[0] = bufcolors[1] = bufcolors[2] = colors[i1][j1];
        }
        return new PolygonFramedTriangle(points[i1][j1],
                                         points[i2][j2],
                                         points[i3][j3],
                                         bufcolors[0],
                                         bufcolors[1],
                                         bufcolors[2],
                                         bufvecs[0],
                                         bufvecs[1],
                                         bufvecs[2],
                                         BLACK );
    }
    
    /**
     * Calculates the polygons for the surface based on points and colors that were already calculated.
     */
    private  void calculatePolygons() {
        PointSortable[][] points = output_.bufferPoints();
        DemoColor[][] colors = output_.bufferColors();
        LinkedList polygonsList = output_.makeBufferDrawables();
        LightingVector[][] lvecs = output_.bufferLightingVectors();
        LightingVector[] bufvecs = new LightingVector[3];
        DemoColor[] bufcolors = new DemoColor[3];
        LinkedList extraLVecs = output_.makeBufferExtraLightingVectors();
        for ( int i = 0; i < points .length - 1; i++ ) {
            for ( int j = 0; j < points[i] .length - 1; j++ ) {
                if (colors[i][j].alpha > transparencyThreshold_) {
                    if ( j + 1 < points[i + 1] .length ) {
                        PolygonFramedTriangle triangle1 =
                                makeTriangle(i,   j,
                                             i+1, j+1,
                                             i,   j+1,
                                             points, colors, lvecs,
                                             extraLVecs, bufcolors, bufvecs);
                        polygonsList .add( triangle1 );
                        PolygonFramedTriangle triangle2 =
                                makeTriangle(i,   j,
                                             i+1, j,
                                             i+1, j+1,
                                             points, colors, lvecs,
                                             extraLVecs, bufcolors, bufvecs);
                        polygonsList .add( triangle2 );
                    }
                    else {
                        PolygonFramedTriangle triangle =
                                makeTriangle(i,   j,
                                             i+1, points[i+1].length-1,
                                             i,   j+1,
                                             points, colors, lvecs,
                                             extraLVecs, bufcolors, bufvecs);
                        polygonsList .add( triangle );
                    }
                }
            }
            if ( points[i] .length < points[i + 1] .length ) {
                if (colors[i][colors[i].length - 1].alpha > transparencyThreshold_) {
                    for ( int k = points[i] .length; k < points[i + 1] .length; k++ ) {
                        PolygonFramedTriangle triangle =
                                makeTriangle(i,   points[i].length-1,
                                             i+1, k-1,
                                             i+1, k,
                                             points, colors, lvecs,
                                             extraLVecs, bufcolors, bufvecs);
                        polygonsList .add( triangle );
                    }
                }
            }
        }
    }

    public PlotOutput output() {
        return output_;
    }
    
    public String title() {
        if (expression == null)
            return "Surface: unspecified";
        return "Surface: " + expression.definitionString();
    }
    


    // ****************************** FILE I/O ****************************** //

    public PlotSurface3D(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        if (props.contains("intrpl"))
            interpolateColors_ = parser.parseBoolean(props.get("intrpl"));
        else
            interpolateColors_ = false;
        if (props.contains("vrtnml"))
            useVertexNormals_ = parser.parseBoolean(props.get("vrtnml"));
        else
            useVertexNormals_ = false;
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        if (interpolateColors_)
            props.add("intrpl", generator.generateBoolean(interpolateColors_));
        if (useVertexNormals_)
            props.add("vrtnml", generator.generateBoolean(useVertexNormals_));
        return generator.generateProperties(props);        
    }

    
}




class PlotSurfaceOutput implements PlotOutput {
    private  PointSortable[][] bufPoints = null, outPoints = null;
    private  DemoColor[][] bufColors = null;
    private  LightingVector[][] bufLVecs = null, outLVecs = null;
    private  LinkedList bufDrawables = new LinkedList(), outDrawables = new LinkedList();
    private  LinkedList bufExtraLVecs = new LinkedList(), outExtraLVecs = new LinkedList();

    public  PointSortable[][] makeBufferPoints(int numarrays) {
        if (bufPoints == null || bufPoints.length != numarrays)
            bufPoints = new PointSortable[numarrays][];
        return bufPoints;
    }

    public PointSortable[] makeBufferPoints(int index, int numpts) {
        if (bufPoints[index] == null || bufPoints[index].length != numpts)
            bufPoints[index] = new PointSortable[numpts];
        return bufPoints[index];
    }

    public  DemoColor[][] makeBufferColors(int numarrays) {
        if (bufColors == null || bufColors.length != numarrays)
            bufColors = new DemoColor[numarrays][];
        return bufColors;
    }

    public DemoColor[] makeBufferColors(int index, int numpts) {
        if (bufColors[index] == null || bufColors[index].length != numpts)
            bufColors[index] = new DemoColor[numpts];
        return bufColors[index];
    }

    public  LightingVector[][] makeBufferLightingVectors(int numarrays) {
        if (bufLVecs == null || bufLVecs.length != numarrays)
            bufLVecs = new LightingVector[numarrays][];
        return bufLVecs;
    }

    public LightingVector[] makeBufferLightingVectors(int index, int numpts) {
        if (bufLVecs[index] == null || bufLVecs[index].length != numpts)
            bufLVecs[index] = new LightingVector[numpts];
        return bufLVecs[index];
    }
    
    public LinkedList makeBufferDrawables() {
        return bufDrawables;
    }

    public LinkedList makeBufferExtraLightingVectors() {
        return bufExtraLVecs;
    }

    public PointSortable[][] bufferPoints() { return bufPoints; }
    public DemoColor[][] bufferColors() { return bufColors; }
    public LightingVector[][] bufferLightingVectors() { return bufLVecs; }
    public LinkedList bufferDrawables() { return bufDrawables; }
    public LinkedList extraLightingVectors() { return bufExtraLVecs; }
        
    public  int numOutputPoints() {
        if ( outPoints == null ) {
            return 0;
        }
        int numPoints = 0;
        for ( int i = 0; i < outPoints .length; i++ ) {
            numPoints += outPoints[i] .length;
        }
        return numPoints;
    }

    public  int copyOutputPoints( PointSortable[] array, int startIndex ) {
        if ( outPoints != null ) {
            int nextIndex = startIndex;
            for ( int i = 0; i < outPoints .length; i++ ) {
                for ( int j = 0; j < outPoints[i] .length; j++ ) {
                    array[nextIndex] = outPoints[i][j];
                    nextIndex++;
                }
            }
            return nextIndex;
        }
        return startIndex;
    }

    public  int numOutputLightingVectors() {
        if ( outLVecs == null ) {
            return 0;
        }
        int n = 0;
        for ( int i = 0; i < outLVecs .length; i++ ) {
            n += outLVecs[i] .length;
        }
        return n + outExtraLVecs.size();
    }

    public  int copyOutputLightingVectors( LightingVector[] array, int startIndex ) {
        if ( outLVecs != null ) {
            int nextIndex = startIndex;
            for ( int i = 0; i < outLVecs .length; i++ ) {
                for ( int j = 0; j < outLVecs[i] .length; j++ ) {
                    array[nextIndex] = outLVecs[i][j];
                    nextIndex++;
                }
            }
            for (java.util.Enumeration vecs = outExtraLVecs.elements();
                 vecs.hasMoreElements();)
                array[nextIndex++] = (LightingVector) vecs.nextElement();
            return nextIndex;
        }
        return startIndex;
    }

    
    public  LinkedList outputDrawableObjects() {
        return outDrawables;
    }

    public void setOutput() {
        Exec.begin_nocancel();
        PointSortable[][] tmp = outPoints;
        outPoints = bufPoints;
        bufPoints = tmp;
        LightingVector[][] tmpv = outLVecs;
        outLVecs = bufLVecs;
        bufLVecs = tmpv;
        outDrawables = bufDrawables;
        bufDrawables = new LinkedList();
        outExtraLVecs = bufExtraLVecs;
        bufExtraLVecs = new LinkedList();
        Exec.end_nocancel();
    }
    
}




