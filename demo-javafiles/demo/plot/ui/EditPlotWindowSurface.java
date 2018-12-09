package demo.plot.ui;

import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import java.awt.event.*;

import demo.ui.*;
import demo.depend.*;
import demo.Demo;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.expr.Expression;
import demo.plot.Plot;
import demo.plot.PlotSurface3D;

public class EditPlotWindowSurface extends EditPlotExpressionWindow implements ItemListener {

        private java.awt.Checkbox interpolateColorsCheckbox_
                                         = new java.awt.Checkbox("Interpolate Colors");
        private java.awt.Checkbox smoothLightingCheckbox_
                                         = new java.awt.Checkbox("Smooth Lighting");

	public 
	EditPlotWindowSurface( Demo demo, Environment env, int numcoords, Set listeners ) {
            this( new PlotSurface3D(new ColoringConstant(new double[]{0,1,0,1}), numcoords),
                  demo, env, numcoords, false, listeners );
            plotCreated();
            DependencyManager.updateDependentObjectsDefMT(plot);
	}

        public EditPlotWindowSurface( PlotSurface3D plot, Demo demo, Environment env, int numcoords, Set listeners ) {
                this( plot, demo, env, numcoords, true, listeners );
        }



	protected
	EditPlotWindowSurface( PlotSurface3D plot, Demo demo, Environment env, int numcoords, boolean editing, Set listeners ) {
		super( plot, demo, env, numcoords, editing, listeners );

			this .expressionLabel .setText( "Enter a parametric surface:" );
                        optionsPanel.add(interpolateColorsCheckbox_);
                        optionsPanel.add(smoothLightingCheckbox_);
                        optionsPanel.add(new EditPlotCommonOptionsBtn(plot, environment_, this));
                        interpolateColorsCheckbox_.setState(plot.interpolateColors());
                        interpolateColorsCheckbox_.addItemListener(this);
                        smoothLightingCheckbox_.setState(plot.useVertexNormals());
                        smoothLightingCheckbox_.addItemListener(this);
                        showOptionsPanel();
                        pack();
                        setVisible(true);
                        setAnimateSetSize(true);
		}

        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == interpolateColorsCheckbox_) {
                ((PlotSurface3D) plot).setInterpolateColors(interpolateColorsCheckbox_.getState());
                updatePlot();
            }
            else if (e.getSource() == smoothLightingCheckbox_) {
                ((PlotSurface3D) plot).setUseVertexNormals(smoothLightingCheckbox_.getState());
                updatePlot();
            }
        }

	public 
	boolean changePlot( String expression, Coloring coloring ) {
            Expression expr;
            if (expression == null) {
                expr = null;
            }
            else {
                expr = demo.recognizeExpression(expression, environment_);
                if (expr == null) return false;
            }
            return this .demo .changePlotSurface( (PlotSurface3D) plot, expr, coloring, numcoords );
	}
        


}


