Alan Cham
Introduction to Computer Systems
Project 3: Sequential Logic
README.txt

All assigned chips have been submitted in working state.
When building the Bit chip, I originally encountered the problem:
"Can't connect gate's output pin to part" which was caused by
trying to feed the chip's output pin as Mux input. I had to resolve
this by defining an internal pin from DFF's output, then feeding
that pin into Mux instead.

Then when making the RAM64 chip, I ran into a problem when I tried to
use a Ram64 instead of the correct RAM64; this was easily fixed by
changing the capitalization.

When creating the PC Chip, it was difficult to think about how
to implement the if-else flow, but that was resolved by just
being careful about the ordering of the Mux chips.