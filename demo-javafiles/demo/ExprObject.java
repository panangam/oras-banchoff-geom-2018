//
//  ExprObject.java
//  Demo
//
//  Created by David Eigen on Fri Apr 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.expr;

import demo.expr.ste.SymbolTableEntry;

/**
 * Classes implementing ExprObject can produce a STE representing themselves,
 * so they can be accessed in Expression environments.
 * Most classes implementing ExprObject are of course outside the 
 * demo.expr package.
 */
public interface ExprObject {

    /**
     * @return the SymbolTableEntry representing this object
     */
    public SymbolTableEntry objectTableEntry();

    /**
     * Makes the SymbolTableEntry for this object, if it is not already created.
     * This should only be used for initialization and file loading.
     */
    public void makeObjectTableEntry();
    
}

/* Copy the following code when implementing ExprObject:

public SymbolTableEntry objectTableEntry() {
    return ;
}

boolean __madeTableEntry__ = false;
public void makeObjectTableEntry() {
    if (__madeTableEntry__) return;
    __madeTableEntry__ = true;
    // set up table entry here
}


*/