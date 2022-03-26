# Come usare il compilatore
Tramite terminale utilizzare il toy2c.bat per la compilazione del programma toy.
Per conoscere la sintassi del toy2c.bat è possibile lanciare il comando: toy2c.bat --help. È obbligatorio specificare il path del file toy da compilare.

# Analisi semantica
La maggior parte dei nodi ritorna un Rowtable, ovvero un oggetto contenente symbol, type e kind.
Prima di ritornare il Rowtable, leghiamo quest'ultimo al nodo sull'albero in modo da averlo nelle fasi successive del compilatore.

Le SymbolTable hanno una struttura di arraylist<rowtable>.
E' stato dichiarato un oggetto globaltable nel nodo ProgramOp e un oggetto localtable nel nodo ProcOP.

Nel visitor SemanticAnalysis è stata utilizzata una variabile di istanza per il type environment ed è di tipo arraylist<arraylist<rowtable>>
Tramite il metodo enterscope vengono aggiunte nel type environment le localtable e le globaltable e tramite il metodo exitscope vengono rimosse.

In merito all compatibilità tra tipi è stato scelto di:
- Effettuare concatenazione e comparazione tra tipi string (es: string s := "A" + "B",  while "A" < "B");
- Poter eseguire operazioni e inizializzazione anche tra tipi float e int (es: int x := 5.4, float f := 4, x := x + f).
- Uminus può essere posto anche prima di una funzione, ma quest'ultima deve avere un solo tipo di ritorno (int o float).

Il main non deve contenere parametri e tipo di ritorno deve essere void (a differenza di C, non accetta int), ed infine non può essere invocato da altre funzioni.

Il null viene visto come una stringa vuota ed è compatibile solo con string.

# Generazione codice C

Le librerie che importiamo sono: #include <stdio.h> , #include <stdbool.h> , #include <string.h> \n\n";

La struttura del codice generato è la seguente:
- Import librerie;
- Dichiarazione delle strutture;
- Dichirazione variabili globali;
- Dichiarazione delle variabili temporanee;
- Dichiarazione dei metodi;
- Implementazione dei metodi;

All'interno del main vengono fatte le inizializzazioni delle variabili globali.
Il tipo di ritorno del main viene convertito in int e ritorna 0.

Le stringhe sono state gestite come char *, e in alcuni casi abbiamo utilizzato un char [] come variabile temporanea per il readln e la concatenazione tra stringhe.

Per il tipo bool abbiamo importato la libreria <stdbool.h>


# Altre info

## Generazione C 
EXPR
'code'-> codiceC
'idProc' -> idfunzione se è callproc
'serviceInstr' -> istruzioni di servizio per le struct o per la concatenazione tra stringhe

## Analisi semantica
vardeclop -> idlistinitop può avere o non l'expr quindi il rowtable che ritorna idlistinit a vardeclop avrà il gettype settato SE expr è presente, altrimenti null

EXPR
SE callproc, allora ritorna un rowtable contenente type e kind. SE NON callproc, il rowtable contiene solo type.

VARDECLOP
idlistinit ritorna un rowtable che contiene type = null SE Expr non è presente altrimenti type
conterrà il tipo di expr


## Generazione AST in XML

Per la generazione dell'xml abbiamo utilizzato una libreria dove in ogni nodo si crea un oggetto contenente l'xml del nodo.
In generale si fa l'accept sui nodi e il suo valore diventa la foglia del nodo padre.

Alla fine della visita di tutti i nodi, ProgramOp ritornerà la radice dell'oggetto xml generato che verrà poi scritto in un file.
