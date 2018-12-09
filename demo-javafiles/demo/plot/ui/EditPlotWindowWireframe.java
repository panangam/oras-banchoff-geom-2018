package demo.plot.ui;

import java.awt .*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.depend.*;
import demo.Demo;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.expr.Expression;
import demo.plot.Plot;
import demo.plot.PlotWireframe;
import demo.expr.*;
import demo.expr.ste.STEInterval;

public class EditPlotWindowWireframe extends EditPlotExpressionWindow{

    public
    EditPlotWindowWireframe( Demo demo, Environment env, int numcoords, Set listeners ) {
        this( new PlotWireframe(new ColoringConstant(new double[]{0,1,0,1}), numcoords),
              demo, env, numcoords, false, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
    }

    public EditPlotWindowWireframe( PlotWireframe plot, Demo demo, Environment env, int numcoords, Set listeners ) {
        this( plot, demo, env, numcoords, true, listeners );
    }

    protected
    EditPlotWindowWireframe( PlotWireframe plot, Demo demo, Environment env, int numcoords, boolean editing, Set listeners ) {
        super( plot, demo, env, numcoords, editing, listeners );

        // set intervals and connection values
        this .intervals = ((PlotWireframe) plot) .intervals();
        this .connectValues = ((PlotWireframe) plot) .connectIntervalValues();
        // add draw thick checkbox to options
        drawThickCheckbox .setState( plot == null ? false
                                                  : ((PlotWireframe) plot) .getDrawThick() );
        optionsPanel .add( drawThickCheckbox );
        // add button for options
        Panel connectBtnPanel = new Panel();
        connectBtnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        connectBtnPanel.add(connectBtn);
        optionsPanel .add( connectBtnPanel );
        optionsPanel.add(new EditPlotCommonOptionsBtn(plot, environment_, this));
        showOptionsPanel();
        // set label for expression
        expressionLabel .setText( "Enter a parametric expression:" );
        pack();
        setVisible(true);
        setAnimateSetSize(true);
    }

    public  boolean changePlot( String expression, Coloring coloring ) {
        Expression expr;
        if (expression == null) {
            expr = null;
        }
        else {
            expr = demo.recognizeExpression(expression, environment_);
            if (expr == null) return false;
        }
        return this .demo .changePlotWireframe( (PlotWireframe) plot,
                                                expr, coloring, numcoords, intervals, connectValues );
    }


    private  Button connectBtn = new Button( "Connections..." );

    private
        Checkbox drawThickCheckbox = new Checkbox( "Draw Thick" );

    public  boolean action( Event ev, Object obj ) {
        if ( ev .target == connectBtn ) {
            if ( findIntervals() ) {
                // success
                this .disable();
                new EditPlotWireConnections( intervals, connectValues, this );
            }
            return true;
        }
        if ( ev .target == drawThickCheckbox ) {
            // update the plot
            ((PlotWireframe) plot) .setDrawThick( drawThickCheckbox .getState() );
            updatePlot();
            return true;
        }
        return super .action( ev, obj );
    }

    public  void setConnectionsValues( boolean[] values ) {
        if ( values .length == this .connectValues .length ) {
            connectValues = values;
            demo.changePlotWireframe((PlotWireframe) plot, null, null, numcoords, intervals, connectValues);
            updatePlot();
        }
    }

    private  STEInterval[] intervals;

    private  boolean[] connectValues;

    private  boolean findIntervals() {
        STEInterval[] oldIntervals = this .intervals;
        boolean[] oldConnects = this .connectValues;
        IntervalExpression expression = demo .recognizeIntervalExpression( this .expressionField .getText() );
        if ( expression == null ) {
            // there was some error
            return false;
        }
        this .intervals = expression .sortedIntervals();
        this .connectValues = new boolean [ intervals .length ];
        if ( oldIntervals == null ) {
            // we didn't have any intervals before; so set all connects to true
            for ( int i = 0; i < this .connectValues .length; i++ ) {
                this .connectValues[i] = true;
            }
            return true;
        }
        // for each of the new intervals, see if it existed in the old intervals, and set the connect
        for ( int interval = 0; interval < this .intervals .length; interval++ ) {
            STEInterval currInterval = this .intervals[interval];
            this .connectValues[interval] = true;
            // see if it was in the old intervals
            for ( int i = 0; i < oldIntervals .length; i++ ) {
                if ( oldIntervals[i] == currInterval ) {
                    this .connectValues[interval] = oldConnects[i];
                    break;
                }
            }
        }
        expression.dispose();
        return true;
    }

    public  void disable() {
        connectBtn .disable();
        super .disable();
    }

    public  void enable() {
        connectBtn .enable();
        super .enable();
    }


}








class EditPlotWireConnections extends Dialog{

