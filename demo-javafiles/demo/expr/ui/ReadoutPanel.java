package demo.expr.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.depend.*;
import demo.io.*;
import demo.ui.*;
import demo.exec.*;
import demo.util.Set;
import demo.expr.Expression;
import demo.Demo;
import demo.ControlsFrame;

public class ReadoutPanel extends Panel implements Dependable, FileObject, ActionListener, ItemListener {

    private java.awt .TextField expField = new java.awt .TextField( "", 20 ) ,
                                 valueField = new java.awt .TextField( "", 30 );

    private  RemoveButton removeBtn = new RemoveButton();

    private  java.awt.Checkbox smartFormatCheckbox = new java.awt.Checkbox("SmartFormat");
    private  java.awt.Checkbox autoUpdateCheckbox = new java.awt.Checkbox("Auto Update");

    private  boolean autoUpdate_ = true, updated_ = false, smartFormat_ = true;

    private  Expression expression = null;
    private  String expressionString = "";

    private  Demo demo;
    private  ControlsFrame controls;

    private DependencyNode myDependencyNode_ = new DependencyNode(this);

    public  ReadoutPanel( Demo demo, ControlsFrame controls ) {
        super();

            this .demo = demo;
            this.controls = controls;
            init();
    }

    public void setControls(ControlsFrame controls) {
        this.controls = controls;
        Component[] comps = getComponents();
        boolean containsRmBtn = false;
        for (int i = 0; i < comps.length; ++i)
            if (comps[i] == removeBtn)
                containsRmBtn = true;
        if (!containsRmBtn)
            this.add(removeBtn);
    }

    private void init() {
        this.expField.addActionListener(this);
        removeBtn.addActionListener(this);
        autoUpdateCheckbox.addItemListener(this);
        smartFormatCheckbox.addItemListener(this);
        autoUpdateCheckbox.setState(autoUpdate_);
        smartFormatCheckbox.setState(smartFormat_);
        setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
        add( this .expField );
        add( new java.awt .Label( "=" ) );
        java.awt .Panel eastPanel = new java.awt .Panel();
        eastPanel .setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
        eastPanel .add( this .valueField );
        eastPanel .add( this .smartFormatCheckbox );
        eastPanel .add( this .autoUpdateCheckbox );
        this .valueField .setEditable(false);
        add( eastPanel );
        if (controls != null)
            this.add(removeBtn);
    }
        
    public String expressionString() {
        return expressionString;
    }
    
