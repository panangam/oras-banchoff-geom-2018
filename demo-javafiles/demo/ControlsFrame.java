package demo;

import java.awt .*;
import java.awt.event .*;
import java.io .*;

import demo.exec.*;
import demo.io.*;
import demo.ui.*;
import demo.plot.ui.*;
import demo.expr.ste.*;
import demo.expr.ui.*;
import demo.depend.HasDependentsException;
import demo.util.Set;
import demo.expr.ExprObject;
import demo.expr.Expression;
import demo.plot.Plot;
import demo.graph.GraphFrame;

public class ControlsFrame extends DemoFrame implements FileObject, ComponentListener {

    private  Demo demo;

    private java.awt.Panel mainPanel = new ControlsMainPanel();

    private java.awt.ScrollPane scrollPane = new ControlsScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
    
    private MenuItem addFunctionMenuitem, addExpressionMenuitem, addIntervalMenuitem,
                                addPlotMenuitem, addPlotCheckboxMenuitem,
                                addVariableMenuitem, addReadoutMenuitem,
                                addGraph2DMenuitem, addGraph3DMenuitem,
                                displayTagMenuitem, readTagMenuitem,
                                loadFileMenuitem, saveFileMenuitem,
                                quitMenuitem, newDemoMenuItem;

    private MenuItem editVisibleMenuItem = new MenuItem("Show/Hide Items...");

    private PanelGroup constantsPanel, variablesPanel, intervalsPanel, functionsPanel, readoutsPanel, miscPanel;

    private UpdateStatusMenu updateStatusMenu;

    private Set functions_ = new Set(), variables_ = new Set(), intervals_ = new Set(), constants_ = new Set(), readouts_ = new Set(), miscObjects_ = new Set();

    private java.util.Dictionary steobjects_ = new java.util.Hashtable();

    public  ControlsFrame( Demo demo ) {
        super( "Controls" );

            this .demo = demo;
            // we want a stack of 4 panels, with different sizes.
            // we can do this by using two border layouts, with one layout nested inside of the first one's south part
            init();
    }

