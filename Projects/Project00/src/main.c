/** Alan Cham
 *  Intro to Computer Systems
 *  Project 0: Programming Best Practices
 *  January 8, 2017
 * 
 *  This source file comprises a program with the following functionality:
 *      1) remove white space
 *      2) remove comments
 *  
 *  See included README.txt for more details about usage and compilation */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define INPUT_EXT ".in"         /* input file extension */
#define OUTPUT_EXT ".out"       /* output file extension */ 
#define MAX_PATH 1000           /* maximum char-width of file path */
#define NC_OPTION "no-comments" /* command-line argument for no-comments */
#define MIN_ARGS 2              /* minimum argc value */
#define MAX_ARGS 3              /* maximum argc value */

char nocomments = 0;            /* indicates comment removal is on/off */
char inpath[MAX_PATH];          /* path of input file */
char outpath[MAX_PATH];         /* path of output file */

char argsvalid(int argc, char* argv[]);
void cpypath(char* dest, char* source);
void replaceext(char* str, char* to, char* from);

int main(int argc, char* argv[]) {
    FILE* fin, *fout;

    /* validate command-line input */
    if (!argsvalid(argc, argv)) {
        return EXIT_FAILURE;
    } else if ((fin = fopen(inpath, "r")) == NULL) {
        fprintf(stderr, "Error: invalid input file path \"%s\".\n", inpath);
        return EXIT_FAILURE;
    }
    /* prepare output FILE* */
    strcpy(outpath, inpath);
    replaceext(outpath, OUTPUT_EXT, INPUT_EXT);
    if ((fout = fopen(outpath, "w")) == NULL) {
        fclose(fin);
        fprintf(stderr, "Error: failed to create file \"%s\".\n", outpath);
        return EXIT_FAILURE;
    }
    printf("NoComment: %s\n", nocomments ? "on" : "off");
    printf("Inpath: %s\n", inpath);
    printf("Outpath: %s\n", outpath);
    fclose(fin);
    fclose(fout);
    return EXIT_SUCCESS;
}

/** replace the file extension in str, previously having extension from,
  * then having extension to */
void replaceext(char* str, char* to, char* from) {
    int lstr, lfrom;

    lstr = strlen(str);
    lfrom = strlen(from);
    strcpy(str+lstr-lfrom, to);
}

/** validate command-line input arguments
 *  returns 1 if valid, 0 otherwise. */
char argsvalid(int argc, char* argv[]) {
    char gotfile;
    int i, l;

    l = strlen(INPUT_EXT);
    if (argc > MAX_ARGS) {
        /* too many arguments */
        fprintf(stderr, "Error: too many input arguments (expected filename "
            "and optional argument \"%s\").\n", NC_OPTION);
        return 0;
    } else if (argc < MIN_ARGS) {
        /* too few arguments */
        fprintf(stderr, "Error: too few input arguments (expected filename "
            "and optional argument \"%s\").\n", NC_OPTION);
        return 0;        
    }
    for (gotfile = 0, i = 1; i < argc; i++) {
        if (!strcmp(NC_OPTION, argv[i]) && !nocomments) {
            /* set nocomments flag on */
            nocomments = 1;
        } else if (!gotfile) {
            /* set gotfile flag on */
            gotfile = 1;
            strcpy(inpath, argv[i]);
        } else {
            /* set gotfile flag on */
             fprintf(stderr, "Error: more than one filename provided: "
                "\"%s\" and \"%s\".\n", inpath, argv[i]);
            return 0;
        }
    }
    if (!gotfile) {
        /* no filename found */
        fprintf(stderr, "Error: received optional argument \"%s\" but no "
            "filename.\n", NC_OPTION);
        return 0;
    } else if (strcmp(INPUT_EXT, inpath+strlen(inpath)-l)) {
        /* input file does not end in correct extension */
        fprintf(stderr, "Error: input filename \"%s\" does not end in"
            " \"%s\".\n", inpath, INPUT_EXT);
        return 0;
    }
    return 1;
}

/** copy path component of source into dest */
void cpypath(char* dest, char* source) {
    char* last, buffer[MAX_PATH];

    strcpy(buffer, source);
    last = strrchr(buffer, '/');
    if (!last++) {
        last = buffer;
    }
    *last = '\0';
    strcpy(dest, buffer);
}
