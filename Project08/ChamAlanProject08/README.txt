Alan Cham
Introduction to Computer Systems
Project 8: Virtual Machine I
README.txt

I.  Compiling
    The source code consists of a single .java file.
    First navigate the current working directory to ChamAlanProject07/src/
    Then compile as follows:

    javac VM1.java

    This should produce a new files, VM1.class in ChamAlanProject07/src/

II. Running
    To run the code after compiling, navigate the current working directory 
    to ChamAlanProject07/src/

    java VM1 xxx.vm

    xxx.vm is the file to be translated. The output will be xxx.asm in the same
    directory as xxx.vm. xxx may be a simple filename if the file is in the 
    current working directory; otherwise it may be an absolute or relative pathname.

III.Things that Don't Work
    This program has been verified to work as far as the dry tests are concerned.
