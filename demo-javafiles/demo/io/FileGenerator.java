package demo.io;
//
//  FileGenerator.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import mathbuild.Environment;

import demo.depend.*;
import demo.expr.ste.*;
import demo.util.Set;
import demo.expr.Expression;

public class FileGenerator extends FileIO {


    // objects_ maps from objects to IDs
    private java.util.Dictionary objects_ = new java.util.Hashtable();
    // generatedObjects_ is a set containing all the objects whose (non-id) token was generated
    private Set generatedObjects_ = new Set();

    private String objNamePrefix_ = "obj";
    private int objNameCount_ = 0;

    public FileGenerator() {
        super();
    }

    /**
     * Generates a file for the given root environment and root objects.
     * @param rootEnvironment the root (ie, global) environment
     * @param rootObjects the root set of objects to generate. Root objects must implement FileObjectIO
     * @return the generated file
     */
    public String generateFile(Environment rootEnvironment, java.util.Enumeration rootObjects) {
        FileProperties props = new FileProperties();
        props.add(PROPERTY_FILEVERSION, new TokenNumber(FILE_FORMAT_VERSION));
        props.add(PROPERTY_ENVIRONMENT, generateLocalEnvironment(rootEnvironment));
        // generate the root objects
        TokenString objsStr = new TokenString();
        while (rootObjects.hasMoreElements())
            objsStr.addToken(generateObject(rootObjects.nextElement()));
        props.add(PROPERTY_OBJECTS, generateList(objsStr));
        // generate objects used by the root objects that might not have been generated
        TokenString nonrootObjsStr = new TokenString();
        boolean allObjectsGenerated = false; // whether there might be more objs to generate
        while (!allObjectsGenerated) {
            allObjectsGenerated = true;
            java.util.Enumeration allObjs = objects_.keys();
            while (allObjs.hasMoreElements()) {
                Object obj = allObjs.nextElement();
                if (!generatedObjects_.contains(obj)) {
                    nonrootObjsStr.addToken(generateObject(obj));
                    allObjectsGenerated = false;
                }
            }
        }
        if (nonrootObjsStr.size() > 0)
            props.add(PROPERTY_NONROOT_OBJECTS, generateList(nonrootObjsStr));
        return ((TokenNode) generateProperties(props)).enclosedString().toString();
    }

    /**
     * Generates the Token representation for the given properties map.
     * @param properties the properties map
     * @return the generated Token for the map
     */
    public Token generateProperties(FileProperties properties) {
        return properties.generate();
    }

