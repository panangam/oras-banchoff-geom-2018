package demo.io;
//
//  FileIO.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

class FileIO implements FileObjectIOClasses {

    public static int FILE_FORMAT_VERSION = 100;
    
    /**
     * Maps file type object name to object class.
     * maps from String to Object
     */
    protected static java.util.Dictionary TYPE_TO_IO = null;

    /**
     * Maps class names to IO classes.
     * maps from String to Object
     */
    protected static java.util.Dictionary CLASS_TO_IO = null;

    /**
     * Maps class names to file type object name.
     * maps from Object to String
     */
    protected static java.util.Dictionary CLASS_TO_TYPE = null;

    private static boolean mapsInitialized_ = false;
    
    public FileIO() {
        if (!mapsInitialized_) {
            mapsInitialized_ = true;
            TYPE_TO_IO = new java.util.Hashtable();
            CLASS_TO_IO = new java.util.Hashtable();
            CLASS_TO_TYPE = new java.util.Hashtable();
            // we need to get the loader classes
            for (int i = 0; i < OBJECT_IO_CLASSES.length; ++i) {
                String name = OBJECT_IO_CLASSES[i].fileTypeName();
                FileObjectIO io = OBJECT_IO_CLASSES[i];
                TYPE_TO_IO.put(name, io);
                CLASS_TO_IO.put(io.fileClass().getName(), io);
                CLASS_TO_TYPE.put(io.fileClass().getName(), name);
            }
        }
    }


    /**
     * Tokens used by both parser and generator.
     */
    public static final Token
        TOKEN_ID = new TokenWord("id"),
        TOKEN_NULL = new TokenWord("null"),
        TOKEN_CONSTANT   = new TokenWord("value"),
        TOKEN_EXPRESSION = new TokenWord("constant"),
        TOKEN_VARIABLE   = new TokenWord("variable"),
        TOKEN_INTERVAL   = new TokenWord("interval"),
        TOKEN_FUNCTION   = new TokenWord("function"),
        TOKEN_OBJECT     = new TokenWord("object");

    /**
     * Property names used by both parser and generator.
     */
    public static final String
        PROPERTY_FILEVERSION = "filever",
        PROPERTY_ENVIRONMENT = "env",
        PROPERTY_OBJECTS = "objects",
        PROPERTY_NONROOT_OBJECTS = "idobjs";

    
    
}