    private void init() {
        this.addWindowListener(new WindowListenerQuit());
        this.addComponentListener(this);
        setLayout( new GridLayout(1,1) );
        this.add(scrollPane);
        Panel scrollPanel = new Panel();
        scrollPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        scrollPane.add(scrollPanel);
        scrollPanel.add(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        java.awt .Panel southPanel = new java.awt .Panel();
        southPanel .setLayout( new java.awt .BorderLayout() );
        mainPanel .add( southPanel, "South" );
        constantsPanel = new PanelGroup();
        variablesPanel = new PanelGroup();
        intervalsPanel = new PanelGroup();
        functionsPanel = new PanelGroup();
        readoutsPanel = new PanelGroup();
        miscPanel = new PanelGroup();
        java.awt .Panel northPanel = new java.awt .Panel();
        northPanel .setLayout( new java.awt .BorderLayout() );
        mainPanel .add( northPanel,"North");
        northPanel.add( constantsPanel, "Center" );
        northPanel .add( variablesPanel, "South" );
        mainPanel .add( intervalsPanel, "Center" );
        southPanel .add( functionsPanel, "North" );
        southPanel .add( miscPanel, "Center" );
        southPanel .add( readoutsPanel, "South" );
        
        AWTMenu demoMenu = new AWTMenu( "Demo" );
        this .displayTagMenuitem = new MenuItem( "Show Applet Tag" );
        demoMenu .add( this .displayTagMenuitem );
        this .saveFileMenuitem = new MenuItem("Save As...");
        if (!demo.isApplet())
            demoMenu .add( this .saveFileMenuitem);
        this .loadFileMenuitem = new MenuItem("Open File...");
        if (!demo.isApplet())
            demoMenu.add( this .loadFileMenuitem );
        this .readTagMenuitem = new MenuItem( "Import Data..." );
        demoMenu .add( this .readTagMenuitem );
        demoMenu .add("-");
        this .newDemoMenuItem = new MenuItem( "New Demo" );
        demoMenu .add( this .newDemoMenuItem );
        this .quitMenuitem = new MenuItem( "Exit Demo" );
        demoMenu .add( this .quitMenuitem );
        
        AWTMenu controlsMenu = new AWTMenu( "Controls" );
        this .addFunctionMenuitem = new MenuItem( "Add New Function" );
        controlsMenu .add( this .addFunctionMenuitem );
        this .addIntervalMenuitem = new MenuItem( "Add New Interval" );
        controlsMenu .add( this .addIntervalMenuitem );
        this .addVariableMenuitem = new MenuItem( "Add New Variable" );
        controlsMenu .add( this .addVariableMenuitem );
        this .addExpressionMenuitem = new MenuItem( "Add New Expression" );
        controlsMenu .add( this .addExpressionMenuitem );
        this .addPlotMenuitem = new MenuItem( "Add Plot Identifier" );
        controlsMenu .add( this .addPlotMenuitem );
        this .addPlotCheckboxMenuitem  = new MenuItem( "Add Plot Checkbox");
        controlsMenu .add(this .addPlotCheckboxMenuitem);
        this .addReadoutMenuitem = new MenuItem( "Add New Readout" );
        controlsMenu .add( this .addReadoutMenuitem );
        controlsMenu .add( new MenuItem( "-" ) );
        controlsMenu .add( this .addGraph2DMenuitem = new MenuItem( "New 2D Graph" ) );
        controlsMenu .add( this .addGraph3DMenuitem = new MenuItem( "New 3D Graph" ) );
        controlsMenu .add(new MenuItem("-"));
        controlsMenu .add(editVisibleMenuItem);

        updateStatusMenu = new UpdateStatusMenu();

        AWTMenuBar bar = new AWTMenuBar();
        bar .add( demoMenu );
        bar .add( controlsMenu );
        bar .add( updateStatusMenu );
        setMenuBar( bar );

        setSize(300,200);
        setLocation(10,10);
        setAnimateSetSize(true);
    }
    
    public  boolean action( java.awt .Event event, Object object ) {
        if ( event .target == this .addFunctionMenuitem ) {
            new AddFunctionWindow( this, demo );
        }
        if ( event .target == this .addExpressionMenuitem ) {
            new AddExpressionWindow( this, demo );
        }
        else if ( event .target == this .addIntervalMenuitem ) {
            new AddIntervalWindow( this, demo );
        }
        else if ( event .target == this .addVariableMenuitem ) {
            new AddVariableWindow( this, demo );
        }
        else if ( event .target == this .addPlotMenuitem ) {
            new AddPlotIdentifierWindow( this, demo );
        }
        else if ( event .target == this .addPlotCheckboxMenuitem ) {
            PlotVisibleCheckbox cb = new PlotVisibleCheckbox("Visible");
            addPlotCheckbox(cb, new EditPlotCheckboxWindow(this, cb, demo));
        }
        else if ( event .target == this .addReadoutMenuitem ) {
            addReadout();
        }
        else if ( event .target == this .addGraph2DMenuitem ) {
            demo .addGraph2D();
        }
        else if ( event .target == this .addGraph3DMenuitem ) {
            demo .addGraph3D();
        }
        else if ( event .target == this .displayTagMenuitem ) {
            new TagWindow( demo .generateTag() );
        }
        else if ( event .target == this .quitMenuitem ) {
            this .demo .quit();
        }
        else if ( event .target == this .newDemoMenuItem ) {
            this .demo .newDemo();
        }
        else if ( event .target == this .readTagMenuitem ) {
            new ImportTagWindow( demo );
        }
        else if ( event .target == this .saveFileMenuitem ) {
            FileDialog dialog = new FileDialog(this, "Save Demo", FileDialog.SAVE);
            
            dialog.setFilenameFilter(new DemoFilenameFilter());
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                try {
                    File f = new File(dialog.getDirectory(), dialog.getFile());
                    FileWriter w = new FileWriter(f);
                    String str = demo.generateFile();
                    w.write(str, 0, str.length());
                    w.close();
                } catch (IOException ex) { demo.showError("An error occured while saving."); }
            }
        }
        else if ( event .target == this .loadFileMenuitem ) {
            FileDialog dialog = new FileDialog(this, "Load Demo", FileDialog.LOAD);
            dialog.setFilenameFilter(new DemoFilenameFilter());
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                try {
                    File f = new File(dialog.getDirectory(), dialog.getFile());
                    FileReader r = new FileReader(f);
                    String str = "";
                    int c;
                    while ((c = r.read()) != -1)
                        str += (char) c;
                    r.close();
                    demo.loadFile(str);
                } catch (IOException ex) { demo.showError("An error occured while loading."); }
            }
        }
        else if ( event .target == this .editVisibleMenuItem ) {
            editVisibleMenuItem.setEnabled(false);
            Set items = new Set(); // items to show
            Set userEntries = new Set(); // STEs that we can show/hide
            for (java.util.Enumeration entries = demo.environment().localEntries();
                 entries.hasMoreElements();) {
                SymbolTableEntry entry = (SymbolTableEntry) entries.nextElement();
                if (entry.isUserEditable())
                    userEntries.add(entry);
            }
            items.addObjects(userEntries.elements());
            items.addObjects(readouts_.elements());
            items.addObjects(demo.allGraphFrames());
            EditVisibleWindow win =
                new EditVisibleWindow(items.elements(), new VisibleCallback());
            addOpenDialog(win);
            win.setVisible(true);
        }
        else {
            return super .action( event, object );
        }
        return true;
    }

    public void addFunction( STEFunction item ) {
        FunctionPanel panel = new FunctionPanel( item, demo, this );
        functionsPanel .insert( item.name(), panel );
        functions_.put(item.name());
        pack();
    }
    
    public void addExpression( STEExpression item ) {
        ExpressionPanel panel = new ExpressionPanel( item, demo, this );
        constantsPanel .insert( item.name(), panel );
        constants_.put(item.name());
        pack();
    }

    public void addInterval( STEInterval item ) {
        IntervalPanel panel = new IntervalPanel( item, demo, this );
        intervalsPanel .insert( item.name(), panel );
        intervals_.put(item.name());
        pack();
    }

    public VariablePanel addVariable( STEVariable entry ) {
        VariablePanel panel = new VariablePanel( entry, demo, this );
        variablesPanel .insert( entry.name(), panel );
        variables_.put(entry.name());
        pack();
        if (size().width < panel.size().width)
            setSize(panel.size().width, size().height);
        return panel;
    }

    public Component addSTEObject( STEObject entry ) {
        ExprObject obj = entry.object();
        Component comp;
        if (obj instanceof Plot)
            comp = new PlotIdentifierPanel(entry, (Plot) obj, demo, this);
        else throw new RuntimeException("Unknown object type in STEObject");
        miscPanel.add(comp);
        steobjects_.put(entry, comp);
        pack();
        return comp;
    }

    public ReadoutPanel addReadout() {
        return addReadout( new ReadoutPanel(demo, this) );
    }

    public ReadoutPanel addReadout(ReadoutPanel panel) {
        panel.setControls(this);
        readoutsPanel .add( panel );
        readouts_.put(panel);
        pack();
        return panel;
    }
    
    public void addPlotCheckbox( PlotVisibleCheckbox checkbox,
                                 EditPlotCheckboxWindow openEditWindow ) {
        if (checkbox.getLabel().equals("Visible")) 
            miscPanel.add(checkbox.getLabel(),
                          new PlotVisibleCheckboxPanel(this, checkbox, demo, openEditWindow));
        else
            miscPanel.insert(checkbox.getLabel(),
                             new PlotVisibleCheckboxPanel(this, checkbox, demo, openEditWindow));
        miscObjects_.put(checkbox);
        pack();
    }
    
    // used when we don't want to sort alphabetically -- this just puts the thing last
    public void addMiscComponent( Component comp ) {
        miscPanel.add(comp);
        miscObjects_.put(comp);
        pack();
    }
    
    public void addMiscComponent( String name, Component comp ) {
        miscPanel.add(name, comp);
        miscObjects_.put(comp);
        pack();
    }

    public void removeFunction( STEFunction item ) {
        functionsPanel .remove( item.name() );
        functions_.remove(item.name());
        pack();
    }

    public void removeExpression( STEExpression item ) {
        constantsPanel .remove( item.name() );
        constants_.remove(item.name());
        pack();
    }

    public void removeInterval( STEInterval item ) {
        intervalsPanel .remove( item.name() );
        intervals_.remove(item.name());
        pack();
    }

    public void removeVariable( STEVariable entry ) {
        variablesPanel .remove( entry.name() );
        variables_.remove(entry.name());
        pack();
    }

    public void removeSTEObject( STEObject entry ) {
        Component component = (Component) steobjects_.get(entry);
        if (component == null)
            return;
        miscPanel.remove(component);
        steobjects_.remove(entry);
        pack();
    }

    public void removeReadout(ReadoutPanel panel) {
        readoutsPanel .remove( panel );
        readouts_.remove(panel);
        pack();
    }

    public void removePlotCheckbox( PlotVisibleCheckbox checkbox, PlotVisibleCheckboxPanel pnl ) {
        miscPanel.remove(pnl);
        miscObjects_.remove(checkbox);
        pack();
    }
    
    public void removeMiscComponent( Component comp ) {
        miscPanel.remove(comp);
        miscObjects_.remove(comp);
        pack();
    }
    
    public java.util.Enumeration plotCheckboxes() {
        java.util.Vector checkboxes = new java.util.Vector();
        Component[] comps = miscPanel.getComponents();
        for (int i = 0; i < comps.length; ++i) {
            if (comps[i] instanceof PlotVisibleCheckbox)
                checkboxes.addElement(comps[i]);
        }
        return checkboxes.elements();
    }
    
    private  void organize() {
        validate();
        int maxwidth = 0, height = 0;
        java.awt .Component[] components = getComponents();
        for ( int i = 0; i < components .length; ++i ) {
            if ( components[i] .size() .width > maxwidth ) {
                maxwidth = components[i] .size() .width;
            }
            height += components[i] .size() .height;
        }
        setSize( maxwidth, height + 10 );
        validate();
    }


    public void componentHidden(ComponentEvent e) {
    }
    public void componentMoved(ComponentEvent e) {
    }
    public void componentResized(ComponentEvent e) {
        if (e.getComponent() == this) {
            int w = getSize().width - getInsets().right - getInsets().left;
            int h = getSize().height - getInsets().top - getInsets().bottom;
            scrollPane.setSize(w,h);
        }
    }
    public void componentShown(ComponentEvent e) {
    }

    

    // ****************************** FILE I/O ****************************** //
    private String[] readouts__, objects__;
    private String[] funcs__, vars__, intervals__, consts__, steobjects__;
    
    public ControlsFrame(Token tok, FileParser parser) {
        this.demo = parser.demo();
        init();
        FileProperties props = parser.parseProperties(tok);
        funcs__ = parser.parseWordList(props.get("functions"));
        vars__ = parser.parseWordList(props.get("variables"));
        intervals__ = parser.parseWordList(props.get("intervals"));
        consts__ = parser.parseWordList(props.get("constants"));
        readouts__ = parser.parseObjectList(props.get("readouts"));
        objects__ = parser.parseObjectList(props.get("objects"));
        if (props.contains("objidents"))
            steobjects__ = parser.parseWordList(props.get("objidents"));
        else
            steobjects__ = new String[0];
    }

    public void loadFileBind(FileParser parser) {
        for (int i = 0; i < readouts__.length; ++i)
            addReadout((ReadoutPanel) parser.getObject(readouts__[i]));
        for (int i = 0; i < objects__.length; ++i) {
            Object obj = parser.getObject(objects__[i]);
            if (obj instanceof PlotVisibleCheckbox)
                addPlotCheckbox((PlotVisibleCheckbox) obj, null);
            else
                addMiscComponent((Component) obj);
        }
        readouts__ = objects__ = null;
    }

    public void loadFileExprs(FileParser parser) {
        for (int i = 0; i < funcs__.length; ++i)
            addFunction((STEFunction) parser.currEnvLookup(funcs__[i]));
        for (int i = 0; i < vars__.length; ++i)
            addVariable((STEVariable) parser.currEnvLookup(vars__[i]));
        for (int i = 0; i < intervals__.length; ++i)
            addInterval((STEInterval) parser.currEnvLookup(intervals__[i]));
        for (int i = 0; i < consts__.length; ++i)
            addExpression((STEExpression) parser.currEnvLookup(consts__[i]));
        for (int i = 0; i < steobjects__.length; ++i)
            addSTEObject((STEObject) parser.currEnvLookup(steobjects__[i]));
        parser.loadExprs(readouts_.elements());
        parser.loadExprs(miscObjects_.elements());
    }

    public void loadFileFinish(FileParser parser) {
        this.setVisible(true);
    }
    
    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("functions", generator.generateWordList(functions_.elements()));
        props.add("variables", generator.generateWordList(variables_.elements()));
        props.add("intervals", generator.generateWordList(intervals_.elements()));
        props.add("constants", generator.generateWordList(constants_.elements()));
        props.add("readouts", generator.generateObjectList(readouts_.elements()));
        props.add("objects", generator.generateObjectList(miscObjects_.elements()));
        if (steobjects_.size() > 0) {
            String[] strs = new String[steobjects_.size()];
            int i = 0;
            for (java.util.Enumeration steobjs = steobjects_.keys();
                 steobjs.hasMoreElements();)
                strs[i++] = ((STEObject) steobjs.nextElement()).name();
            props.add("objidents", generator.generateWordList(strs));
        }
        return generator.generateProperties(props);
    }







    private class WindowListenerQuit extends WindowListenerNoActions {
        public void windowClosing(WindowEvent e) {
            demo.quit();
        }
    }

    private class DemoFilenameFilter implements FilenameFilter {
        public boolean accept(File f, String name) {
            return name.endsWith(".demo");
        }
    }

    private class VisibleCallback implements EditVisibleWindowCallback {

        public boolean isObjectVisible(Object obj) {
            if (obj instanceof STEFunction)
                return functions_.contains(((STEFunction) obj).name());
            if (obj instanceof STEVariable)
                return variables_.contains(((STEVariable) obj).name());
            if (obj instanceof STEExpression)
                return constants_.contains(((STEExpression) obj).name());
            if (obj instanceof STEInterval)
                return intervals_.contains(((STEInterval) obj).name());
            if (obj instanceof STEObject)
                return steobjects_.get(obj) != null;
            if (obj instanceof ReadoutPanel)
                return readouts_.contains(obj);
            if (obj instanceof GraphFrame)
                return ((GraphFrame) obj).isVisible();
            return miscObjects_.contains(obj);
        }

        public void setObjectVisible(Object obj, boolean vis) {
            if (vis) {
                // add object
                if (obj instanceof STEFunction)
                    addFunction((STEFunction) obj);
                else if (obj instanceof STEVariable)
                    addVariable((STEVariable) obj);
                else if (obj instanceof STEExpression)
                    addExpression((STEExpression) obj);
                else if (obj instanceof STEInterval)
                    addInterval((STEInterval) obj);
                else if (obj instanceof STEObject)
                    addSTEObject((STEObject) obj);
                else if (obj instanceof ReadoutPanel)
                    addReadout((ReadoutPanel) obj);
                else if (obj instanceof GraphFrame)
                    ((GraphFrame) obj).setVisible(true);
                else if (obj instanceof Component)
                    addMiscComponent((Component) obj);
            }
            else {
                // remove object
                if (obj instanceof STEFunction)
                    removeFunction((STEFunction) obj);
                else if (obj instanceof STEVariable)
                    removeVariable((STEVariable) obj);
                else if (obj instanceof STEExpression)
                    removeExpression((STEExpression) obj);
                else if (obj instanceof STEInterval)
                    removeInterval((STEInterval) obj);
                else if (obj instanceof STEObject)
                    removeSTEObject((STEObject) obj);
                else if (obj instanceof ReadoutPanel)
                    removeReadout((ReadoutPanel) obj);
                else if (obj instanceof GraphFrame)
                    ((GraphFrame) obj).setVisible(false);
                else if (obj instanceof Component)
                    removeMiscComponent((Component) obj);
            }
        }

        private String padTitle(String title) {
            String str = new String(title);
            for (int i = title.length(); i < 15; ++i)
                str += ' ';
            return str;
        }

        public String getTitle(Object obj) {
            if (obj instanceof STEFunction)
                return padTitle(((SymbolTableEntry) obj).name()) + "  [function]  ";
            if (obj instanceof STEVariable)
                return padTitle(((SymbolTableEntry) obj).name()) + "  [variable]  ";
            if (obj instanceof STEExpression)
                return padTitle(((SymbolTableEntry) obj).name()) + "  [constant]  ";
            if (obj instanceof STEInterval)
                return padTitle(((SymbolTableEntry) obj).name()) + "  [interval]  ";
            if (obj instanceof ReadoutPanel)
                return padTitle(((ReadoutPanel) obj).expressionString()) + "  [readout]   ";
            if (obj instanceof GraphFrame)
                return padTitle(((GraphFrame) obj).getTitle()) + "  [graph]     ";
            if (obj instanceof SymbolTableEntry)
                return padTitle(((SymbolTableEntry) obj).name()) + "  [identifier]";
            System.out.println("Misc. Obj is: " + obj);
            return padTitle("MiscObj") + "  [????]    ";
        }

        public void editVisibleWindowClosed(EditVisibleWindow win) {
            editVisibleMenuItem.setEnabled(true);
            removeOpenDialog(win);
        }
    }
    
    

}



