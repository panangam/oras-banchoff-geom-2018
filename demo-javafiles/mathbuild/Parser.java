//
//  Parser.java
//  mathbuild
//
//  Created by David Eigen on Thu Feb 21 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package mathbuild;

import mathbuild.functions.*;
import mathbuild.impl.*;

public class Parser {

    private static ParserRule[] rules_ = new ParserRule[]{
        new ParserRuleParentheses(),
        new ParserRuleListConstruct(),
        new ParserRuleMatrix(),
        new ParserRuleVector(),
        new ParserRuleOpBin(new TokenWord("or"), Or.inst()),
        new ParserRuleOpBin(new TokenWord("and"), And.inst()),
        new ParserRuleOpBin(new TokenWord[]{new TokenWord("<"), new TokenWord(">"), new TokenWord("=")},
                            new SyntaxNodeConstructorBin[]{
                                new LessThan(), new GreaterThan(), EqualsTol.inst()}),
        new ParserRuleOpBin(new TokenWord[]{new TokenWord("+"), new TokenWord("-")},
                            new SyntaxNodeConstructorBin[]{Add.inst(), Subtract.inst()}),
        new ParserRuleOpBin(new TokenWord[]{new TokenWord("*"), new TokenWord("/")},
                            new SyntaxNodeConstructorBin[]{Multiply.inst(), Divide.inst()}),
		new ParserRuleOpBin(new TokenWord("%"), Cross.inst()),
        new ParserRuleOpUn(new TokenWord("-"), Negate.inst()),
        new ParserRuleOpBin(new TokenWord("^"), Exponent.inst()),
        new ParserRuleListAccess(),
        new ParserRuleRangeMinMaxRes(),
        new ParserRuleComponentDerivative(),
        new ParserRuleMutateRestore(),
        new ParserRuleLet(),
        new ParserRuleFunction(),
        new ParserRuleIntegral(),
        new ParserRuleMinMax(),
        new ParserRuleIf(),
        new ParserRuleRecordLookup(),
        new ParserRuleIdentifier(),
        new ParserRuleApplication()
    };

    private static ParserPreprocessor[] preprocessors_ = new ParserPreprocessor[]{
        new ParserPreprocessorRecordLookup(),
        new ParserPreprocessorMinMaxRes(),
        new ParserPreprocessorParenthesize(new TokenWord("func"), 3),
        new ParserPreprocessorParenthesize(new TokenWord("integral"), 2),
        new ParserPreprocessorParenthesize(new TokenWord("if"), 6)
    };

    /**
     * Parses an expression and returns a syntax node.
     * @param expr the expression string
     * @return a syntax node representing the expression string
     */
    public SyntaxNode parse(String expr) throws ParseException {
        Tokenizer tokenizer = new Tokenizer();
        return parse(preprocess(tokenizer.tokenize(expr)));
    }



    /**
     * Parses an expression and returns a syntax node.
     * If this function is used, it should be called on a preprocessed token string.
     * @param expr the expression string
     * @return a syntax node representing the expression string
     */
    public SyntaxNode parse(TokenString expr) throws ParseException {
        if (expr.length() == 0)
            throw new ParseException("Expression expected.");
        ParseException error = null;
        for (int i = 0; i < rules_.length; ++i) {
            try {
                return ((ParserRule) rules_[i]).parse(expr, this);
            }
            catch (RuleNotApplicableException ex) {
                // the rule was not applicable, so do nothing and go on to the next precidence one
            }
        }
        // no valid parse found
        throw new ParseException("Malformed expression.");
    }


    /**
     * Preprocesses a token string, and returns the preprocessed string.
     * @param str the string to preprocess
     * @return the preprocessed string
     */
    public TokenString preprocess(TokenString str) {
        TokenString str2 = new TokenString();
        for (int i = 0; i < str.length(); ++i) {
            Token t = str.tokenAt(i);
            if (t.isType(Token.PARENTHESIZED_TOKENS)) {
                TokenParenthesizedTokens pt = (TokenParenthesizedTokens) str.tokenAt(i);
                str2.addToken(new TokenParenthesizedTokens(pt.openToken(),
                                                            pt.closeToken(),
                                                            preprocess(pt.enclosedString())));
            }
            else {
                str2.addToken(t);
            }
        }
        for (int i = 0; i < preprocessors_.length; ++i)
            str2 = preprocessors_[i].preprocess(str2);
        return str2;
    }

}



