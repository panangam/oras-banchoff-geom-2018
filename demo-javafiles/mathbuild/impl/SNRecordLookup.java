//
//  SNRecordLookup.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//


package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.*;
import mathbuild.type.*;

public class SNRecordLookup extends SyntaxNode {

    private SyntaxNode recordSN_;
    private String fieldName_;

    /**
     * Creates a syntax node for looking up & executing a field in a record.
     * @param record the expression producing the record to lookup on
     * @param field the name of the field to look up and execute
     */
    public SNRecordLookup(SyntaxNode record, String field) {
        recordSN_ = record;
        fieldName_ = field;
    }

    public SyntaxNode[] children() {
        return new SyntaxNode[]{recordSN_};
    }

    public Executor build(Environment env, ExeStack funcParams, BuildArguments buildArgs) {
        Executor recordExe = recordSN_.build(env, funcParams, buildArgs);
        Type t = recordExe.type();
        if (!t.isType(Type.RECORD))
            throw new BuildException("Member lookup can only be performed on a record.");
        if (!((TypeRecord) t).containsField(fieldName_))
            throw new BuildException("Member " + fieldName_ + " is not defined.");
        return new ExeRecordLookup(recordExe, fieldName_);
    }

    public SyntaxNode derivative(ParameterID wrtvar, SyntaxNode dwrtvar,
                                 Environment env, ExeStack funcParams) {
        throw new BuildException("Can't take the derivative of a record lookup.");
    }

    public SyntaxNode simplify() {
        return this;
    }

    public String toString() {
        return "(SN-record-lkp " + fieldName_ + " " + recordSN_ + ")";
    }

    public void findDependencies(Environment env, demo.util.Set deps, demo.util.Set ranges) {
        recordSN_.findDependencies(env, deps, ranges);
    }

}