class ControlsMainPanel extends Panel {
    public Dimension getMinimumSize() {
        return new Dimension(250, 100);
    }
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Dimension min = getMinimumSize();
        return new Dimension(d.width > min.width ? d.width : min.width,
                             d.height > min.height ? d.height : min.height);
    }
}


class ControlsScrollPane extends ScrollPane {
    public ControlsScrollPane(int value) {
        super(value);
    }
    public Dimension getPreferredSize() {
        Dimension d = getComponents()[0].getPreferredSize();
        // add 5 pixels so the scroll bars definitely don't think they should be on
        return new Dimension(d.width + 5, d.height + 5);
    }
}





abstract class AddSymbolWindow extends DemoFrame implements ActionListener {

    protected  ControlsFrame controlsFrame;

    private  java.awt .TextField specField = new java.awt .TextField( 30 );

    private  java.awt .Button okButton, cancelButton;

    protected  Demo demo;

    public AddSymbolWindow( String title, String message, ControlsFrame controlsFrame, Demo demo ) {
        super( title );

            this .demo = demo;
            this .controlsFrame = controlsFrame;
            setLayout( new java.awt .BorderLayout() );
            setSize( 500, 150 );
            add( new java.awt .Label( message ), "North" );
            java.awt.Panel fldPanel = new Panel();
            fldPanel.setLayout(new GridLayout(1,1));
            fldPanel.add(specField);
            add( fldPanel, "Center" );
            java.awt .Panel southPanel = new java.awt .Panel();
            southPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
            southPanel .add( cancelButton = new java.awt .Button( "Cancel" ) );
            southPanel .add( okButton = new java.awt .Button( "OK" ) );
            add( southPanel, "South" );
            this.addWindowListener(new WindowListenerDispose(this));
            specField.addActionListener(this);
            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
            setLocation(80,80);
            setVisible(true);
        }

    
    protected abstract Object addSymbol(String definition) ;

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == specField || source == okButton) {
            Object success = addSymbol( specField .getText() );
            if ( success != null ) {
                setVisible(false);
                dispatchEvent(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, "OK"));
            }
        }
        else if (source == cancelButton) {
            setVisible(false);
            dispose();
        }
    }

}