class RuleNotApplicableException extends ParseException {
    public RuleNotApplicableException() {super("");}
    public RuleNotApplicableException(String s) {super(s);}
}



abstract class ParserRule {
    public abstract SyntaxNode parse(TokenString str, Parser parser) throws ParseException ;
}


abstract class ParserPreprocessor {
    public abstract TokenString preprocess(TokenString str);
}


class ParserRuleOpUn extends ParserRule {

    private Token token_;
    private SyntaxNodeConstructorUn constructor_;

    /**
     * makes a unary operator rule
     * @param token the token for the unary operator (eg, "cos" for cosine)
     * @param constructor the syntax node constructor (eg, mathbuild.functions.Cosine for cosine)
     */
    public ParserRuleOpUn(Token token, SyntaxNodeConstructorUn constructor) {
        token_ = token;
        constructor_ = constructor;
    }
    
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.first().equals(token_))
            return constructor_.makeSyntaxNode(parser.parse(str.rest()));
        // unary operator is not the first token, so this rule is not applicable
        throw new RuleNotApplicableException(token_.toString() + " was not applied.");
    }
}



class ParserRuleOpBin extends ParserRule {

    private Token[] tokens_;
    private SyntaxNodeConstructorBin[] constructors_;

    /**
     * makes a binary operator rule. This will parse with associativity such that a - b - c is
     * parsed to ((a - b) - c), where "-" is the operator.
     * Lists can be given in order to give operators the same precidence.
     * @param token the tokens for the operator (eg, "+" for addition)
     * @param constructor the syntax node constructor (eg, mathbuild.functions.Add for addition)
     */
    public ParserRuleOpBin(Token token, SyntaxNodeConstructorBin constructor) {
        this(new Token[]{token}, new SyntaxNodeConstructorBin[]{constructor});
    }

    /**
     * makes a binary operator rule. This will parse with associativity such that a - b - c is
     * parsed to ((a - b) - c), where "-" is the operator.
     * Lists can be given in order to give operators the same precidence.
     * @param tokens the tokens for the operators (eg, "+" for addition)
     * @param constructors the syntax node constructors (eg, mathbuild.functions.Add for addition)
     */
    public ParserRuleOpBin(Token[] tokens, SyntaxNodeConstructorBin[] constructors) {
        tokens_ = tokens;
        constructors_ = constructors;
    }

    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        int index = -1;
        int constructorIndex = -1;
        for (int i = 0; i < tokens_.length; ++i) {
            int ix = str.findBackwards(tokens_[i]);
            if (ix > index) {
                index = ix;
                constructorIndex = i;
            }
        }
        // note that finding the operator in the first position doesn't count, since
        // this is a binary operation
        if (index > 0) {
            return constructors_[constructorIndex]
                            .makeSyntaxNode(parser.parse(str.leftOf(index)),
                                            parser.parse(str.rightOf(index)));
        }
        // the operator is not in the string
        throw new RuleNotApplicableException("given binary operators were not applied.");
    }
}


class ParserRuleParentheses extends ParserRule {

    public ParserRuleParentheses() {}

    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.size() == 1 && str.first().type() == Token.PARENTHESIZED_TOKENS)
            return parser.parse(((TokenParenthesizedTokens) str.first()).enclosedString());
        throw new RuleNotApplicableException("Parentheses rule not applicable.");
    }
    
}


class ParserRuleListConstruct extends ParserRule {

    public ParserRuleListConstruct() {}

    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        TokenString[] strs = str.split(Tokenizer.TOKEN_BAR);
        if (strs.length == 1)
            throw new RuleNotApplicableException("List is not being made: there are no bars");
        SyntaxNode[] sns = new SyntaxNode[strs.length];
        for (int i = 0; i < sns.length; ++i)
            sns[i] = parser.parse(strs[i]);
        return new SNListConstruct(sns);
    }

}


class ParserRuleVector extends ParserRule {

