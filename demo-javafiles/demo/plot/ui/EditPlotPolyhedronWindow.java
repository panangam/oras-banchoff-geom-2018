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
import demo.Demo;
import demo.util.Set;
import demo.coloring.Coloring;
import demo.coloring.ColoringConstant;
import demo.plot.Plot;
import demo.plot.PlotPolyhedron;
import demo.expr.Expression;

public class EditPlotPolyhedronWindow extends EditPlotWindow {

    private int dimension;

    // constants for edit mode
    private static final int EDIT_NONE = 0;
    private static final int EDIT_VERTEX = 1;
    private static final int EDIT_FACE = 2;
    int editMode = EDIT_NONE;
    int editItem;

    private Button okBtn = new Button(" OK "), removeBtn = new Button("Remove Plot");
    private Button addVertexBtn = new Button("Add Vertex"),
        removeVertexBtn = new Button("Remove Vertex"),
        addFaceBtn = new Button("Add Face"),
        removeFaceBtn = new Button("Remove Face"),
        setFaceColorBtn = new Button("Set Face Color...");
    private java.awt.List vertexList = new List(10, false), faceList = new List(10, false);
    private TextField titleField = new TextField(30);
    private TextField editVertexField = new TextField();
    private TextField editFaceField = new TextField();
    private PlotVisibleCheckbox plotVisibleCheckbox;

    private PlotPolyhedron myPlot;
    
    public EditPlotPolyhedronWindow( Demo demo, Environment env, int dimension, Set listeners ) {
        this( new PlotPolyhedron("Polyhedron", dimension), demo, env, true, dimension, listeners );
        plotCreated();
        DependencyManager.updateDependentObjectsDefMT(myPlot);
        setAnimateSetSize(true);
    }

    public EditPlotPolyhedronWindow( PlotPolyhedron myPlot, Demo demo, Environment env, int dimension, Set listeners ) {
        this( myPlot, demo, env, true, dimension, listeners );
        setAnimateSetSize(true);
    }

    protected EditPlotPolyhedronWindow( PlotPolyhedron myPlot, Demo demo, Environment env, boolean editing, int dimension, Set listeners) {
        super( myPlot, demo, env, editing, listeners );

        this.dimension = dimension;
        this.myPlot = (PlotPolyhedron) this.plot;

        this.addWindowListener(new WindowLsnrCancel());
        okBtn.addActionListener(new OKBtnLsnr());
        removeBtn.addActionListener(new RemoveBtnLsnr());
        addVertexBtn.addActionListener(new AddVertexBtnLsnr());
        addFaceBtn.addActionListener(new AddFaceBtnLsnr());
        removeVertexBtn.addActionListener(new RemoveVertexBtnLsnr());
        removeFaceBtn.addActionListener(new RemoveFaceBtnLsnr());
        setFaceColorBtn.addActionListener(new SetFaceColorBtnLsnr(this));
        vertexList.addItemListener(new VertexListLsnr());
        faceList.addItemListener(new FaceListLsnr());
        titleField.addActionListener(new TitleFieldLsnr());
        editVertexField.addActionListener(new EditVertexFieldLsnr());
        editFaceField.addActionListener(new EditFaceFieldLsnr());
                
        setLayout(new BorderLayout());
        Panel southPanel = new Panel();
        southPanel.setLayout(new GridLayout(1,2));
        Panel okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPanel.add(okBtn);
        Panel removePanel = new Panel();
        removePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        removePanel.add(removeBtn);
        southPanel.add(removePanel);
        southPanel.add(okPanel);
        this.add( southPanel,"South");
        Panel northPanel = new Panel();
        northPanel.setLayout(new BorderLayout());
        Panel titlePanel = new Panel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        titlePanel.add(new Label("Title:"));
        titlePanel.add(titleField);
        northPanel.add( titlePanel,"West");
        Panel optionsPanel = new Panel();
        optionsPanel.setLayout(new FlowVerticalLayout());
        plotVisibleCheckbox = new PlotVisibleCheckbox("Plot is Visible", myPlot);
        optionsPanel.add(plotVisibleCheckbox);
        optionsPanel.add(new EditPlotCommonOptionsBtn(plot, environment_, this));
        northPanel.add( optionsPanel,"East");
        this.add( northPanel,"North");
        Panel centerPanel = new Panel();
        centerPanel.setLayout(new GridLayout(1,2));
        Panel vertexPanel = new Panel();
        vertexPanel.setLayout(new BorderLayout());
        vertexPanel.add( editVertexField,"Center");
        vertexPanel.add( vertexList,"South");
        Panel vertexBtnPanel = new Panel();
        vertexBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        vertexBtnPanel.add(addVertexBtn);
        vertexBtnPanel.add(removeVertexBtn);
        vertexPanel.add( vertexBtnPanel,"North");
        centerPanel.add(vertexPanel);
        Panel facePanel = new Panel();
        facePanel.setLayout(new BorderLayout());
        facePanel.add( editFaceField,"Center");
        facePanel.add( faceList,"South");
        Panel faceBtnPanel = new Panel();
        faceBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        faceBtnPanel.add(addFaceBtn);
        faceBtnPanel.add(setFaceColorBtn);
        faceBtnPanel.add(removeFaceBtn);
        facePanel.add( faceBtnPanel,"North");
        
        centerPanel.add(facePanel);
        this.add( centerPanel,"Center");

        titleField.setText(myPlot.title());

        updateVertexList();
        updateFaceList();

        setEditNoneMode();

        pack();
        setVisible(true);
    }