class AddFunctionWindow extends AddSymbolWindow {

    public AddFunctionWindow( ControlsFrame controlsFrame, Demo demo ) {
        super( "Add Function", "Enter a function in the form name(variables) = definition:",
                controlsFrame, demo );
        }

    protected Object addSymbol(String definition) {
        return demo.addFunction(definition);
    }

}



class AddExpressionWindow extends AddSymbolWindow {

    public AddExpressionWindow( ControlsFrame controlsFrame, Demo demo ) {
        super( "Add Constant", "Enter a constant in the form name = definition:",
                controlsFrame, demo );
        }

    protected Object addSymbol(String definition) {
        return demo.addExpression(definition);
    }

}



class AddIntervalWindow extends AddSymbolWindow {

    public AddIntervalWindow( ControlsFrame controlsFrame, Demo demo ) {
        super( "Add Interval", "Enter an interval in the form name = start, end, resolution:",
                controlsFrame, demo);
    }
    
    protected Object addSymbol(String definition) {
        return demo.addInterval(definition);
    }

}



class AddVariableWindow extends AddSymbolWindow {

    public AddVariableWindow( ControlsFrame controlsFrame, Demo demo ) {
        super( "Add Variable", "Enter a variable in the form name = minimum, maximum, steps:",
                controlsFrame, demo);
    }

