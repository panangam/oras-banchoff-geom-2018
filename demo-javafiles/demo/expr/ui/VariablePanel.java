package demo.expr.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import mathbuild.value.*;

import demo.Demo;
import demo.ControlsFrame;
import demo.depend.HasDependentsException;
import demo.expr.ste.STEVariable;
import demo.util.*;
import demo.ui.*;
import demo.depend.*;
import demo.exec.*;

public class VariablePanel extends Panel implements Dependable, ActionListener {

    private  String name, start, end, resolution;

    private  String current;

    private  Demo demo;
    private  ControlsFrame controls;

    private 
    java.awt .TextField startField, endField, resolutionField;

    private RemoveButton removeBtn;

    private  TapeDeck deck;

    private  STEVariable varEntry;

    private DependencyNode myDependencyNode_ = new DependencyNode(this);

    public 
    VariablePanel( STEVariable entry, Demo demo, ControlsFrame controls ) {
        super();
        this .name = entry.name();
        this .start = entry.minExpr().definitionString();
        this .end = entry.maxExpr().definitionString();
        this .resolution = entry.resExpr().definitionString();
        this .demo = demo;
        this .controls = controls;
        this .varEntry = entry;
        DependencyManager.setDependency(this, entry);

        setLayout( new FlowLayout(FlowLayout.LEFT) );
        Panel defsPanel = new Panel();
        defsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        defsPanel.add( new java.awt .Label( name + " from" ) );
        defsPanel.add( this .startField = new java.awt .TextField( start, 10 ) );
        defsPanel.add( new java.awt .Label( "to" ) );
        defsPanel.add( this .endField = new java.awt .TextField( end, 10 ) );
        defsPanel.add( new java.awt .Label( "in" ) );
        defsPanel.add( this .resolutionField = new java.awt .TextField( resolution, 3 ) );
        defsPanel.add( new java.awt .Label( "steps" ) );
        Panel animPanel = new Panel();
        animPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        animPanel.add( this .deck = new TapeDeck( this ) );
        Panel removeBtnPanel = new Panel();
        removeBtnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        removeBtnPanel.add(this.removeBtn = new RemoveButton());

        this.add(defsPanel);
        this.add(animPanel);
        this.add(removeBtnPanel);

        removeBtn.addActionListener(this);
        startField.addActionListener(this);
        endField.addActionListener(this);
        resolutionField.addActionListener(this);
        
        setResolution();
        // init current to the current value of the entry
        setCurrentValue( ((ValueScalar) entry .value()).number() );
        this .deck .readout( current );
    }

    public 
    void dependencyUpdateVal( Set updatingObjects ) {
        setCurrentValue( ((ValueScalar) varEntry .value()).number() );
        readout();
    }

    public
    void dependencyUpdateDef( Set updatingObjects ) {
        dependencyUpdateVal(updatingObjects);
    }

    public  void setVariableValue( String value ) {
        try  {
            setVariableValue( new Double( value ) .doubleValue() );
        } catch( NumberFormatException ex ) {
            demo .showError( "That is not a valid number." );
        }
    }

    public  void setVariableValue( double value ) {
        // demo.setVariableValue(this.varEntry, value);
        varEntry .setCurrent( value );
        DependencyManager.updateDependentObjectsValMT(varEntry);
    }

    public  void nowAt( int step ) {
        // set variable's value for current step
        double min = varEntry .min() .number();
        final double current = (varEntry .max().number() - min) / varEntry .resolution().number() * step + min;
        Exec.run(new ExecCallback(){
            public void invoke() {
                varEntry .setCurrent(current);
                // update all things that depend on the variable
                DependencyManager.updateDependentObjectsValST(varEntry );
            }
            public void cleanup(int status) {
                synchronized(deck) {
                    if (status == COMPLETE)
                        deck.notify();
                    else // status is QUEUED or RUNNING
                        deck.done(true);
                }
            }
        });
    }

    private  void setCurrentValue( double value ) {
        int step;
        double max = varEntry .max().number(), min = varEntry .min().number(),
                    res = varEntry .resolution().number();
        double current = ((ValueScalar) varEntry .value()).number();
        if ( max == min ) {
            step = 0;
        }
        else {
            step = (int) Math .round( (current - min) * res / (max - min) );
            if ( step < 0 ) {
                step = 0;
            }
            else {
                if ( step > (int) Math .round( varEntry .resolution().number() ) ) {
                    step = (int) Math .round( varEntry .resolution().number() );
                }
            }
        }
        Exec.begin_nocancel();
        this .current = varEntry .value() .toString();
        deck .setStep( step );
        Exec.end_nocancel();
    }

    public  void readout() {
        this .deck .readout( this .current );
    }

    public void actionPerformed( ActionEvent event ) {
        if (event .getSource() == this .startField ||
            event .getSource() == this .endField ||
            event .getSource() == this .resolutionField) {
            demo.changeVariable(this.varEntry,
                                startField.getText(),
                                endField.getText(),
                                resolutionField.getText());
            Exec.run(new ExecCallback() { public void invoke() { setResolution(); } });
        }
        else if ( event .getSource() == this .removeBtn ) {
            try {
                DependencyManager.removeDependency(this, varEntry);
                demo.removeSymbolTableEntry(varEntry);
                controls.removeVariable(varEntry);
            }
            catch (HasDependentsException ex) {
                Demo.showError("You cannot remove " + varEntry.name() + " because there are things dependent on it.\nIf you want to remove " + varEntry.name() + ", remove the dependencies first, and try again.");
                DependencyManager.setDependency(this, varEntry);
            }
        }
    }

    public  void setResolution() {
        this .deck .setResolution( (int) Math .round( varEntry .resolution().number() ) );
    }

    public  void suspendGraphs() {
        //System .out .println( "suspendGraphs in VariablePanel not yet implemented" );
        //this .demo .suspendGraphs();
    }

    public  void unsuspendGraphs() {
        //System .out .println( "unsuspendGraphs in VariablePanel not yet implemented" );
        //this .demo .unsuspendGraphs();
    }

    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }

}