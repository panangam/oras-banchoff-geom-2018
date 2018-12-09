//
//  TokenWord.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public class TokenWord extends Token {

    private String word_;

    /**
     * constructs a word token for the given word string
     */
    public TokenWord(String word) {
        super(WORD);
        word_ = word;
    }

    /**
     * @return the word for this token
     */
    public String word() {
        return word_;
    }

    public boolean equals(Token token) {
        if (token.type() != WORD)
            return false;
        return ((TokenWord) token).word().equals(word_);
    }

    public String toString() {
        return new String(word_);
    }

    /**
     * Checks to see if this word is a valid name.
     * This word is a valid name if it is alphanumeric and does not start with a number.
     */
    public boolean isValidName() {
        if (word_.length() == 0)
            return false;
        if ('0' <= word_.charAt(0) && word_.charAt(0) <= '9')
            return false;
        for (int i = 0; i < word_.length(); ++i) {
            char c = word_.charAt(i);
            if ( ! ( ('a' <= c && c <= 'z') ||
                     ('A' <= c && c <= 'Z') ||
                     ('0' <= c && c <= '9') ) )
                return false;
        }
        return true;
    }
    
}
