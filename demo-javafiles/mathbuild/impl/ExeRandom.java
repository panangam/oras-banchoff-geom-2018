//
//  ExeRandom.java
//  mathbuild
//
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.value.Value;
import mathbuild.value.ValueScalar;
import mathbuild.type.Type;

/**
 * Executes to a random number between 0 and 1.
 */
public class ExeRandom implements Executor {

    public ExeRandom() {
    }

    public Value execute(Object runID) {
        return new ValueScalar(Math.random());
    }

    public Type type() {
        return MB.TYPE_SCALAR;
    }

}
