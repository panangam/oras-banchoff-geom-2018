package demo.coloring;

import mathbuild.value.*;

import demo.io.*;
import demo.util.U;
import demo.util.Set;
import demo.expr.Expression;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * Evaluates an expression and returns a color based on the value of the expression.
 * The color returned is the color corresponding to the value of the expression in
 * a gradient between multiple colors. For example, let the expression be "x".
 * Also, suppose we set the gradient "pure colors" to be red, blue, and yellow, and
 * the "pure color values" corresponding to these pure colors are -4, 2, and 5, 
 * respectively. Then if x <= -4, the color will be red. If x is between -4 and 2,
 * the color will be a mixture of red and blue. If x is between 2 and 5, the color
 * will be a mixture of blue and yellow. And if x >= 5, the color will be yellow.
 *
 * @author deigen
 */
public class ColoringGradient extends Coloring implements Dependable, FileObject {

    // the expression
    private  Expression expression = null;
    
    // the pure color expressions
    private  Expression[] pureColorExpressions = null;
    
    // this array contains the pure color values to use
    // instead of calculating the pure color expressions over and over,
    // the values are cached in this array.
    private  double[] pureColorValues;
    
    // the colorings
    private  Coloring[] colorings;
    

        
    /**
     * @param expression the expression
     * @param pureColorExpressions an array of Expression containing
     * 			the pure color expressions
     * @param colorings an array oc Colorings containing the Colorings 
     * 			corresponding to the pure color values.
     */
    public ColoringGradient( Expression expression, Expression[] pureColorExpressions,
                                Coloring[] colorings ) {
        super();

            this .expression = expression;
            this .pureColorExpressions = pureColorExpressions;
            this .colorings = colorings;
            DependencyManager.setDependency(this, expression);
            DependencyManager.addDependencies(this, pureColorExpressions);
            DependencyManager.addDependencies(this, colorings);
    }
        
        
    /**
     * Sets the expression to the given expression.
     *
     * @param expression the expression
     */
    public void setExpression( Expression expression ) {
        if (this.expression != null)
            this.expression.dispose();
        this .expression = expression;
        DependencyManager.setDependency(this, expression);
    }
    
    /**
     * Sets the pure color value expressions to the given expressions.
     *
     * @param expressions an array of Expression containing the
     * 			 the pure color expressions
     */
    public void setPureColorExpressions( Expression[] expressions ) {
        if (pureColorExpressions != null)
            for (int i = 0; i < pureColorExpressions.length; ++i)
                pureColorExpressions[i].dispose();
        this .pureColorExpressions = expressions;
        DependencyManager.addDependencies(this, expressions);
    }
    
    /**
     * @return the definition String of the expression
     */
    public  String expressionDefinitionString() {
        return expression.definitionString();
    }
    
    /**
     * @return an array of String containing the definition Strings of
     * 			the pure color expressions
     */
    public  String[] pureColorExpressionDefinitions() {
        String[] array = new String[pureColorExpressions.length];
        for (int i = 0; i < array.length; ++i)
            array[i] = pureColorExpressions[i].definitionString();
        return array;
    }
    
    /**
     * @return the expression
     */
    public  Expression expression() {
        return expression;
    }
    
    /**
     * @return an array of Expression containing the pure color expressions
     */
    public  Expression[] pureColorExpressions() {
        return pureColorExpressions;
    }
    
    /**
     * @return an array of Coloring containing the colorings
     */
    public  Coloring[] colorings() {
        return colorings;
    }
    
    /**
     * Returns the pure color values being used.
     * @return the pure color values being used
     */
    public  double[] pureColorValues() {
        return pureColorValues;
    }
    
    
    // a flag to tell whether the pure color values are stored and up-to-date
    private  boolean cached = false;

    public  void setCache() {
        if ( ! cached ) {
            // calculate and store the pure color values
            this .pureColorValues = new double [ pureColorExpressions .length ];
            for ( int i = 0; i < pureColorExpressions .length; i++ ) {
                this .pureColorValues[i] = ((ValueScalar) pureColorExpressions[i].evaluate()).number();
            }
            // sort the values (and everything else)
            sort();
            // recur on child colorings
            java.util .Enumeration children = childColorings();
            while ( children .hasMoreElements() ) {
                ((Coloring) children .nextElement()) .setCache();
            }
            this .cached = true;
        }
    }
    
    /**
     * Sorts the pure color values and colorings in
     * increasing order of pure color value.
     */
    private  void sort() {
        sort( 0, colorings .length - 1 );
    }
    
    // quicksort method to perform the sort
    private  void sort( int a, int b ) {
        if ( a >= b )
            return ;
        // bit shift makes number effectively random
        int pivotIndex = (a + b) >> 1;
        double pivot = pureColorValues[pivotIndex];
        // swap
        pureColorValues[pivotIndex] = pureColorValues[b];
        pureColorValues[b] = pivot;
        // swap expressions
        Expression pivotNode = pureColorExpressions[pivotIndex];
        pureColorExpressions[pivotIndex] = pureColorExpressions[b];
        pureColorExpressions[b] = pivotNode;
        // swap colorings
        Coloring pivotColoring = colorings[pivotIndex];
        colorings[pivotIndex] = colorings[b];
        colorings[b] = pivotColoring;
        int l = a;
        int r = b - 1;
        while ( l <= r ) {
            while ( (l <= r) && (pureColorValues[l] <= pivot) )
                l++;
            while ( (r >= l) && (pureColorValues[r] >= pivot) )
                r--;
            if ( l < r ) {
                // swap values
                double temp = pureColorValues[l];
                pureColorValues[l] = pureColorValues[r];
                pureColorValues[r] = temp;
                // swap expressions
                Expression tempNode = pureColorExpressions[l];
                pureColorExpressions[l] = pureColorExpressions[r];
                pureColorExpressions[r] = tempNode;
                // swap colorings
                Coloring tempColoring = colorings[l];
                colorings[l] = colorings[r];
                colorings[r] = tempColoring;
            }
        }
        // swap values
        double temp = pureColorValues[l];
        pureColorValues[l] = pureColorValues[b];
        pureColorValues[b] = temp;
        // swap expressions
        Expression tempNode = pureColorExpressions[l];
        pureColorExpressions[l] = pureColorExpressions[b];
        pureColorExpressions[b] = tempNode;
        // swap colorings
        Coloring tempColoring = colorings[l];
        colorings[l] = colorings[b];
        colorings[b] = tempColoring;
        sort( a, l - 1 );
        sort( l + 1, b );
    }