    protected Object addSymbol(String definition) {
        return demo.addVariable(definition);
    }

}



class AddPlotIdentifierWindow extends DemoFrame implements ActionListener {

    private  ControlsFrame controlsFrame;
    private  Demo demo;

    private  Button okButton, cancelButton;
    private  TextField nameFld = new TextField(30);
    private  PlotChoice plotChoice;

    public AddPlotIdentifierWindow( ControlsFrame controlsFrame, Demo demo ) {
        super( "Add Plot Identifier" );

        this .demo = demo;
        this .controlsFrame = controlsFrame;
        setLayout( new java.awt .BorderLayout() );

        plotChoice = new PlotChoice(demo);
        
        Panel northPanel = new Panel();
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        northPanel.add(new Label("Name: "));
        northPanel.add(nameFld);
        add(northPanel, "North");
        
        Panel centerPanel = new Panel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(new Label("Plot: " ));
        centerPanel.add(plotChoice);
        add(centerPanel, "Center");
        
        Panel southPanel = new Panel();
        southPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
        southPanel .add( cancelButton = new java.awt .Button( "Cancel" ) );
        southPanel .add( okButton = new java.awt .Button( " OK " ) );
        add( southPanel, "South" );

        this.addWindowListener(new WindowListenerDispose(this));
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        pack();
        setLocation(80,80);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == okButton) {
            STEObject entry = new STEObject(nameFld.getText(), plotChoice.selectedPlot());
            boolean success = demo.addEnvironmentBinding(nameFld.getText(), entry);
            if ( success ) {
                setVisible(false);
                controlsFrame.addSTEObject(entry);
                dispose();
            }
        }
        else if (source == cancelButton) {
            setVisible(false);
            dispose();
        }
    }
    
}


