//
//  PlotIdentifierPanel.java
//  Demo
//
//  Created by David Eigen on Sat Apr 05 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.expr.ui;

import java.awt.*;
import java.awt.event.*;

import demo.io.*;
import demo.ui.*;
import demo.depend.*;
import demo.exec.*;
import demo.Demo;
import demo.ControlsFrame;
import demo.depend.HasDependentsException;
import demo.expr.ExprObject;
import demo.expr.ste.STEObject;
import demo.plot.Plot;

public class PlotIdentifierPanel extends Panel implements ItemListener, ActionListener, FileObject {

    private  Demo demo;
    private  STEObject entry;
    private  ControlsFrame controls;

    private  RemoveButton removeBtn;
    private  PlotChoice plotChoice;

    public PlotIdentifierPanel( STEObject entry, Plot plot, Demo demo, ControlsFrame controls ) {
        super();
        this .demo = demo;
        this .entry = entry;
        this .controls = controls;
        init();
    }

    private void init() {
        setLayout( new java.awt .FlowLayout( java.awt .FlowLayout .LEFT ) );
        entry.setUserEditable(true);
        add( new java.awt .Label(entry.name + " =") );
        add(plotChoice = new PlotChoice(demo));
        add( this .removeBtn = new RemoveButton() );
        plotChoice.select((Plot) entry.object());
        this.plotChoice.addItemListener(this);
        this.removeBtn.addActionListener(this);
    }

    public void actionPerformed( ActionEvent event ) {
        if ( event .getSource() == this .removeBtn ) {
            try {
                demo.removeSymbolTableEntry(this.entry);
                controls.removeSTEObject(this.entry);
            }
            catch (HasDependentsException ex) {
                Demo.showError("You cannot remove " + this.entry.name() + " because there are things dependent on it.\nIf you want to remove " + this.entry.name() + ", remove the dependencies first, and try again.");
            }
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.plotChoice) {
            final Plot plot = plotChoice.selectedPlot();
            final ExprObject oldPlot = this.entry.object();
            Exec.run(new ExecCallback() { public void invoke() {
                try {
                    PlotIdentifierPanel.this.entry.setObject(plot);
                    DependencyManager.updateDependentObjectsDefST(entry);
                }
                catch (CircularException ex) {
                    demo.showError("Circular dependency.");
                    PlotIdentifierPanel.this.entry.setObject(oldPlot);
                }
            }});
        }
    }


    // ****************************** FILE I/O ****************************** //
    String entry__, controls__;

    public PlotIdentifierPanel(Token tok, FileParser parser) {
        FileProperties props = parser.parseProperties(tok);
        entry__ = parser.parseWord(props.get("entry"));
        controls__ = parser.parseObject(props.get("ctrls"));
        demo = parser.demo();
    }

    public void loadFileBind(FileParser parser) {
        controls = (ControlsFrame) parser.getObject(controls__);
    }

    public void loadFileExprs(FileParser parser) {
        entry = (STEObject) parser.currEnvLookup(entry__);
    }

    public void loadFileFinish(FileParser parser) {
        init();
    }

    public Token saveFile(FileGenerator generator) {
        FileProperties props = new FileProperties();
        props.add("entry", generator.generateWord(entry.name));
        props.add("ctrls", generator.generateObjectID(controls));
        return generator.generateProperties(props);
    }



}


