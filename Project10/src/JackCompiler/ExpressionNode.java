package JackCompiler;

import static JackCompiler.ParseTree.peek;
import JackCompiler.StructureNode.*;
import JackCompiler.ParseTree.*;
import JackCompiler.Token.*;
import java.util.*;
import java.io.*;

/**
 * ExpressionNode.java
 * MPCS 52011 - Project 10
 * Created by Alan Cham on 2/25/2017.
 * Subclass of Node to hold Expression specifically
 */
public class ExpressionNode extends Node {
    /**
     * ExpressionType
     * Enum of different expression types
     */
    enum ExpressionType {
        EXPRESSION          ("expression",      true),
        TERM                ("term",            true),
        EXPRESSION_LIST     ("expressionList",  true),
        SUBROUTINE_CALL     ("subroutineCall",  false),
        OP                  ("op",              false),
        UNARY_OP            ("unaryOp",         false),
        KEYWORD_CONSTANT    ("keywordConstant", false);

        public String text;         // String corresponding to expression type
        public boolean printable;   // true if statement type should be printed, false otherwise

        /**
         * Expressions.Expressions
         * Constructor initializes text field to specified param
         * @param text - String corresponding to the expression type
         * @param printable - boolean true if statement type should be printed, false otherwise
         */
        ExpressionType(String text, boolean printable) {
            this.text = text;
            this.printable = printable;
        } // end method ExpressionType.ExpressionType
    } // end enum ExpressionType

    private ExpressionType expressionType;  // expression type held by this node
    private int childIndent;                // indentation level for children of this node

    /**
     * ExpressionNode.ExpressionNode
     * Constructor creates new node using tokens from TokenList.
     * @param expressionType - expressionType of the node to be create
     * @param iter - ListIterator used to access Tokens
     * @param indentLevel - indentation level of this node
     */
    ExpressionNode(ExpressionType expressionType, ListIterator<Token> iter, int indentLevel) {
        super(NodeType.EXPRESSION, indentLevel);
        this.expressionType = expressionType;
        this.childIndent = (expressionType.printable) ? indentLevel + 1 : indentLevel;
        switch (expressionType) {
            case EXPRESSION:
                this.buildExpression(iter);
                break;
            case TERM:
                this.buildTerm(iter);
                break;
            case SUBROUTINE_CALL:
                this.buildSubroutineCall(iter);
                break;
            case EXPRESSION_LIST:
                this.buildExpressionList(iter);
                break;
            case OP:
                // '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='
            case UNARY_OP:
                // '-' | '~'
            case KEYWORD_CONSTANT:
                // 'true' | 'false' | 'null' | 'this'
                this.children.add(new TokenNode(iter, this.childIndent));
                break;
            default:
                System.err.println("Error: attempted to make StatementNode from invalid StatementType.");
        }
    } // end method Expression.ExpressionNode

