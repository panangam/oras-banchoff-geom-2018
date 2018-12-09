package demo.coloring;

import mathbuild.value.*;

import demo.io.*;
import demo.expr.Expression;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/**
 * Evaluates an expression and returns a corresponding color in the hue spectrum.
 * Red is returned if the expression evaluates to an integer. Other colors
 * are returned depending on the fractional part of the value that the 
 * expression evaluates to.
 *
 * @author deigen
 */
public class ColoringExpression extends Coloring implements FileObject {

    // the expression that gets evaluated
    private  Expression expression = null;
    
    // the String definition of the expression
    private  String expressionDefinitionString;

    /**
     * If this constructor is used, the Expression of the
     * expression must be given later using setExpression(.)
     *
     * @param definition the String of the definition of the expression
     */
    public  ColoringExpression( String definition ) {
        super();

            this .expressionDefinitionString = definition;
        }
        
    /**
     * @param expression the Expression of the expression
     */
    public  ColoringExpression( Expression expression ) {
        super();

        setExpression(expression);
    }
        
    /**
     * @param definition the String definition of the expression
     * @param expression the expression
     */
    public ColoringExpression( String definition, Expression expression ) {
        super();

            this .expressionDefinitionString = definition;
            setExpression(expression);
        }
        
    /**
     * Sets the expression to the given expression.
     *
     * @param expression  the expression
     */
    public  void setExpression( Expression expression ) {
        if (this.expression != null)
            this.expression.dispose();
        this .expression = expression;
        DependencyManager.setDependency(this, expression);
        expressionDefinitionString = expression.definitionString();
    }
    
    /**
     * Sets the String of the expression's definition.
     *
     * @param definition the String of the definition
     */
    public void setExpressionDefinitionString( String definition ) {
        this.expressionDefinitionString = definition;
    }
    
    
    /**
     * @return the expression that gets evaluated by this ColoringExpression
     */
    public  Expression expression() {
        return expression;
    }
    
    /**
     * @return the definition String of the expression of this ColorignExpression
     */
    public  String expressionDefinitionString() {
        return expressionDefinitionString;
    }

    public  void setCache() {}

    public double[] calculate( ) {
        // evaluate the expression and return the corresponding color
        return colorOf( ((ValueScalar) expression.evaluate()).number() );
    }


    public  java.util .Enumeration childColorings() {
        return new java.util .Vector( 0 ) .elements();
    }
    
    protected void disposeInternal() {
        if (expression != null)
            expression.dispose();
    }

    public Object clone(mathbuild.Environment env) {
        if (expression == null)
            return new ColoringExpression(expressionDefinitionString);
        return new ColoringExpression((Expression) expression.clone(env));
    }



    // ************ FILE IO ************* //

    String expression__;
    
    public ColoringExpression(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        expression__ = parser.parseExpression(props.get("expr"));
    }

    public void loadFileBind(FileParser parser) {
    }

    public void loadFileExprs(FileParser parser) {
        setExpression(parser.recognizeExpression(expression__));
    }
    
    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("expr", generator.generateExpression(expression));
        return generator.generateProperties(props);
    }

    
}


