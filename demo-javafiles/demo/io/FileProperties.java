package demo.io;
//
//  FileProperties.java
//  Demo
//
//  Created by David Eigen on Wed Jun 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

/**
 * This class is used for holding a bunch of different properties for an object.
 * Properties are stored as a map of (key, value) pairs, where the key is a String and
 * the value is a Token.
 */
public class FileProperties {

    private static final TokenWord OPEN_TOKEN = new TokenWord("{"),
                                   CLOSE_TOKEN = new TokenWord("}");
    
    private java.util.Dictionary properties_ = new java.util.Hashtable();
    
    /**
     * Constructs an empty Properties map.
     */
    public FileProperties() {
    }

    /**
     * Constructs a Properties map from parsing a TokenNode
     */
    public FileProperties(TokenNode node) {
        this();
        parse(node);
    }

    /**
     * Parses a TokenNode and adds the properties from the TokenNode
     * into this Properties map.
     * @param node the TokenNode to parse
     */
    public void parse(TokenNode node) {
        TokenString str = node.enclosedString();
        if (str.length() % 2 != 0)
            throw new FileParseException("Properties map not properly formatted.");
        for (int i = 0; i < str.length(); i += 2) {
            properties_.put(((TokenWord) str.tokenAt(i)).word(), str.tokenAt(i+1));
        }
    }

    /**
     * Adds the given property to this map.
     * @param key the property's key
     * @param value the value for the property
     */
    public void add(String key, Token value) {
        properties_.put(key, value);
    }

    /**
     * Gets the given property from this map.
     * @param key the property key to look up
     * @return the Token associated with the key, or null if there is none
     */
    public Token get(String key) {
        Object t = properties_.get(key);
        if (t == null) return null;
        return (Token) t;
    }

    /**
     * Checks whether the a given property exists.
     * @param key the property key to look up
     * @return whether the given property exists
     */
    public boolean contains(String key) {
        return properties_.get(key) != null;
    }

    /**
     * Generates the token representation for this property map
     */
    public Token generate() {
        TokenString str = new TokenString();
        java.util.Enumeration keys = properties_.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            str.addToken(new TokenWord(key));
            str.addToken((Token) properties_.get(key));
        }
        return new TokenNode(OPEN_TOKEN, CLOSE_TOKEN, str);
    }
    
}
