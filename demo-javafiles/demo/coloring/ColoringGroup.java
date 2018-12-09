package demo.coloring;

import demo.io.*;
import demo.depend.*;
import demo.util.U;

/**
 * A ColoringGroup puts together multiple layers of Colorings. It works a lot like
 * Photoshop layers. Colorings can be layered one on top of the other, and the 
 * opacity and mixing for each coloring can be set. The opacity works like a
 * standard alpha chanel. The "mixing" (or "blending") is a little different.
 * It mixes together the colors in layers in such a way that if a layer has 100%
 * mixing, it is mixed evenly with all the colors below it. For example, suppose we
 * have three layers, one of blue, one of red, and one of yellow. Then to get equal
 * weights of each, we could set the opacities at 33%, 50%, and 100%. However, this
 * may not create the desired effect if these colors are not constantly shown. What
 * we can do instead is make each of their mixing at 100%.
 *
 * @author deigen, scanon
 */
public class ColoringGroup extends Coloring implements Dependable, FileObject {

    // the colorings, stored in the order they are layered
    private  Coloring[] colorings;
    
    // the alpha values (opacities) corresponding to the colorings
    private  double[] alphaValues;
    
    // the mixing values corresponding to the colorings
    private  double[] mixValues;

    /**
     * @param colorings a Vector containing the colorings, in the order they are layered
     * @param alphaValues a Vector of Double, each in the range of 0..1, storing the
     *			alpha values (opacities) corresponding to the colorings
     * @param mixingValues a Vector of Double, each in the range of 0..1, storing the
     * 			mixing values corresponding to the colorings
     */
    public ColoringGroup( java.util .Vector colorings,
                            java.util .Vector alphaValues,
                            java.util .Vector mixingValues ) {
        super();

            if ( colorings .size() != alphaValues .size() ||
                    colorings .size() != mixingValues .size() ) {
                throw new RuntimeException( "*** ERROR: Number of colorings, alpha values, and mixing values must all be the same" );
            }
            this .colorings = new Coloring [ colorings .size() ];
            this .alphaValues = new double [ alphaValues .size() ];
            this .mixValues = new double [ mixingValues .size() ];
            for ( int i = 0; i < colorings .size(); i++ ) {
                this .colorings[i] = (Coloring) colorings .elementAt( i );
                this .alphaValues[i] = ((Double) alphaValues .elementAt( i )) .doubleValue();
                this .mixValues[i] = ((Double) mixingValues .elementAt( i )) .doubleValue();
            }
            DependencyManager.addDependencies(this, colorings.elements());
    }
        
    /**
     * @param colorings an array of Coloring containing the colorings, in the
     * 			order they are layered
     * @param alphaValues an array of double in the range of 0..1, storing the
     *			alpha values (opacities) corresponding to the colorings
     * @param mixingValues an array of double in the range of 0..1, storing the
     * 			mixing values corresponding to the colorings
     */
    public ColoringGroup( Coloring[] colorings, double[] alphaValues,
                            double[] mixingValues ) {
        super();

            if ( colorings .length != alphaValues .length ||
                    colorings .length != mixingValues .length ) {
                throw new RuntimeException( "*** ERROR: Number of colorings, alpha values, and mixing values must all be the same" );
            }
            this .colorings = colorings;
            this .alphaValues = alphaValues;
            this .mixValues = mixingValues;
            DependencyManager.addDependencies(this, colorings);
        }
        
    /**
     * @return an array storing the colorings in the order they are layered
     */
    public  Coloring[] colorings() {
        return colorings;
    }
    
    /**
     * @return an array of double in the range of 0..1 storing the alpha
     * 			values (opacities) of the layers
     */
    public  double[] alphaValues() {
        return alphaValues;
    }
    
    /**
     * @return an array of double in the range of 0..1 storing the 
     *			mixing values of the layers
     */
    public  double[] mixValues() {
        return mixValues;
    }


    public  void setCache() {
        // recur on child colorings
        java.util .Enumeration children = childColorings();
        while ( children .hasMoreElements() ) {
            ((Coloring) children .nextElement()) .setCache();
        }
    }

    public double[] calculate() {
        double[] unnormalizedResult = calculate( 0 );
        return new double []{
            unnormalizedResult[0] / unnormalizedResult[4],
            unnormalizedResult[1] / unnormalizedResult[4],
            unnormalizedResult[2] / unnormalizedResult[4],
            unnormalizedResult[3] / unnormalizedResult[4]
        };
    }
    
    // the method that actually does the recursive calculation
    private double[] calculate( int index ) {
        if ( index >= colorings .length )
            return new double []{ 0, 0, 0, 0, 1 };
            
        double[] color = colorings[index] .calculate();
        if ( (alphaValues[index] * color[3] >= 1 && mixValues[index] <= 0) )
            return new double []{ color[0], color[1], color[2], color[3], 1 };
        double[] next = calculate( index + 1 );
        double nextCoefficient = 1 + (mixValues[index] - 1) * alphaValues[index] * color[3];
        double red = alphaValues[index] * color[0] + nextCoefficient * next[0];
        double green = alphaValues[index] * color[1] + nextCoefficient * next[1];
        double blue = alphaValues[index] * color[2] + nextCoefficient * next[2];
        double alpha = alphaValues[index] * color[3] + nextCoefficient * next[3];
        double normalizationFactor = alphaValues[index] * color[3] + nextCoefficient * next[4];
        return new double []{ red, green, blue, alpha, normalizationFactor };
    }

    public  java.util .Enumeration childColorings() {
        java.util .Vector children = new java.util .Vector( colorings .length );
        for ( int i = 0; i < colorings .length; i++ )
            children .addElement( colorings[i] );
        return children .elements();
    }

    protected void disposeInternal() {
    }

    public Object clone(mathbuild.Environment env) {
        Coloring[] cs = new Coloring[colorings.length];
        double[] as = new double[alphaValues.length];
        double[] ms = new double[mixValues.length];
        for (int i = 0; i < cs.length; ++i) {
            cs[i] = (Coloring) colorings[i].clone(env);
            as[i] = alphaValues[i];
            ms[i] = mixValues[i];
        }
        return new ColoringGroup(cs, as, ms);
    }
    

    // ************ FILE IO ************* //
    private String[] coloringsStrs__;
    
    public ColoringGroup(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        coloringsStrs__ = parser.parseObjectList(props.get("colors"));
        alphaValues = parser.parseNumberList(props.get("opacities"));
        mixValues = parser.parseNumberList(props.get("mixings"));
        if (coloringsStrs__.length != alphaValues.length || alphaValues.length != mixValues.length)
            parser.error("number of colorings, opacities and mixing values must be equal in coloring group");
    }

    public void loadFileBind(FileParser parser) {
        Object[] objs = parser.getObjects(coloringsStrs__);
        colorings = (Coloring[]) U.arraycopy(objs, new Coloring[objs.length], objs.length);
        DependencyManager.addDependencies(this, colorings);
    }

    public void loadFileExprs(FileParser parser) {
        parser.loadExprs(colorings);
    }
    
    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("colors", generator.generateObjectList(colorings));
        props.add("opacities", generator.generateNumberList(alphaValues));
        props.add("mixings", generator.generateNumberList(mixValues));
        return generator.generateProperties(props);
    }

    
}


