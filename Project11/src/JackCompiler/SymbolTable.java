package JackCompiler;

import java.util.*;

/**
 * SymbolTable.java
 * MPCS 52011 - Project 11
 * Created by Alan on 3/2/2017.
 * Defines a class providing services for creating and using a symbol table.
 */
public class SymbolTable {
    /**
     * SymbolTable.SymbolKind
     * Enum of different Token / terminal element types
     */
    enum SymbolKind {
        STATIC  ("static"),
        FIELD   ("this"),
        ARG     ("argument"),
        VAR     ("local"),
        NONE    ("none");

        String text;    // String representation of SymbolKind

        /**
         * SymbolTable.SymbolKind.SymbolKind
         * Constructor initializes text field
         */
        SymbolKind(String text) {
            this.text = text;
        }
    } // end enum SymbolTable.SymbolKind

    String className;                                       // name of current class scope
    static int labelNum;                                    // number used for making unique labels
    private HashMap<String, Properties> classSymbols;       // map of class scope symbols
    private HashMap<String, Properties> subroutineSymbols;  // map of subroutine scope symbols
    private int numStatics;                                 // running count of static variables
    private int numFields;                                  // running count of fields
    private int numArgs;                                    // running count of argument variables
    private int numVars;                                    // running count of variables

    /**
     * SymbolTable.SymbolTable
     * Constructor initializes data structures.
     */
    SymbolTable() {
        this.classSymbols = new HashMap<>();
        this.subroutineSymbols = new HashMap<>();
        this.numStatics = 0;
        this.numFields = 0;
        this.numArgs = 0;
        this.numVars = 0;
    } // end method SymbolTable.SymbolTable

    /**
     * SymbolTable.setClassName
     * Sets this table's class name field to the specified param.
     * @param className - name of this the class to which this symbol table belongs
     */
    void setClassName(String className) {
        this.className = className;
    } // end method SymbolTable.setClassName

    /**
     * SymbolTable.startSubroutine
     * Starts a new subroutine scope and resets subroutine symbol table
     */
    void startSubroutine() {
        this.subroutineSymbols = new HashMap<>();
        this.numArgs = 0;
        this.numVars = 0;
    } // end method SymbolTable.startSubroutine

    /**
     * SymbolTable.define
     * Define a new identifier unless kind is NONE.
     * @param name - name of identifier
     * @param type - type of identifier
     * @param kind - kind of identifier (e.g. static, field, arg, var)
     */
    void define(String name, String type, SymbolKind kind) {
        switch (kind) {
            case STATIC:
                this.classSymbols.put(name, new Properties(kind, type, this.numStatics++));
                break;
            case FIELD:
                this.classSymbols.put(name, new Properties(kind, type, this.numFields++));
                break;
            case ARG:
                this.subroutineSymbols.put(name, new Properties(kind, type, this.numArgs++));
                break;
            case VAR:
                this.subroutineSymbols.put(name, new Properties(kind, type, this.numVars++));
                break;
            default:
                System.err.println("Warning: tried to create symbol with NONE kind.");
        }
    } // end method SymbolTable.define

    /**
     * SymbolTable.VarCount
     * Returns the number of variables of the given kind, defined in current scope.
     * @param kind - kind of identifier (e.g. static, field, arg, var)
     * @returns number of variables of given kind in current scope
     */
    int varCount(SymbolKind kind) {
        switch(kind) {
            case STATIC:
                return this.numStatics;
            case FIELD:
                return this.numFields;
            case ARG:
                return this.numArgs;
            case VAR:
                return this.numVars;
            case NONE:
            default:
                System.err.println("Warning: tried to find count of symbols with NONE kind.");
        }
        return -1;
    } // end method SymbolTable.varCount

    /**
     * SymbolTable.kindOf
     * Returns kind of the named identifier in the current scope, or NONE if unknown.
     * @param name - name of identifier
     * @return kind of the named identifier
     */
    SymbolKind kindOf(String name) {
        if (this.subroutineSymbols.containsKey(name)) {
            return this.subroutineSymbols.get(name).symbolKind;
        } else if (this.classSymbols.containsKey(name)) {
            return this.classSymbols.get(name).symbolKind;
        }
        return SymbolKind.NONE;
    } // end method SymboLTable.kindOf

    /**
     * SymbolTable.typeOf
     * Returns type of the named identifier in the current scope, or "" if unknown.
     * @param name - name of identifier
     * @return type of the named identifier
     */
    String typeOf(String name) {
        if (this.subroutineSymbols.containsKey(name)) {
            return this.subroutineSymbols.get(name).type;
        } else if (this.classSymbols.containsKey(name)) {
            return this.classSymbols.get(name).type;
        }
        System.err.println("Warning: symbol " + name + " has no type.");
        return "";
    } // end method SymbolTable.typeOf

    /**
     * SymbolTable.indexOf
     * Returns index of the named identifier in the current scope, or -1 if unknown.
     * @param name - name of identifier
     * @return index of the named identifier
     */
    int indexOf(String name) {
        if (this.subroutineSymbols.containsKey(name)) {
            return this.subroutineSymbols.get(name).number;
        } else if (this.classSymbols.containsKey(name)) {
            return this.classSymbols.get(name).number;
        }
        return -1;
    } // end method SymbolTable.indexOf

    /**
     * SymbolTable.Properties
     * Class for organizing/storing symbol properties
     */
    private class Properties {
        SymbolKind symbolKind;  // kind of symbol (e.g. static, field, arg, var)
        String type;            // symbol type text
        int number;             // sequence number of symbol

        /**
         * SymbolTable.Properties
         * Constructor initializes all fields with specified params.
         * @param symbolKind - kind of symbol (e.g. static, field, arg, var)
         * @param type - symbol type text
         * @param number - sequence number of symbol
         */
        Properties(SymbolKind symbolKind, String type, int number) {
            this.symbolKind = symbolKind;
            this.number = number;
            this.type = type;
        } // end method SymbolTable.Properties.Properties
    } // end class SymbolTable.Properties
} // end class SymbolTable
