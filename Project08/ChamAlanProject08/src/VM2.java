/** Alan Cham
 *  Project 8: VMII - Program Control
 *  February 8, 2017
 *  Extends Project 7: VMI */

import java.io.*;
import java.util.*;

public class VM2 {

    private static final boolean DBG = false;            // when true, program will make some printouts for debugging
    private static final String INPUT_EXT = ".vm";      // input file extension
    private static final String OUTPUT_EXT = ".asm";    // output file extension
    private static final int DEFAULT_ARG2 = 0;          // default value of command's arg2

    /** Enumeration of memory segment types */
    private enum SEGMENT_SUPERTYPE {
        ADDRESSPOINTER,     // must be dereferenced to obtain address (e.g. local, argument, this, that)
        ADDRESS,            // actual address (e.g. pointer, temp)
        VIRTUAL,            // constant
        STATIC,             // static
    }

    /** Enumeration of memory segments */
    private enum SEGMENT {
        ARGUMENT    ("argument", SEGMENT_SUPERTYPE.ADDRESSPOINTER, "ARG"),
        LOCAL       ("local",   SEGMENT_SUPERTYPE.ADDRESSPOINTER, "LCL"),
        THIS        ("this",    SEGMENT_SUPERTYPE.ADDRESSPOINTER, "THIS"),
        THAT        ("that",    SEGMENT_SUPERTYPE.ADDRESSPOINTER, "THAT"),
        POINTER     ("pointer", SEGMENT_SUPERTYPE.ADDRESS, "3"),
        TEMP        ("temp",    SEGMENT_SUPERTYPE.ADDRESS, "5"),
        STATIC      ("static",  SEGMENT_SUPERTYPE.STATIC, "16"),
        CONSTANT    ("constant", SEGMENT_SUPERTYPE.VIRTUAL, null);

        private String token;           // token name of the segment
        private SEGMENT_SUPERTYPE type; // type of segment
        private String value;           // value of pointer or address

        private SEGMENT(String token, SEGMENT_SUPERTYPE type, String value) {
            this.token = token;
            this.type = type;
            this.value = value;
        }
    }

    /** Enumeration of possible command super-types */
    private enum CMD_SUPERTYPE {
        ARITHMETIC_LOGICAL,
        MEMORY_ACCESS,
        PROGRAM_FLOW,
        FUNCTION_CALLING,
        OTHER // error type
    }

