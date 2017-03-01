package JackCompiler;

import java.util.*;
import java.io.*;

/**
 * Token.java
 * MPCS 52011 - Project 10
 * Created by Alan Cham on 2/25/2017.
 * Defines a Class for tokens / syntactic atoms.
 */
class Token {
    /**
     * Token.TokenType
     * Enum of different Token / terminal element types
     */
    enum TokenType {
        KEYWORD     ("keyword"),
        SYMBOL      ("symbol"),
        INTEGER     ("integerConstant"),
        STRING      ("stringConstant"),
        IDENTIFIER  ("identifier");

        public String text; // String corresponding to keyword

        /**
         * Token.TokenType.TokenType
         * Constructor sets instance's text field.
         * @param text - String corresponding to token type
         */
        TokenType(String text) {
            this.text = text;
        } // end method Token.TokenType.TokenType
    } // end enum Token.TokenType.TokenType

    /**
     * Token.KeywordType
     * Enum of different Keyword token types
     */
    public enum KeywordType {
        CLASS       ("class"),
        CONSTRUCTOR ("constructor"),
        FUNCTION    ("function"),
        METHOD      ("method"),
        FIELD       ("field"),
        STATIC      ("static"),
        VARIABLE    ("var"),
        INT         ("int"),
        CHAR        ("char"),
        BOOLEAN     ("boolean"),
        VOID        ("void"),
        TRUE        ("true"),
        FALSE       ("false"),
        NULL        ("null"),
        THIS        ("this"),
        LET         ("let"),
        DO          ("do"),
        IF          ("if"),
        ELSE        ("else"),
        WHILE       ("while"),
        RETURN      ("return");

        public String text; // String corresponding to keyword

        /**
         * Token.KeywordType.KeywordType
         * Constructor sets instance's text field.
         * @param text - String corresponding to keyword
         */
        KeywordType(String text) {
            this.text = text;
        } // end method Token.KeywordType.KeywordType

        /**
         * Token.KeywordType.getKeywordType
         * Match a String to a corresponding keyword type
         * @param text - the text which should be matched with a KeywordType
         * @return a KeywordType corresponding to the text, or null if no matches
         */
        public static KeywordType getKeywordType(String text) {
            for (KeywordType k : KeywordType.values()) {
                if (text.equals(k.text)) return k;
            }
            return null;
        } // end method Token.KeywordType.getKeywordType
    } // end enum Token.KeywordType

    /**
     * Token.SymbolType
     * Enum of different Symbol token types
     */
    public enum SymbolType {
        OPEN_BRACE      ("{"),
        CLOSE_BRACE     ("}"),
        OPEN_PAREN      ("("),
        CLOSE_PAREN     (")"),
        OPEN_BRACKET    ("["),
        CLOSE_BRACKET   ("]"),
        PERIOD          ("."),
        COMMA           (","),
        SEMI_COLON      (";"),
        PLUS            ("+"),
        MINUS           ("-"),
        MULTIPLY        ("*"),
        DIVIDE          ("/"),
        AND             ("&"),
        OR              ("|"),
        LESS_THAN       ("<"),
        GREATER_THAN    (">"),
        EQUALS          ("="),
        NOT             ("~");

        public String text; // String corresponding to symbol

        /**
         * Token.SymbolType.SymbolType
         * Constructor sets instance's text field.
         * @param text - String corresponding to keyword
         */
        SymbolType(String text) {
            this.text = text;
        } // end method Token.SymbolType.SymbolType

        /**
         * Token.SymbolType.getSymbolType
         * Match a String to a corresponding Symbol type
         * @param text - the text which should be matched with a SymbolType
         * @return a SymbolType corresponding to the text, or null if no matches
         */
        public static SymbolType getSymbolType(String text) {
            for (SymbolType s : SymbolType.values()) {
                if (text.equals(s.text)) return s;
            }
            return null;
        } // end method Token.SymbolType.getSymbolType
    } // end enum Token.SymbolType

    KeywordType keywordType;// keyword type of this token (null if not applicable)
    SymbolType symbolType;  // symbol type of this token (null if not applicable)
    TokenType tokenType;    // type of this token instance
    String text;            // text comprising this token

