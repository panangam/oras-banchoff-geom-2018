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
import demo.plot.PlotCurve;

public class EditPlotWindowCurve extends EditPlotExpressionWindow{

	public 
	EditPlotWindowCurve(Demo demo, Environment env, int numcoords, Set listeners ) {
            this( new PlotCurve(new ColoringConstant(new double[]{0,1,0,1}), numcoords),
                  demo, env, numcoords, false, listeners );
            plotCreated();
            DependencyManager.updateDependentObjectsDefMT(plot);
        }

        public EditPlotWindowCurve( PlotCurve plot, Demo demo, Environment env, int numcoords, Set listeners ) {
                this( plot, demo, env, numcoords, true, listeners );
        }

	protected
	EditPlotWindowCurve( PlotCurve plot, Demo demo, Environment env, int numcoords, boolean editing, Set listeners ) {
		super( plot, demo, env, numcoords, editing, listeners );

                // add draw thick checkbox to options
                drawThickCheckbox .setState( plot == null ? false 
                                                : ((PlotCurve) plot) .getDrawThick() );
                optionsPanel .add( drawThickCheckbox );
                optionsPanel.add(new EditPlotCommonOptionsBtn(plot, environment_, this));
                showOptionsPanel();
                this .expressionLabel .setText( "Enter a parametric curve:" );
                pack();
                setVisible(true);
                setAnimateSetSize(true);
        }

	private 
	Checkbox drawThickCheckbox = new Checkbox( "Draw Thick" );

	public  boolean action( Event ev, Object obj ) {
		if ( ev .target == drawThickCheckbox ) {
                    // update the plot
                    ((PlotCurve) plot) .setDrawThick( drawThickCheckbox .getState() );
                    updatePlot();
                    return true;
		}
		return super .action( ev, obj );
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
            return this .demo .changePlotCurve( (PlotCurve) plot, expr, coloring, numcoords );
	}
        


}


