package demo.io;
//
//  FileFileObjectIO.java
//  Demo
//
//  Created by David Eigen on Thu Jun 27 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

import java.lang.reflect.Constructor;

/**
 * For all current objects, and probably all or almost all objects in the future,
 * the I/O for the object can be handled by making the object implement FileObject,
 * and defining the methods in FileObject appropriately. If the I/O for the object
 * is implemented like this, the FileFileObjectIO class is the FileObjectIO class
 * that should be used in the list of FileObjectIO classes in FileObjectIOClasses.java.
 * @author deigen
 */ 
class FileFileObjectIO implements FileObjectIO {

    private Class class_;
    private Constructor constructor_;
    private String fileTypeName_;
    
    /**
     * @param className the name of the class this FileObjectIO class is being used on
     *        (e.g. "ColoringGradient")
     * @param fileTypName the file type name for the class (e.g. "coloring.gradient")
     */
    public FileFileObjectIO(String className, String fileTypeName) {
        try {
            fileTypeName_ = fileTypeName;
            class_ = Class.forName(className);
            constructor_ = class_.getConstructor(new Class[]{
                Class.forName("demo.io.Token"),
                Class.forName("demo.io.FileParser")
            });
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException("Couldn't make FileObjectIO class for " + className + ".");
        }
        catch (NoSuchMethodException ed) {
            throw new RuntimeException("Couldn't make FileObjectIO class for " + className + ".");
        }
    }
    
    public Object construct(Token paramToken, FileParser parser) {
        try {
            return constructor_.newInstance(new Object[]{paramToken, parser});
        }
        catch (InstantiationException ex) {
            throw new RuntimeException("Couldn't construct object.");
        }
        catch (IllegalAccessException ex) {
            throw new RuntimeException("Couldn't construct object.");
        }
        catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable targetEx = ex.getTargetException();
            if (targetEx instanceof RuntimeException)
                throw (RuntimeException) targetEx;
            throw new RuntimeException("Couldn't construct object.");
        }
    }
    
    public void loadFileBind(Object obj, FileParser parser) {
        ((FileObject) obj).loadFileBind(parser);
    }

    public void loadFileExprs(Object obj, FileParser parser) {
        ((FileObject) obj).loadFileExprs(parser);
    }

    public void loadFileFinish(Object obj, FileParser parser) {
        ((FileObject) obj).loadFileFinish(parser);
    }
    
    public Token saveFile(Object obj, FileGenerator generator) {
        return ((FileObject) obj).saveFile(generator);
    }
    
    public String fileTypeName() {
        return fileTypeName_;
    }
    
    public Class fileClass() {
        return class_;
    }
    
} 