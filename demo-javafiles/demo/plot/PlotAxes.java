package demo.plot;

import demo.io.*;
import demo.gfx.*;
import demo.depend.*;
import demo.gfx.drawable.*;
import demo.util.M;
import demo.util.DemoColor;

/**
 * This plot makes the axes. It produces 2 or 3 perpendicular 
 * lines, and text labels for the lines.
 *
 * @author deigen
 */
public class PlotAxes extends Plot implements FileObject {

    private  String[] labels;

    private PlotOutputArrays output_ = new PlotOutputArrays();

    /**
     * Makes a new PlotAxes with the given labels.
     * @param labels a 2 or 3 dimensional array holding the text to
     * display for the x, y, and z (if 3D) axes, in that order.
     */
    public  PlotAxes( String[] labels ) {
        super();

            this .labels = labels;
        }

    /**
     * @return a 2 or 3 dimensional array storing the labels for the x, y, and z (if 3D) axes
     */
    public  String[] labels() {
        return labels;
    }

    /**
     * @param labels a 2 or 3 dimensional array storing the labels for the x, y, and z (if 3D) axes
     */
    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public  void calculatePlot() {
        PointSortable[] points = output_.makeBufferPoints(labels.length + 1);
        LightingVector[] lightingVectors = output_.makeBufferLightingVectors(labels.length);
        demo.util.LinkedList drawableObjects = output_.makeBufferDrawables();
        PointSortable origin = new PointSortable( new double []{ 0, 0, 0 },
                                                  labels .length,
                                                  extraZ_);
        double[] moriginpt = M.point(0,0,0);
        DemoColor color = new DemoColor(1,1,1,1);
        for ( int i = 0; i < labels .length; i++ ) {
            double[] coords = new double [ labels .length ];
            for ( int c = 0; c < coords .length; c++ ) {
                coords[c] = 0;
            }
            coords[i] = 1;
            PointSortable pt = new PointSortable( coords, 2,extraZ_ );
            TangentVector tanvec = new TangentVector(M.normalize(M.sub(M.point(coords),
                                                                       moriginpt)));
            // TODO: use flat line
            PolygonLine line = new PolygonLine( origin, pt,
                                                color, color,
                                                tanvec, tanvec);
            drawableObjects .add( line );
            TextDrawable text = new TextDrawable( labels[i], pt, new DemoColor(1,1,1,1) );
            drawableObjects .add( text );
            points[i] = pt;
            lightingVectors[i] = tanvec;
        }
        points[labels .length] = origin;
        output_.setOutput();
    }


    public PlotOutput output() {
        return output_;
    }

    public  String title() {
        String s = "Axes: ";
        for (int i = 0; i < labels.length; ++i)
            s += labels[i] + (i < labels.length-1 ? ", " : "");
        return s;
    }



    // ****************************** FILE I/O ****************************** //

    public PlotAxes(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        labels = parser.parseWordList(props.get("labels"));
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
        props.add("labels", generator.generateWordList(labels));
        return generator.generateProperties(props);
    }


}


