Alan Cham
Introduction to Computer Systems
Project 2: Combinational Chips
README.txt

All assigned chips have been submitted in working state.
Initially I had some trouble indexing the out bus for the ALU chip, 
e.g. 
Foo(in=out[0..7], out=bar);

But I resolved by creating an internal bus/pin, 
e.g. 
Prefoo(in=xyz, out=out, out[0..7]=internal);
Foo(in=internal, out=bar);