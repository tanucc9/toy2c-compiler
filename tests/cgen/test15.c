#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>





int main(){
char *a = "ciao", *b = "tutti";
printf("Fornisci una stringa:  ");
char a_tempVarReadString[100];
scanf(" %[^\n]s", a_tempVarReadString);
a = a_tempVarReadString;
printf("Fornisci un'altra stringa:  ");
char b_tempVarReadString[100];
scanf(" %[^\n]s", b_tempVarReadString);
b = b_tempVarReadString;
printf("Input:  %s ,  %s \n ", a, b);
if (a < b) {
printf("a < b ");
}
else if (a == b) {
printf("a == b ");
}
else if (a > b) {
printf("a > b ");
}
else {
printf("dead code ");
}
return 0;
}