    /** Enumeration of possible vm command types */
    private enum COMMAND_TYPE {
        ADD       ("add",     CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        SUB       ("sub",     CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        NEG       ("neg",     CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        EQ        ("eq",      CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        GT        ("gt",      CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        LT        ("lt",      CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        AND       ("and",     CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        OR        ("or",      CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        NOT       ("not",     CMD_SUPERTYPE.ARITHMETIC_LOGICAL),
        PUSH      ("push",    CMD_SUPERTYPE.MEMORY_ACCESS),
        POP       ("pop",     CMD_SUPERTYPE.MEMORY_ACCESS),
        LABEL     ("label",   CMD_SUPERTYPE.PROGRAM_FLOW),
        GOTO      ("goto",    CMD_SUPERTYPE.PROGRAM_FLOW),
        IF        ("if-goto", CMD_SUPERTYPE.PROGRAM_FLOW),
        FUNCTION  ("function", CMD_SUPERTYPE.FUNCTION_CALLING),
        RETURN    ("return",  CMD_SUPERTYPE.FUNCTION_CALLING),
        CALL      ("call",    CMD_SUPERTYPE.FUNCTION_CALLING),
        OTHER     ("",        CMD_SUPERTYPE.OTHER);

        private String token;               // token in VM language that identifies subtype
        private CMD_SUPERTYPE supertype;    // supertype of the command type

        private COMMAND_TYPE(String token, CMD_SUPERTYPE supertype) {
            this.token = token;
            this.supertype = supertype;
        }

        /** Determines if the command is a comparison command
         *  Args:       void
         *  Returns:    boolean - true if the command is a comparison command, false otherwise */
        private boolean isComparison() {
            return this == EQ || this == GT || this == LT;
        }

        /** Determines if the command is a binary or unary arithmetic operation
         *  Args:       void
         *  Returns:    boolean - true if command is a non-comparison unary arithmetic operation, false otherwise */
        private boolean isUnary() {
            return this == NEG || this == NOT;
        }

        /** Determines if the command is a binary or unary arithmetic operation
         *  Args:       void
         *  Returns:    boolean - true if command is a non-comparison binary arithmetic operation, false otherwise */
        private boolean isBinary() {
            return this == ADD || this == SUB || this == AND || this == OR;
        }
    }

    /** Constructs a Parser to parse the VM input file
     *  Then constructs a CodeWriter to write asm code into outut file */
    public static void main(String[] args) {
        // validate arguments or else terminate
        if (!validArgs(args)) System.exit(1);
        // allocate array of parsed input files
        Parser[] parsedInputFiles = new Parser[args.length];
        // check that each input file exists, then parse
        for (int i = 0; i < args.length; i++) {
            File vmFile = new File(args[i]);
            if (!vmFile.exists()) {
                System.err.println("Error: file \"" + args[i] + "\" does not exist.");
                System.exit(1);
            }
            // parse input file, since it exists
           parsedInputFiles[i] = new Parser(vmFile);
        }
        // output file name based on parent directory's name
        File inFile = parsedInputFiles[0].vmFile;
        File parent = inFile.getParentFile();
        File asmFile = new File(parent, parent.getName() + OUTPUT_EXT);
        // translate and write to output file
        CodeWriter cw = new CodeWriter(asmFile, parsedInputFiles);
        try {
            System.out.println("Finished writing file \"" + asmFile.getCanonicalPath() + "\".");
        } catch (IOException ioe) {
            System.out.println("Wrote file \"" + asmFile.getName() + "\" to the input file directory.");
        }
    }

    /** Validates the command-line inputs
     *  Args:       String[] args - the array of input arguments
     *  Returns:    boolean - true if inputs are valid, false otherwise */
    private static boolean validArgs(String[] args) {
        // check that there's at least 1 argument
        if (args.length < 1) {
            System.err.println("Error: expected at least one command-line argument, the names of input .vm files.");
            return false;
        }
        // check that each file has right type/file extension
        for (int i = 0; i < args.length; i++) {
            if (!args[i].endsWith(INPUT_EXT)) {
                System.err.println("Error: input file \"" + args[i] + "\" does not end in " + INPUT_EXT);
                return false;
            }
        }
        return true;
    }

    /** Reads VM commands, parses them, and strips white-space/comments */
    private static class Parser {

        File vmFile = null;                     // the input .vm file which was parsed to make this instance
        LinkedList<Command> commandList = null; // linkedlist of input commands
        Command currentCommand = null;          // current command
        ListIterator<Command> iter = null;      // iterator for linkedlist of input lines

        /** Constructor: loads input file and stores all commands in linkedlist
         *  Args:       File vmFile - input .vm file
         *  Returns:    Parser - new instance of Parser */
        private Parser(File vmFile) {
            this.vmFile = vmFile;
            this.commandList = new LinkedList<Command>();
            try(
                    FileReader fr = new FileReader(vmFile);
                    BufferedReader br = new BufferedReader(fr);
            ) {
                if (DBG) System.out.println("Parsing file: " + vmFile.getCanonicalPath());
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    // strip the comments and terminal white space
                    line = clean(line);
                    // convert to command and add to list if cleaned line is non-empty
                    if (line != null && !line.isEmpty()) this.commandList.add(new Command(line));
                }
                // set iterator to start of linkedlist
                this.iter = this.commandList.listIterator(0);
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: FileNotFoundException for input file " + vmFile.getPath());
                System.exit(1);
            } catch (IOException ioe) {
                System.err.println("Error: IOException when reading from input file.");
                System.exit(1);
            }
        }

        /** Checks if there are more commands to read
         *  Args:       void
         *  Returns:    boolean - true if there are more commands, false otherwise */
        private boolean hasMoreCommands() {
            if (this.iter == null) return false;
            return this.iter.hasNext();
        }

        /** Changes current command to the next available one
         *  Args:       void
         *  Returns:    boolean - true if successfully advanced, false otherwise */
        private boolean advance() {
            if (this.iter.hasNext()) {
                // advance to next command if available
                this.currentCommand = this.iter.next();
                return true;
            }
            System.err.println("Error: tried to advance when no commands remain.");
            return false;
        }

        /** Returns the type field of the current command
         *  Args:       void
         *  Returns:    COMMAND_TYPE - the type of the current command */
        private COMMAND_TYPE commandType() {
            // null command cannot have a type
            if (currentCommand == null) {
                System.err.println("Error: tried to read type from null command.");
                return COMMAND_TYPE.OTHER;
            }
            return this.currentCommand.type;
        }

        /** For a raw input string from vm file, strips the comments and terminal white space
         *  Args:       String line - input line from .vm file
         *  Returns:    String - containing input line with excess whitespace and comments removed */
        private static String clean(String line) {
            if (line == null) return null;
            // check if line contains comment
            int index = line.indexOf("//");
            if (index == -1) return line.trim();
            if (index == 0) return "";
            return line.substring(0, index).trim();
        }
    }

    /** Writes translated asm code to output file */
    private static class CodeWriter {

        /** Constructor: iterates through parsed input commands and translate and writes each one to output file
         *  Args:       File asmFile - output .asm file
         *              Parser parsed - the result of constructing Parser by parsing input .vm file
         *  Returns:    void */
        private CodeWriter(File asmFile, Parser[] parsedInputFiles) {
            try (
                    PrintWriter pw = new PrintWriter(asmFile);
            ) {
                // identifier number used for distinguishing labels
                int identifier = 0;
                // write bootstrap code to the output file
                writeInit(pw, identifier++);
                // for each parsed file, append its translatation to asm code
                for (int i = 0; i < parsedInputFiles.length; i++) {
                    Parser p = parsedInputFiles[i];
                    String fileName = p.vmFile.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    String functionName = "";
                    while (p.hasMoreCommands()) {
                        p.advance();
                        if (DBG) System.out.println(p.currentCommand.line);
                        String translated = vmToASM(p.currentCommand, identifier, fileName, functionName);
                        if (translated == null || translated.isEmpty()) {
                            System.err.println("Error: failed to translate line: " + p.currentCommand.line);
                        } else {
                            // update name of function which we are currently within
                            functionName = updateFunctionName(p.currentCommand, functionName);
                            // write translated line to output file
                            pw.print(translated);
                            identifier++;
                        }
                    }
                }
                pw.flush();
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: FileNotFoundException when writing to file " + asmFile.getName());
                System.exit(1);
            }
        }

        /** Returns string naming the function currently being defined
         *  Args:       Command command - the current command which might start or ends a function definition
         *              String functionName - current functionName
         *  Returns:    String - name of function being defined */
        private static String updateFunctionName(Command command, String functionName) {
            // command must be a non-null non-error type
            if (command == null || command.type == COMMAND_TYPE.OTHER) return functionName;
            // if arg is a "function" command, set current context function name to this command's arg1
            if (command.type == COMMAND_TYPE.FUNCTION) return command.arg1;
            // all other commands do not change current context's function name
            return functionName;
        }

        /** Print bootstrap code to output file
         *  Args:       PrintWriter pw - the printwriter used to print the code
         *              int identifier - number for distinguishing labels
         *  Returns:    void */
        private static void writeInit(PrintWriter pw, int identifier) {
            pw.println("@256");
            pw.println("D=A");
            pw.println("@SP");
            pw.println("M=D");
            Command callInit = new Command();
            callInit.arg1 = "Sys.init";
            callInit.arg2 = 0;
            callInit.type = COMMAND_TYPE.CALL;
            pw.print(vmToASM(callInit, identifier, null, null));
        }

        /** Translate a vm Command to an asm String
         *  Args:       Command command - individual parsed vm command
         *              int identifier - number used for distinguishing labels
         *              String fileName - name of file, used for static variable access
         *              String functionName - name of function which this command is part of
         *  Returns:    String containing asm code corresponding to vm command */
        private static String vmToASM(Command command, int identifier, String fileName, String functionName) {
            StringBuilder sb = new StringBuilder();
            switch(command.type) {
                case ADD:
                case AND:
                case OR:
                case SUB:
                    buildBinaryArithmetic(command.type, sb);
                    break;
                case NEG:
                case NOT:
                    buildUnaryArithmetic(command.type, sb);
                    break;
                case EQ:
                case GT:
                case LT:
                    buildCompare(command.type, sb, identifier);
                    break;
                case POP:
                    buildPop(sb, command, fileName);
                    break;
                case PUSH:
                    buildPush(sb, command, fileName);
                    break;
                case LABEL:
                    buildLabel(sb, command, functionName);
                    break;
                case GOTO:
                    buildGoTo(sb, command, functionName);
                    break;
                case IF:
                    buildIf(sb, command, functionName);
                    break;
                case FUNCTION:
                    buildFunction(sb, command);
                    break;
                case RETURN:
                    buildReturn(sb, command);
                    break;
                case CALL:
                    buildCall(sb, command, identifier);
                    break;
                default:
                    break;
            }
            // output string from stringbuilder
            return sb.toString();
        }

        /** Builds string containing asm translation of vm "label" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              String functionName - name of function which this command is part of
         *  Returns:    void */
        private static void buildLabel(StringBuilder sb, Command command, String functionName) {
            sb.append("(" + functionName + "$" + command.arg1 + ")\n");
        }

        /** Builds string containing asm translation of vm "goto" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              String functionName - name of function which this command is part of
         *  Returns:    void */
        private static void buildGoTo(StringBuilder sb, Command command, String functionName) {
            sb.append("@" + functionName + "$" + command.arg1 + "\n");
            sb.append("0;JMP\n");
        }

        /** Builds string containing asm translation of vm "if" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              String functionName - name of function which this command is part of
         *  Returns:    void */
        private static void buildIf(StringBuilder sb, Command command, String functionName) {
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("@" + functionName + "$" + command.arg1 + "\n");
            sb.append("D;JNE\n");
        }

        /** Builds string containing asm translation of vm "function" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *  Returns:    void */
        private static void buildFunction(StringBuilder sb, Command command) {
            sb.append("(" + command.arg1 + ")\n");
            sb.append("@" + command.arg2 + "\n");
            sb.append("D=A\n");
            sb.append("@" + command.arg1 + ".endInitLocals\n");
            sb.append("D;JEQ\n");
            sb.append("(" + command.arg1 + ".initLocals)\n");
            sb.append("@SP\n");
            sb.append("M=M+1\n");
            sb.append("A=M-1\n");
            sb.append("M=0\n");
            sb.append("D=D-1\n");
            sb.append("@" + command.arg1 + ".initLocals\n");
            sb.append("D;JGT\n");
            sb.append("(" + command.arg1 + ".endInitLocals)\n");
        }

        /** Builds string containing asm translation of vm "return" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *  Returns:    void */
        private static void buildReturn(StringBuilder sb, Command command) {
            // FRAME = LCL
            sb.append("@LCL\n");
            sb.append("D=M\n");
            sb.append("@FRAME\n");
            sb.append("M=D\n");
            // RET = *(FRAME-5)
            sb.append("@5\n");
            sb.append("D=A\n");
            sb.append("@FRAME\n");
            sb.append("D=M-D\n");
            sb.append("A=D\n");
            sb.append("D=M\n");
            sb.append("@RET\n");
            sb.append("M=D\n");
            // *ARG = pop()
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("@ARG\n");
            sb.append("A=M\n");
            sb.append("M=D\n");
            // SP = ARG+1
            sb.append("@ARG\n");
            sb.append("D=M+1\n");
            sb.append("@SP\n");
            sb.append("M=D\n");
            // THAT = *(FRAME-1)
            sb.append("@FRAME\n");
            sb.append("MD=M-1\n");
            sb.append("A=D\n");
            sb.append("D=M\n");
            sb.append("@THAT\n");
            sb.append("M=D\n");
            // THIS = *(FRAME-2)
            sb.append("@FRAME\n");
            sb.append("MD=M-1\n");
            sb.append("A=D\n");
            sb.append("D=M\n");
            sb.append("@THIS\n");
            sb.append("M=D\n");
            // ARG = *(FRAME-3)
            sb.append("@FRAME\n");
            sb.append("MD=M-1\n");
            sb.append("A=D\n");
            sb.append("D=M\n");
            sb.append("@ARG\n");
            sb.append("M=D\n");
            // LCL = *(FRAME-4)
            sb.append("@FRAME\n");
            sb.append("MD=M-1\n");
            sb.append("A=D\n");
            sb.append("D=M\n");
            sb.append("@LCL\n");
            sb.append("M=D\n");
            // goto RET
            sb.append("@RET\n");
            sb.append("A=M\n");
            sb.append("0;JMP\n");
        }

        /** Builds string containing asm translation of vm "call" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              int identifier - number for distinguishing labels
         *  Returns:    void */
        private static void buildCall(StringBuilder sb, Command command, int identifier) {
            // push return-address
            sb.append("@RETURN." + identifier + "\n");
            sb.append("D=A\n");
            sb.append("@SP\n");
            sb.append("AM=M+1\n");
            sb.append("A=A-1\n");
            sb.append("M=D\n");
            // push LCL
            sb.append("@LCL\n");
            sb.append("D=M\n");
            sb.append("@SP\n");
            sb.append("AM=M+1\n");
            sb.append("A=A-1\n");
            sb.append("M=D\n");
            // push ARG
            sb.append("@ARG\n");
            sb.append("D=M\n");
            sb.append("@SP\n");
            sb.append("AM=M+1\n");
            sb.append("A=A-1\n");
            sb.append("M=D\n");
            // push THIS
            sb.append("@THIS\n");
            sb.append("D=M\n");
            sb.append("@SP\n");
            sb.append("AM=M+1\n");
            sb.append("A=A-1\n");
            sb.append("M=D\n");
            // push THAT
            sb.append("@THAT\n");
            sb.append("D=M\n");
            sb.append("@SP\n");
            sb.append("AM=M+1\n");
            sb.append("A=A-1\n");
            sb.append("M=D\n");
            // ARG = SP-n-5
            sb.append("@5\n");
            sb.append("D=A\n");
            sb.append("@" + command.arg2 + "\n");
            sb.append("D=D+A\n");
            sb.append("@SP\n");
            sb.append("D=M-D\n");
            sb.append("@ARG\n");
            sb.append("M=D\n");
            // LCL = SP
            sb.append("@SP\n");
            sb.append("D=M\n");
            sb.append("@LCL\n");
            sb.append("M=D\n");
            // goto f
            sb.append("@" + command.arg1 + "\n");
            sb.append("0;JMP\n");
            // (return-address)
            sb.append("(RETURN." + identifier + ")\n");
        }

        /** Builds string containing asm translation of vm "push" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              String fileName - name of file, used for static variable access
         *  Returns:    void */
        private static void buildPush(StringBuilder sb, Command command, String fileName) {
            SEGMENT s = command.getSegment();
            // if no valid segment found, do nothing
            if (s == null) return;
            switch (s.type) {
                case ADDRESSPOINTER:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    sb.append("@" + s.value + "\n");
                    sb.append("A=D+M\n");
                    sb.append("D=M\n");
                    break;
                case ADDRESS:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    sb.append("@" + s.value + "\n");
                    sb.append("A=D+A\n");
                    sb.append("D=M\n");
                    break;
                case VIRTUAL:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    break;
                case STATIC:
                    sb.append("@" + fileName + "." + command.arg2 + "\n");
                    sb.append("D=M\n");
                    break;
                default:
                    break;
            }
            sb.append("@SP\n");
            sb.append("M=M+1\n");
            sb.append("A=M-1\n");
            sb.append("M=D\n");
        }

        /** Builds string containing asm translation of vm "pop" command
         *  Args:       Command command - individual parsed vm command
         *              StringBuilder sb - stringbuilder where this function's output is stored
         *              String fileName - name of file, used for static variable access
         *  Returns:    void */
        private static void buildPop(StringBuilder sb, Command command, String fileName) {
            // access the segment type of the command
            SEGMENT s = command.getSegment();
            // if no valid segment found, do nothing
            if (s == null) return;
            switch(s.type) {
                case ADDRESSPOINTER:
                    sb.append("@" + s.value + "\n");
                    sb.append("D=M\n");
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=D+A\n");
                    sb.append("@R13\n");
                    sb.append("M=D\n");
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("@R13\n");
                    sb.append("A=M\n");
                    sb.append("M=D\n");
                    break;
                case ADDRESS:
                    sb.append("@" + s.value + "\n");
                    sb.append("D=A\n");
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=D+A\n");
                    sb.append("@R13\n");
                    sb.append("M=D\n");
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("@R13\n");
                    sb.append("A=M\n");
                    sb.append("M=D\n");
                    break;
                case VIRTUAL:
                    // language does not support popping to constant
                    return;
                case STATIC:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("@" + fileName + "." + command.arg2 + "\n");
                    sb.append("M=D\n");
                    break;
                default:
                    break;
            }
        }

        /** Builds string containing asm translation of non-comparison unary arithmetic operation
         *  Args:       COMMAND_TYPE type - identifies of command is lt, gt, eq or other
         *              StringBuilder sb - where the output asm code of this function is stored
         *  Returns:    void */
        private static void buildUnaryArithmetic(COMMAND_TYPE type, StringBuilder sb) {
            // only operates on non-comparison unary commands
            if (!type.isUnary()) return;
            sb.append("@SP\n");
            sb.append("A=M-1\n");
            sb.append(type == COMMAND_TYPE.NEG ? "M=-M\n" : "M=!M\n");
        }

        /** Builds string containing asm translation of a non-comparison binary arithmetic operation
         *  Args:       COMMAND_TYPE type - the type of vm command being translated
         *              StringBuilder sb - where the output asm code of this function is stored
         *  Returns:    void */
        private static void buildBinaryArithmetic(COMMAND_TYPE type, StringBuilder sb) {
            // only operates on non-comparison binary commands
            if (!type.isBinary()) return;
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("A=A-1\n");
            if (type == COMMAND_TYPE.ADD) {
                sb.append("M=D+M\n");
            } else if (type == COMMAND_TYPE.SUB) {
                sb.append("M=M-D\n");
            } else if (type == COMMAND_TYPE.AND) {
                sb.append("M=D&M\n");
            } else {
                sb.append("M=D|M\n");
            }
        }

        /** Builds string containing asm translation of lt, gt, or eq command
         *  Args:       COMMAND_TYPE type - identifies of command is lt, gt, eq or other
         *              StringBuilder sb - where the output asm code of this function is stored
         *              int identifier - number used for distinguishing labels
         *  Returns:    void */
        private static void buildCompare(COMMAND_TYPE type, StringBuilder sb, int identifier) {
            // only operates on comparison commands
            if (!type.isComparison()) return;
            String trueLabel = "TRUE." + identifier;
            String falseLabel = "FALSE." + identifier;
            String endLabel = "END." + identifier;
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("A=A-1\n");
            sb.append("D=D-M\n");
            sb.append("@" + trueLabel + "\n");
            if (type == COMMAND_TYPE.GT) {
                // gt is true if difference is negative
                sb.append("D;JLT\n");
            } else if (type == COMMAND_TYPE.LT) {
                // lt is true if difference is positive
                sb.append("D;JGT\n");
            } else {
                // eq is true if difference is zero
                sb.append("D;JEQ\n");
            }
            // set D=0 if false
            sb.append("D=0\n");
            sb.append("@" + endLabel + "\n");
            sb.append("0;JMP\n");
            sb.append("(" + trueLabel + ")\n");
            // set D=-1 if true
            sb.append("D=-1\n");
            sb.append("(" + endLabel + ")\n");
            sb.append("@SP\n");
            sb.append("A=M-1\n");
            // write D into top of stack
            sb.append("M=D\n");
        }
    }

    /** Class for containing information about a .vm command */
    private static class Command {

        COMMAND_TYPE type = null;   // command's type
        String line;                // original line of .vm text
        String arg1 = null;         // command's first argument
        int arg2 = DEFAULT_ARG2;    // command's second argument
        SEGMENT segment = null;     // for memory access commands, the segment operand

        // default constructor
        private Command() {}

        /** Constructor: Create Command instance from cleaned input line of .vm file
         *  Args:       String line - cleaned line of .vm file to be parsed into a Command object
         *  Returns:    Command - a new instance of Command */
        private Command(String line) {
            this.line = line;
            if (line == null) {
                System.err.println("Error: tried to make command from null string.");
                // cannot make command from null string
                this.type = COMMAND_TYPE.OTHER;
                return;
            }
            String[] fields = line.split("(\\s)+");
            int numFields = fields.length;
            if (numFields == 0) {
                System.err.println("Error: tried to make command from empty string.");
                // cannot make command from empty string
                this.type = COMMAND_TYPE.OTHER;
                return;
            }
            if (numFields > 1) arg1 = fields[1];
            if (numFields > 2) arg2 = Integer.parseInt(fields[2]);
            // try to match to a known type
            for (COMMAND_TYPE c : COMMAND_TYPE.values()) {
                if (fields[0].toLowerCase().equals(c.token)) {
                    this.type = c;
                    return;
                }
            }
            this.type = COMMAND_TYPE.OTHER;
        }

        /**  Determine and return the segment type of this Command
         *   Args:      void
         *   Returns:   SEGMENT - this Command's segment type, if applicable */
        private SEGMENT getSegment() {
            if (this.arg1 == null) {
                System.err.println("Error: tried to get segment type of null arg1.");
                return (this.segment = null);
            }
            for (SEGMENT s : SEGMENT.values()) {
                if (s.token.equals(this.arg1.toLowerCase())) {
                    return (this.segment = s);
                }
            }
            System.err.println("Error: arg1 does not match any known segment.");
            return (this.segment = null);
        }
    }
}