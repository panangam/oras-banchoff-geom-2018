//
//  ExeStack.java
//  Demo
//
//  Created by David Eigen on Mon Aug 19 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public class ExeStack {

    // an empty stack that can be used as a base of a stack
    public static final ExeStack EMPTY = new ExeStack();

    private Executor[] types_ = null;
    private ExeStack prevStack_ = null;

    public ExeStack() {
    }

    private ExeStack(Executor[] t, ExeStack prevStack) {
        types_ = t;
        prevStack_ = prevStack;
    }
    
    public ExeStack push(Executor[] t) {
        return new ExeStack(t, this);
    }

    public ExeStack pop() {
        return prevStack_;
    }

    public Executor[] peek() {
        return types_;
    }

    public boolean isEmpty() {
        return types_ == null;
    }
    
}
