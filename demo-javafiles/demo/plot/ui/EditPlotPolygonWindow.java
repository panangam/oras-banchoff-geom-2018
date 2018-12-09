package demo.plot.ui;
/**
 * @author deigen
 */

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.depend.*;
import demo.coloring.ui.*;
import demo.util.*;
import demo.Demo;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.plot.Plot;
import demo.plot.PlotPolygon;
import demo.expr.Expression;

public class EditPlotPolygonWindow extends EditPlotWindow {

    private int dimension;

    // constants for edit mode
    private static final int EDIT_NONE = 0;
    private static final int EDIT_VERTEX = 1;
    int editMode = EDIT_NONE;
    int editItem;

    private Button okBtn = new Button(" OK "), removeBtn = new Button("Remove Plot");
    private Button addVertexBtn = new Button("Add Point"),
        removeVertexBtn = new Button("Remove Point");
    private java.awt.List vertexList = new List(10, false);
    private TextField titleField = new TextField(20);
    private TextField editField = new TextField();
    private PlotVisibleCheckbox plotVisibleCheckbox;
    private Checkbox drawThickCheckbox = new Checkbox("Draw Thick");
    private TextField subdivsField = new TextField(4);

    private PlotPolygon plot;
    
    public EditPlotPolygonWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotPolygon("Polygon",
                              new ColoringConstant(new DemoColor(0,1,0,1) .coloringColor()),
                              dimension),
              demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(plot);
        setAnimateSetSize(true);
    }

    public EditPlotPolygonWindow( PlotPolygon plot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( plot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotPolygonWindow( PlotPolygon plot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( plot, demo, env, editing, listeners );

        this.dimension = dimension;

        Environment coloringEnvironment = demo.environment().append(plot.expressionDefinitions());

        this.addWindowListener(new WindowLsnrCancel());
        okBtn.addActionListener(new OKBtnLsnr());
        removeBtn.addActionListener(new RemoveBtnLsnr());
        addVertexBtn.addActionListener(new AddVertexBtnLsnr());
        removeVertexBtn.addActionListener(new RemoveVertexBtnLsnr());
        vertexList.addItemListener(new VertexListLsnr());
        titleField.addActionListener(new TitleFieldLsnr());
        editField.addActionListener(new EditFieldLsnr());
        drawThickCheckbox.addItemListener(new DrawThickCheckboxLsnr());
        subdivsField.addActionListener(new SubdivsFieldLsnr());

        plotVisibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", plot);

        setLayout(new BorderLayout());

        // bottom of window: OK button and remove plot button
        Panel southPanel = new Panel();
        southPanel.setLayout(new BorderLayout());
        Panel southPanelInternal = new Panel();
        southPanelInternal.setLayout(new GridLayout(1,2));
        southPanel.add( new PanelSeperator(PanelSeperator.HORIZONTAL),"North");
        southPanel.add( southPanelInternal,"Center");
        Panel okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPanel.add(okBtn);
        Panel removePanel = new Panel();
        removePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        removePanel.add(removeBtn);
        southPanelInternal.add(removePanel);
        southPanelInternal.add(okPanel);
        this.add( southPanel,"South");

        // top of window: field to edit the title
        Panel northPanel = new Panel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add( new PanelSeperator(PanelSeperator.HORIZONTAL),"South");
        Panel northPanelInternal = new Panel();
        northPanel.add( northPanelInternal,"North");
        northPanelInternal.setLayout(new FlowLayout(FlowLayout.CENTER));
        northPanelInternal.add(new Label("Title: "));
        northPanelInternal.add(titleField);
        this.add( northPanel,"North");

        // center of window: list of vertices and coloring editor
        Panel centerPanel = new Panel();
        centerPanel.setLayout(new BorderLayout());
        Panel vertexPanel = new Panel();
        vertexPanel.setLayout(new BorderLayout());
        vertexPanel.add( vertexList,"Center");
        Panel vertexBtnPanel = new Panel();
        vertexBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        vertexBtnPanel.add(addVertexBtn);
        vertexBtnPanel.add(removeVertexBtn);
        Panel vertexPanelNorth = new Panel();
        vertexPanelNorth.setLayout(new BorderLayout());
        vertexPanelNorth.add( vertexBtnPanel,"North");
        vertexPanelNorth.add( editField,"South");
        vertexPanel.add( vertexPanelNorth,"North");
        centerPanel.add( vertexPanel,"West");

        centerPanel.add( new PanelSeperator(PanelSeperator.VERTICAL),"Center");
        
        ColoringEditorLsnr coloringEditorLsnr = new ColoringEditorLsnr();
        EditColoringGroupPanel coloringEditor = new EditColoringGroupPanel(demo, this, coloringEditorLsnr, plot.coloring(), new String[]{EditColoringWindow.CONSTANT, EditColoringWindow.EXPRESSION, EditColoringWindow.GRADIENT}, coloringEnvironment);
        coloringEditor.addComponentListener(coloringEditorLsnr);
        centerPanel.add( coloringEditor,"East");
        
        this.add( centerPanel,"Center");

        // right of window: options for visibility, draw thick, etc.
        Panel optionsPanel = new Panel();
        optionsPanel.setLayout(new BorderLayout());
        optionsPanel.add( new PanelSeperator(PanelSeperator.VERTICAL),"West");
        Panel optionsPanelInternal = new Panel();
        optionsPanelInternal.setLayout(new GridLayout(0,1));
        Panel optionsPanelCenter = new Panel();
        optionsPanelCenter.setLayout(new FlowLayout(FlowLayout.LEFT));
        optionsPanelCenter.add(optionsPanelInternal);
        optionsPanel.add( optionsPanelCenter,"Center");
        optionsPanelInternal.add(plotVisibleCheckbox);
        optionsPanelInternal.add(drawThickCheckbox);
        Panel subdivsPanel = new Panel();
        subdivsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        subdivsPanel.add(new Label("Subdivisions per edge:"));
        subdivsPanel.add(subdivsField);
        optionsPanelInternal.add(subdivsPanel);
        optionsPanelInternal.add(new PanelSeperator(PanelSeperator.HORIZONTAL));
        optionsPanelInternal.add(new EditPlotCommonOptionsBtn(plot, environment_, this));
        this.add( optionsPanel,"East");
        
        this.plot = plot;
        titleField.setText(plot.title());
        drawThickCheckbox.setState(plot.drawThick());
        subdivsField.setText(new Integer(plot.edgeSubdivisions()) .toString());

        updateVertexList();

        setEditNoneMode();

        pack();
        setVisible(true);
    }


    private void updateVertexList() {
        int selectedIndex = vertexList.getSelectedIndex();
        vertexList.removeAll();
        for (int i = 0; i < plot.vertices().size(); ++i) {
            Expression expr = (Expression) plot.vertices().elementAt(i);
            String str = "[" + (i+1) + "]   " + expr.definitionString();
            vertexList.add(str);
        }
        vertexList.select(selectedIndex);
    }

    
    private void setEditNoneMode() {
        editMode = EDIT_NONE;
        editField.setText("");
        editField.disable();
        removeVertexBtn.disable();
        vertexList.deselect(vertexList.getSelectedIndex());
    }

    private void setEditVertexMode(int vertexNum) {
        editMode = EDIT_VERTEX;
        editItem = vertexNum;
        vertexList.select(vertexNum);
        editField.setText( ((Expression) plot.vertices().elementAt(editItem)).definitionString() );
        editField.enable();
        removeVertexBtn.enable();
    }

    public void dispose() {
        plotVisibleCheckbox.dispose();
        super.dispose();
    }



    

    private class AddVertexBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Expression expr = demo.recognizeExpression(dimension == 2 ? "0,0" : "0,0,0");
            plot.addVertex(expr);
            updatePlot();
            updateVertexList();
            setEditVertexMode(plot.vertices().size() - 1);
        }
    }
    
    private class RemoveVertexBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int elt = vertexList.getSelectedIndex();
            if (0 > elt || elt >= plot.vertices().size())
                return;
            plot.removeVertex(elt);
            updatePlot();
            updateVertexList();
            setEditNoneMode();
        }
    }

    private class VertexListLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEditVertexMode(vertexList.getSelectedIndex());
            }
        }
    }

    private class TitleFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ((PlotPolygon) plot).setTitle(titleField.getText());
            updatePlot();
        }
    }
    
    private class EditFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            switch (editMode) {
                case EDIT_NONE:
                    return;
                case EDIT_VERTEX:
                    Expression expr = demo.recognizeExpression(editField.getText(), environment_);
                    if (expr == null) return;
                    if (expr.numIntervals() > 0) {
                        demo.showError("A vertex cannot have intervals.");
                        expr.dispose();
                        return;
                    }
                    if (!expr.returnsVector(dimension)) {
                        demo.showError("The vertex must be have dimension " + dimension + ".");
                        expr.dispose();
                        return;
                    }
                    plot.setVertex(editItem, expr);
                    updatePlot();
                    updateVertexList();
                    return;
                default:
                    return;
            }
        }
    }

    private class ColoringEditorLsnr implements EditColoringListener, ComponentListener {
        public void coloringChanged(Coloring coloring) {
            plot.setColoring(coloring);
            updatePlot();
        }
        
        public void componentResized( java.awt.event.ComponentEvent ev ) {
            pack();
        }
        public void componentHidden( java.awt.event.ComponentEvent ev ) {
        }
        public void componentShown( java.awt.event.ComponentEvent ev ) {
        }
        public void componentMoved( java.awt.event.ComponentEvent ev ) {
        }
    }

    private class SubdivsFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int subdivs;
            try {
                subdivs = new Integer(subdivsField.getText()).intValue();
            }
            catch (NumberFormatException ex) {
                demo.showError("Number of subdivisions per edge must be a natrural number.");
                return;
            }
            if (subdivs <= 0) {
                demo.showError("Number of subdivisions per edge must be a natrural number.");
                return;
            }
            plot.setEdgeSubdivisions(subdivs);
            updatePlot();
        }
    }

    private class DrawThickCheckboxLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            plot.setDrawThick(drawThickCheckbox.getState());
            updatePlot();
        }
    }

    private class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent ev) {
            ((PlotPolygon) plot).setTitle(titleField.getText());
            updatePlot();
            super.actionPerformed(ev);
        }
    }
    
}