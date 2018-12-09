package demo.expr;



import mathbuild.*;
import mathbuild.value.*;
import mathbuild.type.*;

import demo.depend.CircularException;
import demo.expr.ste.*;
import demo.depend.*;

public class DeclarationRecognizer {

    public DeclarationRecognizer( Environment env ) {
        super();

            this .environment = env;
    }

    /**
     * Tests whether a given string is composed entirely of alphanumeric characters
     */
    public static  boolean isValidName( String text ) {
        boolean alphanumeric = true;
        char[] textchars = text .toCharArray();
        // make sure the first char is a letter
        if ( (textchars .length == 0) || ! ((65 <= textchars[0] && textchars[0] <= 90) || (97 <= textchars[0] && textchars[0] <= 122)) ) {
            return false;
        }
        for ( int c = 0; c < textchars .length; c++ ) {
            char curr = textchars[c];
            alphanumeric &= (48 <= curr && curr <= 57) || (65 <= curr && curr <= 90) || (97 <= curr && curr <= 122);
            if ( ! alphanumeric ) {
                break;
            }
        }
        return alphanumeric;
    }

    public void declareExpressionOrFunction( String text )
    throws ParseException, BuildException, CircularException {
        int equalsIx = text.indexOf('=');
        int parenIx = text.indexOf('(');
        if (parenIx != -1 && parenIx < equalsIx)
            declareFunction(text);
        else
            declareExpression(text);
    }

    public void declareFunction( String text ) throws ParseException, BuildException, CircularException {
        // get different parts:
        // [0] is name, [1] is params list, [2] is expression
        String[] parts = splitFunctionSpecs( text );
        String name = parts[0];
        String params = parts[1];
        String def = parts[2];
        validateName(name);
        mathbuild.Parser parser = new mathbuild.Parser();
        String expressionDef = "func" + "("+params+")" + "{"+def+"}";
        SyntaxNode sn = parser.parse(expressionDef);
        Environment env = environment.extend(name, new EnvironmentEntryError(new BuildException("You cannot use " + name + " in the definition of " + name + ".")));
        // put it all into the environment
        STEFunction tableEntry = new STEFunction(name,
                                                 splitParamsList(params),
                                                 def,
                                                 new Expression(expressionDef, sn, env));
        // everything is OK: put the function into the environment
        this .environment .put( name, tableEntry );
        resultName = name;
        resultEntry = tableEntry;
    }

    public void declareExpression( String text ) throws ParseException, BuildException, CircularException {
        int equalsIx = text.indexOf('=');
        if (equalsIx == -1)
            throw new ParseException("Expression definition must be in the form name = definition or name(args) = definition");
        String name = text.substring(0,equalsIx).trim();
        validateName(name);
        String expr = text.substring(equalsIx+1);
        mathbuild.Parser parser = new mathbuild.Parser();
        SyntaxNode exprTree = parser.parse(expr);
        // put it into the environment
        Environment env = environment.extend(name, new EnvironmentEntryError(new BuildException("You cannot use " + name + " in the definition of " + name + ".")));
        STEExpression entry = new STEExpression(name, new Expression(expr, exprTree, env));
        // everything is OK: put the function into the environment
        this .environment .put( name, entry );
        resultName = name;
        resultEntry = entry;
    }

    public void declareInterval( String text ) throws ParseException, BuildException, CircularException {
        int equalsIx = text.indexOf('=');
        if (equalsIx == -1)
            throw new ParseException("Definition must be in the form name = start, end, resolution");
        String name = text.substring(0,equalsIx).trim();
        validateName(name);
        Environment env = environment.extend(name, new EnvironmentEntryError(new BuildException("You cannot use " + name + " in the definition of " + name + ".")));
        Expression[] exprs = recognizeRange(text.substring(equalsIx+1), env);
        // make the entry and put it in the environment
        STEInterval entry = new STEInterval(name, exprs[0], exprs[1], exprs[2]);
        // put the table entry in:
        environment .put( name, entry );
        resultName = name;
        resultEntry = entry;
    }

    public void declareVariable( String text ) throws ParseException, BuildException, CircularException {
        int equalsIx = text.indexOf('=');
        if (equalsIx == -1)
            throw new ParseException("Variable definition must be in the form name = start, end, resolution");
        String name = text.substring(0,equalsIx).trim();
        validateName(name);
        Environment env = environment.extend(name, new EnvironmentEntryError(new BuildException("You cannot use " + name + " in the definition of " + name + ".")));
        Expression[] exprs = recognizeRange(text.substring(equalsIx+1), env);
        // make table entry
        STEVariable entry = new STEVariable(name, exprs[0], exprs[1], exprs[2]);
        // put the table entry in:
        environment .put( name, entry );
        resultName = name;
        resultEntry = entry;
    }

    public void declareConstant( String text ) throws ParseException, BuildException {
        int equalsIx = text.indexOf('=');
        if (equalsIx == -1)
            throw new ParseException("Definition must be in the form name = definition");
        String name = text.substring(0,equalsIx).trim();
        validateName(name);
        String expr = text.substring(equalsIx+1);
        mathbuild.Parser parser = new mathbuild.Parser();
        SyntaxNode sn = parser.parse( expr );
        // put it all into the environment
        Environment env = environment.extend(name, new EnvironmentEntryError(new BuildException("You cannot use " + name + " in the definition of " + name + ".")));
        STEConstant tableEntry = new STEConstant( name, MB.exec(sn, env) );
        // everything is OK: put the constant into the environment
        this .environment .put( name, tableEntry );
        resultName = name;
        resultEntry = tableEntry;
    }

