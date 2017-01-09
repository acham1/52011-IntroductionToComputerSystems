Alan Cham
Intro to Computer Systems
Project 0: Programming Best Practices
January 8, 2017

>>>>> Contents <<<<<
I.      Compiling
II.     Running
III.    Code Assessment

-------------------------------------------------------------------------------

I.      COMPILING

        The source code consists of a single C file: main.c
        It is fully C89/ANSI-C compliant and can be compiled with any compiler
        that conforms to the C89/ANSI-C standard. To compile on the UChicago CS
        Linux machines, enter the following into the terminal prompt, while the 
        working directory is inside the src/ folder:

        gcc -std=c89 -pedantic-errors -Wall main.c -o strip

-------------------------------------------------------------------------------

II.     RUNNING

        Exactly one input filename must be provided to this program as 
        command-line argument, and the filename must end in extension ".in".
        An optional argument "no-comments" can be provided, which will cause
        any comments in the input file to be stripped as well. This optional
        argument can be provided before or after the filename argument. The
        resulting output file will have the same name  and location as the 
        input, with the extension changed from ".in" to ".out". 

        For example, when running the program from inside the src/ directory:

            ./strip ../examples/sample.in
        (This command will write a file ../examples/sample.out with only 
        whitespace removed)

            ./strip no-comments ../examples/sample.in
            ./strip ../examples/sample.in no-comments
        (Either of these commands will write a file ../examples/sample.out with
        both comments and whitespace removed)

-------------------------------------------------------------------------------

III.    CODE ASSESSMENT
                
        This code meets the interface and functionality specifications laid out
        by the assignment, including some amount of input validation. 