class PlotVisibleCheckboxPanel extends Panel implements ActionListener {
    private ControlsFrame controls;
    private PlotVisibleCheckbox checkbox;
    private Demo demo;
    private RemoveButton removeBtn = new RemoveButton();
    private LittleButton editBtn = new LittleButton("e");
    private Window editWin_ = null;
    public PlotVisibleCheckboxPanel(ControlsFrame controls, PlotVisibleCheckbox cb, Demo demo,
                                    EditPlotCheckboxWindow initialEditWindow) {
        this(controls, cb, demo);
        if (initialEditWindow != null) {
            editWin_ = initialEditWindow;
            editWin_.addWindowListener(new EditWinListener());
        }
    }
    public PlotVisibleCheckboxPanel(ControlsFrame controls, PlotVisibleCheckbox cb, Demo demo) {
        this.controls = controls;
        this.checkbox = cb;
        this.demo = demo;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(checkbox);
        add(new Label("     "));
        add(editBtn);
        add(removeBtn);
        editBtn.addActionListener(this);
        removeBtn.addActionListener(this);
    }
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == removeBtn) {
            checkbox.dispose();
            controls.removePlotCheckbox(checkbox, this);
            if (editWin_ != null)
                editWin_.dispose();
        }
        else if (e.getSource() == editBtn) {
            if (editWin_ != null) {
                // have a window already open: bring it to the front
                editWin_.toFront();
                editWin_.requestFocus();
            }
            else {
                // don't have a window open yet, so create one
                editWin_ = new EditPlotCheckboxWindow(controls, checkbox, demo);
                editWin_.addWindowListener(new EditWinListener());
            }
        }
    }
    private class EditWinListener extends WindowListenerNoActions {
        public void windowClosed(WindowEvent e) {
            editWin_ = null;
        }
    }
}