    public ParserRuleVector() {}

    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        TokenString[] strs = str.split(Tokenizer.TOKEN_COMMA);
        if (strs.length == 1)
            throw new RuleNotApplicableException("Vector is not being made: there are no commas");
        SyntaxNode[] sns = new SyntaxNode[strs.length];
        for (int i = 0; i < sns.length; ++i)
            sns[i] = parser.parse(strs[i]);
        return new SNVector(sns);
    }
    
}


class ParserRuleMatrix extends ParserRule {

    public ParserRuleMatrix() {}

    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        TokenString[] semicolonSplitStrs = str.split(Tokenizer.TOKEN_SEMICOLON);
        if (semicolonSplitStrs.length == 1)
            throw new RuleNotApplicableException("Matrix is not being made: there are no semicolons");
        TokenString[][] commaSplitStrs = new TokenString[semicolonSplitStrs.length][];
        for (int i = 0; i < commaSplitStrs.length; ++i)
            commaSplitStrs[i] = semicolonSplitStrs[i].split(Tokenizer.TOKEN_COMMA);
        // make sure all rows of the matrix are the same size
        int size = commaSplitStrs[0].length;
        for (int i = 0; i < commaSplitStrs.length; ++i) {
            if (commaSplitStrs[i].length != size)
                throw new ParseException("Matrix rows cannot have different sizes.");
        }
        SyntaxNode[][] sns = new SyntaxNode[commaSplitStrs.length][size];
        for (int i = 0; i < sns.length; ++i)
            for (int j = 0; j < sns[i].length; ++j)
                sns[i][j] = parser.parse(commaSplitStrs[i][j]);
        return new SNMatrix(sns);
    }

}


class ParserRuleRecordLookup extends ParserRule {
    private static final TokenWord COLON = new TokenWord(":");
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        int index = str.findBackwards(COLON);
        if (index == -1)
            throw new RuleNotApplicableException("Record lookup is of form expr : fieldname ...");
        TokenString l = str.leftOf(index);
        TokenString r = str.rightOf(index);
        if (r.size() != 1 || !r.first().isType(Token.WORD))
            throw new RuleNotApplicableException("field name must be a string");
        return new SNRecordLookup(parser.parse(l), ((TokenWord) r.first()).word());
    }
}



class ParserRuleIdentifier extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 1)
            throw new RuleNotApplicableException("Multiple tokens cannot be an identifier.");
        Token t = str.first();
        switch (t.type()) {
            case Token.NUMBER:
                return new SNNumber(((TokenNumber) t).number());
            case Token.WORD:
                return new SNIdentifier(((TokenWord) t).word());
            default:
                throw new RuleNotApplicableException("Wrong token type for identifier");
        }
    }
}


class ParserRuleFunction extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 3 || !str.first().equals(new TokenWord("func")))
            throw new RuleNotApplicableException("Functions are of form func (params) {body}");
        Token args = str.tokenAt(1);
        Token body = str.tokenAt(2);
        String[] argnames;
        if (!args.isType(Token.PARENTHESIZED_TOKENS))
            throw new ParseException("Parameter names for a function must be enclosed in parentheses.");
        TokenString[] argnameStrs =
            ((TokenParenthesizedTokens) args).enclosedString().split(Tokenizer.TOKEN_COMMA);
        argnames = new String[argnameStrs.length];
        for (int i = 0; i < argnames.length; ++i) {
            if (argnameStrs[i].length() > 1)
                throw new ParseException("Function parameters cannot start with numbers, or have parentheses or other special characters.");
            if (argnameStrs[i].length() == 0)
                throw new ParseException("Expected argument name.");
            Token argnameTok = argnameStrs[i].first();
            if (!argnameTok.isType(Token.WORD))
                throw new ParseException("Function parameters cannot start with numbers, or have parentheses or other special characters.");
            if (!((TokenWord) argnameTok).isValidName())
                throw new ParseException("Function parameters cannot start with numbers, or have parentheses or other special characters.");
            argnames[i] = ((TokenWord) argnameTok).word();
        }
        if (!body.isType(Token.PARENTHESIZED_TOKENS))
            throw new ParseException("Function body must be enclosed in parentheses.");
        return new SNFunc(argnames,
                          parser.parse(((TokenParenthesizedTokens) body).enclosedString()));
    }
}


