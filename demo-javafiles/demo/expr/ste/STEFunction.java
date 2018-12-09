//
//  STEFunction.java
//  Demo
//
//  Created by David Eigen on Mon Aug 12 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package demo.expr.ste;

import demo.exec.*;
import demo.expr.Expression;

public class STEFunction extends STEExpression {

    private String[] paramNames;
    private String bodyDef; // the definition of the body of this function (eg, "x^2" for f(x) = x^2)

    /**
     * @param name the name of the function (eg. "f")
     * @param paramNames the names of the parameters of this function (eg. {"x", "y"})
     * @param bodyDef the definition of the body of this function (eg. "x^2" if f(x) = x^2)
     * @param expression the expression for the function (eg. func(x){x^2} if f(x) = x^2)
     */
    public
    STEFunction( String name, String[] paramNames, String bodyDef, Expression expression ) {
        super(name, expression);

        this .type = FUNCTION;
        this .paramNames = paramNames;
        this .bodyDef = bodyDef;
    }

    /**
     * @return the names of the parameters of this function
     */
    public String[] paramNames() {
        return paramNames;
    }

    /**
     * @return the definition string for the body of this function (eg. "x^2" for f(x) = x^2)
     */
    public String bodyDefinition() {
        return bodyDef;
    }

    /**
     * Sets the expression this STEFunction evaluates.
     * @param expr the expression
     * @param paramNames the names of the parameters of this function
     * @param bodyDef the definition of the body of this function
     */
    public void setExpression(Expression expr, String[] paramNames, String bodyDef)
    throws demo.depend.CircularException {
        Exec.begin_nocancel();
        super.setExpression(expr);
        this.bodyDef = bodyDef;
        this.paramNames = paramNames;
        Exec.end_nocancel();
    }

}