class UpdateStatusMenu extends AWTMenu implements demo.exec.ExecStatusListener, ActionListener {

    private static final String UPDATE_STRING   = "Status:  Updating";
    private static final String NOUPDATE_STRING = "Status:  Idle    ";
    private MenuItem statusMenuItem_ = new MenuItem();
    private MenuItem breakMenuItem_ = new MenuItem("Stop Execution");
    
    public UpdateStatusMenu() {
        super("Execution");
        breakMenuItem_.addActionListener(this);
        statusMenuItem_.setLabel(Exec.getStatus() ? UPDATE_STRING : NOUPDATE_STRING);
        statusMenuItem_.setEnabled(false);
        Exec.addStatusListener(this);
        this.add(statusMenuItem_);
        this.add(breakMenuItem_);
    }

    /**
     * Sets the status to show in this panel.
     * @param updating whether to show updating is happening or not
     */
    public void execStatusChanged(boolean updating) {
        statusMenuItem_.setLabel(updating ? UPDATE_STRING : NOUPDATE_STRING);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == breakMenuItem_)
            Exec.breakExecution();
    }

}




class PanelGroup extends Panel{

    public  PanelGroup() {
        super();

            this .setLayout( new GridLayout( 0, 1 ) );
        }

    private java.util .Vector names = new java.util .Vector();
     
    // this method puts comp last. does not sort alphabetically
    public void insert( Component comp ) {
        this.add(comp);
    }

    // inserts comp, alphabetically by name. comp is before all comps whose name is not given.
    public  void insert( String name, Component comp ) {
        int index = 0;
        while ( index < names .size() && ((String) names .elementAt( index )) .compareTo( name ) <= 0 ) {
            index++;
        }
        names .insertElementAt( name, index );
        this .add( comp, index );
    }

    public void remove( String name ) {
        int index = 0;
        while ( index < names .size() && !((String) names .elementAt( index )) .equals( name ) ) {
            index++;
        }
        if (index < names.size()) {
            names.removeElementAt(index);
            super.remove(index);
        }
    }

    public void remove( Component comp ) {
        super.remove(comp);
    }

    
}

class FunctionPanel extends Panel implements ActionListener {

    private  STEFunction entry;
    
    private  Demo demo;
    private  ControlsFrame controls;

    private  java.awt .TextField definitionField;
    private  RemoveButton removeBtn;

    public FunctionPanel( STEFunction entry, Demo demo, ControlsFrame controls ) {
        super();
        this.controls = controls;
        this.entry = entry;
        this.demo = demo;
        String variablesStr = "";
        String[] variablesStrings = ((STEFunction) entry).paramNames();
        for ( int i = 0; i < variablesStrings .length; i++ ) {
            variablesStr += variablesStrings[i];
            if ( i < variablesStrings .length - 1 ) {
                variablesStr += ", ";
            }
        }

        setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
        add( new java.awt .Label(entry.name() + (variablesStr.length() > 0 ? "(" + variablesStr + ") =" : " =")) );
        add( this .definitionField = new java.awt .TextField( entry.bodyDefinition(), 30 ) );
        add( this .removeBtn = new RemoveButton() );
        this.definitionField.addActionListener(this);
        this.removeBtn.addActionListener(this);
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event .getSource() == this .definitionField ) {
            this .demo .changeFunction( entry, this .definitionField .getText() );
        }
        else if ( event .getSource() == this .removeBtn ) {
            try {
                demo.removeSymbolTableEntry(this.entry);
                controls.removeFunction(this.entry);
            }
            catch (HasDependentsException ex) {
                Demo.showError("You cannot remove " + this.entry.name() + " because there are things dependent on it.\nIf you want to remove " + this.entry.name() + ", remove the dependencies first, and try again.");
            }
        }
    }
    

}




class ExpressionPanel extends Panel implements ActionListener {

    private  STEExpression entry;

    private  Demo demo;
    private  ControlsFrame controls;

    private  java.awt .TextField definitionField;
    private  RemoveButton removeBtn;

