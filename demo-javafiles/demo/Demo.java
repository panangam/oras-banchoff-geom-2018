package demo;

import java.awt .*;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileReader;
import java.io.File;
import mathbuild.Environment;
import mathbuild.ParseException;
import mathbuild.BuildException;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.ui.*;
import demo.util.*;
import demo.expr.*;
import demo.expr.ste.*;
import demo.graph.*;
import demo.coloring.*;
import demo.plot.*;
import demo.depend.*;
import demo.gfx.*;
import demo.io.*;
import demo.exec.*;

/**
 * This is the main class of the program.
 * It also acts as a class that knows about a lot of other classes.
 *
 * @author deigen
 */
public class Demo extends Object {

    private EnvCloneable clipboard_ = null;
    
    /**
     * Sets the object currently in the object clipboard.
     */
    public void setClipboard(EnvCloneable obj) {
        clipboard_ = obj;
    }

    /**
     * Clones a new instance of the object in the object clipboard. Used for pasting.
     * Expressions are re-built in the given environment. Displays any errors
     * encountered to the user.
     * @return a cloned instance of the clipboard object, or null if there was an error
     */
    public Object cloneClipboard(mathbuild.Environment env) {
        if (clipboard_ == null) return null;
        try {
            return clipboard_.clone(env);
        }
        catch (mathbuild.BuildException ex) {
            showError(ex);
            return null;
        }
        catch (CircularException ex) {
            showError(ex);
            return null;
        }
    }

    /**
     * Clones a new instance of the object in the object clipboard. Used for pasting.
     * Expressions are re-built in the root environment.
     */
    public Object cloneClipboard() {
        return cloneClipboard(environment);
    }

    /**
     * Returns whether the clipboard contains something.
     * @return whether the clipboard is nonempty
     */
    public boolean clipboardNonempty() {
        return clipboard_ != null;
    }

    /**
     * Checks whether the object in the clipboard is of the given class.
     */
    public boolean clipboardInstanceof(Class c) {
        return clipboard_ == null ? false : c.isInstance(clipboard_);
    }
    

    /**
     * Makes a function with the given definition string, and shows it to the user.
     * Shows error dialogs if the definition is not valid for any reason.
     * @param def the definition string
     * @return the table entry for the function, or null if the definition was invalid
     */
    public  STEFunction addFunction( String def ) {
        STEFunction entry = makeFunction( def );
        if ( entry == null ) {
            // there was some error
            return null;
        }
        controls.addFunction( entry );
        return entry;
    }
    
    /**
     * Makes an expression with the given definition string, and shows it to the user.
     * Shows error dialogs if the definition is not valid for any reason.
     * @param def the definition string
     * @return the table entry for the expression, or null if the definition was invalid
     */
    public  STEExpression addExpression( String def ) {
        STEExpression entry = makeExpression( def );
        if ( entry == null ) {
            // there was some error
            return null;
        }
        controls.addExpression(entry);
        return entry;
    }

    /**
     * Makes an interval with the given definition string, and shows it to the user.
     * Shows error dialogs if the definition is not valid for any reason.
     * @param def the definition string
     * @return the table entry for the interval, or null if the definition was invalid
     */
    public  STEInterval addInterval( String def ) {
        STEInterval entry = makeInterval( def );
        if ( entry == null ) {
            // there was some error
            return null;
        }
        // show it in the controls frame
        controls.addInterval(entry);
        return entry;
    }

    /**
     * Makes a varaible with the given definition string, and shows it to the user.
     * Shows error dialogs if the definition is not valid for any reason.
     * @param def the definition string
     * @return the table entry for the variable, or null if the definition was invalid
     */
    public  STEVariable addVariable( String def ) {
        STEVariable entry = makeVariable( def );
        if ( entry == null ) {
            // there was some error
            return null;
        }
        // add the variable to the controls frame
        controls.addVariable(entry);
        return entry;
    }

    /**
     * Changes a function to the given expression (given as a definition string)
     * Displays error dialogs to the user if the definition is invalid for any reason.
     * Updates everything dependent on the function.
     * @param entry the function to change
     * @param expression the definition string to change it to
     */
    public void changeFunction( final STEFunction entry, final String expression ) {
        Exec.run(new ExecCallback() {
            public void invoke() {
                String valExpr = "func(";
                String[] paramNames = entry.paramNames();
                for (int i = 0; i < paramNames.length; ++i)
                    valExpr += paramNames[i] + (i < paramNames.length - 1 ? "," : "");
                valExpr += ")" + "{"+expression+"}";
                Expression expr = recognizeExpression(valExpr, environment.extend(entry.name(),
                                                                    new mathbuild.EnvironmentEntryError(
                                                                    new mathbuild.BuildException(
                "You cannot use " + entry.name() + " in the definition of " + entry.name() + "."))));
                if (expr == null) return;
                // everything is OK: change the expression def
                try {
                    entry.setExpression(expr, paramNames, expression);
                    // update anything that depends on the function
                    DependencyManager.updateDependentObjectsDefST(entry);
                }
                catch (CircularException ex) {
                    showError("Circular dependency.");
                }
            }
        });
    }    

    /**
     * Changes a function to the given expression (given as a definition string).
     * Displays error dialogs to the user if the definition is invalid for any reason.
     * Updates everything dependent on the function.
     * @param name the name of the function to change
     * @param expression the definition string to change it to
     */
    public void changeFunction( String name, String expression ) {
        if ( environment .locallyContains( name ) ) {
            Object entry = environment .lookup( name );
            if ( entry instanceof SymbolTableEntry &&
                 (((SymbolTableEntry) entry).entryType() == SymbolTableEntry.FUNCTION) ) {
                changeFunction( (STEFunction) entry, expression );
            }
        }
    }
    
    /**
     * Changes an expression to the given expression (given as a definition string)
     * Displays error dialogs to the user if the definition is invalid for any reason.
     * Updates everything dependent on the function.
     * @param entry the expression to change
     * @param expression the definition string to change it to
     */
    public void changeExpression( final STEExpression entry, final String expression ) {
        Exec.run(new ExecCallback() { public void invoke() {
            Expression expr = recognizeExpression(expression, environment.extend(entry.name(),
                                                                new mathbuild.EnvironmentEntryError(
                                                                        new mathbuild.BuildException(
                    "You cannot use " + entry.name() + " in the definition of " + entry.name() + "."))));
            if (expr == null) return;
            // everything is OK: change the expression def
            try {
                entry.setExpression(expr);
                // update anything that depends on the function
                DependencyManager.updateDependentObjectsDefMT(entry );
            }
            catch (CircularException ex) {
                showError("Circular dependency.");
            }
        }});
    }
    
    /**
     * Changes an expression to the given expression (given as a definition string).
     * Displays error dialogs to the user if the definition is invalid for any reason.
     * Updates everything dependent on the function.
     * @param name the name of the expression to change
     * @param expression the definition string to change it to
     */
    public void changeExpression( String name, String expression ) {
        if ( environment .locallyContains( name ) ) {
            Object entry = environment .lookup( name );
            if ( entry instanceof SymbolTableEntry &&
                 (((SymbolTableEntry) entry).entryType() == SymbolTableEntry.EXPRESSION) ) {
                changeExpression( (STEExpression) entry, expression );
            }
        }
    }
    
