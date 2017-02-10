/** Alan Cham
 *  Project 7: VM1 - Stack Arithmetic
 *  February 2, 2017
 *  Non-strictly based on program structure from Ch7 Pg 144-146
 **/

import java.io.*;
import java.util.*;

public class VM1 {

    private static final boolean DBG = true; // when true, program will make some printouts for debugging
    private static final String INPUT_EXT = ".vm"; // input file extension
    private static final String OUTPUT_EXT = ".asm"; // output file extension
    private static final int DEFAULT_ARG2 = 0; // default value of command's arg2

    // enumeration of memory segment types
    private enum SEGMENT_SUPERTYPE {
        ADDRESSPOINTER, // must be dereferenced to obtain address (e.g. local, argument, this, that)
        ADDRESS, // actual address (e.g. pointer, temp)
        VIRTUAL, // constant
        STATIC, // static
    }

    // enumeration of memory segments
    private enum SEGMENT {
        ARGUMENT    ("argument", SEGMENT_SUPERTYPE.ADDRESSPOINTER, "ARG"),
        LOCAL       ("local",   SEGMENT_SUPERTYPE.ADDRESSPOINTER, "LCL"),
        THIS        ("this",    SEGMENT_SUPERTYPE.ADDRESSPOINTER, "THIS"),
        THAT        ("that",    SEGMENT_SUPERTYPE.ADDRESSPOINTER, "THAT"),
        POINTER     ("pointer", SEGMENT_SUPERTYPE.ADDRESS, "3"),
        TEMP        ("temp",    SEGMENT_SUPERTYPE.ADDRESS, "5"),
        STATIC      ("static",  SEGMENT_SUPERTYPE.STATIC, "16"),
        CONSTANT    ("constant", SEGMENT_SUPERTYPE.VIRTUAL, null);

        private String token; // token name of the segment
        private SEGMENT_SUPERTYPE type; // type of segment
        private String value; // value of pointer or address

        // enum constructor
        private SEGMENT(String token, SEGMENT_SUPERTYPE type, String value) {
            this.token = token;
            this.type = type;
            this.value = value;
        }
    }

    // enumeration of possible command super-types
    private enum CMD_SUPERTYPE {
        ARITHMETIC_LOGICAL,
        MEMORY_ACCESS,
        PROGRAM_FLOW,
        FUNCTION_CALLING,
        OTHER // error type
    }

    // enumeration of possible command types
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
        IF        ("goto-end", CMD_SUPERTYPE.PROGRAM_FLOW),
        FUNCTION  ("function", CMD_SUPERTYPE.FUNCTION_CALLING),
        RETURN    ("return",  CMD_SUPERTYPE.FUNCTION_CALLING),
        CALL      ("call",    CMD_SUPERTYPE.FUNCTION_CALLING),
        OTHER     ("",        CMD_SUPERTYPE.OTHER);

        private String token; // token in VM language that identifies subtype
        private CMD_SUPERTYPE supertype; // supertype of the command type

