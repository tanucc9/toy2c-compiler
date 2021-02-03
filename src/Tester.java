import org.w3c.dom.Document;
import visitors.SemanticAnalisys;

import java.io.*;

import nodes.ProgramOP;
import visitors.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Tester {
    public static void main(String[] args) throws Exception {
       Reader reader = null;     
        
        try {
                File file = new File(args[0]);
                InputStream in = new FileInputStream(file);
                reader = new InputStreamReader(in);
            } catch (FileNotFoundException e) {
                throw new Error ("Il file toy che stai cercando di compilare non è stato trovato.\n" +
                        "Assicurati di aver inserito correttamente il path.");
            }
        
        
        //Parser & lexer
        parser p = new parser(new Yylex(reader));
        //p.debug_parse();
        ProgramOP pOP= (ProgramOP) p.parse().value;

        /*
        //Generazione ast in xml
        GenerateXML xml = new GenerateXML();
        Document xml_generated = (Document)pOP.accept(xml);

        //Creazione file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(xml_generated);
        StreamResult streamResult = new StreamResult(new File(System.getProperty("user.dir")+"\\albero_sintattico.xml"));
        transformer.transform(domSource, streamResult);
        */

        //Analisi semantica e ast esteso
        SemanticAnalisys sa = new SemanticAnalisys();
        ProgramOP astExt = (ProgramOP) sa.visit(pOP);

        //Generazione codice c
        CGenerator cg= new CGenerator();
        String fileC = (String) cg.visit(astExt);

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
