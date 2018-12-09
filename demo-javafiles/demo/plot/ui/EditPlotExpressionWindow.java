package demo.plot.ui;
import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.coloring.ui.*;
import demo.Demo;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.plot.Plot;
import demo.plot.PlotExpression;
import demo.coloring.ui.EditColoringGroupPanel;
import demo.coloring.ui.EditColoringListener;

public  abstract class EditPlotExpressionWindow extends EditPlotWindow {

    private  Panel centerPanel = new Panel();

    protected  TextField expressionField = new TextField();

    protected  EditColoringGroupPanel colorPanel;

    protected Button okBtn = new Button( "OK" );
    
    protected  Button removeBtn = new Button( "Remove Plot" );

    protected PlotVisibleCheckbox visibleCheckbox = null;

    protected  int numcoords;

    // things that should be set by subclases
    // the label that says "Enter a paremetric <something>: " at the top
    protected  Label expressionLabel = new Label();

    // panel containing options specific to the plot. Say optionsPanel.add(.) to add something.
    protected  Panel optionsPanel;

    private  boolean optionsPanelShowing = false;

    protected EditPlotExpressionWindow( PlotExpression plot, Demo demo, Environment env, int numcoords, boolean editing, Set listeners ) {
        super(plot, demo, env, editing, listeners);

        this .numcoords = numcoords;
        expressionField .setText( ((PlotExpression) plot) .definition());
        Panel buttonPanel = new Panel();
        buttonPanel .setLayout( new BorderLayout() );
        Panel okPanel = new Panel();
        okPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
        okPanel .add( okBtn );
        Panel removeBtnPanel = new Panel();
        removeBtnPanel .setLayout( new FlowLayout( FlowLayout .LEFT ) );
        removeBtnPanel .add( removeBtn );
        buttonPanel .add( removeBtnPanel, "West" );
        buttonPanel .add( okPanel, "East" );
        this .setLayout( new BorderLayout() );
        Panel northPanel = new Panel();
        northPanel .setLayout( new BorderLayout() );
        expressionLabel = new Label( "Enter a parametric expression: " );
        northPanel .add( expressionLabel, "North" );
        northPanel .add( expressionField, "Center" );
        northPanel .add(new PanelSeperator( PanelSeperator .HORIZONTAL ), "South");
        this .add( northPanel, "North" );
        EditPlotWindowColorCanvasListener epwccList = new EditPlotWindowColorCanvasListener(this);
        this .colorPanel = new EditColoringGroupPanel( demo, this, epwccList,
                                                       ((PlotExpression) plot) .coloring(),
                                                       new String[]{ EditColoringWindow.CONSTANT,
                                                           EditColoringWindow.EXPRESSION,
                                                           EditColoringWindow.GRADIENT,
                                                           EditColoringWindow.CHECKER},
                                                       environment_.append(
                                                           plot.expressionDefinitions()) );
        this.colorPanel.addComponentListener(epwccList);
        centerPanel .setLayout( new BorderLayout() );
        Panel coloringsPanel = new Panel();
        coloringsPanel .setLayout( new BorderLayout() );
        coloringsPanel .add( new Label( " Colorings:" ), "North" );
        coloringsPanel .add( this .colorPanel, "Center" );
        centerPanel .add( coloringsPanel, "Center" );
        this .add( centerPanel, "Center" );
        Panel southPanel = new Panel();
        southPanel .setLayout( new BorderLayout() );
        southPanel .add( new PanelSeperator(), "North" );
        southPanel .add( buttonPanel, "South" );
        this .add( southPanel, "South" );
        // create options panel.
        this .optionsPanel = new Panel();
        optionsPanel .setLayout( new GridLayout( 0, 1 ) );
        optionsPanel .add( new Label( "Options:" ), 0 );
        visibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", plot);
        optionsPanel .add( visibleCheckbox );
        showOptionsPanel();
        // adjust size
        pack();
        setLocation(50,50);
    }

    public  boolean action( Event e, Object o ) {
        if ( e .target == okBtn ) {
            if ( changePlot( expressionField .getText(), null ) ) {
                updatePlot();
                setExitStatus(EXIT_STATUS_OK);
                setVisible(false);
                dispose();
            }
            return true;
        }
        else {
            if ( e .target == removeBtn ) {
                if (removePlot()) {
                    setExitStatus(EXIT_STATUS_REMOVE);
                    setVisible(false);
                    dispose();
                }
                return true;
            }
            else {
                if ( e .target == expressionField ) {
                    // update the plot
                    changePlot( expressionField .getText(), null );
                    updatePlot();
                    return true;
                }
            }
        }
        return false;
    }

    public  boolean handleEvent( Event e ) {
        if ( e .id == java.awt .Event .WINDOW_DESTROY ) {
            setExitStatus(EXIT_STATUS_OK);
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .handleEvent( e );
        }
    }

    public  boolean keyDown( java.awt .Event e, int key ) {
        if ( (key == 10 || key == 3) && ! (e .target instanceof TextComponent) ) {
            // enter or return key pressed: send OK event
            return action( new Event( okBtn, Event .ACTION_EVENT, null ), null );
        }
        return super .keyDown( e, key );
    }

    public  abstract boolean changePlot( String expression, Coloring coloring ) ;


    public  void enable() {
        expressionField .enable();
        colorPanel .enable();
        okBtn .enable();
    }

    public  void disable() {
        expressionField .disable();
        colorPanel .disable();
        okBtn .disable();
    }

    public  void dispose() {
        // dispose panel
        colorPanel .dispose();
        if (visibleCheckbox != null)
            visibleCheckbox.dispose();
        super .dispose();
    }

    protected  void showOptionsPanel() {
        if ( ! optionsPanelShowing ) {
            optionsPanelShowing = true;
            // add options panel to east
            centerPanel .add( new PanelSeperator( PanelSeperator .VERTICAL ), "East" );
            Panel eastPanel = new Panel();
            eastPanel .setLayout( new BorderLayout() );
            eastPanel .add( optionsPanel, "North" );
            this .add( eastPanel, "East" );
        }
    }


}


class EditPlotWindowColorCanvasListener implements ComponentListener, EditColoringListener {

    private EditPlotExpressionWindow window_;

    public EditPlotWindowColorCanvasListener(EditPlotExpressionWindow w) {
        window_ = w;
    }

    public void componentResized( java.awt.event.ComponentEvent ev ) {
        window_.pack();
    }

    public void coloringChanged(Coloring coloring) {
        window_.changePlot(null, coloring);
        window_.updatePlot();
    }

    public void componentHidden( java.awt.event.ComponentEvent ev ) {

    }

    public void componentShown( java.awt.event.ComponentEvent ev ) {

    }

    public void componentMoved( java.awt.event.ComponentEvent ev ) {

    }

}






