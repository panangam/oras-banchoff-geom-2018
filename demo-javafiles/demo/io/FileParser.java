package demo.io;
//
//  FileParser.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import mathbuild.Environment;

import demo.depend.*;
import demo.expr.ste.*;

import demo.ui.ProgressBar;
import demo.util.Set;
import demo.Demo;
import demo.ControlsFrame;
import demo.expr.ExprObject;
import demo.expr.Expression;
import demo.expr.DeclarationRecognizer;

public class FileParser extends FileIO {

    
    private java.util.Vector environmentStack_;
    private Environment currEnvironment_;
    private java.util.Dictionary objects_; // maps IDs to objects

    // for progress
    private ProgressBar progressBar_;
    
    // root-level things (need to store them in order for the demo to save them)
    private Environment rootEnvironment_;
    private Set rootObjects_ = new Set();
    private Set rootObjectIDs_ = new Set();

    // other objects and data needed by objects
    private Demo demo_;
    private int currGraphFrameLocX_, currGraphFrameLocY_;
    private java.util.Vector dimensionStack_; // keeps track of curr graph frame dimensions

    // store the current loading phase in order to check that functions
    // are not getting called when they are not supposed to be
    private int currPhase_ = PHASE_NONE;
    private static final int
        PHASE_NONE = -1, PHASE_BEGIN = 0, PHASE_CONSTRUCT = 1, PHASE_BIND = 2,
        PHASE_OBJENTRIES = 3, PHASE_ENV = 4, PHASE_EXPR = 5, PHASE_FINISH = 6,
        PHASE_COMPLETE = 7, PHASE_ERROR = 8;

    public FileParser() {
        super();
    }

