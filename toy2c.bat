@Echo Off
IF "%~1" == "--help" (
    echo *** HELP - TOY2C ***
    echo *  Argomento 1, obbligatorio: nome file toy da compilare. ES. programmi_toy/file.toy
    echo *  Argomento 2, opzionale: nome file c di output generato dal toy. ES. prog.c
    echo *  Argomento 3, opzionale: nome file exe generato dal c. ES. out
    GOTO :EOF
)
IF "%~1" == "" GOTO :noToyFile
IF "%~2" == "" (
SET C=prog.c
GOTO :checkOut
)
SET C=%2%

:checkOut
IF "%~3" == "" (
SET O=out
GOTO :jarToy
)
SET O=%3%

:jarToy
java -jar out/artifacts/mauro_avolicino_es5_scg_jar2/mauro-avolicino_es5_scg.jar %1 %C%
IF %errorlevel% NEQ 0 GOTO :EOF
echo Compilazione in corso...
gcc -pthread -lm -o %O% %C%
%O%.exe
GOTO :EOF

:noToyFile
echo E' necessario passare almeno il nome del file.toy che si vuole compilare.

