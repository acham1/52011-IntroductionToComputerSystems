package JackCompiler;

import static JackCompiler.ParseTree.peek;
import JackCompiler.StructureNode.*;
import JackCompiler.SymbolTable.*;
import JackCompiler.ParseTree.*;
import JackCompiler.Token.*;
import java.util.*;
import java.io.*;

/**
 * ExpressionNode.java
 * MPCS 52011 - Project 11
 * Created by Alan Cham on 3/2/2017.
 * Subclass of Node to hold Expression specifically
 */
class ExpressionNode extends Node {
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

        String text;         // String corresponding to expression type
        boolean printable;   // true if statement type should be printed, false otherwise

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
        // integerConstant | stringConstant
        if (token.tokenType == TokenType.INTEGER
                || token.tokenType == TokenType.STRING) {
            // integerConstant | stringConstant
            this.children.add(new TokenNode(iter, this.childIndent));
            return;
        }
        // keywordConstant
        if (token.tokenType == TokenType.KEYWORD) {
            this.children.add(new ExpressionNode(ExpressionType.KEYWORD_CONSTANT, iter, childIndent));
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
            this.children.add(new StructureNode(StructureType.UNKNOWN_NAME, iter, this.childIndent));
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
    void print(PrintWriter pw) {
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

    /**
     * ExpressionNode.writeCode
     * Writes the vm equivalent of the subtree rooted at this node.
     * @param pw - PrintWriter used to write to file.
     * @param symbolTable - symbol table used for identifier look-ups
     */
    void writeCode(PrintWriter pw, SymbolTable symbolTable) {
        switch (this.expressionType) {
            case EXPRESSION:
                this.children.get(0).writeCode(pw, symbolTable);
                for (int i = 2; i < this.children.size(); i += 2) {
                    this.children.get(i).writeCode(pw, symbolTable);
                    this.children.get(i - 1).writeCode(pw, symbolTable);
                }
                break;
            case TERM:
                this.writeTerm(pw, symbolTable);
                break;
            case SUBROUTINE_CALL:
                this.writeSubroutineCall(pw, symbolTable);
                break;
            case EXPRESSION_LIST:
                for (int i = 0; i < this.children.size(); i += 2) this.children.get(i).writeCode(pw, symbolTable);
                break;
            case OP:
                this.writeOp(pw);
                break;
            case UNARY_OP:
                this.writeUnaryOp(pw);
                break;
            case KEYWORD_CONSTANT:
                this.writeKeywordConstant(pw, symbolTable);
                break;
            default:
                System.err.println("Error: attempted to compile ExpressionNode from invalid ExpressionType.");
        }
    } // end method ExpressionNode.writeCode

    /**
     * ExpressionNode.writeOp
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     */
    private void writeOp(PrintWriter pw) {
        SymbolType symbol = ((TokenNode) this.children.get(0)).token.symbolType;
        if (symbol == SymbolType.PLUS) {
            pw.println("add");
        } else if (symbol == SymbolType.MINUS) {
            pw.println("sub");
        } else if (symbol == SymbolType.MULTIPLY) {
            pw.println("call Math.multiply 2");
        } else if (symbol == SymbolType.DIVIDE) {
            pw.println("call Math.divide 2");
        } else if (symbol == SymbolType.AND) {
            pw.println("and");
        } else if (symbol == SymbolType.OR) {
            pw.println("or");
        } else if (symbol == SymbolType.LESS_THAN) {
            pw.println("lt");
        } else if (symbol == SymbolType.GREATER_THAN) {
            pw.println("gt");
        } else if (symbol == SymbolType.EQUALS) {
            pw.println("eq");
        }
    } // end method ExpressionNode.writeOp

    /**
     * ExpressionNode.writeUnaryOp
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     */
    private void writeUnaryOp(PrintWriter pw) {
        // '-' | '~;
        SymbolType symbolType = ((TokenNode) this.children.get(0)).token.symbolType;
        if (symbolType == SymbolType.NOT) {
            pw.println("not");
        } else if (symbolType == SymbolType.MINUS) {
            pw.println("neg");
        }
    } // end method ExpressionNode.writeUnaryOp

    /**
     * ExpressionNode.writeKeywordConstant
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeKeywordConstant(PrintWriter pw, SymbolTable symbolTable) {
        // 'true' | 'false' | 'null' | 'this'
        KeywordType keywordType = ((TokenNode) this.children.get(0)).token.keywordType;
        if (keywordType == KeywordType.TRUE) {
            pw.println("push constant 1");
            pw.println("neg");
        } else if (keywordType == KeywordType.FALSE || keywordType == KeywordType.NULL) {
            pw.println("push constant 0");
        } else if (keywordType == KeywordType.THIS) {
            pw.println((symbolTable.indexOf("this") != -1) ? "push argument 0" : "push pointer 0");
        }
    } // end method ExpressionNode.writeKeywordConstant

    /**
     * ExpressionNode.writeTerm
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeTerm(PrintWriter pw, SymbolTable symbolTable) {
        NodeType zeroNodeType = this.children.get(0).nodeType;
        if (zeroNodeType == NodeType.TOKEN) {
            // integerConstant | stringConstant | '(' expression ')'
            Token token = ((TokenNode) this.children.get(0)).token;
            if (token.tokenType == TokenType.INTEGER) {
                // integerConstant
                pw.println("push constant " + token.text);
            } else if (token.tokenType == TokenType.STRING) {
                // stringConstant
                pw.println("push constant " + token.text.length());
                pw.println("call String.new 1");
                for (int i = 0; i < token.text.length(); i++) {
                    pw.println("push constant " + (int) (token.text.charAt(i)));
                    pw.println("call String.appendChar 2");
                }
            } else if (token.tokenType == TokenType.SYMBOL) {
                // '(' expression ')'
                this.children.get(1).writeCode(pw, symbolTable);
            }
        } else if (zeroNodeType == NodeType.EXPRESSION) {
            // subroutineCall | unaryOp | keywordConstant
            ExpressionNode zeroExpressionNode = (ExpressionNode) this.children.get(0);
            if (zeroExpressionNode.expressionType == ExpressionType.UNARY_OP) {
                this.children.get(1).writeCode(pw, symbolTable);
            }
            zeroExpressionNode.writeCode(pw, symbolTable);
        } else {
            // varName | varName '[' expression ']'
            String varName = ((TokenNode) this.children.get(0).children.get(0)).token.text;
            SymbolKind kind = symbolTable.kindOf(varName);
            pw.println("push " + kind.text + " " + symbolTable.indexOf(varName));
            // '[' expression ']'
            if (this.children.size() == 4) {
                this.children.get(2).writeCode(pw, symbolTable);
                pw.println("add");
                pw.println("pop pointer 1");
                pw.println("push that 0");
            }
        }
    } // end method ExpressionNode.writeTerm

    /**
     * ExpressionNode.writeSubroutineCall
     * Write the vm equivalent of the subtree rooted at this node
     * @param pw - PrintWriter used to write to file
     * @param symbolTable - symbol table used for identifier look-ups
     */
    private void writeSubroutineCall(PrintWriter pw, SymbolTable symbolTable) {
        SymbolType symbolType = ((TokenNode) this.children.get(1)).token.symbolType;
        if (symbolType == SymbolType.OPEN_PAREN) {
            // subroutineName '(' expressionList ')'
            String subroutineName = ((TokenNode) this.children.get(0).children.get(0)).token.text;
            String functionName = symbolTable.className + "." + subroutineName;
            pw.println((symbolTable.indexOf("this") == -1) ? "push pointer 0" : "push argument 0");
            this.children.get(2).writeCode(pw, symbolTable);
            // minimum 1 argument, since method has self object as arg 0
            int numArgs = this.children.get(2).children.size();
            numArgs = (numArgs == 0) ? 1 : numArgs/2 + 2;
            pw.println("call " + functionName + " " + numArgs);
        } else {
            // (className | varName) '.' subroutineName '(' expressionList ')'
            String name = ((TokenNode) this.children.get(0).children.get(0)).token.text;
            String subroutineName = ((TokenNode) this.children.get(2).children.get(0)).token.text;
            if (symbolTable.kindOf(name) == SymbolKind.NONE) {
                // className
                String functionName = name + "." + subroutineName;
                this.children.get(4).writeCode(pw, symbolTable);
                int numArgs = this.children.get(4).children.size();
                numArgs = (numArgs == 0) ? 0 : numArgs/2 + 1;
                pw.println("call " + functionName + " " + numArgs);
            } else {
                // varName
                String className = symbolTable.typeOf(name);
                SymbolKind kind = symbolTable.kindOf(name);
                pw.println("push " + kind.text + " " + symbolTable.indexOf(name));
                this.children.get(4).writeCode(pw ,symbolTable);
                int numArgs = this.children.get(4).children.size();
                numArgs = (numArgs == 0) ? 1 : numArgs/2 + 2;
                pw.println("call " + className + "." + subroutineName + " " + numArgs);
            }
        }
    } // end method ExpressionNode.writeSubroutineCall
} // end class ExpressionNode