package JackCompiler;

import static JackCompiler.ParseTree.peek;
import JackCompiler.StatementNode.*;
import JackCompiler.ParseTree.*;
import java.util.ListIterator;
import java.io.PrintWriter;

public class StructureNode extends Node {
    /**
     * StructureType
     * Enum of different program structure patterns
     */
    protected enum StructureType {
        CLASS           ("class",           true),
        CLASS_VAR_DEC   ("classVarDec",     true),
        SUBROUTINE_DEC  ("subroutineDec",   true),
        PARAMETER_LIST  ("parameterList",   true),
        SUBROUTINE_BODY ("subroutineBody",  true),
        VAR_DEC         ("varDec",          true),
        TYPE            ("type",            false),
        CLASS_NAME      ("className",       false),
        SUBROUTINE_NAME ("subroutineName",  false),
        VAR_NAME        ("varName",         false);

        public String text;         // String corresponding to syntax pattern type
        public boolean printable;   // true if structure type should be printed, false otherwise

        /**
         * StructureType.StructureType
         * Constructor initializes text field to specified param
         * @param text - String corresponding to syntax pattern type
         * @param printable - boolean true if structure type should be printed, false otherwise
         */
        StructureType(String text, boolean printable) {
            this.text = text;
            this.printable = printable;
        } // end method StructureType.StructureType
    } // end enum StructureType

    private StructureType structureType;    // structure type to be held by this node
    private int childIndent;                // indentation level for children of this node

    StructureNode(StructureType structureType, ListIterator<Token> iter, int indentLevel) {
        super(NodeType.STRUCTURE, indentLevel);
        this.structureType = structureType;
        this.childIndent = (structureType.printable) ? indentLevel + 1 : indentLevel;
        switch (structureType) {
            case CLASS:
                this.buildClass(iter);
                break;
            case CLASS_VAR_DEC:
                this.buildClassVarDec(iter);
                break;
            case TYPE:
                this.buildType(iter);
                break;
            case SUBROUTINE_DEC:
                this.buildSubroutineDec(iter);
                break;
            case PARAMETER_LIST:
                this.buildParameterList(iter);
                break;
            case SUBROUTINE_BODY:
                this.buildSubroutineBody(iter);
                break;
            case VAR_DEC:
                this.buildVarDec(iter);
                break;
            case CLASS_NAME:
                // identifier
            case SUBROUTINE_NAME:
                // identifier
            case VAR_NAME:
                // identifier
                this.children.add(new TokenNode(iter, this.childIndent));
                break;
            default:
                System.err.println("Error: attempted to make StructureNode from invalid structureType.");
        }
    } // end method StructureNode.StructureNode

