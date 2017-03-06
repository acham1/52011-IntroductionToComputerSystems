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
 * MPCS 52011 - Project 11
 * Created by Alan Cham on 3/2/2017.
 * Subclass of Node to hold Statement specifically
 */
class StatementNode extends Node {
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
    void print(PrintWriter pw) {
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

    /**
     * StatementNode.writeCode
     * Writes the vm equivalent of the subtree rooted at this node.
     * @param pw - PrintWriter used to write to file.
     * @param symbolTable - symbol table used for identifier look-ups
     */
    void writeCode(PrintWriter pw, SymbolTable symbolTable) {
        switch (this.statementType) {
            case STATEMENTS:
                for (int i = 0; i < this.children.size(); i++) {
                    this.children.get(i).writeCode(pw, symbolTable);
                }
                break;
            case STATEMENT:
                this.children.get(0).writeCode(pw, symbolTable);
                break;
            case LET_STATEMENT:
                this.writeLetStatement(pw, symbolTable);
                break;
            case IF_STATEMENT:
                this.writeIfStatement(pw, symbolTable);
                break;
            case WHILE_STATEMENT:
                this.writeWhileStatement(pw, symbolTable);
                break;
            case DO_STATEMENT:
                this.writeDoStatement(pw, symbolTable);
                break;
            case RETURN_STATEMENT:
                this.writeReturnStatement(pw, symbolTable);
                break;
            default:
                System.err.println("Warning: tried to compile StatementNode with no valid StatementType.");
        }
    } // end method StatementNode.writeCode

    /**
     * StatementNode.writeLetStatement
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeLetStatement(PrintWriter pw, SymbolTable symbolTable) {
        // 'let' varName ('[' expression ']')? '=' expression ';'
        // varName
        String varName = ((TokenNode) this.children.get(1).children.get(0)).token.text;
        // array indexing case
        if (this.children.size() == 8) {
            pw.println("push " + symbolTable.kindOf(varName).text + " " + symbolTable.indexOf(varName));
            // '[' expression ']'
            this.children.get(3).writeCode(pw, symbolTable);
            pw.println("add");
            pw.println("pop temp 0");
            // '=' expression ';'
            this.children.get(this.children.size() - 2).writeCode(pw, symbolTable);
            pw.println("push temp 0");
            pw.println("pop pointer 1");
            pw.println("pop that 0");
        } else {
            // '=' expression ';'
            this.children.get(3).writeCode(pw, symbolTable);
            pw.println("pop " + symbolTable.kindOf(varName).text + " " + symbolTable.indexOf(varName));
        }
    } // end method StatementNode.writeLetStatement

    /**
     * StatementNode.writeIfStatement
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeIfStatement(PrintWriter pw, SymbolTable symbolTable) {
        int labelNum = symbolTable.labelNum++;
        pw.println("label expression." + labelNum);
        // 'if' '(' expression ')'
        this.children.get(2).writeCode(pw, symbolTable);
        pw.println("if-goto true." + labelNum);
        // ('else' '{' statements '}')?
        if (this.children.size() == 11) this.children.get(9).writeCode(pw, symbolTable);
        pw.println("goto end." + labelNum);
        // case true
        pw.println("label true." + labelNum);
        this.children.get(5).writeCode(pw, symbolTable);
        pw.println("label end." + labelNum);
    } // end method StatementNode.writeIfStatement

    /**
     * StatementNode.writeWhileStatement
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeWhileStatement(PrintWriter pw, SymbolTable symbolTable) {
        int labelNum = symbolTable.labelNum++;
        pw.println("label expression." + labelNum);
        ExpressionNode expression = (ExpressionNode) this.children.get(2);
        expression.writeCode(pw, symbolTable);
        pw.println("if-goto true." + labelNum);
        pw.println("goto false." + labelNum);
        pw.println("label true." + labelNum);
        StatementNode statementsNode = (StatementNode) this.children.get(5);
        statementsNode.writeCode(pw, symbolTable);
        pw.println("goto expression." + labelNum);
        pw.println("label false." + labelNum);
    } // end method StatementNode.writeWhileStatement

    /**
     * StatementNode.writeDoStatement
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeDoStatement(PrintWriter pw, SymbolTable symbolTable) {
        ExpressionNode subroutineCallNode = (ExpressionNode) this.children.get(1);
        subroutineCallNode.writeCode(pw, symbolTable);
        pw.println("pop temp 0");
    } // end method StatementNode.writeDoStatement

    /**
     * StatementNode.writeReturnStatement
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeReturnStatement(PrintWriter pw, SymbolTable symbolTable) {
        if (this.children.size() == 2) {
            pw.println("push constant 0");
        } else {
            this.children.get(1).writeCode(pw, symbolTable);
        }
        pw.println("return");
    } // end method StatementNode.writeReturnStatement
} // end class StatementNode