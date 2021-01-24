
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.rowset.internal.Row;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SemanticAnalisys implements Visitor{

    private Document document;
    private ArrayList<ArrayList<RowTable>> typeEnvironment;

    public SemanticAnalisys () throws ParserConfigurationException {
        //TODO
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    public void enterScope(ArrayList<RowTable> table){
        this.typeEnvironment.add(table);
        }

    public ArrayList<RowTable> lookup(String symbol, String kind){

        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
            for(RowTable rowt : this.typeEnvironment.get(i)){
                if(rowt.getSymbol().equals(symbol) && rowt.getKind().equals(kind)) return this.typeEnvironment.get(i);
            }
        }

        return null;
    }
    public void addId(RowTable rt){
        this.typeEnvironment.get(this.typeEnvironment.size()-1).forEach(rowTable -> {
            if (rowTable.getSymbol().equals(rt.getSymbol()) && rowTable.getKind().equals(rt.getKind())){
                throw new Error("L'id' "+ rt.getSymbol() +" e' stata già dichiarata.");
            } else {
                this.typeEnvironment.get(this.typeEnvironment.size()-1).add(rt);
            }
        });
    }
    public boolean probe(String symbol, String kind){
        for(int i = this.typeEnvironment.size()-1; i>=0; i--) {
              for(RowTable rowt : this.typeEnvironment.get(i)){
                  if(rowt.getSymbol().equals(symbol) && rowt.getKind().equals(kind)) return true;
              }
            }
        return false;
    }
    public void exitScope(){
        this.typeEnvironment.remove(this.typeEnvironment.size()-1);
    }

    public String isCompatibleType(String operazione, String type1, String type2) {

        switch (operazione) {
            //TODO
            case "math_operators":
                if(type1.equals("float") && type2.equals("float")) return "float";
                if(type1.equals("int") && type2.equals("int")) return "int";
                if(type1.equals("float") || type2.equals("float")) return "float";
                throw new Error("Non è possibile effettuare l'addizione tra "+ type1 +" e "+ type2);
            case "boolean_operators":
                if(type1.equals("bool") && type2.equals("bool")) return "bool";
                else throw new Error("Non è possibile effettuare l'operazione booleana tra "+ type1 +" e "+ type2);
            case "boolean_not":
                if(type1.equals("bool")) return "bool";
                else throw new Error("Non è possibile effettuare l'operazione booleana con "+ type1);
            case "uminus":
                String res = this.getResultUminusType(type1);
                if (res != null) {
                    throw new Error("Non è possibile aggiungere uminus con tipo "+ type1);
                }

        }

        //TODO
        return "";
    }

    private String getResultUminusType(String type) {
        if (type.equals("int")) return "int";
        if(type.equals("float")) return "float";
        String[] resType = type.split("->")[1].split(",");
        if(resType.length == 1 && ( resType[0].equals("float") || resType[0].equals("int") )) return resType[0];

        return null;
    }

    @Override
    public Object visit(ProgramOP p) {
        this.enterScope(p.getGlobalTable());
        for (VarDeclOP var :p.getVarDeclList() ) {
            boolean acc = ((boolean) var.accept(this));
        }
        for (ProcOP var: p.getProcList()) {
            boolean acc = ((boolean) var.accept(this));
        }

        this.exitScope();

        return p;
    }

    @Override
    public Object visit(AndOP a) {
        RowTable expr1= (RowTable) a.getE().accept(this);
        RowTable expr2= (RowTable) a.getE1().accept(this);

        String resultType = this.isCompatibleType("boolean_operators", expr1.getType(), expr2.getType());

        a.getRt().setType(resultType);
        return a.getRt();
    }

    @Override
    public Object visit(AssignOP a) {
        for(Id id : a.getIlist()) {
            if(!this.probe(id.getId(), "var") ) throw new Error("La variabile " + id.getId() +" non è stata dichiarate");
        }
        for(Expr e : a.getElist()) {
            Object o = e.accept(this);
            //TODO
        }

        //TODO
        return true;
    }

    @Override
    public Object visit(BodyOP b) {
        for(Stat s:b.getStatList()){
            Element stat= (Element) s.accept(this);
            //TODO
        }

        return bodyop;
    }

    @Override
    public Object visit(CallProcOP cp) {
        ArrayList<RowTable> tableCp = this.lookup(cp.getVal(), "method");
        if(tableCp !=null) {
            for(RowTable row : tableCp) {
                if(row.getSymbol().equals(cp.getVal()) && row.getKind().equals("method")) cp.setRt(row);
            }
        } else throw new Error ("Il proc"+ cp.getVal() +" non è stato dichiarato.");

        if(cp.getElist() != null ) {
            ArrayList<String> parType= new ArrayList<String>();
            for(Expr e : cp.getElist()) {
                RowTable rt = (RowTable) e.accept(this);
                if(rt.getKind().equals("method")) {
                    ArrayList<RowTable> table = this.lookup(e.getCp().getVal(), "method");
                    if(table !=null) {
                        for(RowTable row : table) {
                            if(row.getSymbol().equals(e.getCp().getVal()) && row.getKind().equals("method")) e.setRt(row);
                        }
                        String[] resType = e.getRt().getType().split("->")[1].split(",");
                        for(String type : resType) {
                            parType.add(type);
                        }
                    } else throw new Error ("Il proc"+ e.getCp().getVal() +" non è stato dichiarato.");
                } else {
                    parType.add(rt.getType());
                }
            }

            String[] parTypeCp = cp.getRt().getType().split("->")[0].split(",");

            ArrayList<String> parTypeCpList = new ArrayList<String>();
            for (String s : parTypeCp) {
                parTypeCpList.add(s);
            }

            if(parType.size() != parTypeCpList.size()) throw new Error("Il numero dei parametri passati al proc"+ cp.getVal() +" non corrisponde al numero dei parametri attesi.");
            if (!parType.equals(parTypeCpList)) throw new Error(" I tipi dei parametri passati al proc"+ cp.getVal() +" non corrispondono a quelli attesi. ");

        }
        return cp.getRt();
    }

    @Override
    public Object visit(DivOP d) {
        RowTable expr1= (RowTable) d.getE().accept(this);
        RowTable expr2= (RowTable) d.getE1().accept(this);

        String resultType = this.isCompatibleType("math_operators",expr1.getType(), expr2.getType());

        d.getRt().setType(resultType);
        return d.getRt();
    }

    @Override
    public Object visit(ElifOP c) {
        RowTable rt= (RowTable) c.getE().accept(this);
        if(rt.getType().equals("boolean")){
            Object bodyop= c.getsList().accept(this);
            //TODO
        }else throw new Error("La condizione deve essere di tipo boolean");

        return true;
    }

    @Override
    public Object visit(ElseOP c) {
        Object bodyop= c.getsList().accept(this);
        return true;
    }

    @Override
    public Object visit(EqualsOP d) {
        Element eqOP= document.createElement("EqualsOP");
        Object e= d.getE().accept(this);
        if(e instanceof String){eqOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){eqOP.appendChild((Element)e);}
        Object e1= d.getE1().accept(this);
        if(e1 instanceof String){eqOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){eqOP.appendChild((Element)e1);}
        return eqOP;
    }

    @Override
    public Object visit(Expr e) {
        Element exprOP = document.createElement("ExprOP");
        if(e.getCp() != null){
            Element el= (Element) e.getCp().accept(this);
            exprOP.appendChild(el);
            return exprOP;
        }

        return null;
    }

    @Override
    public Object visit(GreaterEqualsOP ge) {
        Element geOP= document.createElement("GreaterEqualsOP");
        Object e= ge.getE().accept(this);
        if(e instanceof String){geOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){geOP.appendChild((Element)e);}
        Object e1= ge.getE1().accept(this);
        if(e1 instanceof String){geOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){geOP.appendChild((Element)e1);}
        return geOP;
    }

    @Override
    public Object visit(GreaterThanOP gt) {
        Element gtOP= document.createElement("GreaterThanOP");
        Object e= gt.getE().accept(this);
        if(e instanceof String){gtOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){gtOP.appendChild((Element)e);}
        Object e1= gt.getE1().accept(this);
        if(e1 instanceof String){gtOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){gtOP.appendChild((Element)e1);}
        return gtOP;
    }

    @Override
    public Object visit(Id id) {
        ArrayList<RowTable> table = this.lookup(id.getId(), "var");
        if(table == null) throw new Error("La variabile " + id.getId() +" non è stata dichiarate");
        else {
            for (RowTable rt : table ) {
                if(rt.getSymbol().equals(id.getId())) return rt;
            }
        }

        return null;
    }

    @Override
    public Object visit(IdListInitOP x) {
        Element idInitOP=document.createElement("IdInitOP");

        if(x.getExpr() != null) {
            String l=x.getId().getId();
            //TODO
            Object r=x.getExpr().accept(this);
            x.getRt().setSymbol(l);
            x.getRt().setType(null);/*r.getType TIPOOOOO*/


        }
        else {
            String l=(String)x.getId().accept(this);
            x.getRt().setSymbol(l);
        }
        return x.getRt();
    }

    @Override
    public Object visit(IfOP c) {
        Element ifOP= document.createElement("IfOP");
        RowTable rt= (RowTable)c.getE().accept(this);
        if(rt.getType().equals("boolean")){
            Object bodyop=(Object) c.getsList().accept(this);
            //TODO
        }
        else throw new Error("Il tipo della condizione deve essere boolean ");

        for(ElifOP elif: c.getElList()){
            //TODO
            Element elf= (Element)elif.accept(this);
        }
        if(c.getEl()!=null) {
            //TODO
            Element elseop =(Element) c.getEl().accept(this);
        }
        return ifOP;
    }

    @Override
    public Object visit(LessEqualsOP le) {
        Element leOP= document.createElement("LessEqualsOP");
        Object e= le.getE().accept(this);
        if(e instanceof String){leOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){leOP.appendChild((Element)e);}
        Object e1= le.getE1().accept(this);
        if(e1 instanceof String){leOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){leOP.appendChild((Element)e1);}
        return leOP;


    }


    @Override
    public Object visit(LessThanOP lt) {
        Element ltOP= document.createElement("LessThanOP");
        Object e= lt.getE().accept(this);
        if(e instanceof String){ltOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){ltOP.appendChild((Element)e);}
        Object e1= lt.getE1().accept(this);
        if(e1 instanceof String){ltOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){ltOP.appendChild((Element)e1);}
        return ltOP;
    }

    @Override
    public Object visit(MinusOP m) {
        RowTable expr1= (RowTable) m.getE().accept(this);
        RowTable expr2= (RowTable) m.getE1().accept(this);

        String resultType = this.isCompatibleType("math_operators",expr1.getType(), expr2.getType());

        m.getRt().setType(resultType);
        return m.getRt();
    }

    @Override
    public Object visit(NotEqualsOP ne) {
        Element neOP= document.createElement("NotEqualsOP");
        Object e= ne.getE().accept(this);
        if(e instanceof String){neOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){neOP.appendChild((Element)e);}
        Object e1= ne.getE1().accept(this);
        if(e1 instanceof String){neOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){neOP.appendChild((Element)e1);}
        return neOP;
    }

    @Override
    public Object visit(NotOP n) {
        RowTable expr= (RowTable) n.getNe().accept(this);


        String resultType = this.isCompatibleType("boolean_not", expr.getType(), null);

        n.getRt().setType(resultType);
        return n.getRt();
    }

    @Override
    public Object visit(OrOP or) {
        RowTable expr1= (RowTable) or.getE().accept(this);
        RowTable expr2= (RowTable) or.getE1().accept(this);

        String resultType = this.isCompatibleType("boolean_operators", expr1.getType(), expr2.getType());

        or.getRt().setType(resultType);
        return or.getRt();
    }

    @Override
    public Object visit(ParDeclOP p) {
        ArrayList<RowTable> rt= new ArrayList<RowTable>();
        RowTable rowt= new RowTable();
        for(Id id : p.getIdList()) {
            String symbol= id.getId();
            rowt.setType(p.getType());
            rowt.setSymbol(symbol);
            rowt.setKind("var");
            rt.add(rowt);
        }

        return rt;
    }

    @Override
    public Object visit(PlusOP p) {
        RowTable expr1= (RowTable) p.getE().accept(this);
        RowTable expr2= (RowTable) p.getE1().accept(this);

        String resultType = this.isCompatibleType("math_operators",expr1.getType(), expr2.getType());

        p.getRt().setType(resultType);
        return p.getRt();
    }

    @Override
    public Object visit(ProcBodyOP pb) {
        for (VarDeclOP var : pb.getVdList() ) {
            boolean acc= (boolean) var.accept(this);

        }

        if(pb.getsList()!=null) {
            Element bodyOP = (Element) pb.getsList().accept(this);
            //TODO
            procBodyOP.appendChild(bodyOP);
        }
        if(pb.getRe() != null) {
            Element exprList = document.createElement("ExprOPList");
            for(Expr e : pb.getRe()) {
                Element exprOP= document.createElement("ExprOP");
                Object o=e.accept(this);
                if(o instanceof String){ exprOP.appendChild(document.createTextNode(o.toString()));}
                if(o instanceof Element){ exprOP.appendChild((Element)o);}
                exprList.appendChild(exprOP);
            }
            Element resultType=document.createElement("ResultType");
            resultType.appendChild(exprList);
            procBodyOP.appendChild(resultType);
        }
        //TODO

        return procBodyOP;
    }

    @Override
    public Object visit(ProcOP p) {
        String idProc=p.getId().getId();
        ArrayList<RowTable> parDeclOP= new ArrayList<RowTable>();
        if(p.getPdList() != null) {
            for (ParDeclOP parDecl  : p.getPdList()) {
                parDeclOP= (ArrayList<RowTable>) parDecl.accept(this);
                //parDecl.getRt().setType(parDecl.getType());
            }
        }

        String resType = "->";
        for (String s: p.getRtList()) {
            resType.concat(s+",");
        }
        String parListType= "";
        parDeclOP.forEach(rowTable -> {
            parListType.concat(rowTable.getSymbol()+",");
        });

        parListType.concat(resType);

        p.getRowT().setType(parListType);
        p.getRowT().setKind("method");
        p.getRowT().setSymbol(idProc);

        this.addId(p.getRowT());

        parDeclOP.forEach(rowTable -> {
            p.getLocalTable().add(rowTable);
        });
        this.enterScope(p.getLocalTable());

        //TODO
        p.getProcBodyOP().accept(this);

        this.exitScope();

        return true;
    }

    @Override
    public Object visit(ReadOP c) {
        for(Id i:c.getIdList()){
            if(!this.probe(i.getId(), "var") ) throw new Error("La variabile " + i.getId() +" non è stata dichiarate");
        }
        return true;
    }

    @Override
    public Object visit(ReturnExprsOP c) {
        Element returnExpr= document.createElement("ReturnExpr");
        Element exprList= document.createElement("ExprListOP");
        for(Expr e : c.getExprList()) {
            Element exprOP= document.createElement("ExprOP");
            Object o=e.accept(this);
            if(o instanceof String){ exprOP.appendChild(document.createTextNode(o.toString()));}
            if(o instanceof Element){ exprOP.appendChild((Element)o);}
            exprList.appendChild(exprOP);
        }
        returnExpr.appendChild(exprList);

        return returnExpr;
    }

    @Override
    public Object visit(Stat s) {
        Element stat = document.createElement("Stat");
        if(s.getCp() != null){
            Element el= (Element) s.getCp().accept(this);
            stat.appendChild(el);
            return stat;
        }

        return null;
    }

    @Override
    public Object visit(TimesOP t) {
        RowTable expr1= (RowTable) t.getE().accept(this);
        RowTable expr2= (RowTable) t.getE1().accept(this);

        String resultType = this.isCompatibleType("math_operators",expr1.getType(), expr2.getType());

        t.getRt().setType(resultType);
        return t.getRt();
    }

    @Override
    public Object visit(UMinusOP u) {
        RowTable rt = (RowTable) u.getE().accept(this);
        String res = this.isCompatibleType("uminus", rt.getType(), null);
        u.getRt().setType(res);

        return u.getRt();
    }

    @Override
    public Object visit(VarDeclOP c) {

         for(IdListInitOP idList : c.getIdListInit()) {
             RowTable idInitOP= (RowTable) idList.accept(this) ;
             idList.getRt().setType(c.type);
             idList.getRt().setSymbol(idInitOP.getSymbol());
             idList.getRt().setKind("var");

             if(idInitOP.getType()==null || idInitOP.getType().equals(c.type)){
                 addId(idList.getRt());
             }else throw new Error("I tipo di "+idInitOP.getSymbol()+" non è compatibile con "+ idInitOP.getType());



        }

        return true;
    }

    @Override
    public Object visit(WhileOP c) {
        if(c.getsList1() != null ) {
            Object bd=c.getsList1().accept(this);
        }
        RowTable rt = (RowTable) c.getE().accept(this);
        if(rt.getType().equals("boolean")){
            Object bd=c.getsList2().accept(this);
        }else throw new Error("Il tipo della condizione deve essere boolean");

        return true;
    }

    @Override
    public Object visit(WriteOP c) {
        for(Expr e : c.getExprList()) {
            Element exprOP= document.createElement("ExprOP");
            RowTable rt= (RowTable) e.accept(this);
            if(!rt.getType().equals("string"))throw new Error("Write ammette solo tipi string");
        }
        return true;
    }

    @Override
    public Object visit(StringConst sc) {
        sc.getRt().setType("string");
        return sc.getRt();
    }

    @Override
    public Object visit(IntConst ic) {
        ic.getRt().setType("int");
        return ic.getRt();
    }

    @Override
    public Object visit(Bool b) {
        if(b.isB()) b.getRt().setType("true");
        else b.getRt().setType("false");
        return b.getRt();
    }

    @Override
    public Object visit(Null c) {
        c.getRt().setType(c.getN());
        return c.getRt();
    }

    @Override
    public Object visit(FloatConst fc) {
        fc.getRt().setType("float");
        return fc.getRt();
    }
}
