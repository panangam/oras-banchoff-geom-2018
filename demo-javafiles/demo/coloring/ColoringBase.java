package demo.coloring;

import demo.io.*;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * The ColoringBase is a coloring that is put on the bottom of a ColoringGroup
 * to ensure that final color of the ColoringGroup is opaque. It has one child
 * coloring. The output color of a ColoringBase is the color returned by this
 * child color made opaque. That is, it is the color returned with the alpha
 * channel divided out.
 *
 * @author deigen
 */
public class ColoringBase extends Coloring implements FileObject {

    // the coloring whose colors we are making opaque
    private  Coloring coloring;

    /**
     * @param coloring the coloring that the ColoringBase will make opaque
     */
    public  ColoringBase( Coloring coloring ) {
        super();

            this .coloring = coloring;
            DependencyManager.setDependency(this, coloring);
    }

    /**
     * returns the child coloring
     *
     * @return the coloring whose colors are being made opaque.
     */
    public  Coloring coloring() {
        return coloring;
    }

    
    public  void setCache() {
        this .coloring .setCache();
    }

    public double[] calculate() {
        // get the color from the coloring
        return coloring.calculate();
/*
        double[] color = this .coloring .calculate();
        if ( color[3] != 1 ) {
            // make the color opaque
            if ( color[3] == 0 ) {
                // the color is completely transparent, so we have
                // no color information. Return a gray.
                return new double []{ .2, .2, .2, 1 };
            }
            else {
                return new double []{ color[0] / color[3],
                                          color[1] / color[3],
                                          color[2] / color[3],
                                          1 };
            }
        }
        return color;
 */
    }

    public  java.util .Enumeration childColorings() {
        java.util .Vector children = new java.util .Vector( 1 );
        children .addElement( coloring );
        return children .elements();
    }

    protected void disposeInternal() {
    }

    public Object clone(mathbuild.Environment env) {
        return new ColoringBase((Coloring) coloring.clone(env));
    }

    
    // ************ FILE IO ************* //

    private String coloringID__;

    public ColoringBase(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        coloringID__ = parser.parseObject(props.get("coloring"));
    }

    public void loadFileBind(FileParser parser) {
        coloring = (Coloring) parser.getObject(coloringID__);
        DependencyManager.setDependency(this, coloring);
        coloringID__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        parser.loadExprs(coloring);
    }

    public void loadFileFinish(FileParser parser) {

    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("coloring", generator.generateObject(coloring));
        return props.generate();
    }

}


