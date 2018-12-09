//
//  STERecord.java
//  Demo
//
//  Created by David Eigen on Wed Apr 02 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.expr.ste;

import mathbuild.Operator;
import mathbuild.Executor;
import mathbuild.type.*;
import mathbuild.value.*;
import mathbuild.MB;

import java.lang.reflect.*;

import demo.exec.*;
import demo.util.Set;
import demo.expr.ExprHookMaker;

public class STERecord extends SymbolTableEntry implements Executor {

    private java.util.Dictionary members_ = new java.util.Hashtable(5);
    private ValueRecord val_ = new ValueRecord(new String[0], new Executor[0]);
    private TypeRecord type_ = new TypeRecord(new String[0], new Type[0]);

    public STERecord(String name) {
        this .type = RECORD;
        this .name = name;
    }
    
    public void addMember(String name, Executor exe) {
        if (members_.get(name) != null) members_.remove(name);
        members_.put(name, exe);
        makeVal();
    }
    
    public void addMember(String name, Type[] types, Operator op) {
        if (types == null) types = new Type[0];
        if (members_.get(name) != null) members_.remove(name);
        members_.put(name, ExprHookMaker.makeHook(types, op));
        makeVal();
    }

    public void addMember(String name, Operator op) {
        if (members_.get(name) != null) members_.remove(name);
        members_.put(name, ExprHookMaker.makeHook(op));
        makeVal();
    }

    public void addMember(String name, Object obj, String methodDecl) {
        if (members_.get(name) != null) members_.remove(name);
        addMember(name, ExprHookMaker.makeHook(obj, methodDecl));
    }

    public Type type() {
        return type_;
    }
    
    protected Value exec(Object runID) {
        return val_;
    }

    public Value value() {
        return val_;
    }
    
    private void makeVal() {
        java.util.Enumeration namesEnum = members_.keys();
        String[] names = new String[members_.size()];
        Executor[] exes = new Executor[members_.size()];
        Type[] types = new Type[members_.size()];
        int i = 0;
        while (namesEnum.hasMoreElements()) {
            names[i] = (String) namesEnum.nextElement();
            exes[i] = (Executor) members_.get(names[i]);
            types[i] = exes[i].type();
            ++i;
        }
        Exec.begin_nocancel();
        val_ = new ValueRecord(names, exes);
        type_ = new TypeRecord(names, types);
        Exec.end_nocancel();
    }



    public void dependencyUpdateDef(Set updatingObjs) {
        makeVal();
    }

    public void dependencyUpdateVal(Set updatingObjs) {
    }
    



}