    public void setExpression( String exprDefString ) {
        this.expressionString = exprDefString;
        expField.setText(expressionString);
        if (exprDefString.equals("")) {
            this.expression = null;
        }
        else {
            if (expression != null)
                this.expression.dispose();
            this.expression = demo.recognizeExpression(expressionString);
            if (expression != null) {
                DependencyManager.setDependency(this, this.expression);
                this.expression.setTypeChangeAllowed(true);
            }
        }
        readout();
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event .getSource() == this .expField ) {
            parseExpression();
            readout();
        }
        if ( event .getSource() == this .removeBtn ) {
            if (expression != null)
                expression.dispose();
            DependencyManager.remove(this);
            controls.removeReadout(this);
        }
    }


    public void itemStateChanged( ItemEvent event ) {
        if (event.getSource() == this.autoUpdateCheckbox) {
            autoUpdate_ = autoUpdateCheckbox.getState();
            if (!updated_)
                readout();
        }
        if (event.getSource() == this.smartFormatCheckbox) {
            smartFormat_ = smartFormatCheckbox.getState();
            readout();
        }
    }
    
    
    private void parseExpression() {
        expressionString = expField.getText();
        Exec.run(new ExecCallback() {
            public void invoke() {
                if (expression != null)
                    expression.dispose();
                expression = demo.recognizeExpression(expressionString);
                if (expression != null) {
                    DependencyManager.setDependency(ReadoutPanel.this, expression);
                    expression.setTypeChangeAllowed(true);
                }
            }
        });
    }

    private  void readout() {
        Exec.run(new ExecCallback() {
            public void invoke() {
                if (expression == null)
                    valueField.setText("");
                else
                    valueField .setText( valueToString(expression.evaluate()) );
                updated_ = true;
            }
        });
    }

    private String valueToString(Value value) {
        Object[] valformatted = valueToStringImpl(value);
        return
            (((Boolean) valformatted[1]).booleanValue() ? "about " : "") +
            (String) valformatted[0];
    }

    private Object[] valueToStringImpl(Value value) {
        if (value instanceof ValueScalar) {
            return numberToString(((ValueScalar) value).number());
        }
        if (value instanceof ValueVector) {
            Value[] vals = ((ValueVector) value).values();
            boolean approximate = false;
            String str = "(";
            for (int i = 0; i < vals.length; ++i) {
                Object[] valformatted = valueToStringImpl(vals[i]);
                approximate = approximate || ((Boolean) valformatted[1]).booleanValue();
                str += valformatted[0] + (i < vals.length - 1 ? ", " : "");
            }
            str += ")";
            return new Object[]{str, new Boolean(approximate)};
        }
        if (value instanceof ValueMatrix) {
            Value[][] vals = ((ValueMatrix) value).vals();
            boolean approximate = false;
            String str = "[";
            for (int i = 0; i < vals.length; ++i) {
                for (int j = 0; j < vals[i].length; ++j) {
                    Object[] valformatted = valueToStringImpl(vals[i][j]);
                    approximate = approximate || ((Boolean) valformatted[1]).booleanValue();
                    str += valformatted[0] + (j < vals[i].length - 1 ? ", " : "");
                }
                str += (i < vals.length - 1 ? " ; " : "");
            }
            str += "]";
            return new Object[]{str, new Boolean(approximate)};
        }
        return new Object[]{value.toString(), new Boolean(false)};
    }


    // ************************ FORMATING NUMBERS **************************** //

    private static final double EQUAL_TOLERANCE = 1e-10;

    private static final String[] COMMON_VAL_NAMES = new String[]{
        // names of common values to check
        "pi", "e", "sqrt(2)", "sqrt(3)", "pi*sqrt(2)", "pi^2"
    };
    private static final double[] COMMON_VALS = new double[] {
        // common values to check
        Math.PI, Math.E, Math.sqrt(2), Math.sqrt(3), Math.PI*Math.sqrt(2), Math.PI*Math.PI
    };

    private boolean aboutEqual(double a, double b) {
        // a === b if a if a and b are within EQUAL_TOLERANCE of each other
        return Math.abs(a - b) < EQUAL_TOLERANCE;
    }

    // if frac is rational (up to a tolerance), returns an array of {numerator, denominator, tolerance}
    // otherwise returns null
    // checks denominators up to maxDenom (a positive integer)
    private double[] findFraction(double frac, int maxDenom) {
        // go through possible denominators
        for (int q = 1; q <= maxDenom; ++q) {
            // p/q will be our guess
            double p = Math.round(frac * q);
            double tol = Math.abs(frac - p/q);
            if (tol < EQUAL_TOLERANCE)
                return new double[]{p, q, tol};
        }
        return null;
    }

    // [0] is the String
    // [1] is a Boolean containing whether the value is approximate (about right)
    private Object[] numberToString(double num) {
        if (!smartFormat_)
            return new Object[]{String.valueOf(num), new Boolean(false)};
        if (num == (int) num)
            return new Object[]{String.valueOf((int) num), new Boolean(false)};
        if (aboutEqual(num, Math.round(num)))
            return new Object[]{String.valueOf(Math.round(num)), new Boolean(true)};
        double[] fraction = findFraction(num, 20);
        if (fraction != null) {
            return new Object[]{
                new String((int) fraction[0] + "/" + (int) fraction[1]),
                new Boolean(fraction[2] > 0)};
        }
        for (int numindex = 0; numindex < COMMON_VALS.length; ++numindex) {
            double n = COMMON_VALS[numindex];
            {
                // check for n in numerator
                double[] frac = findFraction(num / n, 20);
                if (frac != null) {
                    int numerator = (int) frac[0], denominator = (int) frac[1];
                    double tol = frac[2];
                    return new Object[]{
                        new String((numerator == 1 ? "" : numerator == -1 ? "-" : numerator + "*") +
                                   COMMON_VAL_NAMES[numindex] +
                                   (denominator == 1 ? "" : "/" + denominator) ),
                        new Boolean(tol > 0)};
                }
            }
            {
                // check for n in denominator
                double[] frac = findFraction(num * n, 20);
                if (frac != null) {
                    int numerator = (int) frac[0], denominator = (int) frac[1];
                    double tol = frac[2];
                    boolean useDenom = denominator != 1;
                    return new Object[]{
                        new String(numerator + "/" +
                                   (useDenom ? "(" + denominator + "*" : "") +
                                   COMMON_VAL_NAMES[numindex] +
                                   (useDenom ? ")" : "")),
                        new Boolean(tol > 0)};
                }
            }
        }
        return new Object[]{String.valueOf(num), new Boolean(false)};
    }


    // ************************* DEPENDENCY STUFF *************************** //
    

	public void dependencyUpdateVal( Set updatingObjects ) {
		if (autoUpdate_)
            readout();
        else
            updated_ = false;
    }

    public void dependencyUpdateDef( Set updatingObjects ) {
        dependencyUpdateVal(updatingObjects);
    }

    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }



    // ****************************** FILE I/O ****************************** //

    private String expression__;
    
    public ReadoutPanel(Token tok, FileParser parser) {
        demo = parser.demo();
        try {
            FileProperties props = parser.parseProperties(tok);
            if (props.contains("autoupdate"))
                autoUpdate_ = parser.parseBoolean(props.get("autoupdate"));
            if (props.contains("smart"))
                smartFormat_ = parser.parseBoolean(props.get("smart"));
            expression__ = parser.parseExpression(props.get("expr"));
        }
        catch (FileParseException ex) {
            // no properties: the tok is the expression
            expression__ = parser.parseExpression(tok);
        }

    }

    public void loadFileBind(FileParser parser) {
    }

    public void loadFileExprs(FileParser parser) {
        expression = parser.recognizeExpression(expression__);
        if (expression != null)
            DependencyManager.setDependency(this, expression);
    }
    
    public void loadFileFinish(FileParser parser) {
        if (expression != null) {
            expressionString = expression.definitionString();
            expField.setText(expressionString);
            expression.setTypeChangeAllowed(true);
        }
        init();
        readout();
    }
    
    public Token saveFile(FileGenerator generator) {
        if (!autoUpdate_ || !smartFormat_) {
            FileProperties props = new FileProperties();
            if (!autoUpdate_)
                props.add("autoupdate", generator.generateBoolean(false));
            if (!smartFormat_)
                props.add("smart", generator.generateBoolean(false));
            props.add("expr", generator.generateExpression(expression));
            return generator.generateProperties(props);
        }
        return generator.generateExpression(expression);
    }

}
