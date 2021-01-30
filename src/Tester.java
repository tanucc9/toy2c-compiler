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
        cg.visit(astExt);



       /* GenerateXML xml = new GenerateXML();
        Document xml_generated = (Document)pOP.accept(xml);


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(xml_generated);
        StreamResult streamResult = new StreamResult(new File(System.getProperty("user.dir")+"\\albero_sintattico.xml"));

        transformer.transform(domSource, streamResult);*/
    }
}
