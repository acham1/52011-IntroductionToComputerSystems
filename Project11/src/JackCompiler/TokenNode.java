package JackCompiler;

import JackCompiler.ParseTree.*;
import java.util.*;
import java.io.*;

/**
 * TokenNode.java
 * MPCS 52011 - Project 11
 * Created by Alan Cham on 3/2/2017.
 * Subclass of Node to hold Token specifically
 */
class TokenNode extends Node {
    Token token;    // token payload of this TokenNode

    /**
     * TokenNode.TokenNode
     * Constructor creates a node to hold next Token in list
     * @param iter - ListIterator used to get next token and then advance list position
     * @param indentLevel - indentation level of this TokenNode
     */
    TokenNode(ListIterator<Token> iter, int indentLevel) {
        super(NodeType.TOKEN, indentLevel);
        this.token = iter.next();
    } // end method TokenNode.TokenNode

    /**
     * TokenNode.print
     * Prints a token node in xml format to the file.
     * @param pw - PrintWriter used to write to file.
     */
    void print(PrintWriter pw) {
        this.indent(pw);
        pw.print("<" + this.token.tokenType.text + "> ");
        switch (this.token.text) {
            case ">":
                pw.print("&gt;");
                break;
            case "<":
                pw.print("&lt;");
                break;
            case "&":
                pw.print("&amp;");
                break;
            default:
                pw.print(this.token.text);
                break;
        }
        pw.println(" </" + this.token.tokenType.text + ">");
    } // end method TokenNode.print

    /**
     * TokenNode.writeCode
     * Writes the vm equivalent of the subtree rooted at this node.
     * @param pw - PrintWriter used to write to file.
     * @param symbolTable - symbol table used for identifier look-ups
     */
    void writeCode(PrintWriter pw, SymbolTable symbolTable) {
        // TODO
    } // end method TokenNode.writeCode
} // end class TokenNode
