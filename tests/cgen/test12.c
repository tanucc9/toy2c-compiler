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
getPair_struct getPair_structVar0 = getPair ( );
x = getPair_structVar0.var0;
y = getPair_structVar0.var1;
res = getPair_struct getPair_structParamCP0 = getPair ( );
add ( getPair_structParamCP0.var0, getPair_structParamCP0.var1);
res = res + add ( x, getPair_struct getPair_structParamCP1 = getPair ( );
add ( getPair_structParamCP1.var0, getPair_structParamCP1.var1));
getPair_struct getPair_structVarWrite0 = getPair ( );
getPair_struct getPair_structVarWrite1 = getPair ( );
printf("%d  =>  %d | %d %d | %d | %d %d | %d  <=  %d ", true, res, getPair_structVarWrite0.var0, getPair_structVarWrite0.var1, getPair_struct getPair_structParamCP2 = getPair ( );
add ( getPair_structParamCP2.var0, getPair_structParamCP2.var1), getPair_structVarWrite1.var0, getPair_structVarWrite1.var1, res, false);
return 0;
}
