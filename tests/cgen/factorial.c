#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>



int factorial(int n);


int factorial(int n){
int result = 0;
if (n == 0) {
result = 1;
}
else {
result = n * factorial ( n - 1);
}
return result;
}

int main(){
int n;
printf("Enter n, or <= 0 to exit:  ");
scanf("%d", &n);
while (n > 0) {
printf("Factorial of  %d ", n);
printf(" is  %d \n ", factorial ( n));
n = n - 1;
}
return 0;
}
