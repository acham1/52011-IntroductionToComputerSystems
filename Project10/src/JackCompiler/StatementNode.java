package JackCompiler;

import JackCompiler.ExpressionNode.ExpressionType;
import static JackCompiler.ParseTree.peek;
import JackCompiler.StructureNode.*;
import JackCompiler.ParseTree.*;
import JackCompiler.Token.*;
import java.util.*;
import java.io.*;

/**
 * StatementNode.java
 * MPCS 52011 - Project 10
 * Created by Alan Cham on 2/25/2017.
 * Subclass of Node to hold Statement specifically
 */
public class StatementNode extends Node {
    /**
     * StatementType
     * Enum of different statement types
     */
    enum StatementType {
        STATEMENTS          ("statements",      true),
        LET_STATEMENT       ("letStatement",    true),
        IF_STATEMENT        ("ifStatement",     true),
        WHILE_STATEMENT     ("whileStatement",  true),
        DO_STATEMENT        ("doStatement",     true),
        RETURN_STATEMENT    ("returnStatement", true),
        STATEMENT           ("statement",       false);

        public String text;         // String corresponding to statement type
        public boolean printable;   // true if statement type should be printed, false otherwise

        /**
         * Statements.Statements
         * Constructor initializes text field to specified param
         * @param text - String corresponding to the statement type
         * @param printable - boolean true if statement type should be printed, false otherwise
         */
        StatementType(String text, boolean printable) {
            this.text = text;
            this.printable = printable;
        } // end method StatementType.StatementType
    } // end enum StatementType

    private StatementType statementType;    // statement type to be held by this node
    private int childIndent;                // indentation level for children of this node

    StatementNode(StatementType statementType, ListIterator<Token> iter, int indentLevel) {
        super(NodeType.STATEMENT, indentLevel);
        this.statementType = statementType;
        this.childIndent = (statementType.printable) ? (indentLevel + 1) : indentLevel;
        switch (statementType) {
            case STATEMENTS:
                this.buildStatements(iter);
                break;
            case STATEMENT:
                this.buildStatement(iter);
                break;
            case LET_STATEMENT:
                this.buildLetStatement(iter);
                break;
            case IF_STATEMENT:
                this.buildIfStatement(iter);
                break;
            case WHILE_STATEMENT:
                this.buildWhileStatement(iter);
                break;
            case DO_STATEMENT:
                this.buildDoStatement(iter);
                break;
            case RETURN_STATEMENT:
                this.buildReturnStatement(iter);
                break;
            default:
                System.err.println("Error: attempted to make StatementNode from invalid StatementType.");
        }
    } // end method StatementNode.StatementNode

