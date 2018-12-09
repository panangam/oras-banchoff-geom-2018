package demo.io;
//
//  FileObjectIO.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

/**
 * The FileObjectIO class is used by the FileParser
 * to generate an Object from a file's Token tree.
 */
public interface FileObjectIO {

    /**
     * Constructs the object by parsing object parameter token.
     * @param paramToken the object parameter token
     * @param parser the parser
     * @return the constructed object, with object IDs unbound
     */
    public Object construct(Token paramToken, FileParser parser);

    /**
     * Binds all ID's encountered when loading to fields or other constructs in this object.
     * @param obj the object whose IDs should be binded
     * @param parser the parser (can be used to look up object ID's)
     */
    public void loadFileBind(Object obj, FileParser parser);

    /**
     * Recognizes all Expressions encountered when loading to fields or other constructs in
     * this object.
     * @param obj the object whose IDs should be binded
     * @param parser the parser (can be used to look up object ID's)
     */
    public void loadFileExprs(Object obj, FileParser parser);

    /**
     * Finishes loading the object. This is the last loading step.
     * @param obj the object to finish loading
     * @param parser the parser
     */
    public void loadFileFinish(Object obj, FileParser parser);

    /**
     * Saves the parameter token for this object that is used to load the object.
     * @param obj the object to save
     * @param generator the file generator
     * @return the parameter token for this object
     */
    public Token saveFile(Object obj, FileGenerator generator);

    /**
     * @return the object type saved in the file for this type of object
     */
    public String fileTypeName();

    /**
     * @return the Java class that this FileObjectIO loads and saves
     */
    public Class fileClass();
    
}