class ParserRuleIntegral extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 2 || !str.first().equals(new TokenWord("integral")))
            throw new RuleNotApplicableException("integral is of the form integral(integrand, varname, lower, upper, resolution)");
        Token argsTok = str.tokenAt(1);
        if (!argsTok.isType(Token.PARENTHESIZED_TOKENS))
            throw new ParseException("integral is of the form integral(integrand, varname, lower, upper, resolution)");
        TokenString[] args = ((TokenParenthesizedTokens) argsTok).enclosedString()
                                .split(Tokenizer.TOKEN_COMMA);
        if (args.length < 4 || args.length > 5)
            throw new ParseException("integral is of the form integral(integrand, varname, lower, upper, resolution)");
        if (args[1].length() != 1 || !args[1].first().isType(Token.WORD) || !((TokenWord) args[1].first()).isValidName())
            throw new ParseException("Differential variable name for integral cannot start with a number, or contain any spaces or other special characters.");
        return new SNIntegral(((TokenWord) args[1].first()).word(), parser.parse(args[0]),
                              parser.parse(args[2]), parser.parse(args[3]),
                              args.length == 5 ? parser.parse(args[4])
                                               : new SNNumber(10));
    }
}

class ParserRuleMinMax extends ParserRule {
    private static final Token MIN = new TokenWord("min");
    private static final Token MAX = new TokenWord("max");
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 2 || !(str.first().equals(MIN) || str.first().equals(MAX)))
            throw new RuleNotApplicableException("min/max of form <min/max>(args)");
        Token head = str.first();
        Token argsTok = str.last();
        if (!argsTok.isType(Token.PARENTHESIZED_TOKENS))
            throw new ParseException(head + " is of the form " + head + "(<args>)");
        TokenString[] args = ((TokenParenthesizedTokens) argsTok)
                                  .enclosedString().split(Tokenizer.TOKEN_COMMA);
        if (args.length < 1)
            throw new ParseException(head + " must get at least one argument");
        SyntaxNode[] nodes = new SyntaxNode[args.length];
        for (int i = 0; i < args.length; ++i)
            nodes[i] = parser.parse(args[i]);
        if (head.equals(MIN))
            return new SNMin(nodes);
        else if (head.equals(MAX))
            return new SNMax(nodes);
        else throw new ParseException("Internal error: head tok should be min or max");
    }
}

class ParserRuleIf extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 6 || !str.first().equals(new TokenWord("if")))
            throw new RuleNotApplicableException("if is of the form \"if (condition) then {trueExpr} else {falseExpr}\"");
        if ( ! ( str.tokenAt(2).equals(new TokenWord("then")) &&
                 str.tokenAt(4).equals(new TokenWord("else")) ) )
            throw new RuleNotApplicableException("if is of the form \"if (condition) then {trueExpr} else {falseExpr}\"");
        return new SNIf(parser.parse(new TokenString(str.tokenAt(1))),
                        parser.parse(new TokenString(str.tokenAt(3))),
                        parser.parse(new TokenString(str.tokenAt(5))));
    }
}


class ParserRuleListAccess extends ParserRule {
    private static final TokenWord DELIM = new TokenWord("@");
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.find(DELIM) == -1)
            throw new RuleNotApplicableException("List access is of form expr @ component ...");
        TokenString[] strs = str.split(DELIM);
        SyntaxNode sn = parser.parse(strs[0]);
        boolean valid = false;
        boolean wraparound = false;
        for (int i = 1; i < strs.length; ++i) {
            TokenString s = strs[i];
            if (s.length() == 0 && !wraparound) {
                wraparound = true;
                valid = false;
            }
            else if (s.length() != 0) {
                sn = new SNListAccess(sn, parser.parse(s), wraparound);
                wraparound = false;
                valid = true;
            }
            else {
                throw new ParseException("List access is of form expr @ component, or expr @@ component.");
            }
        }
        if (valid)
            return sn;
        else
            throw new ParseException("List access is of form expr @ component, or expr @@ component.");
    }
}


