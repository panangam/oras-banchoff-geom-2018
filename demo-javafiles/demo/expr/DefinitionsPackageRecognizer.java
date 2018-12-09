//
//  DefinitionsPackageRecognizer.java
//  Demo
//
//  Created by David Eigen on Fri Aug 16 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.expr;

import mathbuild.*;

import java.io.BufferedReader;

import demo.depend.*;

public class DefinitionsPackageRecognizer {

    private DeclarationRecognizer recognizer_;

    /**
     * @param env the environment to put definitions into
     */
    public DefinitionsPackageRecognizer(Environment env) {
        recognizer_ = new DeclarationRecognizer(env);
    }
    
    /**
     * Reads the given environment file into the environment.
     * The file is given as a java.io.Reader. Blank lines and lines that start with '#' are
     * disregarded.  All other lines are parsed as a definition. Definitions have
     * the form "name = defintion" or "name(args) = definition". In the first case,
     * the definition is parsed and built. The resulting executor is put into the
     * environment as the entry for the name. The second case is a shorthand, and is
     * equivalent to saying "name = func(args){definition}".
     *
     * @param defs the definitions, as a Reader
     */
    public void readDefinitions(java.io.Reader defs) {
        BufferedReader in = new BufferedReader(defs);
        String line;
        demo.util.Set entries = new demo.util.Set();
        try {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.equals("") || line.startsWith("#"))
                    continue;
                recognizer_.declareExpressionOrFunction(line);
                entries.add(recognizer_.resultEntry());
            }
            for (java.util.Enumeration entriesEnum = entries.elements();
                 entriesEnum.hasMoreElements();)
                DependencyManager.remove((Dependable) entriesEnum.nextElement());
        }
        catch (java.io.IOException ex) {
            System.err.println(ex);
        }
    }

    
}
