//
//  SyntaxNodeConstructorBin.java
//  mathbuild
//
//  Created by David Eigen on Thu May 30 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild.impl;

import mathbuild.*;

import mathbuild.SyntaxNode;

public interface SyntaxNodeConstructorBin {
    public SyntaxNode makeSyntaxNode(SyntaxNode right, SyntaxNode left);
}