    /**
     * Creates a new FileParser.
     * @param demo the demo
     * @param progressBar the progress bar to show the progress of file loading
     */
    public FileParser(Demo demo, ProgressBar progressBar) {
        super();
        demo_ = demo;
        progressBar_ = progressBar;
    }
    
	
    /**
	 * Parses a file. Results of the loading can be obtained with
     * getRootObjects(), getRootEnvironment(), getObjects()
     * @param filestr a String containing the file data
     * @param baseEnvironment the environment to start out with (root environment is appended to it)
     */
	public void parseFile(String filestr, Environment baseEnvironment) {
        currPhase_ = PHASE_BEGIN;
		TokenNode file = new TokenNode(new Tokenizer().tokenize(filestr));
        environmentStack_ = new java.util.Vector();
        dimensionStack_ = new java.util.Vector();
        currEnvironment_ = baseEnvironment;
        objects_ = new java.util.Hashtable();
        rootObjects_ = new Set();
        rootObjectIDs_ = new Set();
        progressBar_.setIndeterminate(true);
        currGraphFrameLocX_ = 100; currGraphFrameLocY_ = 100;
        try {
            FileProperties props = parseProperties(file);
            if (!props.contains(PROPERTY_OBJECTS)) {
                currPhase_ = PHASE_ENV;
                if (props.contains(PROPERTY_ENVIRONMENT))
                    rootEnvironment_ = baseEnvironment.append(
                            parseLocalEnvironment(props.get(PROPERTY_ENVIRONMENT)));
                else
                    rootEnvironment_ = baseEnvironment;                
            }
            else {
                // parse stage: construct objects
                currPhase_ = PHASE_CONSTRUCT;
                TokenString objectsStr = parseList(props.get(PROPERTY_OBJECTS));
                for (int i = 0; i < objectsStr.size(); ++i) {
                    String id = parseObject(objectsStr.tokenAt(i));
                    // if root object can be put into the set now, put it in the set now
                    // need to do this in case we get an error and need to dispose all frames
                    if (objects_.get(id) != null)
                        rootObjects_.put(objects_.get(id));
                    else
                        rootObjectIDs_.put(id);
                }
                // put all remaining root objects into the set (possible that we only had an ID before)
                for (java.util.Enumeration rootObjIDsEnum = rootObjectIDs_.elements();
                     rootObjIDsEnum.hasMoreElements();)
                    rootObjects_.put(objects_.get((String) rootObjIDsEnum.nextElement()));
                // construct non-root objects
                if (props.contains(PROPERTY_NONROOT_OBJECTS)) {
                    TokenString nonrootObjsStr = parseList(props.get(PROPERTY_NONROOT_OBJECTS));
                    for (int i = 0; i < nonrootObjsStr.size(); ++i)
                        parseObject(nonrootObjsStr.tokenAt(i));
                }
                // ID binding stage
                currPhase_ = PHASE_BIND;
                progressBar_.reset(objects_.size()*3);
                progressBar_.setIndeterminate(false);
                Set controlsFrames = new Set();
                for (java.util.Enumeration objsEnum = objects_.elements();
                     objsEnum.hasMoreElements();) {
                    Object obj = objsEnum.nextElement();
                    progressBar_.increment();
                    ((FileObjectIO) CLASS_TO_IO.get(obj.getClass().getName())).loadFileBind(obj, this);
                    if (obj instanceof ControlsFrame)
                        controlsFrames.add(obj);
                }
                // make expression table entries of all objects
                currPhase_ = PHASE_OBJENTRIES;
                for (java.util.Enumeration objsEnum = objects_.elements();
                     objsEnum.hasMoreElements();) {
                    Object obj = objsEnum.nextElement();
                    if (obj instanceof ExprObject)
                        ((ExprObject) obj).makeObjectTableEntry();
                }
                // load environment
                currPhase_ = PHASE_ENV;
                if (props.contains(PROPERTY_ENVIRONMENT))
                    rootEnvironment_ = baseEnvironment.append(
                            parseLocalEnvironment(props.get(PROPERTY_ENVIRONMENT)));
                else
                    rootEnvironment_ = baseEnvironment;
                pushEnvironment(rootEnvironment_);
                // recognize expressions
                currPhase_ = PHASE_EXPR;
                loadExprs(rootObjects_.elements());
                // finish stage
                currPhase_ = PHASE_FINISH;
                java.util.Vector objectsSorted =
                    DependencyManager.sortByDependency(new Set(objects_.elements()));
                // loadFileFinish on the controls frame first, then all other objects
                for (java.util.Enumeration controlsEnum = controlsFrames.elements();
                     controlsEnum.hasMoreElements();) {
                    Object obj = controlsEnum.nextElement();
                    ((FileObjectIO) CLASS_TO_IO.get(obj.getClass().getName())).loadFileFinish(obj, this);
                    progressBar_.increment();
                }
                for (java.util.Enumeration objsEnum = objectsSorted.elements();
                     objsEnum.hasMoreElements();) {
                    Object obj = objsEnum.nextElement();
                    if ( !(obj instanceof ControlsFrame) ) {
                        ((FileObjectIO) CLASS_TO_IO.get(obj.getClass().getName())).loadFileFinish(obj, this);
                        progressBar_.increment();
                    }
                }
            }
            currPhase_ = PHASE_COMPLETE;
        }
        catch (FileParseException ex) {
            currPhase_ = PHASE_ERROR;
            System.err.println("THE PARSE ERROR:");
            ex.printStackTrace(System.err);
            progressBar_.setIndeterminate(false);
            throw ex;
        }
        catch (CircularException ex) {
            currPhase_ = PHASE_ERROR;
            System.err.println("THE PARSE ERROR:");
            ex.printStackTrace(System.err);
            progressBar_.setIndeterminate(false);
            throw new FileParseException("Circular dependency encountered.");
        }
        catch (ClassCastException ex) {
            currPhase_ = PHASE_ERROR;
            System.err.println("THE PARSE ERROR:");
            ex.printStackTrace(System.err);
            progressBar_.setIndeterminate(false);
            throw new FileParseException("Incompatable types or malformed file.");
        }
        catch (RuntimeException ex) {
            currPhase_ = PHASE_ERROR;
            System.err.println("THE PARSE ERROR:");
            ex.printStackTrace(System.err);
            progressBar_.setIndeterminate(false);
            throw new FileParseException("An error occurred while loading.");
        }
    }

    
    /**
     * @return the root environment
     */
    public Environment getRootEnvironment() {
        if (currPhase_ < PHASE_COMPLETE)
            error("getRootEnvironment can only be called after loading");
        return rootEnvironment_;
    }
    
    /**
     * @return the root objects
     */
    public java.util.Enumeration getRootObjects() {
        if (currPhase_ < PHASE_COMPLETE)
            error("getRootObjects can only be called after loading");
        return rootObjects_.elements();
    }

    /**
     * @return all the objects created
     */
    public java.util.Enumeration getObjects() {
        if (currPhase_ < PHASE_COMPLETE)
            error("getObjects can only be called after loading");
        return objects_.elements();
    }
    
    /**
     * @return the demo class
     */
    public Demo demo() {
        return demo_;
    }
    
