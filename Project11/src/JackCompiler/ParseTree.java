package JackCompiler;

import JackCompiler.StructureNode.*;
import java.util.*;
import java.io.*;

/**
 * Token.java
 * MPCS 52011 - Project 11
 * Created by Alan Cham on 3/2/2017.
 * Defines a Class for organizing tokens.
 */
class ParseTree {
    /**
     * ParseTree.NodeType
     * Enum of different ParseTree node types
     */
    protected enum NodeType {
        TOKEN,
        STRUCTURE,
        STATEMENT,
        EXPRESSION
    }

    private Node root;  // root of the parse tree

    /**
     * ParseTree.ParseTree
     * Constructor builds a parse tree organizing the tokens from list
     * @param list - LinkedList of tokens from a .jack file
     */
    ParseTree(LinkedList<Token> list) {
        ListIterator<Token> iter = list.listIterator();
        if (!iter.hasNext()) {
            System.err.println("Error: cannot create ParseTree from empty Token list.");
            return;
        }
        Token token = peek(iter);
        if (token.tokenType != Token.TokenType.KEYWORD || token.keywordType != Token.KeywordType.CLASS) {
            System.err.println("Error: Token list must begin with Keyword Class token.");
            return;
        }
        this.root = new StructureNode(StructureType.CLASS, iter, 0);
    } // end method ParseTree.ParseTree

    /**
     * ParseTree.writeTree
     * Writes a parse tree to a .xml output file.
     * @param fileName - name of file to write
     */
    void writeTree(String fileName) {
        try (PrintWriter pw = new PrintWriter(fileName)) {
            this.root.print(pw);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error: FileNotFoundException when attempting to write file " + fileName);
        }
    } // end method ParseTree.writeTree

    /**
     * ParseTree.writeCode
     * Writes a parse tree to a .vm output file.
     * @param fileName - name of file to write
     */
    void writeCode(String fileName) {
        try (PrintWriter pw = new PrintWriter(fileName)) {
            this.root.writeCode(pw, new SymbolTable());
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error: FileNotFoundException when attempting to write file " + fileName);
        }
    } // end method ParseTree.writeCode

    /**
     * ParseTree.peek
     * Without net change to iterator, return the element that would be returned by iter.next.
     * @param iter - Token list iterator used to write to file
     * @return Token that would be returned by call to iter.next
     */
    static Token peek(ListIterator<Token> iter) {
        Token token = iter.next();
        iter.previous();
        return token;
    } // end method ParseTree.peek

    /**
     * ParseTree.Node
     * Class of Node of Parse Tree
     */
    static abstract class Node {
        LinkedList<Node> children;  // direct children of this node of ParseTree
        NodeType nodeType;          // type of this node
        int indentLevel;            // indentation level of this node

        /**
         * ParseTree.Node.print
         * Method to print a node's contents to a file
         * @param pw - PrintWriter used to print node to file
         */
        abstract void print(PrintWriter pw);

        /**
         * ParseTree.Node.writeCode
         * Method to write a node's compiled contents to file
         * @param pw - PrintWriter used to print node to file
         * @param symbolTable - symbol table used to look-up identifier
         */
        abstract void writeCode(PrintWriter pw, SymbolTable symbolTable);

        /**
         * ParseTree.Node.Node
         * Constructor initializes the nodeType and indentLevel fields with specified parameters.
         * @param nodeType - nodeType of the new node.
         * @param indentLevel - indentation level of the new node.
         */
        Node(NodeType nodeType, int indentLevel) {
            this.nodeType = nodeType;
            this.indentLevel = indentLevel;
            this.children = new LinkedList<>();
        } // end method ParseTree.Node.Node

        /**
         * ParseTree.Node.indent
         * Write indentation to output file.
         * @param pw - PrintWriter for writing to output file
         */
        void indent(PrintWriter pw) {
            String singleIndent = "  ";
            for (int i = 0; i < this.indentLevel; i++) pw.print(singleIndent);
        } // end method ParseTree.Node.indent
    } // end class ParseTree.Node
} // end class ParseTree
