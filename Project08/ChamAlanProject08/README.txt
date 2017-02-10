Alan Cham
Introduction to Computer Systems
Project 8: Virtual Machine II
README.txt

I.  Compiling
    The source code consists of a single file VM2.java
    First navigate the current working directory to ChamAlanProject08/src/
    Then compile as follows:

    Example 0: javac VM2.java

    This should produce new .class files, including VM1.class, in ChamAlanProject08/src/

II. Running
    To run the code after compiling, navigate the current working directory 
    to ChamAlanProject08/src/

    Example 1: java VM2 path/xxx.vm path/yyy.vm
    Example 2: java VM2 path/*.vm

    The above are two examples of ways to run the code. You must specify the input vm files
    by listing the absolute/relative paths to each file. Alternatively, if all the .vm files 
    are in the same directory, you can use a wildcard to list all the .vm files as program input.

    The output file will be in the same directory as the first input argument file.
    Its name will be the same as the parent directory's name, but its extension will be .asm.

III.Things that Don't Work
    This program has been verified to work as far as the dry tests are concerned.