    private void updateVertexList() {
        int selectedIndex = vertexList.getSelectedIndex();
        vertexList.removeAll();
        java.util.Vector vertexExprs = myPlot.vertices();
        for (int i = 0; i < vertexExprs.size(); ++i) {
            Expression expr = (Expression) vertexExprs.elementAt(i);
            String str = "[" + (i+1) + "]   " + expr.definitionString();
            vertexList.add(str);
        }
        vertexList.select(selectedIndex);
    }


    private void updateFaceList() {
        int selectedIndex = faceList.getSelectedIndex();
        faceList.removeAll();
        java.util.Vector faceArrays = myPlot.faces();
        for (int i = 0; i < faceArrays.size(); ++i) {
            int[] face = (int[]) faceArrays.elementAt(i);
            faceList.add(">>  " + makeFaceString(face));
        }
        faceList.select(selectedIndex);
    }


    private String makeFaceString(int[] faceArray) {
        if (faceArray.length == 0)
            return "";
        String str = "";
        for (int i = 0; i < faceArray.length - 1; ++i)
            str += (faceArray[i]+1) + ", ";
        str += (faceArray[faceArray.length - 1]+1);
        return str;
    }

    
    private int[] parseFaceString(String faceString) throws NumberFormatException {
        java.util.Vector intVec = new java.util.Vector();
        int start = 0;
        while (start < faceString.length()) {
            int comma = faceString.indexOf(',', start);
            if (comma == -1)
                comma = faceString.length();
            intVec.addElement(new Integer(faceString.substring(start, comma).trim()));
            start = comma + 1;
        }
        int[] intArray = new int[intVec.size()];
        for (int i = 0; i < intArray.length; ++i)
            intArray[i] = ((Integer) intVec.elementAt(i)).intValue() - 1;
        return intArray;
    }

    private void setEditNoneMode() {
        editMode = EDIT_NONE;
        editVertexField.setText("");
        editVertexField.setEnabled(false);
        editFaceField.setText("");
        editFaceField.setEnabled(false);
        setFaceColorBtn.setEnabled(false);
        removeFaceBtn.setEnabled(false);
        removeVertexBtn.setEnabled(false);
        faceList.deselect(faceList.getSelectedIndex());
        vertexList.deselect(vertexList.getSelectedIndex());
    }

    private void setEditVertexMode(int vertexNum) {
        editMode = EDIT_VERTEX;
        editItem = vertexNum;
        vertexList.select(vertexNum);
        faceList.deselect(faceList.getSelectedIndex());
        editVertexField.setText( ((Expression) myPlot.vertices().elementAt(editItem)).definitionString() );
        editVertexField.setEnabled(true);
        editVertexField.setCaretPosition(editVertexField.getText().length());
        editFaceField.setText("");
        editFaceField.setEnabled(false);
        setFaceColorBtn.setEnabled(false);
        removeFaceBtn.setEnabled(false);
        removeVertexBtn.setEnabled(true);
        editVertexField.requestFocus();
    }

    private void setEditFaceMode(int faceNum) {
        editMode = EDIT_FACE;
        editItem = faceNum;
        faceList.select(faceNum);
        vertexList.deselect(vertexList.getSelectedIndex());
        editFaceField.setText( makeFaceString((int[]) myPlot.faces().elementAt(editItem)) );
        editFaceField.setEnabled(true);
        editFaceField.setCaretPosition(editFaceField.getText().length());
        editVertexField.setText("");
        editVertexField.setEnabled(false);
        setFaceColorBtn.setEnabled(true);
        removeFaceBtn.setEnabled(true);
        removeVertexBtn.setEnabled(false);
        editFaceField.requestFocus();
    }

    public void dispose() {
        plotVisibleCheckbox.dispose();
        super.dispose();
    }



    

