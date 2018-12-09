//
//  EnvironmentEntryError.java
//  Demo
//
//  Created by David Eigen on Sun Aug 18 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

/**
 * If a name is looked up in an environment and is mapped to this entry, the error contained
 * by this entry is thrown.
 */
public class EnvironmentEntryError {

    private RuntimeException ex_;
    
    public EnvironmentEntryError(RuntimeException ex) {
        ex_ = ex;
    }

    /**
     * Throws the exception contained in this entry.
     * @throws RuntimeException throws the exception contained in this entry
     */
    public void throwException() throws RuntimeException {
        throw ex_;
    }

    /**
     * @return the exception contained in this entry
     */
    public RuntimeException exception() {
        return ex_;
    }
    
}
