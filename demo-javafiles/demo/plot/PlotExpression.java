package demo.plot;

import demo.io.*;
import demo.gfx.*;
import demo.coloring.Coloring;
import demo.expr.Expression;
import demo.expr.IntervalExpression;
import demo.depend.*;

/**
 * Abstract class for plots that calculate drawable objects based on a parameterized expression.
 * 
 * @author deigen
 */
public  abstract class PlotExpression extends Plot implements FileObject {

    protected  IntervalExpression expression = null;

    protected  Coloring coloring = null;

    private String plotTypeName_;

    protected Point currPoint_; // current point for table entry

    private int dimension_;
    
    public PlotExpression(int dimension) {
        dimension_ = dimension;
        currPoint_ = new Point(new double[dimension], 0);
        makeObjectTableEntry();
    }

    /**
     * Sets the expression being used for the parameterization.
     * Note: if this method is overridden by a subclass, super.setExpression(.) should be called
     * @param expression the expression
     */
    public void setExpression( IntervalExpression expression ) {
        if (this.expression != null)
            this.expression.dispose();
        DependencyManager.setDependency(this, expression);
        this.expression = expression;
    }

    public void makeObjectTableEntry() {
        plotEntry_.addMember("Point", this, "Point<"+dimension_+"> currPoint()");
    }
    
    /**
     * Sets the coloring being used for calculated colors of drawable objects.
     * Note: if this method is overridden by a subclass, super.setColoring(.) should be called
     * @param coloring the coloring
     */
    public void setColoring( Coloring coloring ) {
        if (this.coloring != null) {
            DependencyManager.removeDependency(this, this.coloring);
            this.coloring.dispose();
        }
        DependencyManager.setDependency(this, coloring);
        this.coloring = coloring;
    }

    /**
     * @return the definition string of the expression being used for parameterization
     */
    public  String definition() {
        if (expression == null)
            return "";
        return expression.definitionString();
    }

    /**
     * @return the IntervalExpression used for parameterization
     */
    public  IntervalExpression intervalExpression() {
        return expression;
    }

    /**
     * @return the coloring used for finding colors of drawable objects
     */
    public  Coloring coloring() {
        return coloring;
    }


    
    public void dispose() {
        super.dispose();
        if (expression != null)
            expression.dispose();
        if (coloring != null) {
            coloring.dispose();
        }
    }


    // for table entry
    public Point currPoint() {
        return currPoint_;
    }


    // ****************************** FILE I/O ****************************** //
    String coloring__, expr__;

    public PlotExpression(Token tok, FileParser parser) {
        super(parser.parseProperties(tok).get("super"), parser);
        FileProperties props = parser.parseProperties(tok);
        expr__ = parser.parseExpression(props.get("expr"));
        coloring__ = parser.parseObject(props.get("color"));
        dimension_ = parser.currDimension();
        currPoint_ = new Point(new double[dimension_], 0);
    }

    public void loadFileBind(FileParser parser) {
        super.loadFileBind(parser);
        setColoring((Coloring) parser.getObject(coloring__));
        coloring__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        super.loadFileExprs(parser);
        Expression expr = parser.recognizeExpression(expr__);
        if (expr == null)
            this.expression = null;
        else {
            setExpression(new IntervalExpression(expr));
            expr.dispose();
        }
        parser.pushEnvironment(parser.currEnvironment().append(this.expressionDefinitions()));
        parser.loadExprs(this.coloring);
        parser.popEnvironment();
    }

    public void loadFileFinish(FileParser parser) {
        super.loadFileFinish(parser);
    }    

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("super", super.saveFile(generator));
        props.add("expr", generator.generateExpression(expression));
        props.add("color", generator.generateObject(coloring));
        return generator.generateProperties(props);
    }
    

}


