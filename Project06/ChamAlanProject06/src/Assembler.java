/** Alan Cham
 *  January 26, 2017
 *  52011 Introduction to Computer Systems
 *  Project 6: The Assembler */

import java.io.*;
import java.util.*;

public class Assembler {
    private static final boolean PRINT_DBG = false; // for debugging, print all lines at various points in assembling
    private static final String IN_EXT = ".asm"; // input file ext.
    private static final String OUT_EXT = ".hack"; // output file ext.
    private static final int FIRST_FREE_REG = 16; // 1st open reg for variables
    private static final int A_VALUE_DIGITS = 15; // num digits in machine code for memory address
    private static final int JUMP_HACK_WIDTH = 3; // width of c-instruction jump field in .hack
    private static final String[] DEFAULT_SYMS = { // default symbols/vars
            "SP", "LCL", "ARG", "THIS", "THAT", "SCREEN", "KBD", "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8",
            "R9", "R10", "R11", "R12", "R13", "R14", "R15"};
    private static final int[] DEF_SYM_ADDRESSES = { // addresses for default syms
            0, 1, 2, 3, 4, 16384, 24576, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final String[] JUMP_ASM = { // .asm jump options
            "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"};
    private static final String[] ASM_A_COMPS = { // array of possible computations with a=0
            "0", "1", "-1", "D", "A", "!D", "!A", "-D", "-A", "D+1", "A+1", "D-1", "A-1", "D+A", "D-A", "A-D", "D&A",
            "D|A"};
    private static final String[] ASM_M_COMPS = { // array of possible computations with a=1
            "", "", "", "", "M", "", "!M", "", "-M", "", "M+1", "", "M-1", "D+M", "D-M", "M-D", "D&M", "D|M"};
    private static final String[] HACK_COMPS = { // array of possible .hack computation codes
            "101010", "111111", "111010", "001100", "110000", "001101", "110001", "001111", "110011", "011111",
            "110111", "001110", "110010", "000010", "010011", "000111", "000000", "010101"};

    public static void main(String[] args) {
        System.out.println("Parsing console arguments.");
        // parse and validate console argument for input file name
        File inFile = parseConsoleArgs(args);
        System.out.println("Reading lines from .asm file.");
        // read file and store raw lines in linkedlist of lines
        Assembler.AsmLines lines = new AsmLines(inFile);
        // (for debugging only) prints input file
        if (PRINT_DBG) lines.printAll();
        System.out.println("Learning and assigning values to variables in file.");
        // load default symbols and record new symbols used in .asm file
        Hashtable<String, Integer> symbols = lines.getSymbols();
        System.out.println("Replacing symbols with numeric values.");
        // iterate through each line and convert any symbol to number
        lines.replaceSymbols(symbols);
        // (for debugging only) prints input file
        if (PRINT_DBG) lines.printAll();
        System.out.println("Converting asm code to hack code.");
        lines.toHackBinary(symbols);
        // (for debugging only) prints input file
        if (PRINT_DBG) lines.printAll();
        // extract filename prefix and use to make output file name
        String prefix = args[0].substring(0, args[0].lastIndexOf(IN_EXT));
        File outFile = new File(prefix + OUT_EXT);
        System.out.println("Writing binary lines to file " + outFile.getPath());
        // writing machine lines to hack file
        lines.writeToFile(outFile);
        System.out.println("All steps finished.");
        System.exit(0);
    }

    // parse and validate console argument for input file name
    private static File parseConsoleArgs(String[] args) {
        // make sure that one .asm file has been given as input
        if (args.length != 1 || !args[0].endsWith(IN_EXT)) {
            System.err.println("Error: expected exactly one command-line argument (i.e. name of asm file).\ne.g. "
                    + "java Assembler example.asm");
            System.exit(1);
        }
        // check if file exists, then open filestream
        File inFile = new File(args[0]);
        if (!inFile.exists()) {
            System.err.println("Error: argument refers to file that does not exist.");
            System.exit(1);
        }
        return inFile;
    }


    // class that holds a list of lines from a .asm file
    private static class AsmLines extends LinkedList<String> {
        private AsmLines(File inFile) {
            // create new linkedlist and populate with lines from file, ignoring comments and whitespace
            super();
            try (
                    FileInputStream fis = new FileInputStream(inFile);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                // keep adding newly read lines into array list
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.replaceAll("\\s", "");
                    int index = line.indexOf("//");
                    if (index != -1) {
                        if (index == 0) {
                            continue;
                        }
                        line = line.substring(0, index);
                    }
                    if (!line.isEmpty()) {
                        this.add(line);
                    }
                }
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: encountered FileNotFoundException when opening input file.");
                System.exit(1);
            } catch (IOException ioe) {
                System.err.println("Error: encountered IOException when reading from input file.");
                System.exit(1);
            }
        }

        // convert a symbol-free line of A-instruction asm code to a line of hack binary
        private static String AInstructToBinary(String line, Hashtable<String, Integer> symbols) {
            String str = line.substring(1);
            Integer value = Integer.valueOf(str);
            str = Integer.toBinaryString(value);
            return "0" + setWidth(str, A_VALUE_DIGITS);
        }

        // convert a symbol-free line of C-instruction asm code to a line of hack binary
        private static String CInstructToBinary(String line) {
            int fieldIndex = 0;
            String dest = null, comp = null, jump = null;
            // separate string into at most three fields (i.e. dest, comp, jump)
            String[] fields = line.split("[;=]");
            if (line.contains("=")) {
                dest = fields[fieldIndex++];
            }
            comp = fields[fieldIndex++];
            if (line.contains(";")) {
                jump = fields[fieldIndex++];
            }
            String destBinary = destToBinary(dest);
            String compBinary = compToBinary(comp);
            String jumpBinary = jumpToBinary(jump);
            return "111" + compBinary + destBinary + jumpBinary;
        }

        // convert a .asm destination field to .hack binary string
        private static String destToBinary(String dest) {
            if (dest == null) {
                return "000";
            }
            String d1 = dest.contains("A") ? "1" : "0";
            String d2 = dest.contains("D") ? "1" : "0";
            String d3 = dest.contains("M") ? "1" : "0";
            return d1 + d2 + d3;
        }

        // convert a .asm computation field to .hack binary string
        private static String compToBinary(String comp) {
            boolean M = comp.contains("M");
            String[] compList = M ? ASM_M_COMPS : ASM_A_COMPS;
            String a = M ? "1" : "0";
            String c = "XXXXXX";
            for (int i = 0; i < compList.length; i++) {
                // if the comp String matches a known C-instruction, then set c to the corresponding .hack code
                if (comp.equals(compList[i])) {
                    c = HACK_COMPS[i];
                }
            }
            return a + c;
        }

        // convert a .asm jump field to .hack binary string
        private static String jumpToBinary(String jump) {
            if (jump == null) {
                return "000";
            }
            for (int i = 0; i < JUMP_ASM.length; i++) {
                // if jump String matches a known jump option, then return the corresponding .asm jump code
                if (jump.equals(JUMP_ASM[i])) {
                    return setWidth(Integer.toBinaryString(i+1), JUMP_HACK_WIDTH);
                }
            }
            return "XXX";
        }

        // convert each line in this symbol-free instance from asm to hack binary
        private void toHackBinary(Hashtable<String, Integer> symbols) {
            try {
                // iterate through lines to replace line with binary
                ListIterator<String> iter = this.listIterator();
                String str;
                while (iter.hasNext()) {
                    str = iter.next();
                    iter.remove();
                    if (str.matches("@.++")) {
                        // replace A-instruction
                        iter.add(AInstructToBinary(str, symbols));
                    } else {
                        // replace C-instruction
                        iter.add(CInstructToBinary(str));
                    }
                }
            } catch (UnsupportedOperationException uoe) {
                System.err.println("Error: encountered UnsupportedOperationException when replacing symbols.");
                System.exit(1);
            } catch (ClassCastException cce) {
                System.err.println("Error: encountered ClassCastException when replacing symbols.");
                System.exit(1);
            } catch (IllegalArgumentException iae) {
                System.err.println("Error: encountered IllegalArgumentException when replacing symbols.");
                System.exit(1);
            }
        }

        // write all the lines stored in this instance into a file specified by outFile
        private void writeToFile(File outFile) {
            try {
                PrintWriter pw = new PrintWriter(outFile.getPath());
                ListIterator<String> iter = this.listIterator();
                while (iter.hasNext()) {
                    pw.println(iter.next());
                }
                pw.close();
            } catch (FileNotFoundException fnfe) {
                System.err.println("Error: encountered FileNotFoundException when writing output file.");
                System.exit(1);
            }
        }

        // iterate through list of lines and assign number values to new symbols
        private Hashtable<String, Integer> getSymbols() {
            // track next free memory slot for assigning variable name
            int nextFree = FIRST_FREE_REG;
            // track current "instruction" number
            int lineNum = 0;
            // make a new hashtable and populate with standard symbols/vars
            Hashtable<String, Integer> symbols = new Hashtable<String, Integer>();
            try {
                // first load standard symbols
                for (int i = 0; i < DEFAULT_SYMS.length; i++) {
                    if (!symbols.containsKey(DEFAULT_SYMS[i])) {
                        symbols.put(DEFAULT_SYMS[i], DEF_SYM_ADDRESSES[i]);
                        // System.out.println("\tDefault : " + DEFAULT_SYMS[i] + ", " + symbols.get(DEFAULT_SYMS[i]));
                    }
                }
                // first iterate through to find new label symbols
                ListIterator<String> iter = this.listIterator();
                String str;
                while (iter.hasNext()) {
                    str = iter.next();
                    if (str.matches("\\(.*\\)")) {
                        // learn new symbol from L-command
                        str = str.substring(1, str.length()-1);
                        if (!symbols.containsKey(str)) {
                            System.out.println("\tL-Define: " + str + ", " + lineNum);
                            symbols.put(str, lineNum);
                        }
                    } else {
                        lineNum++;
                    }
                }
                // second iterate through to find new variable symbols
                iter = this.listIterator();
                while (iter.hasNext()) {
                    str = iter.next();
                    if (str.matches("@.++")) {
                        // learn new symbol from A-command
                        str = str.substring(1);
                        if (!stringIsPositiveInteger(str) && !symbols.containsKey(str)) {
                            System.out.println("\tA-Define: " + str + ", " + nextFree);
                            symbols.put(str, nextFree++);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                System.err.println("Error: encountered NullPointerException "
                        + "when loading default symbols.");
                System.exit(1);
            }
            return symbols;
        }

        // strip symbols from the AsmLines and replace with appropriate number
        private void replaceSymbols(Hashtable<String, Integer> symbols) {
            try {
                // iterate through lines to remove lines or replace symbols
                ListIterator<String> iter = this.listIterator();
                String str;
                while (iter.hasNext()) {
                    str = iter.next();
                    if (str.matches("\\(.*\\)")) {
                        // delete line defining a label symbol
                        // System.out.println("\tRemoved line: " + str);
                        iter.remove();
                    } else if (str.matches("@.++")) {
                        // replace variable symbol with memory address
                        str = str.substring(1);
                        if (!stringIsPositiveInteger(str)) {
                            iter.remove();
                            iter.add("@" + symbols.get(str));
                        }
                    }
                }
            } catch (UnsupportedOperationException uoe) {
                System.err.println("Error: encountered UnsupportedOperationException when replacing symbols.");
                System.exit(1);
            } catch (ClassCastException cce) {
                System.err.println("Error: encountered ClassCastException when replacing symbols.");
                System.exit(1);
            } catch (IllegalArgumentException iae) {
                System.err.println("Error: encountered IllegalArgumentException when replacing symbols.");
                System.exit(1);
            }
        }

        // truncate or pad a binary number string to have totalWidth
        private static String setWidth(String num, int totalWidth) {
            int len = num.length();
            int diff = totalWidth - num.length();
            if (diff >= 0) {
                String out = "";
                for (int i = 0; i < diff; i++) {
                    out = out + "0";
                }
                return out + num;
            } else {
                return num.substring(-diff, len);
            }
        }

        // for debugging purposes, print all contents of the list
        private void printAll() {
            System.out.println("Printing all lines:");
            ListIterator<String> DBG_ITER = this.listIterator();
            while (DBG_ITER.hasNext()) {
                System.out.println(DBG_ITER.next());
            }
        }

        // check if a string comprises a valid positive integer
        private static boolean stringIsPositiveInteger(String str) {
            return str.matches("[0-9]*");
        }
    } // end Class AsmLines
} // end Class Assembler
