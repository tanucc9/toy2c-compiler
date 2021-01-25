import java.io.*;
import java_cup.runtime.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Tester {
    public static void main(String[] args) throws Exception {

        File file=new File(args[0]);
        InputStream in = new FileInputStream(file);
        Reader reader = new InputStreamReader(in);

        parser p = new parser(new Yylex(reader));
        ProgramOP pOP= (ProgramOP) p.parse().value;


        SemanticAnalisys sa = new SemanticAnalisys();
        sa.visit(pOP);



       /* GenerateXML xml = new GenerateXML();
        Document xml_generated = (Document)pOP.accept(xml);


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(xml_generated);
        StreamResult streamResult = new StreamResult(new File(System.getProperty("user.dir")+"\\albero_sintattico.xml"));

        transformer.transform(domSource, streamResult);*/
    }
}