    /**
     * Changes an interval, and updates everything dependent on the interval.
     * Displays error dialogs to the user if a definition is invalid for any reason.
     * @param entry the interval to change
     * @param start the definition string of the start (min) value of the interval
     * @param end the definition string of the end (max) value of the interval
     * @param resolution the definition string of the resolution of the interval
     */
    public void changeInterval( final STEInterval entry,
                                final String start, final String end, final String resolution ) {
        Exec.run(new ExecCallback() { public void invoke() {
            Environment env = environment.extend(entry.name(),
                                                 new mathbuild.EnvironmentEntryError(
                                                        new mathbuild.BuildException(
                        "You cannot use " + entry.name() + " in the definition of " + entry.name() + ".")));
            Expression startExpr = recognizeExpression(start, env);
            if (startExpr == null) return;
            Expression endExpr = recognizeExpression(end, env);
            if (endExpr == null) return;
            Expression resExpr = recognizeExpression(resolution, env);
            if (resExpr == null) return;
            if ( ! (startExpr.returnsScalar() && endExpr.returnsScalar() && resExpr.returnsScalar()) ) {
                showError("start, end and resolution must all be scalars.");
                return;
            }
            try {
                entry.setExpressions(startExpr, endExpr, resExpr);
                // update things that depend on the variable
                DependencyManager.updateDependentObjectsDefMT(entry);
            }
            catch (CircularException ex) {
                showError("Circular dependency.");
            }
        }});
    }

    /**
     * Changes an interval, and updates everything dependent on the interval.
     * Displays error dialogs to the user if a definition is invalid for any reason.
     * @param name the name of the interval to change
     * @param start the definition string of the start (min) value of the interval
     * @param end the definition string of the end (max) value of the interval
     * @param resolution the definition string of the resolution of the interval
     */
    public void changeInterval( String name, String start, String end, String resolution ) {
        if ( environment .locallyContains( name ) ) {
            Object entry = environment .lookup( name );
            if ( entry instanceof SymbolTableEntry &&
                 (((SymbolTableEntry) entry).entryType() == SymbolTableEntry.INTERVAL)  ) {
                changeInterval( (STEInterval) entry, start, end, resolution );
            }
        }
    }

    /**
     * Changes a variable, and updates everything dependent on the variable.
     * Displays error dialogs to the user if a definition is invalid for any reason.
     * @param entry the variable to change
     * @param start the definition string of the start (min) value of the variable
     * @param end the definition string of the end (max) value of the variable
     * @param resolution the definition string of the resolution of the variable
     */
    public void changeVariable( final STEVariable entry,
                                final String start, final String end, final String resolution ) {
        Exec.run(new ExecCallback() { public void invoke() {
            Environment env = environment.extend(entry.name(),
                                                 new mathbuild.EnvironmentEntryError(
                                                        new mathbuild.BuildException(
                "You cannot use " + entry.name() + " in the definition of " + entry.name() + ".")));
            Expression startExpr = recognizeExpression(start, env);
            if (startExpr == null) return;
            Expression endExpr = recognizeExpression(end, env);
            if (endExpr == null) return;
            Expression resExpr = recognizeExpression(resolution, env);
            if (resExpr == null) return;
            if ( ! (startExpr.returnsScalar() && endExpr.returnsScalar() && resExpr.returnsScalar()) ) {
                showError("start, end and resolution must all be scalars.");
                return;
            }
            try {
                entry.setExpressions(startExpr, endExpr, resExpr);
                // update things that depend on the variable
                DependencyManager.updateDependentObjectsDefMT(entry );
            }
            catch (CircularException ex) {
                showError("Circular dependency.");
            }
        }});
    }

    /**
     * Changes a variable, and updates everything dependent on the variable.
     * Displays error dialogs to the user if a definition is invalid for any reason.
     * @param name the name of the variable to change
     * @param start the definition string of the start (min) value of the variable
     * @param end the definition string of the end (max) value of the variable
     * @param resolution the definition string of the resolution of the variable
     */
    public void changeVariable( String name, String start, String end, String resolution ) {
        if ( environment .locallyContains( name ) ) {
            Object entry = environment .lookup( name );
            if ( entry instanceof SymbolTableEntry &&
                 (((SymbolTableEntry) entry).entryType() == SymbolTableEntry.VARIABLE)  ) {
                changeVariable( (STEVariable) entry, start, end, resolution );
            }
        }
    }


