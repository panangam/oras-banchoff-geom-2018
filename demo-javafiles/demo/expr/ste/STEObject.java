//
//  STEObject.java
//  Demo
//
//  Created by David Eigen on Thu Apr 03 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package demo.expr.ste;

import demo.depend.*;
import demo.expr.ExprObject;

public class STEObject extends SymbolTableEntry {

    private ExprObject obj_;

    public STEObject(String name, ExprObject obj) {
        this.name = name;
        this.type = OBJECT;
        obj_ = obj;
        if (obj instanceof Dependable)
            DependencyManager.setDependency(this, (Dependable) obj);
    }

    public void setObject(ExprObject obj) {
        if (obj_ instanceof Dependable)
            DependencyManager.removeDependency(this, (Dependable) obj_);
        if (obj instanceof Dependable)
            DependencyManager.setDependency(this, (Dependable) obj);
        obj_ = obj;
    }

    public ExprObject object() {
        return obj_;
    }

    public mathbuild.type.Type type() {
        return obj_.objectTableEntry().type();
    }

    protected mathbuild.value.Value exec(Object runID) {
        return obj_.objectTableEntry().execute(runID);
    }
    

}
