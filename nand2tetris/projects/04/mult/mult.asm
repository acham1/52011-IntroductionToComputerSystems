// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// The basic strategy of this code is to start at 0 and add to
// it the value R0, repeated R1 times while accumulating the sum.

// first set R2 to zero
@R2
M=0

// now copy R1 value into counter
@R1
D=M
@counter
M=D

// skip summation if R1 is 0 (D currently has R1 copy)
@INF
D;JEQ

// while counter is positive, add R0 to R2
(ADD_ONCE)
// store R0 value in D
@R0
D=M
// add R0 to R2 once
@R2
M=D+M
// decrement counter
@counter
M=M-1
// repeat summing if counter still positive
D=M
@ADD_ONCE
D;JGT

// infinite loop
(INF)
@INF
0;JMP