    /**
     * Token.Token
     * Constructor initializes instance fields to specified params.
     * @param keywordType - keyword type of this token (if appicable), null otherwise
     * @param symbolType - symbol type of this token (if applicable), null otherwise
     * @param tokenType - token type of this token
     * @param text - String comprising this token
     */
    private Token(KeywordType keywordType, SymbolType symbolType, TokenType tokenType, String text) {
        this.keywordType = keywordType;
        this.symbolType = symbolType;
        this.tokenType = tokenType;
        this.text = text;
    } // end method Token.Token

    /**
     * Token.getTokenList
     * Convert a .jack file to a LinkedList of tokens
     * @param f - .jack file to be tokenized
     * @return LinkedList of tokens
     */
    static LinkedList<Token> getTokenList(File f) throws FileNotFoundException {
        LinkedList<Token> list = new LinkedList<>();
        try (Scanner scan = new Scanner(f)) {
            StringBuilder sb = new StringBuilder();
            while (scan.hasNextLine()) {
                String str = trim(scan.nextLine());
                sb.append(str).append("\n");
            }
            tokenizeStringToList(list, sb.toString());
        }
        return list;
    } // end method Token.getTokenList

    /**
     * Token.tokenizeStringToList
     * Tokenize a string and append its tokens to a given list.
     * @param list - list to which the string's tokens should be added
     * @param str - string which should be tokenized
     */
    private static void tokenizeStringToList(LinkedList<Token> list, String str) {
        str = str.trim();
        int index;
        while (!str.isEmpty()) {
            if (str.startsWith("/*")) {
                // comment case
                index = str.indexOf("*/", 2) + 2;
            } else if (str.startsWith("\"")) {
                // string constant case
                index = str.indexOf("\"", 1) + 1;
                String text = str.substring(1, index - 1);
                Token token = new Token(null, null, TokenType.STRING, text);
                list.add(token);
            } else if ((new Scanner(str)).nextLine().matches("\\d+(\\s|.)*")) {
                // number case
                Scanner scan = new Scanner(str).useDelimiter("[^\\d]");
                String text = scan.next();
                Token token = new Token(null, null, TokenType.INTEGER, text);
                list.add(token);
                index = text.length();
            } else if ((new Scanner(str)).nextLine().matches("[\\w&&[^\\d]](\\w)*(\\s|.)*")) {
                // word case
                Scanner scan = new Scanner(str).useDelimiter("[^\\w]");
                String text = scan.next();
                index = text.length();
                if (KeywordType.getKeywordType(text) != null) {
                    Token token = new Token(KeywordType.getKeywordType(text), null, TokenType.KEYWORD, text);
                    list.add(token);
                } else {
                    Token token = new Token(null, null, TokenType.IDENTIFIER, text);
                    list.add(token);
                }
            } else if (SymbolType.getSymbolType(str.substring(0,1)) != null) {
                // symbol case
                String text = str.substring(0,1);
                Token token = new Token(null, SymbolType.getSymbolType(text), TokenType.SYMBOL, text);
                list.add(token);
                index = 1;
            } else {
                // error case
                Scanner scan = new Scanner(str);
                System.err.println("Error: cannot parse " + scan.nextLine());
                return;
            }
            str = str.substring(index).trim();
        }
    } // end method Token.tokenizeStringToList

    /**
     * Token.trim
     * Trims comments and terminal whitespace from string
     * @param s - String to be trimmed
     * @return trimmed string
     */
    private static String trim(String s) {
        int index = s.indexOf("//");
        if (index == 0) {
            return "";
        } else if (index > 0) {
            s = s.substring(0, index);
            return s.trim();
        } else {
            return s.trim();
        }
    } // end method Token.trim

    /**
     * Token.writeTokenList
     * Write a token list to an output file.
     * @param list - list of tokens to be written
     * @param fileName - name of file to be written
     * @throws FileNotFoundException when opening PrintWriter
     */
    static void writeTokenList(LinkedList<Token> list, String fileName) throws FileNotFoundException {
        try (PrintWriter pw = new PrintWriter(fileName)) {
            pw.println("<tokens>");
            for (Token t : list) {
                pw.print("<" + t.tokenType.text + "> ");
                if (t.text.equals(">")) {
                    pw.print("&gt;");
                } else if (t.text.equals("<")) {
                    pw.print("&lt;");
                } else if (t.text.equals("&")) {
                    pw.print("&amp;");
                } else {
                    pw.print(t.text);
                }
                pw.println(" </" + t.tokenType.text + ">");
            }
            pw.println("</tokens>");
        }
    } // end method Token.writeTokenList
} // end class Token