    /**
     * Generates the Token for a list.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateList(TokenString str) {
        return new TokenNode(Tokenizer.TOKEN_OPEN_SQUARE_BRACKET,
                             Tokenizer.TOKEN_CLOSE_SQUARE_BRACKET,
                             str);
    }

    /**
     * Generates the Token for a list of words.
     * @param list the list to generate the token for (an enumeration of String)
     * @param the Token representing the given list
     */
    public Token generateWordList(java.util.Enumeration list) {
        TokenString str = new TokenString();
        while (list.hasMoreElements())
            str.add(generateWord((String) list.nextElement()));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of expressions.
     * @param list the list to generate the token for (an enumeration of Expression)
     * @param the Token representing the given list
     */
    public Token generateExpressionList(java.util.Enumeration list) {
        TokenString str = new TokenString();
        while (list.hasMoreElements())
            str.add(generateExpression((Expression) list.nextElement()));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of objects.
     * There must be a FileObjectIO registered for each of the objects.
     * @param list the list to generate the token for. (an enumeration of Object)
     * @param the Token representing the given list
     */
    public Token generateObjectList(java.util.Enumeration list) {
        TokenString str = new TokenString();
        while (list.hasMoreElements())
            str.add(generateObject(list.nextElement()));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of objects, but only generates IDs for the objects, and
     * not the actual object definitions.
     * There must be a FileObjectIO registered for each of the objects.
     * @param list the list to generate the token for.
     * @param the Token representing the given list
     */
    public Token generateObjectIDList(java.util.Enumeration list) {
        TokenString str = new TokenString();
        while (list.hasMoreElements())
            str.add(generateObjectID(list.nextElement()));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of words.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateWordList(String[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateWord(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of numbers.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateNumberList(int[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateNumber((double) list[i]));
        return generateList(str);
    }

    /**
        * Generates the Token for a list of numbers.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateNumberList(double[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateNumber(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of boolean.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateBooleanList(boolean[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateBoolean(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of expressions.
     * @param list the list to generate the token for
     * @param the Token representing the given list
     */
    public Token generateExpressionList(Expression[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateExpression(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of objects.
     * There must be a FileObjectIO registered for each of the objects.
     * @param list the list to generate the token for.
     * @param the Token representing the given list
     */
    public Token generateObjectList(Object[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateObject(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token for a list of objects, but only generates IDs for the objects, and 
     * not the actual object definitions.
     * There must be a FileObjectIO registered for each of the objects.
     * @param list the list to generate the token for.
     * @param the Token representing the given list
     */
    public Token generateObjectIDList(Object[] list) {
        TokenString str = new TokenString();
        for (int i = 0; i < list.length; ++i)
            str.add(generateObjectID(list[i]));
        return generateList(str);
    }

    /**
     * Generates the Token representing the given object. If the object
     * was already generated, generateObject(.) returns an object ID token
     * node. If the object was not already generated, it is added to the set
     * of added objects, and the full object token is returned.
     * @param obj the object to generate. There must be a FileObjectIO registered for the object.
     * @return a Token representation of the object
     */
    public Token generateObject(Object obj) {
        if (obj == null)
            return TOKEN_NULL;
        TokenString str = new TokenString();
        if (generatedObjects_.contains(obj)) {
            // already generated the object
            return generateObjectID(obj);
        }
        generatedObjects_.put(obj);
        if (objects_.get(obj) == null)
            objects_.put(obj, genID());
        Token type = new TokenWord((String) CLASS_TO_TYPE.get(obj.getClass().getName()));;
        Token id = (Token) objects_.get(obj);
        str.addToken(type);
        str.addToken(id);
        str.addToken(((FileObjectIO) CLASS_TO_IO.get(obj.getClass().getName())).saveFile(obj, this));
        return new TokenNode(str);
    }

    /**
     * Generates the ID token for the given object. Creates an ID for the object
     * if necessary. Even if the object has not previously been generated, this
     * method will return the ID token, and not the full object representation.
     * @param obj the object to make an ID for. There must be a FileObjectIO registered for the object.
     * @return a the ID token for the object
     */
    public Token generateObjectID(Object obj) {
        if (obj == null)
            return TOKEN_NULL;
        TokenString str = new TokenString();
        str.addToken(TOKEN_ID);
        if (objects_.get(obj) == null)
            objects_.put(obj, genID());
        str.addToken((Token) objects_.get(obj));
        return new TokenNode(str);
    }

    /**
     * Generates a Token for the string. Encloses in quotes ($) if there
     * are any non-alphanumeric chars. Escapes backslashes and quote chars.
     *
     * If generateWord(.) is used on a String, then the resulting token
     * will be parsed into one TokenWord when parseWord(.) is used on the token.
     *
     * @param word the word to make a token for
     * @return a token that will be parsed to a single TokenWord containing the word
     */
    public Token generateWord(String word) {
        if (word.length() == 0)
            return new TokenWord("$$");
        char[] c = word.toCharArray();
        boolean useQuotes = false;
        for (int i = 0; i < c.length; ++i) {
            if ( !(('a' <= c[i] && c[i] <= 'z') || ('A' <= c[i] && c[i] <= 'Z')) ) {
                useQuotes = true;
                break;
            }
        }
        if (useQuotes) {
            // need to replace occurrences of \ with \\, and $ with \$
            String str = replaceStr( replaceStr( word,
                                                 "\\", "\\\\" ),
                                     "$", "\\$" );
            return new TokenWord("$"+str+"$");
        }
        return new TokenWord(word);
    }

    private String replaceStr(String source, String pattern, String replacement) {
        String str = "";
        int index = 0, start = 0;
        while ((index = source.indexOf(pattern, start)) != -1) {
            str += source.substring(start, index) + replacement;
            start = index + pattern.length();
        }
        str += source.substring(start);
        return str;
    }

    /**
     * Generates a Token for a number.
     * @param num the number to generate
     * @reutrn a Token for the number
     */
    public Token generateNumber(double num) {
        return new TokenNumber(num);
    }

    /**
     * Generates a Token for a boolean.
     * @param num the number to generate
     * @reutrn a Token for the boolean
     */
    public Token generateBoolean(boolean b) {
        return new TokenWord(b ? "true" : "false");
    }

    /**
     * Generates the Token for an expression
     * @param expr the expression to generate
     * @return a Token representing the expression
     */
    public Token generateExpression(Expression expr) {
        if (expr == null) return TOKEN_NULL;
        return new TokenWord("$" + expr.definitionString() + "$");
    }

    /**
     * Generates the Token for the local part of an environment
     * @param env the environment
     * @param a Token representation of the local part of the environment
     */
    public Token generateLocalEnvironment(Environment env) {
        TokenString str = new TokenString();
        java.util.Vector symbols = DependencyManager.sortByDependency(new Set(env.localEntries()));
        for (int i = 0; i < symbols.size(); ++i) {
            SymbolTableEntry entry = (SymbolTableEntry) symbols.elementAt(i);
            if (entry.entryType() == SymbolTableEntry.CONSTANT) {
                // TOKEN_CONSTANT $name=value$
                str.addToken(TOKEN_CONSTANT);
                str.addToken(new TokenWord("$" + entry.name() + "=" +
                                           ((STEConstant) entry).value() + "$"));
            }
            else if ( entry.entryType() == SymbolTableEntry.EXPRESSION ) {
                // TOKEN_EXPRESSION $name=definition$
                str.addToken(TOKEN_EXPRESSION);
                str.addToken(new TokenWord("$" + entry.name() + "=" +
                                           ((STEExpression) entry).expressionDef() + "$"));
            }
            else if ( entry.entryType() == SymbolTableEntry.VARIABLE ) {
                // TOKEN_VARIABLE $name=min,max,res$ <current value>
                str.addToken(TOKEN_VARIABLE);
                str.addToken(new TokenWord("$" + entry.name() + "=" +
                                           ((STEVariable) entry).minStr() + "," +
                                           ((STEVariable) entry).maxStr() + "," +
                                           ((STEVariable) entry).resStr() + "$"));
                str.addToken(new TokenWord(((STEVariable) entry).value().toString()));
            }
            else if ( entry.entryType() == SymbolTableEntry.INTERVAL ) {
                // TOKEN_INTERVAL $name=min,max,res$ <current value>
                str.addToken(TOKEN_INTERVAL);
                str.addToken(new TokenWord("$" + entry.name() + "=" +
                                           ((STEInterval) entry).minStr() + "," +
                                           ((STEInterval) entry).maxStr() + "," +
                                           ((STEInterval) entry).resStr() + "$"));
            }
            else if ( entry.entryType() == SymbolTableEntry.FUNCTION ) {
                // TOKEN_FUNCTION $name(params)=definition$
                str.addToken(TOKEN_FUNCTION);
                String params = "";
                String[] paramNames = ((STEFunction) entry).paramNames();
                for (int p = 0; p < paramNames.length; ++p)
                    params += paramNames[p] + (p < paramNames.length-1 ? "," : "");
                str.addToken(new TokenWord("$" + entry.name() + "(" + params + ")" + "=" +
                                           ((STEFunction) entry).bodyDefinition() + "$"));
            }
            else if ( entry.entryType() == SymbolTableEntry.OBJECT ) {
                // TOKEN_OBJECT {props w/ name, obj}
                FileProperties props = new FileProperties();
                props.add("name", generateWord(entry.name));
                props.add("obj", generateObjectID(((STEObject) entry).object()));
                str.addToken(TOKEN_OBJECT);
                str.addToken(generateProperties(props));
            }
        }
        return new TokenNode(str);
    }
    
    private TokenWord genID() {
        return new TokenWord(objNamePrefix_ + (objNameCount_++));
    }
    

}
