//
//  Identity.java
//  mathbuild
//
//  Created by David Eigen on Fri Jul 26 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.functions;

import mathbuild.*;
import mathbuild.impl.*;
import mathbuild.type.*;
import mathbuild.value.*;

public class Identity implements SyntaxNodeConstructorUn {


private static final Identity instance_ = new Identity();
    /**
     * Returns the (singleton) instance of this class.
     */
    public static Identity inst() {
        return instance_;
    }

    public SyntaxNode makeSyntaxNode(SyntaxNode sn) {
        return sn;
    }

}
