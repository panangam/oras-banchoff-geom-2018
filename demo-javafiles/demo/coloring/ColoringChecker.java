package demo.coloring;

import mathbuild.value.*;

import demo.expr.ste.STEInterval;
import demo.io.*;
import demo.depend.*;

/**
 * Generates a checker pattern of two colorings based on one or more intervals.
 *
 * @author deigen
 */
public class ColoringChecker extends Coloring implements FileObject {

    // an array containing the intervals
    private  STEInterval[] intervals;
    
    // the two colorings being used to color the checker pattern
    private  Coloring coloring1, coloring2;

    /**
     * @param intervals an array of the intervals being used to create 
     * 		the checker pattern
     * @param coloring1 one of the two colorings being used
     * @param coloring2 the other coloring of the two being used
     */
    public ColoringChecker( STEInterval[] intervals, Coloring coloring1,
                                Coloring coloring2 ) {
        super();

            this .intervals = intervals;
            this .coloring1 = coloring1;
            this .coloring2 = coloring2;
            for (int i = 0; i < intervals.length; ++i)
                DependencyManager.setDependency(this, intervals[i]);
            DependencyManager.setDependency(this, coloring1);
            DependencyManager.setDependency(this, coloring2);
    }


    /**
     * returns the intervals being used
     * @return an array containing the intervals being used
     */
    public  STEInterval[] intervals() {
        return intervals;
    }

    /**
     * @return one of the two colorings being used
     */
    public  Coloring coloring1() {
        return coloring1;
    }

    /**
     * @return the other of the two colorings
     */
    public  Coloring coloring2() {
        return coloring2;
    }

    public  void setCache() {}

    public double[] calculate() {
        // find out which coloring we should use for this part of the checker pattern
        double sum = 0;
        for ( int i = 0; i < intervals .length; i++ ) {
            STEInterval interval = intervals[i];
            sum += interval .resolution() .number() * ( ((ValueScalar)interval .value()).number() - interval .min().number() ) / (interval .max().number() - interval .min().number());
        }
        // which coloring to use depends on the sum mod 2
        if ( Math .round( sum ) % 2 == 0 ) {
            return coloring1 .calculate();
        }
        else {
            return coloring2 .calculate();
        }
    }

    // returns an enumeration of all colorings that the coloring is dependent on.
    public  java.util .Enumeration childColorings() {
        java.util .Vector temp = new java.util .Vector( 2 );
        temp .addElement( coloring1 );
        temp .addElement( coloring2 );
        return temp .elements();
    }

    protected void disposeInternal() {
    }

    public Object clone(mathbuild.Environment env) {
        return new ColoringChecker(intervals,
                                   (Coloring) coloring1.clone(env),
                                   (Coloring) coloring2.clone(env));
    }
    


    // ************ FILE IO ************* //
    private String coloring1Str__, coloring2Str__;
    private TokenString intervalStrs__;
    
    public ColoringChecker(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        intervalStrs__ = parser.parseList(props.get("intervals"));
        coloring1Str__ = parser.parseObject(props.get("color1"));
        coloring2Str__ = parser.parseObject(props.get("color2"));
    }

    public void loadFileBind(FileParser parser) {
        coloring1 = (Coloring) parser.getObject(coloring1Str__);
        coloring2 = (Coloring) parser.getObject(coloring2Str__);
        DependencyManager.setDependency(this, coloring1);
        DependencyManager.setDependency(this, coloring2);
    }

    public void loadFileExprs(FileParser parser) {
        TokenString strs = intervalStrs__;
        this.intervals = new STEInterval[strs.size()];
        for (int i = 0; i < strs.size(); ++i) {
            this.intervals[i] = (STEInterval) parser.currEnvLookup(strs.tokenAt(i));
            DependencyManager.setDependency(this, this.intervals[i]);
        }
        parser.loadExprs(coloring1);
        parser.loadExprs(coloring2);
    }

    public void loadFileFinish(FileParser parser) {
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        TokenString intervalsStr = new TokenString();
        for (int i = 0; i < intervals.length; ++i)
            intervalsStr.add(generator.generateWord(intervals[i].name()));
        props.add("intervals", generator.generateList(intervalsStr));
        props.add("color1", generator.generateObject(coloring1));
        props.add("color2", generator.generateObject(coloring2));
        return generator.generateProperties(props);
    }
}


