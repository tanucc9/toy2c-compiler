# How to use the compiler
Via the terminal, use toy2c.bat to compile the toy program.
To know the syntax of toy2c.bat, you can run the command: toy2c.bat --help. The path to the toy file to be compiled must be specified.

# Semantic Analysis
Most nodes return a Rowtable, i.e. an object containing symbol, type and kind.
Before returning the Rowtable, we bind it to the node on the tree so that we have it in the compiler's later stages.

The SymbolTable has an arraylist<rowtable> structure.
A globaltable object has been declared in the ProgramOp node and a localtable object in the ProcOP node.

In the SemanticAnalysis visitor, an instance variable has been used for the type environment and is of type arraylist<arraylist<rowtable>>.
Using the enterscope method, localtables and globaltables are added to the type environment, and using the exitscope method, they are removed.

With regard to compatibility between types, the choice was made to:
- Perform concatenation and comparison between string types (e.g.: string s := "A" + "B", while "A" < "B");
- Being able to perform operations and initialisation also between float and int types (e.g.: int x := 5.4, float f := 4, x := x + f).
- Uminus may also be placed before a function, but the latter must only have one return type (int or float).

The main must not contain any parameters and the return type must be void (unlike C, it does not accept int), and finally it cannot be invoked by other functions.

The null is seen as an empty string and is only compatible with string.

# Generating C code

The libraries we import are: #include <stdio.h> , #include <stdbool.h> , #include <string.h> \n\n";

The structure of the generated code is as follows:
- Import libraries;
- Declaration of structures;
- Declaration of global variables;
- Declaration of temporary variables;
- Declaration of methods;
- Implementation of methods;

Within the main, initialisations of global variables are performed.
The return type of the main is converted to int and returns 0.

Strings were handled as char *, and in some cases we used a char [] as a temporary variable for readln and concatenation between strings.

For the bool type, we imported the <stdbool.h> library

# More info

### C Generation 
EXPR
'code'-> codeC
'idProc' -> idfunction if it is callproc
'serviceInstr' -> service instructions for struct or string concatenation

### Semantic analysis
vardeclop -> idlistinitop may or may not have expr so the rowtable returning idlistinit to vardeclop will have the gettype set IF expr is present, otherwise null

EXPR
IF callproc, then it returns a rowtable containing type and kind. IF NOT callproc, the rowtable contains only type.

VARDECLOP
idlistinit returns a rowtable containing type = null IF expr is not present otherwise type
will contain the type of expr


### AST generation in XML

For xml generation we have used a library where an object containing the node's xml is created at each node.
In general we do the accept on the nodes and its value becomes the leaf of the parent node.

At the end of visiting all nodes, ProgramOp will return the root of the generated xml object which will then be written to a file.