    public ExpressionPanel( STEExpression entry, Demo demo, ControlsFrame controls ) {
        super();

            setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
            this .entry = entry;
            this .demo = demo;
            this .controls = controls;
            add( new java.awt .Label(entry.name() + " =") );
            add( this .definitionField = new java.awt .TextField( entry.expressionDef(), 30 ) );
            add( this .removeBtn = new RemoveButton() );
            this .definitionField .addActionListener(this);
            this .removeBtn .addActionListener(this);
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event .getSource() == this .definitionField ) {
            this .demo .changeExpression( entry, this .definitionField .getText() );
        }
        else if ( event .getSource() == this .removeBtn ) {
            try {
                demo.removeSymbolTableEntry(this.entry);
                controls.removeExpression(this.entry);
            }
            catch (HasDependentsException ex) {
                Demo.showError("You cannot remove " + this.entry.name() + " because there are things dependent on it.\nIf you want to remove " + this.entry.name() + ", remove the dependencies first, and try again.");
            }
        }
    }


}





class IntervalPanel extends Panel implements ActionListener {

    private  STEInterval interval;

    private  Demo demo;
    private  ControlsFrame controls;

    private java.awt .TextField startField, endField, resolutionField;

    private RemoveButton removeBtn;

    public IntervalPanel( STEInterval entry, Demo demo, ControlsFrame controls ) {
        super();
        this .demo = demo;
        this .controls = controls;
        this .interval = entry;
        setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
        add( new java.awt .Label( entry.name() + " from" ) );
        add( this .startField = new java.awt .TextField( entry.minStr(), 10 ) );
        add( new java.awt .Label( "to" ) );
        add( this .endField = new java.awt .TextField( entry.maxStr(), 10 ) );
        add( new java.awt .Label( "in" ) );
        add( this .resolutionField = new java.awt .TextField( entry.resStr(), 5 ) );
        add( new java.awt .Label( "steps" ) );
        add( removeBtn = new RemoveButton() );
        this.startField.addActionListener(this);
        this.endField.addActionListener(this);
        this.resolutionField.addActionListener(this);
        this.removeBtn.addActionListener(this);
    }

    public void actionPerformed( ActionEvent event ) {
        if (event .getSource() == this .startField ||
            event .getSource() == this .endField ||
            event .getSource() == this .resolutionField) {
            this.demo.changeInterval(this.interval,
                                     this.startField.getText(),
                                     this.endField.getText(),
                                     this.resolutionField.getText());
        }
        else if ( event .getSource() == this .removeBtn ) {
            try {
                demo.removeSymbolTableEntry(this.interval);
                controls.removeInterval(this.interval);
            }
            catch (HasDependentsException ex) {
                Demo.showError("You cannot remove " + this.interval.name() + " because there are things dependent on it.\nIf you want to remove " + this.interval.name() + ", remove the dependencies first, and try again.");
            }
        }
    }
    

}





class TagWindow extends DemoFrame{

    private  java.awt .Button closeButton;

    public  TagWindow( String text ) {
        super( "Applet Tag" );

            setLayout( new java.awt .BorderLayout() );
            java.awt .TextArea area = new java.awt .TextArea( text, 24, 60 );
            area .setEditable( false );
            add( new java.awt .Label( "This HTML fragment will bring up this demo" + " in its current state:" ), "North" );
            add( area, "Center" );
            add( this .closeButton = new java.awt .Button( "Close" ), "South" );
            pack();
            setLocation(80,80);
            setVisible(true);
            area.selectAll();
    }

    public boolean action( java.awt .Event event, Object object ) {
        if ( event .target == this .closeButton ) {
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .action( event, object );
        }
    }

    public  boolean handleEvent( java.awt .Event event ) {
        if ( event .id == java.awt .Event .WINDOW_DESTROY ) {
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .handleEvent( event );
        }
    }


}



class ImportTagWindow extends DemoFrame{

    private  Demo demo;

    private  java.awt .Button okBtn;

    private  java.awt .TextArea tagArea;

    public  ImportTagWindow( Demo demo ) {
        super( "Import Data" );

            this .demo = demo;
            setLayout( new java.awt .BorderLayout() );
            tagArea = new java.awt .TextArea();
            tagArea .setEditable( true );
            add( new java.awt .Label( "Paste or write in the DATA code here:" ), "North" );
            add( tagArea, "Center" );
            add( this .okBtn = new java.awt .Button( "OK" ), "South" );
            this .setSize( 300, 300 );
            setLocation(80,80);
            setVisible(true);
    }

    public boolean action( java.awt .Event event, Object object ) {
        if ( event .target == this .okBtn ) {
            // recognize data tag, and close window if successful
            if ( demo .loadFile( tagArea .getText() ) ) {
                setVisible(false);
                dispose();
            }
            return true;
        }
        else {
            return super .action( event, object );
        }
    }

    public  boolean handleEvent( java.awt .Event event ) {
        if ( event .id == java.awt .Event .WINDOW_DESTROY ) {
            setVisible(false);
            dispose();
            return true;
        }
        else {
            return super .handleEvent( event );
        }
    }


}


