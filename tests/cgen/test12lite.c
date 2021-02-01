#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>


typedef struct {
int var0;
int var1;
} getPair_struct;
typedef struct {
int var0;
int var1;
} getPairRec_struct;

getPair_struct getPair();
getPairRec_struct getPairRec();
int add(int a, int b);


getPair_struct getPair(){
getPair_struct getPair_structReturn;
getPair_structReturn.var0 = 1;
getPair_structReturn.var1 = 1;
return getPair_structReturn;
}

getPairRec_struct getPairRec(){
getPair_struct getPair_structVarRes0 = getPair ( );
getPairRec_struct getPairRec_structReturn;
getPairRec_structReturn.var0 = getPair_structVarRes0.var0;
getPairRec_structReturn.var1 = getPair_structVarRes0.var1;
return getPairRec_structReturn;
}

int add(int a, int b){
return a + b;
}

int main(){
int x, y;
int res;
getPairRec_struct getPairRec_structVar0 = getPairRec ( );
res = 10;
x = getPairRec_structVar0.var0;
y = getPairRec_structVar0.var1;
res = 1 + getPairRec_struct getPairRec_structParamCP0 = getPairRec ( );
add ( getPairRec_structParamCP0.var0, getPairRec_structParamCP0.var1);
printf("%d %d ", x, y);
return 0;
}
