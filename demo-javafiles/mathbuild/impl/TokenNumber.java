//
//  TokenNumber.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public class TokenNumber extends Token {

    private double number_;

    /**
     * constructs a number token for the given number
     */
    public TokenNumber(double num) {
        super(NUMBER);
        number_ = num;
    }

    /**
     * @return the number this token represents
     */
    public double number() {
        return number_;
    }

    public boolean equals(Token token) {
        if (token.type() != NUMBER)
            return false;
        return ((TokenNumber) token).number() == number_;
    }

    public String toString() {
        return new Double(number_) .toString();
    }

}
