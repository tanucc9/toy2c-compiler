@Echo Off
java -jar mauro-avolicino_es5_scg.jar %1 %2 %3
IF %errorlevel% NEQ 0 GOTO :EOF
IF "%~3" == "" GOTO emptyExe
gcc -pthread -lm -o %3 %2
%3.exe
:emptyExe
gcc -pthread -lm %2
a.exe

