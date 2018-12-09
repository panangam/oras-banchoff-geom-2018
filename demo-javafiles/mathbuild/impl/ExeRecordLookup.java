//
//  ExeRecordLookup.java
//  Demo
//
//  Created by David Eigen on Tue Apr 01 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.type.*;
import mathbuild.value.*;

public class ExeRecordLookup implements Executor {

    private Executor recordExe_;
    private String fieldName_;

    /**
     * Creates an executor for looking up & executing a field in a record.
     * @param record the executor producing the record to lookup on
     * @param field the name of the field to look up and execute
     */
    public ExeRecordLookup(Executor record, String field) {
        recordExe_ = record;
        fieldName_ = field;
    }

    public Value execute(Object runID) {
        return ((ValueRecord) recordExe_.execute(runID)).executeField(fieldName_, runID);
    }

    public Type type() {
        return ((TypeRecord) recordExe_.type()).fieldType(fieldName_);
    }

    
}