    /**
     * StatementNode.buildStatements
     * Set this Expression node instance to a STATEMENTS node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildStatements(ListIterator<Token> iter) {
        // statement*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType == TokenType.SYMBOL && token.symbolType == SymbolType.CLOSE_BRACE) {
                break;
            }
            // statement
            this.children.add(new StatementNode(StatementType.STATEMENT, iter, this.childIndent));
        }
    } // end method StatementNode.buildStatements

    /**
     * StatementNode.buildStatement
     * Set this Expression node instance to a STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildStatement(ListIterator<Token> iter) {
        Token token = peek(iter);
        // letStatement | ifStatement | whileStatement | doStatement | returnStatement
        if (token.keywordType == KeywordType.LET) {
            this.children.add(new StatementNode(StatementType.LET_STATEMENT, iter, this.childIndent));
        } else if (token.keywordType == KeywordType.IF) {
            this.children.add(new StatementNode(StatementType.IF_STATEMENT, iter, this.childIndent));
        } else if (token.keywordType == KeywordType.WHILE) {
            this.children.add(new StatementNode(StatementType.WHILE_STATEMENT, iter, this.childIndent));
        } else if (token.keywordType == KeywordType.DO) {
            this.children.add(new StatementNode(StatementType.DO_STATEMENT, iter, this.childIndent));
        } else if (token.keywordType == KeywordType.RETURN) {
            this.children.add(new StatementNode(StatementType.RETURN_STATEMENT, iter, this.childIndent));
        }
    } // end method StatementNode.buildStatement

    /**
     * StatementNode.buildLetStatement
     * Set this Expression node instance to a LET_STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildLetStatement(ListIterator<Token> iter) {
        // 'let'
        this.children.add(new TokenNode(iter, this.childIndent));
        // varName
        this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        // ('[' expression ']')?
        Token token = peek(iter);
        if (token.tokenType == TokenType.SYMBOL && token.symbolType == SymbolType.OPEN_BRACKET) {
            // '['
            this.children.add(new TokenNode(iter, this.childIndent));
            // expression
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
            // ']'
            this.children.add(new TokenNode(iter, this.childIndent));
        }
        // '='
        this.children.add(new TokenNode(iter, this.childIndent));
        // expression
        this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        // ';'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StatementNode.buildLetStatement

    /**
     * StatementNode.buildIfStatement
     * Set this Expression node instance to a IF_STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildIfStatement(ListIterator<Token> iter) {
        // 'if'
        this.children.add(new TokenNode(iter, this.childIndent));
        // '('
        this.children.add(new TokenNode(iter, this.childIndent));
        // expression
        this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        // ')'
        this.children.add(new TokenNode(iter, this.childIndent));
        // '{'
        this.children.add(new TokenNode(iter, this.childIndent));
        // statements
        this.children.add(new StatementNode(StatementType.STATEMENTS, iter, this.childIndent));
        // '}'
        this.children.add(new TokenNode(iter, this.childIndent));
        // ('else' '{' statements '}')?
        Token token = peek(iter);
        if (token.tokenType == TokenType.KEYWORD && token.keywordType == KeywordType.ELSE) {
            // 'else'
            this.children.add(new TokenNode(iter, this.childIndent));
            // '{'
            this.children.add(new TokenNode(iter, this.childIndent));
            // statements
            this.children.add(new StatementNode(StatementType.STATEMENTS, iter, this.childIndent));
            // '}'
            this.children.add(new TokenNode(iter, this.childIndent));
        }
    } // end method StatementNode.buildIfStatement

    /**
     * StatementNode.buildWhileStatement
     * Set this Expression node instance to a WHILE_STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildWhileStatement(ListIterator<Token> iter) {
        // 'while'
        this.children.add(new TokenNode(iter, this.childIndent));
        // '('
        this.children.add(new TokenNode(iter, this.childIndent));
        // expression
        this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        // ')'
        this.children.add(new TokenNode(iter, this.childIndent));
        // '{'
        this.children.add(new TokenNode(iter, this.childIndent));
        // statements
        this.children.add(new StatementNode(StatementType.STATEMENTS, iter, this.childIndent));
        // '}'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StatementNode.buildWhileStatement

    /**
     * StatementNode.buildDoStatement
     * Set this Expression node instance to a DO_STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildDoStatement(ListIterator<Token> iter) {
        // 'do'
        this.children.add(new TokenNode(iter, this.childIndent));
        // subroutineCall
        this.children.add(new ExpressionNode(ExpressionType.SUBROUTINE_CALL, iter, this.childIndent));
        // ';'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StatementNode.buildDoStatement

    /**
     * StatementNode.buildReturnStatement
     * Set this Expression node instance to a RETURN_STATEMENT node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildReturnStatement(ListIterator<Token> iter) {
        // 'return'
        this.children.add(new TokenNode(iter, this.childIndent));
        // expression?
        Token token = peek(iter);
        if (token.tokenType != TokenType.SYMBOL || token.symbolType != SymbolType.SEMI_COLON) {
            // expression
            this.children.add(new ExpressionNode(ExpressionType.EXPRESSION, iter, this.childIndent));
        }
        // ';'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StatementNode.buildReturnStatement

    /**
     * StatementNode.print
     * Prints a statement node in xml format to the file.
     * @param pw - PrintWriter used to write to file.
     */
    public void print(PrintWriter pw) {
        if (this.statementType.printable) {
            this.indent(pw);
            pw.println("<" + this.statementType.text + ">");
            for (Node n : this.children) n.print(pw);
            this.indent(pw);
            pw.println("</" + this.statementType.text + ">");
        } else {
            for (Node n : this.children) n.print(pw);
        }
    } // end method StatementNode.print
} // end class StatementNode