    /**
     * Parses a properties map.
     * @param node the source TokenNode of the properties map.
     * @return the properties map
     */
    public FileProperties parseProperties(Token node) {
        if (!(node instanceof TokenNode))
            throw new FileParseException("Badly formatted properties");
        return new FileProperties((TokenNode) node);
    }

    /**
     * Parses a list.
     * @param node the source TokenNode of the list
     * @return the list
     */
    public TokenString parseList(Token node) {
        return ((TokenNode) node).enclosedString();
    }

    /**
     * Parses a list of words.
     * @param node the source TokenNode of the list
     * @return the list
     */
    public String[] parseWordList(Token node) {
        TokenString str = parseList(node);
        String[] list = new String[str.size()];
        for (int i = 0; i < list.length; ++i)
            list[i] = ((TokenWord) str.tokenAt(i)).word();
        return list;
    }

    /**
     * Parses a list of boolean.
     * @param node the source TokenNode of the list
     * @return the list
     */
    public boolean[] parseBooleanList(Token node) {
        TokenString str = parseList(node);
        boolean[] list = new boolean[str.size()];
        for (int i = 0; i < list.length; ++i)
            list[i] = parseBoolean(str.tokenAt(i));
        return list;
    }

    /**
     * Parses a list of numbers.
     * @param node the source TokenNode of the list
     * @return the list
     */
    public double[] parseNumberList(Token node) {
        TokenString str = parseList(node);
        double[] list = new double[str.size()];
        for (int i = 0; i < list.length; ++i)
            list[i] = ((TokenNumber) str.tokenAt(i)).number();
        return list;
    }

    /**
     * Parses a list of expressions.
     * @param node the source TokenNode of the list
     * @return the list
     */
    public String[] parseExpressionList(Token node) {
        TokenString str = parseList(node);
        String[] list = new String[str.size()];
        for (int i = 0; i < list.length; ++i)
            list[i] = parseExpression(str.tokenAt(i));
        return list;
    }

    /**
     * Parses a list of objects.
     * @param node the source TokenNode of the list
     * @return the list of IDs
     */
    public String[] parseObjectList(Token node) {
        TokenString str = parseList(node);
        String[] list = new String[str.size()];
        for (int i = 0; i < list.length; ++i)
            list[i] = parseObject(str.tokenAt(i));
        return list;
    }

    /**
     * Parses a word.
     * @param tok the source TokenWord of the word.
     * @return the word as a String
     */
    public String parseWord(Token tok) {
        return ((TokenWord) tok).word();
    }

    /**
     * Parses a number.
     * @param tok the source TokenNumber of the number.
     * @return the number as a double
     */
    public double parseNumber(Token tok) {
        return ((TokenNumber) tok).number();
    }

    /**
     * Parses a boolean.
     * @param tok the source Token of the boolean.
     * @return the number as a boolean
     */
    public boolean parseBoolean(Token tok) {
        String str = ((TokenWord) tok).word();
        if (str.equals("true")) return true;
        if (str.equals("false")) return false;
        throw new FileParseException("Inproperly formatted boolean value: must be true or false.");
    }

    /**
     * Parses an object.
     * @param node the source TokenNode of the object
     * @return the ID for the object
     */
    public String parseObject(Token node) {
        if ( node.equals(TOKEN_NULL) )
            return null;
        TokenString str = ((TokenNode) node).enclosedString();
        TokenWord type = (TokenWord) str.tokenAt(0);
        String id = ((TokenWord) str.tokenAt(1)).word();
        if (type.equals(TOKEN_ID)) {
            return id;
        }
        else {
            // note that the call to last() instead of using the rest is important:
            // in case we add more to the beginning for all objects, we restrict all
            // parameters to a single token node at the end
            Token objParams = str.last();
            if (TYPE_TO_IO.get(type.word()) == null)
                throw new FileParseException("Unknown object type.");
            Object obj = ((FileObjectIO) TYPE_TO_IO.get(type.word())).construct(objParams, this);
            objects_.put(id, obj);
            return id;
        }
    }

    /**
     * Parses a math expression.
     * @param expression the token for the expression.
     * @return an intermediate representation of the expression, which should be
     *         passed to recognizeExpression(.) in the Exprs stage of file loading.
     */
    public String parseExpression(Token expression) {
        if (expression.equals(TOKEN_NULL)) return null;
        return ((TokenWord) expression).word();
    }