        private COMMAND_TYPE(String token, CMD_SUPERTYPE supertype) {
            this.token = token;
            this.supertype = supertype;
        }
    }

    // constructs a Parser to parse the VM input file
    // constructs a CodeWriter to generate code into outut file
    public static void main(String[] args) {
        if (args.length != 1) { // check that there's exactly 1 argument
            System.err.println("Error: expected exactly one command-line argument, the input .vm file name.");
            System.exit(1);
        }
        String inputName = args[0]; // store input file name
        if (!inputName.endsWith(INPUT_EXT)) { // check file has right type
            System.err.println("Error: input file does not end in " + INPUT_EXT);
            System.exit(1);
        }
        String prefix = inputName.substring(0, inputName.lastIndexOf(INPUT_EXT)); // get prefix of file name
        File vmFile = new File(inputName); // prepare to read file
        if (!vmFile.exists()) { // check that file exists
            System.err.println("Error: file " + inputName + " does not exist.");
            System.exit(1);
        }
        Parser parsed = new Parser(vmFile); // parse input file
        String outputName = prefix + OUTPUT_EXT; // swap INPUT_EXT with OUTPUT_EXT for output name
        File asmFile = new File(outputName); // prepare to write file
        CodeWriter cw = new CodeWriter(asmFile, parsed); // translate and write to file
        System.out.println("End.");
    }

    // handles parsing of .vm file
    // reads VM commands, parses them, and strips white-space/comments
    private static class Parser {
        LinkedList<Command> commandList = null; // linkedlist of input commands
        Command currentCommand = null; // current command
        ListIterator<Command> iter = null; // iterator for linkedlist of input lines

        // constructor: loads file and stores all commands in linkedlist
        // arg: File vmFile - input .vm file
        private Parser(File vmFile) {
            this.commandList = new LinkedList<Command>(); // new linkedlist to hold commands
            try(
                    FileReader fr = new FileReader(vmFile); // new Reader for input file
                    BufferedReader br = new BufferedReader(fr); // new bufferedreader for retrieving each input line
            ) {
                if (DBG) System.out.println("Parsing file: " + vmFile.getCanonicalPath());
                String line = null;
                while ((line = br.readLine()) != null) {
                    line = clean(line); // strip the comments and terminal white space
                    if (line != null && !line.isEmpty()) {
                        this.commandList.add(new Command(line)); // convert to command and add to list if nonempty
                    }
                }
                this.iter = this.commandList.listIterator(0); // set iterator to start of linkedlist
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: FileNotFoundException for input file " + vmFile.getPath());
                System.exit(1);
            } catch (IOException ioe) {
                System.err.println("Error: IOException when reading from input file.");
                System.exit(1);
            }
        }

        // returns true if the input file has more commands to process, false otherwise
        private boolean hasMoreCommands() {
            if (this.iter == null) return false;
            return this.iter.hasNext();
        }

        // returns true if succesfully set current command to next command
        // returns false if no new command remains
        private boolean advance() {
            if (this.iter.hasNext()) {
                this.currentCommand = this.iter.next(); // advance to next command if available
                return true;
            }
            System.err.println("Error: tried to advance when no commands remain.");
            return false;
        }

        // returns the type field of the current command
        private COMMAND_TYPE commandType() {
            if (currentCommand == null) { // null command cannot have a type
                System.err.println("Error: tried to read type from null command.");
                return COMMAND_TYPE.OTHER;
            }
            return this.currentCommand.type;
        }

        // strip the comments and terminal white space
        // arg: String line - input line from .vm file
        // return: String containing input line with excess whitespace and comments removed
        private static String clean(String line) {
            if (line == null) return null;
            int index = line.indexOf("//"); // check if line contains comment
            if (index == -1) return line.trim();
            if (index == 0) return "";
            return line.substring(0, index).trim();
        }

        // return arg1 of current vm command if applicable
        String arg1() {
            if (this.currentCommand == null) {
                System.err.println("Error: tried to read arg1 from null command.");
                return null;
            } else if (this.currentCommand.type == COMMAND_TYPE.OTHER) {
                System.err.println("Error: tried to read arg1 from Other command.");
                return null;
            } else if (this.currentCommand.type == COMMAND_TYPE.RETURN) {
                System.err.println("Error: tried to read arg1 from Return command.");
                return null;
            } else if (this.currentCommand.type.supertype == CMD_SUPERTYPE.ARITHMETIC_LOGICAL) {
                return this.currentCommand.type.token; // arithmetic commands return name as arg1
            }
            return this.currentCommand.arg1;
        }

        // return arg2 of current vm command if applicable
        int arg2() {
            if (this.currentCommand == null) {
                System.err.println("Error: tried to read arg1 from null command.");
                return DEFAULT_ARG2;
            } else if (this.currentCommand.type == COMMAND_TYPE.OTHER) {
                System.err.println("Error: tried to read arg1 from Other command.");
                return DEFAULT_ARG2;
            } else if (this.currentCommand.type == COMMAND_TYPE.PUSH
                    || this.currentCommand.type == COMMAND_TYPE.POP
                    || this.currentCommand.type == COMMAND_TYPE.FUNCTION
                    || this.currentCommand.type == COMMAND_TYPE.CALL
            ) {
                return this.currentCommand.arg2; // arg2 only valid for four types
            }
            System.err.println("Error: tried to read arg2 from inappropriate command.");
            return DEFAULT_ARG2;
        }
    }

    // translates vm commands into Hack assembly code and writes to file
    private static class CodeWriter {
        // iterate through input commands and translate and write each one
        // arg: File asmFile - output .asm file
        //      Parser parsed - the result of constructing Parser by parsing input .vm file
        private CodeWriter(File asmFile, Parser parsed) {
            try (
                    PrintWriter pw = new PrintWriter(asmFile);
            ) {
                int identifier = 0; // identifier number used for distinguishing labels
                String translated = null;
                String name = asmFile.getName();
                name = name.substring(0, name.lastIndexOf(".")); // name of file for identifying static variables
                while (parsed.hasMoreCommands()) {
                    parsed.advance(); // set current command to next one
                    if (DBG) System.out.println(parsed.currentCommand.line);
                    translated = vmToASM(parsed.currentCommand, identifier, name);
                    if (translated == null || translated.isEmpty()) {
                        System.err.println("Error: failed to translate line: " + parsed.currentCommand.line);
                    } else {
                        pw.print(translated); // write translated .asm line to file
                        identifier++;
                    }
                }
                pw.println("(" + name + ".END)"); // add infinite loop at end
                pw.println("@" + name + ".END");
                pw.println("0;JMP");
                pw.flush();
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: FileNotFoundException when writing to file " + asmFile.getName());
                System.exit(1);
            }
        }

        // produce asm string from vm command
        // args:    Command command - individual parsed vm command
        //          int identifier - number used for distinguishing labels
        //          String name - name of file, used for static variable access
        // returns: String containing asm code corresponding to vm command
        private static String vmToASM(Command command, int identifier, String name) {
            StringBuilder sb = new StringBuilder();
            switch(command.type) {
                case ADD:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("A=A-1\n");
                    sb.append("M=D+M\n");
                    break;
                case SUB:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("A=A-1\n");
                    sb.append("M=M-D\n");
                    break;
                case NEG:
                    sb.append("@SP\n");
                    sb.append("A=M-1\n");
                    sb.append("M=-M\n");
                    break;
                case EQ:
                    buildEQ(sb, identifier);
                    break;
                case GT:
                    buildGT(sb, identifier);
                    break;
                case LT:
                    buildLT(sb, identifier);
                    break;
                case AND:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("A=A-1\n");
                    sb.append("M=D&M\n");
                    break;
                case OR:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("A=A-1\n");
                    sb.append("M=D|M\n");
                    break;
                case NOT:
                    sb.append("@SP\n");
                    sb.append("A=M-1\n");
                    sb.append("M=!M\n");
                    break;
                case PUSH:
                    buildPush(sb, command, name);
                    break;
                case POP:
                    buildPop(sb, command, name);
                    break;
                default:
                    break;
            }
            return sb.toString(); // output string from stringbuilder
        }

        // convert push command to asm string and append to stringbuilder
        // args:    Command command - individual parsed vm command
        //          StringBuilder sb - stringbuilder where this function's output is stored
        //          String name - name of file, used for static variable access
        private static void buildPush(StringBuilder sb, Command command, String name) {
            SEGMENT s = command.getSegment();
            if (s == null) return; // no valid segment found
            switch (s.type) {
                case ADDRESSPOINTER:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    sb.append("@" + s.value + "\n");
                    sb.append("A=D+M\n");
                    sb.append("D=M\n");
                    sb.append("@SP\n");
                    sb.append("M=M+1\n");
                    sb.append("A=M-1\n");
                    sb.append("M=D\n");
                    break;
                case ADDRESS:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    sb.append("@" + s.value + "\n");
                    sb.append("A=D+A\n");
                    sb.append("D=M\n");
                    sb.append("@SP\n");
                    sb.append("M=M+1\n");
                    sb.append("A=M-1\n");
                    sb.append("M=D\n");
                    break;
                case VIRTUAL:
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("D=A\n");
                    sb.append("@SP\n");
                    sb.append("M=M+1\n");
                    sb.append("A=M-1\n");
                    sb.append("M=D\n");
                    break;
                case STATIC:
                    sb.append("@" + name + "." + command.arg2 + "\n");
                    sb.append("D=M\n");
                    sb.append("@SP\n");
                    sb.append("M=M+1\n");
                    sb.append("A=M-1\n");
                    sb.append("M=D\n");
                    break;
                default:
                    break;
            }
        }

        // convert pop command to asm string and append to stringbuilder
        // args:    Command command - individual parsed vm command
        //          StringBuilder sb - stringbuilder where this function's output is stored
        //          String name - name of file, used for static variable access
        private static void buildPop(StringBuilder sb, Command command, String name) {
            SEGMENT s = command.getSegment(); // access the segment type of the command
            if (s == null) return; // no valid segment found
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
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("@" + command.arg2 + "\n");
                    sb.append("M=D\n");
                    break;
                case STATIC:
                    sb.append("@SP\n");
                    sb.append("AM=M-1\n");
                    sb.append("D=M\n");
                    sb.append("@" + name + "." + command.arg2 + "\n");
                    sb.append("M=D\n");
                    break;
                default:
                    break;
            }
        }

        // convert lt command to asm string
        // args:    StringBuilder sb - where the output asm code of this function is stored
        //          int identifier - number used for distinguishing labels
        private static void buildLT(StringBuilder sb, int identifier) {
            String trueLabel = "TRUE." + identifier;
            String falseLabel = "FALSE." + identifier;
            String endLabel = "END." + identifier;
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("A=A-1\n");
            sb.append("D=D-M\n");
            sb.append("@" + trueLabel + "\n");
            sb.append("D;JGT\n"); // true if difference is positive
            sb.append("D=0\n"); // set D=0 if false
            sb.append("@" + endLabel + "\n");
            sb.append("0;JMP\n");
            sb.append("(" + trueLabel + ")\n");
            sb.append("D=-1\n"); // set D=-1 if true
            sb.append("(" + endLabel + ")\n");
            sb.append("@SP\n");
            sb.append("A=M-1\n");
            sb.append("M=D\n"); // write D into top of stack
        }

        // convert gt command to asm string
        // args:    StringBuilder sb - where the output asm code of this function is stored
        //          int identifier - number used for distinguishing labels
        private static void buildGT(StringBuilder sb, int identifier) {
            String trueLabel = "TRUE." + identifier;
            String falseLabel = "FALSE." + identifier;
            String endLabel = "END." + identifier;
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("A=A-1\n");
            sb.append("D=D-M\n");
            sb.append("@" + trueLabel + "\n");
            sb.append("D;JLT\n"); // true if difference is negative
            sb.append("D=0\n"); // set D=0 if false
            sb.append("@" + endLabel + "\n");
            sb.append("0;JMP\n");
            sb.append("(" + trueLabel + ")\n");
            sb.append("D=-1\n"); // set D=-1 if true
            sb.append("(" + endLabel + ")\n");
            sb.append("@SP\n");
            sb.append("A=M-1\n");
            sb.append("M=D\n"); // write D into top of stack
        }

        // convert eq command to asm string
        // args:    StringBuilder sb - where the output asm code of this function is stored
        //          int identifier - number used for distinguishing labels
        private static void buildEQ(StringBuilder sb, int identifier) {
            String trueLabel = "TRUE." + identifier;
            String falseLabel = "FALSE." + identifier;
            String endLabel = "END." + identifier;
            sb.append("@SP\n");
            sb.append("AM=M-1\n");
            sb.append("D=M\n");
            sb.append("A=A-1\n");
            sb.append("D=D-M\n");
            sb.append("@" + trueLabel + "\n");
            sb.append("D;JEQ\n"); // true if difference is zero
            sb.append("D=0\n"); // set D=0 if false
            sb.append("@" + endLabel + "\n");
            sb.append("0;JMP\n");
            sb.append("(" + trueLabel + ")\n");
            sb.append("D=-1\n"); // set D=-1 if true
            sb.append("(" + endLabel + ")\n");
            sb.append("@SP\n");
            sb.append("A=M-1\n");
            sb.append("M=D\n"); // write D into top of stack
        }
    }

    // class that bundles a command's type, and arguments
    private static class Command {
        COMMAND_TYPE type = null; // command's type
        String line; // original line of .vm text
        String arg1 = null; // command's first argument
        int arg2 = DEFAULT_ARG2; // command's second argument
        SEGMENT segment = null;

        // creates a Command instance from cleaned input line of .vm file
        // args:    String line - cleaned line of .vm file to be parsed into a Command object
        private Command(String line) {
            this.line = line;
            if (line == null) {
                System.err.println("Error: tried to make command from null string.");
                this.type = COMMAND_TYPE.OTHER; // cannot make command from null string
                return;
            }
            String[] fields = line.split("(\\s)+");
            int numFields = fields.length;
            if (numFields == 0) {
                System.err.println("Error: tried to make command from empty string.");
                this.type = COMMAND_TYPE.OTHER; // cannot make command from empty string
                return;
            }
            if (numFields > 1) arg1 = fields[1];
            if (numFields > 2) arg2 = Integer.parseInt(fields[2]);
            for (COMMAND_TYPE c : COMMAND_TYPE.values()) { // try to match to a known type
                if (fields[0].toLowerCase().equals(c.token)) {
                    this.type = c;
                    return;
                }
            }
            this.type = COMMAND_TYPE.OTHER; // unknown command type
        }

        // get segment type
        // access the segment field of this Command instance
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