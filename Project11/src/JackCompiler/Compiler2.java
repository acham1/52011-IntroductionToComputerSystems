package JackCompiler;
import java.util.*;
import java.io.*;

/**
 * Compiler2.java
 * MPCS 52011 - Project 11
 * Created by Alan Cham on 3/2/2017.
 * Project 11 Code Generation.
 */
public class Compiler2 {
    /**
     * Compiler2.main
     * Main driver that parses and tokenizes and code-generates for Jack source files
     * @param args - a .jack filename to be parsed and tokenized, or a directory
     * for which all contained .jack files should be parsed and tokenized and code-generated
     */
    public static void main(String[] args) {
        // check if file is valid
        File file = getFile(args);
        if (file == null) {
            System.err.println("Error: input argument is not an existing file or directory.");
            System.exit(1);
        }
        // in case file is directory, get a list of actual target files
        LinkedList<File> fileList = listFiles(file);
        if (fileList.isEmpty()) {
            System.err.println("Error: file " + file.getAbsolutePath() + " is not a .jack file or a directory containing .jack files.");
            System.exit(1);
        } else {
            System.out.println("To be processed: ");
            for (File f : fileList) printCanonicalPath(f);
        }
        // tokenize and parse each file in list
        compile(fileList);
        System.out.println("Finished.");
    } // end method Compiler2.main

    /**
     * Compiler2.getFile
     * Parse this program's command-line arguments and return a File instance
     * specifying the target .jack files or directory
     * @param args - String[] of command-line arguments
     * @return File instance specifying the target .jack files or directory,
     * or null if input is invalid
     */
    private static File getFile(String[] args) {
        if (args.length < 1) return null;
        File file = new File(args[0]);
        if (!file.exists()) return null;
        return file;
    } // end method Compiler2.getFile


    /**
     * Compiler2.listFiles
     * Return of a list of all valid .jack files to process
     * @param file - a File instance holding the file or directory of files to process
     * @return a list of all valid .jack files this program must process
     */
    private static LinkedList<File> listFiles(File file) {
        LinkedList<File> list = new LinkedList<>();
        if (file.isFile() && isValidFile(file)) {
            // if it's a valid file, add it to the list
            list.add(file);
        } else if (file.isDirectory()){
            // if it's a directory add all contained valid files list
            File[] fileArray = file.listFiles();
            if (fileArray != null) {
                for (File f : fileArray) if (isValidFile(f)) list.add(f);
            }
        }
        return list;
    } // end method Compiler2.listFiles

    /**
     * Compiler2.isValidFile
     * Checks if a File is a valid .jack file
     * @param f - File instance whose validity is to be checked
     * @return boolean true if File is a valid .jack file, false otherwise
     */
    private static boolean isValidFile(File f) {
        try {
            // check that file ends with .jack
            String fileName = f.getCanonicalPath().toLowerCase();
            if (fileName.endsWith(".jack")) return true;
        } catch (IOException ioe) {
            System.err.println("Warning: IOException when getting canonical path for " + f.getName());
        }
        return false;
    } // end method Compiler2.isValidFile

    /**
     * Compiler2.printCanonicalPath
     * Attempt to print the canonical path of a file
     * @param f - File whose canonical path should be printed
     */
    private static void printCanonicalPath(File f) {
        try {
            System.out.println(f.getCanonicalPath());
        } catch (IOException ioe) {
            System.err.println("Warning: IOException when getting canonical path for " + f.getName());
        }
    } // end method Compiler2.printCanonicalPath

    /**
     * Compiler2.getTokenizedFileName
     * Get a String containing the tokenizer output filename
     * @param f - file which is being parsed/tokenized
     * @return String containing the tokenizer output filename
     * @throws IOException from getting canonical path
     */
    private static String getTokenizedFileName(File f) throws IOException {
        String name = f.getCanonicalPath();
        int index = name.toLowerCase().lastIndexOf(".jack");
        name = name.substring(0, index) + "T.xml";
        return name;
    } // end method Compiler2.getTokenizedFileName

    /**
     * Compiler2.getParsedFileName
     * Get a String containing the parser output filename
     * @param f - file which is being parsed/tokenized
     * @return String containing the parser output filename
     * @throws IOException from getting canonical path
     */
    private static String getParsedFileName(File f) throws IOException {
        String name = f.getCanonicalPath();
        int index = name.toLowerCase().lastIndexOf(".jack");
        name = name.substring(0, index) + ".xml";
        return name;
    } // end method Compiler2.getParsedFileName

    /**
     * Compiler2.getVMFileName
     * Get a String containing the compiler output filename
     * @param f - file which is being parsed/tokenized/compiled
     * @return String containing the VM output filename
     * @throws IOException from getting canonical path
     */
    private static String getVMFileName(File f) throws IOException {
        String name = f.getCanonicalPath();
        int index = name.toLowerCase().lastIndexOf(".jack");
        name = name.substring(0, index) + ".vm";
        return name;
    } // end method Compiler2.getVMFileName

    /**
     * Compiler2.compile
     * Tokenize and Parse each File in an array of Files
     * @param fileList - LinkedList of Files to tokenize and parse
     */
    private static void compile(LinkedList<File> fileList) {
        System.out.println("\nWorking...");
        for (File f : fileList) {
            try {
                System.out.println("Tokenizing " + f.getName());
                LinkedList<Token> tokenList = Token.getTokenList(f);
//                Token.writeTokenList(tokenList, getTokenizedFileName(f));
                System.out.println("Parsing " + f.getName());
                ParseTree tree = new ParseTree(tokenList);
//                tree.writeTree(getParsedFileName(f));
                System.out.println("Generating code for " + f.getName());
                tree.writeCode(getVMFileName(f));
                System.out.println();
            } catch (IOException ioe) {
                System.err.println("Error: IOException when trying to create Tokenizer or Parser output .xml");
            }
        }
    } // end method Compiler2.tokenizeAndParse
} // end class Compiler2
