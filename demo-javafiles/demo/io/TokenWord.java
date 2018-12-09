package demo.io;
//
//  TokenWord.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//


class TokenWord extends Token {

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
        if (word_ == null)
            return "!!NULL!!";
        return new String(word_);
    }
    
}