class ParserRuleRangeMinMaxRes extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.length() != 3)
            throw new RuleNotApplicableException("min/max/res can only be applied as <name>_<min|max|res>");
        if (!str.tokenAt(1).equals(new TokenWord("_")))
            throw new RuleNotApplicableException("min/max/res can only be applied as <name>_<min|max|res>");
        TokenString rangeTokStr = new TokenString(str.first());
        Token minmaxres = str.tokenAt(2);
        if (minmaxres.equals(new TokenWord("min")))
            return new SNRangeStart(parser.parse(rangeTokStr));
        else if (minmaxres.equals(new TokenWord("max")))
            return new SNRangeEnd(parser.parse(rangeTokStr));
        else if (minmaxres.equals(new TokenWord("res")))
            return new SNRangeRes(parser.parse(rangeTokStr));
        throw new RuleNotApplicableException("min/max/res was not applied");
    }
}


class ParserRuleApplication extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        TokenString funcStr = new TokenString(str.first());
        TokenString argStr = str.rest();
        SyntaxNode app = parser.parse(funcStr);
        // parse arguments with reverse associativity until we hit a non-parenthesis
        // that way, we have f(g)h(x) => (f(g))(h(x))
        int i = 0;
        // glob derivs/comps
        TokenString glob = ParserRuleComponentDerivative.glob(i, argStr);
        app = ParserRuleComponentDerivative.parseGlob(app, glob);
        i += glob.length();        
        while (i < argStr.length() && argStr.tokenAt(i).isType(Token.PARENTHESIZED_TOKENS)) {
            SyntaxNode[] args;
            SyntaxNode argSN = parser.parse(new TokenString(argStr.tokenAt(i)));
            if (argSN instanceof SNVector)
                // was a vector: that means it was actually multiple parameters
                args = ((SNVector) argSN).components();
            else
                args = new SyntaxNode[]{argSN};
            app = new SNApp(app, args);
            ++i;
            // glob derivatives/components
            glob = ParserRuleComponentDerivative.glob(i, argStr);
            app = ParserRuleComponentDerivative.parseGlob(app, glob);
            i += glob.length();            
        }
        // now, i is the index of after the last token in the function to apply
        // we are either at the end, or there are no more parenthesized tokens.
        // if there are no more parenthesized tokens, we apply the function (stored in "app")
        // to the rest of the string
        if (i < argStr.length()) {
            SyntaxNode argSN = parser.parse(argStr.rightOf(i-1));
            SyntaxNode[] args;
            if (argSN instanceof SNVector)
                // was a vector: that means it was actually multiple parameters
                args = ((SNVector) argSN).components();
            else
                args = new SyntaxNode[]{argSN};
            return new SNApp(app, args);
        }
        return app;
    }
}


class ParserRuleLet extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (!str.first().equals(new TokenWord("let")))
            throw new RuleNotApplicableException("Let statement must start with 'let'");
        int length = str.size() - 2;
        if (length < 0)
            throw new ParseException("Let statements must have a body.");
        String[] names = new String[length];
        SyntaxNode[] defs = new SyntaxNode[length];
        TokenString defsStr = str.substring(1, str.length() - 1);
        for (int i = 0; i < length; ++i) {
            Token curr = defsStr.tokenAt(i);
            if (!curr.isType(Token.PARENTHESIZED_TOKENS))
                throw new ParseException("Let statements are of the form 'let (name=def) ... { body }'");
            TokenString def = ((TokenParenthesizedTokens) curr).enclosedString();
            // b/c there can be an = operator in the def, get first index instead of splitting
            int equals = def.find(new TokenWord("="));
            if (equals == -1)
                throw new ParseException("Let statements are of the form 'let (name=def) ... { body }'");
            TokenString nameStr = def.leftOf(equals);
            TokenString defStr = def.rightOf(equals);
            if (nameStr.length() == 1 &&
                nameStr.first().isType(Token.WORD) &&
                ((TokenWord) nameStr.first()).isValidName()) {
                names[i] = ((TokenWord) nameStr.first()).word();
                defs[i] = parser.parse(defStr);
            }
            else if (nameStr.length() == 2 &&
                     nameStr.first().isType(Token.WORD) &&
                     nameStr.tokenAt(1).isType(Token.PARENTHESIZED_TOKENS) &&
                     ((TokenWord) nameStr.first()).isValidName()) {
                names[i] = ((TokenWord) nameStr.first()).word();
                TokenString funcDefStr = new TokenString();
                funcDefStr.addToken(new TokenWord("func"));
                funcDefStr.addToken(nameStr.tokenAt(1));
                funcDefStr.addToken(new TokenParenthesizedTokens(defStr));
                defs[i] = parser.parse(funcDefStr);
            }
            else {
                throw new ParseException("Names in let statements cannot start with numbers, or have spaces or other special characters.");
            }
        }
        return new SNLet(names, defs, parser.parse(new TokenString(str.last())));
    }
}




