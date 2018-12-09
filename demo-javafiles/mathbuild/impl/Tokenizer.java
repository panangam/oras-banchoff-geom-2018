//
//  Tokenizer.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import java.io.*;

public class Tokenizer {

    // some constant tokens
    public static final Token TOKEN_OPEN_PAREN               = new TokenWord("(");
    public static final Token TOKEN_OPEN_SQUARE_BRACKET      = new TokenWord("[");
    public static final Token TOKEN_OPEN_BRACE               = new TokenWord("{");
    public static final Token TOKEN_CLOSE_PAREN              = new TokenWord(")");
    public static final Token TOKEN_CLOSE_SQUARE_BRACKET     = new TokenWord("]");
    public static final Token TOKEN_CLOSE_BRACE              = new TokenWord("}");
    public static final Token TOKEN_COMMA                    = new TokenWord(",");
    public static final Token TOKEN_SEMICOLON                = new TokenWord(";");
    public static final Token TOKEN_BAR                      = new TokenWord("|");
    public static final Token TOKEN_EOF                      = new TokenEOF();

    public TokenString tokenize(String str) {
        return tokenize(makeTokenizer(str), TOKEN_EOF);
    }

    private TokenString tokenize(StreamTokenizer tokenizer, Token closeToken) {
        TokenString str = new TokenString();
        Token currToken = nextToken(tokenizer);
        while ( !currToken.equals(closeToken) ) {
            // check if the curr token is a parenthesis token
            // if it is an open paren, we need to tokenize the enclosed substring seperately
            if (currToken.equals(TOKEN_OPEN_PAREN))
                str.addToken(new TokenParenthesizedTokens(TOKEN_OPEN_PAREN, TOKEN_CLOSE_PAREN,
                                                          tokenize(tokenizer, TOKEN_CLOSE_PAREN)));
            else if (currToken.equals(TOKEN_OPEN_BRACE))
                str.addToken(new TokenParenthesizedTokens(TOKEN_OPEN_BRACE, TOKEN_CLOSE_BRACE,
                                                          tokenize(tokenizer, TOKEN_CLOSE_BRACE)));
            else if (currToken.equals(TOKEN_OPEN_SQUARE_BRACKET))
                str.addToken(new TokenParenthesizedTokens(TOKEN_OPEN_SQUARE_BRACKET,
                                                          TOKEN_CLOSE_SQUARE_BRACKET,
                                                          tokenize(tokenizer,
                                                                   TOKEN_CLOSE_SQUARE_BRACKET)));
            // if it is a close paren, then we found the close paren before the
            // close char, so we are unbalanced
            else if ( currToken.equals(TOKEN_CLOSE_PAREN) ||
                      currToken.equals(TOKEN_CLOSE_BRACE) ||
                      currToken.equals(TOKEN_CLOSE_SQUARE_BRACKET) )
                throw new ParseException("Unbalanced parentheses.");
            // if it is EOF, we reached EOF prematurely (since the close token is not EOF)
            else if ( currToken.equals(TOKEN_EOF) )
                throw new ParseException("Unexpected end of expression.");
            else // nothing special: just add the token to the string
                str.addToken(currToken);
            currToken = nextToken(tokenizer);
        }
        return str;
    }

    private Token nextToken(StreamTokenizer tokenizer) {
        try  {
            tokenizer.nextToken();
            switch (tokenizer.ttype)
            {
                case StreamTokenizer.TT_EOF:
                    return new TokenEOF();
                    
                case StreamTokenizer.TT_WORD:
                    String wordStr = tokenizer.sval;
                    // see if it's a number
                    try  {
                        return new TokenNumber( Double.valueOf(wordStr).doubleValue() );
                    } catch(NumberFormatException ex) {
                        if (wordStr.endsWith("E") || wordStr.endsWith("e")) {
                            try {
                                double x = Double.valueOf(wordStr.substring(0,wordStr.length()-1))
                                                 .doubleValue();
                                Token negtok = nextToken(tokenizer);
                                boolean negative;
                                Token ptok;
                                if (negtok.equals(new TokenWord("-"))) {
                                    ptok = nextToken(tokenizer);
                                    negative = true;
                                }
                                else {
                                    ptok = negtok;
                                    negative = false;
                                }
                                if (!(ptok instanceof TokenNumber))
                                    throw new ParseException("Exponent for " + x + "E<exponent> is not a valid number.");
                                return new TokenNumber(x * Math.pow(10,
                                                        (negative ? -1 : 1) *((TokenNumber) ptok).number()));
                            }
                            catch (NumberFormatException ex2) {}
                        }
                    };
                    return new TokenWord(wordStr);

                case StreamTokenizer.TT_NUMBER:
                    return new TokenNumber(tokenizer.nval);

                default:
                    return new TokenWord( new Character((char) tokenizer.ttype) .toString() );
            }
        } catch( java.io.IOException ex ) {
            System.err.println(ex);
            return null;
        }        
    }
    
    private StreamTokenizer makeTokenizer(String str) {
        StringBufferInputStream stream = new StringBufferInputStream(str);
        StreamTokenizer t = new StreamTokenizer(stream);
        t.resetSyntax();
        t.wordChars( 'A', 'Z' );
        t.wordChars( 'a', 'z' );
        t.wordChars( '0', '9' );
        t.wordChars( '\u00A0', '\u00FF' );
        t.wordChars('.','.');
        t.whitespaceChars( '\u0000', '\u0020' );
        t.whitespaceChars( ' ', ' ' );
        t.commentChar( '#' );
        t.eolIsSignificant( false );
        t.slashSlashComments( false );
        t.slashStarComments( true );
        return t;
    }


    
}
