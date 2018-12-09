package demo.io;
//
//  FileObject.java
//  Demo
//
//  Created by David Eigen on Thu Jun 27 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

/**
 * The FileObject interface is used by the FileObjectIO classes. Although it is possible to
 * make FileObjectIO classes that do not use the FileObject interface on their object, it is
 * almost always easiest for the object to implement FileObject and load itself. You can then
 * put a FileFileObjectIO class in the list of FileObjectIO classes in FileObjectIOClasses.java.
 *
 * In addition to the methods described in the FileObject interface, a class implementing FileObject
 * to be used with a FileFileObjectIO class must also have a constructor that takes the parameters
 * (Token, FileParser).
 */
public interface FileObject {

    /**
     * Binds all ID's encountered when loading to fields or other constructs in this object.
     * @param parser the parser (can be used to look up object ID's)
     */
    public void loadFileBind(FileParser parser);

    /**
     * Recognizes all Expressions encountered when loading to fields or other constructs in
     * this object.
     * @param parser the parser (can be used to look up object ID's)
     */
    public void loadFileExprs(FileParser parser);

    /**
     * Finishes loading the object. This is the last phase in loading.
     * @param parser the parser
     */
    public void loadFileFinish(FileParser parser);

    /**
     * Saves the parameter token for this object that is used to load the object.
     * @param generator the file generator
     * @return the parameter token for this object
     */
    public Token saveFile(FileGenerator generator);
    
}

/* Use this stub when writing the file I/O for a class using FileObject.

Replace @ with the object class name.
Make the object implement FileObject.
Add an instance of FileFileObjectIO to the list
    of FileObjectIO classes in FileObjectIOClasses.java


Put this in the FileObject class:



// ****************************** FILE I/O ****************************** //

public @(Token tok, FileParser parser) {

}

public void loadFileBind(FileParser parser) {

}

public void loadFileExprs(FileParser parser) {

}

public void loadFileFinish(FileParser parser) {

}

public Token saveFile(FileGenerator generator) {

}


*/