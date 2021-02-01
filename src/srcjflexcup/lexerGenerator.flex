/* JFlex example: part of Java language lexer specification */
import java_cup.runtime.*;
import java.util.ArrayList;


/**
* This class is a simple example lexer.
*/
%%
%cup
%unicode
%line
%column

%{
      StringBuffer string = new StringBuffer();
      //public ArrayList<String> SymbolTable= new ArrayList<String>();

      private Symbol generateToken(int type) {
        return new Symbol(type);
      }

      private Symbol generateToken(int type, Object value) {
            /*
              if(type==20 && !SymbolTable.contains(value.toString())){
                SymbolTable.add(value.toString());
              }
              if(type==6){
                  SymbolTable.add(value.toString());
              }
              */

              //return new Symbol(type, SymbolTable.indexOf(value.toString()));
              return new Symbol(type, value.toString());
      }
      /*
      private Symbol generateError(String value) throws Exception {
      }
      */


%}
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]

Identifier = [:jletter:][:jletterdigit:]*
IntegerLiteral = 0 | [1-9][0-9]*
FloatNumber = (0|[1-9][0-9]*)\.[0-9]*[1-9]+
StringConst = \" [^] \"

/* comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"


%state STRING
%state COMMENTS

%%
<YYINITIAL> {

  /* keywords */
  "if" { return generateToken(sym.IF); }
  "fi" { return generateToken(sym.FI); }
  "then" { return generateToken(sym.THEN); }
  "else" { return generateToken(sym.ELSE); }
  "elif" {return generateToken(sym.ELIF); }
  "while" { return generateToken(sym.WHILE); }
  "int" { return generateToken(sym.INT); }
  "float" { return generateToken(sym.FLOAT); }
  "string" {return generateToken(sym.STRING);}
  "bool" {return generateToken(sym.BOOL);}
  "proc" {return generateToken(sym.PROC);}
  "corp" {return generateToken(sym.CORP);}
  "void" {return generateToken(sym.VOID);}
  "do" {return generateToken(sym.DO);}
  "od" {return generateToken(sym.OD);}
  "readln" {return generateToken(sym.READ);}
  "write" {return generateToken(sym.WRITE);}
  "true" {return generateToken(sym.TRUE); }
  "false" {return generateToken(sym.FALSE); }
  "->" {return generateToken(sym.RETURN);}

   /*Operators*/
  "+" {return generateToken(sym.PLUS);}
  "-" {return generateToken(sym.MINUS);}
  "*" {return generateToken(sym.TIMES);}
  "/" {return generateToken(sym.DIV);}



  /* separators */
  "(" { return generateToken(sym.LPAR); }
  ")" { return generateToken(sym.RPAR); }
  "," { return generateToken(sym.COMMA); }
  ";" { return generateToken(sym.SEMI); }
  ":" { return generateToken(sym.COLON); }

  /* relop */
  "<" { return generateToken(sym.LT); }
  "<=" { return generateToken(sym.LE); }
  "=" { return generateToken(sym.EQ); }
  "<>" { return generateToken(sym.NE); }
  ">" { return generateToken(sym.GT); }
  ">=" { return generateToken(sym.GE); }
  ":=" { return generateToken(sym.ASSIGN); }
  "&&" { return generateToken(sym.AND); }
  "||" { return generateToken(sym.OR); }
  "!" { return generateToken(sym.NOT); }
  "null" { return generateToken(sym.NULL); }

  /* identifiers */
  {Identifier}          { return generateToken(sym.ID, yytext());}
  \" { string.setLength(0); yybegin(STRING); }

  /* literals */
  {IntegerLiteral}   { return generateToken(sym.INT_CONST, Integer.parseInt(yytext())); }
  {FloatNumber}   { return generateToken(sym.FLOAT_CONST, Double.parseDouble(yytext())); }


  /* whitespace */
  {WhiteSpace} { /* ignore */ }

  /* comments */
  "/*" {yybegin(COMMENTS);}
}

<STRING> {
\"          { yybegin(YYINITIAL);
                return generateToken(sym.STRING_CONST,
                string.toString()); }
<<EOF>>     { throw new Error("Errore! Stringa costante non completata.");}

[^\"\\]+    { string.append( yytext() ); }
\\t     { string.append("\t"); }
\\n     { string.append("\n"); }
\\r     { string.append("\r"); }
\\\"    { string.append("\""); }
\\      { string.append("\\"); }

}

<COMMENTS> {

"*/" { yybegin(YYINITIAL);}
<<EOF>>     {throw new Error("Errore! Commento non chiuso.");}
[^] { /* Ignore */ }
}



/* error fallback */
[^] { throw new Error("Errore lessicale su "+yytext()+" nella posizione "+yyline+":"+yycolumn+".");}

<<EOF>> {return new Symbol(sym.EOF);}