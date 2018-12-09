//
//  BuildArguments.java
//  Demo
//
//  Created by David Eigen on Wed Jun 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import java.util.Hashtable;

public class BuildArguments {


    /**
     * some common (and somewhat global) argument keys
     */
    public static final Object APPLYING = new Object();

    /**
     * An empty BuildArguments. Shouldn't/can't be mutated.
     * Contains no args, except for APPLYING, which is false
     */
    public static final BuildArguments EMPTY = new BuildArguments();

    
    private Hashtable table_ = new Hashtable();

    /**
     * previous environment
     */
    private BuildArguments prevEnv_ = null;


    /**
     * Creates an empty arguments environment.
     * Default values for build arguments listed above:
     *   APPLYING = false
     */
    public BuildArguments() {
        put(APPLYING, new Boolean(false));
    }

    /**
     * @param the key of the variable to look up
     * @return the entry bound to the given key
     */
    public Object lookup(Object key) {
        Object entry = table_.get(key);
        if (entry != null) {
            return entry;
        }
        if (prevEnv_ == null)
            throw new VariableNotFoundException("Internal error: " + key + " is not in the arguments environment");
        return prevEnv_.lookup(key);
    }

    /**
     * Puts an entry into this environment.
     * @param key the key of the identifier to put in the environment
     * @param entry the entry for the identifier
     * @return this BuildArguments
     */
    public BuildArguments put(Object key, Object entry) {
        table_.put(key, entry);
        return this;
    }

    /**
     * Removes an entry from the local part of this environment.
     * @param key the identifier key to remove from this environment
     * @return this BuildArguments
     */
    public BuildArguments remove(Object key) {
        table_.remove(key);
        return this;
    }

    /**
     * Removes an entry from the local part of this environment.
     * @param key the identifier key to remove from this environment
     * @return this BuildArguments
     */
    public BuildArguments removeEntry(Object entry) {
        for (java.util.Enumeration keys = table_.keys();
             keys.hasMoreElements();) {
            Object key = keys.nextElement();
            if (table_.get(key) == entry) {
                table_.remove(key);
                break;
            }
        }
        return this;
    }

    /**
     * Checks to see whether the given identifier is defined in this
     * environment, or any environment that this evironment is an extension of.
     * @return true if key is defined
     */
    public boolean contains(Object key) {
        return table_.get(key) != null || (prevEnv_ != null && prevEnv_.contains(key));
    }

    /**
     * Checks to see whether hte given identifier is defined in this
     * environment. Checks only this environment, so true is returned
     * only if the identifier is defined locally.
     * @return whether the key is defined locally in this environment
     */
    public boolean locallyContains(Object key) {
        return table_.get(key) != null;
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
    public BuildArguments extend() {
        BuildArguments extended = new BuildArguments();
        extended.prevEnv_ = this;
        return extended;
    }

    /**
     * Extends this environment as in extend(void) above, and also adds the given key
     * and entry to the new environment. In other words, this method extends the
     * environment "with" the given key and entry.
     * @param key the key of the entry
     * @param entry the entry for the entry this environment is extend with
     * @return the extension of this environment, with (key, entry) in the most local level
     */
    public BuildArguments extend(Object key, Object entry) {
        BuildArguments extended = this.extend();
        extended.put(key, entry);
        return extended;
    }

    /**
     * Extends this environment as in extend(void) above, and also adds the given keys
     * and entries to the new environment. In other words, this method extends the
     * environment "with" the given keys and entries. All the given (key,entry) pairs are
     * in the most local part of the environment, so all given keys should be unique.
     * @param keys the keys for the entries
     * @param entries the entries for the entries this environment is extend with
     * @return the extension of this environment, with all (keys[i], entries[i]) maps
     *         in the most local level
     */
    public BuildArguments extend(Object[] keys, Object[] entries) {
        BuildArguments extended = this.extend();
        for (int i = 0; i < keys.length; ++i)
            extended.put(keys[i], entries[i]);
        return extended;
    }

    /**
     * Extends this BuildArguments env with the value given for the APPLYING arg.
     * Equivalent to extend(APPLYING, new Boolean(val))
     * @param val the value to set APPLYING to
     */
    public BuildArguments extendApplying(boolean val) {
        return extend(APPLYING, new Boolean(val));
    }

    /**
     * Whether APPLYING is true in this BuildArguments (non-local lookup)
     * Equivalent to lookup(APPLYING).booleanValue()
     */
    public boolean applying() {
        return ((Boolean) lookup(APPLYING)).booleanValue();
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
    public BuildArguments append(BuildArguments env) {
        BuildArguments newEnv;
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
