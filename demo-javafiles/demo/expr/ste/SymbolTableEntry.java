
package demo.expr.ste;

import mathbuild.Executor;
import mathbuild.Mutable;
import mathbuild.type.*;
import mathbuild.value.Value;

import demo.util.*;
import demo.depend.Dependable;
import demo.depend.DependencyNode;
import demo.depend.DependencyManager;

/** 
 * The superclass for all SymbolTableEntries.
 * SymbolTableEntries contain information about a symbol (like "f" or "x"),
 * such as its current value or parse tree.
 * The name "SymbolTableEntry" (STE) comes from a previous version of the program.
 * STE's are now used as a link between the Demo side of the program and the 
 * expression package (Mathbuild).
 *
 * @author deigen
 */
public  abstract class SymbolTableEntry extends Object implements Dependable, mathbuild.Executor, mathbuild.Mutable {

    /** 
     * The type of the SymbolTableEntry.
     * There is a type for each subclass of SymbolTableEntry.
     * See the constants in this class for different types.
     */
    protected int type;

    // ints for type
    public static final  int EXPRESSION = 0;

    public static final  int FUNCTION = 1;
    
    public static final  int INTERVAL = 2;

    public static final  int VARIABLE = 3;

    public static final  int CONSTANT = 4;

    public static final  int OBJECT = 5;

    public static final  int RECORD = 6;

    public static final  int OTHER = 10;


    boolean userEditable_ = false;


    private mathbuild.Executor overridingExe = null;
    private boolean overridingExecution = false;
    

    /** 
     * The name of the symbol (eg. "x" or "f")
     */
    public  String name;

    /** 
     * @return the name of the symbol (eg. "x" or "f")
     */
    public  String name() {
        return name;
    }

    /** 
     * @return the type of the symbol (eg. FUNCTION or VARIABLE)
     */
    public  int entryType() {
        return type;
    }

    /**
     * Sets whether the user can view and change this entry
     */
    public void setUserEditable(boolean editable) {
        userEditable_ = editable;
    }

    /**
     * @return whether the user can view and change this entry
     */
    public boolean isUserEditable() {
        return userEditable_;
    }


    /**
     * Overrides execution of this STE. Instead of doing the normal
     * evaluation of this STE, the given expression is evaluated instead.
     */
    public void mutate(Executor exe) {
        if (exe == this) {
            overridingExe = null;
            overridingExecution = false;
        }
        else {
            overridingExe = exe;
            overridingExecution = true;
        }
    }

    /**
     * @return the current mutation of this Executor. Either this object, or another exe.
     */
    public Executor currentMutation() {
        if (overridingExecution)
            return overridingExe;
        return this;
    }

    /**
     * Executes this STE Executor.
     */
    public Value execute(Object runID) {
        if (overridingExecution)
            return overridingExe.execute(runID);
        return exec(runID);
    }

    /**
     * Implements the execution of this STE (when performing normal execution).
     */
    protected abstract Value exec(Object runID);
    

    /**
     * Disposes this SymbolTableEntry: removes this entry from the dependency graph,
     * and disposes any expressions that this entry uses.
     * Subclasses that override this method should call super.dispose()
     */
    public void dispose() {
        DependencyManager.remove(this);
    }

    
    
    public void dependencyUpdateDef(Set updatingObjs) {
        // to be overridden by subclasses
    }

    public void dependencyUpdateVal(Set updatingObjs) {
        // to be overridden by subclasses
    }


    // the dependency node
    private DependencyNode myDependencyNode_ = new DependencyNode(this);
    public DependencyNode dependencyNode() {
        return myDependencyNode_;
    }

    
}


