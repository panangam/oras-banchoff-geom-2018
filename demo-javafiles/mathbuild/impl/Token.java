//
//  Token.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public abstract class Token {

    // types
    public static final int EOF = 0;
    public static final int WORD = 1;
    public static final int NUMBER = 2;
    public static final int PARENTHESIZED_TOKENS = 3;

    private int type_;

    public Token(int type) {
        type_ = type;
    }
    
    /**
     * @return whether this token is the given type
     */
    public boolean isType(int type) {
        return type_ == type;
    }

    /**
     * @return the type of this token
     */
    public int type() {
        return type_;
    }

    /**
     * @return whether the given token equals this token
     */
    public abstract boolean equals(Token token);

    /**
     * @return a String representation of this token
     */
    public abstract String toString();

}
