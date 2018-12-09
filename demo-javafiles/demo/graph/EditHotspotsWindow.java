package demo.graph;

import java.awt.*;
import java.awt.event.*;
import mathbuild.Environment;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.Demo;
import demo.ui.*;
import demo.depend.*;
import demo.exec.*;
import demo.util.Set;
import demo.util.U;
import demo.expr.Expression;
import demo.expr.ste.STEConstant;
import demo.plot.Plot;

public class EditHotspotsWindow extends DemoFrame
implements ActionListener, ItemListener, WindowListener, Dependable {
    DependencyNode myDependencyNode_ = new DependencyNode(this);
    public DependencyNode dependencyNode() {return myDependencyNode_;}

    Demo demo;
    Environment environment;
    GraphCanvas3D canvas;
    Graph3D graph;
    int dimension;
    DialogListener listener;
    
    List hotspotsList = new List();
    java.util.Vector hotspotsVector = new java.util.Vector();
    
    Button okBtn = new Button("OK");
    Button addHotspotBtn = new Button("Add Hotspot");
    Button editHotspotBtn = new Button("Edit...");
    Button removeHotspotBtn = new Button("Remove");
    
    java.util.Dictionary openEditHotspotDialogs = new java.util.Hashtable(3); // key/value is type Hotspot
    
    public EditHotspotsWindow(DialogListener listener, GraphCanvas3D canvas, Graph3D graph, Demo demo, Environment env, int dimension) {
        super("Edit Hotspots");
        this.dimension = dimension;
        this.demo = demo;
        this.environment = env;
        this.graph = graph;
        this.canvas = canvas;
        this.listener = listener;
        java.util.Enumeration hotspotsEnum = canvas.hotspots();
        while ( hotspotsEnum.hasMoreElements() ) {
            Hotspot hs = (Hotspot) hotspotsEnum.nextElement();
            hotspotsVector.addElement(hs);
            DependencyManager.setDependency(this, hs);
        }
        this.addWindowListener(this);
        okBtn.addActionListener(this);
        editHotspotBtn.addActionListener(this);
        addHotspotBtn.addActionListener(this);
        removeHotspotBtn.addActionListener(this);
        hotspotsList.addActionListener(this);
        hotspotsList.addItemListener(this);
        hotspotsList.setMultipleMode(false);
        this.setLayout(new BorderLayout());
        this.add( hotspotsList,"Center");
        Panel okPanel = new Panel();
        okPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okPanel.add(okBtn);
        Panel btnsPanel = new Panel();
        btnsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnsPanel.add(removeHotspotBtn);
        btnsPanel.add(editHotspotBtn);
        btnsPanel.add(addHotspotBtn);
        editHotspotBtn.disable();
        removeHotspotBtn.disable();
        Panel southPanel = new Panel();
        southPanel.setLayout(new BorderLayout());
        southPanel.add( btnsPanel,"West");
        southPanel.add( new Label("        "),"Center");
        southPanel.add( okPanel,"East");
        this.add( southPanel,"South");
        makeList();
        this.setLocation(150,150);
        this.pack();
        this.setSize(size().width, size().height + 100);
        setVisible(true);
    }
    
    
    public void itemStateChanged( ItemEvent e ) {
        Object source = e.getSource();
        if (source == hotspotsList) {
            boolean enable = hotspotsList.getSelectedIndex() != -1;
            editHotspotBtn.setEnabled( enable );
            removeHotspotBtn.setEnabled( enable );
        }
    }
    
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if (source == hotspotsList || source == editHotspotBtn) {
            Hotspot hs = (Hotspot) hotspotsVector.elementAt(hotspotsList.getSelectedIndex());
            if ( openEditHotspotDialogs.get(hs) == null)
                new EditHotspotDialog(graph, environment, hs);
            else
                ((Window) openEditHotspotDialogs.get(hs)).toFront();
        }
        else if (source == addHotspotBtn) {
            new EditHotspotDialog(graph, environment);
        }
        else if (source == removeHotspotBtn) {
            removeHotspot( hotspotsList.getSelectedIndex() );
        }
        else if (source == okBtn) {
            listener.dialogOKed(this);
            setVisible(false);
            dispose();
        }
    }
    
    private void makeList() {
        Exec.begin_nocancel();
        int selectedIndex = hotspotsList.getSelectedIndex();
        hotspotsList.removeAll();
        for (int i = 0; i < hotspotsVector.size(); ++i) {
            Hotspot hs = (Hotspot) hotspotsVector.elementAt(i);
            hotspotsList.addItem( makeListItemStr(hs) );
        }
        if (selectedIndex != -1 && selectedIndex < hotspotsVector.size())
            hotspotsList.select(selectedIndex);
        boolean enableBtns = hotspotsList.getSelectedIndex() != -1;
        editHotspotBtn.setEnabled(enableBtns);
        removeHotspotBtn.setEnabled(enableBtns);
        Exec.end_nocancel();
    }

    private void addHotspot(Hotspot hs) {
        canvas.addHotspot(hs);
        hotspotsVector.addElement(hs);
        hotspotsList.addItem(makeListItemStr(hs));
        DependencyManager.setDependency(this, hs);
    }
    
    private void changeHotspot(Hotspot hs, Value val) {
        hs.setLocation(val);
        DependencyManager.updateDependentObjectsValMT(hs);
    }
    
    private void removeHotspot( int index ) {
        Hotspot hotspot = (Hotspot) hotspotsVector.elementAt(index);
        if (openEditHotspotDialogs.get(hotspot) != null)
            ((java.awt.Window) openEditHotspotDialogs.get(hotspot)).dispose();
        DependencyManager.removeDependency(this, hotspot);
        if ( demo.removeHotspot(hotspot, canvas) ) {
            // remove the hotspot
            hotspotsVector.removeElementAt(index);
            makeList();
        }
        else {
            DependencyManager.setDependency(this, hotspot);
        }
    }
    
    private String makeListItemStr( Hotspot hs ) {
        String name = hs.name();
        int numSpaces = 31 - name.length();
        String spaces = " ";
        for (int i = 1; i < numSpaces; ++i)
            spaces += " ";
        return name + spaces + hs.location().toString();
    }
    
    
    public void dispose() {
        DependencyManager.remove(this);
        super.dispose();
    }
    

    public void dependencyUpdateVal( Set updatingObjects ) {
        makeList();
    }
    public void dependencyUpdateDef( Set updatingObjects ) {
        makeList();
    }
    
    
    // WindowListener methods
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        listener.dialogOKed(this);
        setVisible(false);
        dispose();
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {} 
    
    
    
    
    private class EditHotspotDialog extends DemoFrame implements ActionListener, ItemListener, Dependable {
    
        Hotspot hotspot;
        Environment environment;
        boolean editing; // whether we're editing or creating a new hotspot

        int constraintMode; // the constraint mode we're in -- same as Hotspot modes
        
        TextField nameFld = new TextField(15);
        TextField pointFld = new TextField(30);
        Choice constraintChoice = new Choice();
        Panel constraintPanel = new Panel();
        TextField originFld = new TextField(20);
        TextField constraintFld = new TextField(20);
        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");
        Choice plotChoice = new Choice();
        java.util.Vector plots = new java.util.Vector();
        int currentPlotIndex = 0;

        private final Object PLOT_NONE = new Object(); // plot vector entry for no plot

        public EditHotspotDialog(Graph graph, Environment env) {
            this(graph, env, null);
        }
        
        public EditHotspotDialog(Graph graph, Environment env, Hotspot hs) {
            super(hs == null ? "Add Hotspot" : "Edit Hotspot " + hs.pointTableEntry().name());
            this.environment = env;
            if (hs == null) {
                editing = false;
                String originStr = "";
                for (int i = 0; i < dimension; ++i)
                    originStr += "0" + (i < dimension - 1 ? ", " : "");
                this.hotspot = new Hotspot(dimension,
                                           demo.recognizeExpression(originStr, environment));
            }
            else {
                editing = true;
                this.hotspot = hs;
            }
            plotChoice.add("None");
            plots.addElement(PLOT_NONE);
            plotChoice.select(0);
            for (java.util.Enumeration plotsEnum = graph.plots(); plotsEnum.hasMoreElements();) {
                Plot plot = (Plot) plotsEnum.nextElement();
                plotChoice.add(U.clampString(plot.title(), 40));
                plots.addElement(plot);
                if (hotspot.constraintObjects().contains(plot))
                    plotChoice.select(currentPlotIndex = plotChoice.getItemCount() - 1);
            }
            constraintChoice.add("None");
            constraintChoice.add("Expression");
            constraintChoice.add("Plot");
            constraintChoice.select(0);

            this.setLayout(new BorderLayout());
            Panel center = new Panel();
            center.setLayout(new FlowVerticalLayout(FlowVerticalLayout.LEFT));
            Panel namePanel = new Panel();
            namePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            namePanel.add(new Label("Name: "));
            if (!editing)
                namePanel.add(nameFld);
            else
                namePanel.add(new Label(hotspot.pointTableEntry().name()));
            Panel locPanel = new Panel();
            locPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            locPanel.add(new Label("Current Point: "));
            locPanel.add(pointFld);
            Panel constrPanel = new Panel();
            constrPanel.setLayout(new BorderLayout());
            Panel constrChoicePanel = new Panel();
            constrChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            constrChoicePanel.add(new Label("Constrain to "));
            constrChoicePanel.add(constraintChoice);
            constrPanel.add(constrChoicePanel, "North");
            constrPanel.add(new Label("            "), "West");
            constrPanel.add(new Label("  "), "East");
            constrPanel.add(constraintPanel, "Center");
            center.add(namePanel);
            center.add(locPanel);
            center.add(constrPanel);
            this.add(center, "North");

            originFld.setText( hotspot.originExpr().definitionString() );
            constraintFld.setText( hotspot.constraint().definitionString() );
            pointFld.setText( hotspot.pointTableEntry().value().toString() );
            originFld.addActionListener(this);
            constraintFld.addActionListener(this);
            pointFld.addActionListener(this);
            constraintChoice.addItemListener(this);
            plotChoice.addItemListener(this);

            Panel btnPanel = new Panel();
            btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.add(cancelBtn);
            btnPanel.add(okBtn);
            add(new Label());
            add(btnPanel, "South");
            okBtn.addActionListener(this);
            cancelBtn.addActionListener(this);
            pack();
            setConstraintMode(hotspot.constraintMode());
            
            openEditHotspotDialogs.put(hotspot, this);
            DependencyManager.setDependency(this, hotspot);
            setResizable(false);
            setVisible(true);
            setAnimateSetSize(true);
        }


        // returns success
        private boolean setOrigin() {
            Expression expr = Demo.recognizeExpression(originFld.getText(),
                                                       this.environment);
            if (expr == null)
                return false;
            if (!expr.returnsVector(dimension)) {
                Demo.showError("Origin must be a point with " + dimension + " coordinates.");
                return false;
            }
            try {
                this.hotspot.setOriginExpr(expr);
            }
            catch (CircularException ex) {
                demo.showError("Circular dependency.");
                return false;
            }
            return true;
        }

        // returns success
        private boolean setConstraint() {
            Expression expr = Demo.recognizeExpression(constraintFld.getText(),
                                this.environment.append(this.hotspot.constraintExprDefs()));
            if (expr == null)
                return false;
            if (!expr.returnsVector(dimension)) {
                Demo.showError("Resulting point must be a point with "+dimension+" coordinates.");
                return false;
            }
            try {
                this.hotspot.setConstraint(expr);
            }
            catch (CircularException ex) {
                demo.showError("Circular dependency.");
                return false;
            }
            return true;
        }

        // returns success
        private boolean setLocation() {
            Expression expr = Demo.recognizeExpression(pointFld.getText(),
                                                       this.environment);
            if (expr == null)
                return false;
            if (!expr.returnsVector(dimension)) {
                Demo.showError("Point must be a point with "+dimension+" coordinates.");
                return false;
            }
            this.hotspot.setLocationHotspotSpace(expr.evaluate());
            expr.dispose();
            return true;
        }

        // sets the constraint mode, including adjusting the window, etc
        private void setConstraintMode(int mode) {
            constraintChoice.select(mode);
            switch (mode) {
                case Hotspot.NO_CONSTRAINTS:
                    // no constraint
                    constraintPanel.removeAll();
                    hotspot.setConstraintMode(constraintMode = Hotspot.NO_CONSTRAINTS);
                    break;
                case Hotspot.CONSTRAIN_WITH_EXPR:
                    // expr constraint
                    constraintPanel.removeAll();
                    constraintPanel.setLayout(new GridLayout(0,2));
                    constraintPanel.add(new Label("Origin: "));
                    constraintPanel.add(originFld);
                    constraintPanel.add(new Label("Set Point to "));
                    constraintPanel.add(constraintFld);
                    hotspot.setConstraintMode(constraintMode = Hotspot.CONSTRAIN_WITH_EXPR);
                    break;
                case Hotspot.CONSTRAIN_TO_OBJS:
                    // constraint to a plot
                    constraintPanel.removeAll();
                    constraintPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                    constraintPanel.add(new Label("Constrain to plot: "));
                    constraintPanel.add(plotChoice);
                    hotspot.setConstraintMode(constraintMode = Hotspot.CONSTRAIN_TO_OBJS);
                    break;
            }
            pack();
            hotspot.setConstraintMode(mode);
            DependencyManager.updateDependentObjectsValMT(hotspot);
        }

        // returns success
        public boolean setConstraintPlot() {
            Object entry = plots.elementAt(plotChoice.getSelectedIndex());
            if (entry == PLOT_NONE) {
                hotspot.setConstraintObjects(new Set());
            }
            else {
                Plot plot = (Plot) entry;
                Set plots = new Set();
                plots.add(plot);
                try {
                    hotspot.setConstraintObjects(plots);
                }
                catch (CircularException ex) {
                    demo.showError("Circular dependency.");
                    plotChoice.select(currentPlotIndex);
                    return false;
                }
            }
            currentPlotIndex = plotChoice.getSelectedIndex();
            return true;
        }
        
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == constraintChoice) {
                setConstraintMode(constraintChoice.getSelectedIndex());
            }
            else if (e.getSource() == plotChoice) {
                if (setConstraintPlot())
                    DependencyManager.updateDependentObjectsValMT(hotspot);
            }
        }
        
        
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okBtn) {
                if (!setOrigin()) return;
                if (!setConstraint()) return;
                if (!setLocation()) return;
                if (!editing) {
                    STEConstant c = demo.makeConstant(nameFld.getText(),
                                                      this.hotspot.location(),
                                                      environment);
                    if (c == null)
                        return;
                    this.hotspot.setPoint(c);
                    addHotspot(hotspot);
                }
                setVisible(false);
                this.dispose();
                DependencyManager.updateDependentObjectsDefMT(this.hotspot);
            }
            else if (e.getSource() == cancelBtn) {
                setVisible(false);
                this.dispose();
            }
            else if (e.getSource() == constraintFld) {
                Exec.run(new ExecCallback() { public void invoke() {
                    if (setConstraint())
                        DependencyManager.updateDependentObjectsValST(EditHotspotDialog.this.hotspot);
                }});
            }
            else if (e.getSource() == originFld) {
                Exec.run(new ExecCallback() { public void invoke() {
                    if (setOrigin())
                        DependencyManager.updateDependentObjectsValMT(EditHotspotDialog.this.hotspot);
                }});
            }
            else if (e.getSource() == pointFld) {
                Exec.run(new ExecCallback() { public void invoke() {
                    if (setLocation())
                        DependencyManager.updateDependentObjectsValMT(EditHotspotDialog.this.hotspot);
                }});
            }
        }

        public void dependencyUpdateVal(Set updatingObjects) {
            Exec.begin_nocancel();
            pointFld.setText(hotspot.location().toString());
            constraintFld.setText(hotspot.constraint().definitionString());
            originFld.setText(hotspot.originExpr().definitionString());
            Exec.end_nocancel();
        }

        public void dependencyUpdateDef(Set updatingObjects) {
            dependencyUpdateVal(updatingObjects);
        }

        public void dispose() {
            if (hotspot != null)
                openEditHotspotDialogs.remove(hotspot);
            DependencyManager.remove(this);
            super.dispose();
        }

        
        // *** IMPLEMENTATION FOR DEPENDABLE *** //
        private DependencyNode __myDependencyNode__ = new DependencyNode(this);
        public DependencyNode dependencyNode() { return __myDependencyNode__; }
        
        
    }


    



}
