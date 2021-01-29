EXPR
pos0 codiceC e pos1 idfunzione se è callproc o null

ANALISI SEMANTICA
vardeclop -> idlistinitop può avere o non l'expr quindi il rowtable che ritorna idlistinit a vardeclop avrà il gettype
settato SE expr è presente altrimenti null

EXPR
SE callproc, allora ritorna un rowtable contenente type e kind. SE NON callproc, il rowtable contiene solo type.

VARDECLOP
idlistinit ritorna un rowtable che contiene type = null SE Expr non è presente altrimenti type
conterrà il tipo di expr
TODO

