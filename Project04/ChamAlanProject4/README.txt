Alan Cham
Introduction to Computer Systems
Project 4: Machine Language
README.txt

The assigned programs have been submitted in working state.
While testing, I encountered a problem where the cpu emulator
running the test script would return an error because it
could not find a hack file. This was remedied by feeding my
asm file to the included assembler to make a hack file.

Another issue that I encountered was when writing a jump.
At first, I had a value in M that I wanted to use as a basis
for jumping or not jumping. And then I would write something
like:

@THERE
M;JGE

At first I did not realize that the @THERE instruction 
effectively changes what M-value is, since the location
in memory has changed. This was remedied by storing
the original M value in D first, then using D as the basis
for jumping or not jumping. 