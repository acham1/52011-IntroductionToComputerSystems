package JackCompiler;
import java.util.*;
import java.io.*;

/**
 * Compiler1.java
 * MPCS 52011 - Project 10
 * Created by Alan Cham on 2/25/2017.
 * Project 10 Syntax Analysis driver.
 */
public class Compiler1 {
    /**
     * Compiler1.main
     * Main driver that parses and tokenizes Jack source files
     * @param args - a .jack filename to be parsed and tokenized, or a directory
     * for which all contained .jack files should be parsed and tokenized
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
        tokenizeAndParse(fileList);
        System.out.println("Finished.");
    } // end method Compiler1.main

    /**
     * Compiler1.getFile
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
    } // end method Compiler1.getFile


    /**
     * Compiler1.listFiles
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
    } // end method Compiler1.listFiles

    /**
     * Compiler1.isValidFile
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
    } // end method Compiler1.isValidFile

    /**
     * Compiler1.printCanonicalPath
     * Attempt to print the canonical path of a file
     * @param f - File whose canonical path should be printed
     */
    private static void printCanonicalPath(File f) {
        try {
            System.out.println(f.getCanonicalPath());
        } catch (IOException ioe) {
            System.err.println("Warning: IOException when getting canonical path for " + f.getName());
        }
    } // end method Compiler1.printCanonicalPath

    /**
     * Compiler1.getTokenizedFileName
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
    } // end method Compiler1.getTokenizedFileName

    /**
     * Compiler1.getParsedFileName
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
    } // end method Compiler1.getParsedFileName

    /**
     * Compiler1.tokenizeAndParse
     * Tokenize and Parse each File in an array of Files
     * @param fileList - LinkedList of Files to tokenize and parse
     */
    private static void tokenizeAndParse(LinkedList<File> fileList) {
        System.out.println("\nWorking...");
        for (File f : fileList) {
            try {
                System.out.println("Tokenizing " + f.getName());
                LinkedList<Token> tokenList = Token.getTokenList(f);
                Token.writeTokenList(tokenList, getTokenizedFileName(f));
                System.out.println("Parsing " + f.getName());
                ParseTree tree = new ParseTree(tokenList);
                tree.writeTree(getParsedFileName(f));
                System.out.println();
            } catch (IOException ioe) {
                System.err.println("Error: IOException when trying to create Tokenizer or Parser output .xml");
            }
        }
    } // end method Compiler1.tokenizeAndParse
} // end class Compiler1