    /**
     * StructureNode.buildClass
     * Set this StructureNode instance to a CLASS node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildClass(ListIterator<Token> iter) {
        // 'class'
        this.children.add(new TokenNode(iter, this.childIndent));
        // 'className'
        this.children.add(new StructureNode(StructureType.CLASS_NAME, iter, this.childIndent));
        // '{'
        this.children.add(new TokenNode(iter, this.childIndent));
        // classVarDec*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != Token.TokenType.KEYWORD) break;
            if (token.keywordType != Token.KeywordType.STATIC
                    && token.keywordType != Token.KeywordType.FIELD) break;
            // classVarDec
            this.children.add(new StructureNode(StructureType.CLASS_VAR_DEC, iter, this.childIndent));
        }
        // subroutineDec*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != Token.TokenType.KEYWORD) break;
            if (token.keywordType != Token.KeywordType.CONSTRUCTOR
                    && token.keywordType != Token.KeywordType.FUNCTION
                    && token.keywordType != Token.KeywordType.METHOD) break;
            // subroutineDec
            this.children.add(new StructureNode(StructureType.SUBROUTINE_DEC, iter, this.childIndent));
        }
        // '}'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StructureNode.buildClass

    /**
     * StructureNode.buildClassVarDec
     * Set this StructureNode instance to a CLASS_VAR_DEC node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildClassVarDec(ListIterator<Token> iter) {
        // 'static' | 'field'
        this.children.add(new TokenNode(iter, this.childIndent));
        // type
        this.children.add(new StructureNode(StructureType.TYPE, iter, this.childIndent));
        // varName
        this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        // (',' varName)*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != Token.TokenType.SYMBOL) break;
            if (token.symbolType != Token.SymbolType.COMMA) break;
            // ','
            this.children.add(new TokenNode(iter, this.childIndent));
            // varName
            this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        }
        // ';'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StructureNode.buildClassVarDec

    /**
     * StructureNode.buildType
     * Set this StructureNode instance to a TYPE node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildType(ListIterator<Token> iter) {
        // 'int' | 'char' | 'boolean' | className
        Token token = peek(iter);
        if (token.tokenType == Token.TokenType.KEYWORD) {
            this.children.add(new TokenNode(iter, this.childIndent));
        } else {
            this.children.add(new StructureNode(StructureType.CLASS_NAME, iter, this.childIndent));
        }
    } // end method StructureNode.buildType

    /**
     * StructureNode.buildSubroutineDec
     * Set this StructureNode instance to a SUBROUTINE_DEC node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildSubroutineDec(ListIterator<Token> iter) {
        // ('constructor' | 'function' | 'method')
        this.children.add(new TokenNode(iter, this.childIndent));
        // ('void' | type)
        Token token = peek(iter);
        if (token.tokenType == Token.TokenType.KEYWORD
                && token.keywordType == Token.KeywordType.VOID) {
            this.children.add(new TokenNode(iter, this.childIndent));
        } else {
            this.children.add(new StructureNode(StructureType.TYPE, iter, this.childIndent));
        }
        // subroutineName
        this.children.add(new StructureNode(StructureType.SUBROUTINE_NAME, iter, this.childIndent));
        // '('
        this.children.add(new TokenNode(iter, this.childIndent));
        // parameterList
        this.children.add(new StructureNode(StructureType.PARAMETER_LIST, iter, this.childIndent));
        // ')'
        this.children.add(new TokenNode(iter, this.childIndent));
        // subroutineBody
        this.children.add(new StructureNode(StructureType.SUBROUTINE_BODY, iter, this.childIndent));
    } // end method StructureNode.buildSubroutineDec

    /**
     * StructureNode.buildParameterList
     * Set this StructureNode instance to a PARAMETER_LIST node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildParameterList(ListIterator<Token> iter) {
        Token token = peek(iter);
        if (token.tokenType == Token.TokenType.SYMBOL
                && token.symbolType == Token.SymbolType.CLOSE_PAREN) {
            return;
        }
        // type
        this.children.add(new StructureNode(StructureType.TYPE, iter, this.childIndent));
        // varName
        this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        // (',' type varName)*
        while (true) {
            token = peek(iter);
            if (token.tokenType == Token.TokenType.SYMBOL
                    && token.symbolType == Token.SymbolType.CLOSE_PAREN) {
                break;
            }
            // type
            this.children.add(new StructureNode(StructureType.TYPE, iter, this.childIndent));
            // varName
            this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        }
    } // end method StructureNode.buildParameterList

    /**
     * StructureNode.buildSubroutineBody
     * Set this StructureNode instance to a SUBROUTINE_BODY node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildSubroutineBody(ListIterator<Token> iter) {
        // '{'
        this.children.add(new TokenNode(iter, this.childIndent));
        // varDec*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != Token.TokenType.KEYWORD
                    || token.keywordType != Token.KeywordType.VARIABLE) {
                break;
            }
            // varDec
            this.children.add(new StructureNode(StructureType.VAR_DEC, iter, this.childIndent));
        }
        // statements
        this.children.add(new StatementNode(StatementType.STATEMENTS, iter, this.childIndent));
        // '}'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StructureNode.buildSubroutineBody

    /**
     * StructureNode.buildVarDec
     * Set this StructureNode instance to a VAR_DEC node, and recursively build all nodes
     * in subtree rooted at this node.
     * @param iter - Token list iterator used to retrieve tokens and advance list position
     */
    private void buildVarDec(ListIterator<Token> iter) {
        // 'var'
        this.children.add(new TokenNode(iter, this.childIndent));
        // type
        this.children.add(new StructureNode(StructureType.TYPE, iter, this.childIndent));
        // varName
        this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        // (',' varName)*
        while (true) {
            Token token = peek(iter);
            if (token.tokenType != Token.TokenType.SYMBOL
                    || token.symbolType != Token.SymbolType.COMMA) {
                break;
            }
            // ','
            this.children.add(new TokenNode(iter, this.childIndent));
            // varName
            this.children.add(new StructureNode(StructureType.VAR_NAME, iter, this.childIndent));
        }
        // ';'
        this.children.add(new TokenNode(iter, this.childIndent));
    } // end method StructureNode.buildVarDec

    /**
     * StructureNode.print
     * Prints a StructureNode in xml format to the file.
     * @param pw - PrintWriter used to write to file.
     */
    public void print(PrintWriter pw) {
        if (this.structureType.printable) {
            this.indent(pw);
            pw.println("<" + this.structureType.text + ">");
            for (Node n : this.children) n.print(pw);
            this.indent(pw);
            pw.println("</" + this.structureType.text + ">");
        } else {
            for (Node n : this.children) n.print(pw);
        }
    } // end method StructureNode.print
} // end class StructureNode
