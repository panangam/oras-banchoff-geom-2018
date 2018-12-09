//
//  TokenParenthesizedTokens.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

public class TokenParenthesizedTokens extends Token {

    private Token openToken_, closeToken_;
    private TokenString tokens_;

    /**
     * @param tokens the tokens enclosed by the parentheses
     */
    public TokenParenthesizedTokens(TokenString tokens) {
        this(Tokenizer.TOKEN_OPEN_PAREN, Tokenizer.TOKEN_CLOSE_PAREN, tokens);
    }

    /**
     * @param openToken the opening parenthesis token (eg, Tokenizer.TOKEN_OPEN_BRACE)
     * @param closeToken the closing parenthesis token (eg, Tokenizer.TOKEN_CLOSE_BRACE)
     * @param tokens the tokens enclosed by the parentheses
     */
    public TokenParenthesizedTokens(Token openToken, Token closeToken, TokenString tokens) {
        super(PARENTHESIZED_TOKENS);
        openToken_ = openToken;
        closeToken_ = closeToken;
        tokens_ = tokens;
    }

    /**
     * @return the open paren token of this token
     */
    public Token openToken() { return openToken_; }

    /**
     * @return the close paren token of this token
     */
    public Token closeToken() { return closeToken_; }

    /**
     * @return the token string inside parens
     */
    public TokenString enclosedString() { return tokens_; }

    
    public boolean equals(Token token) {
        if (token.type() != PARENTHESIZED_TOKENS)
            return false;
        TokenParenthesizedTokens t = (TokenParenthesizedTokens) token;
        return t.openToken().equals(openToken_) &&
               t.closeToken().equals(closeToken_) &&
               t.enclosedString().equals(tokens_);
    }

    public String toString() {
        return  openToken_ + " " + tokens_ + closeToken_;
    }
    
}
