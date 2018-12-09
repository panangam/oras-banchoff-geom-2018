//
//  Environment.java
//  Demo
//
//  Created by David Eigen on Wed Jun 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import java.util.Hashtable;

public class Environment {

    /**
     * An empty environment (shouldn't/can't be mutated)
     */
    public static final Environment EMPTY_ENV = new Environment();
    public static final Environment EMPTY = EMPTY_ENV;

    /**
     * Symbol table for this environment
     */
    private Hashtable table_ = new Hashtable();

    /**
     * previous environment
     */
    private Environment prevEnv_ = null;

    /**
     * Creates an empty environment.
     */
    public Environment() {
    }

    /**
     * @param the name of the variable to look up
     * @return the entry bound to the given name
     */
    public Object lookup(String name) throws VariableNotFoundException {
        Object entry = table_.get(name);
        if (entry != null) {
            if (entry instanceof EnvironmentEntryError)
                ((EnvironmentEntryError) entry).throwException();
            return entry;
        }
        if (prevEnv_ == null)
            throw new VariableNotFoundException(name + " is not defined.");
        return prevEnv_.lookup(name);
    }

    /**
     * Puts an entry into this environment.
     * @param name the name of the identifier to put in the environment
     * @param entry the entry for the identifier
     */
    public void put(String name, Object entry) {
        table_.put(name, entry);
    }

    /**
     * Removes an entry from the local part of this environment.
     * @param name the identifier name to remove from this environment
     */
    public void remove(String name) {
        table_.remove(name);
    }

    /**
     * Removes an entry from the local part of this environment.
     * @param name the identifier name to remove from this environment
     */
    public void removeEntry(Object entry) {
        for (java.util.Enumeration keys = table_.keys();
             keys.hasMoreElements();) {
            Object key = keys.nextElement();
            if (table_.get(key) == entry) {
                table_.remove(key);
                break;
            }
        }
    }
    
    /**
     * Checks to see whether the given identifier is defined in this
     * environment, or any environment that this evironment is an extension of.
     * @return true if name is defined
     */
    public boolean contains(String name) {
        return table_.get(name) != null || (prevEnv_ != null && prevEnv_.contains(name));
    }

    /**
     * Checks to see whether hte given identifier is defined in this
     * environment. Checks only this environment, so true is returned
     * only if the identifier is defined locally.
     * @return whether the name is defined locally in this environment
     */
    public boolean locallyContains(String name) {
        return table_.get(name) != null;
    }

    /**
     * Extends this environment, creating a new environment with the same mappings of
     * the old environment. Entries added to the new environment are more "local". That
     * is, if there is an entry "x" mapped to an STEVariable in the old environment,
     * and a variable "x" is put into the new environemnt mapping to an STEConstant, the
     * STEConstant will be returned when "x" is looked up in the new, extended environment
     * but the STEVariable will still be returned if "x" is looked up in the old environment.
     *
     * @return an extension environment of this environment
     */
    public Environment extend() {
        Environment extended = new Environment();
        extended.prevEnv_ = this;
        return extended;
    }

    /**
     * Extends this environment as in extend(void) above, and also adds the given name
     * and entry to the new environment. In other words, this method extends the
     * environment "with" the given name and entry.
     * @param name the name of the entry
     * @param entry the entry for the entry this environment is extend with
     * @return the extension of this environment, with (name, entry) in the most local level
     */
    public Environment extend(String name, Object entry) {
        Environment extended = this.extend();
        extended.put(name, entry);
        return extended;
    }

    /**
     * Extends this environment as in extend(void) above, and also adds the given names
     * and entries to the new environment. In other words, this method extends the
     * environment "with" the given names and entries. All the given (name,entry) pairs are
     * in the most local part of the environment, so all given names should be unique.
     * @param names the names for the entries
     * @param entries the entries for the entries this environment is extend with
     * @return the extension of this environment, with all (names[i], entries[i]) maps
     *         in the most local level
     */
    public Environment extend(String[] names, Object[] entries) {
        Environment extended = this.extend();
        for (int i = 0; i < names.length; ++i)
            extended.put(names[i], entries[i]);
        return extended;
    }

    /**
     * Combines the given environment with this environment, such that the
     * given environment is more local in the combined environment than this
     * environment. If new entries are put into either environment, the changes
     * are reflected in the combined environment. However, the scope of each
     * environment is not changed. That is, a lookup(.) performed on either this
     * environment or the given environment will have the same result even after
     * append(.) is called.
     * @return the combined environment.
     */
    public Environment append(Environment env) {
        Environment newEnv;
        if (env.prevEnv_ == null)
            newEnv = this.extend();
        else
            newEnv = this.append(env.prevEnv_).extend();
        newEnv.table_ = env.table_;
        return newEnv;
    }

    /**
     * @return all the SymbolTableEntries stored in the local part of this environment
     */
    public java.util.Enumeration localEntries() {
        return table_.elements();
    }


}
