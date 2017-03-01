package JackCompiler;

import JackCompiler.ParseTree.*;
import java.util.*;
import java.io.*;

/**
 * TokenNode.java
 * MPCS 52011 - Project 10
 * Created by Alan Cham on 2/25/2017.
 * Subclass of Node to hold Token specifically
 */
public class TokenNode extends Node {
    private Token token;    // token payload of this TokenNode

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
    public void print(PrintWriter pw) {
        this.indent(pw);
        pw.print("<" + this.token.tokenType.text + "> ");
        if (this.token.text.equals(">")) {
            pw.print("&gt;");
        } else if (this.token.text.equals("<")) {
            pw.print("&lt;");
        } else if (this.token.text.equals("&")) {
            pw.print("&amp;");
        } else {
            pw.print(this.token.text);
        }
        pw.println(" </" + this.token.tokenType.text + ">");
    } // end method TokenNode.print
} // end class TokenNode