    private class AddVertexBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            myPlot.addVertex(demo.recognizeExpression(dimension == 2 ? "0,0" : "0,0,0"));
            updateVertexList();
            setEditVertexMode(myPlot.vertices().size() - 1);
        }
    }
    
    private class AddFaceBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Coloring c = new ColoringConstant(new double[]{0,1,0,1});
            myPlot.addFace(new int[]{}, c);
            updateFaceList();
            setEditFaceMode(myPlot.faces().size() - 1);
        }
    }

    private class RemoveVertexBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int elt = vertexList.getSelectedIndex();
            if (0 > elt || elt >= myPlot.vertices().size())
                return;
            // see if vertex is used in faces
            java.util.Enumeration faces = myPlot.faces().elements();
            boolean vertexUsed = false;
            while (faces.hasMoreElements() && !vertexUsed) {
                int[] face = (int[]) faces.nextElement();
                for (int i = 0; i < face.length; ++i) {
                    if (face[i] == elt) {
                        vertexUsed = true;
                        break;
                    }
                }
            }
            if (vertexUsed) {
                demo.showError("You cannot remove that vertex because it is being used by faces.");
            }
            else {
                myPlot.removeVertex(elt);
                // updatePlot();
                updateVertexList();
                setEditNoneMode();
            }
        }
    }

    private class RemoveFaceBtnLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int elt = faceList.getSelectedIndex();
            if (elt < 0 || elt >= myPlot.faces().size())
                return;
            myPlot.removeFace(elt);
            updatePlot();
            updateFaceList();
            setEditNoneMode();
        }
    }

    private class SetFaceColorBtnLsnr implements ActionListener {
        private EditPlotPolyhedronWindow parent;
        public SetFaceColorBtnLsnr(EditPlotPolyhedronWindow parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {
            int elt = faceList.getSelectedIndex();
            if (elt < 0 || elt >= myPlot.faces().size())
                return;
            new EditColoringWindow(new String[]{
                                            EditColoringWindow.CONSTANT,
                                            EditColoringWindow.EXPRESSION,
                                            EditColoringWindow.GRADIENT },
                                   demo,
                                   new FaceColorWindowLsnr(elt),
                                   (Coloring) myPlot.colorings().elementAt(elt),
                                   parent);
        }
    }

    private class FaceColorWindowLsnr implements DialogListener {
        int index;
        public FaceColorWindowLsnr(int ix) { index = ix; }
        public void dialogCanceled( Window dialog ) {
        }
        public void dialogOKed( Window dialog ) {
            Coloring c = ((EditColoringWindow) dialog).coloring();
            myPlot.setColoring(index, c);
            updatePlot();
        }
    }
    
    private class VertexListLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEditVertexMode(vertexList.getSelectedIndex());
            }
        }
    }

    private class FaceListLsnr implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEditFaceMode(faceList.getSelectedIndex());
            }
        }
    }

    private class TitleFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ((PlotPolyhedron) myPlot).setTitle(titleField.getText());
            updatePlot();
        }
    }
    
    private class EditVertexFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            switch (editMode) {
                case EDIT_NONE:
                    return;
                case EDIT_VERTEX:
                    Expression expr = demo.recognizeExpression(editVertexField.getText(), environment_);
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
                    myPlot.setVertex(editItem, expr);
                    updatePlot();
                    updateVertexList();
                    return;
                default:
                    return;
            }
        }
    }

    private class EditFaceFieldLsnr implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            switch (editMode) {
                case EDIT_NONE:
                    return;
                case EDIT_FACE:
                    int[] face;
                    try {
                        face = parseFaceString(editFaceField.getText());
                    }
                        catch (NumberFormatException nfex) {
                            demo.showError("Faces must be of the form\n" +
                                           "<vertex number>, <vertex number>, ..., <vertex number>");
                            return;
                        }
                        for (int i = 0; i < face.length; ++i)
                            if (face[i] < 0 || face[i] >= myPlot.vertices().size()) {
                                if (myPlot.vertices().size() > 0)
                                    demo.showError("You can only use vertex numbers between 1 and "
                                                   + myPlot.vertices().size() + ".");
                                else
                                    demo.showError("There are no vertices to make the face from.");
                                return;
                            }
                                myPlot.setFace(editItem, face);
                        updatePlot();
                    updateFaceList();
                    return;
                default:
                    return;
            }
        }
    }

    private class OKBtnLsnr extends EditPlotWindow.OKBtnLsnr {
        public void actionPerformed(ActionEvent ev) {
            ((PlotPolyhedron) plot).setTitle(titleField.getText());
            updatePlot();
            super.actionPerformed(ev);
        }
    }
    
    
}