    public
    EditPlotWireConnections( STEInterval[] intervals, boolean[] defaultValues, EditPlotWindowWireframe myWindow ) {
        super( myWindow );

        this .myWindow = myWindow;
        this .intervals = intervals;
        //System .out .println( "in dialog constructor." );
        //System .out .println( "intervals is: " + intervals + "    length: " + intervals .length );
        this .checkboxes = new Checkbox [ intervals .length ];
        for ( int i = 0; i < checkboxes .length; i++ ) {
            //System .out .println( "in checkboxes assignment loop. i: " + i );
            checkboxes[i] = new Checkbox( "Connect " + intervals[i] .name(), defaultValues[i] );
        }
        // put into panel alphabetically
        //System .out .println( "about to put checkboxes into frame" );
        Panel checkboxesPanel = new Panel();
        checkboxesPanel .setLayout( new GridLayout( 0, 1 ) );
        for ( int i = 0; i < intervals .length; i++ ) {
            String currName = intervals[i] .name();
            int place = 0;
            // compare to the label's substring from the 8th char, since the label has "Connect " tacked on the front
            while ( place < checkboxesPanel .countComponents()
                    && isGreaterThanOrEqual( currName, ((Checkbox) checkboxesPanel .getComponent( place )) .getLabel() .substring( 8 ) ) ) {
                place++;
            }
            checkboxesPanel .add( checkboxes[i],  place >= intervals .length ? - 1 : place );
        }
        // make buttons panel and set up dialog box
        Panel buttonsPanel = new Panel();
        buttonsPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
        buttonsPanel .add( cancelBtn );
        buttonsPanel .add( okBtn );
        this .setLayout( new BorderLayout() );
        this .add( checkboxesPanel, "Center" );
        this .add( buttonsPanel, "South" );
        this .pack();
        this .setVisible(true);
    }

    public  boolean[] values() {
        boolean[] toReturn = new boolean [ checkboxes .length ];
        for ( int i = 0; i < toReturn .length; i++ ) {
            toReturn[i] = checkboxes[i] .getState();
        }
        return toReturn;
    }

    public  boolean action( Event ev, Object obj ) {
        if ( ev .target == okBtn ) {
            myWindow .setConnectionsValues( values() );
            setVisible(false);
            dispose();
            myWindow .enable();
            return true;
        }
        else {
            if ( ev .target == cancelBtn ) {
                setVisible(false);
                dispose();
                myWindow .enable();
                return true;
            }
            else {
                if ( ev .target instanceof Checkbox ) {
                    return true;
                }
                else {
                    return super .action( ev, obj );
                }
            }
        }
    }

    public  boolean keyDown( java.awt .Event e, int key ) {
        if ( (key == 10 || key == 3) && ! (e .target instanceof TextComponent) ) {
            // enter or return key pressed: send OK event
            return action( new Event( okBtn, Event .ACTION_EVENT, null ),
                           null );
        }
        return super .keyDown( e, key );
    }

    private  EditPlotWindowWireframe myWindow;

    private  STEInterval[] intervals;

    private  Checkbox[] checkboxes;

    private
        Button okBtn = new Button( "OK" ),
        cancelBtn = new Button( "Cancel" );

    private
        boolean isGreaterThanOrEqual( String str1, String str2 ) {
            for ( int i = 0; i < str1 .length() && i < str2.length(); i++ ) {
                if ( (int) str1 .charAt( i ) < (int) str2 .charAt( i ) ) {
                    return false;
                }
                if ( str1 .charAt( i ) > str1 .charAt( i ) ) {
                    return true;
                }
            }
            return str1.length() >= str2.length();
        }


}