    /**
     * Parses the local part of an environment.
     * @param token the Token for the environment.
     * @return an Environment containing the definitions given in the token representation
     */
    public Environment parseLocalEnvironment(Token token) {
        if (currPhase_ < PHASE_ENV || currPhase_ > PHASE_EXPR)
            error("Environments should only be parsed in env or expr phases");
        TokenString str = ((TokenNode) token).enclosedString();
        Environment env = new Environment();
        DeclarationRecognizer recognizer = new DeclarationRecognizer(currEnvironment_.append(env));
        int i = 0;
        try {
            while (i < str.length()) {
                Token type = str.tokenAt(i++);
                if (type.equals(TOKEN_CONSTANT)) {
                    // TOKEN_CONSTANT $name=value$
                    recognizer.declareConstant(((TokenWord) str.tokenAt(i++)).word());
                    if (recognizer.containsErrors())
                        throw new FileParseException("Errors encountered in a symbol definition.");
                }
                else if (type.equals(TOKEN_EXPRESSION)) {
                    // TOKEN_EXPRESSION $name=definition$
                    recognizer.declareExpression(((TokenWord) str.tokenAt(i++)).word());
                    if (recognizer.containsErrors())
                        throw new FileParseException("Errors encountered in a symbol definition.");
                }
                else if (type.equals(TOKEN_VARIABLE)) {
                    // TOKEN_VARIABLE $name=min,max,res$ <current value>
                    recognizer.declareVariable(((TokenWord) str.tokenAt(i++)).word());
                    if (recognizer.containsErrors())
                        throw new FileParseException("Errors encountered in a symbol definition.");
                    STEVariable ste = (STEVariable) recognizer.resultEntry();
                    ste.setCurrent(((TokenNumber) str.tokenAt(i++)).number());
                }
                else if (type.equals(TOKEN_INTERVAL)) {
                    // TOKEN_INTERVAL $name=min,max,res$
                    recognizer.declareInterval(((TokenWord) str.tokenAt(i++)).word());
                    if (recognizer.containsErrors())
                        throw new FileParseException("Errors encountered in a symbol definition.");
                }
                else if (type.equals(TOKEN_FUNCTION)) {
                    // TOKEN_FUNCTION $name(params)=definition$
                    recognizer.declareFunction(((TokenWord) str.tokenAt(i++)).word());
                    if (recognizer.containsErrors())
                        throw new FileParseException("Errors encountered in a symbol definition.");
                }
                else if (type.equals(TOKEN_OBJECT)) {
                    // TOKEN_OBJECT {properties w/ name, obj}
                    if (currPhase_ < PHASE_BIND)
                        error("Can't parse object environment entry before binding phase.");
                    FileProperties props = parseProperties(str.tokenAt(i++));
                    String name = parseWord(props.get("name"));
                    ExprObject obj = (ExprObject) objects_.get(parseObject(props.get("obj")));
                    env.put(name, new STEObject(name, obj));
                }
                else {
                    throw new FileParseException("Unknown symbol type: " + type);
                }
            }
        }
        catch (mathbuild.ParseException ex) {
            throw new FileParseException("Parse exception encountered in symbol definition.");
        }
        // set whether entries are editable by the user (this could get changed later on)
        for (java.util.Enumeration entries = env.localEntries(); entries.hasMoreElements();)
            ((SymbolTableEntry) entries.nextElement()).setUserEditable(true);
        return env;
    }
    
    /**
     * Looks up the given ID in the table of all objects.
     * @param id the ID of the object
     * @return the object whose ID is the given ID, or null if there is none
     */
    public Object getObject(String id) {
        if (currPhase_ != PHASE_BIND)
            error("getObject should only be called during bind phase");
        if (id == null) return null; // null id is flag for null object
        return objects_.get(id);
    }

    /**
     * Looks up the given IDs in the table of all objects.
     * @param ids the IDs of the objects
     * @return the objects whose IDs are the given IDs (or, an entry of null if there is no object)
     */
    public Object[] getObjects(String[] ids) {
        if (currPhase_ != PHASE_BIND)
            error("getObjects should only be called during bind phase");
        Object[] objs = new Object[ids.length];
        for (int i = 0; i < ids.length; ++i)
            objs[i] = ids[i] == null ? null : objects_.get(ids[i]);
        return objs;
    }

    /**
     * Tell the given object to do the Exprs stage of file loading. In this stage,
     * objects recogniz expressions & bind them. All objects are responsible for
     * calling loadExprs(.) on all the objects that they generate as Objects (not as IDs).
     * @param obj the object to do expression loading
     */
    public void loadExprs(Object obj) {
        if (currPhase_ != PHASE_EXPR)
            error("loadExprs should only be called in expr phase");
        if (obj != null)
            ((FileObjectIO) CLASS_TO_IO.get(obj.getClass().getName())).loadFileExprs(obj, this);
        progressBar_.increment();
    }

