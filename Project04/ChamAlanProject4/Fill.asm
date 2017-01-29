// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// make a variable to track position on screen, initialize to SCREEN
@SCREEN
D=A
@screenpos
M=D

// check if any key is pressed or not
(CHECK)
@KBD
D=M
// if not pressed, go to NOT_PRESSED
@NOT_PRESSED
D;JEQ
// else go to PRESSED
@PRESSED
0;JMP

// do this if no key is pressed
(NOT_PRESSED)
// if screen is already blank, wait for key press
@SCREEN
D=A
@screenpos
D=M-D
@CHECK
D;JLE
// else decrement screenpos and clear corresponding pixels
@screenpos
M=M-1
A=M
M=0
// check to see what to do in next cycle
@CHECK
0;JMP

// do this if any key is pressed
(PRESSED)
// if screen is already full, wait for key release
@KBD
D=A
@screenpos
D=D-M
@CHECK
D;JLE
// else fill current pixels and increment screenpos
@screenpos
M=M+1
A=M-1
M=-1
// check to see what to do in next cycle
@CHECK
0;JMP