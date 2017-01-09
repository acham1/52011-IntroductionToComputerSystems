/** Alan Cham
  * Intro to Computer Systems
  * Project 0: Programming Best Practices
  * January 8, 2017
  *
  * This source file comprises a program with the following functionality:
  *     1) remove white space
  *     2) remove comments
  * 
  * See included README.txt for more details about usage and compilation */

#include <stdio.h>
#include <stdlib.h>

#define INPUT_EXT ".in"         /* input file extension */
#define OUTPUT_EXT ".out"       /* output file extension */ 
#define MAX_PATH 1000           /* maximum char-width of file path */
#define NC_OPTION "no-comments" /* command-line argument for no-comments */

char nocomments = 0;            /* indicates comment removal is on/off */
char inpath[MAX_PATH];          /* path of input file */
char outpath[MAX_PATH];         /* path of output file */

char argsvalid(int argc, char* argv[]);

int main(int argc, char* argv[]) {
    FILE* fin, *fout;

    /* validate command-line input */
    if (!argsvalid(argc, argv)) {
        fprintf(stderr, "Error: invalid or missing input arguments.\n");
        return EXIT_FAILURE;
    } else if (!(fin = fopen(inpath, "r"))) {
        fprintf(stderr, "Error: invalid input file path.\n");
        return EXIT_FAILURE;
    }


    fclose(fin);
    fclose(fout);
    return EXIT_SUCESS;
}

char argsvalid(int argc, char* argv[]) {
    
}