class ParserRuleMutateRestore extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (!str.first().equals(new TokenWord("set")))
            throw new RuleNotApplicableException("Set/Restore statement must start with 'set'");
        int length = str.size() - 2;
        if (length < 0)
            throw new ParseException("Set/Restore statements must have a body.");
        String[] names = new String[length];
        SyntaxNode[] defs = new SyntaxNode[length];
        TokenString defsStr = str.substring(1, str.length() - 1);
        for (int i = 0; i < length; ++i) {
            Token curr = defsStr.tokenAt(i);
            if (!curr.isType(Token.PARENTHESIZED_TOKENS))
                throw new ParseException("Set/Restore statements are of the form 'let (name=def) ... { body }'");
            TokenString def = ((TokenParenthesizedTokens) curr).enclosedString();
            // b/c there can be an = operator in the def, get first index instead of splitting
            int equals = def.find(new TokenWord("="));
            if (equals == -1)
                throw new ParseException("Set/Restore statements are of the form 'let (name=def) ... { body }'");
            TokenString nameStr = def.leftOf(equals);
            TokenString defStr = def.rightOf(equals);
            if (nameStr.length() == 1 &&
                nameStr.first().isType(Token.WORD) &&
                ((TokenWord) nameStr.first()).isValidName()) {
                names[i] = ((TokenWord) nameStr.first()).word();
                defs[i] = parser.parse(defStr);
            }
            else {
                throw new ParseException("Names in set/restore statements cannot start with numbers, or have spaces or other special characters.");
            }
        }
        return new SNMutateRestore(names, defs, parser.parse(new TokenString(str.last())));
    }
}



class ParserRuleComponentDerivative extends ParserRule {
    public SyntaxNode parse(TokenString str, Parser parser) throws ParseException {
        if (str.size() < 2)
            throw new RuleNotApplicableException("str must have length at least 2 for comp/diriv");
        TokenString glob = glob(1, str);
        if (glob.size() == 0)
            throw new RuleNotApplicableException("no comp/diriv glob found");
        if (glob.size() + 1 < str.size())
            throw new RuleNotApplicableException("glob does not reach end of string");
        SyntaxNode expr = parser.parse(new TokenString(str.first()));
        return parseGlob(expr, glob);
    }

    /**
     * Matches a glob of components and/or derivatives starting at the given index.
     * @param start the index to start matching with
     * @param str the string
     * @return the component/derivative glob that starts at the given start index
     */
    public static TokenString glob(int start, TokenString str) {
        if (start >= str.length())
            return new TokenString();
        if ( ! (str.tokenAt(start).equals(new TokenWord("_")) ||
                str.tokenAt(start).equals(new TokenWord("'"))) )
            return new TokenString();
        int p = start + 1;
        while (p < str.size() && (str.tokenAt(p).equals(new TokenWord("_")) ||
                                  str.tokenAt(p-1).equals(new TokenWord("_")) ||
                                  str.tokenAt(p).equals(new TokenWord("'"))))
            ++p;
        // p is the index after the last token in the function glob
        return str.substring(start, p);
    }