    /**
     * Expression.buildExpression
     * Set this Expression node instance to a EXPRESSION node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildExpression(ListIterator<Token> iter) {
        // term
        this.children.add(new ExpressionNode(ExpressionType.TERM, iter, this.childIndent));
        // (op term)*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != TokenType.SYMBOL || !("+-*/&|<>=").contains(token.symbolType.text)) {
                break;
            }
            // op
            this.children.add(new ExpressionNode(ExpressionType.OP, iter, this.childIndent));
            // term
            this.children.add(new ExpressionNode(ExpressionType.TERM, iter, this.childIndent));
        }
    } // end method Expression.buildExpression

    /**
     * Expression.buildTerm
     * Set this Expression node instance to a TERM node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildTerm(ListIterator<Token> iter) {
        Token token = peek(iter);
        iter.next();
        Token token2 = peek(iter);
        iter.previous();
        // integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
        // integerConstant | stringConstant | keywordConstant
        if (token.tokenType == TokenType.INTEGER
                || token.tokenType == TokenType.STRING
                || token.tokenType == TokenType.KEYWORD) {
            // integerConstant | stringConstant | keywordConstant
            this.children.add(new TokenNode(iter, this.childIndent));
            return;
        }
        // '(' expression ')'
        if (token.tokenType == TokenType.SYMBOL && token.symbolType == SymbolType.OPEN_PAREN) {
            // '('
            this.children.add(new TokenNode(iter, this.childIndent));
            // expression
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
            // ')'
            this.children.add(new TokenNode(iter, this.childIndent));
            return;
        }
        // unaryOp term
        if (token.tokenType == TokenType.SYMBOL && ("-~").contains(token.symbolType.text)) {
            // unaryOp
            this.children.add(new ExpressionNode(ExpressionType.UNARY_OP, iter, this.childIndent));
            // term
            this.children.add(new ExpressionNode(ExpressionType.TERM, iter, this.childIndent));
            return;
        }
        // subroutineCall
        if (token.tokenType == TokenType.IDENTIFIER
                && token2.tokenType == TokenType.SYMBOL
                && (token2.symbolType == SymbolType.OPEN_PAREN
                || token2.symbolType == SymbolType.PERIOD)) {
            // subroutineCall
            this.children.add(new ExpressionNode(ExpressionType.SUBROUTINE_CALL, iter, this.childIndent));
            return;
        }
        // varName | varName '[' expression ']'
        // varName
        this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        if (token2.tokenType == TokenType.SYMBOL && token2.symbolType == SymbolType.OPEN_BRACKET) {
            // '['
            this.children.add(new TokenNode(iter, this.childIndent));
            // expression
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
            // ']'
            this.children.add(new TokenNode(iter, this.childIndent));
        }
    } // end method Expression.buildTerm

    /**
     * Expression.buildSubroutineCall
     * Set this Expression node instance to a SUBROUTINE_CALL node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildSubroutineCall(ListIterator<Token> iter) {
        // subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
        iter.next();
        // token after the next token
        Token token2 = peek(iter);
        iter.previous();
        if (token2.tokenType == TokenType.SYMBOL && token2.symbolType == SymbolType.OPEN_PAREN) {
            // subroutineName
            this.children.add(new StructureNode(StructureType.SUBROUTINE_NAME, iter, this.childIndent));
            // '('
            this.children.add(new TokenNode(iter, this.childIndent));
            // expressionList
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION_LIST, iter, this.childIndent));
            // ')'
            this.children.add(new TokenNode(iter, this.childIndent));
        } else {
            // (className | varName)
            this.children.add(new StructureNode(StructureType.CLASS_NAME, iter, this.childIndent));
            // '.'
            this.children.add(new TokenNode(iter, this.childIndent));
            // subroutineName
            this.children.add(new StructureNode(StructureType.SUBROUTINE_NAME, iter, this.childIndent));
            // '('
            this.children.add(new TokenNode(iter, this.childIndent));
            // expressionList
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION_LIST, iter, this.childIndent));
            // ')'
            this.children.add(new TokenNode(iter, this.childIndent));
        }
    } // end method Expression.buildSubroutineCall

    /**
     * Expression.buildExpressionList
     * Set this Expression node instance to a EXPRESSION_LIST node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildExpressionList(ListIterator<Token> iter) {
        // (expression (',' expression)*)?
        Token token = peek(iter);
        if (token.tokenType == TokenType.SYMBOL && token.symbolType == SymbolType.CLOSE_PAREN) {
            return;
        }
        // expression
        this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        // (',' expression)*
        while (true) {
            token = peek(iter);
            if (token.tokenType != TokenType.SYMBOL || token.symbolType != SymbolType.COMMA) {
                break;
            }
            // ','
            this.children.add(new TokenNode(iter, this.childIndent));
            // expression
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        }
    } // end method Expression.buildExpressionList

    /**
     * ExpressionNode.print
     * Prints an expression node in xml format to the file.
     * @param pw - PrintWriter used to write to file.
     */
    public void print(PrintWriter pw) {
        if (this.expressionType.printable) {
            this.indent(pw);
            pw.println("<" + this.expressionType.text + ">");
            for (Node n : this.children) n.print(pw);
            this.indent(pw);
            pw.println("</" + this.expressionType.text + ">");
        } else {
            for (Node n : this.children) n.print(pw);
        }
    } // end method ExpressionNode.print
} // end class ExpressionNode