    /**
     * Does loadExprs(Object) for all objects in the array.
     */
    public void loadExprs(Object[] objs) {
        for (int i = 0; i < objs.length; ++i)
            loadExprs(objs[i]);
    }

    /**
     * Does loadExprs(Object) for all objects in the enumeration.
     */
    public void loadExprs(java.util.Enumeration objs) {
        while (objs.hasMoreElements())
            loadExprs(objs.nextElement());
    }
    
    /**
     * Recognizes an expression, in the current environment.
     */
    public Expression recognizeExpression(String expression) {
        if (currPhase_ < PHASE_EXPR)
            error("recognizeExpression cannot be called before expr phase");
        if (expression == null) return null;
        return Demo.recognizeExpression(expression, currEnvironment_);
    }

    /**
     * Recognizes a list of expressions, in the current environment.
     */
    public Expression[] recognizeExpressionList(String[] expressions) {
        Expression[] es = new Expression[expressions.length];
        for (int i = 0; i < es.length; ++i)
            es[i] = recognizeExpression(expressions[i]);
        return es;
    }


    /**
     * @return the current expression environment
     */
    public Environment currEnvironment() {
        if (currPhase_ < PHASE_ENV)
            error("currEnvironment cannot be called before env phase");
        return currEnvironment_;
    }

    /**
     * Pushes the given environment onto the Environment stack. All expressions
     * recognized are recognized with the given environment.
     * Note: to extend the environment, one must say something like
     * fileParser.pushEnvironment(fileParser.currEnvironment().append(myExtendedEnv))
     */
    public void pushEnvironment(Environment env) {
        if (currPhase_ < PHASE_ENV)
            error("pushEnvironment cannot be called before env phase");
        currEnvironment_ = env;
        environmentStack_.addElement(env);
    }

    /**
     * Pops the currenv environment off the environment stack, so the previous
     * environment used is now used to recognize expressions.
     */
    public void popEnvironment() {
        if (currPhase_ < PHASE_ENV)
            error("popEnvironment cannot be called before env phase");
        environmentStack_.removeElementAt(environmentStack_.size() - 1);
        currEnvironment_ = (Environment) environmentStack_.elementAt(environmentStack_.size() - 1);
    }

    /**
     * Performs a lookup on the current environment.
     */
    public Object currEnvLookup(String name) {
        if (currPhase_ < PHASE_EXPR)
            error("currEnvLookup cannot be called before expr phase");
        return currEnvironment_.lookup(name);
    }

    /**
     * Performs a lookup on the current environment.
     * @param tok the name to look up. Must be a TokenWord.
     */
    public Object currEnvLookup(Token name) {
        if (currPhase_ < PHASE_EXPR)
            error("currEnvLookup cannot be called before expr phase");
        return currEnvironment_.lookup(((TokenWord) name).word());
    }

    /**
     * Sets the dimension of the objects being parsed (inside a graph frame).
     * @param dimension the dimension to put on the top of the stack
     */
    public void pushDimension(int dimension) {
        if (currPhase_ != PHASE_CONSTRUCT)
            error("pushDimension can only be called in the construct phase");
        dimensionStack_.addElement(new Integer(dimension));
    }

    /**
     * Reverts the dimension for objects being parsed to what it was before.
     */
    public void popDimension() {
        if (currPhase_ != PHASE_CONSTRUCT)
            error("popDimension can only be called in the construct phase");
        dimensionStack_.removeElementAt(dimensionStack_.size() - 1);
    }

    /**
     * @return the current dimension for objects when they are parsed
     */
    public int currDimension() {
        if (currPhase_ != PHASE_CONSTRUCT)
            error("currDimension can only be called in construct phase");
        return ((Integer) dimensionStack_.elementAt(dimensionStack_.size() - 1)).intValue();
    }

    /**
     * @return the location to show the next GraphFrame at
     */
    public java.awt.Point currGraphFrameLocation() {
        return new java.awt.Point(currGraphFrameLocX_, currGraphFrameLocY_);
    }

    /**
     * increments the current graph frame location, so the next
     * graph frame is shown at the next location
     */
    public void incrementGraphFrameLocation() {
        currGraphFrameLocX_ += 30;
        currGraphFrameLocY_ += 30;
    }



    /**
     * Raises a FileParseException
     */
    public void error(String desc) {
        currPhase_ = PHASE_ERROR;
        throw new FileParseException(desc);
    }

    
}


