//
//  TokenEOF.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public class TokenEOF extends Token {

    public TokenEOF() {
        super(EOF);
    }

    public boolean equals(Token token) {
        return token.type() == EOF;
    }

    public String toString() {
        return "<EOF>";
    }
    
}