    /**
     * Parses a glob of components/derivatives, and returns the component/derivative
     * nodes w/ expr as the expression child node.
     */
    public static SyntaxNode parseGlob(SyntaxNode expr, TokenString glob) {
        if (glob.size() == 0)
            return expr;
        if (glob.last().equals(new TokenWord("'")))
            return new SNDerivative(parseGlob(expr, glob.leftOf(glob.size()-1)), 0);
        int index = glob.findBackwards(new TokenWord("_"));
        if (index == -1)
            throw new ParseException("glob of components/derivatives has no components or derivatives");
        TokenString varStr = glob.rightOf(index);
        if (varStr.size() > 1)
            throw new ParseException("Improperly formatted index for component/derivative.");
        if (varStr.size() == 0)
            throw new ParseException("Number or name expected for component/derivative.");
        Token varTok = varStr.first();
        if (index > 0 && glob.tokenAt(index - 1).equals(new TokenWord("_"))) {
            // derivative
            if (varTok.isType(Token.NUMBER)) {
                // deriv wrt param index
                double d = ((TokenNumber) varTok).number();
                if (d != (int) d || d <= 0)
                    throw new ParseException("Parameter number to take derivative with respect to must be a positive integer.");
                return new SNDerivative(parseGlob(expr, glob.leftOf(index-1)), (int) d - 1);
            }
            else if (varTok.isType(Token.WORD)) {
                // deriv wrt variable name
                return new SNDerivative(parseGlob(expr, glob.leftOf(index-1)),
                                        ((TokenWord) varTok).word());
            }
            throw new ParseException("Variable or index for component/derivative must be a name or number.");
        }
        if (varTok.isType(Token.NUMBER)) {
            // component
            double compNum = ((TokenNumber) varTok).number();
            if (compNum != (int) compNum || compNum <= 0)
                    throw new ParseException("Component number for vector must be a positive integer.");
            return new SNVectorComponent((int) compNum - 1,
                                         parseGlob(expr, glob.leftOf(index)));
        }
        if (varTok.isType(Token.WORD)) {
            // derivative
            return new SNDerivative(parseGlob(expr, glob.leftOf(index)), ((TokenWord) varTok).word());
        }
        throw new ParseException("Variable or index for component/derivative must be a name or number.");
    }
}





// When firstTok is encountered, parenthesizes numToks tokens together w/ firstTok as the first token
// That is, to preprocess functions as "...func(arglist)(body)..." => "...(func(arglist)(body))..."
// make a ParserPreprocessorParenthesize(new TokenWord("func"), 3);
class ParserPreprocessorParenthesize extends ParserPreprocessor {
    private Token firstTok_;
    private int numToks_;
    public ParserPreprocessorParenthesize(Token firstTok, int numToks) {
        firstTok_ = firstTok;
        numToks_ = numToks;
    }
    public TokenString preprocess(TokenString str) {
        TokenString output = new TokenString();
        for (int i = 0; i < str.length(); ++i) {
            if (str.tokenAt(i).equals(firstTok_)) {
                TokenString groupedStr = new TokenString();
                groupedStr.addToken(str.tokenAt(i));
                for (int j = 1; j < numToks_; ++j) {
                    if (i >= str.length() - 1)
                        throw new ParseException("Unexpected end of expression.");
                    groupedStr.addToken(str.tokenAt(++i));
                }
                output.addToken(new TokenParenthesizedTokens(groupedStr));
            }
            else {
                output.addToken(str.tokenAt(i));
            }
        }
        return output;
    }
}


class ParserPreprocessorMinMaxRes extends ParserPreprocessor {
    private static final TokenWord
        UNDERSCORE = new TokenWord("_"),
        MIN = new TokenWord("min"),
        MAX = new TokenWord("max"),
        RES = new TokenWord("res");
    public TokenString preprocess(TokenString str) {
        TokenString output = new TokenString();
        for (int i = 0; i < str.length(); ++i) {
            if (i < str.length() - 2 &&
                str.tokenAt(i+1).equals(UNDERSCORE) &&
                ( str.tokenAt(i+2).equals(MIN) ||
                  str.tokenAt(i+2).equals(MAX) ||
                  str.tokenAt(i+2).equals(RES) )) {
                output.addToken(new TokenParenthesizedTokens(str.substring(i,i+3)));
                output.addString(str.rightOf(i+2));
                return preprocess(output);
            }
            else {
                output.addToken(str.tokenAt(i));
            }
        }
        return output;
    }
}


// hack: preprocess record lookups to put parens around them, so they are parsed 
// correctly with funciton applications
class ParserPreprocessorRecordLookup extends ParserPreprocessor {
    private static final TokenWord COLON = new TokenWord(":");
    public TokenString preprocess(TokenString str) {
        int i = str.find(COLON);
        if (i == -1) return str;
        TokenString output = str.leftOf(i-1);
		if (i+2 <= str.length()) {
			output.addToken(new TokenParenthesizedTokens(str.substring(i-1,i+2)));
			output.addString(str.rightOf(i+1));
		}
		else {
			output.addToken(new TokenParenthesizedTokens(str.substring(i-1,i+1)));
		}
        return preprocess(output);
    }
}

