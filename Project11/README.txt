Alan Cham
52011 - Introduction to Computer Systems
Project 11
March 2, 2016
README.txt

I.  Compilation
    Navigate to the /src directory, then compile with the following command:

    javac JackCompiler/*.java

II. Running
    Navigate to the /src directory, then run with the following command:

    java JackCompiler.Compiler2 <INPUT_NAME>

    <INPUT_NAME> can be a path to a .jack file, or it can be a path to a directory
    containing .jack file(s). The output .vm file(s) will be placed in the
    same directory as the input .jack files, or into the directory provided as input.

III.Difficulties
    This program runs on all the provided test input and gives correct output.

    Notable challenges included correcting the compiler to handle the complex
    array operations. This was solved by temporarily storing the destination
    address in the "temp" segment instead of the directly storing it in "that". 
    This allows the "that" segment to be used in the right side of the assignment
    operation, without conflict.
