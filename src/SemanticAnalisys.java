
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
    public ArrayList<RowTable> lookup(RowTable rt){
        for(ArrayList<RowTable> table : this.typeEnvironment){
            if(table.contains(rt)) return table;
        }
        return null;
    }
    public void addId(RowTable rt){
        this.typeEnvironment.get(this.typeEnvironment.size()-1).forEach(rowTable -> {
            if (rowTable.getSymbol().equals(rt.getSymbol()) && rowTable.getKind().equals(rt.getKind())){
                throw new Error("La variabile "+ rt.getSymbol() +" e' stata già dichiarata.");
            } else {
                this.typeEnvironment.get(this.typeEnvironment.size()-1).add(rt);
            }
        });
    }
    public boolean probe(String symbol){
        for(ArrayList<RowTable> table : this.typeEnvironment){
              for(RowTable rowt : table){
                  if(rowt.getSymbol().equals(symbol)) return true;
              }
            }
        throw new Error("La variabile " + symbol +" non è stata dichiarate");
    }
    public void exitScope(){
        this.typeEnvironment.remove(this.typeEnvironment.size()-1);
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

        return p;
    }

    @Override
    public Object visit(AndOP a) {
        Element andOP = document.createElement("AndOP");
        Object e= a.getE().accept(this);
        if(e instanceof String){andOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){andOP.appendChild((Element)e);}
        Object e1= a.getE1().accept(this);
        if(e1 instanceof String){andOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){andOP.appendChild((Element)e1);}
        return andOP;
    }

    @Override
    public Object visit(AssignOP a) {
        for(Id id : a.getIlist()) {
            String s= (String) id.accept(this);
            this.probe(s);
        }
        for(Expr e : a.getElist()) {
            Object o = e.accept(this);
            //TODO
        }

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
        Element callProcOP =document.createElement("CallProcOP");
        callProcOP.appendChild(document.createTextNode("(ID,\""+cp.getVal()+"\")"));

        if(cp.getElist() != null ) {
            Element exprOPList= document.createElement("ExprOPList");
            for(Expr e : cp.getElist()) {
                Element exprOP= document.createElement("ExprOP");
                Object o = e.accept(this);
                if(o instanceof String){exprOP.appendChild(document.createTextNode(o.toString()));}
                if(o instanceof Element){exprOP.appendChild((Element) o);}
                exprOPList.appendChild(exprOP);
            }
            callProcOP.appendChild(exprOPList);
        }
        return callProcOP;
    }

    @Override
    public Object visit(DivOP d) {
        Element divOP= document.createElement("DivOP");
        Object e= d.getE().accept(this);
        if(e instanceof String){divOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){divOP.appendChild((Element)e);}
        Object e1= d.getE1().accept(this);
        if(e1 instanceof String){divOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){divOP.appendChild((Element)e1);}
        return divOP;
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
        return "(ID, \""+id.getId()+"\")";
    }

    @Override
    public Object visit(IdListInitOP x) {
        Element idInitOP=document.createElement("IdInitOP");

        if(x.getExpr() != null) {
            String l=(String)x.getId().accept(this);
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
        Element mOP= document.createElement("MinusOP");
        Object e= m.getE().accept(this);
        if(e instanceof String){mOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){mOP.appendChild((Element)e);}
        Object e1= m.getE1().accept(this);
        if(e1 instanceof String){mOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){mOP.appendChild((Element)e1);}
        return mOP;
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
        Element notOP= document.createElement("NotOP");
        Object o=n.getNe().accept(this);
        if(o instanceof String){ notOP.appendChild(document.createTextNode(o.toString()));}
        if(o instanceof Element){notOP.appendChild((Element)o);}

        return notOP;
    }

    @Override
    public Object visit(OrOP or) {
        Element orOP= document.createElement("OrOP");
        Object e= or.getE().accept(this);
        if(e instanceof String){orOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){orOP.appendChild((Element)e);}
        Object e1= or.getE1().accept(this);
        if(e1 instanceof String){orOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){orOP.appendChild((Element)e1);}
        return orOP;
    }

    @Override
    public Object visit(ParDeclOP p) {
        ArrayList<RowTable> rt= new ArrayList<RowTable>();
        RowTable rowt= new RowTable();
        for(Id id : p.getIdList()) {
            String symbol= (String)id.accept(this);
            rowt.setType(p.getType());
            rowt.setSymbol(symbol);
            rowt.setKind("var");
            rt.add(rowt);
        }

        return rt;
    }

    @Override
    public Object visit(PlusOP p) {
        Element plusOP= document.createElement("PlusOP");
        Object e= p.getE().accept(this);
        if(e instanceof String){plusOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){plusOP.appendChild((Element)e);}
        Object e1= p.getE1().accept(this);
        if(e1 instanceof String){plusOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){plusOP.appendChild((Element)e1);}
        return plusOP;
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

        return procBodyOP;
    }

    @Override
    public Object visit(ProcOP p) {
        String idProc=(String)p.getId().accept(this);
        ArrayList<RowTable> parDeclOP= new ArrayList<RowTable>();
        if(p.getPdList() != null) {
            for (ParDeclOP parDecl  : p.getPdList()) {
                parDeclOP= (ArrayList<RowTable>) parDecl.accept(this);
                //parDecl.getRt().setType(parDecl.getType());
            }
        }
        ArrayList<String> resTypeOP= new ArrayList<String>();

        for (String s: p.getRtList()) {
            resTypeOP.add(s);
        }
        ArrayList<String> parListType= new ArrayList<String>();
        parDeclOP.forEach(rowTable -> {
            parListType.add(rowTable.getType().toString());

        });
        ArrayList<ArrayList<String>> listType= new ArrayList<ArrayList<String>>();
        listType.add(parListType);
        listType.add(resTypeOP);
        p.getRowT().setType(listType);
        p.getRowT().setKind("method");
        p.getRowT().setSymbol(idProc);

        this.addId(p.getRowT());

        parDeclOP.forEach(rowTable -> {
            p.getLocalTable().add(rowTable);
        });
        this.enterScope(p.getLocalTable());

        return true;
    }

    @Override
    public Object visit(ReadOP c) {
        for(Id i:c.getIdList()){
            String s= i.accept(this).toString();
            this.probe(s);
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
        Element tOP= document.createElement("TimesOP");
        Object e= t.getE().accept(this);
        if(e instanceof String){tOP.appendChild(document.createTextNode(e.toString()));}
        if(e instanceof Element){tOP.appendChild((Element)e);}
        Object e1= t.getE1().accept(this);
        if(e1 instanceof String){tOP.appendChild(document.createTextNode(e1.toString()));}
        if(e1 instanceof Element){tOP.appendChild((Element)e1);}
        return tOP;
    }

    @Override
    public Object visit(UMinusOP u) {
        Element uminusOP =document.createElement("UminusOP");
        Object o = u.getE().accept(this);
        if(o instanceof String) uminusOP.appendChild(document.createTextNode(o.toString()));
        else uminusOP.appendChild((Element)o);

        return uminusOP;
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
        return "(STRING_CONST, \""+ sc.getS() +"\")";
    }

    @Override
    public Object visit(IntConst ic) {
        return "(INT_CONST, \""+ ic.getVal() +"\")";
    }

    @Override
    public Object visit(Bool b) {
        return "("+ b.isB()+")";
    }

    @Override
    public Object visit(Null c) {
        return c.getN();
    }

    @Override
    public Object visit(FloatConst fc) {
        return "(FLOAT_CONST, \""+ fc.getF() +"\")";
    }
}
