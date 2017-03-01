Alan Cham
52011 - Introduction to Computer Systems
Project 10
February 23, 2016
README.txt

I.  Compilation
    Navigate to the /src directory, then compile with the following command:

    javac JackCompiler/*.java

II. Running
    Navigate to the /src directory, then run with the following command:

    java JackCompiler.Compiler1 <INPUT_NAME>

    <INPUT_NAME> can be a path to a .jack file, or it can be a path to a directory
    containing .jack file(s). The output .xml and T.xml files will be placed in the
    same directory as the input .jack file, or into the directory provided as input.

III.Difficulties
    This program runs on all the provided test input and gives valid output
    according to the TextComparer tool.

    Notable challenges included handling multi-line comments and a bug pertaining
    to nested expressions. However, these have been addressed and the program gives
    valid solutions. Particularly, ample usage of print statements were helpful in
    finding and fixing the cause of infinite looping when parsing nested expressions.
    