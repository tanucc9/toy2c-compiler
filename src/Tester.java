import visitors.SemanticAnalisys;

import java.io.*;

import nodes.ProgramOP;
import visitors.*;

public class Tester {
    public static void main(String[] args) throws Exception {

        File file=new File(args[0]);
        InputStream in = new FileInputStream(file);
        Reader reader = new InputStreamReader(in);

        parser p = new parser(new Yylex(reader));
        //p.debug_parse();
        ProgramOP pOP= (ProgramOP) p.parse().value;


        SemanticAnalisys sa = new SemanticAnalisys();
        ProgramOP astExt = (ProgramOP) sa.visit(pOP);

        CGenerator cg= new CGenerator();
        String fileC = (String) cg.visit(astExt);

//        String codIntermedio=null;
//        for(int i=0; i < args.length; i++){
//            if(args[i].equals("-intc")) codIntermedio=args[i+1];
//        }
//
//        String fileOutDir = "";
//        if(codIntermedio != null){
//            fileOutDir = System.getProperty("user.dir") + "/" + codIntermedio;
//        }else{
//            fileOutDir = System.getProperty("user.dir") + "/codIntermedio.c" ;
//        }

        String fileOutDir = System.getProperty("user.dir") + "/" + args[1];
        File f = new File(fileOutDir);
        if(!f.exists()) {
            if (f.createNewFile()) {
                FileWriter fw = new FileWriter(fileOutDir);
                fw.write(fileC);
                fw.close();
            } else {
                throw new Error("Errore sulla generazione del file intermedio.");
            }

        } else {
            FileWriter fw = new FileWriter(fileOutDir);
            fw.write(fileC);
            fw.close();
        }
   }
}