    public double[] calculate( ) {
        double value = ((ValueScalar) expression.evaluate()).number();
        if ( Double .isNaN( value ) )
            value = 0;
        if ( value <= pureColorValues[0] )
            return colorings[0] .calculate( );
        else if ( value >= pureColorValues[pureColorValues .length - 1] )
            return colorings[colorings .length - 1] .calculate( );
        else {
            // binary search for the value
            int lessIndex = 0, greaterIndex = pureColorValues .length - 1;
            int currIndex = pureColorValues .length / 2;
            while ( greaterIndex - lessIndex > 1 ) {
                if ( pureColorValues[currIndex] < value ) {
                    // the value at the current index is too small
                    lessIndex = currIndex;
                    currIndex = (greaterIndex + currIndex) / 2;
                }
                else if ( pureColorValues[currIndex] > value ) {
                    // the value at the current index is too big
                    greaterIndex = currIndex;
                    currIndex = (lessIndex + currIndex) / 2;
                }
                else {
                    // they're equal
                    return colorings[currIndex] .calculate( );
                }
            }
            // the value is between lessIndex and geraterIndex
            double[] lessColor = colorings[lessIndex] .calculate( );
            double[] greaterColor = colorings[greaterIndex] .calculate( );
            if ( lessColor == null || greaterColor == null ) {
                return lessColor == null ? greaterColor : lessColor;
            }
            double lessValue = pureColorValues[lessIndex],
                    greaterValue = pureColorValues[greaterIndex];
            double coefficientLess, coefficientGreater;
            // do a linear combination of the two colors on either side of the value
            if ( greaterValue == lessValue ) {
                coefficientLess = 0;
                coefficientGreater = 1;
            }
            else {
                coefficientLess = (greaterValue - value) / (greaterValue - lessValue);
                coefficientGreater = (value - lessValue) / (greaterValue - lessValue);
            }
            double red = coefficientLess * lessColor[0] + coefficientGreater * greaterColor[0];
            double green = coefficientLess * lessColor[1] + coefficientGreater * greaterColor[1];
            double blue = coefficientLess * lessColor[2] + coefficientGreater * greaterColor[2];
            double alpha = coefficientLess * lessColor[3] + coefficientGreater * greaterColor[3];
            return new double []{ red, green, blue, alpha };
        }
    }

    public void dependencyUpdateVal( Set updatingObjects ) {
        this .cached = false;
    }

    public void dependencyUpdateDef(Set updatingObjects) {
        dependencyUpdateVal(updatingObjects);
    }

    public  java.util .Enumeration childColorings() {
        java.util .Vector children = new java.util .Vector( colorings .length );
        for ( int i = 0; i < colorings .length; i++ ) {
            children .addElement( colorings[i] );
        }
        return children .elements();
    }
    
    protected void disposeInternal() {
        if (expression != null)
            expression.dispose();
        if (pureColorExpressions != null) {
            for (int i = 0; i < pureColorExpressions.length; ++i) {
                pureColorExpressions[i].dispose();
            }
        }
    }

    public Object clone(mathbuild.Environment env) {
        Expression e = (Expression) expression.clone(env);
        Expression[] pces = new Expression[pureColorExpressions.length];
        Coloring[] cs = new Coloring[colorings.length];
        for (int i = 0; i < pureColorExpressions.length; ++i)
            pces[i] = (Expression) pureColorExpressions[i].clone(env);
        for (int i = 0; i < colorings.length; ++i)
            cs[i] = (Coloring) colorings[i].clone(env);
        return new ColoringGradient(e, pces, cs);
    }
    
    

    // ************ FILE IO ************* //
    private String[] coloringStrs__, pureColorExpressions__;
    private String expression__;
        
    public ColoringGradient(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        expression__ = parser.parseExpression(props.get("expr"));
        pureColorExpressions__ = parser.parseExpressionList(props.get("values"));
        coloringStrs__ = parser.parseObjectList(props.get("colors"));
        if (coloringStrs__.length != pureColorExpressions__.length)
            parser.error("Coloring gradient must have the same number of values as colors");
    }

    public void loadFileBind(FileParser parser) {
        Object[] objs = parser.getObjects(coloringStrs__);
        colorings = (Coloring[]) U.arraycopy(objs, new Coloring[objs.length], objs.length);
        DependencyManager.addDependencies(this, colorings);
    }

    public void loadFileExprs(FileParser parser) {
        expression = parser.recognizeExpression(expression__);
        pureColorExpressions = parser.recognizeExpressionList(pureColorExpressions__);
        DependencyManager.setDependency(this, expression);
        DependencyManager.addDependencies(this, pureColorExpressions);
        parser.loadExprs(colorings);
    }
    
    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("expr", generator.generateExpression(expression));
        props.add("values", generator.generateExpressionList(pureColorExpressions));
        props.add("colors", generator.generateObjectList(colorings));
        return generator.generateProperties(props);
    }

    
}


