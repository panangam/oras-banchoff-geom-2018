package demo.coloring;

import demo.io.*;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * ColoringConstant stores a single color and returns it.
 *
 * @author deigen
 */
public class ColoringConstant extends Coloring implements FileObject {

    private  double[] color;
    
    /**
     * @param color a java.awt.Color containing the color information.
     *		the color is made opaque. Note that if the java.awt.Color
     * 		pointed to by color changes, the color in the
     *		ColoringConstant does *not* change.
     */
    public  ColoringConstant( java.awt .Color color ) {
        super();

            setColor( color );
        }
        
    /**
     * @param color a double[] containing the color information in
     *		[r,g,b,a]. Note that if the double[] pointed to by color
     * 		changes, the color in the ColoringConstant *does* change.
     */
    public  ColoringConstant( double[] color ) {
        super();

            setColor( color );
        }

    /**
     * sets the color to the parameter "color"
     *
     * @param color a java.awt.Color containing the color information, assumed to
     * 		be opaque. Note if the java.awt.Color pointed to by color changes,
     * 		then the color stored does *not* change.
     */
    public  void setColor( java.awt .Color color ) {
        setColor( new double []{ ((double) color .getRed()) / 255.0,
                                          ((double) color .getGreen()) / 255.0,
                                          ((double) color .getBlue()) / 255.0,
                                          1 } );
    }
    
    /**
     * sets the color to the parameter "color"
     * 
     * @param color a double[] containing the color information in [r,g,b,a].
     *		Note that if the double[] pointed to by color changes, the color
     * 		stored in the ColoringConstant *does* change.
     */
    public  void setColor( double[] color ) {
        this .color = color;
    }
    
    /**
     * @return the color of this ColoringConstant
     */
    public  double[] color() {
        return color;
    }

    public  void setCache() {}

    public double[] calculate() {
        // return the color
        return color;
    }

    public  java.util .Enumeration childColorings() {
        return new java.util .Vector( 0 ) .elements();
    }

    protected void disposeInternal() {
    }

    public Object clone(mathbuild.Environment env) {
        return new ColoringConstant(color);
    }
    

    // ************ FILE IO ************* //

    public ColoringConstant(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        color = parser.parseNumberList(props.get("color"));
        if (color.length != 4)
            parser.error("colors must have 4 components");
    }

    public void loadFileExprs(FileParser parser) {
    }

    public void loadFileBind(FileParser parser) {
    }

    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("color", generator.generateNumberList(color));
        return generator.generateProperties(props);
    }

    
}


