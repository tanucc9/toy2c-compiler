#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

bool yesOrNo;


float add(float x, float y);
float sub(float x, float y);
float mul(float x, float y);
float div(float x, float y);


float add(float x, float y){
return x + y;
}

float sub(float x, float y){
return x - y;
}

float mul(float x, float y){
return x * y;
}

float div(float x, float y){
float result;
if (y == 0) {
printf("Impossibile dividere per zero ");
result = 0;
}
else {
result = x / y;
}
return result;
}

int main(){
yesOrNo = true;
float a, b, res;
int kind;
printf("pre ");
while (yesOrNo) {
printf("\n[1] Addizione\n ");
printf("[2] Sottrazione\n ");
printf("[3] Multiplicazione\n ");
printf("[4] Divisione\n\n>  ");
scanf("%d", &kind);
printf("\nFornisci il primo numero:  ");
scanf("%f", &a);
printf("Fornisci il secondo numero:  ");
scanf("%f", &b);
if (kind == 1) {
printf("Resultato:  %f ", add ( a, b));
}
else if (kind == 2) {
printf("Resultato:  %f ", sub ( a, b));
}
else if (kind == 3) {
printf("Resultato:  %f ", mul ( a, b));
}
else if (kind == 4) {
printf("Resultato:  %f ", div ( a, b));
}
printf("\nVuoi continuare? (1 si, 0 no):  ");
scanf("%d", &yesOrNo);
}
return 0;
}