    public  String resultName() {
        return resultName;
    }

    public SymbolTableEntry resultEntry() {
        return resultEntry;
    }

    public  boolean containsErrors() {
        return errors .size() > 0;
    }

    public  java.util .Vector errors() {
        return errors;
    }

    private java.util .Vector errors = new java.util .Vector();

    private  String resultName;
    private  SymbolTableEntry resultEntry;

    private  Environment environment;

    private void validateName(String name) {
        // make sure name is alphanumeric
        if ( ! isValidName( name ) )
            throw new ParseException( name + " cannot be the name of an interval, because it contains invalid characters" );
        // make sure the name is not already declared
        if ( environment .locallyContains( name ) )
            throw new BuildException( name + " is already defined." );
        if ( environment.contains(name) ) {
            Object obj = environment.lookup(name);
            if (obj instanceof Dependable && DependencyManager.hasDependentObjects((Dependable) obj))
                throw new BuildException( "There are things dependent on a previous definition of \""+name+"\".\nUse a different name, or remove the dependencies and try again." );
        }
    }

    // text is a string containing "min,max,res".
    // this method returns an array of {minExpr, maxExpr, resExpr}
    private Expression[] recognizeRange(String text, Environment env) throws ParseException, BuildException {
        String[] range = splitRangeSpecs(text);
        String min = range[0], max = range[1], res = range[2];
        // parse the start, end, and resolution
        mathbuild.Parser parser = new mathbuild.Parser();
        SyntaxNode minSN = parser .parse( min );
        SyntaxNode maxSN = parser .parse( max );
        SyntaxNode resSN = parser .parse( res );
        Expression minExpr = new Expression(min, minSN, env);
        Expression maxExpr = new Expression(max, maxSN, env);
        Expression resExpr = new Expression(res, resSN, env);
        // make sure min, max, and res are all scalars
        if ( ! minExpr.returnsScalar() )
            throw new BuildException( "Resolution expression must be a real." );
        if ( ! maxExpr.returnsScalar() )
            throw new BuildException( "Resolution expression must be a real." );
        if ( ! resExpr.returnsScalar() )
            throw new BuildException( "Resolution expression must be a real." );
        return new Expression[]{minExpr, maxExpr, resExpr};
    }

    /**
     * Given a string of the form name(variables)=definition, returns an array
     * of strings name, variables (comma-seperated), and definition.
     */
    private static String[] splitFunctionSpecs( String text ) throws ParseException {
        int openParenIx = text.indexOf('(');
        int closeParenIx = text.indexOf(')');
        int equalsIx = text.indexOf('=');
        if (equalsIx == -1 || openParenIx == -1 || closeParenIx == -1)
            throw new ParseException("Function definition must be in the form name(params) = definition");
        // check that only whitespace is in between the end of the args list and the equals sign
        if (text.substring(closeParenIx+1, equalsIx).trim().length() > 0)
            throw new ParseException("Function definition must be in the form name(params) = definition");
        String name = text.substring(0,openParenIx).trim();
        String argslist = text.substring(openParenIx+1, closeParenIx).trim();
        String def = text.substring(equalsIx+1).trim();
        return new String[]{
            name, argslist, def
        };
    }

    /**
     * Given a string of the form name=start,end,resolution, returns an array of
     * strings name, start, end, and resolution.  Given a string of the form
     * name=start,end,resolution, returns an array of strings
     * name, start, end, resolutoin
     */
    private static String[] splitRangeSpecs( String text ) throws ParseException {
        String[] defs = divide( text, ',' );
        if ( defs.length != 3 ) {
            throw new ParseException( "Incorrect syntax for min,max,res specification." );
        }
        return defs;
    }


    /**
     * Parse the variable list, returning a dictionary of variable names and
     * variables.
     */
    private static String[] splitParamsList( String text ) throws ParseException {
        String[] varnames = divide( text, ',' );
        // make sure each string is only one identifier
        for ( int numString = 0; numString < varnames .length; numString++ ) {
            varnames[numString] = varnames[numString].trim();
            if ( ! isValidName( varnames[numString] ) ) {
                throw new ParseException( "Variables in a function definition must be alphanumeric" );
            }
        }
        return varnames;
    }

    /**
     * Divides the given string up into substrings around the given separator.
     */
    private static
        String[] divide( String text, char sep ) throws ParseException {
            // keep track of the level of parentheses
            int level = 0;
            // use a java.util.Vector to keep track of the positions of separators
            java.util .Vector positions = new java.util .Vector();
            for ( int i = 0; i < text .length(); ++i ) {
                char ch = text .charAt( i );
                if ( ch == '(' ) {
                    ++level;
                }
                if ( ch == ')' ) {
                    --level;
                }
                if ( level == 0 && ch == sep ) {
                    positions .addElement( new Integer( i ) );
                }
                // at no time should level be less than zero
                if ( level < 0 ) {
                    throw new ParseException( "Too many closing parentheses." );
                }
            }
            // level should be zero at the end of the string
            if ( level != 0 ) {
                throw new ParseException( "Not enough closing parentheses." );
            }
            // the end of the string is the final separator
            positions .addElement( new Integer( text .length() ) );
            // construct the substrings
            String[] substrings = new String [ positions .size() ];
            int start = 0;
            for ( int n = 0; n < positions .size(); ++n ) {
                int end = ((Integer) positions .elementAt( n )) .intValue();
                substrings[n] = text .substring( start, end );
                start = end + 1;
            }
            // return them
            return substrings;
        }
    

}


