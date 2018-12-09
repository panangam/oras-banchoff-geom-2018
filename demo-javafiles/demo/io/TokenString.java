package demo.io;
//
//  TokenString.java
//  mathbuild
//
//  Created by David Eigen on Wed May 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//


public class TokenString {

    private java.util.Vector tokens_;

    public TokenString() {
        tokens_ = new java.util.Vector();
    }

    private TokenString(java.util.Vector tokenVector) {
        tokens_ = tokenVector;
    }

    /**
     * adds the given token to the end of the string
     * @param token the token to add to the end
     */
    public void add(Token token) {
        tokens_.addElement(token);
    }

    /**
     * adds the given token to the end of the string
     * @param token the token to add to the end
     */
    public void addToken(Token token) {
        tokens_.addElement(token);
    }

    /**
     * @return the first token in the string
     */
    public Token first() {
        return (Token) tokens_.firstElement();
    }

    /**
     * @return the last token in the string
     */
    public Token last() {
        return (Token) tokens_.elementAt(tokens_.size() - 1);
    }

    /**
     * @return the "rest" of the token string -- everything after the first token
     */
    public TokenString rest() {
        java.util.Vector s = new java.util.Vector(tokens_.size() - 1);
        for (int i = 1; i < tokens_.size(); ++i)
            s.addElement(tokens_.elementAt(i));
        return new TokenString(s);
    }

    /**
     * @return the length of the token string
     */
    public int length() {
        return tokens_.size();
    }

    /**
     * @return the length of the token string
     */
    public int size() {
        return length();
    }

    /**
     * @return the token at position i
     */
    public Token tokenAt(int i) {
        return (Token) tokens_.elementAt(i);
    }

    /**
     * @return the token string consisting of everything to the left of index (non-inclusive)
     */
    public TokenString leftOf(int index) {
        java.util.Vector s = new java.util.Vector(index);
        for (int i = 0; i < index; ++i) {
            s.addElement(tokens_.elementAt(i));
        }
        return new TokenString(s);
    }

    /**
     * @return the token string consisting of everything to the right of index (non-inclusive)
     */
    public TokenString rightOf(int index) {
        java.util.Vector s = new java.util.Vector(tokens_.size() - index - 1);
        for (int i = index + 1; i < tokens_.size(); ++i)
            s.addElement(tokens_.elementAt(i));
        return new TokenString(s);
    }
    
    
    /**
     * @return whether this token string matches the given string
     */
    public boolean equals(TokenString str) {
        if (str.size() != this.size())
            return false;
        for (int i = 0; i < tokens_.size(); ++i)
            if (! ((Token) tokens_.elementAt(i)).equals( ((Token) str.tokens_.elementAt(i)) ))
                return false;
        return true;
    }

    /**
     * @return string representation of this token string
     */
    public String toString() {
        String str = "";
        for (int i = 0; i < tokens_.size(); ++i)
            str += ((Token) tokens_.elementAt(i)).toString() + (i < tokens_.size()-1 ? " " : "");
        return str;
    }

    /**
     * @param token the token to find in the string
     * @return the first occurence of the token in the string, or -1 if none exists
     */
    public int find(Token token) {
        if (size() == 0)
            return -1;
        return find(token, 0);
    }
    
    /**
     * @param token the token to find in the string
     * @param start the index to start the search from
     * @return the first occurence of the token in the string not before start, or -1 if none exists
     */
    public int find(Token token, int start) {
        for (int i = start; i < tokens_.size(); ++i)
            if (((Token) tokens_.elementAt(i)).equals(token))
                return i;
        return -1;
    }

    /**
     * Finds the position of a token in the string, searching backwards from the end of the string.
     * @param token the token to find in the string
     * @return the last occurence of the token in the string, or -1 if none exists
     */
    public int findBackwards(Token token) {
        if (size() == 0)
            return -1;
        return findBackwards(token, size() - 1);
    }

    /**
     * Finds the position of a token in the string, searching backwards from the given start index.
     * @param token the token to find in the string
     * @param start the index to start the search from
     * @return the last occurence of the token in the string not after start, or -1 if none exists
     */
    public int findBackwards(Token token, int start) {
        for (int i = start; i >= 0; --i)
            if (((Token) tokens_.elementAt(i)).equals(token))
                return i;
        return -1;
    }
    
    /**
     * @param token the token to split the string with
     * @return the strings between each occurence of the token, in order from left to right
     */
    public TokenString[] split(Token token) {
        java.util.Vector strs = new java.util.Vector();
        int start = 0;
        int index;
        while ( (index = find(token, start)) != -1 ) {
            java.util.Vector s = new java.util.Vector(index - start);
            for (int i = 0; i < index - start; ++i)
                s.addElement(tokens_.elementAt(i + start));
            strs.add(new TokenString(s));
            start = index != -1 ? index + 1 : start + 1;
        }
        java.util.Vector s = new java.util.Vector(size() - start);
        for (int i = 0; i < size() - start; ++i)
            s.addElement(tokens_.elementAt(i + start));
        strs.add(new TokenString(s));
        TokenString[] toReturn = new TokenString[strs.size()];
        strs.copyInto(toReturn);
        return toReturn;
    }

    /**
     * Creates an array of String for this token string, where each entry in the array
     * is the string representation of the token in this string.
     * @return an array of String containing the String representations of the tokens in this string
     */
    public String[] toStringArray() {
        String[] array = new String[size()];
        for (int i = 0; i < array.length; ++i)
            array[i] = tokens_.elementAt(i).toString();
        return array;
    }
}