    /**
     * Makes a function, and adds it to the program.
     * Does NOT put it into the UI, so the user cannot see it.
     * The crated entry is user editable (has entry.isUserEditable() == true)
     * Displays errors to the user if the definition string is invalid for any reason.
     * @param def the definition string of the function
     * @return the table entry created for the function, or null if there were errors
     */
    public  STEFunction makeFunction( String def ) {
        DeclarationRecognizer recognizer = new DeclarationRecognizer( environment );
        try  {
            recognizer .declareFunction( def );
        } catch( ParseException pex ) {
            showError( pex );
            return null;
        } catch( BuildException bex ) {
            showError( bex );
            return null;
        } catch( CircularException cex ) {
            showError( "Circular dependency." );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        // add the function to the tag generator
        String name = recognizer .resultName();
        // note the casts MUST work OK because the function was recognized correctly
        STEFunction entry = (STEFunction) environment .lookup( name );
        entry.setUserEditable(true);
        return entry;
    }
    
    /**
     * Makes an expression, and adds it to the program.
     * Does NOT put it into the UI, so the user cannot see it.
     * The crated entry is user editable (has entry.isUserEditable() == true)
     * Displays errors to the user if the definition string is invalid for any reason.
     * @param def the definition string of the expression
     * @return the table entry created for the expression, or null if there were errors
     */
    public  STEExpression makeExpression( String def ) {
        DeclarationRecognizer recognizer = new DeclarationRecognizer( environment );
        try  {
            recognizer .declareExpression( def );
        } catch( ParseException pex ) {
            showError( pex );
            return null;
        } catch( BuildException bex ) {
            showError( bex );
            return null;
        } catch( CircularException cex ) {
            showError( "Circular dependency." );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        // add the function to the tag generator
        String name = recognizer .resultName();
        // note the casts MUST work OK because the function was recognized correctly
        STEExpression entry = (STEExpression) environment .lookup( name );
        entry.setUserEditable(true);
        return entry;
    }

    /**
     * Makes an interval, and adds it to the program.
     * Does NOT put it into the UI, so the user cannot see it.
     * The crated entry is user editable (has entry.isUserEditable() == true)
     * Displays errors to the user if the definition string is invalid for any reason.
     * @param def the definition string of the interval
     * @return the table entry created for the interval, or null if there were errors
     */
    public  STEInterval makeInterval( String def ) {
        DeclarationRecognizer recognizer = new DeclarationRecognizer( environment );
        try  {
            recognizer .declareInterval( def );
        } catch( ParseException pex ) {
            showError( pex );
            return null;
        } catch( BuildException bex ) {
            showError( bex );
            return null;
        } catch( CircularException cex ) {
            showError( "Circular dependency." );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        // add the interval to the controls frame
        String name = recognizer .resultName();
        // note the casts MUST work OK because the function was recognized correctly
        STEInterval entry = (STEInterval) environment .lookup( name );
        entry.setUserEditable(true);
        // add to the tag generator
        return entry;
    }

    /**
     * Makes a variable, and adds it to the program.
     * Does NOT put it into the UI, so the user cannot see it.
     * The crated entry is user editable (has entry.isUserEditable() == true)
     * Displays errors to the user if the definition string is invalid for any reason.
     * @param def the definition string of the variable
     * @return the table entry created for the variable, or null if there were errors
     */
    public  STEVariable makeVariable( String def ) {
        DeclarationRecognizer recognizer = new DeclarationRecognizer( environment );
        try  {
            recognizer .declareVariable( def );
        } catch( ParseException pex ) {
            showError( pex );
            return null;
        } catch( BuildException bex ) {
            showError( bex );
            return null;
        } catch( CircularException cex ) {
            showError( "Circular dependency." );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        // get the table entry
        String name = recognizer .resultName();
        // note the casts MUST work OK because the function was recognized correctly
        STEVariable entry = (STEVariable) environment .lookup( name );
        // init variable current to min. value
        entry .setCurrent( ((ValueScalar) entry.min()).number() );
        entry .setUserEditable(true);
        return entry;
    }

    /**
     * Makes a constant with the given name and value, and puts it in the demo environment.
     * This value does not appear in the UI, so the user can't see it.
     * The created entry is not user editable.
     * @param name the name of the constant (eg. "pi")
     * @param value the value of the constant (eg. 3.14159)
     * @return the table entry for the constant
     */
    public  STEConstant makeConstant( String name, Value value) {
        return makeConstant(name, value, this.environment());
    }
    
    /**
     * Makes a constant with the given name and value. 
     * This value does not appear in the UI, so the user can't see it.
     * The created entry is not user editable.
     * @param name the name of the constant (eg. "pi")
     * @param value the value of the constant (eg. 3.14159)
     * @param env the environment to put the constant in
     * @return the table entry for the constant
     */
    public  STEConstant makeConstant( String name, Value value, Environment env ) {
        if ( !DeclarationRecognizer.isValidName(name) ) {
            showError(name + " is not a valid name.");
            return null;
        }
        if (environment.locallyContains(name)) {
            showError(name + " is already defined.");
            return null;
        }
        STEConstant entry = new STEConstant( name, value );
        entry.setUserEditable(false);
        environment .put( name, entry );
        return entry;
    }
    
    /**
     * Makes a constant, and adds it to the program.
     * Does NOT put it into the UI, so the user cannot see it.
     * Displays errors to the user if the definition string is invalid for any reason.
     * The created entry is not user editable.
     * @param def the definition string of the variable
     * @return the table entry created for the variable, or null if there were errors
     */
    public  STEConstant makeConstant( String def ) {
        DeclarationRecognizer recognizer = new DeclarationRecognizer( environment );
        try  {
            recognizer .declareConstant( def );
        } catch( ParseException pex ) {
            showError( pex );
            return null;
        } catch( BuildException bex ) {
            showError( bex );
            return null;
        } catch( CircularException cex ) {
            showError( "Circular dependency." );
            return null;
        }
        if ( recognizer .containsErrors() ) {
            showErrors( recognizer .errors() );
            return null;
        }
        // get the table entry
        String name = recognizer .resultName();
        // note the casts MUST work OK because the function was recognized correctly
        STEConstant entry = (STEConstant) environment .lookup( name );
        entry.setUserEditable(false);
        return entry;
    }

    /**
     * Adds a binding to the demo's environment. Reports any errors to the user.
     * @param name the name for the binding.
     * @param entry what the name should be bound to
     * @return whether the name was successfully bound
     */
    public boolean addEnvironmentBinding(String name, SymbolTableEntry entry) {
        if (environment.locallyContains(name)) {
            showError(name + " is already defined.");
            return false;
        }
        environment.put(name, entry);
        return true;
    }

    /**
     * Makes a 2D GraphFrame with a GraphCanvas, Graph, and axes, and displays it to the user.
     */
    public  void addGraph2D() {
        Graph3D graph = makeGraph3D();
        graph.setDrawingMode(Graph3D.SORT);
        graph.useLighting(false);
        Plot axes = new PlotAxes( new String []{ "x", "y" } );
        graph .addPlot( axes );
        DependencyManager .setDependency( graph, axes );
        GraphCanvas3D canvas = makeGraphCanvas3D( graph );
        canvas.setMouseTool( GraphCanvas3D.TRANSLATE );
        canvas.setDrawingMode(GraphCanvas3D.AWT_GRAPHICS);
        GraphFrame2D frame = makeGraphFrame2D( "2D Graph", canvas );
        frame .setDefaultView();
        // show the frame
        frame.setVisible(true);
        for (java.util.Enumeration l = graphFrameListeners.elements();
             l.hasMoreElements();)
            ((GraphFrameListener) l.nextElement()).graphFrameCreated(frame);
    }

    /**
     * Makes a 3D GraphFrame with a GraphCanvas, Graph, and axes, and displays it to the user.
     */
    public  void addGraph3D() {
        Graph3D graph = makeGraph3D();
        graph.setDrawingMode(Graph3D.SORT);
        graph.setSurfaceMode(Graph3D.FILLED_FRAME);
        graph.useLighting(true);
        graph.useAlphaBlending(true);
        Plot axes = new PlotAxes( new String []{ "x", "y", "z" } );
        graph .addPlot( axes );
        DependencyManager .setDependency( graph, axes );
        GraphCanvas3D canvas = makeGraphCanvas3D( graph );
        canvas.setMouseTool( GraphCanvas3D.ROTATE );
        canvas.setDrawingMode(GraphCanvas3D.Z_BUFFER);
        canvas.suspend(false);
        GraphFrame3D frame = makeGraphFrame3D( "3D Graph", canvas );
        frame .setDefaultView();
        // show the frame
        frame.setVisible(true);
        for (java.util.Enumeration l = graphFrameListeners.elements();
             l.hasMoreElements();)
            ((GraphFrameListener) l.nextElement()).graphFrameCreated(frame);
    }

    /**
     * Disposes the frame, canvas, graph, plots, colorings. Does not close the frame.
     * Updates anything dependent on the frame, canvas, graph, plots, or colorings.
     * @param frame the frame
     * @param canvas the canvas
     * @param graph the graph
     */
    public void disposeGraphFrameAndContents( GraphFrameUserPlottable frame, GraphCanvas3D canvas, Graph3D graph ) {
        // remove frame from list of frames
        this .graphFrames .remove( frame );
        // remove frame, canvas, graph dependencies
        DependencyManager.remove(frame);
        DependencyManager.remove(graph);
        DependencyManager.remove(canvas);
        // remove from rotation group
        GraphGroup group = canvas .getGroup();
        if ( group != null ) {
            removeFromGroup( group, graph, canvas );
        }
        // remove plots
        java.util .Enumeration plotsEnum = graph .plots();
        while ( plotsEnum .hasMoreElements() )
            removePlot((Plot) plotsEnum.nextElement(), graph);
        for (java.util.Enumeration l = graphFrameListeners.elements();
             l.hasMoreElements();)
            ((GraphFrameListener) l.nextElement()).graphFrameDisposed(frame);
    }


    /**
     * Changes a PlotCurve.
     * Displays errors from parsing, etc. to the user.
     * Updates everything dependent on the plot.
     * @param plot the plot to change
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the curve (eg. 2 for 2D)
     * @return whether the changes was successful (false if there were any errors)
     */
    public boolean changePlotCurve( PlotCurve plot, Expression expression, Coloring coloring, int dimension ) {
        if ( expression != null ) {
            IntervalExpression intervalExpr = new IntervalExpression( expression );
            expression.dispose();
            if (!intervalExpr.returnsVector(dimension)) {
                showError( "Expression must be a vector with " + dimension + " coordinates." );
                intervalExpr.dispose();
                return false;
            }
            // make sure it only has one interval
            if ( intervalExpr .numIntervals() != 1 ) {
                showError( "Curves must be dependent on exactly one interval." );
                intervalExpr.dispose();
                return false;
            }
            plot .setExpression( intervalExpr );
        }
        if ( coloring != null && coloring != plot .coloring() ) {
            plot .setColoring( coloring );
        }
        return true;
    }

    /**
     * Changes a PlotSurface3D.
     * Displays errors from parsing, etc. to the user.
     * Updates everything dependent on the plot.
     * @param plot the plot to change
     * @param expression the expression for the surface
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the surface (eg. 3 for 3D)
     * @return whether the changes was successful (false if there were any errors)
     */
    public  boolean changePlotSurface( PlotSurface3D plot, Expression expression,
                                   Coloring coloring, int dimension ) {
        if ( expression != null ) {
            // we need to update the expression
            // make the IntervalExpression
            IntervalExpression intervalExpr = new IntervalExpression( expression );
            expression.dispose();
            // make sure it's the right number of coordinates
            if ( ! intervalExpr .returnsVector( dimension ) ) {
                showError( "Expression must be a vector with " + dimension + " coordinates." );
                intervalExpr.dispose();
                return false;
            }
            // make sure it only has one interval
            if ( intervalExpr .numIntervals() != 2 ) {
                showError( "Surfaces must be dependent on exactly two intervals." );
                intervalExpr.dispose();
                return false;
            }
            // dependencies
            plot .setExpression( intervalExpr );
        }
        if ( coloring != null && coloring != plot .coloring() ) {
            plot .setColoring( coloring );
        }
        return true;
    }

    /**
     * Changes a PlotWireframe.
     * Displays errors from parsing, etc. to the user.
     * Updates everything dependent on the plot.
     * @param plot the plot to change
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the wireframe (eg. 3 for 3D)
     * @param intervals the intervals the wireframe uses
     * @param intervalConnectValues for each interval, whether to connect dots and make lines
     * @return whether the changes was successful (false if there were any errors)
     */
    public  boolean changePlotWireframe( PlotWireframe plot, Expression expression,
                                            Coloring coloring, int dimension,
                                            STEInterval[] intervals,
                                            boolean[] intervalConnectValues ) {
        if ( expression != null ) {
            // we need to update the expression
            // make the IntervalExpression
            IntervalExpression intervalExpr = new IntervalExpression( expression );
            expression.dispose();
            // make sure it's the right number of coordinates
            if ( ! intervalExpr .returnsVector( dimension ) ) {
                showError( "Expression must be a vector with " + dimension + " coordinates." );
                intervalExpr.dispose();
                return false;
            }
            // dependencies
            plot .setExpression( intervalExpr );
        }
        if ( coloring != null && coloring != plot .coloring() ) {
            // we need to set the coloring
            plot .setColoring(coloring);
        }
        if ( intervals != null && intervalConnectValues != null ) {
            for ( int i = 0; i < intervals .length; i++ ) {
                plot .setConnect( intervals[i], intervalConnectValues[i] );
            }
        }
        return true;
    }

    /**
     * Creates a PlotCurve.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @return the plot created, or null if there were any errors
     */
    public PlotCurve makePlotCurve( Expression expression, Coloring coloring ) {
        return makePlotCurve( expression, coloring, - 1 );
    }

    /**
     * Creates a PlotCurve.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the curve (eg. 2 for 2D)
     * @return the plot created, or null if there were any errors
     */
    public PlotCurve makePlotCurve( Expression expression, Coloring coloring, int numcoords ) {
        // make the IntervalExpression
        IntervalExpression intervalExpr = new IntervalExpression( expression );
        expression.dispose();
        // make sure it has the right number of coordinates
        if ( numcoords >= 0 ) {
            if ( (! intervalExpr .returnsVector( numcoords )) &&
                    ! (numcoords == 1 && intervalExpr .returnsScalar()) ) {
                showError( "Expression must have " + numcoords + " coordinates." );
                intervalExpr.dispose();
                return null;
            }
        }
        // make sure it only has one interval
        if ( intervalExpr .numIntervals() != 1 ) {
            showError( "Curves must be dependent on exactly one interval." );
            intervalExpr.dispose();
            return null;
        }
        // everything looks OK. Make the plot
        PlotCurve plot = new PlotCurve( intervalExpr, coloring, numcoords );
        return plot;
    }

    /**
     * Creates a PlotSurface3D.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @return the plot created, or null if there were any errors
     */
    public PlotSurface3D makePlotSurface( Expression expression, Coloring coloring ) {
        return makePlotSurface( expression, coloring, - 1 );
    }

    /**
     * Creates a PlotSurface3D.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the surface (eg. 3 for 3D)
     * @return the plot created, or null if there were any errors
     */
    public PlotSurface3D makePlotSurface( Expression expression, Coloring coloring, int numcoords ) {
        // make the IntervalExpression
        IntervalExpression intervalExpr = new IntervalExpression( expression );
        expression.dispose();
        // make sure it has the right number of coordinates
        if ( numcoords >= 0 ) {
            if ( (! intervalExpr .returnsVector( numcoords )) &&
                    ! (numcoords == 1 && intervalExpr .returnsScalar()) ) {
                showError( "Expression must have " + numcoords + " coordinates." );
                intervalExpr.dispose();
                return null;
            }
        }
        // make sure it only has one interval
        if ( intervalExpr .numIntervals() != 2 ) {
            showError( "Surfaces must be dependent on exactly two intervals." );
            intervalExpr.dispose();
            return null;
        }
        // everything looks OK. Make the plot
        PlotSurface3D plot = new PlotSurface3D( intervalExpr, coloring, numcoords );
        return plot;
    }

    /**
     * Creates a PlotWireframe.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param intervals the intervals the wireframe uses
     * @param intervalConnectValues for each interval, whether to connect dots and make lines
     * @return the plot created, or null if there were any errors
     */
    public PlotWireframe makePlotWireframe( Expression expression, Coloring coloring,
                                    STEInterval[] intervals, boolean[] intervalConnectValues ) {
        return makePlotWireframe( expression, coloring, intervals, intervalConnectValues, - 1 );
    }

    /**
     * Creates a PlotWireframes.
     * Displays errors from parsing, etc. to the user.
     * @param expression the expression for the plot
     * @param coloring the coloring for the plot
     * @param dimension the expected dimension of the wireframe (eg. 3 for 3D)
     * @param intervals the intervals the wireframe uses
     * @param intervalConnectValues for each interval, whether to connect dots and make lines
     * @return the plot created, or null if there were any errors
     */
    public PlotWireframe makePlotWireframe( Expression expression, Coloring coloring,
                                    STEInterval[] intervals, boolean[] intervalConnectValues,
                                    int numcoords ) {
        // make the IntervalExpression
        IntervalExpression intervalExpr = new IntervalExpression( expression );
        expression.dispose();
        // make sure it has the right number of coordinates
        if ( numcoords >= 0 ) {
            if ( (! intervalExpr .returnsVector( numcoords )) &&
                    ! (numcoords == 1 && intervalExpr .returnsScalar()) ) {
                showError( "Expression must have " + numcoords + " coordinates." );
                intervalExpr.dispose();
                return null;
            }
        }
        // everything looks OK. Make the plot
        PlotWireframe plot = new PlotWireframe( intervalExpr, coloring, numcoords );
        // set connect values
        if ( intervals != null ) {
            for ( int i = 0; i < intervals .length; i++ ) {
                plot .setConnect( intervals[i], intervalConnectValues[i] );
            }
        }
        return plot;
    }

    /**
     * Creates a Graph3D.
     * @return the graph created
     */
    public  Graph3D makeGraph3D() {
        Graph3D graph = new Graph3D();
        graph.useLighting(false);
        return graph;
    }

    /**
     * Creates a GraphCanvas3D with the given graph.
     * @param graph the graph that the GraphCanvas3D should draw.
     * @return the canvas created
     */
   public  GraphCanvas3D makeGraphCanvas3D( Graph3D graph ) {
        GraphCanvas3D canvas = new GraphCanvas3D( graph );
        return canvas;
    }

    /**
     * Creates a GraphFrame3D with the given canvas.
     * @param title the title of the frame
     * @param canvas the canvas that the GraphFrame3D should contain.
     * @return the frame created
     */
    public  GraphFrame3D makeGraphFrame3D( String title, GraphCanvas3D canvas ) {
        GraphFrame3D frame = new GraphFrame3D( this, canvas, (Graph3D) canvas .graph(), title );
        this .graphFrames .put( frame );
        return frame;
    }

    /**
     * Creates a GraphFrame2D with the given canvas.
     * @param title the title of the frame
     * @param canvas the canvas that the GraphFrame2D should contain.
     * @return the frame created
     */
    public  GraphFrame2D makeGraphFrame2D( String title, GraphCanvas3D canvas ) {
        GraphFrame2D frame = new GraphFrame2D( this, canvas, (Graph3D) canvas .graph(), title );
        this .graphFrames .put( frame );
        return frame;
    }

    /**
     * @return an Enumeration of all GraphFrames.
     */
    public  java.util .Enumeration allGraphFrames() {
        return graphFrames .elements();
    }

    /**
     * Adds a GraphFrameListener
     */
    public void addGraphFrameListener(GraphFrameListener lis) {
        graphFrameListeners.put(lis);
    }

    /**
     * Removes a GraphFrameListener
     */
    public void removeGraphFrameListener(GraphFrameListener lis) {
        graphFrameListeners.remove(lis);
    }

    
    /**
     * Removes an environment entry from the user-defined part of the environment.
     * @param entry the entry to remove.
     * @throws HasDependentsException if the entry has dependent objects
     */
    public void removeSymbolTableEntry(SymbolTableEntry entry) throws HasDependentsException {
        if (DependencyManager.hasDependentObjects(entry))
            throw new HasDependentsException(entry.name() + " cannot be removed, because there are things dependent on it.");
        this.environment.removeEntry(entry);
        entry.dispose();
    }
    
    /**
     * Removes a (non-Axes) Plot from the program.
     * Updates objects dependent on the graph.
     * @param plot the plot to remove
     * @param graph the graph that the plot is in
     */
     public void removePlot(Plot plot, Graph graph) {
         plot.dispose();
    }
    
    
    /**
     * Removes a hotspot from the program.
     * If anything is dependent on the hotspot variable, an error is displayed to the user instead.
     * @param hotspot the hotspot
     * @param canvas the canvas the hotspot is in
     * @return whether the removal was successful
     */
    public boolean removeHotspot( Hotspot hotspot, GraphCanvas3D canvas ) {
        if ( DependencyManager.hasDependentObjects(hotspot.pointTableEntry()) ) {
            showError("Cannot remove hotspot because there are things dependent on it.");
            return false;
        }
        canvas.removeHotspot(hotspot);
        environment.remove(hotspot.pointTableEntry().name());
        DependencyManager.remove(hotspot.pointTableEntry());
        DependencyManager.remove(hotspot);
        hotspot.dispose();
        return true;
    }

    private static final RotationMatrix4D GRAPH_GROUP_VIEW = new RotationMatrix4D( - 1, 0, - 1.8 );

    /**
     * Creates a GraphGroup containing the given Graphs and Canvases.
     * @param graphs an Enumeration of Graph3D containing the graphs that should be in the group
     * @param canvases and Enumeratino of GraphCanvas3D containing the canvases that hold the graphs
     * @param preserveRelativeRotation if false, then the rotations of all graphs are reset to the default
     * @param linkAllTransformations whether to link all transformations, or just rotation
     */
    public GraphGroup makeGraphGroup( java.util .Enumeration graphs,
                                      java.util .Enumeration canvases,
                                      boolean preserveRelativeRotation,
                                      boolean linkAllTransformations) {
        GraphGroup group = new GraphGroup();
        while ( graphs .hasMoreElements() ) {
            Graph graph = (Graph) graphs .nextElement();
            group .addGraph( graph );
            if ( ! preserveRelativeRotation && graph instanceof Graph3D ) {
                // set the graph to a default view
                graph .transform( ((Graph3D) graph) .getTransformations() .inverse() .multiplyOnLeftBy( GRAPH_GROUP_VIEW ) );
            }
        }
        while ( canvases .hasMoreElements() ) {
            GraphCanvas3D canvas = (GraphCanvas3D) canvases .nextElement();
            group .addCanvas( canvas );
        }
        group.setTransformAll(linkAllTransformations);
        DependencyManager.updateDependentObjectsDefMT(group.graphs());
        return group;
    }

    /**
     * Removes all Graphs and Canvases from a group, and removes the group from the program.
     * @param group the GraphGroup to remove
     */
    public  void removeGraphGroup( GraphGroup group ) {
        if ( ! group .isEmpty() ) {
            java.util .Enumeration graphs = group .graphs();
            while ( graphs .hasMoreElements() ) {
                Graph currGraph = (Graph) graphs .nextElement();
                group .removeGraph( currGraph );
            }
            java.util .Enumeration canvases = group .canvases();
            while ( canvases .hasMoreElements() ) {
                GraphCanvas3D currCanvas = (GraphCanvas3D) canvases .nextElement();
                group .removeCanvas( currCanvas );
                // tell the frame that it's been removed from a group
                // first, find the frame (there should only be about 5 or less...)
                java.util .Enumeration allFrames = allGraphFrames();
                while ( allFrames .hasMoreElements() ) {
                    GraphFrame currFrame = (GraphFrame) allFrames .nextElement();
                    if ( currFrame instanceof GraphFrame3D && ((GraphFrame3D) currFrame) .canvas() == currCanvas ) {
                        ((GraphFrame3D) currFrame) .setGraphGroupState( false );
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds a Graph and the Canvas that holds in to a GraphGroup.
     * @param group the GraphGroup to add the graph and canvas to
     * @param graph the Graph3D to add to the GraphGroup
     * @param canvas the GraphCanvas3D that contains the Graph
     * @param preserverRelativeRotation if false, the rotations of all graphs in the group are set to the default
     * @param linkAllTransformations whether to link all transformations, or just rotation
     */
    public  void addGraphToGroup( GraphGroup group, Graph graph, GraphCanvas3D canvas, boolean preserveRelativeRotation, boolean linkAllTransformations ) {
        group .addGraph( graph );
        group .addCanvas( canvas );
        if ( ! preserveRelativeRotation ) {
            // transform all graphs in group
            java.util .Enumeration graphs = group .graphs();
            while ( graphs .hasMoreElements() ) {
                Graph currGraph = (Graph) graphs .nextElement();
                if ( currGraph instanceof Graph3D ) {
                    // set the graph to a default view
                    currGraph .transform( ((Graph3D) currGraph) .getTransformations() .inverse() .multiplyOnLeftBy( GRAPH_GROUP_VIEW ) );
                }
            }
        }
        group.setTransformAll(linkAllTransformations);
        DependencyManager.updateDependentObjectsDefMT(group.graphs());
    }

    /**
     * Removes a Graph from a GraphGroup.
     * @param group the GraphGroup that the Graph should be removed from
     * @param graph the Graph that should be taken out of the group
     * @param canvas the GraphCanvas that holds the Graph
     */
    public void removeFromGroup( GraphGroup group, Graph graph, GraphCanvas3D canvas ) {
        group .removeGraph( graph );
        group .removeCanvas( canvas );
        if ( group .containsOneItemOrLess() ) {
            removeGraphGroup( group );
        }
    }
    
    /**
     * Sets the mouse place value symbols for a GraphCanvas3D.
     * @param canvas the canvas
     * @param value0 String containing the symbol name for the horizontal STEValue
     * @param value1 String containing the symbol name for the vertical STEValue
     * @param value2 String containing the symbol name for the out-of-screen STEValue
     * @return true if successful, false if not
     */
    public boolean setGraphCanvasMousePlaceValues( GraphCanvas3D canvas, String value0, String value1, String value2 ) {
        // get symbol entries
        Object ste0 = environment.contains(value0) ? environment.lookup(value0) : null;
        Object ste1 = environment.contains(value1) ? environment.lookup(value1) : null;
        Object ste2 = environment.contains(value2) ? environment.lookup(value2) : null;
        // make sure all STEs are values
        if ( ( !(ste0 instanceof STEValue) && !value0.equals("") ) ||
             ( !(ste1 instanceof STEValue) && !value1.equals("") ) ||
             ( !(ste2 instanceof STEValue) && !value2.equals("") ) ) {
            String errMsg = "";
            boolean moreThanOneErr = false;
            if ( !(ste0 instanceof STEValue) && !value0.equals("") )
                errMsg += value0;
            if ( !(ste1 instanceof STEValue) && !value1.equals("") )
                if (errMsg.equals(""))
                    errMsg += value1;
                else {
                    errMsg += ", " + value1;
                    moreThanOneErr = true;
                }
            if ( !(ste2 instanceof STEValue) && !value2.equals("") )
                if (errMsg.equals(""))
                    errMsg += value2;
                else {
                    errMsg += ", " + value2;
                    moreThanOneErr = true;
                }
            if (moreThanOneErr)
                errMsg += " must be variables.";
            else
                errMsg += " must be a variable.";
            showError(errMsg);
            return false;
        }
        // make sure all values are different
        if ( (ste0 == ste1 && ste0 != null) || (ste1 == ste2 && ste1 != null)
             || (ste2 == ste0 && ste2 != null) ) {
            showError("All values must be different, or left unspecified.");
            return false;
        }
        // set the canvas's values
        STEValue[] oldValues = canvas.getMousePointValues();
        canvas.setMousePointValues((STEValue) ste0, (STEValue) ste1, (STEValue) ste2);
        return true;
    }

    /**
     * @return the file string of the current state of the program
     */
    public  String generateFile() {
        Set rootSet = new Set();
        for (java.util.Enumeration frames = allGraphFrames(); frames.hasMoreElements();)
            rootSet.put(frames.nextElement());
        rootSet.put(controls);
        FileGenerator generator = new FileGenerator();
        String s = generator.generateFile(environment, rootSet.elements());
        return wrapText(s + " ", 80);
    }

    /**
     * @return the applet tag of the current state of the program
     */
    public String generateTag() {
        String data = generateFile();
		try {
			Reader r = new InputStreamReader(getClass().getResourceAsStream("/resources/jarfile.url"));
			BufferedReader in = new BufferedReader(r);
			String jarUrl = in.readLine().trim();
			int slash = jarUrl.lastIndexOf('/');
			String jarDir;
			if (slash == -1)
				jarDir = "";
			else
				jarDir = jarUrl.substring(0, slash);
			String tag = "<applet archive=\""+jarUrl+"\" codebase=\""+jarDir+"\" code=\"DemoApplet\" width=200 height=100> <param name=\"NAME\" value=\"Demo\"> <param name=\"DATA\" value=\"\n"+data+"\n\"></applet>";
			return tag;
		}
		catch (java.io.IOException ex) {
			throw new RuntimeException(ex);
		}
    }
        
    private static String wrapText(String str, int maxwidth) {
        String newStr = "";
        String oldStr = new String(str);
        boolean inQuote = false;
        boolean onQuoteMark = false;
        while (oldStr.length() > maxwidth) {
            int i;
            inQuote = false;
            onQuoteMark = false;
            for (i = 0; i < maxwidth; ++i)
                if (onQuoteMark = onQuoteMark(oldStr, i))
                    inQuote = !inQuote;
            while (i > 0 && (oldStr.charAt(i) != ' ' || inQuote || onQuoteMark)) {
                --i;
                if (onQuoteMark = onQuoteMark(oldStr, i))
                    inQuote = !inQuote;
            }
            if (i <= 0) {
                i = -1;
                inQuote = false;
                onQuoteMark = false;
                do {
                    ++i;
                    if (onQuoteMark = onQuoteMark(oldStr, i))
                        inQuote = !inQuote;
                } while (i < oldStr.length()-1 && (oldStr.charAt(i) != ' ' || inQuote || onQuoteMark));
                if (i == oldStr.length() - 1)
                    return newStr + oldStr;
            }
            newStr += oldStr.substring(0,i) + " \n";
            oldStr = oldStr.substring(i+1);
        }
        return newStr + oldStr;
    }

    private static boolean onQuoteMark(String str, int i) {
        if (str.charAt(i) != '$') return false;
        boolean escaped = false;
        while (i > 0 && str.charAt(--i) == '\\')
            escaped = !escaped;
        return !escaped;
    }

    /**
     * Loads a file. If unsuccessfule, loads a blank demo.
     * @param data the data to load
     * @return whether the load was successful
     */
    public  boolean loadFile( String data ) {
        // first, a hack to see if this is the old file format
        String dataTrimmed = data.trim(); // trim whitespace for checking file format version
        if (dataTrimmed.startsWith("define") || dataTrimmed.startsWith("create")) {
            // it is the old file format: alert the user, and do not load.
            showError("This demo was created with an older version of this software.\n" +
                      "Try running with an older version, or contact deigen@math.brown.edu.");
            return false;
        }
        disposeAll();
        environment = makeBaseEnvironment().extend();
        // for now: temp frame; in the future, use the loading progress window
        LoadProgressFrame loadProgressFrame = new LoadProgressFrame();
        loadProgressFrame.setVisible(true);
        imageCreators.put(loadProgressFrame);
        FileParser parser = null;
        java.util.Enumeration errorRootObjs = null;
        Exception errorEx = null;
        boolean error = false;
        try {
            java.util.Enumeration rootObjects;
            parser = new FileParser(this, loadProgressFrame.progressBar());
            parser.parseFile(data, environment);
            environment = parser.getRootEnvironment();
            rootObjects = parser.getRootObjects();
            Set graphFramesSet = new Set(); ControlsFrame controlsFrame = null;
            while (rootObjects.hasMoreElements()) {
                Object obj = rootObjects.nextElement();
                if (obj instanceof GraphFrame)
                    graphFramesSet.put(obj);
                else if (obj instanceof ControlsFrame)
                    controlsFrame = (ControlsFrame) obj;
            }
            if (controlsFrame == null) {
                // there was no controls frame specified in the tag
                this.controls = new ControlsFrame(this);
                this.controls.setVisible(true);
            }
            else {
                // there was a controls frame in the tag
                this.controls = controlsFrame;
            }
            imageCreators.put(controls);
            java.util.Enumeration frames = graphFramesSet.elements();
            while (frames.hasMoreElements()) {
                GraphFrame frame = (GraphFrame) frames.nextElement();
                this.graphFrames.add(frame);
                for (java.util.Enumeration l = graphFrameListeners.elements();
                     l.hasMoreElements();)
                    ((GraphFrameListener) l.nextElement()).graphFrameCreated(frame);
            }
        }
        catch (FileParseException ex) {
            errorRootObjs = parser.getRootObjects();
            errorEx = ex;
            error = true;
        }
        loadProgressFrame.progressBar().setIndeterminate(false);
        imageCreators.remove(loadProgressFrame);
        loadProgressFrame.setVisible(false);
        //loadProgressFrame.dispose(); //should dispose, but it causes problems on some systems
        if (error) {
            // an error occurred: start w/ new demo
            while (errorRootObjs.hasMoreElements()) {
                Object obj = errorRootObjs.nextElement();
                if (obj instanceof Frame)
                    ((Frame) obj).dispose();
            }
            newDemo();
            showError(errorEx);
            return false;
        }
        return true;
    }
    
    /**
     * @return this demo class's base environment, where all global symbols are stored
     */
    public Environment environment() {
        return environment;
    }
    
    /**
     * Creates a parse tree for an expression from an expression definition string.
     * Displays any errors from parsing, etc. to the user.
     * @param str the text of the expression to recognize
     * @param env the environment to use
     * @return the Expression for the expression, or null if there was some error
     */
    public static Expression recognizeExpression( String str, Environment env ) {
        mathbuild.SyntaxNode tree = recognizeExpressionImpl(str, env);
        if (tree == null) return null;
        try {
            return new Expression( str, tree, env );
        }
        catch (BuildException ex) {
            showError(ex);
            return null;
        }
        catch (CircularException cex) {
            showError("Circular dependency.");
            return null;
        }
    }

    /**
     * Creates a parse tree for an expression from an expression definition string.
     * The expression has to return a scalar.
     * Displays any errors from parsing, etc. to the user.
     * @param expr the text of the expression to recognize
     * @param env the environment to use
     * @return the expression, or null if there was an error
     */
    public static Expression recognizeScalarExpression( String expr, Environment env ) {
        Expression expression = recognizeExpression( expr, env );
        if (expression != null)
            if ( ! expression.returnsScalar() ) {
                showError( "Expression must be a scalar value." );
                expression.dispose();
                return null;
            }
        return expression;
    }
    
    /**
     * Creates a parse tree for an expression from an expression definition string.
     * Displays any errors from parsing, etc. to the user.
     * @param str the text of the expression to recognize
     * @param env the environment to use
     * @return the IntervalExpression for the expression, or null if there was some error
     */
    public static IntervalExpression recognizeIntervalExpression( String str, Environment env ) {
        mathbuild.SyntaxNode tree = recognizeExpressionImpl(str, env);
        if (tree == null) return null;
        try {
            return new IntervalExpression( str, tree, env );
        }
        catch (BuildException ex) {
            showError(ex);
            return null;
        }
        catch (CircularException cex) {
            showError("Circular dependency.");
            return null;
        }
    }

    /**
     * Creates a parse tree for an expression from an expression definition string.
     * Uses the Demo class's base environment.
     * Displays any errors from parsing, etc. to the user.
     * @param expr the text of the expression to recognize
     * @return the Expression for the expression, or null if there was some error
     */
    public Expression recognizeExpression( String expr ) {
        return recognizeExpression(expr, environment);
    }

    /**
     * Creates a parse tree for an expression from an expression definition string.
     * The expression has to return a scalar.
     * Uses the demo class's base environment.
     * Displays any errors from parsing, etc. to the user.
     * @param expr the text of the expression to recognize
     * @return the expression, or null if there was an error
     */
    public Expression recognizeScalarExpression( String expr ) {
        return recognizeScalarExpression(expr, environment);
    }

    /**
     * Creates a parse tree for an expression from an expression definition string.
     * Displays any errors from parsing, etc. to the user.
     * Uses the demo class's base environment.
     * @param expr the text of the expression to recognize
     * @return the IntervalExpression for the expression, or null if there was some error
     */
    public IntervalExpression recognizeIntervalExpression( String expr ) {
        return recognizeIntervalExpression(expr, environment);
    }

    // used to make the tree in recognize expression methods
    private static mathbuild.SyntaxNode recognizeExpressionImpl( String expr, Environment env ) {
        mathbuild.SyntaxNode tree = null;
        try  {
            mathbuild.Parser parser = new mathbuild.Parser();
            tree = parser.parse(expr);
        } catch( mathbuild.ParseException ex ) {
            showError(ex);
            return null;
        }
        return tree;
    }
    

    /**
     * Displays a message to the user, in a window with an OK button.
     * @param message the message to display
     */
    public  static void showMessage( String message ) {
        // split by newline
        java.util.Vector strings = new java.util.Vector();
        int start = 0;
        while (start < message.length()) {
            int newline = message.indexOf('\n', start);
            if (newline == -1)
                newline = message.length();
            strings.addElement(message.substring(start, newline));
            start = newline + 1;
        }
        new MessageWindow( strings.elements() );
    }

    /**
     * Displays an error to the user, in a window with an OK button.
     * @param message the error message to display
     */
    public static void showError( String message ) {
        showMessage( "Error: " + message );
    }

    /**
     * Displays an error to the user, in a window with an OK button.
     * @param message the error message to display
     */
    public static void showError( Exception ex ) {
        showError( ex.getMessage() );
    }

    /**
     * Displays an error to the user, in a window with an OK button.
     * @param beginningOfMessage text to stick on the beginning of each error message.
     * @param errorStrings a list of error messages to display
     */
    public static void showErrors( java.util .Vector errorStrings ) {
        java.util .Vector messages = new java.util .Vector( errorStrings .size() );
        for ( int i = 0; i < errorStrings .size(); i++ ) {
            messages .addElement( "Error: " + (String) errorStrings .elementAt( i ) );
        }
        new MessageWindow( messages .elements() );
    }

    /**
     * The global base environment. Maps from a name (as String) to SymbolTableEntry.
     */
    private  Environment environment;

    private  ControlsFrame controls;

    /**
     * All GraphFrames in the program.
     */
    private  Set graphFrames;
    private  Set graphFrameListeners; // GraphFrameListeners


    private static Set imageCreators = new Set();
    
    /**
     * Creates an image that should can be used for TEMPORARY use ONLY.
     * @param w the width
     * @param h the height
     * @return a java.awt.Image with the given dimensions, that can be used TEMPORARILY
     */
    public static java.awt.Image createImage(int w, int h) {
        java.util.Enumeration creators = imageCreators.elements();
        while (creators.hasMoreElements()) {
            Component c = (Component) creators.nextElement();
            if ( c != null && c.isVisible() )
                return c.createImage(w,h);
        }
        return null;
    }

    /**
     * Returns the default FontMetrics for a java.awt.Graphics of a created java.awt.Image
     * It is possible that the returned FontMetrics can only be used temporarily.
     * @return a java.awt.FontMetrics
     */
    public static java.awt.FontMetrics getFontMetrics() {
        return createImage(1,1).getGraphics().getFontMetrics();
    }

    /**
     * Gets the java version number
     * Returns it as an array of integers, the first entry the first number of the version,
     * the second being the second digit, and so on.
     */
     public static int[] javaVersionNumber() {
        String versionStr = System.getProperty("java.version");
         try {
             java.util.Vector version = new java.util.Vector(3);
             while ( !versionStr.equals("") ) {
                 int dot = versionStr.indexOf(".");
                 int uscore = versionStr.indexOf("_");
                 if (dot == -1)
                     dot = versionStr.length();
                 if (uscore == -1)
                     uscore = versionStr.length();
                 dot = dot < uscore ? dot : uscore;
                 version.addElement(new Integer( versionStr.substring(0, dot) ) );
                 versionStr = versionStr.substring(dot == versionStr.length() ? dot : dot + 1);
             }
             int[] versionArray = new int[version.size()];
             for (int i = 0; i < versionArray.length; ++i)
                 versionArray[i] = ((Integer) version.elementAt(i)).intValue();
             return versionArray;
         }
         catch (NumberFormatException ex) {
             // unknown version string format
             // just assume it's 1.2
             return new int[]{1,2};
         }
     }
     
     /** 
      * @param a java version number, in the form specified by javaVersionNumber()
      * @return whether the version of Java running is greater than or equal to the given version
      */
    public static boolean javaVersionIsGreaterThanOrEqualTo( int[] version ) {
        int[] running = javaVersionNumber();
        int i = 0;
        for (i = 0; i < running.length && i < version.length; ++i) {
            if ( running[i] < version[i] )
                return false;
        }
        for (; i < version.length; ++i)
            if (version[i] > 0)
                return false;
        return true;
    }

    /**
     * @return whether the software is currently running on Macintosh
     */
    public static boolean runningOnMac() {
        return System.getProperty("os.name").startsWith("Mac");
    }

    /**
     * @return whether the demo is running as an applet
     */
    public boolean isApplet() {
        return this.demoApplet != null;
    }

    public static  void main( String[] args ) {
        // start a new demo
        System.out.println("Starting Demo application...");
        if (args.length == 1) {
            // if there is an argument, it's the file to open
            try {
                FileReader r = new FileReader(new File(args[0]));
                String str = "";
                int c;
                while ((c = r.read()) != -1)
                    str += (char) c;
                r.close();
                new Demo(null, str);
            }
            catch (java.io.FileNotFoundException ex) {
                System.out.println("File not found: " + args[0]);
                System.exit(1);
            }
            catch (java.io.IOException ex) {
                new Demo(null, null);
            }
        }
        else new Demo(null, null);
    }

    private Environment makeBaseEnvironment() {
        Environment env = mathbuild.Init.baseEnvironment().extend();
        // constants (e and pi)
        // we don't want these constants to be case-sensitive,
        // so put them with all possible capitalizations
        STEConstant eEntry = new STEConstant( "e", Math .E );
        env.put( "e", eEntry );
        env.put( "E", eEntry );
        STEConstant piEntry = new STEConstant( "pi", Math .PI );
        env.put( "pi", piEntry );
        env.put( "Pi", piEntry );
        env.put( "pI", piEntry );
        env.put( "PI", piEntry );
        // read in file of additional built-in functions (like unit normal vector, etc.)
        env = env.extend();
        new DefinitionsPackageRecognizer(env).readDefinitions(
            new InputStreamReader(getClass().getResourceAsStream("/resources/builtins.defs")));
        return env;
    }
    
    public void init(String dataTag) {
        // make sure there is an image creator
        Frame tempFrame = null;
        if (imageCreators.isEmpty()) {
            tempFrame = new Frame();
            tempFrame.setSize(1,1);
            tempFrame.setLocation(-1000000,-1000000);
            tempFrame.setVisible(true);
            imageCreators.put(tempFrame);
        }
        // allow user to override base environment entries by using extension environment
        environment = makeBaseEnvironment().extend();
        // initialize UI element containers
        graphFrames = new Set();
        graphFrameListeners = new Set();
        // make a controls window
        controls = new ControlsFrame( this );
        controls.setVisible(true);
        // get something that can create images
        imageCreators.put(controls);
        if (tempFrame != null) {
            imageCreators.remove(tempFrame);
            tempFrame.dispose();
        }
        // make sure the execution thread is up
        Exec.startup();
        // read in tag
        if ( dataTag != null ) {
            // process the data tag so it has no <br> tags or <p> tags
	    int start = 0, index = 0;
	    String data = dataTag;
	    String newdata = "";
	    while ((index = data.indexOf("<br>", start)) != -1) {
		newdata += data.substring(start, index) + " \n";
		start = index + 4;
	    }
            newdata += data.substring(start);
	    data = newdata;
	    newdata = "";
	    index = start = 0;
	    while ((index = data.indexOf("<BR>", start)) != -1) {
		newdata += dataTag.substring(start, index) + " \n";
		start = index + 4;
	    }
	    newdata += data.substring(start);
	    data = newdata;
            loadFile( data );
        }
    }

    public  Demo( DemoApplet applet, String dataTag ) {
        super();

            this .demoApplet = applet;
            init(dataTag);
        }
        
    private  DemoApplet demoApplet;


    private void disposeAll() {
        // close everything and delete pointers
        java.util .Enumeration graphFramesEnum = graphFrames .elements();
        while ( graphFramesEnum .hasMoreElements() ) {
            GraphFrame gf = (GraphFrame) graphFramesEnum.nextElement();
            gf.setVisible(false);
            gf.dispose();
        }
        graphFrames = new Set();
        if (controls != null) {
            imageCreators.remove(controls);
            controls .setVisible(false);
            controls .dispose();
            controls = null;
        }
    }
    
    public  void newDemo() {
        disposeAll();
        init(null);
    }

    public  void quit() {
        disposeAll();
        // quit
        if ( demoApplet != null ) {
            // this is running from an applet
            this .demoApplet .endDemo();
        }
        else {
            // this is running by itself. we need to quit
            System .exit( 0 );
        }
    }


}




class MessageWindow extends DemoFrame{

    public  MessageWindow( java.util .Enumeration messages ) {
        super();
            this .setLayout( new BorderLayout() );
            Panel messagePanel = new Panel();
            messagePanel .setLayout( new GridLayout( 0, 1 ) );
            while ( messages .hasMoreElements() ) {
                messagePanel .add( new Label( (String) messages .nextElement() ) );
            }
            this .add( new Label("   "),"North");
            this .add( new Label("   "),"East");
            this .add( new Label("   "),"West");
            this .add( messagePanel, "Center" );
            Panel southPanel = new Panel();
            southPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
            southPanel .add( okBtn );
            this .add( southPanel, "South" );
            pack();
            setLocation(100,50);
            setResizable(false);
            setVisible(true);
        }

    public  MessageWindow( String message ) {
        super();

            this .setLayout( new BorderLayout() );
            this .add( new Label("   "),"North");
            this .add( new Label("   "),"East");
            this .add( new Label("   "),"West");
            this .add( new Label( message ), "Center" );
            Panel southPanel = new Panel();
            southPanel .setLayout( new FlowLayout( FlowLayout .RIGHT ) );
            southPanel .add( okBtn );
            this .add( southPanel, "South" );
            pack();
            setLocation(100,50);
            setResizable(false);
            setVisible(true);
        }

    private  Button okBtn = new Button( "OK" );

    public  boolean action( Event e, Object o ) {
        if ( e .target == okBtn ) {
            this .setVisible(false);
            this .dispose();
            return true;
        }
        return super .action( e, o );
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

    public  boolean keyDown( java.awt .Event e, int key ) {
        if ( key == 10 || key == 3 ) {
            // enter or return key pressed: send OK event
            return action( new Event( okBtn, Event .ACTION_EVENT, null ), null );
        }
        return super .keyDown( e, key );
    }


}


