//
//  ParameterID.java
//  mathbuild
//
//  Created by David Eigen on Mon Jul 29 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.ValueFunction;

/**
 * All parameters contain a ParameterID that uniquely identifies a parameter.
 * The ID is can tell what function a parameter comes from, as well as its parameter
 * number and name.
 */
public class ParameterID {

    /**
     * Prefix for the default argument name.
     * Default argument names are formed by appending the argument number + 1 to the prefix.
     */
    public static final String DEFAULT_ARGNAME = "@ARG";

    private int num_;
    private String name_;

    private java.util.Dictionary subparams_ = new java.util.Hashtable();

    /**
     * @param num the parameter number
     * @param name the name of the parameter
     * @param func the function the parameter is from
     */
    public ParameterID(int num, String name) {
        num_ = num;
        name_ = name;
    }

    /**
     * Constructs a ParameterID with the default argument name for the param number
     * @param num the parameter number
     * @param func the function the parameter is from
     */
    public ParameterID(int num) {
        num_ = num;
        name_ = DEFAULT_ARGNAME + (num+1);
    }

    /**
     * Creates a ParameterID for a superfunction of the given funcitons.
     * The name of this parameter is created based on the names of the corresponding
     * paramters of the subfunctions.
     * The ith parameter of the subfunctions are set as subparameters of this.
     * Any of the subfunctions in the array can be null, in which case they will
     * be disregarded as subfunctions.
     * @param num the number of the paramter to create
     * @param funcs the subfunctions
     */
    public ParameterID(int num, ValueFunction[] subfuncs) throws BuildException {
        num_ = num;
        java.util.Vector subparams = new java.util.Vector(subfuncs.length);
        for (int i = 0; i < subfuncs.length; ++i)
            if (subfuncs[i] != null)
                subparams.addElement(subfuncs[i].parameter(num));
        if (subparams.size() == 0) {
            name_ = DEFAULT_ARGNAME + (num+1);
        }
        else {
            name_ = ((ParameterID) subparams.elementAt(0)).name();
            for (int i = 1 ; i < subparams.size(); ++i) {
                if (!name_.equals(((ParameterID) subparams.elementAt(i)).name())) {
                    name_ = DEFAULT_ARGNAME + (num+1);
                    break;
                }
            }
        }
        for (int i = 0; i < subparams.size(); ++i)
            subparams_.put(subparams.elementAt(i), subparams.elementAt(i));
    }


    /**
     * @return the number of the parameter
     */
    public int num() {
        return num_;
    }

    /**
     * @return the name of the parameter
     */
    public String name() {
        return name_;
    }

    /**
     * Adds the given parameter and all of its subparameters to the subparameters
     * of this parameter.
     * The subparameters of a parameter are set to the parameters of functions
     * embedded in another function, in places where auto-application should happen.
     * For example, if h = f + g, then the parameters of h should have the
     * corresponding parameters of f and g as subparameters. However, if h(x) = f(2x) + g,
     * then only the parameters of g are subparameters.
     */
    public void addSubparam(ParameterID param) {
        subparams_.put(param, param);
    }

    /**
     * Checks whether the given parameter is a subparameter of this parameter.
     * This parameter is always a subparameter of this parameter.
     */
    public boolean isSubparam(ParameterID param) {
        if (param == this)
            return true;
        for (java.util.Enumeration params = subparams_.elements();
             params.hasMoreElements();)
            if ( ((ParameterID) params.nextElement()).isSubparam(param) )
                return true;
        return false;
    }


}
